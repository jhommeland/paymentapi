package no.jhommeland.paymentapi.service;

import no.jhommeland.paymentapi.dao.demo.PosDemoCustomerRepository;
import no.jhommeland.paymentapi.dao.demo.PosDemoItemRepository;
import no.jhommeland.paymentapi.dao.demo.PosDemoPurchaseRepository;
import no.jhommeland.paymentapi.model.demo.PosDemoCustomerModel;
import no.jhommeland.paymentapi.model.demo.PosDemoItemModel;
import no.jhommeland.paymentapi.model.demo.PosDemoPurchaseModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DemoDataService {

    private final PosDemoCustomerRepository posDemoCustomerRepository;

    private final PosDemoItemRepository posDemoItemRepository;

    private final PosDemoPurchaseRepository posDemoPurchaseRepository;

    public DemoDataService(PosDemoCustomerRepository posDemoCustomerRepository, PosDemoItemRepository posDemoItemRepository, PosDemoPurchaseRepository posDemoPurchaseRepository) {
        this.posDemoCustomerRepository = posDemoCustomerRepository;
        this.posDemoItemRepository = posDemoItemRepository;
        this.posDemoPurchaseRepository = posDemoPurchaseRepository;
    }

    public List<PosDemoCustomerModel> getPosDemoCustomers() {
        return posDemoCustomerRepository.findAll();
    }

    public List<PosDemoItemModel> getPosDemoItems() {
        return posDemoItemRepository.findAll();
    }

    public List<PosDemoPurchaseModel> getPosDemoPurchases() {
        return posDemoPurchaseRepository.findAll();
    }

}
