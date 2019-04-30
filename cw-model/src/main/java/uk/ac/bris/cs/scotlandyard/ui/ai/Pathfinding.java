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

    public static int minDistance(int distance[], Boolean visited[],int total) { 
        int min = Integer.MAX_VALUE;
        int min_index=-1; 
  
        for (int v = 1; v <= total+1; v++) {
            if (!visited[v] && distance[v] <= min){ 
                min = distance[v]; 
                min_index = v; 
            } 
        }
        return min_index; 
    } 

    //calculates the minimum number of moves to get from one node to another
    public static int minJumps(int node1,int node2 ,ScotlandYardModel model){
        Graph<Integer,Transport> graph = model.getGraph();
        Node<Integer> start = graph.getNode(node1);
        Node<Integer> end = graph.getNode(node2);
        int totalNodes = graph.getNodes().size();
        Boolean[] visited = new Boolean[totalNodes+1];
        int[] distance = new int[totalNodes+1];

        for (int i = 1; i <= totalNodes+1; i++) { 
            distance[i] = Integer.MAX_VALUE; 
            visited[i] = false; 
        } 
        distance[node1] = 0;

        for (int j = 1; j <= totalNodes+1; j++) { 
            // Pick the minimum distance vertex from the set of vertices 
            // not yet processed. u is always equal to src in first 
            // iteration. 
            int u = minDistance(distance, visited,totalNodes); 
  
            // Mark the picked vertex as processed 
            visited[u] = true; 
  
            // Update dist value of the adjacent vertices of the 
            // picked vertex. 
            Collection<Edge<Integer,Transport>> edges = graph.getEdgesFrom(graph.getNode(j));
            for (int v = 1; v <= totalNodes+1; v++){ 
                // Update dist[v] only if is not in sptSet, there is an 
                // edge from u to v, and total weight of path from src to 
                // v through u is smaller than current value of dist[v] 
                if (!visited[v] && distance[u]!=0 && distance[u] != Integer.MAX_VALUE &&
                distance[u]+distance[v] < distance[v] && graph.getEdgesFrom(graph.getNode())){ 
                    distance[v] = distance[u] + 1; 
                }
            }
        } 
        return distance[node2];
    }

    //calculates the average number of hops it mr x is away from the detectives with the given move
    public static int averageMoveDist(int playerLocation,TicketMove m,ScotlandYardModel model){
        return minJumps(playerLocation, m.destination(),model);
    }

    public static int averageMoveDist(int playerLocation,DoubleMove m,ScotlandYardModel model){
        return minJumps(playerLocation, m.secondMove().destination(),model);
    }

    public static int averageMoveDist(int playerLocation,PassMove m,ScotlandYardModel model){


        return Integer.MIN_VALUE+1;

    }
    public static int averageMoveDist(Move m,ScotlandYardModel model){
        ArrayList<ScotlandYardPlayer> players=model.getMutablePlayers();
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

    


    public static Move bestMove(Set<Move> moves,ScotlandYardModel model){
        int bestMoveScore=Integer.MIN_VALUE;
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