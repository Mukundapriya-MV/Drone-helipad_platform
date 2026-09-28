package com.example.dronehelipad.repository;

import com.example.dronehelipad.model.Drone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DroneRepository extends JpaRepository<Drone, Long> {
}
