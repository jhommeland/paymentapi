package no.jhommeland.paymentapi.service;

import com.adyen.model.nexo.*;
import com.adyen.model.terminal.ConnectedTerminalsRequest;
import com.adyen.model.terminal.ConnectedTerminalsResponse;
import com.adyen.model.terminal.TerminalAPIRequest;
import com.adyen.model.terminal.TerminalAPIResponse;
import no.jhommeland.paymentapi.dao.AdyenTerminalApiDao;
import no.jhommeland.paymentapi.dao.MerchantRepository;
import no.jhommeland.paymentapi.dao.TransactionRepository;
import no.jhommeland.paymentapi.model.*;
import no.jhommeland.paymentapi.util.PaymentUtil;
import no.jhommeland.paymentapi.util.TerminalUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.GregorianCalendar;

@Service
public class TerminalService {

    public final String TERMINAL_SALE_ID = "TEST_POS";

    public final String TERMINAL_PAYMENT_METHOD = "terminal";

    public final String TERMINAL_SYNC_REQUEST = "sync";

    public final String TERMINAL_ASYNC_REQUEST = "async";

    public final String TERMINAL_SYNC_RESPONSE_SUCCESS = "success";

    public final String TERMINAL_SYNC_RESPONSE_FAILURE = "failure";

    private final AdyenTerminalApiDao adyenTerminalApiDao;

    private final MerchantRepository merchantRepository;

    private final TransactionRepository transactionRepository;

    private final DatatypeFactory datatypeFactory;

    public TerminalService(AdyenTerminalApiDao adyenTerminalApiDao, MerchantRepository merchantRepository, TransactionRepository transactionRepository) throws DatatypeConfigurationException {
        this.adyenTerminalApiDao = adyenTerminalApiDao;
        this.merchantRepository = merchantRepository;
        this.transactionRepository = transactionRepository;
        this.datatypeFactory = DatatypeFactory.newInstance();
    }

    public ConnectedTerminalsResponse getTerminals(TerminalPaymentModel requestModel) {

        MerchantModel merchantModel = merchantRepository.findById(requestModel.getMerchantId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        ConnectedTerminalsRequest connectedTerminalsRequest = new ConnectedTerminalsRequest();
        connectedTerminalsRequest.setMerchantAccount(merchantModel.getAdyenMerchantAccount());

        return adyenTerminalApiDao.getConnectedTerminals(connectedTerminalsRequest, merchantModel.getAdyenApiKey());

    }

    public TerminalPaymentResponseModel getTerminalPaymentStatus(TerminalPaymentStatusModel requestModel) {

        MerchantModel merchantModel = merchantRepository.findById(requestModel.getMerchantId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        TerminalAPIRequest terminalAPIRequest = createTerminalApiTransactionStatusRequest(requestModel.getPoiId(),
                requestModel.getReferenceServiceId());

        TerminalAPIResponse terminalAPIResponse = adyenTerminalApiDao.callCloudTerminalApiSync(terminalAPIRequest, merchantModel.getAdyenApiKey());
        Response response = terminalAPIResponse.getSaleToPOIResponse().getTransactionStatusResponse().getResponse();

        return buildResponseModel(response);

    }

    public TerminalPaymentResponseModel makePayment(TerminalPaymentModel requestModel) {

        MerchantModel merchantModel = merchantRepository.findById(requestModel.getMerchantId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        //Save to Database
        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setMerchantAccountName(merchantModel.getAdyenMerchantAccount());
        transactionModel.setPaymentMethod(TERMINAL_PAYMENT_METHOD);
        transactionModel.setStatus(TransactionStatus.REGISTERED.getStatus());
        transactionModel.setAmount(requestModel.getAmount());
        transactionModel.setCurrency(requestModel.getCurrency());
        transactionModel.setCreatedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        TerminalAPIRequest terminalAPIRequest = createTerminalApiPaymentRequest(requestModel);

        TerminalPaymentResponseModel responseModel = new TerminalPaymentResponseModel();
        if (TERMINAL_SYNC_REQUEST.equals(requestModel.getRequestMode())) {
            TerminalAPIResponse terminalAPIResponse = adyenTerminalApiDao.callCloudTerminalApiSync(terminalAPIRequest, merchantModel.getAdyenApiKey());
            responseModel = buildResponseModel(terminalAPIResponse.getSaleToPOIResponse().getPaymentResponse().getResponse());
        } else {
            String response = adyenTerminalApiDao.callCloudTerminalApiAsync(terminalAPIRequest, merchantModel.getAdyenApiKey());
            responseModel.setResult(response);
        }

        //Save to Database
        transactionModel.setErrorReason(responseModel.getReason());
        transactionModel.setStatus(TransactionStatus.AWAITING_AUTHORISATION.getStatus());
        transactionModel.setLastModifiedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        return responseModel;
    }

    private TerminalAPIRequest createTerminalApiPaymentRequest(TerminalPaymentModel requestModel) {

        var messageHeader = new MessageHeader();
        messageHeader.setMessageCategory(MessageCategoryType.PAYMENT);
        messageHeader.setMessageClass(MessageClassType.SERVICE);
        messageHeader.setMessageType(MessageType.REQUEST);
        messageHeader.setPOIID(requestModel.getPoiId());
        messageHeader.setSaleID(TERMINAL_SALE_ID);
        messageHeader.setServiceID(requestModel.getServiceId());

        var saleTransactionIdentification = new TransactionIdentification();
        saleTransactionIdentification.setTransactionID(TerminalUtil.buildTransactionId(requestModel.getPoiId(), requestModel.getServiceId()));
        saleTransactionIdentification.setTimeStamp(datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar()));

        var saleData = new SaleData();
        saleData.setSaleTransactionID(saleTransactionIdentification);

        var amountsReq = new AmountsReq();
        amountsReq.setCurrency(requestModel.getCurrency());
        amountsReq.setRequestedAmount(new BigDecimal(requestModel.getAmount()));

        var transactionConditions = new TransactionConditions();
        transactionConditions.setCustomerLanguage(TerminalUtil.localeToIsoLanguage(requestModel.getLocale()));

        var paymentTransaction = new PaymentTransaction();
        paymentTransaction.setAmountsReq(amountsReq);
        paymentTransaction.setTransactionConditions(transactionConditions);

        var paymentRequest = new PaymentRequest();
        paymentRequest.setSaleData(saleData);
        paymentRequest.setPaymentTransaction(paymentTransaction);

        var saleToPOIRequest = new SaleToPOIRequest();
        saleToPOIRequest.setMessageHeader(messageHeader);
        saleToPOIRequest.setPaymentRequest(paymentRequest);

        var terminalAPIRequest = new TerminalAPIRequest();
        terminalAPIRequest.setSaleToPOIRequest(saleToPOIRequest);

        return terminalAPIRequest;

    }

    private TerminalAPIRequest createTerminalApiTransactionStatusRequest(String poiId, String referenceServiceId) {

        var messageHeader = new MessageHeader();
        messageHeader.setMessageCategory(MessageCategoryType.TRANSACTION_STATUS);
        messageHeader.setMessageClass(MessageClassType.SERVICE);
        messageHeader.setMessageType(MessageType.REQUEST);
        messageHeader.setPOIID(poiId);
        messageHeader.setSaleID(TERMINAL_SALE_ID);
        messageHeader.setServiceID(PaymentUtil.generateServiceId());

        var messageReference = new MessageReference();
        messageReference.setSaleID(TERMINAL_SALE_ID);
        messageReference.setServiceID(referenceServiceId);

        var transactionStatusRequest = new TransactionStatusRequest();
        transactionStatusRequest.setReceiptReprintFlag(false);
        transactionStatusRequest.setMessageReference(messageReference);
        //transactionStatusRequest.getDocumentQualifier().add(DocumentQualifierType.CASHIER_RECEIPT);
        //transactionStatusRequest.getDocumentQualifier().add(DocumentQualifierType.CUSTOMER_RECEIPT);

        var saleToPOIRequest = new SaleToPOIRequest();
        saleToPOIRequest.setMessageHeader(messageHeader);
        saleToPOIRequest.setTransactionStatusRequest(transactionStatusRequest);

        var terminalAPIRequest = new TerminalAPIRequest();
        terminalAPIRequest.setSaleToPOIRequest(saleToPOIRequest);

        return terminalAPIRequest;

    }

    private TerminalPaymentResponseModel buildResponseModel(Response response) {
        TerminalPaymentResponseModel responseModel = new TerminalPaymentResponseModel();
        switch (response.getResult()) {
            case SUCCESS:
                responseModel.setResult(TERMINAL_SYNC_RESPONSE_SUCCESS);
                break;
            default:
                responseModel.setResult(TERMINAL_SYNC_RESPONSE_FAILURE);
                responseModel.setReason(response.getErrorCondition().value());
        }

        return responseModel;
    }

}
