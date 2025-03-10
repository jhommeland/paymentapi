//Utility Class Import
import { PaymentsUtil } from './paymentsUtil.js';

const PAYMENT_STATUS_POLL_COUNT = 30;

async function initializeTerminalPayment() {

    const amount = document.getElementById("amount").value
    const currency = document.getElementById("currency").value;
    const locale = document.getElementById("language").value;
    const poiId = document.getElementById("poiId").value
    const requestMode = document.getElementById("requestMode").value;
    const requestTimeout = document.getElementById("requestTimeout").value;
    const merchantId = localStorage.getItem("selectedMerchant");

    const serviceId = PaymentsUtil.generateServiceId();

    let terminalResponse = await PaymentsUtil.makeTerminalPaymentCall(merchantId, serviceId, poiId, amount, currency, locale, requestMode, requestTimeout);
    if (terminalResponse == null) {
        console.log("Payment call interrupted. Starting polling.")
        for (let i = 1; i < PAYMENT_STATUS_POLL_COUNT+1; i++) {
            console.log("Polling " + i);
            terminalResponse = await PaymentsUtil.makeTerminalPaymentStatusCall(merchantId, poiId, serviceId);
            if (terminalResponse.reason != "InProgress") {
                break;
            }
        }
    }

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
