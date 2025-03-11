package no.jhommeland.paymentapi.dao;

import com.adyen.Client;
import com.adyen.Config;
import com.adyen.enums.Environment;
import com.adyen.model.terminal.ConnectedTerminalsRequest;
import com.adyen.model.terminal.ConnectedTerminalsResponse;
import com.adyen.model.terminal.TerminalAPIRequest;
import com.adyen.model.terminal.TerminalAPIResponse;
import com.adyen.model.terminal.security.SecurityKey;
import com.adyen.service.PosPayment;
import com.adyen.service.TerminalCloudAPI;
import com.adyen.service.TerminalLocalAPI;
import com.adyen.terminal.security.exception.NexoCryptoException;
import no.jhommeland.paymentapi.model.AdyenTerminalConfig;
import no.jhommeland.paymentapi.model.MerchantModel;
import no.jhommeland.paymentapi.util.PaymentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AdyenTerminalApiDao {

    public static final Logger logger = LoggerFactory.getLogger(AdyenTerminalApiDao.class);

    public static final String API_TYPE_CLOUD = "cloud";

    public static final String API_TYPE_LOCAL = "local";

    private TerminalCloudAPI initializeCloudApi(String adyenApiKey) {
        return new TerminalCloudAPI(new Client(adyenApiKey, Environment.TEST));
    }

    private PosPayment initializePosPayment(String adyenApiKey) {
        return new PosPayment(new Client(adyenApiKey, Environment.TEST));
    }

    private TerminalLocalAPI initializeLocalApi(MerchantModel merchantModel, AdyenTerminalConfig terminalConfig) {

        Config config = new Config();
        config.setTerminalApiLocalEndpoint(terminalConfig.getLocalEndpoint());

        Client terminalLocalClient = new Client(config);
        terminalLocalClient.setEnvironment(Environment.TEST, null);

        SecurityKey securityKey = new SecurityKey();
        securityKey.setAdyenCryptoVersion(1);
        securityKey.setKeyIdentifier(merchantModel.getSecurityKeyIdentifier());
        securityKey.setPassphrase(merchantModel.getSecurityKeyPassphrase());
        securityKey.setKeyVersion(Integer.valueOf(merchantModel.getSecurityKeyVersion()));

        try {
            return new TerminalLocalAPI(terminalLocalClient, securityKey);
        } catch (NexoCryptoException e) {
            throw new RuntimeException(e);
        }

    }

    public ConnectedTerminalsResponse getConnectedTerminals(ConnectedTerminalsRequest connectedTerminalsRequest, MerchantModel merchantModel) {
        PosPayment posPayment = initializePosPayment(merchantModel.getAdyenApiKey());
        return PaymentUtil.executeApiCall(() -> posPayment.connectedTerminals(connectedTerminalsRequest), connectedTerminalsRequest);
    }

    public String callTerminalApiAsync(TerminalAPIRequest terminalAPIRequest, MerchantModel merchantModel, AdyenTerminalConfig terminalConfig) {

        if (API_TYPE_LOCAL.equals(terminalConfig.getApiType())) {
            logger.warn("TerminalLocalAPI does not support async. Will fallback to TerminalCloudAPI");
        }

        TerminalCloudAPI terminalCloudAPI = initializeCloudApi(merchantModel.getAdyenApiKey());
        return PaymentUtil.executeApiCall(() -> terminalCloudAPI.async(terminalAPIRequest), terminalAPIRequest);
    }

    public TerminalAPIResponse callTerminalApiSync(TerminalAPIRequest terminalAPIRequest, MerchantModel merchantModel, AdyenTerminalConfig terminalConfig) {

        if (API_TYPE_CLOUD.equals(terminalConfig.apiType)) {
            TerminalCloudAPI terminalCloudAPI = initializeCloudApi(merchantModel.getAdyenApiKey());
            return PaymentUtil.executeApiCall(() -> terminalCloudAPI.sync(terminalAPIRequest), terminalAPIRequest);
        }

        TerminalLocalAPI terminalLocalAPI = initializeLocalApi(merchantModel, terminalConfig);
        return PaymentUtil.executeApiCall(() -> terminalLocalAPI.request(terminalAPIRequest), terminalAPIRequest);

    }

}
