import java.util.ArrayList;
import java.util.Scanner;

// Handles all console I/O for the game.
public class GameView {
    private boolean quiet;
    private Scanner scanner;

    GameView(boolean quiet, Scanner scanner) {
        this.quiet = quiet;
        this.scanner = scanner;
    }

    void showGameHeader(int gameNumber) {
        if (!quiet) {
            System.out.println("\n=== Game " + gameNumber + " ===");
        }
    }

    void showTurnState(String upCard, String calledColor, String playerName, ArrayList<String> hand) {
        if (!quiet) {
            System.out.println("\nUp card: " + upCard + (calledColor.equals("") ? "" : " called " + calledColor));
            System.out.println(playerName + " hand: " + formatHand(hand));
        }
    }

    void showDraw(String playerName, String card) {
        if (!quiet) {
            System.out.println(playerName + " draws " + card);
        }
    }

    void showPlay(String playerName, String card) {
        if (!quiet) {
            System.out.println(playerName + " plays " + card);
        }
    }

    void showColorCall(String playerName, String color) {
        if (!quiet) {
            System.out.println(playerName + " calls " + color);
        }
    }

    void showUno(String playerName) {
        if (!quiet) {
            System.out.println(playerName + " says UNO!");
        }
    }

    void showWin(String playerName, int points) {
        if (!quiet) {
            System.out.println(playerName + " wins and scores " + points);
        }
    }

    void showPenalty(String playerName, String card) {
        if (!quiet) {
            System.out.println(playerName + " tried illegal card " + card + " and draws a penalty card.");
        }
    }

    void showInvalidIndex(String playerName) {
        if (!quiet) {
            System.out.println(playerName + " selected an invalid index and draws a penalty card.");
        }
    }

    void showDrawTwo(String playerName) {
        if (!quiet) {
            System.out.println(playerName + " draws two.");
        }
    }

    void showDrawFour(String playerName) {
        if (!quiet) {
            System.out.println(playerName + " draws four.");
        }
    }

    void showSafetyLimit() {
        if (!quiet) {
            System.out.println("Game stopped at safety limit.");
        }
    }

    void showFinalScores(ArrayList<String> playerNames, int[] scores) {
        System.out.println("\nFinal scores:");
        for (int i = 0; i < playerNames.size(); i++) {
            System.out.println(playerNames.get(i) + ": " + scores[i]);
        }
    }

    void showPlayerCount() {
        System.out.println("UNO needs 2 to 4 players.");
    }

    void showHelp() {
        System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
    }

    int askHumanCard(ArrayList<String> hand, String upCard, String calledColor) {
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

    String askColor() {
        while (true) {
            System.out.print("Call color R/Y/G/B: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("R"))
                return "R";
            if (input.equals("Y"))
                return "Y";
            if (input.equals("G"))
                return "G";
            if (input.equals("B"))
                return "B";
            System.out.println("Bad color.");
        }
    }

    boolean askPlayDrawnCard(String card) {
        System.out.print("Play drawn card " + card + "? y/n: ");
        String answer = scanner.nextLine();
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    static String formatHand(ArrayList<String> cards) {
        String out = "";
        for (int i = 0; i < cards.size(); i++) {
            out += i + ":" + cards.get(i);
            if (i < cards.size() - 1) {
                out += " ";
            }
        }
        return out;
    }
}
