package persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;

public class GameRepository {
    private EntityManagerFactory emf;

    public GameRepository() {
        this("uno-pu");
    }

    public GameRepository(String persistenceUnitName) {
        emf = Persistence.createEntityManagerFactory(persistenceUnitName);
    }

    public Player getOrCreatePlayer(String name) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            List<Player> players = em.createQuery("SELECT p FROM Player p WHERE p.name = :name", Player.class)
                    .setParameter("name", name)
                    .getResultList();
            
            Player player;
            if (players.isEmpty()) {
                player = new Player(name);
                em.persist(player);
            } else {
                player = players.get(0);
            }
            em.getTransaction().commit();
            return player;
        } finally {
            em.close();
        }
    }

    public Game createGame() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Game game = new Game();
            em.persist(game);
            em.getTransaction().commit();
            return game;
        } finally {
            em.close();
        }
    }

    public void finishGame(Game game, Player winner) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Game merged = em.merge(game);
            merged.setEndTime(LocalDateTime.now());
            if (winner != null) {
                merged.setWinner(em.merge(winner));
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Round createRound(Game game, int roundNumber) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Round round = new Round(em.merge(game), roundNumber);
            em.persist(round);
            em.getTransaction().commit();
            return round;
        } finally {
            em.close();
        }
    }

    public void finishRound(Round round, Player winner) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Round merged = em.merge(round);
            merged.setEndTime(LocalDateTime.now());
            if (winner != null) {
                merged.setWinner(em.merge(winner));
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void saveScore(Round round, Player player, int points) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Score score = new Score(em.merge(round), em.merge(player), points);
            em.persist(score);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<Game> getRecentGames(int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT g FROM Game g LEFT JOIN FETCH g.winner ORDER BY g.startTime DESC", Game.class)
                     .setMaxResults(limit)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Object[]> getPlayerWinCounts() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                "SELECT p.name, COUNT(g) FROM Game g JOIN g.winner p GROUP BY p.name ORDER BY COUNT(g) DESC", 
                Object[].class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Object[]> getHighestScores(int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                "SELECT p.name, MAX(s.score) FROM Score s JOIN s.player p GROUP BY p.name ORDER BY MAX(s.score) DESC", 
                Object[].class).setMaxResults(limit).getResultList();
        } finally {
            em.close();
        }
    }

    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
