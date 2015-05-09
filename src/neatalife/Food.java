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
    public Color color;
    
    public Food(Point location){
        this.value = 25000;
        this.location = location;
        this.diameter = 5;
        this.color = Color.GREEN;
    }
    
    public void update(){
        //mutate
    }
}
