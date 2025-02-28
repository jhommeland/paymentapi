
async function getMerchants() {
    try {
        // Direct use of await to wait for the response
        const response = await axios.get('/merchants');

        // Log and return the server response data
        console.log('Success:', response.data);
        return response.data;
    } catch (error) {
        // Handle error and return null in case of failure
        console.error('Error:', error);
        return null;
    }
}

async function initializeDropdown() {
    const merchants = await getMerchants();
    merchants.forEach(merchant => {
        const option = document.createElement("option");
        option.value = merchant.merchantId;
        option.textContent = merchant.adyenMerchantAccount;
        merchantDropdown.appendChild(option);
    });

    const savedValue = localStorage.getItem("selectedMerchant");
    if (savedValue) {
        // Set the dropdown to the saved value
        merchantDropdown.value = savedValue;
        console.log("Restored saved value:", savedValue);
    }
}

const merchantDropdown = document.getElementById("merchant-select");

merchantDropdown.addEventListener("change", function(event) {
    const selectedValue = event.target.value;
    localStorage.setItem("selectedMerchant", selectedValue);
    console.log("Saved to localStorage:", selectedValue);
});

initializeDropdown();
