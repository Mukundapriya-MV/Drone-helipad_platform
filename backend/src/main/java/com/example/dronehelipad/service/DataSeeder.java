package com.example.dronehelipad.service;

import com.example.dronehelipad.model.*;
import com.example.dronehelipad.repository.BuildingRepository;
import com.example.dronehelipad.repository.DroneRepository;
import com.example.dronehelipad.repository.HelipadRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Preloads a small sample city (matching the sample use case in the
 * project brief) so the demo works immediately without manual setup.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final BuildingRepository buildingRepository;
    private final HelipadRepository helipadRepository;
    private final DroneRepository droneRepository;

    public DataSeeder(BuildingRepository buildingRepository,
                       HelipadRepository helipadRepository,
                       DroneRepository droneRepository) {
        this.buildingRepository = buildingRepository;
        this.helipadRepository = helipadRepository;
        this.droneRepository = droneRepository;
    }

    @Override
    public void run(String... args) {
        if (buildingRepository.count() > 0) {
            return;
        }

        Building skyline = buildingRepository.save(new Building("Skyline Towers", 13.0827, 80.2707));
        Building phoenixMall = buildingRepository.save(new Building("Phoenix Mall", 13.0674, 80.2376));
        Building metroStation = buildingRepository.save(new Building("Metro Station", 13.0500, 80.2121));
        Building tNagar = buildingRepository.save(new Building("T Nagar", 13.0418, 80.2341));

        Helipad h1 = helipadRepository.save(new Helipad(phoenixMall));
        Helipad h2 = helipadRepository.save(new Helipad(metroStation));
        Helipad h3 = helipadRepository.save(new Helipad(tNagar));
        Helipad h4 = helipadRepository.save(new Helipad(skyline));

        droneRepository.save(new FirefightingDrone("FF-Drone-01", h1, 60.0));
        droneRepository.save(new FirefightingDrone("FF-Drone-02", h2, 45.0));
        droneRepository.save(new SurveillanceDrone("SV-Drone-01", h3, true));
        droneRepository.save(new FirefightingDrone("FF-Drone-03", h4, 50.0));
    }
}
