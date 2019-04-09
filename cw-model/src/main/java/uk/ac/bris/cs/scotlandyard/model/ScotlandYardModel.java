package uk.ac.bris.cs.scotlandyard.model;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import com.google.common.collect.ImmutableSet;
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
import uk.ac.bris.cs.scotlandyard.model.Player;


// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame, Consumer<Move> {
	private List<Boolean> rounds;
	private Graph<Integer, Transport> graph;
	private PlayerConfiguration mrX;
	private PlayerConfiguration firstDetective;
	private ArrayList<PlayerConfiguration> restOfTheDetectives;
	private ArrayList<ScotlandYardPlayer> mutablePlayers;
	private int roundNumber = 0;
	private ScotlandYardPlayer currentPlayer;
	private ArrayList<Spectator> spectators;
	private int mrXLastSeen = 0;

	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
			PlayerConfiguration mrX, PlayerConfiguration firstDetective,
			PlayerConfiguration... restOfTheDetectives) {
		this.spectators=new ArrayList<>();
		this.rounds = requireNonNull(rounds);
		this.graph = requireNonNull(graph);
		this.mrX = requireNonNull(mrX);
		this.firstDetective = requireNonNull(firstDetective);
		this.mutablePlayers=new ArrayList<>();
		this.restOfTheDetectives= new ArrayList<>(Arrays.asList(restOfTheDetectives));
		ArrayList<PlayerConfiguration> configurations = new ArrayList<>();

		checkForEmpty(this.rounds, this.graph, this.mrX);

		//MrX, the first detective, and the rest of the detectives are added to one arraylist so it
		//it easier to iterate over all of them at once.
		for (PlayerConfiguration configuration : restOfTheDetectives){
			configurations.add(requireNonNull(configuration));
		}
		configurations.add(0, firstDetective);
		configurations.add(0, mrX);

		//MrX is given it's own mutable class called ScotlandYardMrX. The detectives use ScotlandYardPlayer
		
		for (PlayerConfiguration p : configurations){
			this.mutablePlayers.add(new ScotlandYardPlayer(p.player, p.colour, p.location, p.tickets));
		}

		checkTickets(configurations);
		checkLocations(configurations);
		
		this.currentPlayer=mutablePlayers.get(0);
	}

	//Checking attribues are not empty
	public void checkForEmpty(List<Boolean> rounds, Graph<Integer, Transport> graph, 
	PlayerConfiguration mrX){
		if (rounds.isEmpty()) {
			throw new IllegalArgumentException("Empty rounds");
		}
		
		if (graph.isEmpty()){
			throw new IllegalArgumentException("Empty graph");
		}

		if (mrX.colour != BLACK) {
			throw new IllegalArgumentException("MrX should be Black");
		}
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
		
		requireNonNull(spectator);
		this.spectators.add(spectator);
	}

	@Override
	public void unregisterSpectator(Spectator spectator) {
		requireNonNull(spectator);
		this.spectators.remove(spectator);
	}

	private boolean freeSpaceAtNode(Edge<Integer,Transport> e){
		for (ScotlandYardPlayer q : this.mutablePlayers.subList(1,this.mutablePlayers.size())){
			if (q.location() == e.destination().value()){
				return false;	
			}
		}
		return true;
	}

	private Set<Move> allValidMoves(){
		Set<Move> singleMoves = validMoves();
		if (this.getCurrentPlayerObject().isMrX()){
			Set<Move> doubleMoves = doubleValidMoves();
			singleMoves.addAll(doubleMoves);
		}
		return singleMoves;
	}

	private Set<Move> validMoves(){
		Set<Move> moves = new HashSet<>();
		ScotlandYardPlayer p = this.getCurrentPlayerObject();
		for(Edge<Integer,Transport> e:this.graph.getEdgesFrom(this.graph.getNode(p.location()))){
			if(p.hasTickets(Ticket.fromTransport(e.data())) && this.freeSpaceAtNode(e)){
				moves.add(new TicketMove(p.colour(),Ticket.fromTransport(e.data()),e.destination().value()));	
			}

			if (p.isMrX() && p.hasTickets(SECRET) && this.freeSpaceAtNode(e)){
				moves.add(new TicketMove(p.colour(),SECRET,e.destination().value()));
			}
		}
		if (moves.isEmpty()){
			moves.add(new PassMove(p.colour()));
		}
		return moves;
	}

	private Set<Move> doubleValidMoves(){
		Set<Move> moves = new HashSet<>();
		ScotlandYardPlayer p = this.getCurrentPlayerObject();
		if (p.hasTickets(DOUBLE) && this.roundNumber <= this.rounds.size() - 2){
			for(Edge<Integer,Transport> e:this.graph.getEdgesFrom(this.graph.getNode(p.location()))){
				Node<Integer> n = e.destination();
				Ticket t = Ticket.fromTransport(e.data());
				//Checks if MrX has enough tickets to perform a double move with transport tickets
				for (Edge<Integer,Transport> f:this.graph.getEdgesFrom(this.graph.getNode(n.value()))){
					Ticket s = Ticket.fromTransport(f.data());
					if(((t == s && p.hasTickets(t,2)) || (t != s && p.hasTickets(t) && p.hasTickets(s))) && freeSpaceAtNode(e) && freeSpaceAtNode(f)){
						TicketMove ticket1 = new TicketMove(p.colour(),Ticket.fromTransport(e.data()),e.destination().value());
						TicketMove ticket2 = new TicketMove(p.colour(),Ticket.fromTransport(f.data()),f.destination().value());
						moves.add(new DoubleMove(p.colour(), ticket1, ticket2));								
					}
					if(p.hasTickets(SECRET) && freeSpaceAtNode(e) && freeSpaceAtNode(f)){
						TicketMove secret1 = new TicketMove(p.colour(),SECRET,e.destination().value());
						TicketMove secret2 = new TicketMove(p.colour(),SECRET,f.destination().value());
						if (p.hasTickets(SECRET,2)){
							moves.add(new DoubleMove(p.colour(), secret1, secret2));
						}
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

		
	


	private ScotlandYardPlayer getCurrentPlayerObject(){
		for (ScotlandYardPlayer p : this.mutablePlayers){
			if (p.colour() == this.getCurrentPlayer()){
				return p;
			}
		}
		return this.mutablePlayers.get(0);
	}
	@Override
	public void startRotate() {//TODO

		ScotlandYardPlayer p = this.getCurrentPlayerObject();
		Set<Move> moves = new HashSet<>();
		moves = this.allValidMoves();
		p.player().makeMove(this,p.location(),ImmutableSet.copyOf(moves),this);	
	}
		
	

	@Override
	public Collection<Spectator> getSpectators() {
		return this.spectators;
	}

	@Override
	public List<Colour> getPlayers() {
		ArrayList<Colour> playerColours= new ArrayList<>();
		for (ScotlandYardPlayer p : this.mutablePlayers){
			playerColours.add(p.colour());
		}
		return Collections.unmodifiableList(playerColours);
	}

	@Override
	public Set<Colour> getWinningPlayers() {
		ArrayList<Colour> mrXWin= new ArrayList<>();

		mrXWin.add(this.mutablePlayers.get(0).colour());

		ArrayList<Colour> detectiveWin= new ArrayList<>();
		detectiveWin.add(this.firstDetective.colour);
		for(PlayerConfiguration p:this.restOfTheDetectives){
			detectiveWin.add(p.colour);
		}

		//checks whether any of the detectives are in the same location as mrX 
		ScotlandYardPlayer mrX=this.mutablePlayers.get(0);
		for(ScotlandYardPlayer p:this.mutablePlayers.subList(1,this.mutablePlayers.size())){
			if(p.location()==mrX.location()){
				return Set.copyOf(detectiveWin);
			}
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
		if(this.roundNumber==getRounds().size()){
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
		if (colour != BLACK){
			for (ScotlandYardPlayer p : this.mutablePlayers){
				if (p.colour() == colour ){
					return Optional.of(p.location());
				}
			}
		}
		else{
			return Optional.of(this.mrXLastSeen);
		}
		return Optional.empty();
	}

	@Override
	public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
		for (ScotlandYardPlayer p: this.mutablePlayers){
			if (p.colour() == colour){
				return Optional.of(p.tickets().get(ticket)); 
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean isGameOver() {
		if(this.getWinningPlayers().isEmpty()){
			return false;
		}
		else{
			for(Spectator s : this.spectators){
				s.onGameOver(this,this.getWinningPlayers());
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

	@Override
	public void accept(Move t) {
		//Set<Move> m = validMoves();
		//if()
		requireNonNull(t);
		final ScotlandYardPlayer finalCurrentPlayer = this.currentPlayer;
		
		t.visit(new MoveVisitor(){
			@Override
			public void visit(TicketMove m){
				finalCurrentPlayer.location(m.destination());
				finalCurrentPlayer.removeTicket(m.ticket());
				if(finalCurrentPlayer.isMrX()){
					roundNumber++;
				}
				else{
					mutablePlayers.get(0).addTicket(m.ticket());
				}
			}

			@Override
			public void visit(DoubleMove m){
				finalCurrentPlayer.location(m.finalDestination());
				finalCurrentPlayer.removeTicket(m.firstMove().ticket());
				roundNumber++;
				finalCurrentPlayer.removeTicket(m.secondMove().ticket());
				roundNumber++;
				finalCurrentPlayer.removeTicket(DOUBLE);

			}
			@Override
			public void visit(PassMove m){
				System.out.println("Pass move used");
			}
		});
		int i = this.mutablePlayers.indexOf(this.currentPlayer);
		if(i == this.mutablePlayers.size() - 1){
			this.currentPlayer = this.mutablePlayers.get(0);
		}
		else{
			this.currentPlayer = this.mutablePlayers.get(i+1);
		}
		Set<Move> newMoves = this.validMoves();
		if(this.currentPlayer.colour() != BLACK){
			this.currentPlayer.player().makeMove(this,this.currentPlayer.location(),newMoves,this);
		}
	}
}

