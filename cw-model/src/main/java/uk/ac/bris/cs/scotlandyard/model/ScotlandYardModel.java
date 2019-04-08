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
public class ScotlandYardModel implements ScotlandYardGame, Consumer<Move> {
	private List<Boolean> rounds;
	private Graph<Integer, Transport> graph;
	private PlayerConfiguration mrX;
	private PlayerConfiguration firstDetective;
	private ArrayList<PlayerConfiguration> restOfTheDetectives;
	private ArrayList<ScotlandYardPlayer> mutablePlayers;
	//private int roundNumber = 0;
	private ScotlandYardPlayer currentPlayer;
	private ArrayList<Spectator> spectators;

	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph, PlayerConfiguration mrX,
			PlayerConfiguration firstDetective, PlayerConfiguration... restOfTheDetectives) {
		this.spectators = new ArrayList<>();
		this.rounds = requireNonNull(rounds);
		this.graph = requireNonNull(graph);
		this.mrX = requireNonNull(mrX);
		this.firstDetective = requireNonNull(firstDetective);
		this.mutablePlayers = new ArrayList<>();
		this.restOfTheDetectives = new ArrayList<>(Arrays.asList(restOfTheDetectives));
		ArrayList<PlayerConfiguration> configurations = new ArrayList<>();

		checkForEmpty(this.rounds, this.graph, this.mrX);

		// MrX, the first detective, and the rest of the detectives are added to one
		// arraylist so it
		// it easier to iterate over all of them at once.
		for (PlayerConfiguration configuration : restOfTheDetectives) {
			configurations.add(requireNonNull(configuration));
		}
		configurations.add(0, firstDetective);
		configurations.add(0, mrX);

		// MrX is given it's own mutable class called ScotlandYardMrX. The detectives
		// use ScotlandYardPlayer
		this.mutablePlayers.add(new ScotlandYardMrX(mrX.player, mrX.colour, mrX.location, mrX.tickets));
		for (PlayerConfiguration p : configurations.subList(1, configurations.size())) {
			this.mutablePlayers.add(new ScotlandYardPlayer(p.player, p.colour, p.location, p.tickets));
		}

		checkTickets(configurations);
		checkLocations(configurations);

		this.currentPlayer = mutablePlayers.get(0);
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

	@Override
	public void startRotate() {// TODO
		//Creates a list of valid moves
		Set<Move> moves = validMoves();
		System.out.println("THE CURRENT PLAYER IN START ROTATE  "+this.currentPlayer);
		if (this.currentPlayer instanceof ScotlandYardMrX){
			System.out.println("INSTANCE OF MR X");
		}
		
		this.currentPlayer.makeMove(this, this.currentPlayer.location(), moves, this);	
	}

	@Override
	public Collection<Spectator> getSpectators() {

		return Collections.unmodifiableList(this.spectators);
	}

	@Override
	public List<Colour> getPlayers() {
		ArrayList<Colour> playerColours = new ArrayList<>();
		for (ScotlandYardPlayer p : this.mutablePlayers) {
			playerColours.add(p.colour());
		}
		return Collections.unmodifiableList(playerColours);
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

		ScotlandYardMrX x = (ScotlandYardMrX)this.mutablePlayers.get(0);
		// checks whether the round limit has been reached
		if (x.turnsPlayed() == getRounds().size()) {
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

	@Override
	public Optional<Integer> getPlayerLocation(Colour colour) {
		if (colour != BLACK) {
			for (ScotlandYardPlayer p : this.mutablePlayers) {
				if (p.colour() == colour) {
					return Optional.of(p.location());
				}
			}
		} else {
			ScotlandYardMrX x = (ScotlandYardMrX) this.mutablePlayers.get(0);
			return Optional.of(x.lastSeen());
		}
		return Optional.empty();
	}

	@Override
	public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
		for (ScotlandYardPlayer p : this.mutablePlayers) {
			if (p.colour() == colour) {
				return Optional.of(p.tickets().get(ticket));
			}
		}
		return Optional.empty();
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
		ScotlandYardMrX x = (ScotlandYardMrX)this.mutablePlayers.get(0);
		return x.turnsPlayed();
		//return this.roundNumber;
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

	//When MrX plays, the round increments

	//onRoundStarted must be called when round increments
	private void spectatorMethods(){
		System.out.println("CURRENT ROUND   " +this.getCurrentRound());
		for (Spectator s : this.getSpectators()){
			s.onRoundStarted(this, this.getCurrentRound());
		}
	}

	//onMoveMade must be called after a move
	private void spectatorMethods(Move m){
		for(Spectator s : getSpectators()){
			s.onMoveMade(this,m);
		}
	}
	
	//This changes the current player
	private int nextPlayer(){
		System.out.println("THE CURRENT PLAYER  "+this.currentPlayer);
		int i = this.mutablePlayers.indexOf(this.currentPlayer);
		System.out.println("PLAYER ARRAY I  "+i);
		System.out.println("SIZE OF ARRAY  "+this.mutablePlayers.size());
		//If the current player is the final one, then MrX is the next player
		if (i == this.mutablePlayers.size() - 1){
			this.currentPlayer = this.mutablePlayers.get(0);
			for(Spectator s : getSpectators()){
				s.onRotationComplete(this);
			}
			return -1;
		}
		//Else it's just the next player in the list
		else{
			this.currentPlayer = this.mutablePlayers.get(i+1);
			return 1;
		}
		//System.out.println("THE CURRENT PLAYER  "+this.currentPlayer);
	}

	@Override
	public void accept(Move m) {
		requireNonNull(m);
		
		
		if (m instanceof TicketMove) {
			
			if(this.currentPlayer instanceof ScotlandYardMrX){
				
				ScotlandYardMrX x = (ScotlandYardMrX) this.currentPlayer;
				x.incTurnsPlayed();
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
			this.spectatorMethods(m);
		}

		
	}
}

