/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neatalife;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
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
    Random rng = new Random();
    public boolean stopped;
    public int nFood;
    public int nPoison;
    public static float valueFood=10000;
    public int nAgents;
    public int sleep=0;

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

        foods = new ArrayList<>(nFood);
        agents = new ArrayList<>(nAgents);
        for (int i = 0; i < nAgents; i++) {
            Point location = new Point(rng.nextInt(this.getWidth()), rng.nextInt(this.getHeight()));
            this.agents.add(new Agent(location));
        }

        this.neat = new NEAT(agents);
        this.repaint();
    }

    public boolean checkCircCollision(Agent agent1, Agent agent2) {
        //if pythagoras-calculated distance between middle points is smaller then combined radius: 
        Point r1 = agent1.location;
        Point r2 = agent2.location;
        float r1radius = agent1.diameter / 2;
        float r2radius = agent2.diameter / 2;
        return Math.sqrt(Math.pow(r2.x - r1.x, 2) + Math.pow(r2.y - r1.y, 2)) < r1radius + r2radius;
    }

    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();
        //update states
        int epoch=0;
        while (this.stopped == false) {
            epoch++;
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
                    for (Iterator<Food> iter = this.foods.iterator(); iter.hasNext();) {
                        Food food = iter.next();
                        double d = eye.distance(food.location);
                        if (d < eyeDiameter / 2 + food.diameter / 2) {
                            agent.input[0 + (3 * i)] += food.getColor().getRed();
                            agent.input[1 + (3 * i)] += food.getColor().getGreen();
                            agent.input[2 + (3 * i)] += food.getColor().getBlue();
                        }
                    }
                    //borders
                    if(eye.x+eyeDiameter/2>this.getWidth() || eye.x-eyeDiameter/2<0 || eye.y+eyeDiameter/2>this.getHeight() || eye.y-eyeDiameter/2 < 0){
                        agent.input[0 + (3 * i)] += 0;
                        agent.input[1 + (3 * i)] += 0;
                        agent.input[2 + (3 * i)] += 255f/2;
                    }

                    //cap
                    agent.input[0 + (3 * i)] = Math.min(agent.input[0 + (3 * i)], 255) / 300;
                    agent.input[1 + (3 * i)] = Math.min(agent.input[1 + (3 * i)], 255) / 300;
                    agent.input[2 + (3 * i)] = Math.min(agent.input[2 + (3 * i)], 255) / 300;
                    agent.eyesColor[i] = new Color(agent.input[0 + (i * 3)], agent.input[1 + (i * 3)], agent.input[2 + (i * 3)]);

                    //System.out.println(agent.input[0 + (i * 3)]+" "+agent.input[1 + (i * 3)]+" "+agent.input[2 + (i * 3)]);
                    //Color eyeColor = new Color(agent.input[0 + (i * 3)], agent.input[1 + (i * 3)], agent.input[2 + (i * 3)]);
                    //System.out.println(eyeColor);
                }
            }

            //check borders
            for (Agent agent : this.agents) {
                agent.update();
                //TODO: move to agent?
                boolean collision=false;
                if (agent.location.x >= this.getWidth() && agent.xVelocity > 0) {
                    agent.xVelocity = 0;
                    collision=true;
                }
                if (agent.location.x <= 0 && agent.xVelocity < 0) {
                    agent.xVelocity = 0;
                    collision=true;
                }
                if (agent.location.y > this.getHeight() && agent.yVelocity > 0) {
                    agent.yVelocity = 0;
                    collision=true;
                }
                if (agent.location.y <= 0 && agent.yVelocity < 0) {
                    agent.yVelocity = 0;
                    collision=true;
                }
                if(collision){
                    agent.energy-=this.valueFood*0.25f;
                }
                agent.location.setLocation(agent.location.x + agent.xVelocity, agent.location.y + agent.yVelocity);
                agent.updateEyeLocation();
            }
            for (Food food : this.foods) {
                food.update();
            }

            //check collisions
            for (Agent a1 : this.agents) {
                for (Agent a2 : this.agents) {
                    if (a1 != a2) {
                        double d = a1.location.distance(a2.location);
                        if (d < a1.diameter / 2 + a2.diameter / 2) {
                            //TODO: check overlap
                            double overlap = a1.diameter / 2 + a2.diameter / 2 - d;
                            Point dir = new Point(a2.location.x - a1.location.x, a2.location.y - a1.location.y);
                            //dir.length= overlap/2 
                            a2.location.translate(dir.x / 4, dir.y / 4);
                            a1.location.translate(-dir.x / 4, -dir.y / 4);
                        }
                    }
                }

                for (Iterator<Food> iter = this.foods.iterator(); iter.hasNext();) {
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
                if(food.value>=0){
                    currentnFood++;
                } else{
                    currentnPoison++;
                }
            }
            ArrayList<Food> foodsTemp = new ArrayList<>();
            for (int i = currentnFood; i < nFood-nPoison; i++) {
                Point location = new Point(rng.nextInt(this.getWidth()-10)+10, rng.nextInt(this.getHeight()-10)+10);
                Food newFood = new Food(location, (int)valueFood);
                foodsTemp.add(newFood);
            }
            this.foods.addAll(foodsTemp);
            foodsTemp = new ArrayList<>();
            for (int i = currentnPoison; i < nPoison; i++) {
                Point location = new Point(rng.nextInt(this.getWidth()-10)+10, rng.nextInt(this.getHeight()-10)+10);
                Food newFood = new Food(location, (int)valueFood);
                newFood.value*=-1;
                //newFood.value*=0.2f;
                foodsTemp.add(newFood);
            }
            this.foods.addAll(foodsTemp);

            //update population
            this.neat.update();

            //print statistic
            int totalFood = 0;
            for (Food food : this.foods) {
                totalFood += food.value;
            }
            int totalFitness = 0;
            int currentnAgents = 0;
            int totalBrainsize = 0;
            for (Agent agent : this.agents) {
                totalFitness += agent.fitness();
                currentnAgents += 1;
                totalBrainsize+=agent.brain.genome.nodes.size()+agent.brain.genome.links.size();
            }
            if(currentnAgents<=10){
                //this.poisonProb*=1.1f;
                Simulation.valueFood*=1.5f;
                this.init(this.nAgents, this.nFood);
                epoch=1;
                System.out.println("RESET "+ this.nFood);
            }

            //update drawing
            System.out.println("epoch: "+epoch+" valueFood: " + Simulation.valueFood+" totalFood: " + totalFood + " nAgents: " + currentnAgents + " averageFitness " + (totalFitness / (currentnAgents+0.001))+ " averageBrainSize " + (totalBrainsize / (currentnAgents+0.001)));
            if(System.currentTimeMillis()-currentTime>33) {
                this.repaint();
                currentTime=System.currentTimeMillis();
            }
            if(epoch>500000){
                this.sleep=10;
            }
            if(this.sleep>0) {
                try {
                    Thread.sleep(this.sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        //draw food first
        for (Food food : new ArrayList<Food>(this.foods)) {
            g.setColor(food.getColor());
            g.fillOval(food.location.x - food.diameter / 2, food.location.y - food.diameter / 2, food.diameter, food.diameter);
        }
        float maxEnergy = 1;
        for (Agent agent : new ArrayList<Agent>(this.agents)) {
            maxEnergy = Math.max(maxEnergy, agent.energy);
        }

        for (Agent agent : new ArrayList<Agent>(this.agents)) {
            for (int i = 0; i < agent.eyesLocation.length; i++) {
                g.setColor(agent.eyesColor[i]);
                Point eye = agent.eyesLocation[i];
                int eyeDiameter = agent.eyesDiameter[i];
                g.fillOval(eye.x - eyeDiameter / 2, eye.y - eyeDiameter / 2, eyeDiameter, eyeDiameter);
            }
            g.setColor(agent.color);
            g.fillOval(agent.location.x - agent.diameter / 2, agent.location.y - agent.diameter / 2, agent.diameter, agent.diameter);
            float w = (float)Math.tanh(agent.fitness() / maxEnergy);
            g.setColor(new Color(w,w,w));
            g.fillOval(agent.location.x - agent.diameter / 4, agent.location.y - agent.diameter / 4, agent.diameter/2, agent.diameter/2);
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
