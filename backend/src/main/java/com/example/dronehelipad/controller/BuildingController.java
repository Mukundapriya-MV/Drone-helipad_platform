package com.example.dronehelipad.controller;

import com.example.dronehelipad.model.Building;
import com.example.dronehelipad.repository.BuildingRepository;
import com.example.dronehelipad.repository.FireIncidentRepository;
import com.example.dronehelipad.repository.HelipadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buildings")
@CrossOrigin
public class BuildingController {

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private HelipadRepository helipadRepository;

    @Autowired
    private FireIncidentRepository incidentRepository;

    @GetMapping
    public List<Building> all() {
        return buildingRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Building> get(@PathVariable Long id) {
        return buildingRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Building create(@RequestBody Building building) {
        building.setId(null);
        return buildingRepository.save(building);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Building> update(@PathVariable Long id, @RequestBody Building building) {
        return buildingRepository.findById(id)
                .map(existing -> {
                    existing.setName(building.getName());
                    existing.setLatitude(building.getLatitude());
                    existing.setLongitude(building.getLongitude());
                    return ResponseEntity.ok(buildingRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!buildingRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        boolean hasHelipads = helipadRepository.findAll().stream()
                .anyMatch(helipad -> helipad.getBuilding() != null && id.equals(helipad.getBuilding().getId()));
        boolean hasIncidents = incidentRepository.findAll().stream()
                .anyMatch(incident -> incident.getBuilding() != null && id.equals(incident.getBuilding().getId()));
        if (hasHelipads || hasIncidents) {
            return ResponseEntity.status(409).body("Building is referenced by helipads or incidents");
        }
        buildingRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
