package no.jhommeland.paymentapi.api;

import com.adyen.model.checkout.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import no.jhommeland.paymentapi.model.*;
import no.jhommeland.paymentapi.service.PaymentsService;
import no.jhommeland.paymentapi.util.UrlUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class PaymentsController {

    private final PaymentsService paymentsService;


    public PaymentsController(PaymentsService paymentsService) {
        this.paymentsService = paymentsService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionModel>> getTransactions() {
        HttpHeaders headers = new HttpHeaders();
        List<TransactionModel> transactions = paymentsService.getTransactions();
        return new ResponseEntity<>(transactions, headers, HttpStatus.OK);
    }

    @PostMapping("/sessions")
    public ResponseEntity<CreateCheckoutSessionResponse> createCheckoutSession(@RequestBody PaymentModel requestModel) {
        HttpHeaders headers = new HttpHeaders();
        CreateCheckoutSessionResponse createCheckoutSessionResponse = paymentsService.createCheckoutSession(requestModel);
        return new ResponseEntity<>(createCheckoutSessionResponse, headers, HttpStatus.OK);
    }

    @PostMapping("/payment-methods")
    public ResponseEntity<PaymentMethodsResponse> getPaymentMethods(@RequestBody PaymentMethodsModel requestModel) {
        HttpHeaders headers = new HttpHeaders();
        PaymentMethodsResponse paymentMethods = paymentsService.getPaymentMethods(requestModel);
        return new ResponseEntity<>(paymentMethods, headers, HttpStatus.OK);
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> makePayment(@RequestBody PaymentModel requestModel) {
        HttpHeaders headers = new HttpHeaders();
        PaymentResponse paymentResponse = paymentsService.makePayment(requestModel);
        return new ResponseEntity<>(paymentResponse, headers, HttpStatus.OK);
    }

    @PostMapping("/payments/capture")
    public ResponseEntity<PaymentCaptureResponse> capturePayment(@RequestBody PaymentModificationModel requestModel) {
        HttpHeaders headers = new HttpHeaders();
        PaymentCaptureResponse paymentCaptureResponse = paymentsService.capturePayment(requestModel);
        return new ResponseEntity<>(paymentCaptureResponse, headers, HttpStatus.OK);
    }

    @PostMapping("/payments/reverse")
    public ResponseEntity<PaymentReversalResponse> reversePayment(@RequestBody PaymentModificationModel requestModel) {
        HttpHeaders headers = new HttpHeaders();
        PaymentReversalResponse paymentReversalResponse = paymentsService.reversePayment(requestModel);
        return new ResponseEntity<>(paymentReversalResponse, headers, HttpStatus.OK);
    }

    @PostMapping("/payments/details")
    public ResponseEntity<PaymentDetailsResponse> submitPaymentDetails(@RequestBody PaymentDetailsModel requestModel) {
        HttpHeaders headers = new HttpHeaders();
        PaymentDetailsResponse paymentDetailsResponse = paymentsService.submitPaymentDetails(requestModel);
        return new ResponseEntity<>(paymentDetailsResponse, headers, HttpStatus.OK);
    }

    @GetMapping("/payments/complete")
    public String completePayment(@RequestParam String merchantId, @RequestParam String channel, @RequestParam String redirectResult, Model model) throws JsonProcessingException {
        PaymentDetailsResponse paymentDetailsResponse = paymentsService.submitPaymentDetailsRedirect(merchantId, redirectResult);
        model.addAttribute("base64Result", UrlUtil.toBase64(paymentDetailsResponse.toJson()));
        return channel.equals(PaymentRequest.ChannelEnum.WEB.toString()) ? "result" : "paymentapp-ios-redirect.html";
    }

    @GetMapping("/payments/complete/sessions")
    public String completeSessionsPayment(@RequestParam String merchantId, @RequestParam String sessionId, String sessionResult, Model model) throws JsonProcessingException {
        SessionResultResponse paymentDetailsResponse = paymentsService.getSessionResult(merchantId, sessionId, sessionResult);
        model.addAttribute("base64Result", UrlUtil.toBase64(paymentDetailsResponse.toJson()));
        return "result";
    }

}
