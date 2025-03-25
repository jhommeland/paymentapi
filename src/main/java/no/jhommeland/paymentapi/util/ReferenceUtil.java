package no.jhommeland.paymentapi.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class ReferenceUtil {

    public static String generateReference() {
        // Get the current date in YYYYMMDD format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String date = dateFormat.format(new Date());

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
