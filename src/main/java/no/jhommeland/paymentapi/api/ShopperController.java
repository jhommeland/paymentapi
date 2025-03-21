package no.jhommeland.paymentapi.api;

import no.jhommeland.paymentapi.model.ShopperModel;
import no.jhommeland.paymentapi.service.ShopperService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ShopperController {

    private final ShopperService shopperService;

    public ShopperController(ShopperService shopperService) {
        this.shopperService = shopperService;
    }

    @GetMapping("/shoppers")
    public ResponseEntity<List<ShopperModel>> getShoppers() {
        HttpHeaders headers = new HttpHeaders();
        List<ShopperModel> shoppers = shopperService.getShoppers();
        return new ResponseEntity<>(shoppers, headers, HttpStatus.OK);
    }

}
