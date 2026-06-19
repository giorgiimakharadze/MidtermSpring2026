import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    static GameState gs = new GameState();
    static boolean quiet = false;
    static Scanner scanner = new Scanner(System.in);
    static GameView view;
    static persistence.GameRepository repo;
    static persistence.Game dbGame;
    static ArrayList<persistence.Player> dbPlayers;

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
            } else if (args[i].equals("--help")) {
                new GameView(false, scanner).showHelp();
                System.out.println("  --report        Show game history report");
                return;
            } else if (args[i].equals("--report")) {
                showReport();
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

        repo = new persistence.GameRepository();
        dbPlayers = new ArrayList<>();
        for (String name : gs.getPlayerNames()) {
            dbPlayers.add(repo.getOrCreatePlayer(name));
        }
        dbGame = repo.createGame();

        for (int g = 1; g <= games; g++) {
            log.info("Starting game {} of {}", g, games);
            view.showGameHeader(g);
            playGame(g);
        }

        view.showFinalScores(gs.getPlayerNames(), gs.getScores());

        persistence.Player overallWinner = null;
        int maxScore = -1;
        for (int i = 0; i < gs.getPlayerCount(); i++) {
            if (gs.getScores()[i] > maxScore) {
                maxScore = gs.getScores()[i];
                overallWinner = dbPlayers.get(i);
            }
        }
        repo.finishGame(dbGame, overallWinner);
        repo.close();
    }

    static void showReport() {
        System.out.println("=== UNO Game Report ===");
        persistence.GameRepository reportRepo = new persistence.GameRepository();
        
        System.out.println("\n--- Recent Games ---");
        for (persistence.Game g : reportRepo.getRecentGames(5)) {
            String winnerName = (g.getWinner() != null) ? g.getWinner().getName() : "None";
            System.out.printf("Game %d | Start: %s | Winner: %s%n", g.getId(), g.getStartTime(), winnerName);
        }

        System.out.println("\n--- Player Win Counts ---");
        for (Object[] row : reportRepo.getPlayerWinCounts()) {
            System.out.printf("%s: %s wins%n", row[0], row[1]);
        }

        System.out.println("\n--- Highest Single-Round Scores ---");
        for (Object[] row : reportRepo.getHighestScores(5)) {
            System.out.printf("%s: %s points%n", row[0], row[1]);
        }
        
        reportRepo.close();
    }

    static void playGame(int roundNumber) {
        persistence.Round dbRound = repo.createRound(dbGame, roundNumber);
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
                log.info("{} drew a card", name);
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
                log.info("{} played {}", name, card);
                view.showPlay(name, card);

                if (card.equals("W") || card.equals("W4")) {
                    if (gs.isCurrentPlayerHuman()) {
                        gs.setCalledColor(view.askColor());
                    } else {
                        gs.setCalledColor(BotStrategy.chooseColor(hand));
                    }
                    log.info("{} called color {}", name, gs.getCalledColor());
                    view.showColorCall(name, gs.getCalledColor());
                }

                if (hand.size() == 1) {
                    log.info("{} called UNO!", name);
                    view.showUno(name);
                }

                if (hand.size() == 0) {
                    int points = gs.calculateScore();
                    gs.addScore(gs.getCurrentPlayer(), points);
                    log.info("{} won the game with {} points!", name, points);
                    view.showWin(name, points);

                    persistence.Player roundWinner = dbPlayers.get(gs.getCurrentPlayer());
                    repo.finishRound(dbRound, roundWinner);
                    for (int i = 0; i < dbPlayers.size(); i++) {
                        int roundScore = (i == gs.getCurrentPlayer()) ? points : 0;
                        repo.saveScore(dbRound, dbPlayers.get(i), roundScore);
                    }
                    return;
                }

                applyCardEffect(card);
            } else {
                gs.next();
            }
        }
        view.showSafetyLimit();
        repo.finishRound(dbRound, null);
        for (int i = 0; i < dbPlayers.size(); i++) {
            repo.saveScore(dbRound, dbPlayers.get(i), 0);
        }
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
