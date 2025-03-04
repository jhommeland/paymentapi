package no.jhommeland.paymentapi.dao;

import no.jhommeland.paymentapi.model.MerchantModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MerchantRepository extends JpaRepository<MerchantModel, String> {

    List<MerchantModel> findAllByOrderByAdyenMerchantAccount();

    Optional<MerchantModel> findByAdyenMerchantAccount(String adyenMerchantAccount);

}
