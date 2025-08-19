package no.jhommeland.paymentapi.dao.demo;

import no.jhommeland.paymentapi.model.demo.PosDemoPurchaseModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PosDemoPurchaseRepository extends JpaRepository<PosDemoPurchaseModel, String> {

    List<PosDemoPurchaseModel> findAllByCustomerName(String customerName);

}