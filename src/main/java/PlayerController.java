import java.util.List;

/**
 * Abstracts player decisions so game logic can run without console I/O.
 * Implemented by BotController (automated) and HumanController (console input).
 */
public interface PlayerController {

    /**
     * Choose a card index to play from the hand, or -1 to draw.
     */
    int chooseCard(List<String> hand, String upCard, String calledColor);

    /**
     * Choose a color after playing a Wild or Wild Draw Four.
     * Returns "R", "Y", "G", or "B".
     */
    String chooseColor(List<String> hand);

    /**
     * After drawing a card that is legal to play, decide whether to play it.
     */
    boolean wantToPlayDrawnCard(String card);

    /**
     * When the player has one card left, decide whether to call UNO.
     * Returning false risks a penalty.
     */
    boolean callUno();
}
