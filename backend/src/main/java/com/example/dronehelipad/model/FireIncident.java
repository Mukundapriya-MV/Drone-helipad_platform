package com.example.dronehelipad.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class FireIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "building_id")
    private Building building;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    private LocalDateTime reportedTime = LocalDateTime.now();

    public FireIncident() {}

    public FireIncident(Building building, Severity severity) {
        this.building = building;
        this.severity = severity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public LocalDateTime getReportedTime() { return reportedTime; }
    public void setReportedTime(LocalDateTime reportedTime) { this.reportedTime = reportedTime; }
}
