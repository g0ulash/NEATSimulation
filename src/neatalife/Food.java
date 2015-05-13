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
    public Point location;
    public int diameter;
    
    public Food(Point location, int value){
        this.value = value;
        this.location = location;
        this.diameter = 14;
    }

    public Color getColor(){
        if(this.value<0){
            return Color.red;
        } else{
            return Color.green;
        }
    }
    
    public void update(){
        //mutate
    }
}
