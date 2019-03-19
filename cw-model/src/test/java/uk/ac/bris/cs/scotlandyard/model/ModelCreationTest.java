package uk.ac.bris.cs.scotlandyard.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.gamekit.graph.UndirectedGraph;
import uk.ac.bris.cs.scotlandyard.harness.TestHarness;
import uk.ac.bris.cs.scotlandyard.model.PlayerConfiguration.Builder;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.ac.bris.cs.scotlandyard.auxiliary.TestGames.assertTicketCount;
import static uk.ac.bris.cs.scotlandyard.auxiliary.TestGames.assertTicketsForAllTicketTypeSuchThat;
import static uk.ac.bris.cs.scotlandyard.auxiliary.TestGames.doNothingBlue;
import static uk.ac.bris.cs.scotlandyard.auxiliary.TestGames.doNothingGreen;
import static uk.ac.bris.cs.scotlandyard.auxiliary.TestGames.doNothingMrX;
import static uk.ac.bris.cs.scotlandyard.auxiliary.TestGames.doNothingPlayer;
import static uk.ac.bris.cs.scotlandyard.auxiliary.TestGames.doNothingRed;
import static uk.ac.bris.cs.scotlandyard.auxiliary.TestGames.doNothingWhite;
import static uk.ac.bris.cs.scotlandyard.auxiliary.TestGames.doNothingYellow;
import static uk.ac.bris.cs.scotlandyard.auxiliary.TestGames.dummyPlayer;
import static uk.ac.bris.cs.scotlandyard.auxiliary.TestGames.makeTickets;
import static uk.ac.bris.cs.scotlandyard.auxiliary.TestGames.rounds;
import static uk.ac.bris.cs.scotlandyard.model.Colour.BLACK;
import static uk.ac.bris.cs.scotlandyard.model.Colour.BLUE;
import static uk.ac.bris.cs.scotlandyard.model.Colour.GREEN;
import static uk.ac.bris.cs.scotlandyard.model.Colour.RED;
import static uk.ac.bris.cs.scotlandyard.model.Colour.WHITE;
import static uk.ac.bris.cs.scotlandyard.model.Colour.YELLOW;

/**
 * Tests model initialisation and constructor
 * <p>
 * Note that exception message testing is intentionally left out as throwing the
 * correct exception type is enough to proof the given model is designed to spec
 */

public class  ModelCreationTest extends ParameterisedModelTestBase {

	private TestHarness harness;
	@Before public void initialise() { harness = new TestHarness(); }
	@After public void tearDown() { harness.forceReleaseShutdownLock(); }

	@Test
	public void testNullMrXShouldThrow() {//COMPLETED
		assertThatThrownBy(() -> createGame(null, doNothingPlayer(RED, 1)))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	public void testNullDetectiveShouldThrow() {//COMPLETED
		assertThatThrownBy(() -> createGame(doNothingPlayer(BLACK, 1), null))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	public void testAnyNullDetectiveShouldThrow() {//COMPLETED
		assertThatThrownBy(() -> createGame(
				doNothingPlayer(BLACK, 1),
				doNothingPlayer(BLUE, 2),
				(PlayerConfiguration) null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	public void testNoMrXShouldThrow() { //COMPLETED
		assertThatThrownBy(() -> createGame(
				doNothingPlayer(BLUE, 1),
				doNothingPlayer(RED, 2))).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testMoreThanOneMrXShouldThrow() {//COMPLETED
		assertThatThrownBy(() -> createGame(
				doNothingPlayer(BLACK, 1),
				doNothingPlayer(BLACK, 2))).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testSwappedMrXShouldThrow() { //COMPLETED
		assertThatThrownBy(() -> createGame(
				doNothingPlayer(BLUE, 1),
				doNothingPlayer(BLACK, 2))).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testDuplicateDetectivesShouldThrow() {//COMPLETED
		assertThatThrownBy(() -> createGame(
				doNothingPlayer(BLACK, 1),
				doNothingPlayer(BLUE, 2),
				doNothingPlayer(BLUE, 2))).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testLocationOverlapBetweenMrXAndDetectiveShouldThrow() { //COMPLETED
		assertThatThrownBy(() -> createGame(
				doNothingPlayer(BLACK, 1),
				doNothingPlayer(BLUE, 1))).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testLocationOverlapBetweenDetectivesShouldThrow() {//COMPLETED
		assertThatThrownBy(() -> createGame(
				doNothingPlayer(BLACK, 1),
				doNothingPlayer(BLUE, 2),
				doNothingPlayer(GREEN, 2))).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testDetectiveHaveSecretTicketShouldThrow() {//COMPLETED
		PlayerConfiguration blue = new Builder(BLUE).using(dummyPlayer())
				.with(makeTickets(1, 1, 1, 0, 1))
				.at(2).build();
		assertThatThrownBy(() -> createGame(doNothingMrX(), blue))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testDetectiveHaveDoubleTicketShouldThrow() {//COMPLETED
		PlayerConfiguration blue = new Builder(BLUE).using(dummyPlayer())
				.with(makeTickets(1, 1, 0, 1, 0))
				.at(2).build();
		assertThatThrownBy(() -> createGame(doNothingMrX(), blue))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testDetectiveMissingAnyTicketsShouldThrow() {//COMPLETED
		Map<Ticket, Integer> tickets = new HashMap<>();
		tickets.put(Ticket.TAXI, 1);
		PlayerConfiguration blue = new Builder(BLUE).using(dummyPlayer())
				.with(tickets).at(2).build();
		assertThatThrownBy(() -> createGame(doNothingMrX(), blue))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testMrXMissingAnyTicketsShouldThrow() {//COMPLETED
		Map<Ticket, Integer> tickets = new HashMap<>();
		tickets.put(Ticket.SECRET, 1);
		tickets.put(Ticket.DOUBLE, 1);
		PlayerConfiguration black = new Builder(BLACK).using(dummyPlayer())
				.with(tickets).at(2).build();
		assertThatThrownBy(() -> createGame(black, doNothingBlue()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testEmptyRoundsShouldThrow() {//COMPLETED
		assertThatThrownBy(() -> createGame(
				emptyList(),
				defaultGraph(),
				doNothingPlayer(BLACK, 1),
				doNothingPlayer(BLUE, 1))).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testNullRoundsShouldThrow() {//COMPLETED
		assertThatThrownBy(() -> createGame(
				null,
				defaultGraph(),
				doNothingPlayer(BLACK, 1),
				doNothingPlayer(BLUE, 1))).isInstanceOf(NullPointerException.class);
	}

	@Test
	public void testEmptyMapShouldThrow() {//COMPLETED
		assertThatThrownBy(() -> createGame(
				emptyList(),
				new UndirectedGraph<>(),
				doNothingPlayer(BLACK, 1),
				doNothingPlayer(BLUE, 1))).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testNullMapShouldThrow() {//COMPLETED
		assertThatThrownBy(() -> createGame(
				rounds(true),
				(Graph<Integer, Transport>) null,
				doNothingPlayer(BLACK, 1),
				doNothingPlayer(BLUE, 1))).isInstanceOf(NullPointerException.class);
	}

	@Test
	public void testTwoPlayerDoesNotThrow() {//COMPLETED
		createGame(
				doNothingMrX(),
				doNothingBlue());
	}

	@Test
	public void testSixPlayerDoesNotThrow() {//COMPLETED
		createGame(
				doNothingMrX(),
				doNothingRed(),
				doNothingGreen(),
				doNothingBlue(),
				doNothingWhite(),
				doNothingYellow());
	}

	@Test
	public void testGetRoundsMatchesSupplied() { //COMPLETED
		ScotlandYardGame game = createGame(
				asList(true, false, true, true),
				defaultGraph(),
				doNothingMrX(),
				doNothingBlue());
		assertThat(game.getRounds()).containsExactly(true, false, true, true);
	}

	@Test
	public void testGetRoundsIsImmutable() { //COMPLETED
		ScotlandYardGame game = createGame(
				new ArrayList<>(asList(true, false)),
				defaultGraph(),
				doNothingMrX(),
				doNothingBlue());
		assertThatThrownBy(() -> game.getRounds().add(true))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	public void testGetGraphMatchesSupplied() { //COMPLETED
		ScotlandYardGame game = createGame(
				asList(true, false),
				defaultGraph(),
				doNothingMrX(),
				doNothingBlue());
		assertThat(game.getGraph()).isEqualTo(defaultGraph());
	}

	@Test
	public void testGetGraphIsImmutable() { //COMPLETED
		ScotlandYardGame game = createGame(
				asList(true, false),
				new UndirectedGraph<>(defaultGraph()),
				doNothingMrX(),
				doNothingBlue());
		assertThatThrownBy(() -> game.getGraph().addNode(new Node<>(500)))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	public void testGetPlayersMatchesSupplied() {//COMPLETED
		ScotlandYardGame game = createGame(
				asList(true, false, true, false),
				defaultGraph(),
				doNothingMrX(),
				doNothingRed(),
				doNothingGreen(),
				doNothingBlue());
		assertThat(game.getPlayers()).containsExactly(BLACK, RED, GREEN, BLUE);
	}

	@Test
	public void testGetPlayersIsImmutable() {//COMPLETED
		assertThatThrownBy(() -> createValidSixPlayerGame().getPlayers().add(RED))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	public void testGameIsNotOverInitially() { //COMPLETED
		assertThat(createValidSixPlayerGame().isGameOver()).isFalse();
	}

	@Test
	public void testWinningPlayerIsEmptyInitially() { //COMPLETED
		assertThat(createValidSixPlayerGame().getWinningPlayers()).isEmpty();
	}

	@Test
	public void testWinningPlayerIsImmutable() { //COMPLETED
		assertThatThrownBy(() -> createValidSixPlayerGame().getWinningPlayers().add(BLACK))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	public void testGetRoundIsNOT_STARTEDInitially() {//COMPLETED
		assertThat(createValidSixPlayerGame().getCurrentRound()).isEqualTo(ScotlandYardGame
				.NOT_STARTED);
	}

	@Test
	public void testGetPlayerIsMrXInitially() {
		assertThat(createValidSixPlayerGame().getCurrentPlayer()).isEqualTo(BLACK);
	}

	@Test
	public void testGetPlayersStartsWithBlack() { //COMPLETED
		ScotlandYardGame game = createGame(
				asList(true, false, true, false),
				defaultGraph(),
				doNothingMrX(),
				doNothingRed(),
				doNothingGreen(),
				doNothingBlue());
		assertThat(game.getPlayers()).startsWith(BLACK);
	}

	@Test
	public void testGetDetectiveLocationMatchesSupplied() {//COMPLETED
		ScotlandYardGame game = createGame(
				asList(false, false, false),
				defaultGraph(),
				doNothingPlayer(BLACK, 35),
				doNothingPlayer(RED, 26),
				doNothingPlayer(BLUE, 50),
				doNothingPlayer(GREEN, 94),
				doNothingPlayer(WHITE, 155),
				doNothingPlayer(YELLOW, 174));
		assertThat(game.getPlayerLocation(RED)).hasValue(26);
		assertThat(game.getPlayerLocation(BLUE)).hasValue(50);
		assertThat(game.getPlayerLocation(GREEN)).hasValue(94);
		assertThat(game.getPlayerLocation(WHITE)).hasValue(155);
		assertThat(game.getPlayerLocation(YELLOW)).hasValue(174);
	}


	@Test
	public void testGetPlayerLocationConcealsMrXLocationInitially() {
		ScotlandYardGame game = createGame(
				asList(false, false, false),
				defaultGraph(),
				doNothingPlayer(BLACK, 35),
				doNothingPlayer(RED, 26));
		assertThat(game.getPlayerLocation(BLACK)).hasValue(0);
	}

	@Test
	public void testGetPlayerLocationForNonExistentPlayerIsEmpty() {
		ScotlandYardGame game = createGame(
				asList(false, false, false),
				defaultGraph(),
				doNothingPlayer(BLACK, 35),
				doNothingPlayer(RED, 26));
		assertThat(game.getPlayerLocation(BLACK)).isNotEmpty();
		assertThat(game.getPlayerLocation(RED)).isNotEmpty();
		assertThat(game.getPlayerLocation(BLUE)).isEmpty();
		assertThat(game.getPlayerLocation(GREEN)).isEmpty();
		assertThat(game.getPlayerLocation(WHITE)).isEmpty();
		assertThat(game.getPlayerLocation(YELLOW)).isEmpty();
	}

	@Test
	public void testGetPlayerTicketsMatchesSupplied() { //COMPLETED
		PlayerConfiguration mrX = new Builder(BLACK).using(dummyPlayer())
				.with(makeTickets(1, 2, 3, 4, 5)).at(1).build();

		PlayerConfiguration blue = new Builder(BLUE).using(dummyPlayer())
				.with(makeTickets(5, 4, 3, 0, 0)).at(2).build();
		ScotlandYardGame game = createGame(mrX, blue);
		assertTicketCount(game, BLACK, 1, 2, 3, 4, 5);
		assertTicketCount(game, BLUE, 5, 4, 3, 0, 0);
	}

	@Test
	public void testGetPlayerTicketsForNonExistentPlayerIsEmpty() {
		PlayerConfiguration mrX = new Builder(BLACK).using(dummyPlayer())
				.with(makeTickets(1, 2, 3, 4, 5)).at(1).build();

		PlayerConfiguration blue = new Builder(BLUE).using(dummyPlayer())
				.with(makeTickets(5, 4, 3, 0, 0)).at(2).build();
		ScotlandYardGame game = createGame(mrX, blue);

		// notice that when the player(colour) exists, getPlayerTickets() returns ticket count
		// even if it's zero
		assertTicketsForAllTicketTypeSuchThat(game, BLACK, o -> assertThat(o).isNotEmpty());
		assertTicketsForAllTicketTypeSuchThat(game, BLUE, o -> assertThat(o).isNotEmpty());

		assertTicketsForAllTicketTypeSuchThat(game, RED, o -> assertThat(o).isEmpty());
		assertTicketsForAllTicketTypeSuchThat(game, GREEN, o -> assertThat(o).isEmpty());
		assertTicketsForAllTicketTypeSuchThat(game, YELLOW, o -> assertThat(o).isEmpty());
		assertTicketsForAllTicketTypeSuchThat(game, WHITE, o -> assertThat(o).isEmpty());

	}


}
