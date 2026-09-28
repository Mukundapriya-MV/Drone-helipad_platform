package com.example.dronehelipad.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class DeploymentMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "drone_id")
    private Drone drone;

    @ManyToOne
    @JoinColumn(name = "incident_id")
    private FireIncident incident;

    @Enumerated(EnumType.STRING)
    private MissionStatus status = MissionStatus.DISPATCHED;

    private LocalDateTime dispatchTime = LocalDateTime.now();

    private double distanceKm;

    public DeploymentMission() {}

    public DeploymentMission(Drone drone, FireIncident incident, double distanceKm) {
        this.drone = drone;
        this.incident = incident;
        this.distanceKm = distanceKm;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Drone getDrone() { return drone; }
    public void setDrone(Drone drone) { this.drone = drone; }
    public FireIncident getIncident() { return incident; }
    public void setIncident(FireIncident incident) { this.incident = incident; }
    public MissionStatus getStatus() { return status; }
    public void setStatus(MissionStatus status) { this.status = status; }
    public LocalDateTime getDispatchTime() { return dispatchTime; }
    public void setDispatchTime(LocalDateTime dispatchTime) { this.dispatchTime = dispatchTime; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
}
