import java.util.List;

/**
 * Prints game events to console. Respects the quiet flag.
 * Replaces most of GameView's display methods.
 */
public class ConsoleEventListener implements GameEventListener {
    private final boolean quiet;

    public ConsoleEventListener(boolean quiet) {
        this.quiet = quiet;
    }

    @Override
    public void onGameStart(int gameNumber, int totalGames) {
        if (!quiet) {
            System.out.println("\n=== Game " + gameNumber + " of " + totalGames + " ===");
        }
    }

    @Override
    public void onRoundStart(int roundNumber) {
        if (!quiet) {
            System.out.println("\n--- Round " + roundNumber + " ---");
        }
    }

    @Override
    public void onTurnStart(String playerName, String upCard, String calledColor, List<String> hand) {
        if (!quiet) {
            System.out.println("\nUp card: " + upCard + (calledColor.isEmpty() ? "" : " called " + calledColor));
            System.out.println(playerName + " hand: " + formatHand(hand));
        }
    }

    @Override
    public void onCardPlayed(String playerName, String card) {
        if (!quiet) {
            System.out.println(playerName + " plays " + card);
        }
    }

    @Override
    public void onCardDrawn(String playerName, String card) {
        if (!quiet) {
            System.out.println(playerName + " draws " + card);
        }
    }

    @Override
    public void onColorCalled(String playerName, String color) {
        if (!quiet) {
            System.out.println(playerName + " calls " + color);
        }
    }

    @Override
    public void onUnoCall(String playerName) {
        if (!quiet) {
            System.out.println(playerName + " says UNO!");
        }
    }

    @Override
    public void onUnoPenalty(String playerName) {
        if (!quiet) {
            System.out.println(playerName + " failed to call UNO! Drawing 2 penalty cards.");
        }
    }

    @Override
    public void onSkip(String skippedPlayer) {
        if (!quiet) {
            System.out.println(skippedPlayer + " is skipped.");
        }
    }

    @Override
    public void onReverse() {
        if (!quiet) {
            System.out.println("Play direction reversed.");
        }
    }

    @Override
    public void onDrawTwo(String affectedPlayer) {
        if (!quiet) {
            System.out.println(affectedPlayer + " draws two.");
        }
    }

    @Override
    public void onDrawFour(String affectedPlayer) {
        if (!quiet) {
            System.out.println(affectedPlayer + " draws four.");
        }
    }

    @Override
    public void onIllegalPlay(String playerName, String card) {
        if (!quiet) {
            System.out.println(playerName + " tried illegal card " + card + " and draws a penalty card.");
        }
    }

    @Override
    public void onInvalidIndex(String playerName) {
        if (!quiet) {
            System.out.println(playerName + " selected an invalid index and draws a penalty card.");
        }
    }

    @Override
    public void onRoundEnd(String winnerName, int points) {
        if (!quiet) {
            System.out.println(winnerName + " wins and scores " + points);
        }
    }

    @Override
    public void onGameEnd(String winnerName, List<String> playerNames, int[] scores) {
        System.out.println("\nFinal scores:");
        for (int i = 0; i < playerNames.size(); i++) {
            System.out.println(playerNames.get(i) + ": " + scores[i]);
        }
        if (winnerName != null) {
            System.out.println("\nWinner: " + winnerName + "!");
        }
    }

    @Override
    public void onSafetyLimit() {
        if (!quiet) {
            System.out.println("Game stopped at safety limit.");
        }
    }

    @Override
    public void onPass(String playerName) {
        if (!quiet) {
            System.out.println(playerName + " passes.");
        }
    }

    private String formatHand(List<String> cards) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            out.append(i).append(":").append(cards.get(i));
            if (i < cards.size() - 1) {
                out.append(" ");
            }
        }
        return out.toString();
    }
}
