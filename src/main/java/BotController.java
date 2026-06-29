import java.util.ArrayList;
import java.util.List;

/**
 * Bot player controller. Delegates card/color choices to BotStrategy.
 * Bots always call UNO and always play legal drawn cards.
 */
public class BotController implements PlayerController {

    @Override
    public int chooseCard(List<String> hand, String upCard, String calledColor) {
        return BotStrategy.chooseCard(new ArrayList<>(hand), upCard, calledColor);
    }

    @Override
    public String chooseColor(List<String> hand) {
        return BotStrategy.chooseColor(new ArrayList<>(hand));
    }

    @Override
    public boolean wantToPlayDrawnCard(String card) {
        return true;
    }

    @Override
    public boolean callUno() {
        return true;
    }
}
