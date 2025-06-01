export class CheckoutUtil {

    static MOUNT_CONTAINER = "#dropin-container";

    static async mountCheckout(component, checkoutConfiguration, dropinConfiguration, version) {

        console.log("Mounting Checkout version " + version)

        if (version.startsWith("6.")) {
            const { AdyenCheckout } = window.AdyenWeb;
            const checkout = await AdyenCheckout(checkoutConfiguration);
            return CheckoutUtil.initiateCheckoutV6FromComponent(component, checkout, dropinConfiguration).mount(CheckoutUtil.MOUNT_CONTAINER)
        } else if (version.startsWith("5.")) {
            //For 5.x.x paymentsMethodsConfiguration is part of the checkoutConfiguration
            checkoutConfiguration.paymentMethodsConfiguration = dropinConfiguration.paymentMethodsConfiguration
            const checkout = await window.AdyenCheckout(checkoutConfiguration);
            return checkout.create(component).mount(CheckoutUtil.MOUNT_CONTAINER);
        } else {
            console.error("Unsupported checkout version");
        }

    }

    static initiateCheckoutV6FromComponent(component, checkout, dropinConfiguration) {
        switch (component) {
            case "applepay":
                return new window.AdyenWeb.ApplePay(checkout, dropinConfiguration);
            case "card":
                return new window.AdyenWeb.Card(checkout, dropinConfiguration);
            default:
                return new window.AdyenWeb.Dropin(checkout, dropinConfiguration);
        }
    }

}