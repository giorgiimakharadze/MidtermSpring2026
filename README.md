# Midterm UNO CLI

This is a standalone CLI UNO-like game.

The code is written as plausible feature-grown Java: almost everything lives in one procedural `Main` class. It works, but it has mixed responsibilities, duplicated rule logic, primitive-heavy card handling, global state, and condition-heavy gameplay code. The goal is to refactor it safely, not rewrite it.

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

### Run Bot Games

```bash
java -jar target/uno-game-1.0-SNAPSHOT-jar-with-dependencies.jar --bots 3 --games 5 --quiet
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

## Rules

See `docs/rules.html` for the implemented game rules.

## Midterm Materials

* `docs/midterm-exam.md`: midterm brief
* `docs/rubric.md`: grading rubric
* `docs/refactoring-guide.md`: suggested refactoring path
