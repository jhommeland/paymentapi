package no.jhommeland.paymentapi.dao;

import com.adyen.Client;
import com.adyen.enums.Environment;
import com.adyen.model.terminal.ConnectedTerminalsRequest;
import com.adyen.model.terminal.ConnectedTerminalsResponse;
import com.adyen.model.terminal.TerminalAPIRequest;
import com.adyen.model.terminal.TerminalAPIResponse;
import com.adyen.service.PosPayment;
import com.adyen.service.TerminalCloudAPI;
import no.jhommeland.paymentapi.util.PaymentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AdyenTerminalApiDao {

    public static final Logger logger = LoggerFactory.getLogger(AdyenTerminalApiDao.class);

    private TerminalCloudAPI initializeCloudApi(String adyenApiKey) {
        return new TerminalCloudAPI(new Client(adyenApiKey, Environment.TEST));
    }

    private PosPayment initializePosPayment(String adyenApiKey) {
        return new PosPayment(new Client(adyenApiKey, Environment.TEST));
    }

    public ConnectedTerminalsResponse getConnectedTerminals(ConnectedTerminalsRequest connectedTerminalsRequest, String adyenApiKey) {
        PosPayment posPayment = initializePosPayment(adyenApiKey);
        return PaymentUtil.executeApiCall(() -> posPayment.connectedTerminals(connectedTerminalsRequest), connectedTerminalsRequest);
    }

    public String callCloudTerminalApiAsync(TerminalAPIRequest terminalAPIRequest, String adyenApiKey) {
        TerminalCloudAPI terminalCloudAPI = initializeCloudApi(adyenApiKey);
        return PaymentUtil.executeApiCall(() -> terminalCloudAPI.async(terminalAPIRequest), terminalAPIRequest);
    }

    public TerminalAPIResponse callCloudTerminalApiSync(TerminalAPIRequest terminalAPIRequest, String adyenApiKey) {
        TerminalCloudAPI terminalCloudAPI = initializeCloudApi(adyenApiKey);
        return PaymentUtil.executeApiCall(() -> terminalCloudAPI.sync(terminalAPIRequest), terminalAPIRequest);
    }

}
