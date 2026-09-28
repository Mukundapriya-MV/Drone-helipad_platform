package com.example.dronehelipad.repository;

import com.example.dronehelipad.model.DeploymentMission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeploymentMissionRepository extends JpaRepository<DeploymentMission, Long> {
}
