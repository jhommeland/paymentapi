package no.jhommeland.paymentapi.util;

import com.adyen.model.nexo.*;
import org.apache.commons.io.FileUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class PrintUtil {

    private static final String PRINT_PARAM_NAME = "name";

    private static final String PRINT_PARAM_VALUE = "value";

    private static final String VALUE_SEPARATOR = ": ";

    private static final List<String> SANITIZE_TEXT_LIST = List.of("JPReceiptType: ");

    public static void decodeAndFormat(OutputContent outputContent) {
        outputContent.getOutputText().forEach((outputText) -> {

            StringBuilder stringBuilder = new StringBuilder();
            Map<String,String> map = UrlUtil.parseUrlParameters(outputText.getText(), true);

            if (map.containsKey(PRINT_PARAM_NAME)) {
                stringBuilder.append(map.get(PRINT_PARAM_NAME));
            }
            if (map.containsKey(PRINT_PARAM_VALUE)) {
                stringBuilder.append(VALUE_SEPARATOR);
                stringBuilder.append(map.get(PRINT_PARAM_VALUE));
            }

            String formattedString = stringBuilder.toString();
            for (String replaceText : SANITIZE_TEXT_LIST) {
                formattedString = formattedString.replaceAll(replaceText, "");
            }
            outputText.setText(formattedString);
        });
    }

    public static OutputContent createLogoPrintOutput() {
        try {
            File imageFile = ResourceUtils.getFile("classpath:static/images/receiptLogo.png");
            File xhtmlFile = ResourceUtils.getFile("classpath:static/html/receiptImage.xhtml");

            String xhtml = FileUtils.readFileToString(xhtmlFile, StandardCharsets.UTF_8);
            String base64ImageString = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(imageFile));

            xhtml = xhtml.replace("<IMAGE_DATA", base64ImageString);

            OutputContent outputContent = new OutputContent();
            outputContent.setOutputFormat(OutputFormatType.XHTML);
            outputContent.setOutputXHTML(xhtml.getBytes(StandardCharsets.UTF_8));

            return outputContent;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
