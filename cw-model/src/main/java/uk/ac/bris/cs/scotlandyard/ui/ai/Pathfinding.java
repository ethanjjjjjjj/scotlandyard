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
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardPlayer;
import java.util.ArrayList;
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