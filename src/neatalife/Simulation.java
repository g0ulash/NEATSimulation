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
    public int nAgents;

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

        foods = new ArrayList<>(nFood);
        for (int i = 0; i < nFood; i++) {
            Point location = new Point(rng.nextInt(this.getWidth()), rng.nextInt(this.getHeight()));
            this.foods.add(new Food(location));
        }
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
        //update states
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
                    for (Iterator<Food> iter = this.foods.iterator(); iter.hasNext();) {
                        Food food = iter.next();
                        double d = eye.distance(food.location);
                        if (d < eyeDiameter / 2 + food.diameter / 2) {
                            agent.input[0 + (3 * i)] += food.color.getRed();
                            agent.input[1 + (3 * i)] += food.color.getGreen();
                            agent.input[2 + (3 * i)] += food.color.getBlue();
                        }

                    }
                    //cap
                    agent.input[0 + (3 * i)] = Math.min(agent.input[0 + (3 * i)], 255) / 256;
                    agent.input[1 + (3 * i)] = Math.min(agent.input[1 + (3 * i)], 255) / 256;
                    agent.input[2 + (3 * i)] = Math.min(agent.input[2 + (3 * i)], 255) / 256;
                    agent.eyesColor[i] = new Color(agent.input[0 + (i * 3)], agent.input[1 + (i * 3)], agent.input[2 + (i * 3)]);

                    //System.out.println(agent.input[0 + (i * 3)]+" "+agent.input[1 + (i * 3)]+" "+agent.input[2 + (i * 3)]);
                    //Color eyeColor = new Color(agent.input[0 + (i * 3)], agent.input[1 + (i * 3)], agent.input[2 + (i * 3)]);
                    //System.out.println(eyeColor);
                }
            }


            for (Agent agent : this.agents) {
                agent.update();
                //TODO: move to agent?
                if (agent.location.x >= this.getWidth() && agent.xVelocity > 0) {
                    agent.xVelocity = 0;
                }
                if (agent.location.x <= 0 && agent.xVelocity < 0) {
                    agent.xVelocity = 0;
                }
                if (agent.location.y > this.getHeight() && agent.yVelocity > 0) {
                    agent.yVelocity = 0;
                }
                if (agent.location.y <= 0 && agent.yVelocity < 0) {
                    agent.yVelocity = 0;
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
            ArrayList<Food> foodsTemp = new ArrayList<>();
            for (int i = this.foods.size(); i < nFood; i++) {
                Point location = new Point(rng.nextInt(this.getWidth()), rng.nextInt(this.getHeight()));
                foodsTemp.add(new Food(location));
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
            for (Agent agent : this.agents) {
                totalFitness += agent.fitness();
                currentnAgents += 1;
            }
            System.out.println("totalFood: " + totalFood + " nAgents: " + currentnAgents + " averageFitness " + totalFitness / currentnAgents);

            //update drawing
            this.repaint();
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(Simulation.class.getName()).log(Level.SEVERE, null, ex);
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

        for (Agent agent : new ArrayList<Agent>(this.agents)) {
            g.setColor(agent.color);
            g.fillOval(agent.location.x - agent.diameter / 2, agent.location.y - agent.diameter / 2, agent.diameter, agent.diameter);
            for (int i = 0; i < agent.eyesLocation.length; i++) {
                g.setColor(agent.eyesColor[i]);
                Point eye = agent.eyesLocation[i];
                int eyeDiameter = agent.eyesDiameter[i];
                g.fillOval(eye.x - eyeDiameter / 2, eye.y - eyeDiameter / 2, eyeDiameter, eyeDiameter);
            }
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
