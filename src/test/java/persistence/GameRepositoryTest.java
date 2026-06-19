package persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameRepositoryTest {
    private GameRepository repo;

    @BeforeEach
    public void setup() {
        repo = new GameRepository("uno-test-pu");
    }

    @AfterEach
    public void tearDown() {
        if (repo != null) {
            repo.close();
        }
    }

    @Test
    public void testCreateAndGetPlayer() {
        Player p1 = repo.getOrCreatePlayer("Alice");
        assertNotNull(p1.getId());
        assertEquals("Alice", p1.getName());

        Player p2 = repo.getOrCreatePlayer("Alice");
        assertEquals(p1.getId(), p2.getId());
    }

    @Test
    public void testSaveGameAndRound() {
        Player p1 = repo.getOrCreatePlayer("Alice");
        Player p2 = repo.getOrCreatePlayer("Bob");

        Game game = repo.createGame();
        assertNotNull(game.getId());
        assertNotNull(game.getStartTime());

        Round round = repo.createRound(game, 1);
        assertNotNull(round.getId());
        assertEquals(1, round.getRoundNumber());

        repo.saveScore(round, p1, 50);
        repo.saveScore(round, p2, 0);

        repo.finishRound(round, p1);
        repo.finishGame(game, p1);

        List<Game> recent = repo.getRecentGames(10);
        assertEquals(1, recent.size());
        assertEquals("Alice", recent.get(0).getWinner().getName());
        assertNotNull(recent.get(0).getEndTime());
    }

    @Test
    public void testQueryFeatures() {
        Player alice = repo.getOrCreatePlayer("Alice");
        Player bob = repo.getOrCreatePlayer("Bob");

        // Game 1: Alice wins
        Game game1 = repo.createGame();
        Round r1 = repo.createRound(game1, 1);
        repo.saveScore(r1, alice, 100);
        repo.saveScore(r1, bob, 0);
        repo.finishRound(r1, alice);
        repo.finishGame(game1, alice);

        // Game 2: Bob wins
        Game game2 = repo.createGame();
        Round r2 = repo.createRound(game2, 1);
        repo.saveScore(r2, alice, 0);
        repo.saveScore(r2, bob, 200);
        repo.finishRound(r2, bob);
        repo.finishGame(game2, bob);

        // Game 3: Bob wins again
        Game game3 = repo.createGame();
        Round r3 = repo.createRound(game3, 1);
        repo.saveScore(r3, alice, 0);
        repo.saveScore(r3, bob, 150);
        repo.finishRound(r3, bob);
        repo.finishGame(game3, bob);

        // Test getPlayerWinCounts
        List<Object[]> winCounts = repo.getPlayerWinCounts();
        assertEquals(2, winCounts.size());
        // Bob has 2 wins, Alice has 1. Ordered by COUNT(g) DESC
        assertEquals("Bob", winCounts.get(0)[0]);
        assertEquals(2L, winCounts.get(0)[1]);
        assertEquals("Alice", winCounts.get(1)[0]);
        assertEquals(1L, winCounts.get(1)[1]);

        // Test getHighestScores
        List<Object[]> highestScores = repo.getHighestScores(2);
        assertEquals(2, highestScores.size());
        // Bob 200, Alice 100
        assertEquals("Bob", highestScores.get(0)[0]);
        assertEquals(200, highestScores.get(0)[1]);
        assertEquals("Alice", highestScores.get(1)[0]);
        assertEquals(100, highestScores.get(1)[1]);
    }
}
