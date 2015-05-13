/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neatalife;

import java.awt.Color;
import java.awt.Point;
import java.util.Random;

/**
 *
 * @author Paul
 */
public class Agent {
    
    public Brain brain;
    public float xVelocity;
    public float yVelocity;
    
    public int energy;
    public Point location;
    public int diameter;
    public Color color;
    
    public Point[] eyesLocation;
    public int[] eyesDiameter;
    public Color[] eyesColor;
    Random rng = new Random();
    
    public float[] input;
    //input current energy;
    
    public Agent(Point location){
        this.xVelocity = 0;
        this.yVelocity = 0;
        this.energy = (int)Simulation.valueFood;
        this.location = location;
        this.diameter = 16;
        this.color = Color.BLUE;
        
        int nSensors = 4;
        this.input = new float[nSensors*3+1];
        
        this.eyesLocation = new Point[nSensors];
        this.eyesColor = new Color[nSensors];
        this.eyesDiameter = new int[nSensors];
        for(int i=0; i<this.eyesDiameter.length; i++) {
            this.eyesDiameter[i] = this.diameter*2;
        }
        this.updateEyeLocation();

        this.brain = new Brain(this.input.length, 2);
    }
    
    public int fitness(){
        return Math.max(0, energy);
    }
    
    public void updateEyeLocation() {         
        Point offset = new Point(this.diameter, this.diameter);
        this.eyesLocation[0] = new Point(this.location.x+offset.x, this.location.y);
        this.eyesLocation[1] = new Point(this.location.x-offset.x, this.location.y);
        this.eyesLocation[2] = new Point(this.location.x, this.location.y+offset.y);
        this.eyesLocation[3] = new Point(this.location.x, this.location.y-offset.y);
    }
    
    public void update(){
        //update outputs
        this.input[this.input.length-1] = (float)Math.tanh(this.energy/Simulation.valueFood);
        this.brain.setInput(this.input);
        this.brain.execute();
        float[] output = this.brain.getOutput();
        this.xVelocity = output[0]*2;
        this.yVelocity = output[1]*2;
        
        //update energy
        //System.out.println("NN cost: "+(this.brain.genome.nodes.size()+this.brain.genome.links.size()));
        //System.out.println("movement cost: "+Math.sqrt(Math.pow(xVelocity, 2)+Math.pow(yVelocity, 2)));
        float basecost = 50f;
        float movementcost = (float)Math.sqrt(Math.pow(xVelocity, 2)+Math.pow(yVelocity, 2))*10;
        float braincost = (float) (Math.pow(this.brain.genome.nodes.size() + this.brain.genome.links.size(), 2)/50000);
        this.energy-=movementcost+basecost+braincost;
    }

    public Agent copy(){
        Agent newAgent = new Agent(new Point((int)(this.location.x+rng.nextGaussian()*15), (int)(this.location.y+rng.nextGaussian()*15)));
        newAgent.brain = this.brain.copy();
        return newAgent;
    }
    
}
