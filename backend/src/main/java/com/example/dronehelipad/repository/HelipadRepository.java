package com.example.dronehelipad.repository;

import com.example.dronehelipad.model.Helipad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HelipadRepository extends JpaRepository<Helipad, Long> {
}
