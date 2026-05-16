# Extension Readiness

## Realistic Extension: Smarter Bot Strategies

The current design supports adding new bot strategies with minimal changes.

### Where the design is ready

BotStrategy is already a separate class with two static methods:

- chooseCard(hand, upCard, calledColor) returns the index of the card to play
- chooseColor(hand) returns the color to call after playing a wild

To add a new strategy (for example, a bot that prioritizes getting rid of high-point cards first), you would:

1. Create a new class (e.g. AggressiveBotStrategy) with the same method signatures.
2. Change the two call sites in Main.playGame() to use the new class.

No other files need to change. CardRules, GameView, and GameState are completely unaffected.

### Where the design still resists change

- Bot strategy methods are static. To support multiple bots with different strategies in the same game, you would need to convert BotStrategy to an interface or abstract class and associate a strategy instance with each player.
- The player model (playerNames, humanPlayers) uses parallel ArrayLists. Adding per-player strategy selection would require either a Player class or another parallel list, which is fragile.
- applyCardEffect in Main mixes state changes with view calls. If a new card type were added (for example, a swap-hands card), both Main.applyCardEffect and CardRules.rank would need updates. Moving effect application into GameState would reduce this coupling.

## Other Plausible Extensions

### Adding a new card type

CardRules.rank() would need a new return value. CardRules.points() and CardRules.isLegal() would need new cases. GameState.buildDeck() would need to include the new cards. Main.applyCardEffect() would need a new branch for the effect. The separation of concerns means each change is localized to one class.

### Network multiplayer

GameView already separates all console I/O from game logic. Replacing GameView with a network-aware version that sends state over sockets and receives commands would be feasible without changing GameState, CardRules, or BotStrategy. The main challenge would be replacing the synchronous Scanner-based input with asynchronous message handling.
