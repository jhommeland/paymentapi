package no.jhommeland.paymentapi.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogUtil {

    private static final DateTimeFormatter logLineDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public static void appendLogLine(StringBuilder stringBuilder, String line) {
        stringBuilder.append(String.format("%s --- %s\n", LocalDateTime.now().format(logLineDateFormatter), line));
    }
}
