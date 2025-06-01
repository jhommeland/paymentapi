export class PaymentsUtil {

    static async getCredentials(merchantId) {
        try {
            // Direct use of await to wait for the response
            const response = await axios.get('/merchants/credentials/' + merchantId);

            // Log and return the server response data
            console.log('Credentials retrieved');
            return response.data;
        } catch (error) {
            // Handle error and return null in case of failure
            console.error('Credentials Retrieval Error:', error);
            return null;
        }
    }

    static async makeSessionsCall(merchantId, shopperId, amount, currency, countryCode, locale, tdsMode, savePaymentMethod) {
        try {
            // Direct use of await to wait for the response
            const response = await axios.post('/sessions', {
                merchantId: merchantId,
                shopperId: shopperId,
                amount: amount,
                currency: currency,
                countryCode: countryCode,
                locale: locale,
                tdsMode: tdsMode,
                savePaymentMethod: savePaymentMethod
            }, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            // Log and return the server response data
            console.log('Session Created:', response.data);
            return response.data;
        } catch (error) {
            // Handle error and return null in case of failure
            console.error('Session Creation Error:', error);
            return null;
        }
    }

    static async makePaymentMethodsCall(merchantId, shopperId, amount, currency, countryCode, locale) {
        try {
            // Direct use of await to wait for the response
            const response = await axios.post('/payment-methods', {
                merchantId: merchantId,
                shopperId: shopperId,
                amount: amount,
                currency: currency,
                countryCode: countryCode,
                locale: locale
            });

            // Log and return the server response data
            console.log('Payment Methods Retrieved:', response.data);
            return response.data;
        } catch (error) {
            // Handle error and return null in case of failure
            console.error('Payments Methods Retrieval Error:', error);
            return null;
        }
    }

    static async makePaymentsCall(data, merchantId, shopperId, amount, currency, countryCode, locale, tdsMode, origin, savePaymentMethod) {
        console.log('Will call /payments with data:', PaymentsUtil.printObject(data));
        try {
            // Use await to wait for the axios.post response
            const response = await axios.post('/payments', {
                merchantId: merchantId,
                shopperId: shopperId,
                amount: amount,
                currency: currency,
                countryCode: countryCode,
                locale: locale,
                tdsMode: tdsMode,
                paymentMethod: data.paymentMethod,
                browserInfo: data.browserInfo,
                origin: origin,
                savePaymentMethod: savePaymentMethod
            });

            // Log and return the resolved response data
            console.log('Payments Call Success', PaymentsUtil.printObject(response.data));
            return response.data;
        } catch (error) {
            // Handle the error gracefully and return null
            console.error('Payments Call Error:', error);
            return null;
        }
    }

    static async makeDetailsCall(merchantId, data) {
        try {
            // Direct use of await to wait for the response
            const response = await axios.post('/payments/details', {
                merchantId: merchantId,
                paymentDetails: data
            });
            // Log and return the server response data
            console.log('Details Call Success:', PaymentsUtil.printObject(response.data));
            return response.data;
        } catch (error) {
            // Handle error and return null in case of failure
            console.error('Details Call Error:', error);
            return null;
        }
    }

    static async getTransactions() {
        try {
            // Direct use of await to wait for the response
            const response = await axios.get('/transactions');

            // Log and return the server response data
            console.log('Transaction Retrieved:', response.data);
            return response.data;
        } catch (error) {
            // Handle error and return null in case of failure
            console.error('Transactions Retrieval Error:', error);
            return null;
        }
    }

    static async getReports() {
        try {
            // Direct use of await to wait for the response
            const response = await axios.get('/reports');

            // Log and return the server response data
            console.log('Reports Retrieved:', response.data);
            return response.data;
        } catch (error) {
            // Handle error and return null in case of failure
            console.error('Reports Retrieval Error:', error);
            return null;
        }
    }


    static async capturePayment(transactionId) {
        try {
            // Direct use of await to wait for the response
            const response = await axios.post('/payments/capture', {
                transactionId: transactionId
            });

            // Log the response and reload page
            console.log('Capture Success:', PaymentsUtil.printObject(response.data));
            window.location.reload();
        } catch (error) {
            // Handle error and return null in case of failure
            console.error('Capture Error:', error);
        }
    }

    static async revertPayment(transactionId) {
        try {
            // Direct use of await to wait for the response
            const response = await axios.post('/payments/reverse', {
                transactionId: transactionId
            });

            // Log the response and reload page
            console.log('Revert Success:', PaymentsUtil.printObject(response.data));
            window.location.reload();
        } catch (error) {
            // Handle error and return null in case of failure
            console.error('Revert Error:', error);
        }
    }

    static async reconcile(eventId) {
        try {
            // Direct use of await to wait for the response
            const response = await axios.post('/reconcile', {
                eventId: eventId
            });

            // Log the response and reload page
            console.log('Reconciliation Success:', PaymentsUtil.printObject(response.data));
            const reconciliationLog = document.getElementById("reconciliationLog");
            reconciliationLog.value += response.data;
            reconciliationLog.scrollTop = reconciliationLog.scrollHeight;
        } catch (error) {
            // Handle error and return null in case of failure
            console.error('Reconciliation Error:', error);
        }
    }

    static async makeTerminalPaymentCall(merchantId, serviceId, shopperId, poiId, amount, currency, apiType, localEndpoint, connectionType, printReceipt, timeout, savePaymentMethod) {
        try {
            // Direct use of await to wait for the response
            const response = await axios.post('/terminal/payments', {
                merchantId: merchantId,
                serviceId: serviceId,
                shopperId: shopperId,
                amount: amount,
                currency: currency,
                printReceipt: printReceipt,
                savePaymentMethod: savePaymentMethod,
                terminalConfig: {
                    poiId: poiId,
                    apiType: apiType,
                    connectionType: connectionType,
                    localEndpoint: localEndpoint
                }
            }, {
                headers: {
                    'Content-Type': 'application/json'
                },
                timeout: timeout
            });

            // Log and return the server response data
            console.log('Terminal Payment Response:', response.data);
            return response.data;
        } catch (error) {
            // Handle error and return null in case of failure
            console.error('Terminal Payment Error:', error);
            return null;
        }
    }

    static async makeTerminalPaymentAbortCall(merchantId, poiId, referenceServiceId, apiType, localEndpoint) {
        try {
            // Direct use of await to wait for the response
            const response = await axios.post('/terminal/payments/abort', {
                 merchantId: merchantId,
                 referenceServiceId: referenceServiceId,
                 terminalConfig: {
                     poiId: poiId,
                     apiType: apiType,
                     localEndpoint: localEndpoint
                 }
            }, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            // Log and return the server response data
            console.log('Terminal Payment Abort Response:', response.data);
            return response.data;
        } catch (error) {
            // Handle error and return null in case of failure
            console.error('Terminal Payment Abort Error:', error);
            return null;
        }
    }

    static async makeTerminalPaymentStatusCall(merchantId, poiId, referenceServiceId, apiType, localEndpoint) {
         try {
             // Direct use of await to wait for the response
             const response = await axios.post('/terminal/payments/status', {
                 merchantId: merchantId,
                 referenceServiceId: referenceServiceId,
                 terminalConfig: {
                     poiId: poiId,
                     apiType: apiType,
                     localEndpoint: localEndpoint
                 }
             }, {
                 headers: {
                     'Content-Type': 'application/json'
                 }
             });

             // Log and return the server response data
             console.log('Terminal Payment Status Response:', response.data);
             return response.data;
         } catch (error) {
             // Handle error and return null in case of failure
             console.error('Terminal Payment Status Error:', error);
             return null;
         }
    }

    static async getTerminals(merchantId) {
        try {
            // Direct use of await to wait for the response
            const response = await axios.post('/terminals', {
                merchantId: merchantId
            }, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            // Log and return the server response data
            console.log('Terminals Retrieved:', response.data);
            return response.data;
        } catch (error) {
            // Handle error and return null in case of failure
            console.error('Terminal Retrieval Error:', error);
            return null;
        }
    }

    static async onSubmitPayment(state, component, actions, merchantId, shopperId, amount, currency, countryCode, locale, tdsMode, origin, savePaymentMethod) {
        try {
            // Make a POST /payments request from your server.
            const result = await PaymentsUtil.makePaymentsCall(state.data, merchantId, shopperId, amount, currency, countryCode, locale, tdsMode, origin, savePaymentMethod);

            // If the /payments request from your server fails, or if an unexpected error occurs.
            if (!result.resultCode) {
                actions.reject();
                return;
            }

            const {
                resultCode,
                action,
                order,
                donationToken
            } = result;

            // If the /payments request form your server is successful, you must call this to resolve whichever of the listed objects are available.
            // You must call this, even if the result of the payment is unsuccessful.
            actions.resolve({
                resultCode,
                action,
                order,
                donationToken,
            });
        } catch (error) {
            console.error("onSubmit", error);
            actions.reject();
        }
    }

    static async onAdditionalDetails(merchantId, state, component, actions) {
        try {

            // Make a POST /payments/details request from your server.
            const result = await PaymentsUtil.makeDetailsCall(merchantId, state.data);

            // If the /payments/details request from your server fails, or if an unexpected error occurs.
            if (!result.resultCode) {
                actions.reject();
                return;
            }

            const {
                resultCode,
                action,
                order,
                donationToken
            } = result;

            // If the /payments/details request from your server is successful, you must call this to resolve whichever of the listed objects are available.
            // You must call this, even if the result of the payment is unsuccessful.
            actions.resolve({
                resultCode,
                action,
                order,
                donationToken,
            });
        } catch (error) {
            console.error("onSubmit", error);
            actions.reject();
        }
    }

    static async getClientKeyForSelectedMerchant() {
        const selectedMerchant = localStorage.getItem("selectedMerchant");
        return PaymentsUtil.getCredentials(selectedMerchant);
    }

    static onPaymentEvent(result, component) {
        const resultUrl = "/html/result.html?result=";
        window.location.href = resultUrl + btoa(JSON.stringify(result));
    }

    static getDropinConfiguration(amount, currency, countryCode) {
        const dropinConfiguration = {
            // Other Drop-in configuration...
            paymentMethodsConfiguration: {
                applepay: {
                    amount: {
                        value: amount,
                        currency: currency
                    },
                    countryCode: countryCode
                },
                card: {
                    // Optional configuration.
                    hasHolderName: true, // Show the cardholder name field.
                    holderNameRequired: true, // Mark the cardholder name field as required.
                    installmentOptions: {
                        card: {
                            values: [1, 2, 3, 4, 5]
                        },
                        showInstallmentAmounts: false
                    },
                    showInstallmentAmounts: false,
                    onBinLookup: (binData) => {
                        console.log(binData)
                    }
                }
            }
        }
        return dropinConfiguration;
    }

    static populatePaymentOptions() {

        const merchantSettings = JSON.parse(localStorage.getItem("selectedMerchantSettings"));

        const currencyDropdown = document.getElementById("currency");
        Object.entries(merchantSettings.currency).forEach(([key, value]) => {
            const option = document.createElement("option");
            option.value = key;
            option.textContent = value;
            currencyDropdown.appendChild(option);
        });

        const languageDropdown = document.getElementById("language");
        Object.entries(merchantSettings.language).forEach(([key, value]) => {
            const option = document.createElement("option");
            option.value = key;
            option.textContent = value;
            languageDropdown.appendChild(option);
        });

        const countryDropdown = document.getElementById("country");
        Object.entries(merchantSettings.country).forEach(([key, value]) => {
            const option = document.createElement("option");
            option.value = key;
            option.textContent = value;
            countryDropdown.appendChild(option);
        });

    }

    static async populateTerminalOptions() {

        const merchantSettings = JSON.parse(localStorage.getItem("selectedMerchantSettings"));

        const currencyDropdown = document.getElementById("currency");
        Object.entries(merchantSettings.currency).forEach(([key, value]) => {
            const option = document.createElement("option");
            option.value = key;
            option.textContent = value;
            currencyDropdown.appendChild(option);
        });

        const selectedMerchant = localStorage.getItem("selectedMerchant");

        const terminals = await PaymentsUtil.getTerminals(selectedMerchant);
        const poiIdDropdown = document.getElementById("poiId");
        terminals.uniqueTerminalIds.forEach((terminal) => {
            const option = document.createElement("option");
            option.value = terminal;
            option.textContent = terminal;
            poiIdDropdown.appendChild(option);
        });
    }

    static populateFilterDropdown(fieldName, filterObject, elements) {
        const uniqueValues = new Set(elements.map((element) => element[fieldName]));
        Array.from(uniqueValues).map((value) => {
            const option = document.createElement('option');
            option.value = value;
            option.textContent = value;
            filterObject.appendChild(option);
        });
    }

    static filterElements(fieldName, filterValue, elements) {
        if (filterValue === "all") {
            return elements
        }

        return elements.filter(
            (element) => element[fieldName] === filterValue
        );
    }

    static toAdyenLink(pspReference) {
        let adyenLink = "https://ca-test.adyen.com/ca/ca/accounts/showTx.shtml?pspReference=REPLACE_ME&txType=Payment";
        adyenLink = adyenLink.replace("REPLACE_ME", pspReference);
        return `<a href="${adyenLink}" target="_blank" rel="noopener noreferrer">${pspReference}</a>`
    }

    static printObject(obj) {
        return JSON.stringify(obj).replace(/\\n|\\t/g, '');
    }

    static generateServiceId() {
        return crypto.randomUUID().replaceAll("-", "").substring(0,10);
    }

}