package no.jhommeland.paymentapi.dao;

import no.jhommeland.paymentapi.model.TransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionModel, String> {

    List<TransactionModel> findAllByOrderByCreatedAtDesc();

    List<TransactionModel> findByStatus(String status);

}
