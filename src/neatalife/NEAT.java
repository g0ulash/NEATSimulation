/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neatalife;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import neatalife.Brain.Genome.Link;

/**
 *
 * @author Paul
 */
public class NEAT {

    ArrayList<Agent> population;
    ArrayList<Link> innovations;
    Random rng;

    public NEAT(ArrayList<Agent> population, Random rng) {
        this.population = population;
        this.rng = rng;
        Brain.INNOVATION_NUMBER=0;
        innovations = new ArrayList<>();
    }

    public void update() {
        //clear current innovations
        //FIXME: reset innovations each generation?
        while(this.innovations.size()>this.population.size()){
            this.innovations.remove(0);
        }
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
        //TODO: separate sexual from asexual population, or run separate experiments for each type in different environments and compare fitness.
        ArrayList<Agent> newAgents = new ArrayList<>();
        while(population.size()>=2 && population.size()+newAgents.size()<=48){
            //selection, pick 2 for reproduction
            Agent agent1 = this.selection();
            Agent agent2 = this.selection();
            while(agent2==agent1){
                agent2 = this.selection();
            }
            Agent offspring1 = agent1.copy();
            Agent offspring2 = agent2.copy();
            if(agent1.brain.genome.crossover || agent2.brain.genome.crossover) {
                if (rng.nextFloat() <= agent1.brain.genome.crossoverRate*0.1f) {
                    //.out.println("CROSSOVER");
                    this.crossover(agent1, agent2, offspring1, offspring2);
                }
            }
            newAgents.add(offspring1);
            newAgents.add(offspring2);
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

    public void crossover(Agent parent1, Agent parent2, Agent offspring1, Agent offspring2){
        //TODO: reactivate disabled links?
        for(Link link : parent1.brain.genome.links){
            //check match
            boolean match = false;
            Link matchedLink = null;
            for(Link link2 : parent2.brain.genome.links){
                if( link.input.id == link2.input.id && link.output.id == link2.output.id){ //FIXME: use innovation number
                    match = true;
                    matchedLink = link2;
                    break;
                }
            }
            if(match){
                //randomly pick link
                if(rng.nextInt(2)==0){
                    Link newLink1 = link.copy(offspring1.brain.genome.nodes);
                    Link newLink2 = matchedLink.copy(offspring2.brain.genome.nodes);
                    if(newLink1.input == null || newLink2.output==null){
                        System.out.println("MISMATCH");
                    }
                    offspring1.brain.genome.links.add(newLink1);
                    offspring2.brain.genome.links.add(newLink2);
                } else {
                    //reverse
                    Link newLink1 = matchedLink.copy(offspring1.brain.genome.nodes);
                    Link newLink2 = link.copy(offspring2.brain.genome.nodes);
                    if(newLink1.input == null || newLink2.output==null){
                        System.out.println("MISMATCH");
                    }
                    offspring1.brain.genome.links.add(newLink1);
                    offspring2.brain.genome.links.add(newLink2);
                }
            } else {
                //TODO: check missing nodes
                //add the link to both
                addLinkToBoth(offspring1, offspring2, link);
            }
        }
        //check other parent
        for(Link link : parent2.brain.genome.links){
            //check match
            boolean match = false;
            for(Link link2 : parent1.brain.genome.links){
                if( link.input.id == link2.input.id && link.output.id == link2.output.id){
                    match = true;
                    break;
                }
            }
            if(match){
                //ignore, already visited
            } else {
                //TODO: check missing nodes
                //add the link to both
                addLinkToBoth(offspring1, offspring2, link);
            }
        }
        if(rng.nextInt(2)==0) {
            if (parent1.brain.genome.crossover) {
                offspring1.brain.genome.crossover = true;
            }
            if (parent2.brain.genome.crossover) {
                offspring2.brain.genome.crossover = true;
            }
        } else {
            if (parent2.brain.genome.crossover) {
                offspring1.brain.genome.crossover = true;
            }
            if (parent1.brain.genome.crossover) {
                offspring2.brain.genome.crossover = true;
            }
        }
    }

    private void addLinkToBoth(Agent offspring1, Agent offspring2, Link link) {
        Link newLink1 = link.copy(offspring1.brain.genome.nodes);
        Link newLink2 = link.copy(offspring2.brain.genome.nodes);
        checkMismatch(newLink1, offspring1);
        checkMismatch(newLink2, offspring2);
        offspring1.brain.genome.links.add(newLink1);
        offspring2.brain.genome.links.add(newLink2);
    }

    private void checkMismatch(Link newLink, Agent offspring){
        if(newLink.input==null){
            //mismatch, create new node
            Brain.Genome.Node newNode = offspring.brain.genome.addNode();
            newLink.input = newNode;
        }
        if(newLink.output==null){
            //mismatch, create new node
            Brain.Genome.Node newNode = offspring.brain.genome.addNode();
            newLink.output = newNode;
        }
    }
}
