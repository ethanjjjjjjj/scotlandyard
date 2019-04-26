package uk.ac.bris.cs.scotlandyard.model;

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
import uk.ac.bris.cs.gamekit.graph.Node;
public class ScotlandYardModel implements ScotlandYardGame,Consumer<Move> {
	private List<Boolean> rounds;
	private Graph<Integer, Transport> graph;
	private PlayerConfiguration mrX;
	private PlayerConfiguration firstDetective;
	private PlayerConfiguration[] restOfTheDetectives;
	private ArrayList<Spectator> spectators;
	private int currentRound;
	private ArrayList<ScotlandYardPlayer> mutablePlayers;
	private ScotlandYardPlayer currentPlayer;
	private int mrXLastSeen = 0;
	private ArrayList<PlayerConfiguration> playerConfigurations;


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
		this.currentPlayer=this.mutablePlayers.get(0);
		this.playerConfigurations=configurations;
		this.checkTickets(configurations);
		this.checkLocations(configurations);
		this.checkForEmpty(this.rounds, this.graph, this.mrX);
	}
	// Checking attribues are not empty
	private void checkForEmpty(List<Boolean> rounds, Graph<Integer, Transport> graph, PlayerConfiguration mrX) {
		if (rounds.isEmpty()) throw new IllegalArgumentException("Empty rounds");
		if (graph.isEmpty()) throw new IllegalArgumentException("Empty graph");
		if (mrX.colour != BLACK) throw new IllegalArgumentException("MrX should be Black");
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
		if(this.isGameOver()) throw new IllegalStateException("game is already over");
		Set<Move> moves = this.allValidMoves(this.currentPlayer);
		this.currentPlayer.makeMove(this, this.currentPlayer.location(), moves, this);	
	}

	@Override
	public Collection<Spectator> getSpectators() {
		return Collections.unmodifiableList(this.spectators);
	}

	public void spectatorsOnGameOver(){
		for(Spectator s:this.spectators){
			s.onGameOver(this,this.getWinningPlayers());
		}
	}

	public void spectatorsOnMoveMade(Move m, ScotlandYardPlayer p){
		//The spectators must see the double move ticket
		if (m instanceof DoubleMove){
			DoubleMove n = (DoubleMove)m;
			int destinationOne,destinationTwo;
			//If it is a reveal round, the first ticket shows the location
			if(this.rounds.get(this.currentRound)) destinationOne = n.firstMove().destination();
			//Otherwise it's MrX's last seen
			else destinationOne = this.mrXLastSeen;	
			//Same thing for second ticket and next round, as a double move takes 2 rounds
			if(this.rounds.get(this.currentRound+1)){
				destinationTwo = n.secondMove().destination();
			}
			else destinationTwo = destinationOne;
			DoubleMove d = new DoubleMove(BLACK,n.firstMove().ticket(),destinationOne,n.secondMove().ticket(),destinationTwo);
			for (Spectator s : this.spectators){
				s.onMoveMade(this, d);
			}
			return;
		}
		if (p.isMrX()){
			TicketMove t;
			TicketMove n = (TicketMove)m;
			if (this.rounds.get(this.currentRound - 1)) t = new TicketMove(BLACK,n.ticket(),n.destination());
			else t = new TicketMove(BLACK,n.ticket(),this.mrXLastSeen);
			for (Spectator s : this.spectators){
				s.onMoveMade(this, t);
			}
		}
		else{
			for (Spectator s : this.spectators){
				s.onMoveMade(this, m);
			}
		}
	}

	public void spectatorsOnRoundStarted(){
		for(Spectator s:this.spectators){
			s.onRoundStarted(this,this.currentRound);
		}
	}

	public void spectatorsOnRotationComplete(){
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

	private PlayerConfiguration getplayerConfiguration(Colour colour){
		for(PlayerConfiguration item : this.playerConfigurations){
			if(item.colour==colour)return item;
		}
		throw new NullPointerException();
	}

	private Boolean gamehasplayer(Colour colour){
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
		//MrX must return 0 when game has not started
		else if(colour==BLACK){
			if(this.currentRound==0){
				return Optional.of(0);}
			//It should always return mrXLastSeen as it will be edited in the accept method
			else{
				return Optional.of(this.mrXLastSeen);
			}
		}
		//Detectives just have their normal location returned
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
		if (this.getWinningPlayers().isEmpty()) return false;
		else return true;
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

	//Changes the current player of the model
	public void nextPlayer(){
		if(this.mutablePlayers.get((this.mutablePlayers.size())-1)==this.currentPlayer){
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
		if(!this.allValidMoves(this.currentPlayer).contains(m))throw new IllegalArgumentException();
		else{
			Visitor visitor = new Visitor(this);
			m.visit(visitor);
			if (this.isGameOver()) this.spectatorsOnGameOver();
			else if (this.currentPlayer.isMrX()) this.spectatorsOnRotationComplete();
			else if (this.currentPlayer.isDetective()) this.startRotate();
		}
	}


	public void editMrXTicketsForDoubleMove(TicketMove m){
		this.currentRound++;
		int newLocation = m.destination();
		Ticket theTicket = m.ticket();
		this.mutablePlayers.get(0).location(newLocation);
		this.mutablePlayers.get(0).removeTicket(theTicket);
		
		if (this.rounds.get(this.currentRound - 1))	this.mrXLastSeen = newLocation;
		this.spectatorsOnRoundStarted();
	}
	
	public void prepareNextRound(TicketMove m, ScotlandYardPlayer p){
		if(p.isMrX()){
			this.currentRound++;
			this.editPlayerTickets(m, p);
			this.spectatorsOnRoundStarted();
		}
		else{
			this.editPlayerTickets(m,p);
		}
	}

	public void editPlayerTickets(TicketMove m, ScotlandYardPlayer p){
		int newLocation = m.destination();
		Ticket theTicket = m.ticket();
		p.location(newLocation);
		p.removeTicket(theTicket);
		if (p.isDetective()){
			this.mutablePlayers.get(0).addTicket(theTicket);
		}
		if (p.isMrX() && this.rounds.get(this.currentRound - 1)){
			this.mrXLastSeen = newLocation;
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
		if (p.hasTickets(DOUBLE) && this.currentRound <= (this.rounds.size() - 2)){
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