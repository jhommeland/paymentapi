package no.jhommeland.paymentapi.dao.demo;

import no.jhommeland.paymentapi.model.demo.PosDemoPurchaseModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PosDemoPurchaseRepository extends JpaRepository<PosDemoPurchaseModel, String> {
}