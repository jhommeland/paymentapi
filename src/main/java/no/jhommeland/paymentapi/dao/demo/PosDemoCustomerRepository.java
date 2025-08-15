package no.jhommeland.paymentapi.dao.demo;

import no.jhommeland.paymentapi.model.demo.PosDemoCustomerModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PosDemoCustomerRepository extends JpaRepository<PosDemoCustomerModel, String> {
}