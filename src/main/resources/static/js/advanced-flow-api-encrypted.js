//Utility Class Import
import { PaymentsUtil } from '../js-util/paymentsUtil.js';
import { CheckoutUtil } from '../js-util/checkoutUtil.js';

var cardData = {};
var cardComponent = {};

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

    //Initialize Existing cards
    const paymentMethodsResponse = await PaymentsUtil.makePaymentMethodsCall(merchantId, shopperId, amount, currency, countryCode, locale);
    if (paymentMethodsResponse.storedPaymentMethods && paymentMethodsResponse.storedPaymentMethods.length > 0) {

        const existingCardTable = document.querySelector("#existingCardTable");
        existingCardTable.innerHTML = "";

        paymentMethodsResponse.storedPaymentMethods.forEach((storedPaymentMethod, index) => {
            const row = document.createElement("tr");
            row.id = storedPaymentMethod.id;

            row.innerHTML = `
                <td>
                    <input type="radio" name="cardOption" value=${storedPaymentMethod.id} ${index === 0 ? "checked" : ""}>
                    <label for="securefield-${storedPaymentMethod.id}">${storedPaymentMethod.brand} ****${storedPaymentMethod.lastFour}</label>
                </td>
                <td><div class="adyen-securefields" ${index === 0 ? '' : 'style="visibility: hidden;"'} id="div-${storedPaymentMethod.id}"><span data-cse="encryptedSecurityCode" id="securefield-${storedPaymentMethod.id}"></span></div></td>
            `;
            existingCardTable.appendChild(row);
        });

        document.getElementById('existingCardRadio').disabled = false;
        document.getElementById('existingCardRadio').checked = true;
        document.getElementById('newCardRadio').checked = false;

    } else {
        document.getElementById('existingCardRadio').disabled = true;
        document.getElementById('newCardRadio').checked = true;
        document.getElementById('existingCardRadio').checked = false;
    }
    handlePaymentOptionSelected();
    addCardRadioEventListener();

    //Initialize Adyen checkout
    const configuration = {
        clientKey: await PaymentsUtil.getCredentials(merchantId),
        environment: merchantEnvironment,
        countryCode: countryCode,
        locale: locale,
        onChange: handleOnChange
    };

    const dropinConfiguration = CheckoutUtil.getDropinConfiguration(amount, currency, countryCode);
    cardComponent = await CheckoutUtil.mountCheckout('securedfields', configuration, dropinConfiguration, checkoutVersion);

    const inputForm = document.getElementById("inputForm");
    const checkoutForm = document.getElementById("checkoutForm");
    inputForm.style.display = "none";
    checkoutForm.style.display = "block";

}

async function handleOnChange(data) {
    const isExistingCardInput = document.getElementById('existingCardRadio').checked;
    if ((isExistingCardInput && data.valid && data.valid.encryptedSecurityCode) || (!isExistingCardInput && data.isValid)) {
        console.log("Valid Card Data:", data);
        PaymentsUtil.enableWithMessage("payButton", "Pay");
    } else {
        console.log("Valid Card Data:", data)
        PaymentsUtil.disableWithMessage("payButton", "Pay");
    }
    cardData = data;
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

        const useExistingCard = document.getElementById('existingCardRadio').checked;
        if (useExistingCard) {
            cardData.data.paymentMethod.encryptedExpiryMonth = null;
            cardData.data.paymentMethod.encryptedExpiryYear = null;
            cardData.data.paymentMethod.encryptedCardNumber = null;
            cardData.data.paymentMethod.storedPaymentMethodId = getSelectedCardId();
        }

        const result = await PaymentsUtil.makePaymentsCall(cardData.data, merchantId, shopperId, amount, currency, countryCode, locale, tdsMode, origin, savePaymentMethod);
        if (!result.action) {
            CheckoutUtil.onPaymentEvent(result, "securefields");
        } else if (result.action.type == "redirect"){
            PaymentsUtil.disableWithMessage("payButton", "Redirecting...");
            cardComponent.handleAction(result.action);
        } else {
            console.log("Error: Unknown action:", result.action);
        }

    });
    document.querySelectorAll('input[name="paymentOption"]').forEach((radio) => {
        radio.addEventListener('change', function () {
            handlePaymentOptionSelected();
        });
    });
};

function getSelectedCardId() {
    const selected = [...document.querySelectorAll('input[name="cardOption"]')]
        .find(radio => radio.checked);
    return selected ? selected.value : null;
}

function addCardRadioEventListener() {
    document.querySelectorAll('input[name="cardOption"]').forEach((radio) => {
        radio.addEventListener('change', function () {
            document.querySelectorAll('input[name="cardOption"]').forEach((radio) => {
                const securityCodeField = document.getElementById('div-' + radio.value);
                if (radio.checked) {
                    securityCodeField.style.visibility = '';
                } else {
                    securityCodeField.style.visibility = 'hidden';
                }
            });
        });
    });
}

function handlePaymentOptionSelected() {
    const existingCardRadio = document.getElementById('existingCardRadio');
    const newCardForm = document.getElementById('newCardForm');
    const existingCardForm = document.getElementById('existingCardForm');
    if (existingCardRadio.checked) {
        newCardForm.style.display = 'none';
        existingCardForm.style.display = 'block';
    } else {
        newCardForm.style.display = 'block';
        existingCardForm.style.display = 'none';
    }
    handleOnChange(cardData);
}

PaymentsUtil.populatePaymentOptions();