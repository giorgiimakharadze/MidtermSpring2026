# Midterm UNO CLI

This is a standalone CLI UNO-like game.

The code has been refactored into a `GameEngine` architecture that separates game logic from CLI interaction. Rules and game state are completely testable without any console dependencies, via the `PlayerController` and `GameEventListener` interfaces.

## Build and Package

This project uses Maven for building, testing, and packaging.

```bash
mvn clean package
```

This will run the automated JUnit 5 tests and create an executable JAR file in `target/uno-game-1.0-SNAPSHOT-jar-with-dependencies.jar`.

## Run Tests

To run the characterization tests:

```bash
mvn test
```

## Run the Game

Once packaged, you can run the executable JAR:

### Run Bot Games to a Target Score

```bash
java -jar target/uno-game-1.0-SNAPSHOT-jar-with-dependencies.jar --bots 3 --target 500 --quiet
```

### Run Interactive Game

```bash
java -jar target/uno-game-1.0-SNAPSHOT-jar-with-dependencies.jar --human --bots 2 --games 1
```

Card input examples:

```text
R5   red 5
YS   yellow skip
BR   blue reverse
G+2  green draw two
W    wild
W4   wild draw four
draw draw a card
```

## Logging

Gameplay events are logged using SLF4J and Logback to `logs/game.log`. Check this file to see detailed execution logs during or after gameplay.

## Docker Support

You can build and run the game using Docker.

### Build Docker Image

```bash
docker build -t uno-game .
```

### Run via Docker

```bash
# Interactive game
docker run -it uno-game --human --bots 2 --games 1

# Bot only game
docker run -it uno-game --bots 3 --games 5
```

## Documentation

* `docs/rules-supported.md`: Details of implemented UNO rules and variants.
* `docs/final-report.md`: Project summary report covering architecture, testing, and limitations.
* `docs/rules.html`: Original basic rules.

## Persistence

See `docs/database.md` for information on the database schema, history reporting, and persistence testing.

## Midterm Materials

* `docs/midterm-exam.md`: midterm brief
* `docs/rubric.md`: grading rubric
* `docs/refactoring-guide.md`: suggested refactoring path
