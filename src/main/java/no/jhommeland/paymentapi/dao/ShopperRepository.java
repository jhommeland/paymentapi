package no.jhommeland.paymentapi.dao;

import no.jhommeland.paymentapi.model.ShopperModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopperRepository extends JpaRepository<ShopperModel, String> {
}
