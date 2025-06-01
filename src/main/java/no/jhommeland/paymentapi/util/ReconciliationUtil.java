package no.jhommeland.paymentapi.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.ArrayList;
import java.util.List;

public class ReconciliationUtil {

    private static final Logger logger = LoggerFactory.getLogger(ReconciliationUtil.class);

    public static List<CSVRecord> downloadAndParseCsv(String url, String apiKey) {

        logger.info("Will download and parse URL: {}", url);

        List<CSVRecord> records = new ArrayList<>();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-API-key", apiKey)
                .build();

        try (
            InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
        ) {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .get();
            format.parse(reader).forEach(records::add);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return records;
    }

}
