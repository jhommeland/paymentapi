
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

    //Select Merchant Dropdown
    merchants = await getMerchants();
    merchants.forEach(merchant => {
        const textPrefix = merchant.livePrefix ? "[LIVE]" : "[TEST]";
        const option = document.createElement("option");
        option.value = merchant.id;
        option.textContent = textPrefix + " " + merchant.adyenMerchantAccount;
        merchantDropdown.appendChild(option);
    });
    const selectedMerchant = localStorage.getItem("selectedMerchant");
    if (selectedMerchant) {
        merchantDropdown.value = selectedMerchant;
    }
    refreshMerchantLocalStorage(merchants, selectedMerchant);

    //Select Shopper Dropdown
    shoppers = await getShoppers();
    shoppers.forEach(shopper => {
        const option = document.createElement("option");
        option.value = shopper.id;
        option.textContent = shopper.firstName + " " + shopper.lastName;
        shopperDropdown.appendChild(option);
    });
    shopperDropdown.value = localStorage.getItem("selectedShopper");

    //Select Checkout version dropdown
    checkoutVersionDropdown.value = localStorage.getItem("selectedCheckoutVersion");

    //Set red background color if live
    if (merchantDropdown.options[merchantDropdown.selectedIndex].text.startsWith("[LIVE]")) {
        document.body.style.backgroundColor = "lightcoral";
    } else {
        document.body.style.backgroundColor = "";
    }
}

const merchantDropdown = document.getElementById("merchant-select");
const shopperDropdown = document.getElementById("shopper-select");
const checkoutVersionDropdown = document.getElementById("checkout-version-select");

merchantDropdown.addEventListener("change", async function(event) {
    const selectedValue = event.target.value;
    localStorage.setItem("selectedMerchant", selectedValue);
    merchants = await getMerchants();
    refreshMerchantLocalStorage(merchants, selectedValue);

    window.location.reload();
});
shopperDropdown.addEventListener("change", function(event) {
    const selectedValue = event.target.value;
    localStorage.setItem("selectedShopper", selectedValue);
    shoppers.forEach(shopper => {
        if (shopper.id === selectedValue) {
            localStorage.setItem("selectedShopperSettings", shopper.shopperSettings);
        }
    });

    window.location.reload();
});
checkoutVersionDropdown.addEventListener("change", function(event) {
    const selectedValue = event.target.value;
    localStorage.setItem("selectedCheckoutVersion", selectedValue);
    window.location.reload();
});

function refreshMerchantLocalStorage(merchants, selectedValue) {
    merchants.forEach(merchant => {
        if (merchant.id === selectedValue) {
            localStorage.setItem("selectedMerchantSettings", merchant.merchantSettings);
            localStorage.setItem("selectedMerchantEnvironment", merchant.livePrefix ? "live" : "test");
        }
    });
}

initializeDropdown();
