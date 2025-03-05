package no.jhommeland.paymentapi.dao;

import no.jhommeland.paymentapi.model.EventModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<EventModel, String> {
}
