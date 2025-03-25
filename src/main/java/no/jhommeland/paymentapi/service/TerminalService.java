package no.jhommeland.paymentapi.service;

import com.adyen.model.nexo.*;
import com.adyen.model.terminal.*;
import no.jhommeland.paymentapi.dao.AdyenTerminalApiDao;
import no.jhommeland.paymentapi.dao.MerchantRepository;
import no.jhommeland.paymentapi.dao.ShopperRepository;
import no.jhommeland.paymentapi.dao.TransactionRepository;
import no.jhommeland.paymentapi.model.*;
import no.jhommeland.paymentapi.util.PaymentUtil;
import no.jhommeland.paymentapi.util.PrintUtil;
import no.jhommeland.paymentapi.util.TerminalUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.GregorianCalendar;
import java.util.List;

@Service
public class TerminalService {

    private static final Logger logger = LoggerFactory.getLogger(TerminalService.class);

    public final String TERMINAL_SALE_ID = "TEST_POS";

    public final String TERMINAL_PAYMENT_METHOD = "terminal";

    public final String TERMINAL_SYNC_REQUEST = "sync";

    public final String TERMINAL_ASYNC_REQUEST = "async";

    public final String TERMINAL_SYNC_RESPONSE_SUCCESS = "success";

    public final String TERMINAL_SYNC_RESPONSE_FAILURE = "failure";

    public final String TERMINAL_ABORT_CANCELLED_BY_OPERATOR = "Cancelled by Operator";

    public final String TERMINAL_RECURRING_CONTRACT_ONECLICK = "ONECLICK";

    private final AdyenTerminalApiDao adyenTerminalApiDao;

    private final MerchantRepository merchantRepository;

    private final ShopperRepository shopperRepository;

    private final TransactionRepository transactionRepository;

    private final DatatypeFactory datatypeFactory;

    public TerminalService(AdyenTerminalApiDao adyenTerminalApiDao, MerchantRepository merchantRepository, ShopperRepository shopperRepository, TransactionRepository transactionRepository) throws DatatypeConfigurationException {
        this.adyenTerminalApiDao = adyenTerminalApiDao;
        this.merchantRepository = merchantRepository;
        this.shopperRepository = shopperRepository;
        this.transactionRepository = transactionRepository;
        this.datatypeFactory = DatatypeFactory.newInstance();
    }

    public ConnectedTerminalsResponse getTerminals(TerminalPaymentModel requestModel) {

        MerchantModel merchantModel = merchantRepository.findById(requestModel.getMerchantId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        ConnectedTerminalsRequest connectedTerminalsRequest = new ConnectedTerminalsRequest();
        connectedTerminalsRequest.setMerchantAccount(merchantModel.getAdyenMerchantAccount());

        return adyenTerminalApiDao.getConnectedTerminals(connectedTerminalsRequest, merchantModel);

    }

    public TerminalPaymentResponseModel getTerminalPaymentStatus(TerminalPaymentReferenceModel requestModel) {

        MerchantModel merchantModel = merchantRepository.findById(requestModel.getMerchantId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        TerminalAPIRequest terminalAPIRequest = createTerminalApiTransactionStatusRequest(requestModel.getReferenceServiceId(), requestModel.getTerminalConfig());

        TerminalAPIResponse terminalAPIResponse = adyenTerminalApiDao.callTerminalApiSync(terminalAPIRequest, merchantModel, requestModel.getTerminalConfig());

        RepeatedMessageResponse repeatedResponse = terminalAPIResponse.getSaleToPOIResponse().getTransactionStatusResponse().getRepeatedMessageResponse();
        if (repeatedResponse != null) {
            return buildResponseModel(repeatedResponse.getRepeatedResponseMessageBody().getPaymentResponse().getResponse());
        }

        return buildResponseModel(terminalAPIResponse.getSaleToPOIResponse().getTransactionStatusResponse().getResponse());

    }

    public TerminalPaymentResponseModel makePayment(TerminalPaymentModel requestModel) {

        MerchantModel merchantModel = merchantRepository.findById(requestModel.getMerchantId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        ShopperModel shopperModel = StringUtils.isEmpty(requestModel.getShopperId()) ? null : shopperRepository.findById(requestModel.getShopperId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shopper not found"));

        //Save to Database
        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setMerchantReference(TerminalUtil.buildTransactionId(requestModel.getTerminalConfig().getPoiId(), requestModel.getServiceId()));
        transactionModel.setMerchantAccountName(merchantModel.getAdyenMerchantAccount());
        transactionModel.setPaymentMethod(TERMINAL_PAYMENT_METHOD);
        transactionModel.setStatus(TransactionStatus.REGISTERED.getStatus());
        transactionModel.setAmount(requestModel.getAmount());
        transactionModel.setCurrency(requestModel.getCurrency());
        transactionModel.setCreatedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        TerminalAPIRequest paymentRequest = createTerminalApiPaymentRequest(transactionModel.getMerchantReference(), requestModel, shopperModel);

        TerminalPaymentResponseModel responseModel = new TerminalPaymentResponseModel();
        if (TERMINAL_SYNC_REQUEST.equals(requestModel.getTerminalConfig().getConnectionType())) {

            //Initiate Payment
            TerminalAPIResponse paymentResponse = adyenTerminalApiDao.callTerminalApiSync(paymentRequest, merchantModel, requestModel.getTerminalConfig());
            responseModel = buildResponseModel(paymentResponse.getSaleToPOIResponse().getPaymentResponse().getResponse());

            List<PaymentReceipt> paymentReceiptList = paymentResponse.getSaleToPOIResponse().getPaymentResponse().getPaymentReceipt();
            paymentReceiptList.forEach((paymentReceipt -> {
                if (requestModel.getPrintReceipt().contains(paymentReceipt.getDocumentQualifier().value())) {

                    //Print Logo
                    TerminalAPIRequest logoPrintRequest = createTerminalApiPrintRequest(PrintUtil.createLogoPrintOutput(), requestModel.getTerminalConfig());
                    adyenTerminalApiDao.callTerminalApiSync(logoPrintRequest, merchantModel, requestModel.getTerminalConfig());

                    //Print Contents
                    PrintUtil.decodeAndFormat(paymentReceipt.getOutputContent());
                    TerminalAPIRequest printRequest = createTerminalApiPrintRequest(paymentReceipt.getOutputContent(), requestModel.getTerminalConfig());
                    adyenTerminalApiDao.callTerminalApiSync(printRequest, merchantModel, requestModel.getTerminalConfig());
                } else {
                    logger.info("Skipping printing of {}", paymentReceipt.getDocumentQualifier().value());
                }
            }));

        } else {
            String response = adyenTerminalApiDao.callTerminalApiAsync(paymentRequest, merchantModel, requestModel.getTerminalConfig());
            responseModel.setResult(response);
        }

        //Save to Database
        transactionModel.setErrorReason(responseModel.getReason());
        transactionModel.setStatus(TransactionStatus.AWAITING_AUTHORISATION.getStatus());
        transactionModel.setLastModifiedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        return responseModel;
    }

    public TerminalPaymentResponseModel abortPayment(TerminalPaymentReferenceModel requestModel) {

        MerchantModel merchantModel = merchantRepository.findById(requestModel.getMerchantId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        TerminalAPIRequest terminalAPIRequest = createTerminalApiTransactionAbortRequest(requestModel.getReferenceServiceId(), requestModel.getTerminalConfig());
        adyenTerminalApiDao.callTerminalApiSync(terminalAPIRequest, merchantModel, requestModel.getTerminalConfig());

        TerminalPaymentResponseModel responseModel = new TerminalPaymentResponseModel();
        responseModel.setResult(TERMINAL_SYNC_RESPONSE_SUCCESS);

        return responseModel;

    }

    private TerminalAPIRequest createTerminalApiPaymentRequest(String transactionId, TerminalPaymentModel requestModel, ShopperModel shopperModel) {

        var messageHeader = new MessageHeader();
        messageHeader.setMessageCategory(MessageCategoryType.PAYMENT);
        messageHeader.setMessageClass(MessageClassType.SERVICE);
        messageHeader.setMessageType(MessageType.REQUEST);
        messageHeader.setPOIID(requestModel.getTerminalConfig().getPoiId());
        messageHeader.setSaleID(TERMINAL_SALE_ID);
        messageHeader.setServiceID(requestModel.getServiceId());

        var saleTransactionIdentification = new TransactionIdentification();
        saleTransactionIdentification.setTransactionID(transactionId);
        saleTransactionIdentification.setTimeStamp(datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar()));

        var saleData = new SaleData();
        saleData.setSaleTransactionID(saleTransactionIdentification);

        if (shopperModel != null) {
            var saleToAcquirerData = new SaleToAcquirerData();
            saleToAcquirerData.setShopperReference(shopperModel.getShopperReference());
            saleToAcquirerData.setRecurringContract(TERMINAL_RECURRING_CONTRACT_ONECLICK);
            saleData.setSaleToAcquirerData(saleToAcquirerData);
        }

        var amountsReq = new AmountsReq();
        amountsReq.setCurrency(requestModel.getCurrency());
        amountsReq.setRequestedAmount(new BigDecimal(requestModel.getAmount()));

        var paymentTransaction = new PaymentTransaction();
        paymentTransaction.setAmountsReq(amountsReq);

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

    private TerminalAPIRequest createTerminalApiTransactionStatusRequest(String referenceServiceId, AdyenTerminalConfig terminalConfig) {

        var messageHeader = new MessageHeader();
        messageHeader.setMessageCategory(MessageCategoryType.TRANSACTION_STATUS);
        messageHeader.setMessageClass(MessageClassType.SERVICE);
        messageHeader.setMessageType(MessageType.REQUEST);
        messageHeader.setPOIID(terminalConfig.getPoiId());
        messageHeader.setSaleID(TERMINAL_SALE_ID);
        messageHeader.setServiceID(PaymentUtil.generateServiceId());

        var messageReference = new MessageReference();
        messageReference.setSaleID(TERMINAL_SALE_ID);
        messageReference.setServiceID(referenceServiceId);

        var transactionStatusRequest = new TransactionStatusRequest();
        transactionStatusRequest.setReceiptReprintFlag(false);
        transactionStatusRequest.setMessageReference(messageReference);
        transactionStatusRequest.getDocumentQualifier().add(DocumentQualifierType.CASHIER_RECEIPT);
        transactionStatusRequest.getDocumentQualifier().add(DocumentQualifierType.CUSTOMER_RECEIPT);

        var saleToPOIRequest = new SaleToPOIRequest();
        saleToPOIRequest.setMessageHeader(messageHeader);
        saleToPOIRequest.setTransactionStatusRequest(transactionStatusRequest);

        var terminalAPIRequest = new TerminalAPIRequest();
        terminalAPIRequest.setSaleToPOIRequest(saleToPOIRequest);

        return terminalAPIRequest;

    }

    private TerminalAPIRequest createTerminalApiTransactionAbortRequest(String referenceServiceId, AdyenTerminalConfig adyenTerminalConfig) {

        var messageHeader = new MessageHeader();
        messageHeader.setMessageCategory(MessageCategoryType.ABORT);
        messageHeader.setMessageClass(MessageClassType.SERVICE);
        messageHeader.setMessageType(MessageType.REQUEST);
        messageHeader.setPOIID(adyenTerminalConfig.getPoiId());
        messageHeader.setSaleID(TERMINAL_SALE_ID);
        messageHeader.setServiceID(PaymentUtil.generateServiceId());

        var messageReference = new MessageReference();
        messageReference.setSaleID(TERMINAL_SALE_ID);
        messageReference.setServiceID(referenceServiceId);

        var abortRequest = new AbortRequest();
        abortRequest.setMessageReference(messageReference);
        abortRequest.setAbortReason(TERMINAL_ABORT_CANCELLED_BY_OPERATOR);

        var saleToPOIRequest = new SaleToPOIRequest();
        saleToPOIRequest.setMessageHeader(messageHeader);
        saleToPOIRequest.setAbortRequest(abortRequest);

        var terminalAPIRequest = new TerminalAPIRequest();
        terminalAPIRequest.setSaleToPOIRequest(saleToPOIRequest);

        return terminalAPIRequest;

    }

    private TerminalAPIRequest createTerminalApiPrintRequest(OutputContent outputContent, AdyenTerminalConfig adyenTerminalConfig) {

        var messageHeader = new MessageHeader();
        messageHeader.setMessageCategory(MessageCategoryType.PRINT);
        messageHeader.setMessageClass(MessageClassType.DEVICE);
        messageHeader.setMessageType(MessageType.REQUEST);
        messageHeader.setPOIID(adyenTerminalConfig.getPoiId());
        messageHeader.setSaleID(TERMINAL_SALE_ID);
        messageHeader.setServiceID(PaymentUtil.generateServiceId());

        var printOutput = new PrintOutput();
        printOutput.setDocumentQualifier(DocumentQualifierType.DOCUMENT);
        printOutput.setResponseMode(ResponseModeType.PRINT_END);
        printOutput.setOutputContent(outputContent);

        var printRequest = new PrintRequest();
        printRequest.setPrintOutput(printOutput);

        var saleToPOIRequest = new SaleToPOIRequest();
        saleToPOIRequest.setMessageHeader(messageHeader);
        saleToPOIRequest.setPrintRequest(printRequest);

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
