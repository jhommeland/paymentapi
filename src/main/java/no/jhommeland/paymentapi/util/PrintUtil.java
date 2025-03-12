package no.jhommeland.paymentapi.util;

import com.adyen.model.nexo.OutputContent;

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

}
