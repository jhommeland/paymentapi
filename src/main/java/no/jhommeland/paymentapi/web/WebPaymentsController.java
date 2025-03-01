package no.jhommeland.paymentapi.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebPaymentsController {

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/sessions")
    public String sessions() {
        return "sessions";
    }

    @GetMapping("/payments")
    public String payments() {
        return "payments";
    }

    @GetMapping("/management")
    public String transactionManagement() {
        return "management";
    }

}

