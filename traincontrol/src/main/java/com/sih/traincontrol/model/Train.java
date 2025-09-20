package com.sih.traincontrol.model;

import lombok.Data;
import java.util.List;

@Data // Lombok annotation to auto-generate getters, setters, etc.
public class Train {
    private String id;
    private String name;
    private int priority; // 1 = high, 3 = low
    private double speedKmph; // Speed in kilometers per hour
    private double currentPositionKm; // Position on the track in kilometers
    private String status; // e.g., "RUNNING", "HALTED"
    private List<ScheduleEntry> schedule;
}