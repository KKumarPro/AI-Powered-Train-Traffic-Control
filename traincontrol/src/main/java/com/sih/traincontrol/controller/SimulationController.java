package com.sih.traincontrol.controller;

import com.sih.traincontrol.model.SimulationState;
import com.sih.traincontrol.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulation")
@CrossOrigin(origins = "*")
public class SimulationController {

    @Autowired
    private SimulationService simulationService;

    @GetMapping("/state")
    public SimulationState getState() {
        return simulationService.getCurrentState();
    }

    // --- NEW: Endpoint for the normal simulation tick ---
    @PostMapping("/tick/normal")
    public SimulationState tickNormal() {
        simulationService.tickNormal();
        return simulationService.getCurrentState();
    }

    // --- NEW: Endpoint for the optimized simulation tick ---
    @PostMapping("/tick/optimized")
    public SimulationState tickOptimized() {
        simulationService.tickOptimized();
        return simulationService.getCurrentState();
    }

    @PostMapping("/reset")
    public SimulationState reset() {
        simulationService.loadInitialState();
        return simulationService.getCurrentState();
    }

    @PostMapping("/optimize")
    public String getOptimalPlan() {
        return simulationService.applyOptimalPlan();
    }
}