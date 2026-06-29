import java.util.List;

/**
 * Receives game event notifications. Separates display/logging from game logic.
 * Implemented by ConsoleEventListener for CLI output.
 */
public interface GameEventListener {

    void onGameStart(int gameNumber, int totalGames);

    void onRoundStart(int roundNumber);

    void onTurnStart(String playerName, String upCard, String calledColor, List<String> hand);

    void onCardPlayed(String playerName, String card);

    void onCardDrawn(String playerName, String card);

    void onColorCalled(String playerName, String color);

    void onUnoCall(String playerName);

    void onUnoPenalty(String playerName);

    void onSkip(String skippedPlayer);

    void onReverse();

    void onDrawTwo(String affectedPlayer);

    void onDrawFour(String affectedPlayer);

    void onIllegalPlay(String playerName, String card);

    void onInvalidIndex(String playerName);

    void onRoundEnd(String winnerName, int points);

    void onGameEnd(String winnerName, List<String> playerNames, int[] scores);

    void onSafetyLimit();

    void onPass(String playerName);
}
