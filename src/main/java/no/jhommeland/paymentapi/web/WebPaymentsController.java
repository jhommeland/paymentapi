package no.jhommeland.paymentapi.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebPaymentsController {

    @Value("${adyen.client.key}")
    private String clientKey;

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/sessions")
    public String sessions(Model model) {
        model.addAttribute("clientKey", clientKey);
        return "sessions";
    }

    @GetMapping("/payments")
    public String payments(Model model) {
        model.addAttribute("clientKey", clientKey);
        return "payments";
    }

    @GetMapping("/management")
    public String transactionManagement() {
        return "management";
    }

}

