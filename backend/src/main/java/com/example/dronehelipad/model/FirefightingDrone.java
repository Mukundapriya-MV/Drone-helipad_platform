package com.example.dronehelipad.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("FIREFIGHTING")
public class FirefightingDrone extends Drone {

    private double foamCapacityKg;

    public FirefightingDrone() {}

    public FirefightingDrone(String model, Helipad helipad, double foamCapacityKg) {
        super(model, helipad);
        this.foamCapacityKg = foamCapacityKg;
    }

    public double getFoamCapacityKg() { return foamCapacityKg; }
    public void setFoamCapacityKg(double foamCapacityKg) { this.foamCapacityKg = foamCapacityKg; }
}
