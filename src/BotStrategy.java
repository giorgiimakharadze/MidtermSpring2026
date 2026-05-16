import java.util.ArrayList;

// Bot strategy
public class BotStrategy {

    static int chooseCard(ArrayList<String> hand, String upCard, String calledColor) {
        // play a legal draw two
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            if (CardRules.isLegal(card, upCard, calledColor) && CardRules.rank(card).equals("DRAW_TWO")) {
                return i;
            }
        }
        // play a legal skip
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            if (CardRules.isLegal(card, upCard, calledColor) && CardRules.rank(card).equals("SKIP")) {
                return i;
            }
        }
        // play a legal number card
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            if (CardRules.isLegal(card, upCard, calledColor) && CardRules.rank(card).equals("NUMBER")) {
                return i;
            }
        }
        // play a wild card
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).startsWith("W")) {
                return i;
            }
        }
        // No legal card found
        return -1;
    }

    static String chooseColor(ArrayList<String> hand) {
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
}
