//Utility Class Import
import { PaymentsUtil } from './paymentsUtil.js';

// DOM references
const transactionTable = document.querySelector("#transactionTable tbody");
const statusFilter = document.getElementById("statusFilter");

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
      <td>${transaction.transactionId}</td>
      <td>${transaction.merchantAccountName}</td>
      <td>${transaction.paymentMethod}</td>
      <td>${transaction.status}</td>
      <td>${transaction.amount}</td>
      <td>${transaction.currency}</td>
      <td>${transaction.originalPspReference || ""}</td>
      <td>${transaction.pspReference || ""}</td>
      <td>${transaction.errorReason || ""}</td>
      <td>${new Date(transaction.createdAt).toLocaleString()}</td>
      <td>${new Date(transaction.lastModifiedAt).toLocaleString()}</td>
      <td>
        <button ${enableCapture ? "" : "disabled"} onclick="PaymentsUtil.capturePayment('${transaction.transactionId}')">Capture</button>
        <button ${enableRevert ? "" : "disabled"} onclick="PaymentsUtil.revertPayment('${transaction.transactionId}')">Revert</button>
      </td>
    `;

    transactionTable.appendChild(row);
  });
}

function populateDropdown(transactions) {
  const uniqueStatuses = new Set(transactions.map((transaction) => transaction.status));
  Array.from(uniqueStatuses).map((status) => {
    const option = document.createElement('option');
    option.value = status;
    option.textContent = status;
    statusFilter.appendChild(option);
  });
}

function filterTransactions(transactions) {
  const filterValue = statusFilter.value;

  if (filterValue === "all") {
    populateTable(transactions);
  } else {
    const filteredTransactions = transactions.filter(
        (transaction) => transaction.status === filterValue
    );
    populateTable(filteredTransactions);
  }
}

async function initializeTable() {
  let transactions = await PaymentsUtil.getTransactions();
  populateTable(transactions);
  populateDropdown(transactions);
  statusFilter.addEventListener("change", (event) => filterTransactions(transactions));
}

initializeTable();