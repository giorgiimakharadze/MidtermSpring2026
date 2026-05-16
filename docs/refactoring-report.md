# Refactoring Report

## Characterized Behavior

Before refactoring, 46 characterization tests were added covering:

- Card color, rank, and number extraction
- Legal play matching by color, number, and action type
- Wild and wild draw four always being legal
- Called color matching after a wild is played
- Illegal play rejection (wrong color and number)
- Point values for all card types
- Skip effect: next player is skipped
- Reverse effect: direction flips, acts like skip with 2 players
- Draw two effect: victim receives 2 cards and loses their turn
- Drawing from deck, reshuffling discard pile, fallback wild when both piles empty
- Bot card selection priority (draw two > skip > number > wild)
- Bot color selection (picks most common color in hand)
- Bot returning -1 when no legal card is available

These tests run via `scripts/test.sh` and protect behavior during refactoring.

## Design Problems Found

The original code had several design problems:

1. **One monolithic class**: all game logic, rules, I/O, and bot strategy lived in a single Main class.
2. **Duplicated legality checks**: the legal-play logic was copy-pasted 4 times in the original code.
3. **Mixed console I/O and game logic**: System.out.println calls and if (!quiet) guards were scattered throughout the game loop.
4. **Bot decisions mixed with game flow**: bot card selection and color calling were embedded in Main.
5. **Long game loop**: playGame() was approximately 190 lines handling deck setup, dealing, turn flow, scoring, and card effects.
6. **Global mutable state**: all game data lived as static fields in Main.

## Refactorings Performed

Each refactoring step preserved all 46 characterization tests.

### Step 1: Extract CardRules class

Moved color(), rank(), number(), points(), and isLegal() into CardRules.java. Replaced the 4 duplicated inline legality checks with calls to CardRules.isLegal(). The chooseBotCard() method went from 50+ lines of duplicated checks to 3 clean loops using CardRules.isLegal().

### Step 2: Extract GameView class

Moved all System.out.println and System.out.print calls into GameView.java. Moved human input methods (askHuman, askColor) into GameView. Moved join() (hand formatting) into GameView.formatHand(). The quiet flag is now handled entirely inside GameView.

### Step 3: Extract methods from game loop

Extracted buildDeck(), dealHands(), calculateScore(), and applyCardEffect() from the playGame() method. The game loop now reads as a clear turn-by-turn flow.

### Step 4: Extract BotStrategy class

Moved chooseBotCard() and chooseBotColor() into BotStrategy.java. Bot methods now take explicit parameters (hand, upCard, calledColor) instead of reading global state, making them independently testable.

### Step 5: Extract GameState class

Moved all mutable game state (playerNames, humanPlayers, hands, deck, discard, scores, currentPlayer, direction, upCard, calledColor) into GameState.java with private fields and accessor methods. Also moved state-manipulation methods (setupPlayers, buildDeck, dealHands, draw, next, calculateScore) into GameState. Main now holds only game orchestration and the test harness.

## Final Class Structure

| Class | Responsibility |
|-------|---------------|
| Main | Turn orchestration, CLI argument parsing, test harness |
| GameState | All mutable game state and state-manipulation methods |
| CardRules | Card rules: color, rank, number, points, legality |
| GameView | All console output and human input |
| BotStrategy | Bot card selection and color calling |

## Preserved Behavior

All existing behavior was preserved, including:

- Humans can type draw even when holding a legal card
- Illegal index input causes a penalty card and turn loss
- Typing a card code for an illegal card re-prompts (does not penalize)
- Bot players automatically play drawn cards when legal
- UNO is announced when a player has one card left
- Reverse acts like skip with 2 players
- Safety limit at 3000 turns
- Fallback wild card when both deck and discard are empty

No behavior changes were made during refactoring.

## Remaining Risks

- Card representation is still primitive strings. A Card value object would prevent invalid card states.
- Tests use a manual selfTest() pattern rather than a proper test framework.
- applyCardEffect still lives in Main rather than in GameState, because it mixes state changes with view calls.
