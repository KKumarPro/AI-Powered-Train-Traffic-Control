// =================================================================
//                 AI Train Control - app.js (Complete)
// =================================================================

// --- Configuration ---
const API_BASE_URL = "http://localhost:8080/api/simulation";

// --- DOM Elements ---
const trackContainer = document.getElementById('track-container');
const simTimeEl = document.getElementById('sim-time');
const logMessagesEl = document.getElementById('log-messages');
const planDisplayEl = document.getElementById('plan-display');
const planTextEl = document.getElementById('plan-text');
const runUnoptimizedBtn = document.getElementById('run-unoptimized-btn');
const runOptimizedBtn = document.getElementById('run-optimized-btn');
const outcomeStatusEl = document.getElementById('outcome-status');
const totalDelayEl = document.getElementById('total-delay');
const throughputEl = document.getElementById('throughput');

// --- Main Simulation Function ---

async function runFullSimulation(isOptimized) {
    // 1. Disable buttons and clear previous results
    setButtonsEnabled(false);
    planDisplayEl.style.display = 'none';
    logMessage(isOptimized ? "Running simulation with AI plan..." : "Running normal simulation...");
    
    // 2. Get the optimized plan from the AI if needed
    if (isOptimized) {
        try {
            const response = await fetch(`${API_BASE_URL}/optimize`, { method: 'POST' });
            if (!response.ok) throw new Error('Network response was not ok');
            const plan = await response.text();
            planTextEl.innerText = plan;
            planDisplayEl.style.display = 'block';
        } catch (error) {
            logMessage("Error: Failed to get AI plan from backend.", "error");
            setButtonsEnabled(true);
            return;
        }
    }

    // 3. Reset the simulation on the backend
    await fetch(`${API_BASE_URL}/reset`, { method: 'POST' });

    // 4. Run the simulation step-by-step
    let isFinished = false;
    let finalState;
    const tickUrl = isOptimized ? `${API_BASE_URL}/tick/optimized` : `${API_BASE_URL}/tick/normal`;

    while (!isFinished) {
        try {
            const response = await fetch(tickUrl, { method: 'POST' });
            if (!response.ok) throw new Error('Tick request failed');
            const state = await response.json();
            finalState = state;
            render(state);
            
            const trainsFinished = state.trains.every(t => t.status === 'ARRIVED' || t.status === 'CONFLICT');
            if (trainsFinished || state.simulationTimeMinutes > 300) {
                isFinished = true;
            }
            await new Promise(resolve => setTimeout(resolve, 50));
        } catch (error) {
            logMessage("Error: Lost connection to backend during simulation.", "error");
            isFinished = true; // Stop the loop on error
        }
    }
    
    // 5. Display the final results
    updateResults(finalState);
    setButtonsEnabled(true);
}

// --- Helper Functions ---

function render(state) {
    if (!state) return;
    trackContainer.innerHTML = '<div class="track-line"></div>';

    state.stations.forEach(station => {
        const stationEl = document.createElement('div');
        stationEl.className = 'station';
        stationEl.style.left = `${station.positionKm}%`;
        stationEl.innerHTML = `<div class="label">${station.name}</div>`;
        trackContainer.appendChild(stationEl);
    });

    state.trains.forEach(train => {
        const trainEl = document.createElement('div');
        const shortId = train.id.split('-')[0];
        trainEl.className = `train train-priority-${train.priority} train-status-${train.status}`;
        trainEl.style.left = `${train.currentPositionKm}%`;
        trainEl.innerText = shortId;
        trackContainer.appendChild(trainEl);
    });

    const hours = Math.floor(state.simulationTimeMinutes / 60);
    const minutes = state.simulationTimeMinutes % 60;
    simTimeEl.textContent = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
}

function updateResults(finalState) {
    if (!finalState) {
        logMessage("Could not get final state from simulation.", "error");
        return;
    }

    const trainsArrived = finalState.trains.filter(t => t.status === 'ARRIVED').length;
    const trainsInConflict = finalState.trains.filter(t => t.status === 'CONFLICT').length;
    const totalTrains = finalState.trains.length;
    let totalDelay = 0;
    
    if (trainsInConflict > 0) {
        outcomeStatusEl.textContent = "FAILURE (Conflict)";
        outcomeStatusEl.className = 'outcome-fail';
        totalDelay = 200;
    } else if (trainsArrived === totalTrains) {
        const expressTrain = finalState.trains.find(t => t.id.includes('EXP'));
        const [sch_h, sch_m] = expressTrain.schedule[1].scheduledArrival.split(':');
        const scheduledTime = parseInt(sch_h) * 60 + parseInt(sch_m);
        totalDelay = finalState.simulationTimeMinutes > scheduledTime ? finalState.simulationTimeMinutes - scheduledTime : 0;
        
        outcomeStatusEl.textContent = "SUCCESS";
        outcomeStatusEl.className = 'outcome-success';
    } else {
         outcomeStatusEl.textContent = "FAILURE (Timeout)";
         outcomeStatusEl.className = 'outcome-fail';
         totalDelay = 300;
    }

    totalDelayEl.textContent = `${totalDelay} minutes`;
    throughputEl.textContent = `${trainsArrived} / ${totalTrains} trains`;
}

function logMessage(message, type = "info") {
    const p = document.createElement('p');
    p.className = type;
    p.innerHTML = message;
    logMessagesEl.prepend(p);
}

function setButtonsEnabled(enabled) {
    runUnoptimizedBtn.disabled = !enabled;
    runOptimizedBtn.disabled = !enabled;
}

// --- Initializer ---
// This function runs when the page first loads
function initialize() {
    // Attach the functions to the buttons
    runUnoptimizedBtn.addEventListener('click', () => runFullSimulation(false));
    runOptimizedBtn.addEventListener('click', () => runFullSimulation(true));
    
    logMessage("UI Initialized. Choose a simulation to run.");
    
    // Fetch the starting state from the backend to draw the initial positions
    fetch(`${API_BASE_URL}/state`)
        .then(res => {
            if (!res.ok) {
                throw new Error('Backend not reachable');
            }
            return res.json();
        })
        .then(render)
        .catch(err => {
            logMessage("Error: Could not connect to the backend server. Please ensure it's running.", "error");
        });
}

// --- START THE APP ---
initialize();