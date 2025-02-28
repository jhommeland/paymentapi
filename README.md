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

Before running, please set the credentials through environment variables:
```shell
export ADYEN_API_KEY=yourAdyenApiKey
export ADYEN_MERCHANT_ACCOUNT=yourAdyenMerchantAccount
export ADYEN_CLIENT_KEY=yourAdyenClientKey
```

Alternatively, it's possible to define the variables in the `application.properties`.
```txt
ADYEN_API_KEY=yourAdyenApiKey
ADYEN_MERCHANT_ACCOUNT=yourAdyenMerchantAccount
ADYEN_CLIENT_KEY=yourAdyenClientKey
```

The application can be run using the following command:

`./gradlew clean build run`

After running, by default the application can be accessed at http://localhost:8080

### Reference Documentation
For Adyen API reference, please access:

* [Adyen Docs](https://docs.adyen.com)
* [Adyen API Explorer](https://docs.adyen.com/api-explorer/)
