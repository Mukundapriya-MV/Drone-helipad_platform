package com.example.dronehelipad.service;

import com.example.dronehelipad.model.*;
import com.example.dronehelipad.repository.BuildingRepository;
import com.example.dronehelipad.repository.DeploymentMissionRepository;
import com.example.dronehelipad.repository.DroneRepository;
import com.example.dronehelipad.repository.FireIncidentRepository;
import com.example.dronehelipad.repository.HelipadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentDispatchService {

    private final FireIncidentRepository incidentRepository;
    private final BuildingRepository buildingRepository;
    private final DeploymentMissionRepository missionRepository;
    private final DroneRepository droneRepository;
    private final HelipadRepository helipadRepository;
    private final DispatchService dispatchService;
    private final SimulationService simulationService;

    public IncidentDispatchService(FireIncidentRepository incidentRepository,
                                   BuildingRepository buildingRepository,
                                   DeploymentMissionRepository missionRepository,
                                   DroneRepository droneRepository,
                                   HelipadRepository helipadRepository,
                                   DispatchService dispatchService,
                                   SimulationService simulationService) {
        this.incidentRepository = incidentRepository;
        this.buildingRepository = buildingRepository;
        this.missionRepository = missionRepository;
        this.droneRepository = droneRepository;
        this.helipadRepository = helipadRepository;
        this.dispatchService = dispatchService;
        this.simulationService = simulationService;
    }

    @Transactional
    public ReportResult report(Long buildingId, Severity severity) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new IllegalArgumentException("Building not found"));
        FireIncident incident = incidentRepository.save(new FireIncident(building, severity));

        var candidate = dispatchService.findBestDrone(building);
        if (candidate.isEmpty()) {
            return new ReportResult(incident, null);
        }

        Drone drone = candidate.get().getDrone();
        drone.setStatus(DroneStatus.BUSY);
        droneRepository.save(drone);

        if (drone.getHelipad() != null) {
            Helipad helipad = drone.getHelipad();
            helipad.setStatus(HelipadStatus.OCCUPIED);
            helipadRepository.save(helipad);
        }

        DeploymentMission mission = missionRepository.save(
                new DeploymentMission(drone, incident, candidate.get().getDistanceKm()));
        simulationService.startMission(mission.getId(), mission.getDistanceKm());
        return new ReportResult(incident, mission);
    }

    public record ReportResult(FireIncident incident, DeploymentMission mission) {
        public boolean dispatched() {
            return mission != null;
        }
    }
}