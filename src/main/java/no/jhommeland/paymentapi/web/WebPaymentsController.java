package no.jhommeland.paymentapi.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebPaymentsController {

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/sessions-flow-dropin")
    public String sessionsFlowDropin() {
        return "sessions-flow-dropin";
    }

    @GetMapping("/advanced-flow-dropin")
    public String advancedFlowDropin() {
        return "advanced-flow-dropin";
    }

    @GetMapping("/advanced-flow-api-encrypted")
    public String advancedFlowApiEncrypted() {
        return "advanced-flow-api-encrypted";
    }

    @GetMapping("/advanced-flow-components")
    public String advancedFlowComponents() {
        return "advanced-flow-components";
    }

    @GetMapping("/in-person-payments")
    public String inPersonPayments() {
        return "in-person-payments";
    }

    @GetMapping("/transaction-management")
    public String transactionManagement() {
        return "transaction-management";
    }

    @GetMapping("/report-management")
    public String reportManagement() {
        return "report-management";
    }

}

