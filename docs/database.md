# Database Persistence

This document outlines the persistence layer of the UNO Game, detailing how game history, statistics, and player records are stored and queried.

## Selected Database
The project utilizes **H2**, an embedded SQL database ideal for local development. Data is persisted to a local file (`uno_db.mv.db`) so history remains intact across multiple application runs.

## Selected ORM/Persistence Framework
We use **Hibernate ORM** via the **Java Persistence API (JPA)**. The `GameRepository` Data Access Object abstracts all raw SQL away from the game mechanics, using an `EntityManager` to manipulate Entity objects.

## Schema Setup
The schema tracks the complete state of gameplay:
- `players`: Stores unique player names.
- `games`: Represents a full session or execution of the program. It tracks the start and end times, along with the overall winner.
- `rounds`: Represents the individual matches played within a game (e.g., if you specify `--games 3`, there are 3 rounds). Tracks the round winner.
- `scores`: Tracks the points awarded to players at the end of each round.

The initial database design is located in `src/main/resources/schema.sql`. At runtime, Hibernate's `hbm2ddl.auto=update` property automatically generates and updates the schema based on our JPA entities (`Player`, `Game`, `Round`, `Score`).

## How to Run Persistence Tests
Unit tests are fully isolated. They use a separate test persistence unit (`uno-test-pu`) that runs an in-memory database (`jdbc:h2:mem:testdb`). This guarantees that tests will not corrupt your real game history.

To run the persistence tests, execute:
```bash
mvn clean test
```

## How to View Game History or Statistics
Game history and statistics are queried using JPQL and can be viewed directly from the command-line interface without starting a new game session.

To view the report, use the `--report` flag:
```bash
java -cp target/uno-game-1.0-SNAPSHOT-jar-with-dependencies.jar Main --report
```
*Note: Make sure to compile and package the application first using `mvn clean package` if you haven't already.*

The report includes:
- **Recent Games**: A chronological list of the latest game sessions.
- **Player Win Counts**: An aggregated leaderboard showing the total number of overall games won by each player.
- **Highest Single-Round Scores**: The highest points achieved in individual rounds.
