package com.example.dronehelipad.controller;

import com.example.dronehelipad.model.Building;
import com.example.dronehelipad.model.Helipad;
import com.example.dronehelipad.model.HelipadStatus;
import com.example.dronehelipad.repository.BuildingRepository;
import com.example.dronehelipad.repository.DroneRepository;
import com.example.dronehelipad.repository.HelipadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/helipads")
@CrossOrigin
public class HelipadController {

    @Autowired
    private HelipadRepository helipadRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private DroneRepository droneRepository;

    @GetMapping
    public List<Helipad> all() {
        return helipadRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Helipad> get(@PathVariable Long id) {
        return helipadRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Long buildingId = Long.valueOf(String.valueOf(body.get("buildingId")));
        Optional<Building> buildingOpt = buildingRepository.findById(buildingId);
        if (buildingOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Building not found"));
        }
        Helipad helipad = new Helipad(buildingOpt.get());
        if (body.containsKey("status")) {
            helipad.setStatus(HelipadStatus.valueOf(String.valueOf(body.get("status")).toUpperCase()));
        }
        if (body.containsKey("allowExternalDispatch")) {
            helipad.setAllowExternalDispatch(Boolean.parseBoolean(
                    String.valueOf(body.get("allowExternalDispatch"))));
        }
        return ResponseEntity.ok(helipadRepository.save(helipad));
    }

    @PatchMapping("/{id}/external-dispatch")
    public ResponseEntity<?> updateExternalDispatch(@PathVariable Long id,
                                                   @RequestBody Map<String, Object> body) {
        if (!body.containsKey("allowExternalDispatch")) {
            return ResponseEntity.badRequest().body(Map.of("error", "allowExternalDispatch is required"));
        }
        return helipadRepository.findById(id)
                .<ResponseEntity<?>>map(helipad -> {
                    helipad.setAllowExternalDispatch(Boolean.parseBoolean(
                            String.valueOf(body.get("allowExternalDispatch"))));
                    return ResponseEntity.ok(helipadRepository.save(helipad));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Optional<Helipad> helipadOpt = helipadRepository.findById(id);
        if (helipadOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Helipad helipad = helipadOpt.get();
        if (body.containsKey("buildingId")) {
            Optional<Building> building = buildingRepository.findById(
                    Long.valueOf(String.valueOf(body.get("buildingId"))));
            if (building.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Building not found"));
            }
            helipad.setBuilding(building.get());
        }
        if (body.containsKey("allowExternalDispatch")) {
            helipad.setAllowExternalDispatch(Boolean.parseBoolean(
                    String.valueOf(body.get("allowExternalDispatch"))));
        }
        if (body.containsKey("status")) {
            helipad.setStatus(HelipadStatus.valueOf(String.valueOf(body.get("status")).toUpperCase()));
        }
        return ResponseEntity.ok(helipadRepository.save(helipad));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Optional<Helipad> helipad = helipadRepository.findById(id);
        if (helipad.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        boolean hasDrones = droneRepository.findAll().stream()
                .anyMatch(drone -> drone.getHelipad() != null && id.equals(drone.getHelipad().getId()));
        if (hasDrones) {
            return ResponseEntity.status(409).body("Helipad is referenced by drones");
        }
        helipadRepository.delete(helipad.get());
        return ResponseEntity.noContent().build();
    }
}
