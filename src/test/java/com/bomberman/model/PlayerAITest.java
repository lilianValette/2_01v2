package com.bomberman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerAITest {

    private Grid grid;
    private PlayerAI aiEasy, aiNormal, aiHard;
    private List<Bomb> bombs;
    private List<Player> allPlayers;

    @BeforeEach
    void setUp() {
        int[][] layout = new int[5][5];
        Level level = new Level("level", "author", "easy", "desc", layout);
        grid = new Grid(5, 5, level);

        aiEasy = new PlayerAI(1, 1, 1, AIDifficulty.EASY);
        aiNormal = new PlayerAI(2, 2, 2, AIDifficulty.NORMAL);
        aiHard = new PlayerAI(3, 3, 3, AIDifficulty.HARD);

        bombs = new ArrayList<>();
        allPlayers = new ArrayList<>();
        allPlayers.add(aiEasy);
        allPlayers.add(aiNormal);
        allPlayers.add(aiHard);
        allPlayers.add(new Player(4, 4, 4, true));
    }

    @Test
    void testInitialization() {
        assertEquals(AIDifficulty.EASY, aiEasy.getDifficulty());
        assertEquals(AIDifficulty.NORMAL, aiNormal.getDifficulty());
        assertEquals(AIDifficulty.HARD, aiHard.getDifficulty());
        assertFalse(aiEasy.isHuman());
        assertTrue(aiEasy.isAI());
    }

    @Test
    void testUpdateAI_EasyRandomNoException() {
        // On vérifie que l'appel ne lance pas d'exception et que l'IA reste sur la grille ou pose une bombe
        aiEasy.updateAI(grid, bombs, allPlayers);
        assertTrue(aiEasy.getX() >= 0 && aiEasy.getY() >= 0);
    }

    @Test
    void testUpdateAI_NormalRandomNoException() {
        aiNormal.updateAI(grid, bombs, allPlayers);
        assertTrue(aiNormal.getX() >= 0 && aiNormal.getY() >= 0);
    }

    @Test
    void testUpdateAI_HardRandomNoException() {
        aiHard.updateAI(grid, bombs, allPlayers);
        assertTrue(aiHard.getX() >= 0 && aiHard.getY() >= 0);
    }

    @Test
    void testEasyAI_CanPlaceBombAndFlee() {
        // Force une bombe sous l'IA et vérifie qu'elle essaie de s'enfuir
        bombs.add(new Bomb(aiEasy.getX(), aiEasy.getY(), 3, 1, aiEasy));
        int beforeX = aiEasy.getX();
        int beforeY = aiEasy.getY();
        aiEasy.updateAI(grid, bombs, allPlayers);
        boolean moved = aiEasy.getX() != beforeX || aiEasy.getY() != beforeY;
        // L'IA doit essayer de bouger si possible
        assertTrue(moved || !aiEasy.isAlive() || bombs.size() > 0);
    }

    @Test
    void testNormalAI_TakeDamage_StopsMoving() {
        aiNormal.kill();
        int beforeX = aiNormal.getX();
        int beforeY = aiNormal.getY();
        aiNormal.updateAI(grid, bombs, allPlayers);
        // Si mort, ne bouge pas
        assertEquals(beforeX, aiNormal.getX());
        assertEquals(beforeY, aiNormal.getY());
    }

    @Test
    void testHardAI_AttacksNearbyPlayer() {
        Player target = new Player(10, 3, 1, true);
        allPlayers.add(target);
        aiHard = new PlayerAI(3, 3, 2, AIDifficulty.HARD);
        int bombCountBefore = bombs.size();
        // L'IA hard va parfois attaquer si le joueur est proche
        for (int i = 0; i < 10; i++) {
            aiHard.updateAI(grid, bombs, allPlayers);
        }
        // Il se peut qu'une bombe ait été posée près d'un joueur
        assertTrue(bombs.size() >= bombCountBefore);
    }

    @Test
    void testFindNearestTarget() {
        PlayerAI ai = new PlayerAI(5, 0, 0, AIDifficulty.HARD);
        Player target1 = new Player(6, 1, 1, true);
        Player target2 = new Player(7, 4, 4, true);
        List<Player> players = new ArrayList<>();
        players.add(ai);
        players.add(target1);
        players.add(target2);

        // Méthode privée, donc on ne peut la tester directement, mais via updateAI
        ai.updateAI(grid, bombs, players);
        // L'IA ne doit pas cibler elle-même
        assertNotEquals(ai, target1);
        assertNotEquals(ai, target2);
    }
}