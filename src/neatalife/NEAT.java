/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neatalife;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author Paul
 */
public class NEAT {

    ArrayList<Agent> population;
    ArrayList<Brain.Genome.Link> innovations;
    Random rng;

    public NEAT(ArrayList<Agent> population, Random rng) {
        this.population = population;
        this.rng = rng;
        Brain.INNOVATION_NUMBER=0;
    }

    public void update() {
        //clear current innovations
        //TODO: reset innovations each generation, move to agent?
        innovations = new ArrayList<>();
        //update population
        for (Iterator<Agent> iter = this.population.iterator(); iter.hasNext();) {
            Agent agent = iter.next();
            //remove
            if(agent.energy<=0){
                iter.remove();
            }
        }
        //reproduce
        if(population.size()<2){
            System.out.println("ERROR POPULATION SIZE");
        }
        ArrayList<Agent> newAgents = new ArrayList<>();
        while(population.size()>=2 && population.size()+newAgents.size()<=48){
            //selection, pick 2 for reproduction
            Agent agent1 = this.selection();
            Agent agent2 = this.selection();
            while(agent2==agent1){
                agent2 = this.selection();
            }
            newAgents.add(agent1.copy());
            newAgents.add(agent2.copy());
        }
        //mutate/crossover offspring
        for(Agent agent : newAgents){
            agent.brain.genome.mutate(innovations);
        }
        this.population.addAll(newAgents);
    }

    private Agent selection(){
        Agent selected = population.get(0);
        double total = population.get(0).fitness();

        for( int i = 1; i < population.size(); i++ ) {
            if(total<=0){
                System.out.println("ERROR FITNESS <0");
            }
            total += population.get(i).fitness();
            if( rng.nextDouble() <= (population.get(i).fitness() / total)) {
                selected = population.get(i);
            }
        }
        return selected;
    }
}
