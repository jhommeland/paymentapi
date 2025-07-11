//Utility Class Import
import { PaymentsUtil } from './paymentsUtil.js';
import { CheckoutUtil } from './checkoutUtil.js';

async function initializeCheckout() {

    PaymentsUtil.disableWithMessage("startPaymentButton", "Loading...");

    const amount = document.getElementById("amount").value
    const currency = document.getElementById("currency").value;
    const locale = document.getElementById("language").value;
    const tdsMode = document.getElementById("tdsMode").value;
    const countryCode = document.getElementById("country").value;
    const savePaymentMethod = document.getElementById("savePaymentMethod").value;
    const origin = window.location.origin;

    const checkoutVersion = localStorage.getItem("selectedCheckoutVersion");
    const merchantId = localStorage.getItem("selectedMerchant");
    const shopperId = localStorage.getItem("selectedShopper");
    const merchantEnvironment = localStorage.getItem("selectedMerchantEnvironment");

    const paymentMethodsResponse = await PaymentsUtil.makePaymentMethodsCall(merchantId, shopperId, amount, currency, countryCode, locale);
    const configuration = {
        clientKey: await PaymentsUtil.getCredentials(merchantId),
        environment: merchantEnvironment,
        countryCode: countryCode,
        locale: locale,
        paymentMethodsResponse: paymentMethodsResponse,
        onSubmit: async (state, component, actions) => {
            await CheckoutUtil.onSubmitPayment(state, component, actions, merchantId, shopperId, amount, currency, countryCode, locale, tdsMode, origin, savePaymentMethod);
        },
        onAdditionalDetails: async (state, component, actions) => {
            await CheckoutUtil.onAdditionalDetails(merchantId, state, component, actions);
        },
        onPaymentCompleted: (result, component) => {
            CheckoutUtil.onPaymentEvent(result, component);
        },
        onPaymentFailed: (result, component) => {
            CheckoutUtil.onPaymentEvent(result, component);
        },
        onError: (error, component) => {
            CheckoutUtil.onPaymentEvent(error, component);
        }
    };

    const dropinConfiguration = CheckoutUtil.getDropinConfiguration(amount, currency, countryCode);
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