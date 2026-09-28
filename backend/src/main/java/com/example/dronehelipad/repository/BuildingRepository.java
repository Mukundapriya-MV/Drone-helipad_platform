package com.example.dronehelipad.repository;

import com.example.dronehelipad.model.Building;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingRepository extends JpaRepository<Building, Long> {
}
