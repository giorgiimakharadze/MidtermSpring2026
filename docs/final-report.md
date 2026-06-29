# Final Report

## Rules Implemented
I have successfully implemented the full suite of required UNO rules defined in the rubric, including:
- Correct 108-card deck composition
- Full legal play validation (color, number, action type, wild)
- Skip, Reverse, Draw Two, Wild, and Wild Draw Four card effects
- Draw and pass behavior (with the option to play a legally drawn card)
- UNO calls and 2-card penalties for missed calls
- End-of-round point scoring based on remaining hands
- Multi-round play until a target score (default 500) is reached

See `rules-supported.md` for a full breakdown and variants used (e.g., Reverse acts as Skip in 2-player games).

## CLI Playability
The game is completely playable from the command line.
You can run it with: `java -jar target/uno-game-1.0-SNAPSHOT-jar-with-dependencies.jar --human`

Humans use simple console input:
- Card selection uses either the card's index in the hand (`0`, `1`, `2`) or the card string code (`R5`, `YS`, `W4`).
- Play can be bypassed by typing `DRAW`.
- Wild color choices prompt specifically for `R`/`Y`/`G`/`B`.
- UNO calls are triggered via a `y/n` prompt when reaching 1 card.
- Invalid input is handled safely via a retry loop.

## Game Architecture & Organization
The most significant architectural change was extracting the game loop from `Main.java` into a standalone `GameEngine` class.
- **Rule Execution**: `GameEngine` now owns the loop, turn handling, rule validation (via `CardRules`), and game state updates.
- **I/O Separation**: 
  - Player decisions are abstracted via the `PlayerController` interface, with `BotController` and `HumanController` implementations.
  - Game events (turns, plays, draws, wins) are broadcast via the `GameEventListener` interface.
  - The CLI display is handled purely by `ConsoleEventListener`.
- **Benefit**: This guarantees that **game logic is 100% testable without console input**, satisfying the design requirements of the rubric. `Main.java` only handles CLI argument parsing, dependency wiring, and high-level persistence reporting.

## Testing Strategy
The separation of the `GameEngine` enabled the addition of comprehensive automated tests.
- We added `GameEngineTest.java` with 24 dedicated tests covering every section of the rubric (Deck, Legal Play, Skips, Reverses, Draw Two, Wild, Wild4, Draw/Pass, UNO Call/Penalty, Scoring, and Multi-round).
- These tests use a `TestPlayerController` to script specific card plays and verify the engine's responses.
- The pre-existing `CharacterizationTests.java` (10 tests) and persistence tests (3 tests) were retained and continue to pass. 
- In total, 37 automated tests enforce the correctness of the codebase.

## Remaining Limitations
- **Stacking**: Draw Two stacking and Wild Draw Four challenge rules are not implemented.
- **UI**: The interface is purely text-based CLI. No graphical elements or TUI elements (like ncurses) are used.
- **Save States**: Mid-game pausing and resuming is not natively supported by the core engine; persistence only tracks finished rounds and overall scores.
