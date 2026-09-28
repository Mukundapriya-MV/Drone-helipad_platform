package com.example.dronehelipad.service;

import com.example.dronehelipad.model.*;
import com.example.dronehelipad.repository.DroneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Decides which drone should respond to a fire incident.
 * Score = 50% closeness + 30% battery level + 20% payload suitability.
 */
@Service
public class DispatchService {

    @Autowired
    private DroneRepository droneRepository;

    private static final double MIN_BATTERY_TO_FLY = 25.0;

    /** Haversine great-circle distance between two lat/lon points, in km. */
    public double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public Optional<DroneCandidate> findBestDrone(Building incidentBuilding) {
        List<Drone> drones = droneRepository.findAll();

        List<Drone> eligibleDrones = drones.stream()
                .filter(d -> d.getStatus() == DroneStatus.IDLE)
                .filter(d -> d.getBatteryPercent() >= MIN_BATTERY_TO_FLY)
                .filter(d -> d.getHelipad() != null && d.getHelipad().getBuilding() != null)
            .toList();

        List<Drone> buildingDrones = eligibleDrones.stream()
            .filter(d -> d.getHelipad().getBuilding().getId().equals(incidentBuilding.getId()))
            .toList();

        List<Drone> candidates = buildingDrones.isEmpty()
            ? eligibleDrones.stream()
                .filter(d -> d.getHelipad().isAllowExternalDispatch())
                .toList()
            : buildingDrones;

        return candidates.stream()
                .map(d -> score(d, incidentBuilding))
                .max(Comparator.comparingDouble(DroneCandidate::getScore));
    }

    private DroneCandidate score(Drone d, Building incidentBuilding) {
        Building homeBuilding = d.getHelipad().getBuilding();
        double dist = distanceKm(incidentBuilding.getLatitude(), incidentBuilding.getLongitude(),
                homeBuilding.getLatitude(), homeBuilding.getLongitude());

        double payloadScore = (d instanceof FirefightingDrone) ? 1.0 : 0.3;
        double distanceScore = 1.0 / (1.0 + dist);
        double batteryScore = d.getBatteryPercent() / 100.0;

        double total = distanceScore * 0.5 + batteryScore * 0.3 + payloadScore * 0.2;
        return new DroneCandidate(d, dist, total);
    }

    public static class DroneCandidate {
        private final Drone drone;
        private final double distanceKm;
        private final double score;

        public DroneCandidate(Drone drone, double distanceKm, double score) {
            this.drone = drone;
            this.distanceKm = distanceKm;
            this.score = score;
        }

        public Drone getDrone() { return drone; }
        public double getDistanceKm() { return distanceKm; }
        public double getScore() { return score; }
    }
}
