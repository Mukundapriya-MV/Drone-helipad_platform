package com.example.dronehelipad.controller;

import com.example.dronehelipad.model.DeploymentMission;
import com.example.dronehelipad.model.Drone;
import com.example.dronehelipad.model.FireIncident;
import com.example.dronehelipad.model.MissionStatus;
import com.example.dronehelipad.repository.DeploymentMissionRepository;
import com.example.dronehelipad.repository.DroneRepository;
import com.example.dronehelipad.repository.FireIncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/missions")
@CrossOrigin
public class MissionController {

    @Autowired
    private DeploymentMissionRepository missionRepository;

    @Autowired
    private DroneRepository droneRepository;

    @Autowired
    private FireIncidentRepository incidentRepository;

    @GetMapping
    public List<DeploymentMission> all() {
        return missionRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return missionRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Optional<Drone> drone = droneRepository.findById(Long.valueOf(String.valueOf(body.get("droneId"))));
        Optional<FireIncident> incident = incidentRepository.findById(
                Long.valueOf(String.valueOf(body.get("incidentId"))));
        if (drone.isEmpty() || incident.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Drone or incident not found"));
        }
        double distanceKm = Double.parseDouble(String.valueOf(body.getOrDefault("distanceKm", 0)));
        DeploymentMission mission = new DeploymentMission(drone.get(), incident.get(), distanceKm);
        if (body.containsKey("status")) {
            mission.setStatus(MissionStatus.valueOf(String.valueOf(body.get("status")).toUpperCase()));
        }
        return ResponseEntity.ok(missionRepository.save(mission));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Optional<DeploymentMission> missionOpt = missionRepository.findById(id);
        if (missionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        DeploymentMission mission = missionOpt.get();
        if (body.containsKey("droneId")) {
            Optional<Drone> drone = droneRepository.findById(Long.valueOf(String.valueOf(body.get("droneId"))));
            if (drone.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Drone not found"));
            }
            mission.setDrone(drone.get());
        }
        if (body.containsKey("incidentId")) {
            Optional<FireIncident> incident = incidentRepository.findById(
                    Long.valueOf(String.valueOf(body.get("incidentId"))));
            if (incident.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Incident not found"));
            }
            mission.setIncident(incident.get());
        }
        if (body.containsKey("distanceKm")) {
            mission.setDistanceKm(Double.parseDouble(String.valueOf(body.get("distanceKm"))));
        }
        if (body.containsKey("status")) {
            mission.setStatus(MissionStatus.valueOf(String.valueOf(body.get("status")).toUpperCase()));
        }
        if (body.containsKey("dispatchTime")) {
            mission.setDispatchTime(LocalDateTime.parse(String.valueOf(body.get("dispatchTime"))));
        }
        return ResponseEntity.ok(missionRepository.save(mission));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!missionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        missionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
