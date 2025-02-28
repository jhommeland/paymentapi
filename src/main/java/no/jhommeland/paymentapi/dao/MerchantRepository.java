package no.jhommeland.paymentapi.dao;

import no.jhommeland.paymentapi.model.MerchantModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MerchantRepository extends JpaRepository<MerchantModel, String> {

    List<MerchantModel> findAllByOrderByAdyenMerchantAccount();

}
