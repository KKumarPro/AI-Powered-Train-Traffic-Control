package com.sih.traincontrol.model;

import lombok.Data;
import java.util.Map;

@Data
public class Chromosome {
    // The key is the conflict ID, the value is the ID of the train that gets
    // priority
    private Map<String, String> decisionMap;
    private double fitness = -1; // -1 indicates fitness has not been calculated yet
}