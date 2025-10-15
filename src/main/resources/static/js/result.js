window.onload = function () {
    const params = new URLSearchParams(window.location.search);
    let data = params.get('result');
    if (data === null) {
        data = document.getElementById("base64Result").innerHTML;
    }
    const decodedString = atob(data);
    console.log("Decoded Result: " + decodedString);
    const jsonData = JSON.parse(decodedString);

    const resultImage = document.getElementById("result-image");
    resultImage.src = jsonData["resultCode"] === "Authorised" ? "/images/success.svg" : "/images/error.svg";

    const tableBody = document.getElementById("result-table");
    Object.entries(jsonData).forEach(([key, value]) => {
        const row = document.createElement("tr");

        const keyCell = document.createElement("td");
        keyCell.textContent = key;
        row.appendChild(keyCell);

        // Create a cell for the value
        const valueCell = document.createElement("td");

        // Insert into input field
        const inputField = document.createElement("input");
        inputField.value = JSON.stringify(value).replaceAll('"', '');
        inputField.className = "input-field";
        inputField.disabled = true;

        valueCell.appendChild(inputField);
        row.appendChild(valueCell);

        if (inputField.value !== "null") {
            tableBody.appendChild(row);
        }
    });

};