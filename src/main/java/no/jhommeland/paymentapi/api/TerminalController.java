package no.jhommeland.paymentapi.api;

import com.adyen.model.terminal.ConnectedTerminalsResponse;
import no.jhommeland.paymentapi.model.TerminalPaymentModel;
import no.jhommeland.paymentapi.model.TerminalPaymentResponseModel;
import no.jhommeland.paymentapi.service.TerminalService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class TerminalController {

    private TerminalService terminalService;

    public TerminalController(TerminalService terminalService) {
        this.terminalService = terminalService;
    }

    @PostMapping("/terminals")
    public ResponseEntity<ConnectedTerminalsResponse> getTerminals(@RequestBody TerminalPaymentModel requestModel) {
        HttpHeaders headers = new HttpHeaders();
        ConnectedTerminalsResponse response = terminalService.getTerminals(requestModel);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @PostMapping("/terminal/payments")
    public ResponseEntity<TerminalPaymentResponseModel> makePayment(@RequestBody TerminalPaymentModel requestModel) {
        HttpHeaders headers = new HttpHeaders();
        TerminalPaymentResponseModel paymentResponse = terminalService.makePayment(requestModel);
        return new ResponseEntity<>(paymentResponse, headers, HttpStatus.OK);
    }

}
