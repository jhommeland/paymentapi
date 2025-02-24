window.onload = function () {
    const params = new URLSearchParams(window.location.search);
    let data = params.get('result');
    if (data === null) {
        data = document.getElementById("base64Result").innerHTML;
    }
    const decodedString = atob(data);
    console.log(decodedString);
    const jsonData = JSON.parse(decodedString);
    const tableBody = document.getElementById("result-table");

    Object.entries(jsonData).forEach(([key, value]) => {
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

};