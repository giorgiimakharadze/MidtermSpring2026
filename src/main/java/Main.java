import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int bots = 3;
        int maxRounds = 100;
        boolean human = false;
        boolean quiet = false;
        long seed = System.currentTimeMillis();
        int targetScore = 500;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--bots") && i + 1 < args.length) {
                bots = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--games") && i + 1 < args.length) {
                maxRounds = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--target") && i + 1 < args.length) {
                targetScore = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--human")) {
                human = true;
            } else if (args[i].equals("--quiet")) {
                quiet = true;
            } else if (args[i].equals("--seed") && i + 1 < args.length) {
                seed = Long.parseLong(args[++i]);
            } else if (args[i].equals("--help")) {
                showHelp();
                return;
            } else if (args[i].equals("--report")) {
                showReport();
                return;
            }
        }

        GameState gs = new GameState();
        gs.setRandom(new Random(seed));
        gs.setupPlayers(bots, human);

        if (gs.getPlayerCount() < 2 || gs.getPlayerCount() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        // Build controllers
        PlayerController[] controllers = new PlayerController[gs.getPlayerCount()];
        for (int i = 0; i < gs.getPlayerCount(); i++) {
            if (gs.isHuman(i)) {
                controllers[i] = new HumanController(scanner);
            } else {
                controllers[i] = new BotController();
            }
        }

        // Persistence setup
        persistence.GameRepository repo = new persistence.GameRepository();
        ArrayList<persistence.Player> dbPlayers = new ArrayList<>();
        for (String name : gs.getPlayerNames()) {
            dbPlayers.add(repo.getOrCreatePlayer(name));
        }
        persistence.Game dbGame = repo.createGame();

        // Build event listener
        ConsoleEventListener listener = new ConsoleEventListener(quiet) {
            private persistence.Round currentRound;

            @Override
            public void onRoundStart(int roundNumber) {
                super.onRoundStart(roundNumber);
                currentRound = repo.createRound(dbGame, roundNumber);
            }

            @Override
            public void onRoundEnd(String winnerName, int points) {
                super.onRoundEnd(winnerName, points);
                persistence.Player roundWinner = null;
                for (persistence.Player p : dbPlayers) {
                    if (p.getName().equals(winnerName)) {
                        roundWinner = p;
                        break;
                    }
                }
                repo.finishRound(currentRound, roundWinner);
                if (roundWinner != null) {
                    repo.saveScore(currentRound, roundWinner, points);
                }
            }

            @Override
            public void onSafetyLimit() {
                super.onSafetyLimit();
                if (currentRound != null) {
                    repo.finishRound(currentRound, null);
                }
            }
        };

        // Build and run engine
        GameEngine engine = new GameEngine(gs, controllers, listener);

        // Play game with target score
        log.info("Starting game with target score {} and max {} rounds", targetScore, maxRounds);
        int overallWinner = engine.playGame(targetScore, maxRounds);

        // Persist results
        persistence.Player winner = dbPlayers.get(overallWinner);
        repo.finishGame(dbGame, winner);
        repo.close();
    }

    static void showHelp() {
        System.out.println("Usage: java -jar uno-game.jar [options]");
        System.out.println("  --bots N        Number of bot players (default: 3)");
        System.out.println("  --games N       Maximum number of rounds (default: 100)");
        System.out.println("  --target N      Target score to win (default: 500)");
        System.out.println("  --human         Add a human player");
        System.out.println("  --quiet         Suppress game output");
        System.out.println("  --seed N        Random seed for reproducibility");
        System.out.println("  --report        Show game history report");
        System.out.println("  --help          Show this help");
        System.out.println();
        System.out.println("Card input examples:");
        System.out.println("  R5   red 5");
        System.out.println("  YS   yellow skip");
        System.out.println("  BR   blue reverse");
        System.out.println("  G+2  green draw two");
        System.out.println("  W    wild");
        System.out.println("  W4   wild draw four");
        System.out.println("  draw draw a card");
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
}
