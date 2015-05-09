/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neatalife;

import java.awt.Color;
import java.awt.Point;

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
    
    public float[] input;
    //input current energy;
    
    public Agent(Point location){
        this.brain = new Brain();
        this.xVelocity = 0;
        this.yVelocity = 0;
        this.energy = 10000;
        this.location = location;
        this.diameter = 16;
        this.color = Color.BLUE;
        
        int nSensors = 2;
        this.input = new float[nSensors*3];
        
        this.eyesLocation = new Point[nSensors];
        this.eyesColor = new Color[nSensors];
        this.updateEyeLocation();
        this.eyesDiameter = new int[nSensors];
        this.eyesDiameter[0] = this.diameter/2;
        this.eyesDiameter[1] = this.diameter/2;
    }
    
    public int fitness(){
        return energy;
    }
    
    public void updateEyeLocation() {         
        Point offset = new Point(0, this.diameter);
        this.eyesLocation[0] = new Point(this.location.x+offset.x, this.location.y+offset.y);
        this.eyesLocation[1] = new Point(this.location.x+offset.x, this.location.y-offset.y);
    }
    
    public void update(){
        //update outputs
        this.brain.setInput(this.input);
        this.brain.execute();
        float[] output = this.brain.getOutput();
        this.xVelocity = output[0];
        this.yVelocity = output[1];
        
        //update energy
        this.energy-=(1+Math.sqrt(Math.pow(xVelocity, 2)+Math.pow(yVelocity, 2)));
    }
    
}
