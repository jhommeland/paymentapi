package no.jhommeland.paymentapi.service;

import com.adyen.model.nexo.*;
import com.adyen.model.terminal.*;
import no.jhommeland.paymentapi.dao.AdyenTerminalApiDao;
import no.jhommeland.paymentapi.dao.MerchantRepository;
import no.jhommeland.paymentapi.dao.ShopperRepository;
import no.jhommeland.paymentapi.dao.TransactionRepository;
import no.jhommeland.paymentapi.dao.demo.PosDemoCustomerRepository;
import no.jhommeland.paymentapi.dao.demo.PosDemoPurchaseRepository;
import no.jhommeland.paymentapi.model.*;
import no.jhommeland.paymentapi.model.demo.PosDemoCustomerModel;
import no.jhommeland.paymentapi.model.demo.PosDemoPurchaseModel;
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
import java.util.Optional;

@Service
public class TerminalService {

    private static final Logger logger = LoggerFactory.getLogger(TerminalService.class);

    public final String TERMINAL_SALE_ID = "TEST_POS";

    public final String POS_SHOPPER_INTERACTION = "pos";

    public final String TERMINAL_PAYMENT_METHOD = "terminal";

    public final String TERMINAL_SYNC_REQUEST = "sync";

    public final String TERMINAL_ASYNC_REQUEST = "async";

    public final String TERMINAL_SYNC_RESPONSE_SUCCESS = "success";

    public final String TERMINAL_SYNC_RESPONSE_FAILURE = "failure";

    public final String RESPONSE_REASON_INSUFFICIENT_BALANCE = "Insufficient Balance";

    public final String TERMINAL_ABORT_CANCELLED_BY_OPERATOR = "Cancelled by Operator";

    public final String TERMINAL_RECURRING_CONTRACT_ONECLICK = "ONECLICK";

    public final String PREDEFINED_CONTENT_DECLINED_ANIMATED = "DeclinedAnimated";

    private final AdyenTerminalApiDao adyenTerminalApiDao;

    private final MerchantRepository merchantRepository;

    private final ShopperRepository shopperRepository;

    private final TransactionRepository transactionRepository;

    private final PosDemoCustomerRepository posDemoCustomerRepository;

    private final PosDemoPurchaseRepository posDemoPurchaseRepository;

    private final DatatypeFactory datatypeFactory;

    public TerminalService(AdyenTerminalApiDao adyenTerminalApiDao, MerchantRepository merchantRepository, ShopperRepository shopperRepository,
                           TransactionRepository transactionRepository, PosDemoCustomerRepository posDemoCustomerRepository, PosDemoPurchaseRepository posDemoPurchaseRepository) throws DatatypeConfigurationException {
        this.adyenTerminalApiDao = adyenTerminalApiDao;
        this.merchantRepository = merchantRepository;
        this.shopperRepository = shopperRepository;
        this.transactionRepository = transactionRepository;
        this.posDemoCustomerRepository = posDemoCustomerRepository;
        this.posDemoPurchaseRepository = posDemoPurchaseRepository;
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
        transactionModel.setShopperInteraction(POS_SHOPPER_INTERACTION);
        transactionModel.setPaymentMethod(TERMINAL_PAYMENT_METHOD);
        transactionModel.setStatus(TransactionStatus.REGISTERED.getStatus());
        transactionModel.setAmount(requestModel.getAmount());
        transactionModel.setCurrency(requestModel.getCurrency());
        transactionModel.setCreatedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        TerminalAPIRequest paymentRequest = createTerminalApiPaymentRequest(transactionModel.getMerchantReference(), requestModel, shopperModel, null);

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

    public TerminalPaymentResponseModel makePaymentWithCardAcquisition(TerminalPaymentModel requestModel) {

        MerchantModel merchantModel = merchantRepository.findById(requestModel.getMerchantId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        //requestModel.shopperId can be set, but don't require it to be in shopper table

        //Save to Database
        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setMerchantReference(TerminalUtil.buildTransactionId(requestModel.getTerminalConfig().getPoiId(), requestModel.getServiceId()));
        transactionModel.setMerchantAccountName(merchantModel.getAdyenMerchantAccount());
        transactionModel.setShopperInteraction(POS_SHOPPER_INTERACTION);
        transactionModel.setPaymentMethod(TERMINAL_PAYMENT_METHOD);
        transactionModel.setStatus(TransactionStatus.REGISTERED.getStatus());
        transactionModel.setAmount(requestModel.getAmount());
        transactionModel.setCurrency(requestModel.getCurrency());
        transactionModel.setCreatedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        TerminalAPIRequest cardAcquisitionRequest = createTerminalApiCardAcquisitionRequest(transactionModel.getMerchantReference() + "-cardAcq", requestModel);
        TerminalAPIResponse cardAcquisitionResponse = adyenTerminalApiDao.callTerminalApiSync(cardAcquisitionRequest, merchantModel, requestModel.getTerminalConfig());

        //Check whether to proceed with the payment or refuse it.
        //In this example, we check the shopper's remaining balance. If the shopper is not found, skip and always succeed.
        Optional<PosDemoCustomerModel> customerModelOptional = posDemoCustomerRepository.findById(requestModel.getShopperId());
        if (customerModelOptional.isPresent()) {
            PosDemoCustomerModel customerModel = customerModelOptional.get();
            List<PosDemoPurchaseModel> purchases = posDemoPurchaseRepository.findAllByCustomerName(customerModel.getCustomerName());
            double purchaseAmount = purchases.stream().map(PosDemoPurchaseModel::getItemAmount).mapToDouble(Double::parseDouble).sum();
            double totalAmount = purchaseAmount + Double.parseDouble(requestModel.getAmount());
            if (totalAmount > Double.parseDouble(customerModel.getBalance())) { //Not enough balance
                TerminalAPIRequest transactionDeclinedMessageRequest = createTransactionDeclinedEnableServiceRequest(requestModel.getTerminalConfig(),
                        "決済失敗", "残高が足りません \uD83D\uDE22");
                adyenTerminalApiDao.callTerminalApiSync(transactionDeclinedMessageRequest, merchantModel, requestModel.getTerminalConfig());

                //Failure response
                TerminalPaymentResponseModel responseModel = new TerminalPaymentResponseModel();
                responseModel.setResult(TERMINAL_SYNC_RESPONSE_FAILURE);
                responseModel.setReason(RESPONSE_REASON_INSUFFICIENT_BALANCE);

                //Save to Database
                transactionModel.setErrorReason(responseModel.getReason());
                transactionModel.setStatus(TransactionStatus.REFUSED.getStatus());
                transactionModel.setLastModifiedAt(OffsetDateTime.now());
                transactionRepository.save(transactionModel);

                return responseModel;
            }
        }

        //Create shopperModel
        ShopperModel shopperModel = new ShopperModel();
        customerModelOptional.ifPresent(customerModel -> shopperModel.setShopperReference(customerModel.getCustomerName()));

        //Make the payment request
        TransactionIdentification cardAcqReference = cardAcquisitionResponse.getSaleToPOIResponse().getCardAcquisitionResponse().getPOIData().getPOITransactionID();
        TerminalAPIRequest paymentRequest = createTerminalApiPaymentRequest(transactionModel.getMerchantReference(), requestModel, shopperModel, cardAcqReference);
        TerminalAPIResponse paymentResponse = adyenTerminalApiDao.callTerminalApiSync(paymentRequest, merchantModel, requestModel.getTerminalConfig());

        TerminalPaymentResponseModel responseModel = buildResponseModel(paymentResponse.getSaleToPOIResponse().getPaymentResponse().getResponse());

        //Save to purchases Database
        if (customerModelOptional.isPresent()) {
            PosDemoPurchaseModel purchase = new PosDemoPurchaseModel();
            purchase.setCustomerName(customerModelOptional.get().getCustomerName());
            purchase.setItemAmount(requestModel.getAmount());
            posDemoPurchaseRepository.save(purchase);
        }

        //Save to transaction Database
        transactionModel.setErrorReason(responseModel.getReason());
        transactionModel.setStatus(TransactionStatus.AWAITING_AUTHORISATION.getStatus());
        transactionModel.setLastModifiedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        return responseModel;
    }

    private TerminalAPIRequest createTerminalApiPaymentRequest(String transactionId, TerminalPaymentModel requestModel, ShopperModel shopperModel, TransactionIdentification cardAcqReference) {

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

        if (cardAcqReference != null) {
            var paymentData = new PaymentData();
            paymentData.setCardAcquisitionReference(cardAcqReference);
            paymentRequest.setPaymentData(paymentData);
        }

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

    private TerminalAPIRequest createTerminalApiCardAcquisitionRequest(String transactionId, TerminalPaymentModel requestModel) {

        var messageHeader = new MessageHeader();
        messageHeader.setMessageCategory(MessageCategoryType.CARD_ACQUISITION);
        messageHeader.setMessageClass(MessageClassType.SERVICE);
        messageHeader.setMessageType(MessageType.REQUEST);
        messageHeader.setPOIID(requestModel.getTerminalConfig().getPoiId());
        messageHeader.setSaleID(TERMINAL_SALE_ID);
        messageHeader.setServiceID(PaymentUtil.generateServiceId());

        var saleTransactionIdentification = new TransactionIdentification();
        saleTransactionIdentification.setTransactionID(transactionId);
        saleTransactionIdentification.setTimeStamp(datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar()));

        var saleData = new SaleData();
        saleData.setSaleTransactionID(saleTransactionIdentification);

        var cardAcquisitionTransaction = new CardAcquisitionTransaction();
        cardAcquisitionTransaction.setTotalAmount(new BigDecimal(requestModel.getAmount()));

        var cardAcquisitionRequest = new CardAcquisitionRequest();
        cardAcquisitionRequest.setSaleData(saleData);
        cardAcquisitionRequest.setCardAcquisitionTransaction(cardAcquisitionTransaction);

        var saleToPOIRequest = new SaleToPOIRequest();
        saleToPOIRequest.setMessageHeader(messageHeader);
        saleToPOIRequest.setCardAcquisitionRequest(cardAcquisitionRequest);

        var terminalAPIRequest = new TerminalAPIRequest();
        terminalAPIRequest.setSaleToPOIRequest(saleToPOIRequest);

        return terminalAPIRequest;
    }

    private TerminalAPIRequest createTransactionDeclinedEnableServiceRequest(AdyenTerminalConfig terminalConfig, String title, String message) {

        var messageHeader = new MessageHeader();
        messageHeader.setMessageCategory(MessageCategoryType.ENABLE_SERVICE);
        messageHeader.setMessageClass(MessageClassType.SERVICE);
        messageHeader.setMessageType(MessageType.REQUEST);
        messageHeader.setPOIID(terminalConfig.getPoiId());
        messageHeader.setSaleID(TERMINAL_SALE_ID);
        messageHeader.setServiceID(PaymentUtil.generateServiceId());

        var predefinedContent = new PredefinedContent();
        predefinedContent.setReferenceID(PREDEFINED_CONTENT_DECLINED_ANIMATED);

        var outputTextTitle = new OutputText();
        outputTextTitle.setText(title);

        var outputTextMessage = new OutputText();
        outputTextMessage.setText(message);

        var outputContent = new OutputContent();
        outputContent.setPredefinedContent(predefinedContent);
        outputContent.setOutputFormat(OutputFormatType.TEXT);
        outputContent.setOutputText(List.of(outputTextTitle, outputTextMessage));

        var displayOutput = new DisplayOutput();
        displayOutput.setDevice(DeviceType.CUSTOMER_DISPLAY);
        displayOutput.setInfoQualify(InfoQualifyType.DISPLAY);
        displayOutput.setOutputContent(outputContent);

        var enableServiceRequest = new EnableServiceRequest();
        enableServiceRequest.setTransactionAction(TransactionActionType.ABORT_TRANSACTION);
        enableServiceRequest.setDisplayOutput(displayOutput);

        var saleToPOIRequest = new SaleToPOIRequest();
        saleToPOIRequest.setMessageHeader(messageHeader);
        saleToPOIRequest.setEnableServiceRequest(enableServiceRequest);

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
