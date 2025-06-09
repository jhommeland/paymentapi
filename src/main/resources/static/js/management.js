//Utility Class Import
import { PaymentsUtil } from './paymentsUtil.js';

// DOM references
const transactionTable = document.querySelector("#elementTable tbody");
const statusFilter = document.getElementById("statusFilter");
const merchantFilter = document.getElementById("merchantFilter");
const paymentMethodFilter = document.getElementById("paymentMethodFilter");
const autoRefresh = document.getElementById("autoRefresh");
const transactionCount = document.getElementById("transactionCount");

// Register utility methods
window.capturePayment = PaymentsUtil.capturePayment;
window.revertPayment = PaymentsUtil.revertPayment;

// Global variables
let transactions = [];
let intervalId = 0;

// Populate the table with transaction data
function populateTable(transactionData) {

  // Clear table rows
  transactionTable.innerHTML = "";

  // Add rows
  transactionData.forEach((transaction) => {
    const row = document.createElement("tr");
    row.id = transaction.id;

    const enableCapture = transaction.status === 'AUTHORISATION' && transaction.errorReason === null;
    const enableRevert = (transaction.status === 'AUTHORISATION' || transaction.status === 'CAPTURE') && transaction.errorReason === null;

    row.innerHTML = `
      <td>${transaction.merchantReference}</td>
      <td>${transaction.paymentMethod}</td>
      <td>${transaction.status}</td>
      <td style="white-space: nowrap;">${transaction.currency} ${transaction.amount}</td>
      <td>${transaction.originalPspReference ? PaymentsUtil.toAdyenLink(transaction.originalPspReference) : ""}</td>
      <td>${transaction.pspReference ? PaymentsUtil.toAdyenLink(transaction.pspReference) : ""}</td>
      <td>${transaction.errorReason || ""}</td>
      <td>${new Date(transaction.createdAt).toLocaleString()}</td>
      <td>${new Date(transaction.lastModifiedAt).toLocaleString()}</td>
      <td>
        <button ${enableCapture ? "" : "disabled"} onclick="capturePayment('${transaction.id}')">Capture</button>
        <button ${enableRevert ? "" : "disabled"} onclick="revertPayment('${transaction.id}')">Revert</button>
      </td>
    `;

    row.classList.remove("update-highlight"); // Reset in case it's still applied
    void row.offsetWidth; // Trigger reflow to restart the animation
    row.classList.add("update-highlight");

    transactionTable.appendChild(row);
  });
}

function setAutoRefresh() {
  clearInterval(intervalId);
  intervalId = 0;
  const autoRefreshValue = autoRefresh.value;
    if (autoRefreshValue != "off") {
      intervalId = setInterval(autoRefreshTransactions, autoRefreshValue * 1000)
    }
}

function autoRefreshTransactions() {
  setTableData();
}

async function setTableData() {
  transactions = await PaymentsUtil.getTransactions();
  populateDropdown(transactions);
  filterTransactions(transactions);
}

function filterTransactions() {
  const merchantFilterValue = merchantFilter.value
  const statusFilterValue = statusFilter.value;
  const paymentMethodFilterValue = paymentMethodFilter.value;

  let filteredTransactions = transactions;
  filteredTransactions = PaymentsUtil.filterElements("merchantAccountName", merchantFilterValue, filteredTransactions);
  filteredTransactions = PaymentsUtil.filterElements("status", statusFilterValue, filteredTransactions);
  filteredTransactions = PaymentsUtil.filterElements("paymentMethod", paymentMethodFilterValue, filteredTransactions);
  populateTable(filteredTransactions)
  transactionCount.textContent = filteredTransactions.length;
}

async function initializeTable() {
  setTableData();
  merchantFilter.addEventListener("change", (event) => filterTransactions());
  statusFilter.addEventListener("change", (event) => filterTransactions());
  autoRefresh.addEventListener("change", (event) => setAutoRefresh());
  paymentMethodFilter.addEventListener("change", (event) => filterTransactions());
}

function populateDropdown(elements) {
  PaymentsUtil.populateFilterDropdown("merchantAccountName", merchantFilter, elements)
  PaymentsUtil.populateFilterDropdown("status", statusFilter, elements)
  PaymentsUtil.populateFilterDropdown("paymentMethod", paymentMethodFilter, elements)
}

initializeTable();