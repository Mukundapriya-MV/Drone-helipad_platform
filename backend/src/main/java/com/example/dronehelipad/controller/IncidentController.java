package com.example.dronehelipad.controller;

import com.example.dronehelipad.model.DeploymentMission;
import com.example.dronehelipad.model.FireIncident;
import com.example.dronehelipad.model.Severity;
import com.example.dronehelipad.repository.BuildingRepository;
import com.example.dronehelipad.repository.DeploymentMissionRepository;
import com.example.dronehelipad.repository.FireIncidentRepository;
import com.example.dronehelipad.service.IncidentDispatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/incidents")
@CrossOrigin
public class IncidentController {

    private final FireIncidentRepository incidentRepository;
    private final IncidentDispatchService incidentDispatchService;
    private final BuildingRepository buildingRepository;
    private final DeploymentMissionRepository missionRepository;

    public IncidentController(FireIncidentRepository incidentRepository,
                              IncidentDispatchService incidentDispatchService,
                              BuildingRepository buildingRepository,
                              DeploymentMissionRepository missionRepository) {
        this.incidentRepository = incidentRepository;
        this.incidentDispatchService = incidentDispatchService;
        this.buildingRepository = buildingRepository;
        this.missionRepository = missionRepository;
    }

    @GetMapping
    public List<FireIncident> all() {
        return incidentRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FireIncident> get(@PathVariable Long id) {
        return incidentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Reports a fire and immediately triggers automated dispatch:
     * finds the best drone, creates a mission, and starts the
     * background simulation of that mission.
     * body: { buildingId, severity: LOW|MEDIUM|HIGH }
     */
    @PostMapping
    public ResponseEntity<?> reportIncident(@RequestBody Map<String, Object> body) {
        Long buildingId = Long.valueOf(String.valueOf(body.get("buildingId")));
        Severity severity = Severity.valueOf(String.valueOf(body.get("severity")).toUpperCase());

        try {
            IncidentDispatchService.ReportResult result = incidentDispatchService.report(buildingId, severity);
            if (!result.dispatched()) {
                return ResponseEntity.ok(Map.of(
                    "incidentId", result.incident().getId(),
                    "status", "NO_DRONE_AVAILABLE"
                ));
            }
            DeploymentMission mission = result.mission();
            return ResponseEntity.ok(mission);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        var incidentOpt = incidentRepository.findById(id);
        if (incidentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        FireIncident incident = incidentOpt.get();
        if (body.containsKey("buildingId")) {
            var building = buildingRepository.findById(Long.valueOf(String.valueOf(body.get("buildingId"))));
            if (building.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Building not found"));
            }
            incident.setBuilding(building.get());
        }
        if (body.containsKey("severity")) {
            incident.setSeverity(Severity.valueOf(String.valueOf(body.get("severity")).toUpperCase()));
        }
        return ResponseEntity.ok(incidentRepository.save(incident));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        var incident = incidentRepository.findById(id);
        if (incident.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        boolean hasMissions = missionRepository.findAll().stream()
                .anyMatch(mission -> mission.getIncident() != null && id.equals(mission.getIncident().getId()));
        if (hasMissions) {
            return ResponseEntity.status(409).body("Incident is referenced by missions");
        }
        incidentRepository.delete(incident.get());
        return ResponseEntity.noContent().build();
    }
}
