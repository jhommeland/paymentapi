# PaymentAPI (Powered by Adyen)

### Overview

This repository provides samples on how to integrate with Adyen in Java 
with Springboot and Thymeleaf.

By default, the application expects a postgres database running on localhost:5432 with user:postgres password:password.

Such a database can be set up with docker using:
```
docker run --name postgres -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres
```

### How to run

Before running, please set the credentials by running the following sql with your merchant credentials:
```sql
INSERT INTO merchants (id, adyen_api_key, adyen_client_key, adyen_merchant_account, merchant_settings, return_url, security_key_identifier, security_key_passphrase, security_key_version) VALUES ('test_merchant_id', 'AdyenApiKey', 'test_AdyenClientKey', 'AdyenMerchantAccount', '{"currency":{"EUR":"Euro"},"language":{"en-UK":"English(UK)"},"country":{"NL":"Netherlands"}}', 'http://localhost:8080/payments/complete', 'test', 'test_key', '1');
```

To add a shopper, you can optionally add one with the following sql:

```sql
INSERT INTO shoppers (id, first_name, last_name, shopper_reference, shopper_settings) VALUES ('test_shopper', 'Test', 'Shopper', 'testshopper', null);
```

The application can be run using the following command:

`./gradlew clean build run`

After running, by default the application can be accessed at http://localhost:8080

### Reference Documentation
For Adyen API reference, please access:

* [Adyen Docs](https://docs.adyen.com)
* [Adyen API Explorer](https://docs.adyen.com/api-explorer/)
