package com.sih.traincontrol.model;

import lombok.Data;
import java.util.List;

@Data
public class SimulationState {
    private List<Train> trains;
    private List<Station> stations;
    private long simulationTimeMinutes; // Time in minutes from start
}