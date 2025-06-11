//Utility Class Import
import { PaymentsUtil } from './paymentsUtil.js';

const PAYMENT_STATUS_POLL_COUNT = 30;

async function initializeTerminalPayment() {

    document.getElementById("inputForm").style.display = "none";
    document.getElementById("progressForm").style.display = "block";
    document.getElementById("responseForm").style.display = "none";
    processPayment();

}

async function processPayment() {

    PaymentsUtil.disableWithMessage("startPaymentButton", "Loading...");

    const amount = document.getElementById("amount").value
    const currency = document.getElementById("currency").value;
    const poiId = document.getElementById("poiId").value
    const apiType = document.getElementById("apiType").value;
    const localEndpoint = document.getElementById("localEndpoint").value;
    const connectionTypeElement = document.getElementById("connectionType");
    const connectionTypeValue = connectionTypeElement.value;
    const connectionTypeText = connectionTypeElement.options[connectionTypeElement.selectedIndex].text;
    const connectionTimeout = document.getElementById("connectionTimeout").value;
    const savePaymentMethod = document.getElementById("savePaymentMethod").value;
    const merchantId = localStorage.getItem("selectedMerchant");
    const shopperId = localStorage.getItem("selectedShopper");
    const printReceipt = document.getElementById("printReceipt").value;

    let shouldLoop = true;
    let terminalResponse = null;
    while(shouldLoop) {
        const serviceId = PaymentsUtil.generateServiceId();
        localStorage.setItem("currentServiceId", serviceId);

        terminalResponse = await PaymentsUtil.makeTerminalPaymentCall(merchantId, serviceId, shopperId, poiId, amount, currency, apiType, localEndpoint, connectionTypeValue, printReceipt, connectionTimeout, savePaymentMethod);
        if (terminalResponse == null) {
            console.log("Payment call interrupted. Starting polling.")
            for (let i = 1; i < PAYMENT_STATUS_POLL_COUNT+1; i++) {
                console.log("Polling " + i);
                terminalResponse = await PaymentsUtil.makeTerminalPaymentStatusCall(merchantId, poiId, serviceId, apiType, localEndpoint);
                if (terminalResponse.reason != "InProgress") {
                    break;
                }
            }
        }

        const shouldCancel = document.getElementById("cancel-button").disabled;
        shouldLoop = (connectionTypeText === "SynchronousLoop") && !shouldCancel;
    }



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

    document.getElementById("inputForm").style.display = "none";
    document.getElementById("progressForm").style.display = "none";
    document.getElementById("responseForm").style.display = "block";

}

window.onload = function() {
    document.getElementById("paymentForm").addEventListener("submit", function(event) {
        event.preventDefault(); // Prevent the default form submission
        initializeTerminalPayment();
    });
    document.getElementById("cancel-button").addEventListener("click", function(event) {
        document.getElementById("cancel-button").disabled = true;
        const merchantId = localStorage.getItem("selectedMerchant");
        const poiId = document.getElementById("poiId").value
        const serviceId = localStorage.getItem("currentServiceId");
        const apiType = document.getElementById("apiType").value;
        const localEndpoint = document.getElementById("localEndpoint").value;
        PaymentsUtil.makeTerminalPaymentAbortCall(merchantId, poiId, serviceId, apiType, localEndpoint);
    });
    document.getElementById("posDemoButton").addEventListener("click", function(event) {
        let settings = {};
        settings.currency = document.getElementById("currency").value;
        settings.poiId = document.getElementById("poiId").value;
        settings.apiType = document.getElementById("apiType").value;
        settings.localEndpoint = document.getElementById("localEndpoint").value;
        settings.connectionType = document.getElementById("connectionType").value;
        settings.connectionTimeout = document.getElementById("connectionTimeout").value;
        settings.savePaymentMethod = document.getElementById("savePaymentMethod").value;
        settings.merchantId = localStorage.getItem("selectedMerchant");
        settings.shopperId = localStorage.getItem("selectedShopper");
        settings.printReceipt = document.getElementById("printReceipt").value;

        let settingsBase64 = btoa(JSON.stringify(settings));
        location.href = `/demos/pos-demo.html?settings=${settingsBase64}`;

    });
    document.getElementById("apiType").addEventListener("change", function(event) {
        if (document.getElementById("apiType").value === "cloud") {
            document.getElementById("localEndpointRow").hidden = true;
            document.getElementById("localEndpoint").required = false;
        } else {
            document.getElementById("localEndpointRow").hidden = false;
            document.getElementById("localEndpoint").required = true;
        }
    });
};

PaymentsUtil.populateTerminalOptions();
