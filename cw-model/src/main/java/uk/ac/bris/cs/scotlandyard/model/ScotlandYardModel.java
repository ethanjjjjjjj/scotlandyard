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
import uk.ac.bris.cs.gamekit.graph.Node;


// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame {
	List<Boolean> rounds;
	Graph<Integer, Transport> graph;
	PlayerConfiguration mrX;
	PlayerConfiguration firstDetective;
	ArrayList<PlayerConfiguration> restOfTheDetectives;
	ArrayList<PlayerConfiguration> players;
	ArrayList<ScotlandYardPlayer> mutablePlayers;
	int roundNumber = 0;

	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
			PlayerConfiguration mrX, PlayerConfiguration firstDetective,
			PlayerConfiguration... restOfTheDetectives) {
		this.rounds = requireNonNull(rounds);
		this.graph = requireNonNull(graph);
		this.mrX = requireNonNull(mrX);
		this.mutablePlayers=new ArrayList<>();
		this.restOfTheDetectives= new ArrayList<>(Arrays.asList(restOfTheDetectives));
		ArrayList<PlayerConfiguration> configurations = new ArrayList<>();
		if (rounds.isEmpty()) {
			throw new IllegalArgumentException("Empty rounds");
		}
		
		if (graph.isEmpty()){
			throw new IllegalArgumentException("Empty graph");
		}

		if (mrX.colour != BLACK) {
			throw new IllegalArgumentException("MrX should be Black");
		}

		//MrX, the first detective, and the rest of the detectives are added to one arraylist so it
		//it easier to iterate over all of them at once.
		
		
		for (PlayerConfiguration configuration : restOfTheDetectives){
			configurations.add(requireNonNull(configuration));
		}
		configurations.add(0, firstDetective);
		configurations.add(0, mrX);
		this.players = configurations;

		
		checkTickets(players);
		checkLocations(players);
		


		for (PlayerConfiguration p : players){
			this.mutablePlayers.add(new ScotlandYardPlayer(p.player, p.colour, p.location, p.tickets));
		}


		
		checkTickets(players);
		checkLocations(players);
		
		
		
	}
	
	//Checking there are not duplicate colours or locations
	public void checkLocations(ArrayList<PlayerConfiguration> configs){
		Set<Integer> setLocation = new HashSet<>();
		Set<Colour> setColour = new HashSet<>();
		for (PlayerConfiguration configuration : configs) {
			if (setLocation.contains(configuration.location)){
				throw new IllegalArgumentException("Duplicate location");
			}
			if (setColour.contains(configuration.colour)){
				throw new IllegalArgumentException("Duplicate colour");
			}
		setLocation.add(configuration.location);
		setColour.add(configuration.colour);
		}
	}

	//Checks whether the detective pr MrX has invalid tickets or missing tickets
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
		for (PlayerConfiguration p : this.players){
			playerColours.add(p.colour);
		}
		return Collections.unmodifiableList(playerColours);
	}

	@Override
	public Set<Colour> getWinningPlayers() {
		/*Currently will just check if anyof the detecives is in the same space as MrX
		if so, then all of the colours of the detectives are returned. If not, the empty set
		is returned. Need to consider case that MrX wins.
		Maybe put code from isGameOver and this into one function as they both use the same code.
		*/
	
		
		ArrayList<Colour> mrXWin= new ArrayList<>();
		mrXWin.add(this.mutablePlayers.get(0).colour());

		ArrayList<Colour> detectiveWin= new ArrayList<>();
		detectiveWin.add(this.firstDetective.colour);
		for(PlayerConfiguration p:this.restOfTheDetectives){
			detectiveWin.add(p.colour);
		}

		//checks whether any of the detectives are in teh same location as mrX 
		
		ScotlandYardPlayer mrX=this.mutablePlayers.get(0);
		for(ScotlandYardPlayer p:this.mutablePlayers.subList(1,this.mutablePlayers.size())){
			if(p.location()==mrX.location())return Set.copyOf(detectiveWin);
		}


		//checks whether all of mr x's possible moves have a detective on the node
		Node<Integer> location = this.graph.getNode(mrX.location());
		Collection<Edge<Integer, Transport>> edgesFrom = this.graph.getEdgesFrom(location);
		ArrayList<Node<Integer>> nodesToCheck=new ArrayList<>();
		for(Edge<Integer, Transport> e:edgesFrom){
			nodesToCheck.add(e.destination());
		}
		ArrayList<Integer> nodeValues=new ArrayList<>();
		for(Node<Integer> n:nodesToCheck){
			nodeValues.add(n.value());
		}

		int samevalues=0;
		for(ScotlandYardPlayer p : this.mutablePlayers.subList(1, this.mutablePlayers.size())){
			if(nodeValues.contains(p.location())){samevalues++;};
		}
		if(samevalues==this.mutablePlayers.size()){
			return Set.copyOf(detectiveWin);
		}
		
		
		
		//checks whether the round limit has been reached
		if(this.roundNumber==22){
			return Set.copyOf(mrXWin);
		}
		


		//checks whether all of the detectives have no possible moves left

		for(ScotlandYardPlayer p: this.mutablePlayers.subList(1, this.mutablePlayers.size())){
			edgesFrom=this.graph.getEdgesFrom(this.graph.getNode(p.location()));
			for(Edge<Integer,Transport> e:edgesFrom){
				if(p.tickets().containsKey(Ticket.fromTransport(e.data()))){
					if(p.tickets().get(Ticket.fromTransport(e.data()))>0){
						return Set.of();
					}
				}
			}
		}

		
		return Set.copyOf(mrXWin);





	}

	@Override
	public Optional<Integer> getPlayerLocation(Colour colour) {
		for (ScotlandYardPlayer p : this.mutablePlayers){
			if (p.colour() == colour){
				return Optional.of(p.location());
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public boolean isGameOver() {
		if(this.getWinningPlayers().isEmpty()){return false;}
		else{return true;}
	}

	@Override
	public Colour getCurrentPlayer() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public int getCurrentRound() {
		return this.roundNumber;
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

