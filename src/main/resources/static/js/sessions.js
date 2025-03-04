//Utility Class Import
import { PaymentsUtil } from './paymentsUtil.js';

async function initializeCheckout() {

    const amount = document.getElementById("amount").value
    const currency = document.getElementById("currency").value;
    const locale = document.getElementById("language").value;
    const countryCode = locale.split('-')[1];

    const merchantId = localStorage.getItem("selectedMerchant");

    const sessionsResponse = await PaymentsUtil.makeSessionsCall(merchantId, amount, currency, countryCode, locale);
    const configuration = {
        session: {
            id: sessionsResponse.id,
            sessionData: sessionsResponse.sessionData
        },
        clientKey: await PaymentsUtil.getCredentials(merchantId),
        environment: "test",
        countryCode: countryCode,
        locale: locale,
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

