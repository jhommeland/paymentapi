//Utility Class Import
import { PaymentsUtil } from './paymentsUtil.js';

// DOM references
const transactionTable = document.querySelector("#transactionTable tbody");
const statusFilter = document.getElementById("statusFilter");
const merchantFilter = document.getElementById("merchantFilter");
const paymentMethodFilter = document.getElementById("paymentMethodFilter");
const transactionCount = document.getElementById("transactionCount");

// Register utility methods
window.capturePayment = PaymentsUtil.capturePayment;
window.revertPayment = PaymentsUtil.revertPayment;

// Populate the table with transaction data
function populateTable(transactions) {

  // Clear table rows
  transactionTable.innerHTML = "";

  // Add rows
  transactions.forEach((transaction) => {
    const row = document.createElement("tr");

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

    transactionTable.appendChild(row);
  });
}

function populateDropdown(transactions) {
  PaymentsUtil.populateTransactionFilterDropdown("merchantAccountName", merchantFilter, transactions)
  PaymentsUtil.populateTransactionFilterDropdown("status", statusFilter, transactions)
  PaymentsUtil.populateTransactionFilterDropdown("paymentMethod", paymentMethodFilter, transactions)
}

function filterTransactions(transactions) {
  const merchantFilterValue = merchantFilter.value
  const statusFilterValue = statusFilter.value;
  const paymentMethodFilterValue = paymentMethodFilter.value;

  let filteredTransactions = transactions;
  filteredTransactions = PaymentsUtil.filterTransactions("merchantAccountName", merchantFilterValue, filteredTransactions);
  filteredTransactions = PaymentsUtil.filterTransactions("status", statusFilterValue, filteredTransactions);
  filteredTransactions = PaymentsUtil.filterTransactions("paymentMethod", paymentMethodFilterValue, filteredTransactions);
  populateTable(filteredTransactions)
  transactionCount.textContent = filteredTransactions.length;
}

async function initializeTable() {
  let transactions = await PaymentsUtil.getTransactions();
  populateTable(transactions);
  populateDropdown(transactions);
  filterTransactions(transactions);
  merchantFilter.addEventListener("change", (event) => filterTransactions(transactions));
  statusFilter.addEventListener("change", (event) => filterTransactions(transactions));
  paymentMethodFilter.addEventListener("change", (event) => filterTransactions(transactions));
}

initializeTable();