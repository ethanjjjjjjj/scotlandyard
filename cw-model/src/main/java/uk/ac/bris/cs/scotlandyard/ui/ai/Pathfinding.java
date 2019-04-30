package uk.ac.bris.cs.scotlandyard.ui.ai;


import java.util.Collection;
import uk.ac.bris.cs.scotlandyard.model.Move;
import java.util.Set;
import uk.ac.bris.cs.scotlandyard.model.DoubleMove;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;
import uk.ac.bris.cs.scotlandyard.model.PassMove;
import uk.ac.bris.cs.scotlandyard.model.Transport;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardModel;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardPlayer;
import java.util.ArrayList;

public class Pathfinding{

    //calculates the minimum number of moves to get from one node to another
    public static int minJumps(int node1,int node2 ,ScotlandYardModel model)  throws InterruptedException{

        Graph<Integer, Transport> graph = model.getGraph();
        int[] nodeDistances=new int[graph.getNodes().size()+2];
        //Boolean[] visited = new Boolean[graph.getNodes().size()];

        for(int i=1;i<graph.getNodes().size()+2;i++){
            nodeDistances[i]=Integer.MAX_VALUE;
        }

        
        ArrayList<Node<Integer>> queue=new ArrayList<>();
        Node<Integer> startNode = graph.getNode(node1);
        Boolean[] visited2=new Boolean[graph.getNodes().size()+2];
        for(int i=0;i<graph.getNodes().size()+2;i++){
            visited2[i]=false;
        }
        Node<Integer> endNode=graph.getNode(node2);

        nodeDistances[startNode.value()]=0;
        queue.add(startNode);
        while(!visited2[endNode.value()]){
            ArrayList<Thread> threads=new ArrayList<>();
            ArrayList<DijkstraWorker> workers=new ArrayList<>();
            for(Node<Integer> item:queue){
                DijkstraWorker worker=new DijkstraWorker(graph,nodeDistances,item,visited2);
                workers.add(worker);
                threads.add(new Thread(worker));

                queue.remove(item);
                
            }
            for(Thread t:threads){
                t.start();

            }
            for(Thread t:threads){
                t.join();

            }
            for(DijkstraWorker d:workers){
                for(Node<Integer> item:d.getqueue()){
                    queue.add(item);
                }
            }
            
        }

        //System.out.println("mindistance"+String.valueOf(nodeDistances[endNode.value()]));
        return nodeDistances[endNode.value()];

    }

    //calculates the average number of hops it mr x is away from the detectives with the given move
    public static int averageMoveDist(int playerLocation,TicketMove m,ScotlandYardModel model)  throws InterruptedException{
        return minJumps(playerLocation, m.destination(),model);
    }

    public static int averageMoveDist(int playerLocation,DoubleMove m,ScotlandYardModel model)  throws InterruptedException {
        return minJumps(playerLocation, m.secondMove().destination(),model);
    }

    public static int averageMoveDist(int playerLocation,PassMove m,ScotlandYardModel model){


        return Integer.MIN_VALUE+1;

    }
    public static int averageMoveDist(Move m,ScotlandYardModel model)  throws InterruptedException{
        ArrayList<ScotlandYardPlayer> playersfirst = model.getMutablePlayers();
        ArrayList<ScotlandYardPlayer> players= new ArrayList<>(playersfirst);

        int distanceSum=0;
        int location;
        for(ScotlandYardPlayer p:players){
            location=p.location();
        if(m instanceof DoubleMove){
            distanceSum+= averageMoveDist(location,(DoubleMove)m,model);
        }

        else if(m instanceof TicketMove){
            distanceSum+= averageMoveDist(location,(TicketMove)m,model);
        }
        else if(m instanceof PassMove){
            distanceSum+= averageMoveDist(location ,(PassMove)m,model);
        }
        
    }
    
    return distanceSum/players.size();
}

    


    public static Move bestMove(Set<Move> moves,ScotlandYardModel model)  throws InterruptedException{
        int bestMoveScore = Integer.MIN_VALUE;
        Move bestMove=null;
        for(Move item:moves){
            if(averageMoveDist(item,model)>bestMoveScore){
                bestMoveScore=averageMoveDist(item,model);
                bestMove=item;
            }
        }
        return bestMove;
    }


}