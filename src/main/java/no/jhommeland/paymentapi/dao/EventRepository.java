package no.jhommeland.paymentapi.dao;

import no.jhommeland.paymentapi.model.EventModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<EventModel, String> {

    List<EventModel> findAllByEventCodeOrderByEventDateDesc(String eventCode);

}
