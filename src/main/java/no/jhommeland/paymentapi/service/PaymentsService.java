package no.jhommeland.paymentapi.service;

import com.adyen.model.checkout.*;
import jakarta.persistence.EntityNotFoundException;
import no.jhommeland.paymentapi.dao.AdyenPaymentsApiDao;
import no.jhommeland.paymentapi.dao.MerchantRepository;
import no.jhommeland.paymentapi.dao.ShopperRepository;
import no.jhommeland.paymentapi.dao.TransactionRepository;
import no.jhommeland.paymentapi.model.*;
import no.jhommeland.paymentapi.util.PaymentUtil;
import no.jhommeland.paymentapi.util.UrlUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service class responsible for interacting with the Adyen payment API.
 * Provides methods for retrieving payment methods based on input parameters
 * and initializes the Adyen API client.
 */
@Service
public class PaymentsService {

    private final String STRING_TRUE_VALUE = "true";

    private final String LOCALIZED_SHOPPER_STATEMENT_LOCALE_JP = "ja-Kana";

    private final String SAVE_PAYMENT_METHOD_RECURRING_ONLY = "recurring";

    private final String SAVE_PAYMENT_METHOD_ONECLICK_ONLY = "oneClick";

    private final String SAVE_PAYMENT_METHOD_ASK_USER = "askUser";

    private final Logger logger = LoggerFactory.getLogger(PaymentsService.class);

    private final AdyenPaymentsApiDao adyenPaymentsApiDao;

    private final MerchantRepository merchantRepository;

    private final ShopperRepository shopperRepository;

    private final TransactionRepository transactionRepository;

    public PaymentsService(AdyenPaymentsApiDao adyenPaymentsApiDao, MerchantRepository merchantRepository, ShopperRepository shopperRepository, TransactionRepository transactionRepository) {
        this.adyenPaymentsApiDao = adyenPaymentsApiDao;
        this.merchantRepository = merchantRepository;
        this.shopperRepository = shopperRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionModel> getTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc();
    }

    public PaymentMethodsResponse getPaymentMethods(PaymentMethodsModel requestModel) {

        MerchantModel merchantModel = merchantRepository.findById(requestModel.getMerchantId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        ShopperModel shopperModel = StringUtils.isEmpty(requestModel.getShopperId()) ? new ShopperModel() : shopperRepository.findById(requestModel.getShopperId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shopper not found"));

        //Create Amount Object
        Amount amountObject = new Amount()
                .currency(requestModel.getCurrency())
                .value(Long.parseLong(requestModel.getAmount()));

        //Create Payment Methods Object
        PaymentMethodsRequest paymentMethodsRequest = new PaymentMethodsRequest()
                .amount(amountObject)
                .merchantAccount(merchantModel.getAdyenMerchantAccount())
                .shopperReference(shopperModel.getShopperReference())
                .countryCode(requestModel.getCountryCode())
                .shopperLocale(requestModel.getLocale())
                .channel(PaymentMethodsRequest.ChannelEnum.fromValue(requestModel.getChannel()));

        return adyenPaymentsApiDao.callPaymentMethodsApi(paymentMethodsRequest, merchantModel);
    }

    public SessionResultResponse getSessionResult(String merchantId, String sessionId, String sessionResult) {
        MerchantModel merchantModel = merchantRepository.findById(merchantId).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));
        return adyenPaymentsApiDao.callSessionResultApi(sessionId, sessionResult, merchantModel);
    }

    public CreateCheckoutSessionResponse createCheckoutSession(PaymentModel requestModel) {

        MerchantModel merchantModel = merchantRepository.findById(requestModel.getMerchantId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        ShopperModel shopperModel = StringUtils.isEmpty(requestModel.getShopperId()) ? new ShopperModel() : shopperRepository.findById(requestModel.getShopperId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shopper not found"));

        //Save to Database
        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setMerchantAccountName(merchantModel.getAdyenMerchantAccount());
        transactionModel.setShopperInteraction(requestModel.getShopperInteraction());
        transactionModel.setPaymentMethod("sessions");
        transactionModel.setStatus(TransactionStatus.REGISTERED.getStatus());
        transactionModel.setAmount(requestModel.getAmount());
        transactionModel.setCurrency(requestModel.getCurrency());
        transactionModel.setCreatedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        //Create Amount Object
        Amount amountObject = new Amount()
                .currency(transactionModel.getCurrency())
                .value(Long.parseLong(transactionModel.getAmount()));

        //3DS Authentication Data
        AuthenticationData authenticationData = TdsMode.fromValue(requestModel.getTdsMode()).getAuthenticationData();

        //Sessions Mode
        CreateCheckoutSessionRequest.ModeEnum modeEnum = CreateCheckoutSessionRequest.ModeEnum.fromValue(requestModel.getSessionsMode());

        CreateCheckoutSessionRequest checkoutSessionRequest = new CreateCheckoutSessionRequest()
                .amount(amountObject)
                .merchantAccount(transactionModel.getMerchantAccountName())
                .shopperReference(shopperModel.getShopperReference())
                .shopperInteraction(CreateCheckoutSessionRequest.ShopperInteractionEnum.fromValue(transactionModel.getShopperInteraction()))
                .countryCode(requestModel.getCountryCode())
                .shopperLocale(requestModel.getLocale())
                .shopperStatement(merchantModel.getShopperStatement())
                .reference(transactionModel.getMerchantReference())
                .mode(modeEnum);

        //Recurring Processing Model
        if (SAVE_PAYMENT_METHOD_RECURRING_ONLY.equals(requestModel.getSavePaymentMethod())) {
            checkoutSessionRequest.enableRecurring(true);
            checkoutSessionRequest.recurringProcessingModel(CreateCheckoutSessionRequest.RecurringProcessingModelEnum.CARDONFILE);
        } else if (SAVE_PAYMENT_METHOD_ONECLICK_ONLY.equals(requestModel.getSavePaymentMethod())) {
            checkoutSessionRequest.enableOneClick(true);
            checkoutSessionRequest.recurringProcessingModel(CreateCheckoutSessionRequest.RecurringProcessingModelEnum.CARDONFILE);
        }  else if (SAVE_PAYMENT_METHOD_ASK_USER.equals(requestModel.getSavePaymentMethod())) {
            checkoutSessionRequest.storePaymentMethodMode(CreateCheckoutSessionRequest.StorePaymentMethodModeEnum.ASKFORCONSENT);
            checkoutSessionRequest.recurringProcessingModel(CreateCheckoutSessionRequest.RecurringProcessingModelEnum.CARDONFILE);
        } else if (STRING_TRUE_VALUE.equals(requestModel.getSavePaymentMethod())) {
            checkoutSessionRequest.storePaymentMethod(true);
            checkoutSessionRequest.recurringProcessingModel(CreateCheckoutSessionRequest.RecurringProcessingModelEnum.CARDONFILE);
        }

        if (modeEnum == CreateCheckoutSessionRequest.ModeEnum.HOSTED) {
            String returnUrl = UrlUtil.addUrlParameter(merchantModel.getReturnUrl() + "/sessions", "merchantId", merchantModel.getId());
            returnUrl = UrlUtil.addUrlParameter(returnUrl, "channel", requestModel.getChannel());
            checkoutSessionRequest.setReturnUrl(returnUrl);
        } else {
            String returnUrl = UrlUtil.addUrlParameter(merchantModel.getReturnUrl(), "merchantId", merchantModel.getId());
            returnUrl = UrlUtil.addUrlParameter(returnUrl, "channel", requestModel.getChannel());
            checkoutSessionRequest.setChannel(CreateCheckoutSessionRequest.ChannelEnum.fromValue(requestModel.getChannel()));
            checkoutSessionRequest.setAuthenticationData(authenticationData);
            checkoutSessionRequest.setReturnUrl(returnUrl);
        }

        //Call API
        CreateCheckoutSessionResponse createCheckoutSessionResponse = adyenPaymentsApiDao.callCreateSessionApi(checkoutSessionRequest, merchantModel);

        //Save to Database
        transactionModel.setStatus(TransactionStatus.AWAITING_AUTHORISATION.getStatus());
        transactionModel.setLastModifiedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        return createCheckoutSessionResponse;
    }

    public PaymentResponse makePayment(PaymentModel requestModel) {

        MerchantModel merchantModel = merchantRepository.findById(requestModel.getMerchantId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        ShopperModel shopperModel = StringUtils.isEmpty(requestModel.getShopperId()) ? new ShopperModel() : shopperRepository.findById(requestModel.getShopperId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shopper not found"));

        String paymentType = PaymentUtil.getPaymentType(requestModel.getPaymentMethod());

        //This is not required, just a POC of using encryptedCardNumber to look up card details before the payment.
        if (PaymentUtil.PAYMENT_TYPE_SCHEME.equals(paymentType)) {
            CardDetails cardDetails = requestModel.getPaymentMethod().getCardDetails();
            CardDetailsRequest cardDetailsRequest = new CardDetailsRequest()
                    .merchantAccount(merchantModel.getAdyenMerchantAccount())
                    .encryptedCardNumber(cardDetails.getEncryptedCardNumber());
            try {
                adyenPaymentsApiDao.callCardDetailsApi(cardDetailsRequest, merchantModel);
            } catch (ResponseStatusException e) {
                logger.warn("Could not retrieve card details.");
            }
        }

        //Save to Database
        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setMerchantAccountName(merchantModel.getAdyenMerchantAccount());
        transactionModel.setShopperInteraction(requestModel.getShopperInteraction());
        transactionModel.setPaymentMethod(paymentType);
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

        //Create localized shopper statement map
        Map<String, String> localizedStatementMap = null;
        if (merchantModel.getShopperStatement() != null) {
            localizedStatementMap = Map.of(LOCALIZED_SHOPPER_STATEMENT_LOCALE_JP, merchantModel.getShopperStatement());
        }

        //Channel
        PaymentRequest.ChannelEnum channel = PaymentRequest.ChannelEnum.fromValue(requestModel.getChannel());

        //ReturnUrl
        String returnUrl = UrlUtil.addUrlParameter(merchantModel.getReturnUrl(), "merchantId", merchantModel.getId());
        returnUrl = UrlUtil.addUrlParameter(returnUrl, "channel", requestModel.getChannel());

        //Create Payment Object
        PaymentRequest paymentRequest = new PaymentRequest()
                .amount(amountObject)
                .merchantAccount(transactionModel.getMerchantAccountName())
                .shopperReference(shopperModel.getShopperReference())
                .shopperInteraction(PaymentRequest.ShopperInteractionEnum.fromValue(transactionModel.getShopperInteraction()))
                .channel(channel)
                .countryCode(requestModel.getCountryCode())
                .shopperLocale(requestModel.getLocale())
                .shopperStatement(merchantModel.getShopperStatement())
                .localizedShopperStatement(localizedStatementMap)
                .paymentMethod(requestModel.getPaymentMethod())
                .reference(transactionModel.getMerchantReference())
                .authenticationData(authenticationData)
                .browserInfo(requestModel.getBrowserInfo())
                .origin(requestModel.getOrigin())
                .redirectToIssuerMethod(channel == PaymentRequest.ChannelEnum.IOS ? HttpMethod.GET.toString() : null)
                .returnUrl(returnUrl);

        //Recurring Processing Model
        if (SAVE_PAYMENT_METHOD_RECURRING_ONLY.equals(requestModel.getSavePaymentMethod())) {
            paymentRequest.enableRecurring(true);
            paymentRequest.recurringProcessingModel(PaymentRequest.RecurringProcessingModelEnum.CARDONFILE);
        } else if (SAVE_PAYMENT_METHOD_ONECLICK_ONLY.equals(requestModel.getSavePaymentMethod())) {
            paymentRequest.enableOneClick(true);
            paymentRequest.recurringProcessingModel(PaymentRequest.RecurringProcessingModelEnum.CARDONFILE);
        } else if (STRING_TRUE_VALUE.equals(requestModel.getSavePaymentMethod())) {
            paymentRequest.storePaymentMethod(true);
            paymentRequest.recurringProcessingModel(PaymentRequest.RecurringProcessingModelEnum.CARDONFILE);
        } else if (PaymentUtil.isUsingStoredCard(requestModel.getPaymentMethod())) {
            paymentRequest.recurringProcessingModel(PaymentRequest.RecurringProcessingModelEnum.CARDONFILE);
        }

        //Call API
        PaymentResponse paymentResponse = adyenPaymentsApiDao.callPaymentApi(paymentRequest, merchantModel);

        //Save to Database
        transactionModel.setStatus(TransactionStatus.AWAITING_AUTHORISATION.getStatus());
        transactionModel.setAdyenStatus(paymentResponse.getResultCode().getValue());
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
        PaymentCaptureResponse captureResponse = adyenPaymentsApiDao.callPaymentCaptureApi(transactionModel.getOriginalPspReference(), captureRequest, merchantModel);

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
        PaymentReversalResponse reversalResponse = adyenPaymentsApiDao.callPaymentReversalApi(transactionModel.getOriginalPspReference(), reversalRequest, merchantModel);

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

        PaymentDetailsRequest paymentDetailsRequest = new PaymentDetailsRequest()
                .details(requestModel.getPaymentDetails().getDetails());

        PaymentDetailsResponse paymentDetailsResponse = adyenPaymentsApiDao.callPaymentDetailsApi(paymentDetailsRequest, merchantModel);

        //Update Database
        TransactionModel transactionModel = transactionRepository.findByMerchantReference(paymentDetailsResponse.getMerchantReference()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Transaction Not Found"));
        transactionModel.setAdyenStatus(paymentDetailsResponse.getResultCode().getValue());
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

        PaymentDetailsResponse paymentDetailsResponse = adyenPaymentsApiDao.callPaymentDetailsApi(paymentDetailsRequest, merchantModel);

        //Update Database
        TransactionModel transactionModel = transactionRepository.findByMerchantReference(paymentDetailsResponse.getMerchantReference()).orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
        transactionModel.setAdyenStatus(paymentDetailsResponse.getResultCode().getValue());
        transactionModel.setOriginalPspReference(paymentDetailsResponse.getPspReference());
        transactionModel.setLastModifiedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        return paymentDetailsResponse;
    }

}
