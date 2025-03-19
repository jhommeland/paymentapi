package no.jhommeland.paymentapi.service;

import com.adyen.model.checkout.*;
import jakarta.persistence.EntityNotFoundException;
import no.jhommeland.paymentapi.dao.AdyenPaymentsApiDao;
import no.jhommeland.paymentapi.dao.MerchantRepository;
import no.jhommeland.paymentapi.dao.TransactionRepository;
import no.jhommeland.paymentapi.model.*;
import no.jhommeland.paymentapi.util.PaymentUtil;
import no.jhommeland.paymentapi.util.UrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Service class responsible for interacting with the Adyen payment API.
 * Provides methods for retrieving payment methods based on input parameters
 * and initializes the Adyen API client.
 */
@Service
public class PaymentsService {

    private final Logger logger = LoggerFactory.getLogger(PaymentsService.class);

    AdyenPaymentsApiDao adyenPaymentsApiDao;

    MerchantRepository merchantRepository;

    TransactionRepository transactionRepository;

    public PaymentsService(AdyenPaymentsApiDao adyenPaymentsApiDao, MerchantRepository merchantRepository, TransactionRepository transactionRepository) {
        this.adyenPaymentsApiDao = adyenPaymentsApiDao;
        this.merchantRepository = merchantRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionModel> getTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc();
    }

    public PaymentMethodsResponse getPaymentMethods(PaymentMethodsModel requestModel) {

        MerchantModel merchantModel = merchantRepository.findById(requestModel.getMerchantId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        //Create Amount Object
        Amount amountObject = new Amount()
                .currency(requestModel.getCurrency())
                .value(Long.parseLong(requestModel.getAmount()));

        //Create Payment Methods Object
        PaymentMethodsRequest paymentMethodsRequest = new PaymentMethodsRequest()
                .amount(amountObject)
                .merchantAccount(merchantModel.getAdyenMerchantAccount())
                .countryCode(requestModel.getCountryCode())
                .shopperLocale(requestModel.getLocale())
                .channel(PaymentMethodsRequest.ChannelEnum.WEB);

        return adyenPaymentsApiDao.callPaymentMethodsApi(paymentMethodsRequest, merchantModel.getAdyenApiKey());
    }

    public CreateCheckoutSessionResponse createCheckoutSession(PaymentModel requestModel) {

        MerchantModel merchantModel = merchantRepository.findById(requestModel.getMerchantId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        //Save to Database
        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setMerchantAccountName(merchantModel.getAdyenMerchantAccount());
        transactionModel.setPaymentMethod("session");
        transactionModel.setStatus(TransactionStatus.REGISTERED.getStatus());
        transactionModel.setAmount(requestModel.getAmount());
        transactionModel.setCurrency(requestModel.getCurrency());
        transactionModel.setCreatedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        //Create Amount Object
        Amount amountObject = new Amount()
                .currency(transactionModel.getCurrency())
                .value(Long.parseLong(transactionModel.getAmount()));

        AuthenticationData authenticationData = TdsMode.fromValue(requestModel.getTdsMode()).getAuthenticationData();

        CreateCheckoutSessionRequest checkoutSessionRequest = new CreateCheckoutSessionRequest()
                .amount(amountObject)
                .merchantAccount(transactionModel.getMerchantAccountName())
                .channel(CreateCheckoutSessionRequest.ChannelEnum.WEB)
                .countryCode(requestModel.getCountryCode())
                .shopperLocale(requestModel.getLocale())
                .reference(transactionModel.getMerchantReference())
                .authenticationData(authenticationData)
                .returnUrl(UrlUtil.addUrlParameter(merchantModel.getReturnUrl(), "merchantId", merchantModel.getId()));

        //Call API
        CreateCheckoutSessionResponse createCheckoutSessionResponse = adyenPaymentsApiDao.callCreateSessionApi(checkoutSessionRequest, merchantModel.getAdyenApiKey());

        //Save to Database
        transactionModel.setStatus(TransactionStatus.AWAITING_AUTHORISATION.getStatus());
        transactionModel.setLastModifiedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        return createCheckoutSessionResponse;
    }

    public PaymentResponse makePayment(PaymentModel requestModel) {

        MerchantModel merchantModel = merchantRepository.findById(requestModel.getMerchantId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        //Save to Database
        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setMerchantAccountName(merchantModel.getAdyenMerchantAccount());
        transactionModel.setPaymentMethod(PaymentUtil.getPaymentType(requestModel.getPaymentMethod()));
        transactionModel.setStatus(TransactionStatus.REGISTERED.getStatus());
        transactionModel.setAmount(requestModel.getAmount());
        transactionModel.setCurrency(requestModel.getCurrency());
        transactionModel.setCreatedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        //Create Amount Object
        Amount amountObject = new Amount()
                .currency(transactionModel.getCurrency())
                .value(Long.parseLong(transactionModel.getAmount()));

        AuthenticationData authenticationData = TdsMode.fromValue(requestModel.getTdsMode()).getAuthenticationData();

        //Create Payment Object
        PaymentRequest paymentRequest = new PaymentRequest()
                .amount(amountObject)
                .merchantAccount(transactionModel.getMerchantAccountName())
                .channel(PaymentRequest.ChannelEnum.WEB)
                .countryCode(requestModel.getCountryCode())
                .shopperLocale(requestModel.getLocale())
                .paymentMethod(requestModel.getPaymentMethod())
                .reference(transactionModel.getMerchantReference())
                .authenticationData(authenticationData)
                .browserInfo(requestModel.getBrowserInfo())
                .origin(requestModel.getOrigin())
                .returnUrl(UrlUtil.addUrlParameter(merchantModel.getReturnUrl(), "merchantId", merchantModel.getId()));

        //Call API
        PaymentResponse paymentResponse = adyenPaymentsApiDao.callPaymentApi(paymentRequest, merchantModel.getAdyenApiKey());

        //Save to Database
        transactionModel.setStatus(TransactionStatus.AWAITING_AUTHORISATION.getStatus());
        transactionModel.setApiResultCode(paymentResponse.getResultCode().getValue());
        transactionModel.setOriginalPspReference(paymentResponse.getPspReference());
        transactionModel.setLastModifiedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        return paymentResponse;
    }

    public PaymentCaptureResponse capturePayment(PaymentModificationModel requestModel) {

        TransactionModel transactionModel = transactionRepository.findById(requestModel.getTransactionId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction Not Found"));

        MerchantModel merchantModel = merchantRepository.findByAdyenMerchantAccount(transactionModel.getMerchantAccountName()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        //Create Amount Object
        Amount amountObject = new Amount()
                .currency(transactionModel.getCurrency())
                .value(requestModel.getAmount() != null ? Long.parseLong(requestModel.getAmount()) : Long.parseLong(transactionModel.getAmount()));

        //Create Reverse Object
        PaymentCaptureRequest captureRequest = new PaymentCaptureRequest();
        captureRequest.setReference(transactionModel.getMerchantReference());
        captureRequest.setMerchantAccount(transactionModel.getMerchantAccountName());
        captureRequest.setAmount(amountObject);

        //Call API
        PaymentCaptureResponse captureResponse = adyenPaymentsApiDao.callPaymentCaptureApi(transactionModel.getOriginalPspReference(), captureRequest, merchantModel.getAdyenApiKey());

        //Update Database
        transactionModel.setStatus(TransactionStatus.AWAITING_CAPTURE.getStatus());
        transactionModel.setPspReference(captureResponse.getPspReference());
        transactionModel.setLastModifiedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        return captureResponse;
    }

    public PaymentReversalResponse reversePayment(PaymentModificationModel requestModel) {

        TransactionModel transactionModel = transactionRepository.findById(requestModel.getTransactionId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction Not Found"));

        MerchantModel merchantModel = merchantRepository.findByAdyenMerchantAccount(transactionModel.getMerchantAccountName()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        //Create Reverse Object
        PaymentReversalRequest reversalRequest = new PaymentReversalRequest();
        reversalRequest.setReference(transactionModel.getMerchantReference());
        reversalRequest.setMerchantAccount(transactionModel.getMerchantAccountName());

        //Call API
        PaymentReversalResponse reversalResponse = adyenPaymentsApiDao.callPaymentReversalApi(transactionModel.getOriginalPspReference(), reversalRequest, merchantModel.getAdyenApiKey());

        //Update Database
        transactionModel.setStatus(TransactionStatus.AWAITING_REVERSAL.getStatus());
        transactionModel.setPspReference(reversalResponse.getPspReference());
        transactionModel.setLastModifiedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        return reversalResponse;
    }

    public PaymentDetailsResponse submitPaymentDetails(PaymentDetailsModel requestModel) {

        MerchantModel merchantModel = merchantRepository.findById(requestModel.getMerchantId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        // Create the request object(s) TODO: Might be able to combine with below redirect method?
        PaymentCompletionDetails paymentCompletionDetails = new PaymentCompletionDetails()
                .threeDSResult(requestModel.getPaymentDetails().getDetails().getThreeDSResult());

        PaymentDetailsRequest paymentDetailsRequest = new PaymentDetailsRequest()
                .details(paymentCompletionDetails);

        PaymentDetailsResponse paymentDetailsResponse = adyenPaymentsApiDao.callPaymentDetailsApi(paymentDetailsRequest, merchantModel.getAdyenApiKey());

        //Update Database
        TransactionModel transactionModel = transactionRepository.findByMerchantReference(paymentDetailsResponse.getMerchantReference()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Transaction Not Found"));
        transactionModel.setApiResultCode(paymentDetailsResponse.getResultCode().getValue());
        transactionModel.setOriginalPspReference(paymentDetailsResponse.getPspReference());
        transactionModel.setLastModifiedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        return paymentDetailsResponse;
    }

    public PaymentDetailsResponse submitPaymentDetailsRedirect(String merchantId, String redirectResult) {

        MerchantModel merchantModel = merchantRepository.findById(merchantId).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        // Create the request object(s)
        PaymentCompletionDetails paymentCompletionDetails = new PaymentCompletionDetails()
                .redirectResult(redirectResult);

        PaymentDetailsRequest paymentDetailsRequest = new PaymentDetailsRequest()
                .details(paymentCompletionDetails);

        PaymentDetailsResponse paymentDetailsResponse = adyenPaymentsApiDao.callPaymentDetailsApi(paymentDetailsRequest, merchantModel.getAdyenApiKey());

        //Update Database
        TransactionModel transactionModel = transactionRepository.findByMerchantReference(paymentDetailsResponse.getMerchantReference()).orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
        transactionModel.setApiResultCode(paymentDetailsResponse.getResultCode().getValue());
        transactionModel.setOriginalPspReference(paymentDetailsResponse.getPspReference());
        transactionModel.setLastModifiedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        return paymentDetailsResponse;
    }

}
