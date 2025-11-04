import { PaymentsUtil } from '../js-util/paymentsUtil.js';

export class CheckoutUtil {

    static MOUNT_CONTAINER = "#dropin-container";
    static SAVED_CARD_MOUNT_CONTAINER = "#stored-card";

    static getDropinConfiguration(amount, currency, countryCode) {
        const dropinConfiguration = {
            paymentMethodsConfiguration: {
                applepay: {
                    amount: {
                        value: amount,
                        currency: currency
                    },
                    countryCode: countryCode
                },
                card: {
                    showPayButton: true,
                    styles: CheckoutUtil.getCardStyling(),
                    hasHolderName: true,
                    holderNameRequired: true,
                    installmentOptions: {
                        mc: {
                            values: [1,2,3,4,5]
                        },
                        visa: {
                            values: [1,2,3,4,5]
                        },
                        jcb: {
                            values: [1,2,3,4,5]
                        },
                        diners: {
                            values: [1],
                        	plans: ["revolving"]
                        },
                        showInstallmentAmounts: false
                    },
                    showInstallmentAmounts: false,
                    onBinLookup: (binData) => {
                        console.log("BIN data retrieved:", binData)
                    },
                    onBinValue: (binValue) => {
                        console.log("BIN value retrieved:", binValue)
                    },
                    onFieldValid: (data) => {
                        console.log("Field validation:", data)
                    }
                }
            }
        }
        return dropinConfiguration;
    }

    static getCardStyling() {
        var styleObject = {
        	base: {
        		color: '#001b2b',
        		fontSize: '16px',
        		fontSmoothing: 'antialiased',
        		fontFamily: 'Helvetica',
        		fontWeight: '400'
        	},
        	error: {
        		color: '#001b2b'
        	},
        	placeholder: {
        		color: '#90a2bd',
        		fontWeight: '200'
        	},
        	validated: {
        		color: 'green'
        	}
        };
        return styleObject;
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

    static onPaymentEvent(result, component) {
        const resultUrl = "/html/result.html?result=";
        window.location.href = resultUrl + btoa(JSON.stringify(result));
    }

    static async mountCheckout(component, checkoutConfiguration, dropinConfiguration, version) {

        console.log("Mounting Checkout version " + version)

        if (version.startsWith("6.")) {
            const { AdyenCheckout } = window.AdyenWeb;
            const checkout = await AdyenCheckout(checkoutConfiguration);
            return CheckoutUtil.initiateCheckoutV6FromComponent(component, checkout, dropinConfiguration).mount(CheckoutUtil.MOUNT_CONTAINER)
        } else if (version.startsWith("5.")) {
            //For 5.x.x paymentsMethodsConfiguration is part of the checkoutConfiguration
            checkoutConfiguration.paymentMethodsConfiguration = dropinConfiguration.paymentMethodsConfiguration
            const checkout = await window.AdyenCheckout(checkoutConfiguration);
            return checkout.create(component).mount(CheckoutUtil.MOUNT_CONTAINER);
        } else {
            console.error("Unsupported checkout version");
        }

    }

    static async mountSavedCards(checkoutConfiguration, dropinConfiguration, version) {

        if (version.startsWith("6.")) {
            const { AdyenCheckout } = window.AdyenWeb;
            const checkout = await AdyenCheckout(checkoutConfiguration);
            //TBD
        } else if (version.startsWith("5.")) {
            //For 5.x.x paymentsMethodsConfiguration is part of the checkoutConfiguration
            checkoutConfiguration.paymentMethodsConfiguration = dropinConfiguration.paymentMethodsConfiguration
            const checkout = await window.AdyenCheckout(checkoutConfiguration);
            const storedPaymentMethod = checkout.paymentMethodsResponse.storedPaymentMethods[0];
            return checkout.create("card", storedPaymentMethod).mount(CheckoutUtil.SAVED_CARD_MOUNT_CONTAINER);
        } else {
            console.error("Unsupported checkout version");
        }

    }


    static initiateCheckoutV6FromComponent(component, checkout, dropinConfiguration) {
        switch (component) {
            case "applepay":
                return new window.AdyenWeb.ApplePay(checkout, dropinConfiguration.paymentMethodsConfiguration.applepay);
            case "googlepay":
                return new window.AdyenWeb.GooglePay(checkout, dropinConfiguration.paymentMethodsConfiguration.googlepay);
            case "card":
                return new window.AdyenWeb.Card(checkout, dropinConfiguration.paymentMethodsConfiguration.card);
            case "securedfields":
                return new window.AdyenWeb.CustomCard(checkout, dropinConfiguration.paymentMethodsConfiguration.card);
            default:
                return new window.AdyenWeb.Dropin(checkout, dropinConfiguration);
        }
    }

}