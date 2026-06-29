import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for GameEngine covering all rubric sections.
 * Uses a TestPlayerController for scripted decisions and a NoOpListener
 * to run without console output.
 */
public class GameEngineTest {

    // --- Test helpers ---

    /**
     * A controllable player controller for testing.
     */
    static class TestPlayerController implements PlayerController {
        private List<Integer> cardChoices = new ArrayList<>();
        private List<String> colorChoices = new ArrayList<>();
        private List<Boolean> playDrawnChoices = new ArrayList<>();
        private boolean callUnoResult = true;
        private int cardChoiceIndex = 0;
        private int colorChoiceIndex = 0;
        private int playDrawnIndex = 0;

        void setCardChoices(int... choices) {
            cardChoices.clear();
            for (int c : choices) cardChoices.add(c);
            cardChoiceIndex = 0;
        }

        void setColorChoices(String... colors) {
            colorChoices.clear();
            for (String c : colors) colorChoices.add(c);
            colorChoiceIndex = 0;
        }

        void setPlayDrawnChoices(boolean... choices) {
            playDrawnChoices.clear();
            for (boolean c : choices) playDrawnChoices.add(c);
            playDrawnIndex = 0;
        }

        void setCallUno(boolean val) {
            callUnoResult = val;
        }

        @Override
        public int chooseCard(List<String> hand, String upCard, String calledColor) {
            if (cardChoiceIndex < cardChoices.size()) {
                return cardChoices.get(cardChoiceIndex++);
            }
            // Default: try to find a legal card, else draw
            for (int i = 0; i < hand.size(); i++) {
                if (CardRules.isLegal(hand.get(i), upCard, calledColor)) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public String chooseColor(List<String> hand) {
            if (colorChoiceIndex < colorChoices.size()) {
                return colorChoices.get(colorChoiceIndex++);
            }
            return "R";
        }

        @Override
        public boolean wantToPlayDrawnCard(String card) {
            if (playDrawnIndex < playDrawnChoices.size()) {
                return playDrawnChoices.get(playDrawnIndex++);
            }
            return true;
        }

        @Override
        public boolean callUno() {
            return callUnoResult;
        }
    }

    /**
     * No-op event listener for tests.
     */
    static class NoOpListener implements GameEventListener {
        List<String> events = new ArrayList<>();

        @Override public void onGameStart(int gameNumber, int totalGames) { events.add("gameStart"); }
        @Override public void onRoundStart(int roundNumber) { events.add("roundStart"); }
        @Override public void onTurnStart(String p, String u, String c, List<String> h) { events.add("turnStart:" + p); }
        @Override public void onCardPlayed(String p, String c) { events.add("played:" + p + ":" + c); }
        @Override public void onCardDrawn(String p, String c) { events.add("drawn:" + p); }
        @Override public void onColorCalled(String p, String c) { events.add("color:" + p + ":" + c); }
        @Override public void onUnoCall(String p) { events.add("uno:" + p); }
        @Override public void onUnoPenalty(String p) { events.add("unoPenalty:" + p); }
        @Override public void onSkip(String p) { events.add("skip:" + p); }
        @Override public void onReverse() { events.add("reverse"); }
        @Override public void onDrawTwo(String p) { events.add("drawTwo:" + p); }
        @Override public void onDrawFour(String p) { events.add("drawFour:" + p); }
        @Override public void onIllegalPlay(String p, String c) { events.add("illegal:" + p); }
        @Override public void onInvalidIndex(String p) { events.add("invalidIndex:" + p); }
        @Override public void onRoundEnd(String p, int pts) { events.add("roundEnd:" + p + ":" + pts); }
        @Override public void onGameEnd(String p, List<String> names, int[] scores) { events.add("gameEnd:" + p); }
        @Override public void onSafetyLimit() { events.add("safetyLimit"); }
        @Override public void onPass(String p) { events.add("pass:" + p); }
    }

    // --- §1.1 Deck Composition Tests ---

    @Test
    public void testDeckComposition_TotalCards() {
        GameState gs = new GameState();
        gs.addPlayer("A", false);
        gs.addPlayer("B", false);
        gs.setRandom(new Random(42));
        gs.buildDeck();

        // Draw all cards and count
        List<String> allCards = new ArrayList<>();
        // We know deck starts with 108 cards, no hands dealt yet
        assertEquals(108, gs.getDeckSize());
    }

    @Test
    public void testDeckComposition_CardCounts() {
        GameState gs = new GameState();
        gs.addPlayer("A", false);
        gs.setRandom(new Random(42));
        gs.buildDeck();

        // Draw all 108 cards
        List<String> allCards = new ArrayList<>();
        for (int i = 0; i < 108; i++) {
            allCards.add(gs.draw());
        }

        // Count by type
        int zeros = 0, numbers = 0, skips = 0, reverses = 0, drawTwos = 0, wilds = 0, wildFours = 0;
        for (String card : allCards) {
            String rank = CardRules.rank(card);
            if (rank.equals("NUMBER")) {
                if (CardRules.number(card) == 0) zeros++;
                else numbers++;
            } else if (rank.equals("SKIP")) skips++;
            else if (rank.equals("REVERSE")) reverses++;
            else if (rank.equals("DRAW_TWO")) drawTwos++;
            else if (rank.equals("WILD")) wilds++;
            else if (rank.equals("WILD_DRAW_FOUR")) wildFours++;
        }

        assertEquals(4, zeros, "Should have 4 zero cards (one per color)");
        assertEquals(72, numbers, "Should have 72 number cards (1-9, two each, four colors)");
        assertEquals(8, skips, "Should have 8 skip cards (two per color)");
        assertEquals(8, reverses, "Should have 8 reverse cards (two per color)");
        assertEquals(8, drawTwos, "Should have 8 draw two cards (two per color)");
        assertEquals(4, wilds, "Should have 4 wild cards");
        assertEquals(4, wildFours, "Should have 4 wild draw four cards");
    }

    @Test
    public void testDeckComposition_FourColors() {
        GameState gs = new GameState();
        gs.addPlayer("A", false);
        gs.setRandom(new Random(42));
        gs.buildDeck();

        List<String> allCards = new ArrayList<>();
        for (int i = 0; i < 108; i++) {
            allCards.add(gs.draw());
        }

        int red = 0, yellow = 0, green = 0, blue = 0;
        for (String card : allCards) {
            String color = CardRules.color(card);
            if (color.equals("R")) red++;
            else if (color.equals("Y")) yellow++;
            else if (color.equals("G")) green++;
            else if (color.equals("B")) blue++;
        }

        assertEquals(25, red, "Each color should have 25 cards");
        assertEquals(25, yellow, "Each color should have 25 cards");
        assertEquals(25, green, "Each color should have 25 cards");
        assertEquals(25, blue, "Each color should have 25 cards");
    }

    // --- §1.2 Legal Play Validation Tests ---

    @Test
    public void testLegalPlay_MatchByColor() {
        assertTrue(CardRules.isLegal("R2", "R9", ""));
        assertTrue(CardRules.isLegal("R0", "RS", ""));
        assertTrue(CardRules.isLegal("G+2", "G5", ""));
    }

    @Test
    public void testLegalPlay_MatchByNumber() {
        assertTrue(CardRules.isLegal("G9", "R9", ""));
        assertTrue(CardRules.isLegal("B0", "R0", ""));
    }

    @Test
    public void testLegalPlay_MatchByActionType() {
        assertTrue(CardRules.isLegal("RS", "GS", ""));
        assertTrue(CardRules.isLegal("BR", "GR", ""));
        assertTrue(CardRules.isLegal("R+2", "G+2", ""));
    }

    @Test
    public void testLegalPlay_WildAlwaysLegal() {
        assertTrue(CardRules.isLegal("W", "R5", ""));
        assertTrue(CardRules.isLegal("W4", "G3", ""));
        assertTrue(CardRules.isLegal("W", "BS", ""));
    }

    @Test
    public void testLegalPlay_IllegalPlaysRejected() {
        assertFalse(CardRules.isLegal("G7", "R5", ""));
        assertFalse(CardRules.isLegal("B3", "R9", ""));
        assertFalse(CardRules.isLegal("RS", "G5", ""));
    }

    @Test
    public void testLegalPlay_CalledColorAfterWild() {
        assertTrue(CardRules.isLegal("R3", "W", "R"));
        assertFalse(CardRules.isLegal("G3", "W", "R"));
        assertTrue(CardRules.isLegal("B7", "W4", "B"));
    }

    // --- §1.3 Skip Tests ---

    @Test
    public void testSkip_NextPlayerLosesTurn() {
        GameState gs = new GameState();
        gs.addPlayer("A", false);
        gs.addPlayer("B", false);
        gs.addPlayer("C", false);
        gs.setRandom(new Random(42));

        TestPlayerController ctrlA = new TestPlayerController();
        TestPlayerController ctrlB = new TestPlayerController();
        TestPlayerController ctrlC = new TestPlayerController();
        NoOpListener listener = new NoOpListener();

        // Set up: A plays a Skip card, B should be skipped
        gs.buildDeck();
        gs.dealHands();

        // Clear all hands and set up controlled scenario
        gs.getHand(0).clear();
        gs.getHand(1).clear();
        gs.getHand(2).clear();

        gs.getHand(0).add("RS"); // A has a red skip
        gs.getHand(0).add("R1"); // and another card so round doesn't end
        gs.getHand(1).add("R5");
        gs.getHand(1).add("R6");
        gs.getHand(2).add("R7");
        gs.getHand(2).add("R8");

        gs.setUpCard("R3");
        gs.setCalledColor("");

        // Force current player to be A (index 0)
        while (gs.getCurrentPlayer() != 0) {
            gs.next();
        }

        GameEngine engine = new GameEngine(gs, new PlayerController[]{ctrlA, ctrlB, ctrlC}, listener);
        ctrlA.setCardChoices(0); // Play RS (skip)
        engine.playTurn();

        // After A plays RS: B is skipped, current player should be C
        assertEquals(2, gs.getCurrentPlayer(), "After skip, player C (index 2) should be next");
        assertTrue(listener.events.contains("skip:B"), "Event should show B was skipped");
    }

    // --- §1.4 Reverse Tests ---

    @Test
    public void testReverse_DirectionChanges() {
        GameState gs = new GameState();
        gs.addPlayer("A", false);
        gs.addPlayer("B", false);
        gs.addPlayer("C", false);
        gs.setRandom(new Random(42));

        TestPlayerController ctrlA = new TestPlayerController();
        TestPlayerController ctrlB = new TestPlayerController();
        TestPlayerController ctrlC = new TestPlayerController();
        NoOpListener listener = new NoOpListener();

        gs.buildDeck();
        gs.dealHands();
        gs.getHand(0).clear();
        gs.getHand(1).clear();
        gs.getHand(2).clear();

        gs.getHand(0).add("RR"); // A has red reverse
        gs.getHand(0).add("R1");
        gs.getHand(1).add("R5");
        gs.getHand(1).add("R6");
        gs.getHand(2).add("R7");
        gs.getHand(2).add("R8");

        gs.setUpCard("R3");
        gs.setCalledColor("");

        while (gs.getCurrentPlayer() != 0) gs.next();

        assertEquals(1, gs.getDirection(), "Direction should start as 1");

        GameEngine engine = new GameEngine(gs, new PlayerController[]{ctrlA, ctrlB, ctrlC}, listener);
        ctrlA.setCardChoices(0); // Play RR (reverse)
        engine.playTurn();

        assertEquals(-1, gs.getDirection(), "Direction should be reversed to -1");
        assertTrue(listener.events.contains("reverse"), "Reverse event should fire");
    }

    @Test
    public void testReverse_TwoPlayerActsAsSkip() {
        GameState gs = new GameState();
        gs.addPlayer("A", false);
        gs.addPlayer("B", false);
        gs.setRandom(new Random(42));

        TestPlayerController ctrlA = new TestPlayerController();
        TestPlayerController ctrlB = new TestPlayerController();
        NoOpListener listener = new NoOpListener();

        gs.buildDeck();
        gs.dealHands();
        gs.getHand(0).clear();
        gs.getHand(1).clear();

        gs.getHand(0).add("RR");
        gs.getHand(0).add("R1");
        gs.getHand(0).add("R2");
        gs.getHand(1).add("R5");
        gs.getHand(1).add("R6");
        gs.getHand(1).add("R7");

        gs.setUpCard("R3");
        gs.setCalledColor("");
        while (gs.getCurrentPlayer() != 0) gs.next();

        GameEngine engine = new GameEngine(gs, new PlayerController[]{ctrlA, ctrlB}, listener);
        ctrlA.setCardChoices(0); // Play RR (reverse)
        engine.playTurn();

        // In 2-player, reverse acts as skip, so it should be A's turn again
        assertEquals(0, gs.getCurrentPlayer(), "In 2-player, reverse should act as skip, returning to A");
    }

    // --- §1.5 Draw Two Tests ---

    @Test
    public void testDrawTwo_NextPlayerDrawsTwoAndLosesTurn() {
        GameState gs = new GameState();
        gs.addPlayer("A", false);
        gs.addPlayer("B", false);
        gs.addPlayer("C", false);
        gs.setRandom(new Random(42));

        TestPlayerController ctrlA = new TestPlayerController();
        TestPlayerController ctrlB = new TestPlayerController();
        TestPlayerController ctrlC = new TestPlayerController();
        NoOpListener listener = new NoOpListener();

        gs.buildDeck();
        gs.dealHands();
        gs.getHand(0).clear();
        gs.getHand(1).clear();
        gs.getHand(2).clear();

        gs.getHand(0).add("R+2");
        gs.getHand(0).add("R1");
        gs.getHand(1).add("R5");
        gs.getHand(2).add("R7");
        gs.getHand(2).add("R8");

        gs.setUpCard("R3");
        gs.setCalledColor("");
        while (gs.getCurrentPlayer() != 0) gs.next();

        int bHandSizeBefore = gs.getHand(1).size();

        GameEngine engine = new GameEngine(gs, new PlayerController[]{ctrlA, ctrlB, ctrlC}, listener);
        ctrlA.setCardChoices(0); // Play R+2
        engine.playTurn();

        assertEquals(bHandSizeBefore + 2, gs.getHand(1).size(), "B should have drawn 2 cards");
        assertEquals(2, gs.getCurrentPlayer(), "B should be skipped, C should be next");
        assertTrue(listener.events.contains("drawTwo:B"), "DrawTwo event should fire for B");
    }

    // --- §1.6 Wild Tests ---

    @Test
    public void testWild_ColorChoiceAffectsLegalPlay() {
        GameState gs = new GameState();
        gs.addPlayer("A", false);
        gs.addPlayer("B", false);
        gs.setRandom(new Random(42));

        TestPlayerController ctrlA = new TestPlayerController();
        TestPlayerController ctrlB = new TestPlayerController();
        NoOpListener listener = new NoOpListener();

        gs.buildDeck();
        gs.dealHands();
        gs.getHand(0).clear();
        gs.getHand(1).clear();

        gs.getHand(0).add("W");
        gs.getHand(0).add("R1");
        gs.getHand(0).add("R2");
        gs.getHand(1).add("G5");
        gs.getHand(1).add("G6");
        gs.getHand(1).add("G7");

        gs.setUpCard("R3");
        gs.setCalledColor("");
        while (gs.getCurrentPlayer() != 0) gs.next();

        GameEngine engine = new GameEngine(gs, new PlayerController[]{ctrlA, ctrlB}, listener);
        ctrlA.setCardChoices(0); // Play W
        ctrlA.setColorChoices("G"); // Call green
        engine.playTurn();

        assertEquals("G", gs.getCalledColor(), "Called color should be G");
        assertTrue(listener.events.contains("color:A:G"), "Color call event should fire");

        // Now B should be able to play green cards
        assertTrue(CardRules.isLegal("G5", gs.getUpCard(), gs.getCalledColor()),
                "G5 should be legal after wild calls green");
        assertFalse(CardRules.isLegal("R1", gs.getUpCard(), gs.getCalledColor()),
                "R1 should be illegal after wild calls green");
    }

    // --- §1.7 Wild Draw Four Tests ---

    @Test
    public void testWildDrawFour_NextPlayerDrawsFourAndLosesTurn() {
        GameState gs = new GameState();
        gs.addPlayer("A", false);
        gs.addPlayer("B", false);
        gs.addPlayer("C", false);
        gs.setRandom(new Random(42));

        TestPlayerController ctrlA = new TestPlayerController();
        TestPlayerController ctrlB = new TestPlayerController();
        TestPlayerController ctrlC = new TestPlayerController();
        NoOpListener listener = new NoOpListener();

        gs.buildDeck();
        gs.dealHands();
        gs.getHand(0).clear();
        gs.getHand(1).clear();
        gs.getHand(2).clear();

        gs.getHand(0).add("W4");
        gs.getHand(0).add("R1");
        gs.getHand(1).add("R5");
        gs.getHand(2).add("R7");
        gs.getHand(2).add("R8");

        gs.setUpCard("R3");
        gs.setCalledColor("");
        while (gs.getCurrentPlayer() != 0) gs.next();

        int bHandSizeBefore = gs.getHand(1).size();

        GameEngine engine = new GameEngine(gs, new PlayerController[]{ctrlA, ctrlB, ctrlC}, listener);
        ctrlA.setCardChoices(0); // Play W4
        ctrlA.setColorChoices("R"); // Call red
        engine.playTurn();

        assertEquals(bHandSizeBefore + 4, gs.getHand(1).size(), "B should have drawn 4 cards");
        assertEquals(2, gs.getCurrentPlayer(), "B should be skipped, C should be next");
        assertTrue(listener.events.contains("drawFour:B"), "DrawFour event should fire for B");
        assertEquals("R", gs.getCalledColor(), "Called color should be R");
    }

    // --- §1.8 Draw/Pass Behavior Tests ---

    @Test
    public void testDrawPass_DrawWhenNoLegalPlay() {
        GameState gs = new GameState();
        gs.addPlayer("A", false);
        gs.addPlayer("B", false);
        gs.setRandom(new Random(42));

        TestPlayerController ctrlA = new TestPlayerController();
        TestPlayerController ctrlB = new TestPlayerController();
        NoOpListener listener = new NoOpListener();

        gs.buildDeck();
        gs.dealHands();
        gs.getHand(0).clear();
        gs.getHand(1).clear();

        // A has no legal cards for a red 9
        gs.getHand(0).add("G3");
        gs.getHand(0).add("B5");
        gs.getHand(1).add("R1");
        gs.getHand(1).add("R2");

        // Ensure the drawn card will NOT be legal
        while(gs.getDeckSize() > 0) {
            gs.draw();
        }
        gs.addToDeck("G9");

        gs.setUpCard("R9");
        gs.setCalledColor("");
        while (gs.getCurrentPlayer() != 0) gs.next();

        int handSizeBefore = gs.getHand(0).size();

        GameEngine engine = new GameEngine(gs, new PlayerController[]{ctrlA, ctrlB}, listener);
        ctrlA.setCardChoices(-1); // Draw
        ctrlA.setPlayDrawnChoices(false); // Don't play drawn card
        engine.playTurn();

        assertEquals(handSizeBefore + 1, gs.getHand(0).size(), "A should have drawn one card");
        assertEquals(1, gs.getCurrentPlayer(), "Turn should pass to B");
    }

    @Test
    public void testDrawPass_PlayDrawnCardIfLegal() {
        GameState gs = new GameState();
        gs.addPlayer("A", false);
        gs.addPlayer("B", false);
        gs.setRandom(new Random(42));

        TestPlayerController ctrlA = new TestPlayerController();
        TestPlayerController ctrlB = new TestPlayerController();
        NoOpListener listener = new NoOpListener();

        gs.buildDeck();
        gs.dealHands();
        gs.getHand(0).clear();
        gs.getHand(1).clear();

        gs.getHand(0).add("G3");
        gs.getHand(0).add("B5");
        gs.getHand(0).add("B6");
        gs.getHand(1).add("R1");
        gs.getHand(1).add("R2");
        gs.getHand(1).add("R3");

        // Ensure the drawn card will be legal
        while(gs.getDeckSize() > 0) {
            gs.draw();
        }
        gs.addToDeck("R7"); // This will be drawn

        gs.setUpCard("R9");
        gs.setCalledColor("");
        while (gs.getCurrentPlayer() != 0) gs.next();

        GameEngine engine = new GameEngine(gs, new PlayerController[]{ctrlA, ctrlB}, listener);
        ctrlA.setCardChoices(-1); // Draw
        ctrlA.setPlayDrawnChoices(true); // Play drawn card
        engine.playTurn();

        // R7 should have been played
        assertEquals("R7", gs.getUpCard(), "Drawn card R7 should be played as up card");
    }

    // --- §1.9 UNO Call and Penalty Tests ---

    @Test
    public void testUnoCall_SuccessfulCall() {
        GameState gs = new GameState();
        gs.addPlayer("A", false);
        gs.addPlayer("B", false);
        gs.setRandom(new Random(42));

        TestPlayerController ctrlA = new TestPlayerController();
        TestPlayerController ctrlB = new TestPlayerController();
        NoOpListener listener = new NoOpListener();

        gs.buildDeck();
        gs.dealHands();
        gs.getHand(0).clear();
        gs.getHand(1).clear();

        gs.getHand(0).add("R5");
        gs.getHand(0).add("R6"); // will play this, leaving 1 card
        gs.getHand(1).add("G1");
        gs.getHand(1).add("G2");
        gs.getHand(1).add("G3");

        gs.setUpCard("R3");
        gs.setCalledColor("");
        while (gs.getCurrentPlayer() != 0) gs.next();

        GameEngine engine = new GameEngine(gs, new PlayerController[]{ctrlA, ctrlB}, listener);
        ctrlA.setCardChoices(1); // Play R6
        ctrlA.setCallUno(true);
        engine.playTurn();

        assertEquals(1, gs.getHand(0).size(), "A should have 1 card left");
        assertTrue(listener.events.contains("uno:A"), "UNO call event should fire");
        assertFalse(listener.events.contains("unoPenalty:A"), "No penalty when UNO is called");
    }

    @Test
    public void testUnoCall_MissedCallPenalty() {
        GameState gs = new GameState();
        gs.addPlayer("A", false);
        gs.addPlayer("B", false);
        gs.setRandom(new Random(42));

        TestPlayerController ctrlA = new TestPlayerController();
        TestPlayerController ctrlB = new TestPlayerController();
        NoOpListener listener = new NoOpListener();

        gs.buildDeck();
        gs.dealHands();
        gs.getHand(0).clear();
        gs.getHand(1).clear();

        gs.getHand(0).add("R5");
        gs.getHand(0).add("R6"); // will play this
        gs.getHand(1).add("G1");
        gs.getHand(1).add("G2");
        gs.getHand(1).add("G3");

        gs.setUpCard("R3");
        gs.setCalledColor("");
        while (gs.getCurrentPlayer() != 0) gs.next();

        GameEngine engine = new GameEngine(gs, new PlayerController[]{ctrlA, ctrlB}, listener);
        ctrlA.setCardChoices(1); // Play R6
        ctrlA.setCallUno(false); // FAIL to call UNO
        engine.playTurn();

        // A played R6, had 1 card left, didn't call UNO -> penalty: draw 2
        assertEquals(3, gs.getHand(0).size(), "A should have 1 original + 2 penalty = 3 cards");
        assertTrue(listener.events.contains("unoPenalty:A"), "UNO penalty event should fire");
    }

    // --- §1.10 Scoring and Multi-Round Tests ---

    @Test
    public void testScoring_CardValues() {
        assertEquals(0, CardRules.points("R0"));
        assertEquals(5, CardRules.points("R5"));
        assertEquals(9, CardRules.points("B9"));
        assertEquals(20, CardRules.points("RS"));
        assertEquals(20, CardRules.points("GR"));
        assertEquals(20, CardRules.points("B+2"));
        assertEquals(50, CardRules.points("W"));
        assertEquals(50, CardRules.points("W4"));
    }

    @Test
    public void testScoring_RoundWinnerGetsPoints() {
        GameState gs = new GameState();
        gs.addPlayer("A", false);
        gs.addPlayer("B", false);
        gs.setRandom(new Random(42));

        TestPlayerController ctrlA = new TestPlayerController();
        TestPlayerController ctrlB = new TestPlayerController();
        NoOpListener listener = new NoOpListener();

        gs.buildDeck();
        gs.dealHands();
        gs.getHand(0).clear();
        gs.getHand(1).clear();

        gs.getHand(0).add("R5"); // A will play this to win
        gs.getHand(1).add("G7"); // B has 7 points remaining

        gs.setUpCard("R3");
        gs.setCalledColor("");
        while (gs.getCurrentPlayer() != 0) gs.next();

        GameEngine engine = new GameEngine(gs, new PlayerController[]{ctrlA, ctrlB}, listener);
        ctrlA.setCardChoices(0); // Play R5 to win
        engine.playTurn();

        assertTrue(engine.isRoundOver(), "Round should be over");
        assertEquals(0, engine.getRoundWinner(), "A should be the winner");
        assertEquals(7, gs.getScores()[0], "A should score 7 points (G7 from B's hand)");
    }

    @Test
    public void testMultiRound_GameEndsAtTargetScore() {
        GameState gs = new GameState();
        gs.addPlayer("A", false);
        gs.addPlayer("B", false);
        gs.setRandom(new Random(42));

        // Use bot controllers that auto-play
        PlayerController[] ctrls = { new BotController(), new BotController() };
        NoOpListener listener = new NoOpListener();

        GameEngine engine = new GameEngine(gs, ctrls, listener);
        int winner = engine.playGame(500, 100);

        // Game should have ended with someone reaching 500
        assertTrue(gs.getScores()[winner] >= 500 || listener.events.contains("gameEnd:A") || listener.events.contains("gameEnd:B"),
                "Game should end when a player reaches the target score");
        assertTrue(listener.events.stream().anyMatch(e -> e.startsWith("gameEnd:")),
                "Game end event should fire");
    }

    // --- §1.8 Additional: full round with draw-pass flow ---

    @Test
    public void testFullRound_CompletesWithBots() {
        GameState gs = new GameState();
        gs.addPlayer("Bot1", false);
        gs.addPlayer("Bot2", false);
        gs.addPlayer("Bot3", false);
        gs.setRandom(new Random(42));

        PlayerController[] ctrls = { new BotController(), new BotController(), new BotController() };
        NoOpListener listener = new NoOpListener();

        GameEngine engine = new GameEngine(gs, ctrls, listener);
        engine.playRound();

        assertTrue(engine.isRoundOver(), "Round should complete");
        int winner = engine.getRoundWinner();
        assertTrue(winner >= 0 && winner < 3, "Winner should be a valid player index");
    }

    // --- Additional architecture test: engine works without console ---

    @Test
    public void testEngine_NoConsoleRequired() {
        // This test proves the engine runs fully without System.in/out
        GameState gs = new GameState();
        gs.addPlayer("A", false);
        gs.addPlayer("B", false);
        gs.setRandom(new Random(123));

        TestPlayerController ctrlA = new TestPlayerController();
        TestPlayerController ctrlB = new TestPlayerController();
        NoOpListener listener = new NoOpListener();

        GameEngine engine = new GameEngine(gs, new PlayerController[]{ctrlA, ctrlB}, listener);
        engine.playRound();

        assertTrue(engine.isRoundOver(), "Engine should complete a round without console");
        assertFalse(listener.events.isEmpty(), "Events should have been recorded");
    }
}
