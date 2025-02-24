// DOM references
const transactionTable = document.querySelector("#transactionTable tbody");
const statusFilter = document.getElementById("statusFilter");

async function getTransactions() {
  try {
    // Direct use of await to wait for the response
    const response = await axios.get('/transactions');

    // Log and return the server response data
    console.log('Success:', response.data);
    return response.data;
  } catch (error) {
    // Handle error and return null in case of failure
    console.error('Error:', error);
    return null;
  }
}

async function capturePayment(transactionId) {
  try {
    // Direct use of await to wait for the response
    const response = await axios.post('/payments/capture', {
      transactionId: transactionId
    });

    // Log the response and reload page
    console.log('Success:', response.data);
    window.location.reload();
  } catch (error) {
    // Handle error and return null in case of failure
    console.error('Error:', error);
  }
}

async function revertPayment(transactionId) {
  try {
    // Direct use of await to wait for the response
    const response = await axios.post('/payments/reverse', {
      transactionId: transactionId
    });

    // Log the response and reload page
    console.log('Success:', response.data);
    window.location.reload();
  } catch (error) {
    // Handle error and return null in case of failure
    console.error('Error:', error);
  }
}

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
      <td>${transaction.errorReason || ""}</td>
      <td>${new Date(transaction.createdAt).toLocaleString()}</td>
      <td>${new Date(transaction.lastModifiedAt).toLocaleString()}</td>
      <td>
        <button style="margin-bottom: 5px;" ${enableCapture ? "" : "disabled"} onclick="capturePayment('${transaction.transactionId}')">Capture</button>
        <br>
        <button ${enableRevert ? "" : "disabled"} onclick="revertPayment('${transaction.transactionId}')">Revert</button>
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
  let transactions = await getTransactions();
  populateTable(transactions);
  populateDropdown(transactions);
  statusFilter.addEventListener("change", (event) => filterTransactions(transactions));
}

initializeTable();