package no.jhommeland.paymentapi.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class ReferenceUtil {

    public static String generateDate_yyyyMMdd() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(new Date());
    }

    public static String generateReference() {
        String date = generateDate_yyyyMMdd();

        // Generate a random alphanumeric string of length 10
        String randomString = generateRandomAlphanumeric(10);

        // Combine date and random string
        return date + "-" + randomString;
    }

    private static String generateRandomAlphanumeric(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }
}
