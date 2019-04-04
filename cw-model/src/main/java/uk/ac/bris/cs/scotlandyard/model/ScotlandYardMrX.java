package uk.ac.bris.cs.scotlandyard.model;

import java.util.HashMap;
import java.util.Map;


/**
 * A class that extends ScotlandYardPlayer, specifically for MrX
 */
public class ScotlandYardMrX extends ScotlandYardPlayer {

    private int lastSeen;
    private int turnsPlayed;

    public ScotlandYardMrX(Player player, Colour colour, int location,
			Map<Ticket, Integer> tickets) {
            super(player, colour, location, tickets);
            this.lastSeen = 0;
            this.turnsPlayed = 0;
    }
    
    public void lastSeen(int location){
        this.lastSeen = location;
    }

    public int lastSeen(){
        return this.lastSeen;
    }

    public void incTurnsPlayed(){
        this.turnsPlayed++;
    }

    public int turnsPlayed(){
        return this.turnsPlayed;
    }

    /**
     * After MrX has had their turn in {@link #startRotate()}, this needs to be called on true rounds
     */
    public void updateLastSeen(){
        this.lastSeen = this.location();
    }


}