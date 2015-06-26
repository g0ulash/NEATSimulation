# NEATSimulation

Implementation of NEAT in a simulated artificial environment. No outside dependencies are used.
Data gets automatically generated at the end of each experimental run.

Running the program:
run NEATSimulation.jar

The program GUI:
- Create: creates a new simulation
- Run: runs the simulation
- Stop: slows/speeds up the simulation

Important variables for running experiments:
- Simulation.java:
public boolean dynamic = false; //whether the environment is dynamic, i.e. changes every 2k iterations.
public int nEpochs = 600000; //number of epochs/iterations/update steps to run the simulation for
public int nExperiments = 30; //number of experiments to run
