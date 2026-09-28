package com.example.dronehelipad.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SURVEILLANCE")
public class SurveillanceDrone extends Drone {

    private boolean thermalImaging;

    public SurveillanceDrone() {}

    public SurveillanceDrone(String model, Helipad helipad, boolean thermalImaging) {
        super(model, helipad);
        this.thermalImaging = thermalImaging;
    }

    public boolean isThermalImaging() { return thermalImaging; }
    public void setThermalImaging(boolean thermalImaging) { this.thermalImaging = thermalImaging; }
}
