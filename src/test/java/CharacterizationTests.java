import org.junit.jupiter.api.Test;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class CharacterizationTests {

    @Test
    public void testBasicLegality() {
        assertEquals("R", CardRules.color("R5"));
        assertEquals("DRAW_TWO", CardRules.rank("G+2"));
        assertEquals(50, CardRules.points("W4"));
        assertTrue(CardRules.isLegal("R2", "R9", ""));
        assertTrue(CardRules.isLegal("G9", "R9", ""));
        assertTrue(CardRules.isLegal("B3", "W", "B"));
        assertFalse(CardRules.isLegal("B3", "R9", ""));
    }

    @Test
    public void testBotStrategy() {
        ArrayList<String> h = new ArrayList<>();
        h.add("B3"); h.add("R4"); h.add("W");
        assertEquals(1, BotStrategy.chooseCard(h, "R9", ""));

        ArrayList<String> h2 = new ArrayList<>();
        h2.add("B1"); h2.add("B2"); h2.add("R3");
        assertEquals("B", BotStrategy.chooseColor(h2));

        // bot returns -1 when no legal card
        ArrayList<String> noMatch = new ArrayList<>();
        noMatch.add("B1"); noMatch.add("G2");
        assertEquals(-1, BotStrategy.chooseCard(noMatch, "R9", ""));

        // bot prioritizes draw two over other legal cards
        ArrayList<String> h3 = new ArrayList<>();
        h3.add("R5"); h3.add("R+2"); h3.add("W");
        assertEquals(1, BotStrategy.chooseCard(h3, "R9", ""));

        // bot prioritizes skip after draw two
        ArrayList<String> h4 = new ArrayList<>();
        h4.add("R5"); h4.add("RS"); h4.add("W");
        assertEquals(1, BotStrategy.chooseCard(h4, "R9", ""));
    }

    @Test
    public void testActionTypeMatches() {
        assertTrue(CardRules.isLegal("RS", "GS", ""));
        assertTrue(CardRules.isLegal("BR", "GR", ""));
        assertTrue(CardRules.isLegal("R+2", "G+2", ""));
    }

    @Test
    public void testWildCards() {
        assertTrue(CardRules.isLegal("W", "R5", ""));
        assertTrue(CardRules.isLegal("W4", "G3", ""));
        assertTrue(CardRules.isLegal("W", "BS", ""));
        assertTrue(CardRules.isLegal("W4", "YR", ""));
    }

    @Test
    public void testCalledColorAfterWild() {
        assertTrue(CardRules.isLegal("R3", "W", "R"));
        assertFalse(CardRules.isLegal("G3", "W", "R"));
        assertTrue(CardRules.isLegal("B7", "W4", "B"));
    }

    @Test
    public void testIllegalPlays() {
        assertFalse(CardRules.isLegal("G7", "R5", ""));
        assertFalse(CardRules.isLegal("RS", "G5", ""));
    }

    @Test
    public void testScoring() {
        assertEquals(0, CardRules.points("R0"));
        assertEquals(5, CardRules.points("R5"));
        assertEquals(9, CardRules.points("B9"));
        assertEquals(20, CardRules.points("RS"));
        assertEquals(20, CardRules.points("GR"));
        assertEquals(20, CardRules.points("B+2"));
    }

    @Test
    public void testColorAndRankExtraction() {
        assertEquals("", CardRules.color("W"));
        assertEquals("", CardRules.color("W4"));
        assertEquals("SKIP", CardRules.rank("RS"));
        assertEquals("REVERSE", CardRules.rank("GR"));
        assertEquals("NUMBER", CardRules.rank("B5"));
        assertEquals(0, CardRules.number("R0"));
        assertEquals(9, CardRules.number("G9"));
        assertEquals(-1, CardRules.number("RS"));
    }

    @Test
    public void testGameStateTurnFlow() {
        // skip advances past next player
        GameState ts = new GameState();
        ts.addPlayer("A", false); ts.addPlayer("B", false); ts.addPlayer("C", false);
        ts.next(); ts.next();
        assertEquals(2, ts.getCurrentPlayer());

        // direction changes
        GameState ts2 = new GameState();
        ts2.addPlayer("A", false); ts2.addPlayer("B", false); ts2.addPlayer("C", false);
        ts2.reverseDirection(); ts2.next();
        assertEquals(2, ts2.getCurrentPlayer());
        assertEquals(-1, ts2.getDirection());

        // Reverse with 2 players acts like skip
        GameState ts3 = new GameState();
        ts3.addPlayer("A", false); ts3.addPlayer("B", false);
        ts3.reverseDirection(); ts3.next(); ts3.next();
        assertEquals(0, ts3.getCurrentPlayer());

        // next player gets 2 cards and is skipped
        GameState ts4 = new GameState();
        ts4.addPlayer("A", false); ts4.addPlayer("B", false); ts4.addPlayer("C", false);
        ts4.addToDeck("R1"); ts4.addToDeck("G2"); ts4.addToDeck("B3");
        ts4.next();
        ts4.getHand(ts4.getCurrentPlayer()).add(ts4.draw());
        ts4.getHand(ts4.getCurrentPlayer()).add(ts4.draw());
        assertEquals(2, ts4.getHand(1).size());
        ts4.next();
        assertEquals(2, ts4.getCurrentPlayer());
    }

    @Test
    public void testGameStateDrawing() {
        // Drawing from deck
        GameState ts5 = new GameState();
        ts5.addToDeck("Y7");
        assertEquals("Y7", ts5.draw());

        // Reshuffle when deck empty
        GameState ts6 = new GameState();
        ts6.addToDiscard("R1"); ts6.addToDiscard("G2");
        String reshuffled = ts6.draw();
        assertTrue(reshuffled.equals("R1") || reshuffled.equals("G2"));

        // Fallback wild when both piles empty
        GameState ts7 = new GameState();
        assertEquals("W", ts7.draw());
    }
}
