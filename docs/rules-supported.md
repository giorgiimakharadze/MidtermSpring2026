# UNO Rules Supported

This document outlines the UNO rules supported by this project, mapping to the `Final_Project_UNO_rules_reference.md` document.

## 1. Deck Composition
- **Implemented:** Yes (108 cards)
- **Details:** The deck consists of 4 colors (Red, Yellow, Green, Blue). Each color has one `0` card, two `1-9` cards, two `Skip`, two `Reverse`, and two `Draw Two` cards. There are 4 `Wild` and 4 `Wild Draw Four` cards.

## 2. Basic Turn Flow
- **Implemented:** Yes
- **Details:** Players start with 7 cards. On a turn, a player plays a legal card. If they cannot play, they draw a card. Turn passes if no legal play is made.

## 3. Legal Play Validation
- **Implemented:** Yes
- **Details:** A card is legal if it matches the current active color, number, or action type of the top card. Wild cards and Wild Draw Four cards are always legal to play. When a wild card is played, the active color is updated.

## 4. Skip
- **Implemented:** Yes
- **Details:** When played, the next player is skipped and loses their turn.

## 5. Reverse
- **Implemented:** Yes
- **Details:** Play direction reverses. **Variant:** In a two-player game, a `Reverse` card acts exactly like a `Skip` card.

## 6. Draw Two
- **Implemented:** Yes
- **Details:** The next player draws two cards and loses their turn.
- **Simplifications:** Stacking Draw Two cards is not supported.

## 7. Wild
- **Implemented:** Yes
- **Details:** The player chooses the next active color. Play continues normally with the next player.

## 8. Wild Draw Four
- **Implemented:** Yes
- **Details:** The player chooses the next active color. The next player draws four cards and loses their turn.
- **Simplifications:** Wild Draw Four challenge rules are not implemented.

## 9. Draw And Pass Behavior
- **Implemented:** Yes
- **Details:** A player can choose to draw instead of playing. If the drawn card is legal, the player is asked if they want to play it immediately. If they decline or the card is illegal, the turn passes.

## 10. UNO Call And Missed-UNO Penalty
- **Implemented:** Yes
- **Details:** When a player reaches 1 card, they must call UNO. 
- **Variant:** Bots automatically call UNO. Human players are prompted ("Call UNO? y/n"). If a player fails to call UNO, they immediately draw a 2-card penalty.

## 11. Round End & Scoring
- **Implemented:** Yes
- **Details:** The round ends when a player empties their hand. The winner scores points based on the cards left in other players' hands (Number cards = face value, Action cards = 20, Wilds = 50).

## 12. Multi-Round Game Target
- **Implemented:** Yes
- **Details:** The game continues across multiple rounds until a player reaches a target score (default `500`). The winner is the player with the highest score at that point.
