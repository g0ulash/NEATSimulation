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
public class Food {
    
    public int value; //<0 is poison
    public int age;
    public Point location;
    public int diameter;
    public Color color;

    public Food(Point location, int value, boolean reversed){
        this.value = value;
        this.age = 0;
        this.location = location;
        this.diameter = 14;
        if(this.value<0){
           this.color = Color.red;
        } else{
            this.color = Color.green;
        }
        if(reversed){
            if(this.value<0){
                this.color = Color.green;
            } else{
                this.color = Color.red;
            }
        }
    }
    
    public void update(){
        this.age++;
    }
}
