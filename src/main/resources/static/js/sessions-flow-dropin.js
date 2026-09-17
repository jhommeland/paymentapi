//Utility Class Import
import { PaymentsUtil } from '../js-util/paymentsUtil.js';
import { CheckoutUtil } from '../js-util/checkoutUtil.js';

var adyenComponent = null;
var cardLastFour = "";
var cardBrand = "";
const inputForm = document.getElementById("inputForm");
const checkoutForm = document.getElementById("checkoutForm");
const confirmationForm = document.getElementById("confirmationForm");
const nextButton = document.getElementById("nextButton");
const cardInformation = document.getElementById("cardInformation");

async function initializeCheckout() {

    PaymentsUtil.disableWithMessage("startPaymentButton", "Loading...");

    const amount = document.getElementById("amount").value
    const currency = document.getElementById("currency").value;
    const locale = document.getElementById("language").value;
    const tdsMode = document.getElementById("tdsMode").value;
    const countryCode = document.getElementById("country").value;
    const savePaymentMethod = document.getElementById("savePaymentMethod").value;
    const sessionsMode = document.getElementById("sessionsMode").value;

    const checkoutVersion = localStorage.getItem("selectedCheckoutVersion");
    const merchantId = localStorage.getItem("selectedMerchant");
    const shopperId = localStorage.getItem("selectedShopper");
    const merchantEnvironment = localStorage.getItem("selectedMerchantEnvironment");

    const sessionsResponse = await PaymentsUtil.makeSessionsCall(merchantId, shopperId, amount, currency, countryCode, locale, tdsMode, savePaymentMethod, sessionsMode);
    if (sessionsMode === "hosted") {
        PaymentsUtil.disableWithMessage("startPaymentButton", "Redirecting...");
        window.location.href = sessionsResponse.url;
        return;
    }

    const configuration = {
        session: {
            id: sessionsResponse.id,
            sessionData: sessionsResponse.sessionData
        },
        clientKey: await PaymentsUtil.getCredentials(merchantId),
        environment: merchantEnvironment,
        countryCode: countryCode,
        locale: locale,
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

    var dropinConfiguration = CheckoutUtil.getDropinConfiguration(amount, currency, countryCode);
    if (sessionsMode === "two-step") {
        dropinConfiguration.paymentMethodsConfiguration.card.showPayButton = false;
        dropinConfiguration.paymentMethodsConfiguration.card.onBinLookup =  (binData) => {
            console.log("BIN data retrieved:", binData)
            cardBrand = binData.detectedBrands.at(0);
        }
        dropinConfiguration.paymentMethodsConfiguration.card.onFieldValid = (data) => {
            console.log("Field validation:", data)
            if (data.encryptedFieldName === "encryptedCardNumber") {
                cardLastFour = data.endDigits;
            }
        }
    }
    adyenComponent = await CheckoutUtil.mountCheckout('dropin', configuration, dropinConfiguration, checkoutVersion);

    inputForm.style.display = "none";
    checkoutForm.style.display = "block";

    if (sessionsMode === "two-step") {
        nextButton.style.display = "block";
    }
}

window.onload = function() {
    document.getElementById("paymentForm").addEventListener("submit", function(event) {
        event.preventDefault(); // Prevent the default form submission
        initializeCheckout();
    });
    document.getElementById("nextButton").addEventListener("click", function(event) {
        adyenComponent.showValidation();
        if (adyenComponent.isValid) {
            console.log(adyenComponent);
            checkoutForm.style.display = "none";
            confirmationForm.style.display = "block";
            nextButton.style.display = "none";
            cardInformation.innerHTML = `
                <h2>${cardBrand} ****${cardLastFour}</h2>
            `
        }
    });
    document.getElementById("paymentButton").addEventListener("click", function(event) {
        if (adyenComponent.isValid) {
            PaymentsUtil.disableWithMessage("paymentButton", "Loading...");
        }
        adyenComponent.submit();
    });
};

PaymentsUtil.populatePaymentOptions();

