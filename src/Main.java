import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static GameState gs = new GameState();
    static boolean quiet = false;
    static Scanner scanner = new Scanner(System.in);
    static GameView view;

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
                CharacterizationTests.runAll();
                return;
            } else if (args[i].equals("--help")) {
                new GameView(false, scanner).showHelp();
                return;
            }
        }

        gs.setRandom(new Random(seed));
        view = new GameView(quiet, scanner);
        gs.setupPlayers(bots, human);

        if (gs.getPlayerCount() < 2 || gs.getPlayerCount() > 4) {
            view.showPlayerCount();
            return;
        }

        for (int g = 1; g <= games; g++) {
            view.showGameHeader(g);
            playGame();
        }

        view.showFinalScores(gs.getPlayerNames(), gs.getScores());
    }

    static void playGame() {
        gs.buildDeck();
        gs.dealHands();
        gs.setUpCard(gs.draw());
        while (gs.getUpCard().startsWith("W")) {
            gs.addToDiscard(gs.getUpCard());
            gs.setUpCard(gs.draw());
        }
        gs.setCalledColor("");
        gs.resetForNewGame();

        int guard = 0;
        while (guard < 3000) {
            guard++;
            String name = gs.currentPlayerName();
            ArrayList<String> hand = gs.currentHand();

            view.showTurnState(gs.getUpCard(), gs.getCalledColor(), name, hand);

            int chosen = -1;
            if (gs.isCurrentPlayerHuman()) {
                chosen = view.askHumanCard(hand, gs.getUpCard(), gs.getCalledColor());
            } else {
                chosen = BotStrategy.chooseCard(hand, gs.getUpCard(), gs.getCalledColor());
            }

            if (chosen == -1) {
                String drawn = gs.draw();
                hand.add(drawn);
                view.showDraw(name, drawn);
                if (CardRules.isLegal(drawn, gs.getUpCard(), gs.getCalledColor())) {
                    if (!gs.isCurrentPlayerHuman()) {
                        chosen = hand.size() - 1;
                    } else {
                        if (view.askPlayDrawnCard(drawn)) {
                            chosen = hand.size() - 1;
                        }
                    }
                }
            }

            if (chosen >= 0) {
                if (chosen >= hand.size()) {
                    view.showInvalidIndex(name);
                    hand.add(gs.draw());
                    gs.next();
                    continue;
                }

                String card = hand.get(chosen);
                boolean ok = CardRules.isLegal(card, gs.getUpCard(), gs.getCalledColor());

                if (!ok) {
                    view.showPenalty(name, card);
                    hand.add(gs.draw());
                    gs.next();
                    continue;
                }

                hand.remove(chosen);
                gs.addToDiscard(gs.getUpCard());
                gs.setUpCard(card);
                gs.setCalledColor("");
                view.showPlay(name, card);

                if (card.equals("W") || card.equals("W4")) {
                    if (gs.isCurrentPlayerHuman()) {
                        gs.setCalledColor(view.askColor());
                    } else {
                        gs.setCalledColor(BotStrategy.chooseColor(hand));
                    }
                    view.showColorCall(name, gs.getCalledColor());
                }

                if (hand.size() == 1) {
                    view.showUno(name);
                }

                if (hand.size() == 0) {
                    int points = gs.calculateScore();
                    gs.addScore(gs.getCurrentPlayer(), points);
                    view.showWin(name, points);
                    return;
                }

                applyCardEffect(card);
            } else {
                gs.next();
            }
        }
        view.showSafetyLimit();
    }

    static void applyCardEffect(String card) {
        if (CardRules.rank(card).equals("SKIP")) {
            gs.next();
            gs.next();
        } else if (CardRules.rank(card).equals("REVERSE")) {
            gs.reverseDirection();
            if (gs.getPlayerCount() == 2) {
                gs.next();
                gs.next();
            } else {
                gs.next();
            }
        } else if (CardRules.rank(card).equals("DRAW_TWO")) {
            gs.next();
            gs.currentHand().add(gs.draw());
            gs.currentHand().add(gs.draw());
            view.showDrawTwo(gs.currentPlayerName());
            gs.next();
        } else if (CardRules.rank(card).equals("WILD_DRAW_FOUR")) {
            gs.next();
            for (int i = 0; i < 4; i++) {
                gs.currentHand().add(gs.draw());
            }
            view.showDrawFour(gs.currentPlayerName());
            gs.next();
        } else {
            gs.next();
        }
    }
}
