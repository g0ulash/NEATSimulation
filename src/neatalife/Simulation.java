/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neatalife;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Paul
 */
public class Simulation extends javax.swing.JPanel implements Runnable {

    public ArrayList<Food> foods;
    public ArrayList<Agent> agents;
    public NEAT neat;
    Random rng;
    public boolean stopped;
    public int nFood;
    public int nPoison;
    public static float valueFood=10000;
    public int nAgents;
    public int sleep=0;
    public boolean dynamic = false;
    public int nEpochs = 600000;
    public int nExperiments = 30;

    /**
     * Creates new form Simulation2
     */
    public Simulation() {
        initComponents();
        init(0, 0);
    }

    public void init(int nAgents, int nFood) {
        this.stopped = false;
        this.nFood = nFood;
        this.nAgents = nAgents;
        this.nPoison = (int)(0.5f*nFood);
        this.rng = new Random(0);

        foods = new ArrayList<>(nFood);
        agents = new ArrayList<>(nAgents);
        for (int i = 0; i < nAgents; i++) {
            Point location = new Point(rng.nextInt(this.getWidth() - 20) + 10, rng.nextInt(this.getHeight() - 20) + 10);
            this.agents.add(new Agent(location));
        }
        for (int i =0; i< nAgents/2; i++){
            this.agents.get(i).brain.genome.crossover=true;
        }

        this.neat = new NEAT(agents, rng);
        this.repaint();
    }

    @Override
    public void run() {
        //init conditions
        int REVERSE_FREQ = 300000;
        if(dynamic){
            REVERSE_FREQ = 2000;
        }
        for(int k=0; k<nExperiments; k++) {
            this.init(50, 50);
            this.rng.setSeed(k);
            String data = "";
            long currentTime = System.currentTimeMillis();
            //update states
            int epoch=0;
            boolean reversed=true;
            while (this.stopped == false) {
                //check collisions eyes
                for (Agent agent : this.agents) {
                    for (int i = 0; i < agent.eyesLocation.length; i++) {
                        agent.input[0 + (3 * i)] = 0;
                        agent.input[1 + (3 * i)] = 0;
                        agent.input[2 + (3 * i)] = 0;
                        Point eye = agent.eyesLocation[i];
                        int eyeDiameter = agent.eyesDiameter[i];
                        for (Agent a2 : this.agents) {
                            if (agent != a2) {
                                double d = eye.distance(a2.location);
                                if (d < eyeDiameter / 2 + a2.diameter / 2) {
                                    agent.input[0 + (3 * i)] += a2.color.getRed();
                                    agent.input[1 + (3 * i)] += a2.color.getGreen();
                                    agent.input[2 + (3 * i)] += a2.color.getBlue();
                                }
                            }
                        }
                        for (Iterator<Food> iter = this.foods.iterator(); iter.hasNext(); ) {
                            Food food = iter.next();
                            double d = eye.distance(food.location);
                            if (d < eyeDiameter / 2 + food.diameter / 2) {
                                agent.input[0 + (3 * i)] += food.color.getRed();
                                agent.input[1 + (3 * i)] += food.color.getGreen();
                                agent.input[2 + (3 * i)] += food.color.getBlue();
                            }
                        }
                        //borders
                        if (eye.x + eyeDiameter / 2 > this.getWidth() || eye.x - eyeDiameter / 2 < 0 || eye.y + eyeDiameter / 2 > this.getHeight() || eye.y - eyeDiameter / 2 < 0) {
                            agent.input[0 + (3 * i)] += 255f / 3;
                            agent.input[1 + (3 * i)] += 255f / 3;
                            agent.input[2 + (3 * i)] += 255f / 3;
                        }

                        //cap
                        agent.input[0 + (3 * i)] = Math.min(agent.input[0 + (3 * i)], 255) / 256;
                        agent.input[1 + (3 * i)] = Math.min(agent.input[1 + (3 * i)], 255) / 256;
                        agent.input[2 + (3 * i)] = Math.min(agent.input[2 + (3 * i)], 255) / 256;
                        agent.eyesColor[i] = new Color(agent.input[0 + (i * 3)] / 1.2f, agent.input[1 + (i * 3)] / 1.2f, agent.input[2 + (i * 3)] / 1.2f);

                        //System.out.println(agent.input[0 + (i * 3)]+" "+agent.input[1 + (i * 3)]+" "+agent.input[2 + (i * 3)]);
                        //Color eyeColor = new Color(agent.input[0 + (i * 3)], agent.input[1 + (i * 3)], agent.input[2 + (i * 3)]);
                        //System.out.println(eyeColor);
                    }
                }

                //check borders
                for (Agent agent : this.agents) {
                    agent.update();
                    boolean collision = false;
                    if (agent.location.x >= this.getWidth() && agent.xVelocity > 0) {
                        agent.xVelocity = 0;
                        collision = true;
                    }
                    if (agent.location.x <= 0 && agent.xVelocity < 0) {
                        agent.xVelocity = 0;
                        collision = true;
                    }
                    if (agent.location.y > this.getHeight() && agent.yVelocity > 0) {
                        agent.yVelocity = 0;
                        collision = true;
                    }
                    if (agent.location.y <= 0 && agent.yVelocity < 0) {
                        agent.yVelocity = 0;
                        collision = true;
                    }
                    if (collision) {
                        agent.energy -= this.valueFood * 0.25f;
                    }
                    agent.location.setLocation(agent.location.x + agent.xVelocity, agent.location.y + agent.yVelocity);
                    agent.updateEyeLocation();
                }

                //check collisions
                for (Agent a1 : this.agents) {
                    for (Agent a2 : this.agents) {
                        if (a1 != a2) {
                            double d = a1.location.distance(a2.location);
                            if (d < a1.diameter / 2 + a2.diameter / 2) {
                                double overlap = a1.diameter / 2 + a2.diameter / 2 - d;
                                Point dir = new Point(a2.location.x - a1.location.x, a2.location.y - a1.location.y);
                                double l = Math.sqrt(dir.x * dir.x + dir.y * dir.y);
                                dir.setLocation(dir.x / l, dir.y / l);
                                dir.setLocation(dir.x * (overlap), dir.y * (overlap));
                                //dir.length= overlap/2
                                a2.location.translate(dir.x / 4, dir.y / 4);
                                a1.location.translate(-dir.x / 4, -dir.y / 4);
                            }
                        }
                    }

                    for (Iterator<Food> iter = this.foods.iterator(); iter.hasNext(); ) {
                        Food food = iter.next();
                        double d = a1.location.distance(food.location);
                        if (d < a1.diameter / 2 + food.diameter / 2) {
                            //eat food
                            a1.energy += food.value;
                            iter.remove();
                        }

                    }
                }

                //spawn food
                int currentnFood = 0;
                int currentnPoison = 0;
                for (Food food : this.foods) {
                    if (food.value >= 0) {
                        currentnFood++;
                    } else {
                        currentnPoison++;
                    }
                }
                ArrayList<Food> foodsTemp = new ArrayList<>();
                for (int i = currentnFood; i < nFood - nPoison; i++) {
                    Point location;
                    Double minDistance;
                    int l = 0;
                    do {
                        location = new Point(rng.nextInt(this.getWidth() - 20) + 10, rng.nextInt(this.getHeight() - 20) + 10);
                        minDistance = Double.MAX_VALUE;
                        for (Agent agent : agents) {
                            minDistance = Math.min(minDistance, location.distance(agent.location));
                        }
                        for (Food food : foods) {
                            minDistance = Math.min(minDistance, location.distance(food.location));
                        }
                        l++;
                    } while (minDistance < 64 && l < 1000);
                    Food newFood = new Food(location, (int) valueFood, reversed);
                    foodsTemp.add(newFood);
                }
                this.foods.addAll(foodsTemp);
                //spawn poison
                foodsTemp = new ArrayList<>();
                for (int i = currentnPoison; i < nPoison; i++) {
                    Point location;
                    Double minDistance;
                    int l = 0;
                    do {
                        location = new Point(rng.nextInt(this.getWidth() - 20) + 10, rng.nextInt(this.getHeight() - 20) + 10);
                        minDistance = Double.MAX_VALUE;
                        for (Food food : foods) {
                            minDistance = Math.min(minDistance, location.distance(food.location));
                        }
                        l++;
                    } while (minDistance < 64 && l < 1000);
                    Food newFood = new Food(location, (int) valueFood * -1, reversed);
                    //newFood.value*=0.2f;
                    foodsTemp.add(newFood);
                }
                this.foods.addAll(foodsTemp);

                //relocate food
                if (epoch % REVERSE_FREQ == 0) {
                    System.out.println("REVERSED FOOD");
                    reversed = !reversed;
                }
                for (Iterator<Food> iter = this.foods.iterator(); iter.hasNext(); ) {
                    Food food = iter.next();
                    food.update();
                    if (epoch % REVERSE_FREQ == 0) {
                        food.value *= -1;
                    }
                    if (food.age > 100000) {
                        iter.remove();
                    }
                }

                //update population
                this.neat.update();

                //print statistic
                //TODO: track statistics
                int totalFood = 0;
                long totalAgeFood = 0;
                long totalAgePoison = 0;
                for (Food food : this.foods) {
                    totalFood += food.value;
                    if (food.value > 0) {
                        totalAgeFood += food.age;
                    } else {
                        totalAgePoison += food.age;
                    }
                }
                int totalFitness = 0;
                int totalEnergy = 0;
                int currentnAgents = 0;
                int totalBrainsize = 0;
                float nSexual = 0;
                for (Agent agent : this.agents) {
                    if (agent.brain.genome.crossover) {
                        nSexual++;
                    }
                    totalFitness += agent.fitness();
                    totalEnergy += agent.energy;
                    currentnAgents += 1;
                    totalBrainsize += agent.brain.genome.nodes.size() + agent.brain.genome.links.size();
                }
                if (currentnAgents < 2) {
                    //this.poisonProb*=1.1f;
                    Simulation.valueFood *= 1.5f;
                    this.init(this.nAgents, this.nFood);
                    epoch = 1;
                    System.out.println("RESET " + this.nFood);
                }

                //update drawing
                if (System.currentTimeMillis() - currentTime > 33) {
                    this.repaint();
                    currentTime = System.currentTimeMillis();
                }
                if (epoch == 0) {
                    data = "epoch, valueFood, totalFood, avgAgeFood, avgAgePoison, nAgents, avgFitness, avgEnergy, avgBrainSize, avgCrossover \n";
                }
                if (epoch % 100 == 0) {
                    System.out.println("epoch: " + epoch + " valueFood: " + Simulation.valueFood + " totalFood: " + totalFood
                            + " avgAgeFood: " + totalAgeFood / (nFood - nPoison) + " avgAgePoison: " + totalAgePoison / (nPoison) + " nAgents: "
                            + currentnAgents + " avgFitness " + (totalFitness / (currentnAgents)) + " avgEnergy: " + totalEnergy / (currentnAgents)
                            + " avgBrainSize " + (totalBrainsize / (currentnAgents)) + " sexualReproduction: " + nSexual / (currentnAgents));
                    //add to file
                    data+=epoch+", " + Simulation.valueFood+", " +totalFood+", " +totalAgeFood / (nFood - nPoison)+", " + totalAgePoison / (nPoison)+
                            ", " +currentnAgents+", " +(totalFitness / (currentnAgents))+", " +totalEnergy / (currentnAgents)+", " +(totalBrainsize / (currentnAgents))
                            +", " +nSexual / (currentnAgents)+" \n";
                }

                if (epoch == nEpochs) {
                    //write to file
                    String filename = "experiment_" + Integer.toString(k) +"_dynamic_" + Boolean.toString(dynamic)+".csv";
                    try
                    {
                        FileWriter writer = new FileWriter(filename);
                        System.out.println("Writing data to file");
                        writer.append(data);
                        writer.flush();
                        writer.close();
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                    //exit strategy
                    if(k!=nExperiments-1) {
                        stopped = true;
                    } else {
                        this.sleep = 50;
                    }
                }

                if (this.sleep > 0) {
                    try {
                        Thread.sleep(this.sleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                epoch++;
                //TODO: visualize neural network
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        //draw food first
        for (Food food : new ArrayList<Food>(this.foods)) {
            g.setColor(food.color);
            g.fillOval(food.location.x - food.diameter / 2, food.location.y - food.diameter / 2, food.diameter, food.diameter);
        }
        float maxEnergy = 1;
        int maxAge = 1;
        Agent maxAgent = null;
        for (Agent agent : new ArrayList<Agent>(this.agents)) {
            maxEnergy = Math.max(maxEnergy, agent.energy);
            if(agent.age>maxAge){
                maxAgent=agent;
            }
            maxAge = Math.max(maxAge, agent.age);
        }

        g.setColor(Color.black);
        for (Agent agent : new ArrayList<Agent>(this.agents)) {
            for (int i = 0; i < agent.eyesLocation.length; i++) {
                g.setColor(agent.eyesColor[i]);
                Point eye = agent.eyesLocation[i];
                int eyeDiameter = agent.eyesDiameter[i];
                g.fillOval(eye.x - eyeDiameter / 2, eye.y - eyeDiameter / 2, eyeDiameter, eyeDiameter);
            }
        }
        for (Agent agent : new ArrayList<Agent>(this.agents)) {
            g.setColor(agent.color);
            if(agent.brain.genome.crossover){
                g.setColor(Color.magenta);
            } else {
                g.setColor(Color.cyan);
            }
            g.fillOval(agent.location.x - agent.diameter / 2, agent.location.y - agent.diameter / 2, agent.diameter, agent.diameter);
            if(maxAgent!=null && agent==maxAgent){
                g.setColor(new Color(255, 255,0));
                g.fillOval(agent.location.x - agent.diameter / 2, agent.location.y - agent.diameter / 2, agent.diameter, agent.diameter);
            }
            float w = (float)Math.tanh(agent.age / maxAge);
            g.setColor(new Color(w,w,w));
            g.fillOval(agent.location.x - agent.diameter / 4, agent.location.y - agent.diameter / 4 - agent.diameter / 4, agent.diameter/2, agent.diameter/2);
            float gr = (float)Math.max(0, Math.tanh(agent.energy / maxEnergy));
            g.setColor(new Color(0,gr,0));
            g.fillOval(agent.location.x - agent.diameter / 4, agent.location.y - agent.diameter / 4 + agent.diameter / 4, agent.diameter/2, agent.diameter/2);
            g.setColor(Color.black);
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 726, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 466, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
