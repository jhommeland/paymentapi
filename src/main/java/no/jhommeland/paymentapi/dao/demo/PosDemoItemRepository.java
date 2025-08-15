package no.jhommeland.paymentapi.dao.demo;

import no.jhommeland.paymentapi.model.demo.PosDemoItemModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PosDemoItemRepository extends JpaRepository<PosDemoItemModel, String> {
}