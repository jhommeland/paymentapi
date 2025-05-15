//Utility Class Import
import { PaymentsUtil } from './paymentsUtil.js';

// DOM references
const reportTable = document.querySelector("#elementTable tbody");
const reportTypeFilter = document.getElementById("reportTypeFilter");
const merchantFilter = document.getElementById("merchantFilter");
const reportCount = document.getElementById("reportCount");

// Register utility methods
window.capturePayment = PaymentsUtil.capturePayment;
window.revertPayment = PaymentsUtil.revertPayment;

// Populate the table with transaction data
function populateTable(reports) {

  // Clear table rows
  reportTable.innerHTML = "";

  // Add rows
  reports.forEach((report) => {
    const row = document.createElement("tr");

    row.innerHTML = `
      <td>${report.reportType}</td>
      <td>${new Date(report.creationDate).toLocaleString()}</td>
      <td><a href="${report.reportLink}" target="_blank" download>${report.filename}</a></td>
      <td>
        <button onclick="">Reconcile</button>
      </td>
    `;

    reportTable.appendChild(row);
  });
}

function filterReports(reports) {
  const merchantFilterValue = merchantFilter.value
  const reportTypeFilterValue = reportTypeFilter.value;

  let filteredReports = reports;
  filteredReports = PaymentsUtil.filterElements("merchantAccountName", merchantFilterValue, filteredReports);
  filteredReports = PaymentsUtil.filterElements("reportType", reportTypeFilterValue, filteredReports);
  populateTable(filteredReports)
  reportCount.textContent = filteredReports.length;
}

function populateDropdown(elements) {
  PaymentsUtil.populateFilterDropdown("merchantAccountName", merchantFilter, elements)
  PaymentsUtil.populateFilterDropdown("reportType", reportTypeFilter, elements)
}

async function initializeTable() {
  let reports = await PaymentsUtil.getReports();
  populateTable(reports);
  populateDropdown(reports);
  filterReports(reports);
  merchantFilter.addEventListener("change", (event) => filterReports(reports));
  reportTypeFilter.addEventListener("change", (event) => filterReports(reports));
}

initializeTable();