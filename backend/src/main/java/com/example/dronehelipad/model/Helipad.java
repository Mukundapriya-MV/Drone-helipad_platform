package com.example.dronehelipad.model;

import jakarta.persistence.*;

@Entity
public class Helipad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private HelipadStatus status = HelipadStatus.AVAILABLE;

    private boolean allowExternalDispatch;

    @ManyToOne
    @JoinColumn(name = "building_id")
    private Building building;

    public Helipad() {}

    public Helipad(Building building) {
        this.building = building;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public HelipadStatus getStatus() { return status; }
    public void setStatus(HelipadStatus status) { this.status = status; }
    public boolean isAllowExternalDispatch() { return allowExternalDispatch; }
    public void setAllowExternalDispatch(boolean allowExternalDispatch) { this.allowExternalDispatch = allowExternalDispatch; }
    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }
}
