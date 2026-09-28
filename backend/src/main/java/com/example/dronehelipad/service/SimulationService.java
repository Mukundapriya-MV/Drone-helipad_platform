package com.example.dronehelipad.service;

import com.example.dronehelipad.model.*;
import com.example.dronehelipad.repository.DeploymentMissionRepository;
import com.example.dronehelipad.repository.DroneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Simulates a drone mission's lifecycle in the background:
 * DISPATCHED -> TRANSIT -> SUPPRESSING -> RETURNING -> COMPLETED
 * Flight stages scale with the mission distance so the tracker reflects
 * longer outbound and return trips.
 */
@Service
public class SimulationService {

    private static final long DISPATCH_DELAY_SECONDS = 2;
    private static final long MIN_TRAVEL_DURATION_SECONDS = 5;
    private static final double SIMULATED_SECONDS_PER_KM = 12.0;
    private static final long SUPPRESSION_DURATION_SECONDS = 20;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    @Autowired
    private DeploymentMissionRepository missionRepository;

    @Autowired
    private DroneRepository droneRepository;

        public void startMission(Long missionId, double distanceKm) {
        long travelDurationSeconds = Math.max(MIN_TRAVEL_DURATION_SECONDS,
            (long) Math.ceil(Math.max(0, distanceKm) * SIMULATED_SECONDS_PER_KM));
        long arrivalDelaySeconds = DISPATCH_DELAY_SECONDS + travelDurationSeconds;
        long returnDelaySeconds = arrivalDelaySeconds + SUPPRESSION_DURATION_SECONDS;
        long completionDelaySeconds = returnDelaySeconds + travelDurationSeconds;

        scheduler.schedule(() -> advanceStage(missionId, MissionStatus.TRANSIT),
            DISPATCH_DELAY_SECONDS, TimeUnit.SECONDS);
        scheduler.schedule(() -> advanceStage(missionId, MissionStatus.SUPPRESSING),
            arrivalDelaySeconds, TimeUnit.SECONDS);
        scheduler.schedule(() -> advanceStage(missionId, MissionStatus.RETURNING),
            returnDelaySeconds, TimeUnit.SECONDS);
        scheduler.schedule(() -> completeMission(missionId), completionDelaySeconds, TimeUnit.SECONDS);
    }

    private void advanceStage(Long missionId, MissionStatus status) {
        missionRepository.findById(missionId).ifPresent(mission -> {
            mission.setStatus(status);
            missionRepository.save(mission);

            Drone drone = mission.getDrone();
            double batteryDrop = switch (status) {
                case TRANSIT -> 5.0;
                case SUPPRESSING -> 10.0;
                case RETURNING -> 5.0;
                default -> 0.0;
            };
            drone.setBatteryPercent(Math.max(0, drone.getBatteryPercent() - batteryDrop));
            droneRepository.save(drone);
        });
    }

    private void completeMission(Long missionId) {
        missionRepository.findById(missionId).ifPresent(mission -> {
            mission.setStatus(MissionStatus.COMPLETED);
            missionRepository.save(mission);

            Drone drone = mission.getDrone();
            drone.setStatus(DroneStatus.IDLE);
            if (drone.getHelipad() != null) {
                drone.getHelipad().setStatus(HelipadStatus.AVAILABLE);
            }
            droneRepository.save(drone);
        });
    }
}
