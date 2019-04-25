package uk.ac.bris.cs.scotlandyard.model;

import static uk.ac.bris.cs.scotlandyard.model.Ticket.DOUBLE;


/**
 * The implementation of the MoveVisitor interface
 * @param model The model of the game
 * @param currentPlayer The current player of the model
 */
public class Visitor implements MoveVisitor {

    ScotlandYardModel model;
    ScotlandYardPlayer currentPlayer;

    Visitor(ScotlandYardModel model){
        this.model = model;
        this.currentPlayer = model.currentPlayer;
    }

	public void visit(PassMove move) {
		model.nextPlayer();
		model.spectatorsOnMoveMade(move,currentPlayer);
    }

	
	public void visit(TicketMove move) {
		model.nextPlayer();
		model.prepareNextRound(move,currentPlayer);
		model.spectatorsOnMoveMade(move,currentPlayer);
    }

	
	public void visit(DoubleMove move) {
		model.nextPlayer();
		model.mutablePlayers.get(0).removeTicket(DOUBLE);
		model.spectatorsOnMoveMade(move,currentPlayer);
		TicketMove move1 = move.firstMove();
		TicketMove move2 = move.secondMove();
		model.editMrXTicketsForDoubleMove(move1);   model.spectatorsOnMoveMade(move1,currentPlayer);
		model.editMrXTicketsForDoubleMove(move2);   model.spectatorsOnMoveMade(move2,currentPlayer);
    }

}
