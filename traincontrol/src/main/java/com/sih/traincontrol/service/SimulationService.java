package com.sih.traincontrol.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sih.traincontrol.model.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class SimulationService {

    // (GA constants remain the same)
    private static final int POPULATION_SIZE = 50;
    private static final int NUM_GENERATIONS = 30;
    private static final double MUTATION_RATE = 0.1;
    private static final int TOURNAMENT_SIZE = 5;

    private SimulationState originalState;
    private SimulationState currentState;
    private List<Conflict> conflicts;

    private Chromosome lastOptimalPlan = null;

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = getClass().getResourceAsStream("/scenario.json");
            this.originalState = mapper.readValue(inputStream, SimulationState.class);
            this.originalState.setSimulationTimeMinutes(0);
            this.conflicts = identifyConflicts(this.originalState);
            // Load the initial state into the current state when the app starts
            loadInitialState();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load scenario.json on startup", e);
        }
    }

    // This method now ONLY resets the train positions and time
    public void loadInitialState() {
        try {
            // Reset the current state to the original pristine state
            this.currentState = deepCopyState(originalState);
            // --- CRITICAL FIX: The line that erased the plan is REMOVED from here. ---
        } catch (Exception e) {
            throw new RuntimeException("Failed to deep copy state", e);
        }
    }

    // --- PUBLIC API METHODS ---

    public SimulationState getCurrentState() {
        return this.currentState;
    }

    public void tickNormal() {
        // --- ADDED SAFETY: Explicitly use null for the plan in normal mode ---
        runSimulationStep(this.currentState, null);
    }

    public void tickOptimized() {
        // This will now correctly use the plan if it exists
        runSimulationStep(this.currentState, this.lastOptimalPlan);
    }

    public String applyOptimalPlan() {
        Chromosome bestSolution = runGeneticAlgorithm();
        this.lastOptimalPlan = bestSolution;
        return formatPlan(bestSolution);
    }

    // (The rest of the file, including runSimulationStep, the GA, and helpers,
    // remains IDENTICAL)
    // No other changes are needed below this line.

    // --- CORE SIMULATION LOGIC ---
    private void runSimulationStep(SimulationState state, Chromosome plan) {
        state.setSimulationTimeMinutes(state.getSimulationTimeMinutes() + 1);
        double timeStepHours = 1.0 / 60.0;

        for (Train train : state.getTrains()) {
            if ("ARRIVED".equals(train.getStatus()) || "CONFLICT".equals(train.getStatus())) {
                continue;
            }

            ScheduleEntry lastStop = train.getSchedule().get(train.getSchedule().size() - 1);
            Station destination = findStationById(state, lastStop.getStationId());
            if (Math.abs(train.getCurrentPositionKm() - destination.getPositionKm()) < 1) {
                train.setStatus("ARRIVED");
                continue;
            }

            train.setStatus("RUNNING");

            if (plan != null) {
                for (Conflict conflict : this.conflicts) {
                    Train trainA = findTrainById(state, conflict.getTrainIds().get(0));
                    Train trainB = findTrainById(state, conflict.getTrainIds().get(1));
                    if (isNearConflict(trainA, conflict) && isNearConflict(trainB, conflict)) {
                        String priorityTrainId = plan.getDecisionMap().get(conflict.getId());
                        if (!trainA.getId().equals(priorityTrainId))
                            trainA.setStatus("HALTED");
                        if (!trainB.getId().equals(priorityTrainId))
                            trainB.setStatus("HALTED");
                    }
                }
            } else {
                for (Conflict conflict : this.conflicts) {
                    Train trainA = findTrainById(state, conflict.getTrainIds().get(0));
                    Train trainB = findTrainById(state, conflict.getTrainIds().get(1));
                    if (Math.abs(trainA.getCurrentPositionKm() - trainB.getCurrentPositionKm()) < 5.0) {
                        trainA.setStatus("CONFLICT");
                        trainB.setStatus("CONFLICT");
                        return;
                    }
                }
            }

            if ("RUNNING".equals(train.getStatus())) {
                double distanceMoved = train.getSpeedKmph() * timeStepHours;
                if (train.getCurrentPositionKm() < destination.getPositionKm()) {
                    train.setCurrentPositionKm(train.getCurrentPositionKm() + distanceMoved);
                } else {
                    train.setCurrentPositionKm(train.getCurrentPositionKm() - distanceMoved);
                }
            }
        }
    }

    // --- GENETIC ALGORITHM IMPLEMENTATION ---
    private Chromosome runGeneticAlgorithm() {
        List<Chromosome> population = createInitialPopulation();
        for (int generation = 0; generation < NUM_GENERATIONS; generation++) {
            for (Chromosome chromosome : population) {
                calculateFitness(chromosome);
            }
            List<Chromosome> newPopulation = new ArrayList<>();
            while (newPopulation.size() < POPULATION_SIZE) {
                Chromosome parent1 = tournamentSelection(population);
                Chromosome parent2 = tournamentSelection(population);
                Chromosome child = crossover(parent1, parent2);
                mutate(child);
                newPopulation.add(child);
            }
            population = newPopulation;
        }
        return population.stream().max(Comparator.comparing(Chromosome::getFitness)).orElse(null);
    }

    private void calculateFitness(Chromosome chromosome) {
        SimulationState simCopy = deepCopyState(this.originalState);
        int maxSimTime = 240;
        while (simCopy.getSimulationTimeMinutes() < maxSimTime) {
            runSimulationStep(simCopy, chromosome);
            if (simCopy.getTrains().stream().allMatch(t -> "ARRIVED".equals(t.getStatus()))) {
                break;
            }
        }
        long trainsArrived = simCopy.getTrains().stream().filter(t -> "ARRIVED".equals(t.getStatus())).count();
        long totalDelay = calculateTotalDelay(simCopy);
        double fitness = (trainsArrived * 1000) - totalDelay;
        chromosome.setFitness(fitness);
    }

    // --- GA OPERATORS ---
    private List<Chromosome> createInitialPopulation() {
        List<Chromosome> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Chromosome c = new Chromosome();
            Map<String, String> decisionMap = new HashMap<>();
            for (Conflict conflict : this.conflicts) {
                String priorityTrain = conflict.getTrainIds()
                        .get(ThreadLocalRandom.current().nextInt(conflict.getTrainIds().size()));
                decisionMap.put(conflict.getId(), priorityTrain);
            }
            c.setDecisionMap(decisionMap);
            population.add(c);
        }
        return population;
    }

    private Chromosome tournamentSelection(List<Chromosome> population) {
        List<Chromosome> tournament = new ArrayList<>();
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(population.size());
            tournament.add(population.get(randomIndex));
        }
        return tournament.stream().max(Comparator.comparing(Chromosome::getFitness)).orElse(null);
    }

    private Chromosome crossover(Chromosome parent1, Chromosome parent2) {
        Chromosome child = new Chromosome();
        Map<String, String> childDecisionMap = new HashMap<>();
        for (Conflict conflict : this.conflicts) {
            if (Math.random() > 0.5) {
                childDecisionMap.put(conflict.getId(), parent1.getDecisionMap().get(conflict.getId()));
            } else {
                childDecisionMap.put(conflict.getId(), parent2.getDecisionMap().get(conflict.getId()));
            }
        }
        child.setDecisionMap(childDecisionMap);
        return child;
    }

    private void mutate(Chromosome chromosome) {
        for (Conflict conflict : this.conflicts) {
            if (Math.random() < MUTATION_RATE) {
                String currentPriorityTrain = chromosome.getDecisionMap().get(conflict.getId());
                String otherTrain = conflict.getTrainIds().stream()
                        .filter(id -> !id.equals(currentPriorityTrain))
                        .findFirst().orElse(null);
                if (otherTrain != null) {
                    chromosome.getDecisionMap().put(conflict.getId(), otherTrain);
                }
            }
        }
    }

    // --- HELPER METHODS ---
    private List<Conflict> identifyConflicts(SimulationState state) {
        List<String> conflictingTrainIds = Arrays.asList("12801-EXP", "54321-GOODS");
        Conflict conflict = new Conflict("C1", conflictingTrainIds, 50.0);
        return Collections.singletonList(conflict);
    }

    private boolean isNearConflict(Train train, Conflict conflict) {
        return Math.abs(train.getCurrentPositionKm() - conflict.getPositionKm()) < 10.0
                && !"ARRIVED".equals(train.getStatus());
    }

    private long calculateTotalDelay(SimulationState state) {
        long totalDelay = 0;
        for (Train train : state.getTrains()) {
            if ("ARRIVED".equals(train.getStatus())) {
                ScheduleEntry finalStop = train.getSchedule().get(train.getSchedule().size() - 1);
                String[] timeParts = finalStop.getScheduledArrival().split(":");
                long scheduledArrivalTime = Long.parseLong(timeParts[0]) * 60 + Long.parseLong(timeParts[1]);
                long actualArrivalTime = state.getSimulationTimeMinutes();
                if (actualArrivalTime > scheduledArrivalTime) {
                    totalDelay += (actualArrivalTime - scheduledArrivalTime);
                }
            } else {
                totalDelay += 1000;
            }
        }
        return totalDelay;
    }

    private String formatPlan(Chromosome chromosome) {
        if (chromosome == null)
            return "AI could not find a solution.";
        StringBuilder plan = new StringBuilder(
                "AI Optimal Plan (Fitness: " + String.format("%.2f", chromosome.getFitness()) + "): ");
        chromosome.getDecisionMap().forEach((conflictId, trainId) -> {
            plan.append("At Conflict ").append(conflictId)
                    .append(", give priority to Train ").append(trainId).append(". ");
        });
        return plan.toString();
    }

    private Train findTrainById(SimulationState state, String id) {
        return state.getTrains().stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }

    private Station findStationById(SimulationState state, String id) {
        return state.getStations().stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }

    private SimulationState deepCopyState(SimulationState original) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(mapper.writeValueAsString(original), SimulationState.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deep copy state", e);
        }
    }
}