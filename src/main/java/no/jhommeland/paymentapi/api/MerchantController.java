package no.jhommeland.paymentapi.api;

import no.jhommeland.paymentapi.model.MerchantModel;
import no.jhommeland.paymentapi.service.MerchantService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @GetMapping("/merchants")
    public ResponseEntity<List<MerchantModel>> getMerchants() {
        HttpHeaders headers = new HttpHeaders();
        List<MerchantModel> merchants = merchantService.getMerchants();
        return new ResponseEntity<>(merchants, headers, HttpStatus.OK);
    }

    @GetMapping("/merchants/credentials/{merchantId}")
    public ResponseEntity<String> getCredentials(@PathVariable String merchantId) {
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(merchantService.getClientKey(merchantId), headers, HttpStatus.OK);
    }

}
