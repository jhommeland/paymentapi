import { PaymentsUtil } from '/js-util/paymentsUtil.js';

const PAYMENT_STATUS_POLL_COUNT = 30;

let totalAmount = 0;

async function doCheckout() {

    const params = new URLSearchParams(window.location.search);
    let settingsBase64 = params.get('settings');
    if (settingsBase64 === null) {
        return;
    }

    PaymentsUtil.disableWithMessage("checkoutButton", "Processing...");

    let settings = JSON.parse(atob(settingsBase64));
    const customerName = document.getElementById("customerName").value;
    const shopperId = customerName != null ? customerName : settings.shopperId

    const serviceId = PaymentsUtil.generateServiceId();
    let terminalResponse = await PaymentsUtil.makeTerminalPaymentWithCardAcqCall(settings.merchantId, serviceId, shopperId, settings.poiId, totalAmount, settings.currency, settings.apiType, settings.localEndpoint, settings.connectionType, settings.printReceipt, settings.connectionTimeout, settings.savePaymentMethod);
    if (terminalResponse == null) {
        console.log("Payment call interrupted. Starting polling.")
        for (let i = 1; i < PAYMENT_STATUS_POLL_COUNT+1; i++) {
            console.log("Polling " + i);
            terminalResponse = await PaymentsUtil.makeTerminalPaymentStatusCall(settings.merchantId, settings.poiId, serviceId, settings.apiType, settings.localEndpoint);
            if (terminalResponse.reason != "InProgress") {
                break;
            }
        }
    }

    PaymentsUtil.disableWithMessage("checkoutButton", "Thank you!");

    //Reload page after 3 seconds
    setTimeout(() => {
      location.reload();
    }, 3000);

}

const defaultProducts = [
  { itemName: 'Beer', itemAmount: 500 },
  { itemName: 'White wine', itemAmount: 600 },
  { itemName: 'Red wine', itemAmount: 600 },
  { itemName: 'Coca Cola', itemAmount: 300 },
  { itemName: 'Iced tea', itemAmount: 400 },
  { itemName: 'Hot tea', itemAmount: 350 },
  { itemName: 'Coffee', itemAmount: 450 }
];

var products = await getItems();
if (products.length == 0) {
    products = defaultProducts;
}

const customers = await getCustomers();

const cart = [];
const cartTotalElement = document.getElementById('cartTotal');
const cartItemsElement = document.getElementById('cartItems');

const updateCart = () => {
  let total = cart.reduce((sum, item) => sum + Number(item.itemAmount), 0);
  totalAmount = total;
  cartTotalElement.textContent = total.toLocaleString();
  if (cart.length > 0) {
    cartItemsElement.innerHTML = cart.map(item =>
      `<div class="cart-item">${item.itemName} - ¥${item.itemAmount.toLocaleString()}</div>`
    ).join('');
  } else {
    cartItemsElement.textContent = 'No items in the cart';
  }
};

const productGrid = document.getElementById('productGrid');
products.forEach(product => {
  const btn = document.createElement('button');
  btn.className = 'product-btn';
  btn.setAttribute('data-name', product.itemName);
  btn.setAttribute('data-price', product.itemAmount);
  btn.innerHTML = `
    <h3>${product.itemName}</h3>
    <div class="price">¥${product.itemAmount.toLocaleString()}</div>
  `;
  btn.addEventListener('click', () => {
    cart.push(product);
    updateCart();
  });
  productGrid.appendChild(btn);
});

var customerNameDropdown = document.getElementById('customerName');
if (customers.length > 0) {
    customerNameDropdown.style.display = "block";
}

customers.forEach(customer => {
    const option = document.createElement("option");
    option.value = customer.id;
    option.textContent = customer.customerName;
    customerNameDropdown.appendChild(option);
});

document.getElementById("checkoutButton").addEventListener("click", function(event) {
    doCheckout();
});

async function getCustomers() {
    try {
        // Direct use of await to wait for the response
        const response = await axios.get('/pos-demo-customers');

        // Log and return the server response data
        console.log('PosDemoCustomers retrieved');
        return response.data;
    } catch (error) {
        // Handle error and return null in case of failure
        console.error('PosDemoCustomers Retrieval Error:', error);
        return null;
    }
}

async function getItems() {
    const storeName = document.getElementById("storeName").value;
    try {
        // Direct use of await to wait for the response
        const response = await axios.get('/pos-demo-items?storeName=' + storeName);

        // Log and return the server response data
        console.log('PosDemoItems retrieved');
        return response.data;
    } catch (error) {
        // Handle error and return null in case of failure
        console.error('PosDemoItems Retrieval Error:', error);
        return null;
    }
}