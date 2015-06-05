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
    public int age;
    public Point location;
    public int diameter;
    public Color color;
    
    public Point[] eyesLocation;
    public int[] eyesDiameter;
    public Color[] eyesColor;
    Random rng;
    
    public float[] input;
    //input current energy;
    
    public Agent(Point location){
        this.xVelocity = 0;
        this.yVelocity = 0;
        this.energy = (int)Simulation.valueFood;
        this.age = 0;
        this.location = location;
        this.diameter = 16;
        this.color = Color.BLUE;
        rng = new Random(location.x);
        
        int nSensors = 4;
        this.input = new float[nSensors*3+1];
        
        this.eyesLocation = new Point[nSensors];
        this.eyesColor = new Color[nSensors];
        this.eyesDiameter = new int[nSensors];
        for(int i=0; i<this.eyesDiameter.length; i++) {
            this.eyesDiameter[i] = this.diameter*2;
            this.eyesColor[i] = new Color(0, 0, 0);
        }
        this.updateEyeLocation();

        this.brain = new Brain(this.input.length, 2, rng);
    }
    
    public int fitness(){
        return Math.max(0, age);
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
        int modifier = 6;
        this.xVelocity = output[0]*modifier;
        this.yVelocity = output[1]*modifier;
        
        //update energy
        //System.out.println("NN cost: "+(this.brain.genome.nodes.size()+this.brain.genome.links.size()));
        //System.out.println("movement cost: "+Math.sqrt(Math.pow(xVelocity, 2)+Math.pow(yVelocity, 2)));
        float energyCost = Math.max(0, this.energy)/10000;
        float basecost = Simulation.valueFood/1000;
        float movementcost = (float)Math.sqrt(Math.pow(xVelocity, 2)+Math.pow(yVelocity, 2));
        float braincost = (float) (Math.pow(this.brain.genome.nodes.size() + this.brain.genome.links.size(), 2)/100000);
        float reproductionCost = 0;
        if (this.brain.genome.crossover){
            reproductionCost = basecost*0.1f;
        }
        this.energy-=(basecost+reproductionCost+braincost+energyCost)*(modifier/2);
        this.age++;
    }

    public Agent copy(){
        Agent newAgent = new Agent(new Point((int)(this.location.x+rng.nextGaussian()*2), (int)(this.location.y+rng.nextGaussian()*2)));
        newAgent.brain = this.brain.copy();
        return newAgent;
    }
    
}
