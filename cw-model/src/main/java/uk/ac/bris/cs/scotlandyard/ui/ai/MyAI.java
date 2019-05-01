package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.Set;
import java.util.function.Consumer;
import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.ui.ai.Pathfinding;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardModel;

@ManagedAI("Skynet")
public class MyAI implements PlayerFactory {

	@Override
	public Player createPlayer(Colour colour) {
		return new MyPlayer();
	}
	private static class MyPlayer implements Player {

		@Override
		public void makeMove(ScotlandYardView view, int location, Set<Move> moves, Consumer<Move> callback) {
			ScotlandYardModel model=(ScotlandYardModel)view;
			Move bestMove = Pathfinding.bestMove(moves,model);
			System.out.println(bestMove);
			callback.accept(bestMove);
		}
	}
}
