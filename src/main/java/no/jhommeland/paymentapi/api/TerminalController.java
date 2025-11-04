package no.jhommeland.paymentapi.api;

import com.adyen.model.terminal.ConnectedTerminalsResponse;
import no.jhommeland.paymentapi.model.TerminalPaymentModel;
import no.jhommeland.paymentapi.model.TerminalPaymentResponseModel;
import no.jhommeland.paymentapi.model.TerminalPaymentReferenceModel;
import no.jhommeland.paymentapi.service.TerminalService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class TerminalController {

    private final String INPUT_MODE_MULTI = "multi";

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
        TerminalPaymentResponseModel paymentResponse = null;
        if (requestModel.getTerminalConfig().getInputMode().equals(INPUT_MODE_MULTI)) {
            paymentResponse = terminalService.makePaymentWithMultiChoice(requestModel);
        } else {
            paymentResponse = terminalService.makePayment(requestModel);

        }
        return new ResponseEntity<>(paymentResponse, headers, HttpStatus.OK);
    }

    @PostMapping("/terminal/payments/status")
    public ResponseEntity<TerminalPaymentResponseModel> getPaymentStatus(@RequestBody TerminalPaymentReferenceModel requestModel) {
        HttpHeaders headers = new HttpHeaders();
        TerminalPaymentResponseModel paymentResponse = terminalService.getTerminalPaymentStatus(requestModel);
        return new ResponseEntity<>(paymentResponse, headers, HttpStatus.OK);
    }

    @PostMapping("/terminal/payments/abort")
    public ResponseEntity<TerminalPaymentResponseModel> abortPayment(@RequestBody TerminalPaymentReferenceModel requestModel) {
        HttpHeaders headers = new HttpHeaders();
        TerminalPaymentResponseModel paymentResponse = terminalService.abortPayment(requestModel);
        return new ResponseEntity<>(paymentResponse, headers, HttpStatus.OK);
    }

    @PostMapping("/terminal/cardAcq/payments")
    public ResponseEntity<TerminalPaymentResponseModel> makeCardAcqPayment(@RequestBody TerminalPaymentModel requestModel) {
        HttpHeaders headers = new HttpHeaders();
        TerminalPaymentResponseModel paymentResponse = terminalService.makePaymentWithCardAcquisition(requestModel);
        return new ResponseEntity<>(paymentResponse, headers, HttpStatus.OK);
    }

}
