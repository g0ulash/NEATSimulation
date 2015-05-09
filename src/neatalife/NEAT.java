/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neatalife;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Paul
 */
public class NEAT {

    ArrayList<Agent> population;

    public NEAT(ArrayList<Agent> population) {
        this.population = population;
    }

    public void update() {
        //update population
        for (Iterator<Agent> iter = this.population.iterator(); iter.hasNext();) {
            Agent agent = iter.next();
            if(agent.fitness()<0){
                iter.remove();
            }
        }
    }
}
