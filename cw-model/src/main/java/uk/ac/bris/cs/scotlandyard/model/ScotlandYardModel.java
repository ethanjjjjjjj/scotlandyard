package uk.ac.bris.cs.scotlandyard.model;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static uk.ac.bris.cs.scotlandyard.model.Colour.BLACK;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.DOUBLE;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.SECRET;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Arrays;
import java.util.function.Consumer;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;
import uk.ac.bris.cs.scotlandyard.model.Ticket;


// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame {
	List<Boolean> rounds;
	Graph<Integer, Transport> graph;
	PlayerConfiguration mrX;
	PlayerConfiguration firstDetective;
	ArrayList<PlayerConfiguration> restOfTheDetectives;
	ArrayList<PlayerConfiguration> players;

	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
			PlayerConfiguration mrX, PlayerConfiguration firstDetective,
			PlayerConfiguration... restOfTheDetectives) {
		this.rounds = requireNonNull(rounds);
		this.graph = requireNonNull(graph);
		this.mrX = requireNonNull(mrX);
		this.restOfTheDetectives= new ArrayList<>(Arrays.asList(restOfTheDetectives));

		if (rounds.isEmpty()) {
			throw new IllegalArgumentException("Empty rounds");
		}
		
		if (graph.isEmpty()){
			throw new IllegalArgumentException("Empty grpah");
		}

		if (mrX.colour != BLACK) {
			throw new IllegalArgumentException("MrX should be Black");
		}

		ArrayList<PlayerConfiguration> configurations = new ArrayList<>();
		for (PlayerConfiguration configuration : restOfTheDetectives){
			configurations.add(requireNonNull(configuration));
		}
		configurations.add(0, firstDetective);
		configurations.add(0, mrX);
		this.players = configurations;
		Set<Integer> set = new HashSet<>();//Checking there are not duplicate colours or locations
		Set<Colour> setColour = new HashSet<>();
		for (PlayerConfiguration configuration : players) {
			if (set.contains(configuration.location)){
				throw new IllegalArgumentException("Duplicate location");
			}
			if (setColour.contains(configuration.colour)){
				throw new IllegalArgumentException("Duplicate colour");
			}
		set.add(configuration.location);
		setColour.add(configuration.colour);
		}
		

		//checks whether the detective pr MrX has invalid tickets or missing tickets
		checkTickets(configurations);

		
	}
	
	public void checkTickets(ArrayList<PlayerConfiguration> configs){

		

		for(PlayerConfiguration player:configs){
			if(player.tickets.containsKey(DOUBLE) ||  player.tickets.containsKey(SECRET)){
				if(player.colour.isDetective() && (player.tickets.get(DOUBLE) != 0 ||  player.tickets.get(SECRET) != 0)){
					throw new IllegalArgumentException("Detective has invalid tickets");
				}
			}
			if(player.colour.isDetective()  && (!(player.tickets.containsKey(Ticket.TAXI)) || !(player.tickets.containsKey(Ticket.BUS)) || !(player.tickets.containsKey(Ticket.UNDERGROUND)) )){
				
				throw new IllegalArgumentException("Detective Missing tickets");
			}
			if(player.colour.isMrX() && (!(player.tickets.containsKey(Ticket.TAXI)) || !(player.tickets.containsKey(Ticket.BUS)) || !(player.tickets.containsKey(Ticket.UNDERGROUND))|| !(player.tickets.containsKey(Ticket.DOUBLE))|| !(player.tickets.containsKey(Ticket.SECRET)))){
				throw new IllegalArgumentException("Mr X Missing tickets");
			}
		}
		
	}



	@Override
	public void registerSpectator(Spectator spectator) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public void unregisterSpectator(Spectator spectator) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public void startRotate() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Collection<Spectator> getSpectators() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public List<Colour> getPlayers() {
		ArrayList<Colour> playerColours= new ArrayList<>();
		playerColours.add(this.firstDetective.colour);
		playerColours.add(this.mrX.colour);
		for (PlayerConfiguration p : this.restOfTheDetectives){
			playerColours.add(p.colour);
		}
		return Collections.unmodifiableList(playerColours);
	}

	@Override
	public Set<Colour> getWinningPlayers() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Optional<Integer> getPlayerLocation(Colour colour) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public boolean isGameOver() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Colour getCurrentPlayer() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public int getCurrentRound() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public List<Boolean> getRounds() {
		return Collections.unmodifiableList(rounds);
	}

	@Override
	public Graph<Integer, Transport> getGraph() {
		ImmutableGraph<Integer, Transport> iGraph = new ImmutableGraph<>(graph);
		return iGraph;
	}
}

