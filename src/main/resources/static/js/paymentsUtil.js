export class PaymentsUtil {

    static async makeSessionsCall(amount, currency, countryCode, locale) {
        try {
            // Direct use of await to wait for the response
            const response = await axios.post('/sessions', {
                amount: amount,
                currency: currency,
                countryCode: countryCode,
                locale: locale
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

    static async makePaymentMethodsCall(amount, currency, countryCode, locale) {
        try {
            // Direct use of await to wait for the response
            const response = await axios.post('/payment-methods', {
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

    static async makePaymentsCall(data, amount, currency, countryCode, locale) {
        try {
            // Use await to wait for the axios.post response
            const response = await axios.post('/payments', {
                amount: amount,
                currency: currency,
                countryCode: countryCode,
                locale: locale,
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

    static async makeDetailsCall(data) {
        try {
            // Direct use of await to wait for the response
            const response = await axios.post('/payments/details', data);
            // Log and return the server response data
            console.log('Success:', response.data);
            return response.data;
        } catch (error) {
            // Handle error and return null in case of failure
            console.error('Error:', error);
            return null;
        }
    }

    static async onSubmitPayment(state, component, actions, amount, currency, countryCode, locale) {
        try {
            // Make a POST /payments request from your server.
            const result = await PaymentsUtil.makePaymentsCall(state.data, amount, currency, countryCode, locale);

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

    static async onAdditionalDetails(state, component, actions) {
        try {

            // Make a POST /payments/details request from your server.
            const result = await PaymentsUtil.makeDetailsCall(state.data);

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

}