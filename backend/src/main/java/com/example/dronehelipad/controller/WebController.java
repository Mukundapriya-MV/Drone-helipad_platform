package com.example.dronehelipad.controller;

import com.example.dronehelipad.model.Severity;
import com.example.dronehelipad.repository.BuildingRepository;
import com.example.dronehelipad.repository.DeploymentMissionRepository;
import com.example.dronehelipad.repository.DroneRepository;
import com.example.dronehelipad.repository.HelipadRepository;
import com.example.dronehelipad.service.IncidentDispatchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebController {

    private final BuildingRepository buildingRepository;
    private final HelipadRepository helipadRepository;
    private final DroneRepository droneRepository;
    private final DeploymentMissionRepository missionRepository;
    private final IncidentDispatchService incidentDispatchService;

    public WebController(BuildingRepository buildingRepository,
                         HelipadRepository helipadRepository,
                         DroneRepository droneRepository,
                         DeploymentMissionRepository missionRepository,
                         IncidentDispatchService incidentDispatchService) {
        this.buildingRepository = buildingRepository;
        this.helipadRepository = helipadRepository;
        this.droneRepository = droneRepository;
        this.missionRepository = missionRepository;
        this.incidentDispatchService = incidentDispatchService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("buildings", buildingRepository.findAll());
        model.addAttribute("helipads", helipadRepository.findAll());
        model.addAttribute("drones", droneRepository.findAll());
        return "dashboard";
    }

    @PostMapping("/helipads/{id}/external-dispatch")
    public String updateExternalDispatch(@PathVariable Long id,
                                        @RequestParam(defaultValue = "false") boolean allowExternalDispatch,
                                        RedirectAttributes redirectAttributes) {
        return helipadRepository.findById(id).map(helipad -> {
            helipad.setAllowExternalDispatch(allowExternalDispatch);
            helipadRepository.save(helipad);
            redirectAttributes.addFlashAttribute("helipadSettingMessage",
                    helipad.getBuilding().getName() + " external dispatch is "
                            + (allowExternalDispatch ? "enabled." : "disabled."));
            return "redirect:/dashboard";
        }).orElse("redirect:/dashboard");
    }

    @GetMapping("/incidents")
    public String incidents(Model model) {
        model.addAttribute("buildings", buildingRepository.findAll());
        model.addAttribute("severities", Severity.values());
        return "incidents";
    }

    @PostMapping("/incidents/report")
    public String reportIncident(@RequestParam Long buildingId,
                                 @RequestParam Severity severity,
                                 RedirectAttributes redirectAttributes) {
        try {
            IncidentDispatchService.ReportResult result = incidentDispatchService.report(buildingId, severity);
            if (!result.dispatched()) {
                redirectAttributes.addFlashAttribute("resultType", "warning");
                redirectAttributes.addFlashAttribute("resultMessage",
                        "Incident recorded, but no drone is available. All drones may be busy, charging, or low on battery.");
            } else {
                redirectAttributes.addFlashAttribute("resultType", "success");
                redirectAttributes.addFlashAttribute("mission", result.mission());
            }
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("resultType", "error");
            redirectAttributes.addFlashAttribute("resultMessage", exception.getMessage());
        }
        return "redirect:/incidents";
    }

    @GetMapping("/missions")
    public String missions(Model model) {
        model.addAttribute("missions", missionRepository.findAll());
        return "missions";
    }
}