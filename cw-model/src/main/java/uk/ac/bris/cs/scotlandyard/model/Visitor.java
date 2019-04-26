package uk.ac.bris.cs.scotlandyard.model;

import static uk.ac.bris.cs.scotlandyard.model.Ticket.DOUBLE;


/**
 * The implementation of the MoveVisitor interface
 * @param model The model of the game
 * @param currentPlayer The current player of the model
 */
public class Visitor implements MoveVisitor {

    private ScotlandYardModel model;
    private ScotlandYardPlayer currentPlayer;

    Visitor(ScotlandYardModel model){
        this.model = model;
        this.currentPlayer = model.getMutablePlayer(model.getCurrentPlayer());
    }

	public void visit(PassMove move) {
		model.nextPlayer();
		model.spectatorsOnMoveMade(move,this.currentPlayer);
    }

	
	public void visit(TicketMove move) {
		model.nextPlayer();
		model.prepareNextRound(move,this.currentPlayer);
		model.spectatorsOnMoveMade(move,this.currentPlayer);
    }

	
	public void visit(DoubleMove move) {
		model.nextPlayer();
		model.getMutablePlayer(Colour.BLACK).removeTicket(DOUBLE);
		model.spectatorsOnMoveMade(move,this.currentPlayer);
		TicketMove move1 = move.firstMove();
		TicketMove move2 = move.secondMove();
		model.editMrXTicketsForDoubleMove(move1);   model.spectatorsOnMoveMade(move1,this.currentPlayer);
		model.editMrXTicketsForDoubleMove(move2);   model.spectatorsOnMoveMade(move2,this.currentPlayer);
    }

}


