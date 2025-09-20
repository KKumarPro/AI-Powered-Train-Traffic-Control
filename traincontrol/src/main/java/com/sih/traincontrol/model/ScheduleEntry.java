package com.sih.traincontrol.model;

import lombok.Data;

@Data
public class ScheduleEntry {
    private String stationId;
    private String scheduledArrival; // Format "HH:mm"
}