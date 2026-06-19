package persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rounds")
public class Round {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private Player winner;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @PrePersist
    protected void onCreate() {
        endTime = LocalDateTime.now();
    }

    public Round() {}

    public Round(Game game, int roundNumber) {
        this.game = game;
        this.roundNumber = roundNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }
    public Player getWinner() { return winner; }
    public void setWinner(Player winner) { this.winner = winner; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
