# Automated Urban Rooftop Drone Helipad & Firefighting Deployment Platform

A Spring Boot demo application with a browser-based operator console and REST API:

- **backend/** — Spring Boot (Maven) web server, Thymeleaf HTML pages, REST API,
   and MySQL database. Owns all data and makes the automatic dispatch decision.

The code is kept intentionally simple (plain Java, no Lombok, no complex
config) so it's easy to open, read line-by-line, and explain in a review.

## 1. Prerequisites

- JDK 17+
- Maven 3.8+ (internet access the first time, to download dependencies)
- MySQL Server running on `localhost:3306`

## 2. Run the backend

```bash
cd backend
```

In PowerShell, set the MySQL password securely and start the application:

```powershell
$env:DB_USERNAME = "root"
$securePassword = Read-Host "MySQL password" -AsSecureString
$passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
try {
   $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
   mvn spring-boot:run
} finally {
   [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
   Remove-Item Env:DB_PASSWORD -ErrorAction SilentlyContinue
}
```

The app creates/updates the `drone_helipad_db` schema and seeds a sample city
on first boot:

- Buildings: Skyline Towers, Phoenix Mall, Metro Station, T Nagar
- One rooftop helipad per building
- 4 drones: FF-Drone-01, FF-Drone-02, FF-Drone-03 (firefighting) and
  SV-Drone-01 (surveillance)

You can inspect the database using MySQL Workbench with the same local MySQL
connection and schema `drone_helipad_db`.

## 3. Open the operator console

With the server running, open:

`http://localhost:8080`

The web console has three navigable pages: **Fleet Dashboard** shows buildings,
helipads, and drones; **Report Incident** records a fire and automatically
dispatches a suitable drone; **Mission Tracker** shows mission progress and
drone battery levels, refreshing every two seconds. The REST API remains
available under `/api`.

Mission distance is the approximate one-way distance from the assigned drone's
helipad to the incident building. Simulated outbound and return travel durations
scale with this distance; fire suppression has a separate fixed duration.

Helipads default to private for external incidents. The Fleet Dashboard lets
operators opt a helipad into the shared response network. Dispatch always checks
eligible drones at the incident building first; only when none are available
does it consider drones at helipads that allow external dispatch.

## 4. Suggested demo flow for your review

1. Show the Fleet Dashboard — explain the Building → Helipad → Drone
   relationship (foreign keys visible in MySQL Workbench if asked).
2. Go to "Report incident", pick **Skyline Towers**, severity **HIGH**,
   submit the form. Point out the response tells you which drone was chosen
   and why (closest + healthiest battery + firefighting payload).
3. Open "Mission Tracker" and let it run for ~20 seconds live in front
   of the panel — the status updates on its own every two seconds.
4. Go back to the Dashboard and refresh — show the drone's battery reduced
   and it returned to IDLE / the helipad is AVAILABLE again once the
   mission completes.
5. Try reporting a second incident quickly to show a *different* drone gets
   picked (closer one), and a third/fourth to demonstrate
   "NO_DRONE_AVAILABLE" if every drone is busy.

## 5. How the automatic dispatch decision works (DispatchService)

For every fire incident:
1. Get the incident building's latitude/longitude.
2. For every **IDLE** drone with battery ≥ 25%, compute the straight-line
   (Haversine) distance from its helipad's building to the incident.
3. Score = `0.5 × closeness + 0.3 × battery level + 0.2 × payload fit`
   (firefighting drones score higher than surveillance drones for a fire).
4. The drone with the highest score is dispatched automatically.

## 6. Project structure

```
drone-helipad-platform/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/example/dronehelipad/
│       ├── model/        Building, Helipad, Drone (+ Firefighting/Surveillance subtypes),
│       │                 FireIncident, DeploymentMission, enums
│       ├── repository/   Spring Data JPA repositories
│       ├── service/      DispatchService, SimulationService, DataSeeder
│       ├── controller/   REST controllers (buildings, helipads, drones, incidents, missions)
│       └── DroneHelipadApplication.java
│   └── src/main/resources/
│       ├── templates/  Thymeleaf dashboard, incident form, and mission pages
│       └── static/css/ Operator console styles
```

## 7. Notes / possible viva questions

- **Why MySQL?** Application data persists across restarts and can be inspected
   directly in MySQL Workbench.
- **Why SINGLE_TABLE inheritance for Drone?** Keeps `FirefightingDrone` and
  `SurveillanceDrone` as real Java subclasses (matches the spec) while
  storing them in one simple table — easiest to query and explain.
- **Why does the mission page poll instead of using WebSockets?** Simplicity —
   a 2-second page refresh is easy to read/debug and is more than fast enough
   to look live in front of a panel.
