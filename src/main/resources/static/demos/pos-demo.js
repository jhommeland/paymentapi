import { PaymentsUtil } from '/js/paymentsUtil.js';

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
    const serviceId = PaymentsUtil.generateServiceId();
    let terminalResponse = await PaymentsUtil.makeTerminalPaymentCall(settings.merchantId, serviceId, settings.shopperId, settings.poiId, totalAmount, settings.currency, settings.apiType, settings.localEndpoint, settings.connectionType, settings.printReceipt, settings.connectionTimeout, settings.savePaymentMethod);
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

const products = [
  { name: 'Beer', price: 500 },
  { name: 'White wine', price: 600 },
  { name: 'Red wine', price: 600 },
  { name: 'Coca Cola', price: 300 },
  { name: 'Iced tea', price: 400 },
  { name: 'Hot tea', price: 350 },
  { name: 'Coffee', price: 450 }
];

const cart = [];
const cartTotalElement = document.getElementById('cartTotal');
const cartItemsElement = document.getElementById('cartItems');

const updateCart = () => {
  let total = cart.reduce((sum, item) => sum + item.price, 0);
  totalAmount = total;
  cartTotalElement.textContent = total.toLocaleString();
  if (cart.length > 0) {
    cartItemsElement.innerHTML = cart.map(item =>
      `<div class="cart-item">${item.name} - ¥${item.price.toLocaleString()}</div>`
    ).join('');
  } else {
    cartItemsElement.textContent = 'No items in the cart';
  }
};

const productGrid = document.getElementById('productGrid');
products.forEach(product => {
  const btn = document.createElement('button');
  btn.className = 'product-btn';
  btn.setAttribute('data-name', product.name);
  btn.setAttribute('data-price', product.price);
  btn.innerHTML = `
    <h3>${product.name}</h3>
    <div class="price">¥${product.price.toLocaleString()}</div>
  `;
  btn.addEventListener('click', () => {
    cart.push(product);
    updateCart();
  });
  productGrid.appendChild(btn);
});

document.getElementById("checkoutButton").addEventListener("click", function(event) {
    doCheckout();
});