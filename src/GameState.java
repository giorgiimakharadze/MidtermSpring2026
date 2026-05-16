import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

// Game State
public class GameState {
    private ArrayList<String> playerNames = new ArrayList<String>();
    private ArrayList<Boolean> humanPlayers = new ArrayList<Boolean>();
    private ArrayList<ArrayList<String>> hands = new ArrayList<ArrayList<String>>();
    private ArrayList<String> deck = new ArrayList<String>();
    private ArrayList<String> discard = new ArrayList<String>();
    private int[] scores = new int[10];
    private int currentPlayer = 0;
    private int direction = 1;
    private String upCard = "";
    private String calledColor = "";
    private Random random = new Random();

    int getPlayerCount() {
        return playerNames.size();
    }

    String getPlayerName(int index) {
        return playerNames.get(index);
    }

    boolean isHuman(int index) {
        return humanPlayers.get(index).booleanValue();
    }

    ArrayList<String> getPlayerNames() {
        return playerNames;
    }

    int getCurrentPlayer() {
        return currentPlayer;
    }

    String currentPlayerName() {
        return playerNames.get(currentPlayer);
    }

    ArrayList<String> currentHand() {
        return hands.get(currentPlayer);
    }

    boolean isCurrentPlayerHuman() {
        return humanPlayers.get(currentPlayer).booleanValue();
    }

    String getUpCard() {
        return upCard;
    }

    void setUpCard(String card) {
        this.upCard = card;
    }

    String getCalledColor() {
        return calledColor;
    }

    void setCalledColor(String color) {
        this.calledColor = color;
    }

    int getDirection() {
        return direction;
    }

    void reverseDirection() {
        direction = direction * -1;
    }

    int[] getScores() {
        return scores;
    }

    void addScore(int player, int points) {
        scores[player] += points;
    }

    ArrayList<String> getHand(int index) {
        return hands.get(index);
    }

    void setRandom(Random r) {
        this.random = r;
    }

    int randomPlayerIndex() {
        return random.nextInt(playerNames.size());
    }

    void resetForNewGame() {
        direction = 1;
        currentPlayer = random.nextInt(playerNames.size());
    }

    void addToDeck(String card) {
        deck.add(card);
    }

    void addToDiscard(String card) {
        discard.add(card);
    }

    void setupPlayers(int bots, boolean human) {
        playerNames.clear();
        humanPlayers.clear();
        hands.clear();
        if (human) {
            playerNames.add("You");
            humanPlayers.add(Boolean.TRUE);
            hands.add(new ArrayList<String>());
        }
        for (int i = 1; i <= bots; i++) {
            playerNames.add("Bot" + i);
            humanPlayers.add(Boolean.FALSE);
            hands.add(new ArrayList<String>());
        }
    }

    void addPlayer(String name, boolean isHuman) {
        playerNames.add(name);
        humanPlayers.add(isHuman);
        hands.add(new ArrayList<String>());
    }

    void buildDeck() {
        deck.clear();
        String[] colors = { "R", "Y", "G", "B" };
        for (int c = 0; c < colors.length; c++) {
            deck.add(colors[c] + "0");
            for (int n = 1; n <= 9; n++) {
                deck.add(colors[c] + n);
                deck.add(colors[c] + n);
            }
            deck.add(colors[c] + "S");
            deck.add(colors[c] + "S");
            deck.add(colors[c] + "R");
            deck.add(colors[c] + "R");
            deck.add(colors[c] + "+2");
            deck.add(colors[c] + "+2");
        }
        for (int i = 0; i < 4; i++) {
            deck.add("W");
            deck.add("W4");
        }
        Collections.shuffle(deck, random);
        discard.clear();
    }

    void dealHands() {
        for (int i = 0; i < hands.size(); i++) {
            hands.get(i).clear();
        }
        for (int i = 0; i < playerNames.size(); i++) {
            for (int j = 0; j < 7; j++) {
                hands.get(i).add(draw());
            }
        }
    }

    String draw() {
        if (deck.size() == 0) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.size() == 0) {
            return "W";
        }
        return deck.remove(0);
    }

    void next() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) {
            currentPlayer = 0;
        }
        if (currentPlayer < 0) {
            currentPlayer = playerNames.size() - 1;
        }
    }

    int calculateScore() {
        int points = 0;
        for (int i = 0; i < hands.size(); i++) {
            if (i != currentPlayer) {
                for (int j = 0; j < hands.get(i).size(); j++) {
                    points += CardRules.points(hands.get(i).get(j));
                }
            }
        }
        return points;
    }
}
