package com.bomberman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class GameTest {

    private Game game;
    private Level dummyLevel;
    private AIDifficulty aiDifficulty;

    @BeforeEach
    void setUp() {
        int[][] grid = new int[7][7];
        dummyLevel = new Level("test", "tester", "easy", "unit test", grid);
        aiDifficulty = AIDifficulty.EASY;

        game = new Game(7, 7, 1, 0, dummyLevel, aiDifficulty);
    }

    @Test
    void testPlayersInitialization() {
        List<Player> players = game.getPlayers();
        assertEquals(1, players.size());
        Player p = players.get(0);
        assertTrue(p.isAlive());
        assertTrue(p.isHuman());
    }

    @Test
    void testMovePlayer() {
        Player p = game.getPlayers().get(0);
        int startX = p.getX();
        int startY = p.getY();
        game.movePlayer(p, 1, 0);
        assertNotEquals(startX, p.getX());
        assertEquals(startY, p.getY());
    }

    @Test
    void testPlaceBombAndExplosion() {
        Player p = game.getPlayers().get(0);
        game.placeBomb(p);
        assertEquals(1, game.getBombs().size());

        Bomb bomb = game.getBombs().get(0);
        assertEquals(p, bomb.getOwner());

        // Simuler le tick de la bombe jusqu'à explosion
        for (int i = 0; i < Bomb.DEFAULT_TIMER + 1; i++) {
            game.updateBombs();
        }

        // La bombe doit avoir explosé et disparu
        assertEquals(0, game.getBombs().size());
    }

    @Test
    void testGameOverDetection() {
        Player p = game.getPlayers().get(0);
        // Simuler la mort du joueur
        p.takeDamage(); // Peut nécessiter d'appeler plusieurs fois selon le nombre de vies
        game.updateGameState();
        assertTrue(game.isGameOver());
        assertEquals(p, game.getWinner());
    }

    @Test
    void testBonusCollection() {
        // Ajouter un bonus sur la position du joueur
        Player p = game.getPlayers().get(0);
        Bonus bonus = new FlameBonus(p.getX(), p.getY(), 1);
        game.getBonuses().add(bonus);

        game.updateBombs(); // Déclenche la collecte de bonus

        assertTrue(bonus.isCollected());
    }
}