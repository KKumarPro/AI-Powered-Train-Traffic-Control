package com.sih.traincontrol.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class Conflict {
    private String id;
    private List<String> trainIds;
    private double positionKm;
}