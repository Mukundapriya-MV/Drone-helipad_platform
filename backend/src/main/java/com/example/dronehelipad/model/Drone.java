package com.example.dronehelipad.model;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "drone_type")
public abstract class Drone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String model;
    private double batteryPercent = 100.0;

    @Enumerated(EnumType.STRING)
    private DroneStatus status = DroneStatus.IDLE;

    @ManyToOne
    @JoinColumn(name = "helipad_id")
    private Helipad helipad;

    public Drone() {}

    public Drone(String model, Helipad helipad) {
        this.model = model;
        this.helipad = helipad;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public double getBatteryPercent() { return batteryPercent; }
    public void setBatteryPercent(double batteryPercent) { this.batteryPercent = batteryPercent; }
    public DroneStatus getStatus() { return status; }
    public void setStatus(DroneStatus status) { this.status = status; }
    public Helipad getHelipad() { return helipad; }
    public void setHelipad(Helipad helipad) { this.helipad = helipad; }
}
