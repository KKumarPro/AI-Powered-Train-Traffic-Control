# 🚂 AI-Powered Train Traffic Control (SIH Prototype)

> An intelligent decision-support system built for the Smart India Hackathon (SIH) to maximize railway section throughput and minimize train delays using a Genetic Algorithm.

This project tackles the complex challenge of real-time train traffic management. It moves beyond traditional manual control by leveraging an AI-powered optimization engine to create conflict-free, efficient train schedules dynamically.

## 🚀 Live Demo

This prototype demonstrates a clear "Before & After" scenario. First, a normal simulation shows a conflict and failure. Then, the AI-powered simulation runs, showing the optimal, conflict-free solution.

![Live Demo GIF](https-placeholder-for-your-demo.gif)
*(**Important:** Replace the link above with a GIF of your working application!)*

---

## 🎯 Problem Statement

**SIH25022: Maximizing Section throughput using AI-Powered precise train traffic control.**

Indian Railways manages train movements primarily through the experience of controllers. This manual approach faces limitations as network congestion grows. This project aims to create an intelligent, data-driven system to enhance efficiency, punctuality, and utilization of railway infrastructure by assisting section controllers in making optimized, real-time decisions.

---

## ✨ Key Features

* **Dynamic Simulation Engine:** A fully functional backend that simulates train movements, speeds, and positions based on a predefined schedule.
* **AI Optimization Core:** A **Genetic Algorithm (GA)** that intelligently searches for the best possible traffic plan to resolve conflicts.
* **"Before & After" Visualization:** A clear and intuitive UI that first demonstrates the problem (a traffic conflict) and then showcases the AI's optimal solution.
* **Real-time KPI Dashboard:** The interface displays key performance indicators like **Total Delay** and **Throughput** to quantitatively measure the effectiveness of the AI's plan.
* **RESTful API:** A clean API built with Spring Boot that connects the Java backend to the JavaScript frontend.

---

## 🧠 How It Works: The AI Core

The brain of this project is a **Genetic Algorithm (GA)**, a type of AI inspired by Charles Darwin's theory of evolution.

1.  **Population:** The AI starts by creating a population of dozens of random potential solutions (plans).
2.  **Fitness Function:** Each plan is tested in a rapid, internal simulation. It is then assigned a "fitness score" based on our goals: maximizing throughput (trains arrived) and minimizing total delay.
3.  **Evolution:** The best plans ("parents") are selected. Their strategies are combined (**Crossover**) and slightly, randomly altered (**Mutation**) to create a new, potentially better generation of "child" plans.
4.  **Survival of the Fittest:** This evolutionary cycle repeats for many generations. Bad solutions die out, and good solutions are refined, until a near-optimal plan emerges.

This approach allows the system to intelligently search a massive solution space and find an excellent strategy in seconds.

---

## 🛠️ Technology Stack

| Backend                                                                                                                                                                             | Frontend                                                                                                                                                                               |
| ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| ![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)                                                                                   | ![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)                                                                                       |
| ![Spring](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)                                                                                | ![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)                                                                                         |
| ![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)                                                                             | ![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)                                                                     |

---

## 🔧 Setup and Installation

To run this project locally, follow these steps:

### Prerequisites

* **Java JDK 17** or newer
* **Apache Maven**
* A modern web browser (e.g., Chrome, Firefox)

### 1. Backend (Java Spring Boot)

```bash
# Clone the repository
git clone [https://github.com/your-username/your-repo-name.git](https://github.com/your-username/your-repo-name.git)
cd your-repo-name/

# Navigate to the backend project folder (e.g., traincontrol)
cd traincontrol 

# Run the Spring Boot application using Maven
mvn spring-boot:run
```
The backend server will start on `http://localhost:8080`.

### 2. Frontend (HTML/JS/CSS)

The frontend is a simple static application.

1.  Navigate to the frontend folder (e.g., `train-control-ui`).
2.  Open the `index.html` file directly in your web browser.

> **Pro Tip:** For a better experience, serve the frontend folder using a simple web server to avoid any potential browser issues. If you have Node.js installed, you can use `live-server`:
> ```bash
> # (From inside the train-control-ui folder)
> npx live-server
> ```

---

## 📖 Usage - The "Before & After" Story

The UI is designed to tell a clear story:

1.  **Click "1. Run Normal Simulation"**: Watch the trains move according to basic rules. You will see them run into a conflict, causing the simulation to **FAIL**. The results panel will show a high delay and low throughput.
2.  **Click "2. Run AI Optimized Simulation ⚡"**: First, the AI's optimal plan will be displayed. Then, the simulation will run again, but this time it will follow the AI's instructions. You will see one train correctly wait for the other to pass, leading to a **SUCCESSFUL** outcome with minimal delay.

---

## 💡 Future Improvements

* Model more complex track layouts (junctions, multiple sidings).
* Incorporate real-time disruptions (e.g., signal failure, engine trouble).
* Develop a "What-If" scenario editor for controllers to test their own strategies against the AI's.
* Integrate with a real database for persistent schedules and logging.

---

## 📄 License

This project is licensed under the MIT License. See the `LICENSE` file for details.

---

## 🧑‍💻 Author

* **Karan Kumar** - https://github.com/KKumarPro/
