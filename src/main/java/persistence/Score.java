package persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "scores")
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private int score;

    public Score() {}

    public Score(Round round, Player player, int score) {
        this.round = round;
        this.player = player;
        this.score = score;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Round getRound() { return round; }
    public void setRound(Round round) { this.round = round; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}
