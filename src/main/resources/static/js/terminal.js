//Utility Class Import
import { PaymentsUtil } from './paymentsUtil.js';

async function initializeTerminalPayment() {

    const amount = document.getElementById("amount").value
    const currency = document.getElementById("currency").value;
    const locale = document.getElementById("language").value;
    const poiId = document.getElementById("poiId").value
    const requestMode = document.getElementById("requestMode").value;
    const merchantId = localStorage.getItem("selectedMerchant");

    const terminalResponse = await PaymentsUtil.makeTerminalPaymentCall(merchantId, poiId, amount, currency, locale, requestMode);

    const inputForm = document.getElementById("inputForm");
    const checkoutForm = document.getElementById("responseForm");
    inputForm.style.display = "none";
    checkoutForm.style.display = "block";

    const tableBody = document.getElementById("result-table");
    Object.entries(terminalResponse).forEach(([key, value]) => {
        const row = document.createElement("tr");

        const keyCell = document.createElement("td");
        keyCell.textContent = key;
        row.appendChild(keyCell);

        // Create a cell for the value
        const valueCell = document.createElement("td");
        valueCell.textContent = JSON.stringify(value);
        row.appendChild(valueCell);

        tableBody.appendChild(row);
    });

}

window.onload = function() {
    document.getElementById("paymentForm").addEventListener("submit", function(event) {
        event.preventDefault(); // Prevent the default form submission
        initializeTerminalPayment();
    });
};

PaymentsUtil.populateTerminalOptions();
