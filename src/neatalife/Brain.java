/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neatalife;

import java.util.Random;

/**
 *
 * @author Paul
 */
public class Brain {

    float[] input;
    float[] output;
    Random rng = new Random();

    public Brain() {
        input = new float[2];
        output = new float[2];
    }

    public void setInput(float[] input) {
        this.input = input;
    }
    
    public float[] getOutput(){
        return this.output;
    }

    public void execute() {
        this.output[0] = rng.nextFloat()*2-1;
        this.output[1] = rng.nextFloat()*2-1;
    }
}
