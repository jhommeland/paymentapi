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

    public final String TERMINAL_PROTOCOL_VERSION = "3.0";

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

    public TerminalPaymentResponseModel makePayment(TerminalPaymentModel requestModel) {

        MerchantModel merchantModel = merchantRepository.findById(requestModel.getMerchantId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        //Save to Database
        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setMerchantAccountName(merchantModel.getAdyenMerchantAccount());
        transactionModel.setPaymentMethod("terminal");
        transactionModel.setStatus(TransactionStatus.REGISTERED.getStatus());
        transactionModel.setAmount(requestModel.getAmount());
        transactionModel.setCurrency(requestModel.getCurrency());
        transactionModel.setCreatedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        TerminalAPIRequest terminalAPIRequest = new TerminalAPIRequest();
        SaleToPOIRequest saleToPOIRequest = new SaleToPOIRequest();
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setProtocolVersion(TERMINAL_PROTOCOL_VERSION);
        messageHeader.setMessageClass(MessageClassType.SERVICE);
        messageHeader.setMessageCategory(MessageCategoryType.PAYMENT);
        messageHeader.setMessageType(MessageType.REQUEST);
        messageHeader.setSaleID(TERMINAL_SALE_ID);
        messageHeader.setServiceID(PaymentUtil.generateServiceId());
        messageHeader.setPOIID(requestModel.getPoiId());
        saleToPOIRequest.setMessageHeader(messageHeader);
        PaymentRequest paymentRequest = new PaymentRequest();
        SaleData saleData = new SaleData();
        TransactionIdentification transactionIdentification = new TransactionIdentification();
        transactionIdentification.setTransactionID(transactionModel.getTransactionId());
        transactionIdentification.setTimeStamp(datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar()));
        saleData.setSaleTransactionID(transactionIdentification);
        saleData.setOperatorID(requestModel.getOperator());
        paymentRequest.setSaleData(saleData);
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        AmountsReq amountsReq = new AmountsReq();
        amountsReq.setCurrency(requestModel.getCurrency());
        amountsReq.setRequestedAmount(new BigDecimal(requestModel.getAmount()));
        paymentTransaction.setAmountsReq(amountsReq);
        paymentRequest.setPaymentTransaction(paymentTransaction);
        saleToPOIRequest.setPaymentRequest(paymentRequest);
        terminalAPIRequest.setSaleToPOIRequest(saleToPOIRequest);

        TerminalPaymentResponseModel responseModel = new TerminalPaymentResponseModel();
        if (requestModel.getRequestMode().equals("sync")) {
            TerminalAPIResponse terminalAPIResponse = adyenTerminalApiDao.callCloudTerminalApiSync(terminalAPIRequest, merchantModel.getAdyenApiKey());
            responseModel.setResult("Success");
            responseModel.setDetails(terminalAPIResponse.getSaleToPOIResponse().getPaymentResponse().getPaymentResult());
        } else {
            String response = adyenTerminalApiDao.callCloudTerminalApiAsync(terminalAPIRequest, merchantModel.getAdyenApiKey());
            responseModel.setResult(response);
        }

        //Save to Database
        transactionModel.setStatus(TransactionStatus.AWAITING_AUTHORISATION.getStatus());
        transactionModel.setLastModifiedAt(OffsetDateTime.now());
        transactionRepository.save(transactionModel);

        return responseModel;
    }
}
