package no.jhommeland.paymentapi.util;

import com.adyen.model.nexo.OutputContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugUtil {

    private static final Logger logger = LoggerFactory.getLogger(DebugUtil.class);

    public static void printReceipt(OutputContent outputContent) {
        outputContent.getOutputText().forEach((outputText) -> {
            logger.info(outputText.getText());
        });
    }

}
