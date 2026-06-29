import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core game engine that manages UNO game logic without any console dependency.
 * All player decisions come through PlayerController, all events go through GameEventListener.
 */
public class GameEngine {
    private static final Logger log = LoggerFactory.getLogger(GameEngine.class);

    private final GameState gs;
    private final PlayerController[] controllers;
    private final GameEventListener listener;

    // Per-round tracking
    private boolean roundOver;
    private int roundWinner; // player index, or -1

    public GameEngine(GameState gs, PlayerController[] controllers, GameEventListener listener) {
        this.gs = gs;
        this.controllers = controllers;
        this.listener = listener;
        this.roundOver = false;
        this.roundWinner = -1;
    }

    /**
     * Play a full multi-round game until a player reaches the target score.
     * Returns the index of the overall winner.
     */
    public int playGame(int targetScore, int maxRounds) {
        for (int round = 1; round <= maxRounds; round++) {
            listener.onRoundStart(round);
            log.info("Starting round {} (target score: {})", round, targetScore);
            playRound();

            // Check if any player reached the target score
            for (int i = 0; i < gs.getPlayerCount(); i++) {
                if (gs.getScores()[i] >= targetScore) {
                    // Find the player with the highest score
                    int bestPlayer = 0;
                    for (int j = 1; j < gs.getPlayerCount(); j++) {
                        if (gs.getScores()[j] > gs.getScores()[bestPlayer]) {
                            bestPlayer = j;
                        }
                    }
                    listener.onGameEnd(gs.getPlayerName(bestPlayer),
                            gs.getPlayerNames(), gs.getScores());
                    return bestPlayer;
                }
            }
        }

        // Max rounds reached — winner is highest score
        int bestPlayer = 0;
        for (int j = 1; j < gs.getPlayerCount(); j++) {
            if (gs.getScores()[j] > gs.getScores()[bestPlayer]) {
                bestPlayer = j;
            }
        }
        listener.onGameEnd(gs.getPlayerName(bestPlayer),
                gs.getPlayerNames(), gs.getScores());
        return bestPlayer;
    }

    /**
     * Play a single round. The round ends when a player empties their hand
     * or the safety limit is reached.
     */
    public void playRound() {
        roundOver = false;
        roundWinner = -1;

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
        while (guard < 3000 && !roundOver) {
            guard++;
            playTurn();
        }

        if (!roundOver) {
            log.warn("Round stopped at safety limit");
            listener.onSafetyLimit();
        }
    }

    /**
     * Play a single turn for the current player.
     */
    void playTurn() {
        String name = gs.currentPlayerName();
        ArrayList<String> hand = gs.currentHand();
        int playerIndex = gs.getCurrentPlayer();
        PlayerController ctrl = controllers[playerIndex];

        log.info("Turn started for player: {}", name);
        listener.onTurnStart(name, gs.getUpCard(), gs.getCalledColor(), hand);

        int chosen = ctrl.chooseCard(hand, gs.getUpCard(), gs.getCalledColor());

        // Draw phase
        if (chosen == -1) {
            String drawn = gs.draw();
            hand.add(drawn);
            log.info("{} drew a card", name);
            listener.onCardDrawn(name, drawn);

            if (CardRules.isLegal(drawn, gs.getUpCard(), gs.getCalledColor())) {
                if (ctrl.wantToPlayDrawnCard(drawn)) {
                    chosen = hand.size() - 1;
                }
            }

            if (chosen == -1) {
                listener.onPass(name);
                gs.next();
                return;
            }
        }

        // Play phase
        if (chosen >= hand.size()) {
            log.warn("{} entered an invalid card index", name);
            listener.onInvalidIndex(name);
            hand.add(gs.draw());
            gs.next();
            return;
        }

        String card = hand.get(chosen);
        boolean ok = CardRules.isLegal(card, gs.getUpCard(), gs.getCalledColor());

        if (!ok) {
            log.warn("{} attempted to play an illegal card: {}", name, card);
            listener.onIllegalPlay(name, card);
            hand.add(gs.draw());
            gs.next();
            return;
        }

        // Valid play
        hand.remove(chosen);
        gs.addToDiscard(gs.getUpCard());
        gs.setUpCard(card);
        gs.setCalledColor("");
        log.info("{} played {}", name, card);
        listener.onCardPlayed(name, card);

        // Wild color selection
        if (card.equals("W") || card.equals("W4")) {
            String color = ctrl.chooseColor(hand);
            gs.setCalledColor(color);
            log.info("{} called color {}", name, color);
            listener.onColorCalled(name, color);
        }

        // UNO call check
        if (hand.size() == 1) {
            boolean called = ctrl.callUno();
            if (called) {
                log.info("{} called UNO!", name);
                listener.onUnoCall(name);
            } else {
                // Missed UNO penalty: draw 2 cards
                log.warn("{} failed to call UNO! Drawing 2 penalty cards.", name);
                listener.onUnoPenalty(name);
                hand.add(gs.draw());
                hand.add(gs.draw());
            }
        }

        // Round end check
        if (hand.size() == 0) {
            int points = gs.calculateScore();
            gs.addScore(playerIndex, points);
            log.info("{} won the round with {} points!", name, points);
            listener.onRoundEnd(name, points);
            roundOver = true;
            roundWinner = playerIndex;
            return;
        }

        // Apply card effects
        applyCardEffect(card);
    }

    /**
     * Apply the effect of an action card.
     */
    void applyCardEffect(String card) {
        String rank = CardRules.rank(card);
        if (rank.equals("SKIP")) {
            gs.next();
            String skippedName = gs.currentPlayerName();
            log.info("{} is skipped", skippedName);
            listener.onSkip(skippedName);
            gs.next();
        } else if (rank.equals("REVERSE")) {
            gs.reverseDirection();
            log.info("Play direction reversed");
            listener.onReverse();
            if (gs.getPlayerCount() == 2) {
                gs.next();
                String skippedName = gs.currentPlayerName();
                log.info("{} is skipped (reverse in 2-player)", skippedName);
                listener.onSkip(skippedName);
                gs.next();
            } else {
                gs.next();
            }
        } else if (rank.equals("DRAW_TWO")) {
            gs.next();
            String affected = gs.currentPlayerName();
            gs.currentHand().add(gs.draw());
            gs.currentHand().add(gs.draw());
            log.info("{} is forced to draw two cards", affected);
            listener.onDrawTwo(affected);
            gs.next();
        } else if (rank.equals("WILD_DRAW_FOUR")) {
            gs.next();
            String affected = gs.currentPlayerName();
            for (int i = 0; i < 4; i++) {
                gs.currentHand().add(gs.draw());
            }
            log.info("{} is forced to draw four cards", affected);
            listener.onDrawFour(affected);
            gs.next();
        } else {
            gs.next();
        }
    }

    // --- Getters for test inspection ---

    public boolean isRoundOver() {
        return roundOver;
    }

    public int getRoundWinner() {
        return roundWinner;
    }

    public GameState getGameState() {
        return gs;
    }
}
