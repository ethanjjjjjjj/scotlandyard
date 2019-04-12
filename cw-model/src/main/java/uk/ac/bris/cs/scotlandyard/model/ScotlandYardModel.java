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
	int mrXLastSeen = 0;
	ArrayList<PlayerConfiguration> playerConfigurations;
	boolean oneRevealRound;

	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
			PlayerConfiguration mrX, PlayerConfiguration firstDetective,
			PlayerConfiguration... restOfTheDetectives) {
				this.spectators=new ArrayList<>();

		this.rounds = requireNonNull(rounds);
		this.graph = requireNonNull(graph);
		this.mrX = requireNonNull(mrX);
		this.firstDetective=requireNonNull(firstDetective);
		this.restOfTheDetectives=requireNonNull(restOfTheDetectives);

		ArrayList<PlayerConfiguration> configurations = new ArrayList<>();
		configurations.add(mrX);
		configurations.add(firstDetective);
		for (PlayerConfiguration configuration : restOfTheDetectives){
			configurations.add(requireNonNull(configuration));
		}
		//creates a list of scotlandyardplayer objects that can me modified
		this.mutablePlayers=new ArrayList<>();
		for(PlayerConfiguration p:configurations){
			this.mutablePlayers.add(new ScotlandYardPlayer(p.player, p.colour, p.location, p.tickets));
		}
		this.mrXLastSeen=0;
		this.currentPlayer=this.mutablePlayers.get(0);
		this.playerConfigurations=configurations;
		this.oneRevealRound=false;
		this.checkTickets(configurations);
		this.checkLocations(configurations);
		this.checkForEmpty(this.rounds, this.graph, this.mrX);
	}

	// Checking attribues are not empty
	private void checkForEmpty(List<Boolean> rounds, Graph<Integer, Transport> graph, PlayerConfiguration mrX) {
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
	private void checkLocations(ArrayList<PlayerConfiguration> configs) {
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
	private void checkTickets(ArrayList<PlayerConfiguration> configs) {
		for (PlayerConfiguration p : configs) {
			if (p.tickets.containsKey(DOUBLE) || p.tickets.containsKey(SECRET)) {
				if (p.colour.isDetective()
						&& (p.tickets.get(DOUBLE) != 0 || p.tickets.get(SECRET) != 0)) {
					throw new IllegalArgumentException("Detective has invalid tickets");
				}
			}
			if (p.colour.isDetective()
					&& (!(p.tickets.containsKey(Ticket.TAXI)) || !(p.tickets.containsKey(Ticket.BUS))
							|| !(p.tickets.containsKey(Ticket.UNDERGROUND)))) {

				throw new IllegalArgumentException("Detective Missing tickets");
			}
			if (p.colour.isMrX() && (!(p.tickets.containsKey(Ticket.TAXI))
					|| !(p.tickets.containsKey(Ticket.BUS)) || !(p.tickets.containsKey(Ticket.UNDERGROUND))
					|| !(p.tickets.containsKey(Ticket.DOUBLE)) || !(p.tickets.containsKey(Ticket.SECRET)))) {
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
		if(this.isGameOver()){
			throw new IllegalStateException("game is already over");
		}
		Set<Move> moves = this.allValidMoves(this.currentPlayer);
		this.currentPlayer.makeMove(this, this.currentPlayer.location(), moves, this);	
	}

	@Override
	public Collection<Spectator> getSpectators() {
		return Collections.unmodifiableList(this.spectators);
	}

	private void spectatorsOnGameOver(){
		for(Spectator s:this.spectators){
			s.onGameOver(this,this.getWinningPlayers());
		}
	}

	private void spectatorsOnMoveMade(Move m){
		
	}

	private void spectatorsOnRoundStarted(){
		for(Spectator s:this.spectators){
			s.onRoundStarted(this,this.currentRound);
		}
	}
	private void spectatorsOnRotationComplete(){
		for(Spectator s:this.spectators){
			s.onRotationComplete(this);
		}
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

		if(this.currentPlayer.colour()==BLACK && this.allValidMoves(this.getMutablePlayer(BLACK)).size()==1){
			return Set.copyOf(detectiveWin);
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
		

		if(this.allValidMoves(this.getMutablePlayer(BLACK)).size()==0){
			return Set.copyOf(detectiveWin);
		}

		// checks whether the round limit has been reached
		if ((this.currentPlayer.colour()==BLACK) &&  (getCurrentRound() == getRounds().size())) {
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
		if(!(this.gamehasplayer(colour))){
			return Optional.empty();
		}
		else if(colour==BLACK){
			if(this.currentRound==0){
				return Optional.of(0);
			}
			
			else if(this.rounds.get(currentRound - 1)){
				this.mrXLastSeen=this.getMutablePlayer(colour).location();
				this.oneRevealRound=true;
				return Optional.of(this.mrXLastSeen);
			}
			else{
				if(this.oneRevealRound){
					return Optional.of(mrXLastSeen);
				}
				else{
					return Optional.of(0);
				}
			}
		}
		else{
			return Optional.of(this.getMutablePlayer(colour).location());
		}
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
				//s.onGameOver(this, this.getWinningPlayers());
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
		if(this.mutablePlayers.get((this.mutablePlayers.size())-1)==this.currentPlayer){
			System.out.println("YESS");
			this.currentPlayer=this.mutablePlayers.get(0);
		}
		else{
			this.currentPlayer=this.mutablePlayers.get(this.mutablePlayers.indexOf(this.currentPlayer)+1);
		}
	}


	//The accept method from the consumer
	@Override
	public void accept(Move m) {
		
		requireNonNull(m);
		if(this.allValidMoves(this.currentPlayer).contains(m)){}
		else{throw new IllegalArgumentException();}

		m.visit(new MoveVisitor() {
			@Override
			public void visit(PassMove m) {
				spectatorsOnMoveMade(m);
				nextPlayer();
			}
			@Override
			public void visit(TicketMove m) {
				editPlayerTickets(m);
				spectatorsOnMoveMade(m);
				nextPlayer();
			}
			@Override
			public void visit(DoubleMove m) {

			}
		});
		//isGameOver();
		if(this.isGameOver()){
			this.spectatorsOnGameOver();
		}
		else if(this.currentPlayer.isMrX()){
			this.spectatorsOnRotationComplete();
		}
		
		else if (this.currentPlayer.isDetective()){
			this.startRotate();	
		}
	}

	

	private void editPlayerTickets(TicketMove m){
		if(this.currentPlayer.colour().isMrX()){
			this.currentRound++;
			this.spectatorsOnRoundStarted();
		}
		int newLocation = m.destination();
		Ticket theTicket = m.ticket();
		this.currentPlayer.location(newLocation);
		this.currentPlayer.removeTicket(theTicket);
		if (this.currentPlayer.isDetective()){
			this.mutablePlayers.get(0).addTicket(theTicket);
		}
		if (this.currentPlayer.isMrX()){
			if(this.rounds.get(this.currentRound - 1)){
				this.mrXLastSeen = newLocation;
			}
		}
	}

	//Checks there is no detective at a specified node for generating valid moves
	private boolean freeSpaceAtNode(Edge<Integer,Transport> e){
		for (ScotlandYardPlayer q : this.mutablePlayers.subList(1,this.mutablePlayers.size())){
			if (q.location() == e.destination().value()){
				return false;	
			}
		}
		return true;
	}

	//Calls the other move generating methods and puts them in one set
	private Set<Move> allValidMoves(ScotlandYardPlayer p){
		Set<Move> singleMoves = this.validMoves(p);
		//Only MrX can have double and secret moves;
		if (p.isMrX()){
			Set<Move> doubleMoves = this.doubleValidMoves(p);
			singleMoves.addAll(doubleMoves);
		}
		return singleMoves;
	}

	//Generates a set of valid moves for all players.
	private Set<Move> validMoves(ScotlandYardPlayer p){
		Set<Move> moves = new HashSet<>();
		//ScotlandYardPlayer p = this.getMutablePlayer(this.getCurrentPlayer());
		for(Edge<Integer,Transport> e:this.graph.getEdgesFrom(this.graph.getNode(p.location()))){
			if(p.hasTickets(Ticket.fromTransport(e.data())) && this.freeSpaceAtNode(e)){
				moves.add(new TicketMove(p.colour(),Ticket.fromTransport(e.data()),e.destination().value()));	
			}
			//Makes the single secret moves for MrX
			if (p.isMrX() && p.hasTickets(SECRET) && this.freeSpaceAtNode(e)){
				moves.add(new TicketMove(p.colour(),SECRET,e.destination().value()));
			}
		}
		//If the detectives have no valid moves, they have to pass
		if (moves.isEmpty()){
			moves.add(new PassMove(p.colour()));
		}
		return moves;
	}

	//Generates double moves for MrX, a long with secret variations
	private Set<Move> doubleValidMoves(ScotlandYardPlayer p){
		Set<Move> moves = new HashSet<>();
		//ScotlandYardPlayer p = this.getMutablePlayer(this.getCurrentPlayer());
		if (p.hasTickets(DOUBLE) && this.currentRound <= this.rounds.size() - 2){
			for(Edge<Integer,Transport> e:this.graph.getEdgesFrom(this.graph.getNode(p.location()))){
				Node<Integer> n = e.destination();
				Ticket t = Ticket.fromTransport(e.data());
				//Checks if MrX has enough tickets to perform a double move with transport tickets
				for (Edge<Integer,Transport> f:this.graph.getEdgesFrom(this.graph.getNode(n.value()))){
					Ticket s = Ticket.fromTransport(f.data());
					//MrX must have 2 tranport tickets of the same type, or at least one of each for the transport. And the node must be free.
					if(((t == s && p.hasTickets(t,2)) || (t != s && p.hasTickets(t) && p.hasTickets(s))) && freeSpaceAtNode(e) && freeSpaceAtNode(f)){
						TicketMove ticket1 = new TicketMove(p.colour(),Ticket.fromTransport(e.data()),e.destination().value());
						TicketMove ticket2 = new TicketMove(p.colour(),Ticket.fromTransport(f.data()),f.destination().value());
						moves.add(new DoubleMove(p.colour(), ticket1, ticket2));								
					}
					//Secret variations genearted here
					if(p.hasTickets(SECRET) && freeSpaceAtNode(e) && freeSpaceAtNode(f)){
						TicketMove secret1 = new TicketMove(p.colour(),SECRET,e.destination().value());
						TicketMove secret2 = new TicketMove(p.colour(),SECRET,f.destination().value());
						//If MrX has 2 secret tickets they can do a double secret move
						if (p.hasTickets(SECRET,2)){
							moves.add(new DoubleMove(p.colour(), secret1, secret2));
						}
						//Secret moves for just one half of the double move
						TicketMove ticket1 = new TicketMove(p.colour(),Ticket.fromTransport(e.data()),e.destination().value());
						TicketMove ticket2 = new TicketMove(p.colour(),Ticket.fromTransport(f.data()),f.destination().value());
						moves.add(new DoubleMove(p.colour(), ticket1, secret2));
						moves.add(new DoubleMove(p.colour(), secret1, ticket2));
					}
				}
			}			
		}
		return moves;
	}
}


