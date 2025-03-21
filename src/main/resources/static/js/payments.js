//Utility Class Import
import { PaymentsUtil } from './paymentsUtil.js';

async function initializeCheckout() {

    const amount = document.getElementById("amount").value
    const currency = document.getElementById("currency").value;
    const locale = document.getElementById("language").value;
    const tdsMode = document.getElementById("tdsMode").value;
    const countryCode = document.getElementById("country").value;
    const savePaymentMethod = document.getElementById("savePaymentMethod").value;
    const origin = window.location.origin;

    const merchantId = localStorage.getItem("selectedMerchant");
    const shopperId = localStorage.getItem("selectedShopper");

    const paymentMethodsResponse = await PaymentsUtil.makePaymentMethodsCall(merchantId, shopperId, amount, currency, countryCode, locale);
    const configuration = {
        clientKey: await PaymentsUtil.getCredentials(merchantId),
        environment: "test",
        countryCode: countryCode,
        locale: locale,
        paymentMethodsResponse: paymentMethodsResponse,
        onSubmit: async (state, component, actions) => {
            await PaymentsUtil.onSubmitPayment(state, component, actions, merchantId, shopperId, amount, currency, countryCode, locale, tdsMode, origin, savePaymentMethod);
        },
        onAdditionalDetails: async (state, component, actions) => {
            await PaymentsUtil.onAdditionalDetails(merchantId, state, component, actions);
        },
        onPaymentCompleted: (result, component) => {
            PaymentsUtil.onPaymentEvent(result, component);
        },
        onPaymentFailed: (result, component) => {
            PaymentsUtil.onPaymentEvent(result, component);
        },
        onError: (error, component) => {
            PaymentsUtil.onPaymentEvent(error, component);
        }
    };

    const { AdyenCheckout, Dropin, Card } = window.AdyenWeb;
    const checkout = await AdyenCheckout(configuration);
    const dropin = new Dropin(checkout, PaymentsUtil.getDropinConfiguration()).mount('#dropin-container')

    const inputForm = document.getElementById("inputForm");
    const checkoutForm = document.getElementById("checkoutForm");
    inputForm.style.display = "none";
    checkoutForm.style.display = "block";
}

window.onload = function() {
    document.getElementById("paymentForm").addEventListener("submit", function(event) {
        event.preventDefault(); // Prevent the default form submission
        initializeCheckout();
    });
};

PaymentsUtil.populatePaymentOptions();