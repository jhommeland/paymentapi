
let merchants = [];
let shoppers = [];

async function getMerchants() {
    try {
        // Direct use of await to wait for the response
        const response = await axios.get('/merchants');

        // Log and return the server response data
        console.log('Merchants Retrieved:', response.data);
        return response.data;
    } catch (error) {
        // Handle error and return null in case of failure
        console.error('Error:', error);
        return null;
    }
}

async function getShoppers() {
    try {
        // Direct use of await to wait for the response
        const response = await axios.get('/shoppers');

        // Log and return the server response data
        console.log('Shoppers Retrieved:', response.data);
        return response.data;
    } catch (error) {
        // Handle error and return null in case of failure
        console.error('Error:', error);
        return null;
    }
}

async function initializeDropdown() {
    merchants = await getMerchants();
    merchants.forEach(merchant => {
        const option = document.createElement("option");
        option.value = merchant.id;
        option.textContent = merchant.adyenMerchantAccount;
        merchantDropdown.appendChild(option);
    });

    shoppers = await getShoppers();
    shoppers.forEach(shopper => {
        const option = document.createElement("option");
        option.value = shopper.id;
        option.textContent = shopper.firstName + " " + shopper.lastName;
        shopperDropdown.appendChild(option);
    });

    const selectedMerchant = localStorage.getItem("selectedMerchant");
    if (selectedMerchant) {
        merchantDropdown.value = selectedMerchant;
    }
    shopperDropdown.value = localStorage.getItem("selectedShopper");
}

const merchantDropdown = document.getElementById("merchant-select");
const shopperDropdown = document.getElementById("shopper-select");

merchantDropdown.addEventListener("change", function(event) {
    const selectedValue = event.target.value;
    localStorage.setItem("selectedMerchant", selectedValue);
    merchants.forEach(merchant => {
        if (merchant.id === selectedValue) {
            localStorage.setItem("selectedMerchantSettings", merchant.merchantSettings);
        }
    });

    window.location.reload();
});
shopperDropdown.addEventListener("change", function(event) {
    const selectedValue = event.target.value;
    localStorage.setItem("selectedShopper", selectedValue);
    shopper.forEach(shopper => {
        if (shopper.id === selectedValue) {
            localStorage.setItem("selectedShopperSettings", shopper.shopperSettings);
        }
    });

    window.location.reload();
});

initializeDropdown();
