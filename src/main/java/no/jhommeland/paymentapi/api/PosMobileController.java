package no.jhommeland.paymentapi.api;

import com.adyen.model.posmobile.CreateSessionResponse;
import no.jhommeland.paymentapi.model.PosMobileModel;
import no.jhommeland.paymentapi.service.PosMobileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class PosMobileController {

    private final PosMobileService posMobileService;

    public PosMobileController(PosMobileService posMobileService) {
        this.posMobileService = posMobileService;
    }

    @PostMapping("/posMobile/sessions")
    public ResponseEntity<CreateSessionResponse> createPosMobileSession(@RequestBody PosMobileModel posMobileModel) {
        HttpHeaders headers = new HttpHeaders();
        CreateSessionResponse createCheckoutSessionResponse = posMobileService.createSession(posMobileModel);
        return new ResponseEntity<>(createCheckoutSessionResponse, headers, HttpStatus.OK);
    }

}
