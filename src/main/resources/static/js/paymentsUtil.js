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
            console.error('Error:', error);
            return null;
        }
    }

    static async makeSessionsCall(merchantId, amount, currency, countryCode, locale, tdsMode) {
        try {
            // Direct use of await to wait for the response
            const response = await axios.post('/sessions', {
                merchantId: merchantId,
                amount: amount,
                currency: currency,
                countryCode: countryCode,
                locale: locale,
                tdsMode: tdsMode
            }, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            // Log and return the server response data
            console.log('Success:', response.data);
            return response.data;
        } catch (error) {
            // Handle error and return null in case of failure
            console.error('Error:', error);
            return null;
        }
    }

    static async makePaymentMethodsCall(merchantId, amount, currency, countryCode, locale) {
        try {
            // Direct use of await to wait for the response
            const response = await axios.post('/payment-methods', {
                merchantId: merchantId,
                amount: amount,
                currency: currency,
                countryCode: countryCode,
                locale: locale
            });

            // Log and return the server response data
            console.log('Success:', response.data);
            return response.data;
        } catch (error) {
            // Handle error and return null in case of failure
            console.error('Error:', error);
            return null;
        }
    }

    static async makePaymentsCall(data, merchantId, amount, currency, countryCode, locale, tdsMode) {
        try {
            // Use await to wait for the axios.post response
            const response = await axios.post('/payments', {
                merchantId: merchantId,
                amount: amount,
                currency: currency,
                countryCode: countryCode,
                locale: locale,
                tdsMode: tdsMode,
                paymentMethod: data.paymentMethod
            });

            // Log and return the resolved response data
            console.log('Success:', response.data);
            return response.data;
        } catch (error) {
            // Handle the error gracefully and return null
            console.error('Error:', error);
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
            console.log('Success:', response.data);
            return response.data;
        } catch (error) {
            // Handle error and return null in case of failure
            console.error('Error:', error);
            return null;
        }
    }

    static async getTransactions() {
        try {
            // Direct use of await to wait for the response
            const response = await axios.get('/transactions');

            // Log and return the server response data
            console.log('Success:', response.data);
            return response.data;
        } catch (error) {
            // Handle error and return null in case of failure
            console.error('Error:', error);
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
            console.log('Success:', response.data);
            window.location.reload();
        } catch (error) {
            // Handle error and return null in case of failure
            console.error('Error:', error);
        }
    }

    static async revertPayment(transactionId) {
        try {
            // Direct use of await to wait for the response
            const response = await axios.post('/payments/reverse', {
                transactionId: transactionId
            });

            // Log the response and reload page
            console.log('Success:', response.data);
            window.location.reload();
        } catch (error) {
            // Handle error and return null in case of failure
            console.error('Error:', error);
        }
    }

    static async onSubmitPayment(state, component, actions, merchantId, amount, currency, countryCode, locale, tdsMode) {
        try {
            // Make a POST /payments request from your server.
            const result = await PaymentsUtil.makePaymentsCall(state.data, merchantId, amount, currency, countryCode, locale, tdsMode);

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

    static getDropinConfiguration() {
        const dropinConfiguration = {
            // Other Drop-in configuration...
            paymentMethodsConfiguration: {
                card: {
                    // Optional configuration.
                    hasHolderName: true, // Show the cardholder name field.
                    holderNameRequired: true, // Mark the cardholder name field as required.
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

        const tdsModeDropdown = document.getElementById("tdsMode");


    }

}