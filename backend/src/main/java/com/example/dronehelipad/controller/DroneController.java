package com.example.dronehelipad.controller;

import com.example.dronehelipad.model.*;
import com.example.dronehelipad.repository.DeploymentMissionRepository;
import com.example.dronehelipad.repository.DroneRepository;
import com.example.dronehelipad.repository.HelipadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/drones")
@CrossOrigin
public class DroneController {

    @Autowired
    private DroneRepository droneRepository;

    @Autowired
    private HelipadRepository helipadRepository;

    @Autowired
    private DeploymentMissionRepository missionRepository;

    @GetMapping
    public List<Drone> all() {
        return droneRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return droneRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** body: { model, type: FIREFIGHTING|SURVEILLANCE, helipadId, foamCapacityKg?, thermalImaging? } */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        String model = String.valueOf(body.get("model"));
        String type = String.valueOf(body.get("type"));
        Long helipadId = Long.valueOf(String.valueOf(body.get("helipadId")));

        Optional<Helipad> helipadOpt = helipadRepository.findById(helipadId);
        if (helipadOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Helipad not found"));
        }
        Helipad helipad = helipadOpt.get();

        Drone drone;
        if ("FIREFIGHTING".equalsIgnoreCase(type)) {
            double foam = body.get("foamCapacityKg") != null
                    ? Double.parseDouble(String.valueOf(body.get("foamCapacityKg"))) : 50.0;
            drone = new FirefightingDrone(model, helipad, foam);
        } else {
            boolean thermal = body.get("thermalImaging") != null
                    && Boolean.parseBoolean(String.valueOf(body.get("thermalImaging")));
            drone = new SurveillanceDrone(model, helipad, thermal);
        }

        return ResponseEntity.ok(droneRepository.save(drone));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Optional<Drone> droneOpt = droneRepository.findById(id);
        if (droneOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Drone drone = droneOpt.get();
        if (body.containsKey("type")) {
            String type = String.valueOf(body.get("type"));
            boolean typeMatches = (drone instanceof FirefightingDrone && "FIREFIGHTING".equalsIgnoreCase(type))
                    || (drone instanceof SurveillanceDrone && "SURVEILLANCE".equalsIgnoreCase(type));
            if (!typeMatches) {
                return ResponseEntity.badRequest().body(Map.of("error", "Drone type cannot be changed"));
            }
        }
        if (body.containsKey("model")) {
            drone.setModel(String.valueOf(body.get("model")));
        }
        if (body.containsKey("helipadId")) {
            Optional<Helipad> helipad = helipadRepository.findById(
                    Long.valueOf(String.valueOf(body.get("helipadId"))));
            if (helipad.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Helipad not found"));
            }
            drone.setHelipad(helipad.get());
        }
        if (body.containsKey("batteryPercent")) {
            drone.setBatteryPercent(Double.parseDouble(String.valueOf(body.get("batteryPercent"))));
        }
        if (body.containsKey("status")) {
            drone.setStatus(DroneStatus.valueOf(String.valueOf(body.get("status")).toUpperCase()));
        }
        if (drone instanceof FirefightingDrone firefightingDrone && body.containsKey("foamCapacityKg")) {
            firefightingDrone.setFoamCapacityKg(Double.parseDouble(String.valueOf(body.get("foamCapacityKg"))));
        }
        if (drone instanceof SurveillanceDrone surveillanceDrone && body.containsKey("thermalImaging")) {
            surveillanceDrone.setThermalImaging(Boolean.parseBoolean(String.valueOf(body.get("thermalImaging"))));
        }
        return ResponseEntity.ok(droneRepository.save(drone));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Optional<Drone> drone = droneRepository.findById(id);
        if (drone.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        boolean hasMissions = missionRepository.findAll().stream()
                .anyMatch(mission -> mission.getDrone() != null && id.equals(mission.getDrone().getId()));
        if (hasMissions) {
            return ResponseEntity.status(409).body("Drone is referenced by missions");
        }
        droneRepository.delete(drone.get());
        return ResponseEntity.noContent().build();
    }
}
