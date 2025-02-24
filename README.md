# PaymentAPI (Powered by Adyen)

### Overview

This repository provides samples on how to integrate with Adyen in Java 
with Springboot and Thymeleaf.

By default, [Testcontainers](https://testcontainers.com) is used to create and 
ephemereal DB at runtime. As such, transactions are not stored between runs.

### How to run

`./gradlew clean build run`

After running, by default the application can be accessed at http://localhost:8080

### Reference Documentation
For Adyen API reference, please access:

* [Adyen Docs](https://docs.adyen.com)
* [Adyen API Explorerer](https://docs.adyen.com/api-explorer/)
