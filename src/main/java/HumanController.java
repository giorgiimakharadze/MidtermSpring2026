import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Human player controller. Reads decisions from console via Scanner.
 */
public class HumanController implements PlayerController {
    private final Scanner scanner;

    public HumanController(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public int chooseCard(List<String> hand, String upCard, String calledColor) {
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
            // Try matching by card code
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

    @Override
    public String chooseColor(List<String> hand) {
        while (true) {
            System.out.print("Call color R/Y/G/B: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("R")) return "R";
            if (input.equals("Y")) return "Y";
            if (input.equals("G")) return "G";
            if (input.equals("B")) return "B";
            System.out.println("Bad color.");
        }
    }

    @Override
    public boolean wantToPlayDrawnCard(String card) {
        System.out.print("Play drawn card " + card + "? y/n: ");
        String answer = scanner.nextLine();
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    @Override
    public boolean callUno() {
        System.out.print("Call UNO? y/n: ");
        String answer = scanner.nextLine();
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }
}
