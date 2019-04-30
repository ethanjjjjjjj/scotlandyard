package uk.ac.bris.cs.scotlandyard.ui.ai;
import uk.ac.bris.cs.scotlandyard.model.Move;
import java.util.Set;
import uk.ac.bris.cs.scotlandyard.model.DoubleMove;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;
import uk.ac.bris.cs.scotlandyard.model.PassMove;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;
import uk.ac.bris.cs.gamekit.graph.Node;

public class Pathfinding{

    //calculates the minimum number of moves to get from one node to another
    public int minJumps(int node1,int node2){
        return 1;
    }

    //calculates the average number of hops it mr x is away from the detectives with the given move
    public int averageMoveDist(int playerLocation,TicketMove m){
        return minJumps(playerLocation, m.destination());
    }

    public int averageMoveDist(int playerLocation,DoubleMove m){
        return minJumps(playerLocation, m.secondMove().destination());
    }

    public int averageMoveDist(int playerLocation,PassMove m){


        return Integer.MIN_VALUE+1;

    }
    public int averageMoveDist(int playerLocation,Move m){


        if(m instanceof DoubleMove){
            return averageMoveDist(playerLocation,m);
        }

        else if(m instanceof TicketMove){
            return averageMoveDist(playerLocation,m);
        }
        else if(m instanceof PassMove){
            return averageMoveDist(playerLocation,m);
        }
        else return 0;
    }

    


    public Move bestMove(int playerLocation,Set<Move> moves){
        int bestMoveScore=Integer.MIN_VALUE;
        Move bestMove;
        for(Move item:moves){
            if(averageMoveDist(playerLocation,item)>bestMoveScore){
                bestMoveScore=averageMoveDist(playerLocation,item);
                bestMove=item;
            }
        }
        return bestMove;
    }


}