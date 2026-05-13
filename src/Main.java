import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static ArrayList<String> playerNames = new ArrayList<String>();
    static ArrayList<Boolean> humanPlayers = new ArrayList<Boolean>();
    static ArrayList<ArrayList<String>> hands = new ArrayList<ArrayList<String>>();
    static ArrayList<String> deck = new ArrayList<String>();
    static ArrayList<String> discard = new ArrayList<String>();
    static int[] scores = new int[10];
    static int currentPlayer = 0;
    static int direction = 1;
    static String upCard = "";
    static String calledColor = "";
    static boolean quiet = false;
    static Random random = new Random();
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int bots = 3;
        int games = 1;
        boolean human = false;
        long seed = System.currentTimeMillis();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--bots") && i + 1 < args.length) {
                bots = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--games") && i + 1 < args.length) {
                games = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--human")) {
                human = true;
            } else if (args[i].equals("--quiet")) {
                quiet = true;
            } else if (args[i].equals("--seed") && i + 1 < args.length) {
                seed = Long.parseLong(args[++i]);
            } else if (args[i].equals("--self-test")) {
                selfTest();
                return;
            } else if (args[i].equals("--help")) {
                System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
                return;
            }
        }

        random = new Random(seed);
        setupPlayers(bots, human);

        if (playerNames.size() < 2 || playerNames.size() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        for (int g = 1; g <= games; g++) {
            if (!quiet) {
                System.out.println("\n=== Game " + g + " ===");
            }
            playGame();
        }

        System.out.println("\nFinal scores:");
        for (int i = 0; i < playerNames.size(); i++) {
            System.out.println(playerNames.get(i) + ": " + scores[i]);
        }
    }

    static void setupPlayers(int bots, boolean human) {
        playerNames.clear();
        humanPlayers.clear();
        hands.clear();
        if (human) {
            playerNames.add("You");
            humanPlayers.add(Boolean.TRUE);
            hands.add(new ArrayList<String>());
        }
        for (int i = 1; i <= bots; i++) {
            playerNames.add("Bot" + i);
            humanPlayers.add(Boolean.FALSE);
            hands.add(new ArrayList<String>());
        }
    }

    static void playGame() {
        deck.clear();
        String[] colors = { "R", "Y", "G", "B" };
        for (int c = 0; c < colors.length; c++) {
            deck.add(colors[c] + "0");
            for (int n = 1; n <= 9; n++) {
                deck.add(colors[c] + n);
                deck.add(colors[c] + n);
            }
            deck.add(colors[c] + "S");
            deck.add(colors[c] + "S");
            deck.add(colors[c] + "R");
            deck.add(colors[c] + "R");
            deck.add(colors[c] + "+2");
            deck.add(colors[c] + "+2");
        }
        for (int i = 0; i < 4; i++) {
            deck.add("W");
            deck.add("W4");
        }
        Collections.shuffle(deck, random);
        discard.clear();
        for (int i = 0; i < hands.size(); i++) {
            hands.get(i).clear();
        }
        for (int i = 0; i < playerNames.size(); i++) {
            for (int j = 0; j < 7; j++) {
                hands.get(i).add(draw());
            }
        }
        upCard = draw();
        while (upCard.startsWith("W")) {
            discard.add(upCard);
            upCard = draw();
        }
        calledColor = "";
        direction = 1;
        currentPlayer = random.nextInt(playerNames.size());

        int guard = 0;
        while (guard < 3000) {
            guard++;
            String name = playerNames.get(currentPlayer);
            ArrayList<String> hand = hands.get(currentPlayer);

            if (!quiet) {
                System.out.println("\nUp card: " + upCard + (calledColor.equals("") ? "" : " called " + calledColor));
                System.out.println(name + " hand: " + join(hand));
            }

            int chosen = -1;
            if (humanPlayers.get(currentPlayer).booleanValue()) {
                chosen = askHuman(hand);
            } else {
                chosen = chooseBotCard(hand);
            }

            if (chosen == -1) {
                String drawn = draw();
                hand.add(drawn);
                if (!quiet) {
                    System.out.println(name + " draws " + drawn);
                }
                if (CardRules.isLegal(drawn, upCard, calledColor)) {
                    if (!humanPlayers.get(currentPlayer).booleanValue()) {
                        chosen = hand.size() - 1;
                    } else {
                        System.out.print("Play drawn card " + drawn + "? y/n: ");
                        String answer = scanner.nextLine();
                        if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
                            chosen = hand.size() - 1;
                        }
                    }
                }
            }

            if (chosen >= 0) {
                if (chosen >= hand.size()) {
                    if (!quiet) {
                        System.out.println(name + " selected an invalid index and draws a penalty card.");
                    }
                    hand.add(draw());
                    next();
                    continue;
                }

                String card = hand.get(chosen);
                boolean ok = CardRules.isLegal(card, upCard, calledColor);

                if (!ok) {
                    if (!quiet) {
                        System.out.println(name + " tried illegal card " + card + " and draws a penalty card.");
                    }
                    hand.add(draw());
                    next();
                    continue;
                }

                hand.remove(chosen);
                discard.add(upCard);
                upCard = card;
                calledColor = "";
                if (!quiet) {
                    System.out.println(name + " plays " + card);
                }

                if (card.equals("W") || card.equals("W4")) {
                    if (humanPlayers.get(currentPlayer).booleanValue()) {
                        calledColor = askColor();
                    } else {
                        calledColor = chooseBotColor(hand);
                    }
                    if (!quiet) {
                        System.out.println(name + " calls " + calledColor);
                    }
                }

                if (hand.size() == 1 && !quiet) {
                    System.out.println(name + " says UNO!");
                }

                if (hand.size() == 0) {
                    int points = 0;
                    for (int i = 0; i < hands.size(); i++) {
                        if (i != currentPlayer) {
                            for (int j = 0; j < hands.get(i).size(); j++) {
                                points += CardRules.points(hands.get(i).get(j));
                            }
                        }
                    }
                    scores[currentPlayer] += points;
                    if (!quiet) {
                        System.out.println(name + " wins and scores " + points);
                    }
                    return;
                }

                if (CardRules.rank(card).equals("SKIP")) {
                    next();
                    next();
                } else if (CardRules.rank(card).equals("REVERSE")) {
                    direction = direction * -1;
                    if (playerNames.size() == 2) {
                        next();
                        next();
                    } else {
                        next();
                    }
                } else if (CardRules.rank(card).equals("DRAW_TWO")) {
                    next();
                    hands.get(currentPlayer).add(draw());
                    hands.get(currentPlayer).add(draw());
                    if (!quiet) {
                        System.out.println(playerNames.get(currentPlayer) + " draws two.");
                    }
                    next();
                } else if (CardRules.rank(card).equals("WILD_DRAW_FOUR")) {
                    next();
                    for (int i = 0; i < 4; i++) {
                        hands.get(currentPlayer).add(draw());
                    }
                    if (!quiet) {
                        System.out.println(playerNames.get(currentPlayer) + " draws four.");
                    }
                    next();
                } else {
                    next();
                }
            } else {
                next();
            }
        }
        if (!quiet) {
            System.out.println("Game stopped at safety limit.");
        }
    }

    static String draw() {
        if (deck.size() == 0) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.size() == 0) {
            return "W";
        }
        return deck.remove(0);
    }

    static int chooseBotCard(ArrayList<String> hand) {
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            if (CardRules.isLegal(card, upCard, calledColor) && CardRules.rank(card).equals("DRAW_TWO")) {
                return i;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            if (CardRules.isLegal(card, upCard, calledColor) && CardRules.rank(card).equals("SKIP")) {
                return i;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            if (CardRules.isLegal(card, upCard, calledColor) && CardRules.rank(card).equals("NUMBER")) {
                return i;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).startsWith("W")) {
                return i;
            }
        }
        return -1;
    }

    static int askHuman(ArrayList<String> hand) {
        while (true) {
            System.out.print("Choose card index/code or draw: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("DRAW")) {
                return -1;
            }
            try {
                int index = Integer.parseInt(input);
                if (index >= 0 && index < hand.size()) {
                    return index;
                }
            } catch (Exception ignored) {
            }
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).equals(input)) {
                    if (CardRules.isLegal(hand.get(i), upCard, calledColor)) {
                        return i;
                    }
                    System.out.println("That card is not legal.");
                }
            }
            System.out.println("Card not found.");
        }
    }

    static String askColor() {
        while (true) {
            System.out.print("Call color R/Y/G/B: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("R")) {
                return "R";
            }
            if (input.equals("Y")) {
                return "Y";
            }
            if (input.equals("G")) {
                return "G";
            }
            if (input.equals("B")) {
                return "B";
            }
            System.out.println("Bad color.");
        }
    }

    static String chooseBotColor(ArrayList<String> hand) {
        int r = 0;
        int y = 0;
        int g = 0;
        int b = 0;
        for (int i = 0; i < hand.size(); i++) {
            String c = CardRules.color(hand.get(i));
            if (c.equals("R")) {
                r++;
            } else if (c.equals("Y")) {
                y++;
            } else if (c.equals("G")) {
                g++;
            } else if (c.equals("B")) {
                b++;
            }
        }
        if (r >= y && r >= g && r >= b) {
            return "R";
        } else if (y >= r && y >= g && y >= b) {
            return "Y";
        } else if (g >= r && g >= y && g >= b) {
            return "G";
        } else {
            return "B";
        }
    }

    // Card utility methods now live in CardRules class

    static void next() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) {
            currentPlayer = 0;
        }
        if (currentPlayer < 0) {
            currentPlayer = playerNames.size() - 1;
        }
    }

    static String join(ArrayList<String> cards) {
        String out = "";
        for (int i = 0; i < cards.size(); i++) {
            out += i + ":" + cards.get(i);
            if (i < cards.size() - 1) {
                out += " ";
            }
        }
        return out;
    }

    static void selfTest() {
        int passed = 0;
        if (CardRules.color("R5").equals("R"))
            passed++;
        else
            fail("color R5");
        if (CardRules.rank("G+2").equals("DRAW_TWO"))
            passed++;
        else
            fail("rank +2");
        if (CardRules.points("W4") == 50)
            passed++;
        else
            fail("wild points");
        if (CardRules.isLegal("R2", "R9", ""))
            passed++;
        else
            fail("same color");
        if (CardRules.isLegal("G9", "R9", ""))
            passed++;
        else
            fail("same number");
        if (CardRules.isLegal("B3", "W", "B"))
            passed++;
        else
            fail("called color");
        if (!CardRules.isLegal("B3", "R9", ""))
            passed++;
        else
            fail("illegal mismatch");

        ArrayList<String> h = new ArrayList<String>();
        h.add("B3");
        h.add("R4");
        h.add("W");
        upCard = "R9";
        calledColor = "";
        if (chooseBotCard(h) == 1)
            passed++;
        else
            fail("bot normal before wild");

        ArrayList<String> h2 = new ArrayList<String>();
        h2.add("B1");
        h2.add("B2");
        h2.add("R3");
        if (chooseBotColor(h2).equals("B"))
            passed++;
        else
            fail("bot color");

        // Match by action type
        if (CardRules.isLegal("RS", "GS", ""))
            passed++;
        else
            fail("skip on skip");
        if (CardRules.isLegal("BR", "GR", ""))
            passed++;
        else
            fail("reverse on reverse");
        if (CardRules.isLegal("R+2", "G+2", ""))
            passed++;
        else
            fail("draw two on draw two");

        // Wild and wild draw four always legal
        if (CardRules.isLegal("W", "R5", ""))
            passed++;
        else
            fail("wild on number");
        if (CardRules.isLegal("W4", "G3", ""))
            passed++;
        else
            fail("w4 on number");
        if (CardRules.isLegal("W", "BS", ""))
            passed++;
        else
            fail("wild on action");
        if (CardRules.isLegal("W4", "YR", ""))
            passed++;
        else
            fail("w4 on reverse");

        // Called color after wild
        if (CardRules.isLegal("R3", "W", "R"))
            passed++;
        else
            fail("match called color after wild");
        if (!CardRules.isLegal("G3", "W", "R"))
            passed++;
        else
            fail("wrong called color after wild");
        if (CardRules.isLegal("B7", "W4", "B"))
            passed++;
        else
            fail("match called color after w4");

        // Illegal plays
        if (!CardRules.isLegal("G7", "R5", ""))
            passed++;
        else
            fail("different color and number");
        if (!CardRules.isLegal("RS", "G5", ""))
            passed++;
        else
            fail("skip not matching color or type");

        // Scoring
        if (CardRules.points("R0") == 0)
            passed++;
        else
            fail("points R0");
        if (CardRules.points("R5") == 5)
            passed++;
        else
            fail("points R5");
        if (CardRules.points("B9") == 9)
            passed++;
        else
            fail("points B9");
        if (CardRules.points("RS") == 20)
            passed++;
        else
            fail("points skip");
        if (CardRules.points("GR") == 20)
            passed++;
        else
            fail("points reverse");
        if (CardRules.points("B+2") == 20)
            passed++;
        else
            fail("points draw two");

        // Color and rank extraction
        if (CardRules.color("W").equals(""))
            passed++;
        else
            fail("wild has no color");
        if (CardRules.color("W4").equals(""))
            passed++;
        else
            fail("w4 has no color");
        if (CardRules.rank("RS").equals("SKIP"))
            passed++;
        else
            fail("rank skip");
        if (CardRules.rank("GR").equals("REVERSE"))
            passed++;
        else
            fail("rank reverse");
        if (CardRules.rank("B5").equals("NUMBER"))
            passed++;
        else
            fail("rank number");
        if (CardRules.number("R0") == 0)
            passed++;
        else
            fail("number R0");
        if (CardRules.number("G9") == 9)
            passed++;
        else
            fail("number G9");
        if (CardRules.number("RS") == -1)
            passed++;
        else
            fail("number of skip");

        // Save global state
        ArrayList<String> savedNames = new ArrayList<String>(playerNames);
        ArrayList<Boolean> savedHumans = new ArrayList<Boolean>(humanPlayers);
        ArrayList<ArrayList<String>> savedHands = new ArrayList<ArrayList<String>>(hands);
        ArrayList<String> savedDeck = new ArrayList<String>(deck);
        ArrayList<String> savedDiscard = new ArrayList<String>(discard);
        int savedDir = direction;
        int savedCurrent = currentPlayer;
        String savedUp = upCard;
        String savedCall = calledColor;

        // next player is skipped
        playerNames.clear();
        humanPlayers.clear();
        hands.clear();
        playerNames.add("A");
        playerNames.add("B");
        playerNames.add("C");
        humanPlayers.add(false);
        humanPlayers.add(false);
        humanPlayers.add(false);
        hands.add(new ArrayList<String>());
        hands.add(new ArrayList<String>());
        hands.add(new ArrayList<String>());
        direction = 1;
        currentPlayer = 0;
        next();
        next(); // skip advances past next player
        if (currentPlayer == 2)
            passed++;
        else
            fail("skip effect skips next player");

        // direction changes
        direction = 1;
        currentPlayer = 0;
        direction = direction * -1;
        next();
        if (currentPlayer == 2 && direction == -1)
            passed++;
        else
            fail("reverse changes direction");

        // Reverse with 2 players acts like skip
        playerNames.clear();
        humanPlayers.clear();
        hands.clear();
        playerNames.add("A");
        playerNames.add("B");
        humanPlayers.add(false);
        humanPlayers.add(false);
        hands.add(new ArrayList<String>());
        hands.add(new ArrayList<String>());
        direction = 1;
        currentPlayer = 0;
        direction = direction * -1;
        next();
        next(); // reverse + 2 players = skip
        if (currentPlayer == 0)
            passed++;
        else
            fail("reverse with 2 players acts like skip");

        // next player gets 2 cards and is skipped
        playerNames.clear();
        humanPlayers.clear();
        hands.clear();
        playerNames.add("A");
        playerNames.add("B");
        playerNames.add("C");
        humanPlayers.add(false);
        humanPlayers.add(false);
        humanPlayers.add(false);
        hands.add(new ArrayList<String>());
        hands.add(new ArrayList<String>());
        hands.add(new ArrayList<String>());
        deck.clear();
        deck.add("R1");
        deck.add("G2");
        deck.add("B3");
        direction = 1;
        currentPlayer = 0;
        next(); // move to player 1 (the victim)
        hands.get(currentPlayer).add(draw());
        hands.get(currentPlayer).add(draw());
        if (hands.get(1).size() == 2)
            passed++;
        else
            fail("draw two adds 2 cards");
        next(); // skip player 1's turn, move to player 2
        if (currentPlayer == 2)
            passed++;
        else
            fail("draw two skips victim");

        // Drawing from deck
        deck.clear();
        deck.add("Y7");
        String drawn = draw();
        if (drawn.equals("Y7"))
            passed++;
        else
            fail("draw from deck");

        // Reshuffle when deck empty
        deck.clear();
        discard.clear();
        discard.add("R1");
        discard.add("G2");
        String reshuffled = draw();
        if (reshuffled.equals("R1") || reshuffled.equals("G2"))
            passed++;
        else
            fail("reshuffle from discard");

        // Fallback wild when both piles empty
        deck.clear();
        discard.clear();
        String fallback = draw();
        if (fallback.equals("W"))
            passed++;
        else
            fail("fallback wild when both piles empty");

        // bot returns -1 when no legal card
        ArrayList<String> noMatch = new ArrayList<String>();
        noMatch.add("B1");
        noMatch.add("G2");
        upCard = "R9";
        calledColor = "";
        if (chooseBotCard(noMatch) == -1)
            passed++;
        else
            fail("bot draws when no legal card");

        // bot prioritizes draw two over other legal cards
        ArrayList<String> h3 = new ArrayList<String>();
        h3.add("R5");
        h3.add("R+2");
        h3.add("W");
        upCard = "R9";
        calledColor = "";
        if (chooseBotCard(h3) == 1)
            passed++;
        else
            fail("bot prioritizes draw two");

        // bot prioritizes skip after draw two
        ArrayList<String> h4 = new ArrayList<String>();
        h4.add("R5");
        h4.add("RS");
        h4.add("W");
        upCard = "R9";
        calledColor = "";
        if (chooseBotCard(h4) == 1)
            passed++;
        else
            fail("bot prioritizes skip");

        // Restore global state
        playerNames.clear();
        playerNames.addAll(savedNames);
        humanPlayers.clear();
        humanPlayers.addAll(savedHumans);
        hands.clear();
        hands.addAll(savedHands);
        deck.clear();
        deck.addAll(savedDeck);
        discard.clear();
        discard.addAll(savedDiscard);
        direction = savedDir;
        currentPlayer = savedCurrent;
        upCard = savedUp;
        calledColor = savedCall;

        System.out.println("Passed " + passed + " characterization checks.");
    }

    static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}
