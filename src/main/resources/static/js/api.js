//Utility Class Import
import { PaymentsUtil } from './paymentsUtil.js';
import { CheckoutUtil } from './checkoutUtil.js';

var cardData = {};

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

    const configuration = {
        clientKey: await PaymentsUtil.getCredentials(merchantId),
        environment: merchantEnvironment,
        countryCode: countryCode,
        locale: locale,
        onChange: async (data) => {
            if (data.isValid) {
                PaymentsUtil.enableWithMessage("payButton", "Pay");
            } else {
                PaymentsUtil.disableWithMessage("payButton", "Pay");
            }
            cardData = data.data;
        }
    };

    const dropinConfiguration = CheckoutUtil.getDropinConfiguration(amount, currency, countryCode);
    await CheckoutUtil.mountCheckout('securedfields', configuration, dropinConfiguration, checkoutVersion);

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
    document.getElementById("checkoutForm").addEventListener("submit", async function(event) {
        event.preventDefault(); // Prevent the default form submission

        PaymentsUtil.disableWithMessage("payButton", "Loading...");

        const merchantId = localStorage.getItem("selectedMerchant");
        const shopperId = localStorage.getItem("selectedShopper");
        const amount = document.getElementById("amount").value
        const currency = document.getElementById("currency").value;
        const countryCode = document.getElementById("country").value;
        const locale = document.getElementById("language").value;
        const tdsMode = document.getElementById("tdsMode").value;
        const origin = window.location.origin;
        const savePaymentMethod = document.getElementById("savePaymentMethod").value;

        console.log("Card Data:", cardData)

        const result = await PaymentsUtil.makePaymentsCall(cardData, merchantId, shopperId, amount, currency, countryCode, locale, tdsMode, origin, savePaymentMethod);
        CheckoutUtil.onPaymentEvent(result, "securefields");
    });
};

PaymentsUtil.populatePaymentOptions();