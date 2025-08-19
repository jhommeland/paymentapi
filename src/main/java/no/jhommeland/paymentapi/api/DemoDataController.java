package no.jhommeland.paymentapi.api;

import no.jhommeland.paymentapi.model.demo.PosDemoCustomerModel;
import no.jhommeland.paymentapi.model.demo.PosDemoItemModel;
import no.jhommeland.paymentapi.model.demo.PosDemoPurchaseModel;
import no.jhommeland.paymentapi.service.DemoDataService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DemoDataController {

    private final DemoDataService demoDataService;

    public DemoDataController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @GetMapping("/pos-demo-customers")
    public ResponseEntity<List<PosDemoCustomerModel>> getPosDemoCustomers() {
        HttpHeaders headers = new HttpHeaders();
        List<PosDemoCustomerModel> customers = demoDataService.getPosDemoCustomers();
        return new ResponseEntity<>(customers, headers, HttpStatus.OK);
    }

    @GetMapping("/pos-demo-items")
    public ResponseEntity<List<PosDemoItemModel>> getPosDemoItems(@RequestParam(required = false) String storeName) {
        HttpHeaders headers = new HttpHeaders();
        List<PosDemoItemModel> items = demoDataService.getPosDemoItems(storeName);
        return new ResponseEntity<>(items, headers, HttpStatus.OK);
    }

    @GetMapping("/pos-demo-purchases")
    public ResponseEntity<List<PosDemoPurchaseModel>> getPosDemoPurchases() {
        HttpHeaders headers = new HttpHeaders();
        List<PosDemoPurchaseModel> purchases = demoDataService.getPosDemoPurchases();
        return new ResponseEntity<>(purchases, headers, HttpStatus.OK);
    }

}
