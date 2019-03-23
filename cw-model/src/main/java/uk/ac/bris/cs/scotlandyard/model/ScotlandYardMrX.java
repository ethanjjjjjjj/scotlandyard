package uk.ac.bris.cs.scotlandyard.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A class that extends ScotlandYardPlayer, specifically for MrX
 */
public class ScotlandYardMrX extends ScotlandYardPlayer {

    private int lastSeen;

    public ScotlandYardMrX(Player player, Colour colour, int location,
			Map<Ticket, Integer> tickets) {
            super(player, colour, location, tickets);
            this.lastSeen = 0;
    }
    
    public void lastSeen(int location){
        this.lastSeen = location;
    }

    public int lastSeen(){
        return this.lastSeen;
    }

    /**
     * After MrX has had their turn in {@link #startRotate()}, this needs to be called on true rounds
     */
    public void updateLastSeen(){
        this.lastSeen = this.location();
    }


}