package uk.ac.bris.cs.scotlandyard.ui.ai;


import uk.ac.bris.cs.scotlandyard.model.Transport;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import java.util.ArrayList;
public class DijkstraWorker implements Runnable{
    Graph<Integer,Transport> graph;
    int nodeDistances[];
    Node<Integer> currentNode;
    ArrayList<Node<Integer>> queue;
    Boolean[] visited;
    public ArrayList<Node<Integer>> toAdd;
    
    public ArrayList<Node<Integer>> getqueue(){
        return toAdd;
    }
    public DijkstraWorker(Graph<Integer,Transport> graph, int nodeDistances[],Node<Integer> currentNode,Boolean[] visited){
        this.graph=graph;
        this.nodeDistances=nodeDistances;
        this.currentNode=currentNode;
       
        this.visited=visited;
        this.toAdd=new ArrayList<Node<Integer>>();
    }
    

    @Override
    public void run() {
        ArrayList<Edge<Integer,Transport>> edges =new ArrayList<Edge<Integer,Transport>>(graph.getEdgesFrom(currentNode));

        for(Edge<Integer,Transport> item:edges){
            if(!this.visited[item.destination().value()] && !this.queue.contains(item.destination())){
            this.toAdd.add(item.destination());
        }
            if(!(this.nodeDistances[item.destination().value()]<(this.nodeDistances[this.currentNode.value()]+1)))
            this.nodeDistances[item.destination().value()]=this.nodeDistances[this.currentNode.value()]+1;
        }
        this.visited[this.currentNode.value()]=true;
    }
    
}