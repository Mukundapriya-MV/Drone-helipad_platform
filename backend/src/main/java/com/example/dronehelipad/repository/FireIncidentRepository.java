package com.example.dronehelipad.repository;

import com.example.dronehelipad.model.FireIncident;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FireIncidentRepository extends JpaRepository<FireIncident, Long> {
}
