/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neatalife;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Paul
 */
public class Brain {

    public static int INNOVATION_NUMBER=0;
    float[] input;
    float[] output;
    Random rng = new Random();
    Genome genome;

    public enum Type {input, hidden, output}

    public Brain(int inputSize, int outputSize) {
        input = new float[inputSize];
        output = new float[outputSize];
        genome = new Genome(input.length, output.length);
    }

    public void setInput(float[] input) {
        this.input = input;
    }

    public float[] getOutput(){
        return this.output;
    }

    public void execute() {
        this.output=genome.execute(this.input);
        //this.output[0] = rng.nextFloat()*2-1;
        //this.output[1] = rng.nextFloat()*2-1;
    }

    public Brain copy(){
        Brain newBrain = new Brain(this.genome.inputSize, this.genome.outputSize);
        newBrain.genome = this.genome.copy();
        return newBrain;
    }

    public class Genome {

        public ArrayList<Node> nodes;
        public ArrayList<Link> links;
        public int inputSize;
        public int outputSize;
        public float mutationRate = 0.05f;
        public float crossoverRate = 0.1f;
        public int currentId;

        public Random rng = new Random();

        public Genome(int inputSize1, int outputSize1){
            //init nodes
            this.inputSize = inputSize1+1;
            this.outputSize = outputSize1;
            nodes=new ArrayList<>(inputSize+outputSize);
            this.currentId=0;
            for(int i=0; i<inputSize;i++) {
                nodes.add(new Node(Type.input, currentId));
                currentId++;
            }
            for(int i=0; i<outputSize;i++) {
                nodes.add(new Node(Type.output, currentId));
                currentId++;
            }
            //init links
            links=new ArrayList<>(inputSize*outputSize);
            for(int i=0; i<inputSize;i++){
                for(int j=0; j<outputSize;j++){
                    Link newLink=new Link();
                    newLink.input = nodes.get(i);
                    newLink.output = nodes.get(inputSize+j);
                    newLink.innovation=i*inputSize+j;
                    newLink.weight=(float)rng.nextGaussian()/outputSize;
                    links.add(newLink);
                }
            }
            if(Brain.INNOVATION_NUMBER==0){
                Brain.INNOVATION_NUMBER=inputSize*outputSize;
            }
        }

        public void mutate(ArrayList<Link> innovations){
            //normal mutations
            for(Link link: links) {
                if (rng.nextFloat() <= mutationRate) {
                    link.weight+=rng.nextGaussian();
                }
            }

            //Structural mutation
            //add links
            if(rng.nextFloat()<=mutationRate && links.size()<1500){
                //pick two random nodes
                Node node1 = nodes.get(rng.nextInt(nodes.size()));
                Node node2 = nodes.get(rng.nextInt(nodes.size()));
                while(node2==node1 || node2.type==Type.input){
                    node2 = nodes.get(rng.nextInt(nodes.size()));
                }
                //check exists
                boolean exists=false;
                Link newLink = null;
                for(Link link: links){ //todo: optimize
                    if(!link.enabled && link.input==node1 && link.output==node2){
                        exists=true;
                        //reenable
                        link.enabled=true;
                        newLink = link;
                    }
                }
                //else insert link
                if(!exists){
                    newLink = new Link();
                    newLink.input = node1;
                    newLink.output = node2;
                }

                //check others in population, update innovation number.
                checkInnovations(innovations, newLink);
            }

            //add node
            ArrayList<Link> newLinks = new ArrayList<>();
            for(Link link: links){
                if(rng.nextFloat()<=mutationRate && links.size()<1500){
                    if(link.enabled){
                        //disable existing
                        link.enabled=false;
                        //insert new node and links
                        Node newNode = new Node(Type.hidden, currentId);
                        currentId++;
                        nodes.add(newNode);
                        Link newLink1 = new Link();
                        newLink1.input = link.input;
                        newLink1.output = newNode;
                        checkInnovations(innovations, newLink1);
                        Link newLink2 = new Link();
                        newLink2.input = newNode;
                        newLink2.weight = link.weight;
                        newLink2.output = link.output;
                        checkInnovations(innovations, newLink2);
                        newLinks.add(newLink1);
                        newLinks.add(newLink2);
                    }
                }
            }
            links.addAll(newLinks);
        }

        private void checkInnovations(ArrayList<Link> innovations, Link newLink) {
            boolean newInnovation=true;
            for(Link link: innovations){
                if(link.input.id==newLink.input.id && link.output.id==newLink.output.id){
                    newInnovation=false;
                    newLink.innovation=link.innovation;
                    break;
                }
            }
            //if new innovation
            if(newInnovation) {
                Brain.INNOVATION_NUMBER++;
                newLink.innovation=Brain.INNOVATION_NUMBER;
                //add innovation
                innovations.add(newLink);
            }
        }

        public void crossover(){

        }

        public Genome copy(){
            //TODO: impl copy
            Genome copy = new Genome(this.inputSize-1, this.outputSize);
            copy.nodes = new ArrayList<>();
            for(Node node: this.nodes){
                copy.nodes.add(node.copy());
            }
            copy.links = new ArrayList<>();
            for(Link link: this.links){
                copy.links.add(link.copy(copy.nodes));
            }
            copy.currentId = this.currentId;
            return copy;
        }

        public float[] execute(float[] input){
            float[] output = new float[this.outputSize];
            //construct network and compute outputs;
            //set bias and input
            nodes.get(inputSize-1).output=1;
            for(int i=0; i<inputSize-1; i++){
                Node node=nodes.get(i);
                node.output=input[i];
            }

            //compute summation
            for(Link link: links){
                link.output.summation += link.input.output*link.weight;
            }
            //compute final output and reset summation
            for(Node node: nodes){
                node.activate();
            }
            //return output
            for(int i=0; i<outputSize; i++) {
                output[i] = this.nodes.get(this.inputSize+i).output;
            }
            return output;
        }

        public class Node{

            public int id;
            public Type type;
            public float output;
            public float summation;
            //public boolean enabled;
            //public int innovation;

            public Node(Type type, int id){
                this.type = type;
                this.id = id;
            }

            public void activate(){
                this.output = (float)Math.tanh(summation);
                summation=0;
            }

            public Node copy(){
                Node newNode = new Node(this.type, this.id);
                //TODO: check output preservation.
                newNode.output=this.output;
                newNode.summation=this.summation;
                return newNode;
            }
        }

        public class Link{

            public Node input;
            public Node output;
            public float weight;
            public boolean enabled;
            public int innovation;

            public Link(){
                this.weight = 1;
                this.enabled = true;
            }

            public Link copy(ArrayList<Node> nodes){
                Link newLink = new Link();
                newLink.weight = this.weight;
                newLink.innovation = this.innovation;
                newLink.enabled = this.enabled;
                //TODO: efficiency.
                for(Node node:nodes){
                    if(this.input.id==node.id){
                        newLink.input = node;
                    }
                    if(this.output.id==node.id){
                        newLink.output = node;
                    }
                }
                return newLink;
            }
        }
    }
}
