package no.jhommeland.paymentapi.dao;

import no.jhommeland.paymentapi.model.TransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<TransactionModel, String> {

    List<TransactionModel> findAllByOrderByCreatedAtDesc();

    Optional<TransactionModel> findByMerchantReference(String merchantReference);

}
