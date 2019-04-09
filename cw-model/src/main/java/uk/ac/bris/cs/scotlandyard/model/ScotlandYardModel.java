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
import java.util.function.Consumer;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;

// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame,Consumer<Move> {
	List<Boolean> rounds;
	Graph<Integer, Transport> graph;
	PlayerConfiguration mrX;
	PlayerConfiguration firstDetective;
	PlayerConfiguration[] restOfTheDetectives;
	ArrayList<Spectator> spectators;
	int currentRound;
	ArrayList<ScotlandYardPlayer> mutablePlayers;
	ScotlandYardPlayer currentPlayer;
	int mrXLastSeen;
	ArrayList<PlayerConfiguration> playerConfigurations;


	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
			PlayerConfiguration mrX, PlayerConfiguration firstDetective,
			PlayerConfiguration... restOfTheDetectives) {
				
		this.rounds = requireNonNull(rounds);
		this.graph = requireNonNull(graph);
		this.mrX = requireNonNull(mrX);
		
		this.firstDetective=requireNonNull(firstDetective);
		this.restOfTheDetectives=requireNonNull(restOfTheDetectives);
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

		configurations.add(mrX);
		configurations.add(firstDetective);
		

		for (PlayerConfiguration configuration : restOfTheDetectives){
			configurations.add(requireNonNull(configuration));
		}
		Set<Integer> set = new HashSet<>();//Checking there are not duplicate colours or locations
		Set<Colour> setColour = new HashSet<>();
		for (PlayerConfiguration configuration : configurations) {
			if (set.contains(configuration.location)){
				throw new IllegalArgumentException("Duplicate location");
			}
			if (setColour.contains(configuration.colour)){
				throw new IllegalArgumentException("Duplicate colour");
			}
		set.add(configuration.location);
		setColour.add(configuration.colour);
		}	




		//creates a list of scotlandyardplayer objects that can me modified
		this.mutablePlayers=new ArrayList<>();
		for(PlayerConfiguration p:configurations){
			this.mutablePlayers.add(new ScotlandYardPlayer(p.player, p.colour, p.location, p.tickets));
		}
		this.mrXLastSeen=this.mutablePlayers.get(0).location();
		this.currentPlayer=this.mutablePlayers.get(0);
		this.playerConfigurations=configurations;
		checkTickets(configurations);
		checkLocations(configurations);
		checkForEmpty(this.rounds, this.graph, this.mrX);

	}



	// Checking attribues are not empty
	public void checkForEmpty(List<Boolean> rounds, Graph<Integer, Transport> graph, PlayerConfiguration mrX) {
		if (rounds.isEmpty()) {
			throw new IllegalArgumentException("Empty rounds");
		}

		if (graph.isEmpty()) {
			throw new IllegalArgumentException("Empty graph");
		}

		if (mrX.colour != BLACK) {
			throw new IllegalArgumentException("MrX should be Black");
		}
	}




	// Checking there are not duplicate colours or locations
	public void checkLocations(ArrayList<PlayerConfiguration> configs) {
		Set<Integer> setLocation = new HashSet<>();
		Set<Colour> setColour = new HashSet<>();
		for (PlayerConfiguration configuration : configs) {
			if (setLocation.contains(configuration.location)) {
				throw new IllegalArgumentException("Duplicate location");
			}
			if (setColour.contains(configuration.colour)) {
				throw new IllegalArgumentException("Duplicate colour");
			}
			setLocation.add(configuration.location);
			setColour.add(configuration.colour);
		}
	}

	// Checks whether the detective pr MrX has invalid tickets or missing tickets
	public void checkTickets(ArrayList<PlayerConfiguration> configs) {
		for (PlayerConfiguration player : configs) {
			if (player.tickets.containsKey(DOUBLE) || player.tickets.containsKey(SECRET)) {
				if (player.colour.isDetective()
						&& (player.tickets.get(DOUBLE) != 0 || player.tickets.get(SECRET) != 0)) {
					throw new IllegalArgumentException("Detective has invalid tickets");
				}
			}
			if (player.colour.isDetective()
					&& (!(player.tickets.containsKey(Ticket.TAXI)) || !(player.tickets.containsKey(Ticket.BUS))
							|| !(player.tickets.containsKey(Ticket.UNDERGROUND)))) {

				throw new IllegalArgumentException("Detective Missing tickets");
			}
			if (player.colour.isMrX() && (!(player.tickets.containsKey(Ticket.TAXI))
					|| !(player.tickets.containsKey(Ticket.BUS)) || !(player.tickets.containsKey(Ticket.UNDERGROUND))
					|| !(player.tickets.containsKey(Ticket.DOUBLE)) || !(player.tickets.containsKey(Ticket.SECRET)))) {
				throw new IllegalArgumentException("Mr X Missing tickets");
			}
		}
	}


	@Override
	public void registerSpectator(Spectator spectator) {
		requireNonNull(spectator);
		if (this.spectators.contains(spectator)) {
			throw new IllegalArgumentException("same spectator added twice");
		}
		this.spectators.add(spectator);
	}

	@Override
	public void unregisterSpectator(Spectator spectator) {
		requireNonNull(spectator);
		if (!this.spectators.contains(spectator)) {
			throw new IllegalArgumentException("spectator not registered");
		}
		this.spectators.remove(spectator);
	}

	@Override
	public void startRotate() {
		Set<Move> moves = validMoves();
		this.currentPlayer.makeMove(this, this.currentPlayer.location(), moves, this);	

	}

	@Override
	public Collection<Spectator> getSpectators() {
		return Collections.unmodifiableList(this.spectators);
	}

	@Override
	public List<Colour> getPlayers() {
		
		ArrayList<Colour> players= new ArrayList<>();
		players.add(this.mrX.colour);
		players.add(this.firstDetective.colour);
		for(PlayerConfiguration item : restOfTheDetectives){
			players.add(item.colour);
		}
		return Collections.unmodifiableList(players); 
	}

	@Override
	public Set<Colour> getWinningPlayers() {
		ArrayList<Colour> mrXWin = new ArrayList<>();

		mrXWin.add(this.mutablePlayers.get(0).colour());

		ArrayList<Colour> detectiveWin = new ArrayList<>();
		detectiveWin.add(this.firstDetective.colour);
		for (PlayerConfiguration p : this.restOfTheDetectives) {
			detectiveWin.add(p.colour);
		}

		// checks whether any of the detectives are in the same location as mrX
		ScotlandYardPlayer mrX = this.mutablePlayers.get(0);
		for (ScotlandYardPlayer p : this.mutablePlayers.subList(1, this.mutablePlayers.size())) {
			if (p.location() == mrX.location()) {
				return Set.copyOf(detectiveWin);
			}
		}

		// checks whether all of mr x's possible moves have a detective on the node
		Node<Integer> location = this.graph.getNode(mrX.location());
		Collection<Edge<Integer, Transport>> edgesFrom = this.graph.getEdgesFrom(location);
		ArrayList<Node<Integer>> nodesToCheck = new ArrayList<>();
		for (Edge<Integer, Transport> e : edgesFrom) {
			nodesToCheck.add(e.destination());
		}
		ArrayList<Integer> nodeValues = new ArrayList<>();
		for (Node<Integer> n : nodesToCheck) {
			nodeValues.add(n.value());
		}

		int samevalues = 0;
		for (ScotlandYardPlayer p : this.mutablePlayers.subList(1, this.mutablePlayers.size())) {
			if (nodeValues.contains(p.location())) {
				samevalues++;
			}
			;
		}
		if (samevalues == this.mutablePlayers.size()) {
			return Set.copyOf(detectiveWin);
		}

		//ScotlandYardMrX x = (ScotlandYardMrX)this.mutablePlayers.get(0);
		// checks whether the round limit has been reached
		if (getCurrentRound() == getRounds().size()) {
			return Set.copyOf(mrXWin);
		}

		// checks whether all of the detectives have no possible moves left
		for (ScotlandYardPlayer p : this.mutablePlayers.subList(1, this.mutablePlayers.size())) {
			edgesFrom = this.graph.getEdgesFrom(this.graph.getNode(p.location()));
			for (Edge<Integer, Transport> e : edgesFrom) {
				if (p.tickets().containsKey(Ticket.fromTransport(e.data()))) {
					if (p.tickets().get(Ticket.fromTransport(e.data())) > 0) {
						return Set.of();
					}
				}
			}
		}

		return Set.copyOf(mrXWin);

	}

	ScotlandYardPlayer getMutablePlayer(Colour colour){
		for(ScotlandYardPlayer item : this.mutablePlayers){
			if(item.colour()==colour)return item;
		}
		throw new NullPointerException();
	}
	PlayerConfiguration getplayerConfiguration(Colour colour){
		for(PlayerConfiguration item : this.playerConfigurations){
			if(item.colour==colour)return item;
		}
		throw new NullPointerException();
	}
	

	Boolean gamehasplayer(Colour colour){
		for(ScotlandYardPlayer p: this.mutablePlayers){
			if(p.colour()==colour)return true;
		}
		return false;
	}

	@Override
	public Optional<Integer> getPlayerLocation(Colour colour) {
		if(gamehasplayer(colour)){
		if (colour != BLACK) {

					return Optional.of(this.getMutablePlayer(colour).location());

		} 
		else{
			for(int i=0;i<=this.getCurrentRound();i++){
				if(this.rounds.get(i)){return Optional.of(this.mrXLastSeen);}
			}
			return Optional.of(0);
		}}
		else return Optional.empty();
	}

	@Override
	public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
		if(gamehasplayer(colour)){
			return Optional.of(this.getMutablePlayer(colour).tickets().get(ticket));
		}
		else return Optional.empty();
	}

	@Override
	public boolean isGameOver() {
		if (this.getWinningPlayers().isEmpty()) {

			return false;
		} else {
			for (Spectator s : this.spectators) {
				s.onGameOver(this, this.getWinningPlayers());
			}

			return true;
		}
	}

	@Override
	public Colour getCurrentPlayer() {
		return this.currentPlayer.colour();
	}

	@Override
	public int getCurrentRound() {
		return this.currentRound;
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


	void nextPlayer(){


		if(this.mutablePlayers.get(this.mutablePlayers.size()-1)==this.currentPlayer){
			this.currentPlayer=this.mutablePlayers.get(0);
		}
		else{
			this.currentPlayer=this.mutablePlayers.get(this.mutablePlayers.indexOf(this.currentPlayer)+1);
		}


	}




	@Override
	public void accept(Move m) {


		requireNonNull(m);
		
		
		if (m instanceof TicketMove) {
			
			if(this.currentPlayer.colour()==BLACK){
				
				this.currentRound++;
			}

			else{this.nextPlayer();}
			TicketMove n = (TicketMove) m;
			int newLocation = n.destination();
			Ticket theTicket = n.ticket();
			this.currentPlayer.location(newLocation);
			this.currentPlayer.removeTicket(theTicket);
			
			
		}
		// Remeber ticket has to be given to MrX
		else if (m instanceof PassMove) {
			this.nextPlayer();
			

		} else if (m instanceof DoubleMove) {
			
			DoubleMove n = (DoubleMove) m;
			Ticket ticket1 = n.firstMove().ticket();
			Ticket ticket2 = n.secondMove().ticket();
			int newLocation = n.finalDestination();
			
			
			this.currentPlayer.removeTicket(ticket1);
			//this.roundIncrementer(m);
			this.currentPlayer.removeTicket(ticket2);
			//this.roundIncrementer(m);
			this.currentPlayer.removeTicket(Ticket.DOUBLE);
			this.currentPlayer.location(newLocation);
			this.nextPlayer();
			//this.spectatorMethods(m);
		}

		








	}


	private Set<Move> validMoves(){
		Set<Move> moves = new HashSet<>();
		ScotlandYardPlayer p = this.currentPlayer;
		for (Edge<Integer, Transport> e : this.graph.getEdgesFrom(this.graph.getNode(p.location()))) {
			if (p.hasTickets(Ticket.fromTransport(e.data()))) {
				moves.add(new TicketMove(p.colour(), Ticket.fromTransport(e.data()), e.destination().value()));
			}
		}
		return moves;
	}




}


