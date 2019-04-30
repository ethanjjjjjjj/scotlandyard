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
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardModel;
public class Pathfinding{

    //calculates the minimum number of moves to get from one node to another
    public static int minJumps(int node1,int node2 ,ScotlandYardModel model){
        return 1;
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
    public static int averageMoveDist(int playerLocation,Move m,ScotlandYardModel model){


        if(m instanceof DoubleMove){
            return averageMoveDist(playerLocation,m,model);
        }

        else if(m instanceof TicketMove){
            return averageMoveDist(playerLocation,m,model);
        }
        else if(m instanceof PassMove){
            return averageMoveDist(playerLocation,m,model);
        }
        else return 0;
    }

    


    public static Move bestMove(int playerLocation,Set<Move> moves,ScotlandYardModel model){
        int bestMoveScore=Integer.MIN_VALUE;
        Move bestMove=null;
        for(Move item:moves){
            if(averageMoveDist(playerLocation,item,model)>bestMoveScore){
                bestMoveScore=averageMoveDist(playerLocation,item,model);
                bestMove=item;
            }
        }
        return bestMove;
    }


}