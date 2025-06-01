//Utility Class Import
import { PaymentsUtil } from './paymentsUtil.js';
import { CheckoutUtil } from './checkoutUtil.js';

async function initializeCheckout() {

    const amount = document.getElementById("amount").value
    const currency = document.getElementById("currency").value;
    const locale = document.getElementById("language").value;
    const tdsMode = document.getElementById("tdsMode").value;
    const countryCode = document.getElementById("country").value;
    const savePaymentMethod = document.getElementById("savePaymentMethod").value;

    const checkoutVersion = localStorage.getItem("selectedCheckoutVersion");
    const merchantId = localStorage.getItem("selectedMerchant");
    const shopperId = localStorage.getItem("selectedShopper");

    const sessionsResponse = await PaymentsUtil.makeSessionsCall(merchantId, shopperId, amount, currency, countryCode, locale, tdsMode, savePaymentMethod);
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

    const dropinConfiguration = PaymentsUtil.getDropinConfiguration(amount, currency, countryCode);
    await CheckoutUtil.mountCheckout('dropin', configuration, dropinConfiguration, checkoutVersion);

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

