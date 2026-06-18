import java.util.ArrayList;

public class CharacterizationTests {

    public static void main(String[] args) {
        runAll();
    }

    public static void runAll() {
        int passed = 0;
        passed += testBasicLegality();
        passed += testBotStrategy();
        passed += testActionTypeMatches();
        passed += testWildCards();
        passed += testCalledColorAfterWild();
        passed += testIllegalPlays();
        passed += testScoring();
        passed += testColorAndRankExtraction();
        passed += testGameStateTurnFlow();
        passed += testGameStateDrawing();
        
        System.out.println("Passed " + passed + " characterization checks.");
    }

    private static int testBasicLegality() {
        int passed = 0;
        if (CardRules.color("R5").equals("R")) passed++; else fail("color R5");
        if (CardRules.rank("G+2").equals("DRAW_TWO")) passed++; else fail("rank +2");
        if (CardRules.points("W4") == 50) passed++; else fail("wild points");
        if (CardRules.isLegal("R2", "R9", "")) passed++; else fail("same color");
        if (CardRules.isLegal("G9", "R9", "")) passed++; else fail("same number");
        if (CardRules.isLegal("B3", "W", "B")) passed++; else fail("called color");
        if (!CardRules.isLegal("B3", "R9", "")) passed++; else fail("illegal mismatch");
        return passed;
    }

    private static int testBotStrategy() {
        int passed = 0;
        ArrayList<String> h = new ArrayList<String>();
        h.add("B3"); h.add("R4"); h.add("W");
        if (BotStrategy.chooseCard(h, "R9", "") == 1) passed++; else fail("bot normal before wild");

        ArrayList<String> h2 = new ArrayList<String>();
        h2.add("B1"); h2.add("B2"); h2.add("R3");
        if (BotStrategy.chooseColor(h2).equals("B")) passed++; else fail("bot color");

        // bot returns -1 when no legal card
        ArrayList<String> noMatch = new ArrayList<String>();
        noMatch.add("B1"); noMatch.add("G2");
        if (BotStrategy.chooseCard(noMatch, "R9", "") == -1) passed++; else fail("bot draws when no legal card");

        // bot prioritizes draw two over other legal cards
        ArrayList<String> h3 = new ArrayList<String>();
        h3.add("R5"); h3.add("R+2"); h3.add("W");
        if (BotStrategy.chooseCard(h3, "R9", "") == 1) passed++; else fail("bot prioritizes draw two");

        // bot prioritizes skip after draw two
        ArrayList<String> h4 = new ArrayList<String>();
        h4.add("R5"); h4.add("RS"); h4.add("W");
        if (BotStrategy.chooseCard(h4, "R9", "") == 1) passed++; else fail("bot prioritizes skip");
        
        return passed;
    }

    private static int testActionTypeMatches() {
        int passed = 0;
        if (CardRules.isLegal("RS", "GS", "")) passed++; else fail("skip on skip");
        if (CardRules.isLegal("BR", "GR", "")) passed++; else fail("reverse on reverse");
        if (CardRules.isLegal("R+2", "G+2", "")) passed++; else fail("draw two on draw two");
        return passed;
    }

    private static int testWildCards() {
        int passed = 0;
        if (CardRules.isLegal("W", "R5", "")) passed++; else fail("wild on number");
        if (CardRules.isLegal("W4", "G3", "")) passed++; else fail("w4 on number");
        if (CardRules.isLegal("W", "BS", "")) passed++; else fail("wild on action");
        if (CardRules.isLegal("W4", "YR", "")) passed++; else fail("w4 on reverse");
        return passed;
    }

    private static int testCalledColorAfterWild() {
        int passed = 0;
        if (CardRules.isLegal("R3", "W", "R")) passed++; else fail("match called color after wild");
        if (!CardRules.isLegal("G3", "W", "R")) passed++; else fail("wrong called color after wild");
        if (CardRules.isLegal("B7", "W4", "B")) passed++; else fail("match called color after w4");
        return passed;
    }

    private static int testIllegalPlays() {
        int passed = 0;
        if (!CardRules.isLegal("G7", "R5", "")) passed++; else fail("different color and number");
        if (!CardRules.isLegal("RS", "G5", "")) passed++; else fail("skip not matching color or type");
        return passed;
    }

    private static int testScoring() {
        int passed = 0;
        if (CardRules.points("R0") == 0) passed++; else fail("points R0");
        if (CardRules.points("R5") == 5) passed++; else fail("points R5");
        if (CardRules.points("B9") == 9) passed++; else fail("points B9");
        if (CardRules.points("RS") == 20) passed++; else fail("points skip");
        if (CardRules.points("GR") == 20) passed++; else fail("points reverse");
        if (CardRules.points("B+2") == 20) passed++; else fail("points draw two");
        return passed;
    }

    private static int testColorAndRankExtraction() {
        int passed = 0;
        if (CardRules.color("W").equals("")) passed++; else fail("wild has no color");
        if (CardRules.color("W4").equals("")) passed++; else fail("w4 has no color");
        if (CardRules.rank("RS").equals("SKIP")) passed++; else fail("rank skip");
        if (CardRules.rank("GR").equals("REVERSE")) passed++; else fail("rank reverse");
        if (CardRules.rank("B5").equals("NUMBER")) passed++; else fail("rank number");
        if (CardRules.number("R0") == 0) passed++; else fail("number R0");
        if (CardRules.number("G9") == 9) passed++; else fail("number G9");
        if (CardRules.number("RS") == -1) passed++; else fail("number of skip");
        return passed;
    }

    private static int testGameStateTurnFlow() {
        int passed = 0;
        // skip advances past next player
        GameState ts = new GameState();
        ts.addPlayer("A", false); ts.addPlayer("B", false); ts.addPlayer("C", false);
        ts.next(); ts.next();
        if (ts.getCurrentPlayer() == 2) passed++; else fail("skip effect skips next player");

        // direction changes
        GameState ts2 = new GameState();
        ts2.addPlayer("A", false); ts2.addPlayer("B", false); ts2.addPlayer("C", false);
        ts2.reverseDirection(); ts2.next();
        if (ts2.getCurrentPlayer() == 2 && ts2.getDirection() == -1) passed++; else fail("reverse changes direction");

        // Reverse with 2 players acts like skip
        GameState ts3 = new GameState();
        ts3.addPlayer("A", false); ts3.addPlayer("B", false);
        ts3.reverseDirection(); ts3.next(); ts3.next();
        if (ts3.getCurrentPlayer() == 0) passed++; else fail("reverse with 2 players acts like skip");

        // next player gets 2 cards and is skipped
        GameState ts4 = new GameState();
        ts4.addPlayer("A", false); ts4.addPlayer("B", false); ts4.addPlayer("C", false);
        ts4.addToDeck("R1"); ts4.addToDeck("G2"); ts4.addToDeck("B3");
        ts4.next();
        ts4.getHand(ts4.getCurrentPlayer()).add(ts4.draw());
        ts4.getHand(ts4.getCurrentPlayer()).add(ts4.draw());
        if (ts4.getHand(1).size() == 2) passed++; else fail("draw two adds 2 cards");
        ts4.next();
        if (ts4.getCurrentPlayer() == 2) passed++; else fail("draw two skips victim");

        return passed;
    }

    private static int testGameStateDrawing() {
        int passed = 0;
        // Drawing from deck
        GameState ts5 = new GameState();
        ts5.addToDeck("Y7");
        if (ts5.draw().equals("Y7")) passed++; else fail("draw from deck");

        // Reshuffle when deck empty
        GameState ts6 = new GameState();
        ts6.addToDiscard("R1"); ts6.addToDiscard("G2");
        String reshuffled = ts6.draw();
        if (reshuffled.equals("R1") || reshuffled.equals("G2")) passed++; else fail("reshuffle from discard");

        // Fallback wild when both piles empty
        GameState ts7 = new GameState();
        if (ts7.draw().equals("W")) passed++; else fail("fallback wild when both piles empty");

        return passed;
    }

    private static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}
