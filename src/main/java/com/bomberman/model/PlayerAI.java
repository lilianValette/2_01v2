package com.bomberman.model;

import java.util.*;

/**
 * Représente un joueur contrôlé par une intelligence artificielle.
 * Gère le comportement selon le niveau de difficulté.
 */
public class PlayerAI extends Player {
    private final Random random = new Random();
    private final AIDifficulty difficulty;

    // Pour la gestion de la fuite après avoir posé une bombe
    private boolean mustFleeOwnBomb = false;
    private int lastBombX = -1, lastBombY = -1, lastBombTimer = -1;
    private boolean mustFleeOwnBombNormal = false;

    public PlayerAI(int id, int startX, int startY, AIDifficulty difficulty) {
        super(id, startX, startY, false);
        this.difficulty = difficulty;
    }

    /** @return le niveau de difficulté de l'IA */
    public AIDifficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Met à jour l'IA selon la difficulté.
     * @param grid grille du jeu
     * @param bombs bombes en jeu
     * @param allPlayers liste de tous les joueurs (pour la difficulté DIFFICILE)
     */
    public void updateAI(Grid grid, List<Bomb> bombs, List<Player> allPlayers) {
        if (!isAlive()) return;
        switch (difficulty) {
            case EASY -> updateEasyAI(grid, bombs);
            case NORMAL -> updateNormalAI(grid, bombs);
            case HARD -> updateHardAI(grid, bombs, allPlayers);
        }
    }

    // --- IA FACILE ---
    private void updateEasyAI(Grid grid, List<Bomb> bombs) {
        int curX = getX(), curY = getY();
        if (random.nextDouble() < 0.3) return;
        if (random.nextDouble() < 0.2) {
            Bomb bomb = dropBomb(Bomb.DEFAULT_TIMER, bombs);
            if (bomb != null) {
                bombs.add(bomb);
                mustFleeOwnBomb = true;
                lastBombX = curX;
                lastBombY = curY;
                lastBombTimer = bomb.getTimer();
            }
        }
        if (mustFleeOwnBomb) {
            fleeOwnBomb(grid, bombs, curX, curY);
            return;
        }
        tryMoveToSafeNeighbour(grid, bombs, curX, curY);
    }

    // --- IA NORMALE ---
    private void updateNormalAI(Grid grid, List<Bomb> bombs) {
        int curX = getX(), curY = getY();
        if (mustFleeOwnBombNormal) {
            fleeOwnBombNormal(grid, bombs, curX, curY);
            return;
        }
        int myDanger = dangerLevelAt(curX, curY, bombs, grid, false);
        if (myDanger < Integer.MAX_VALUE) {
            tryMoveToSafeNeighbour(grid, bombs, curX, curY);
            return;
        }
        if (random.nextDouble() < 0.12) {
            Bomb bomb = dropBomb(Bomb.DEFAULT_TIMER, bombs);
            if (bomb != null) {
                bombs.add(bomb);
                mustFleeOwnBombNormal = true;
                lastBombX = curX;
                lastBombY = curY;
                lastBombTimer = bomb.getTimer();
                fleeOwnBombNormal(grid, bombs, curX, curY);
                return;
            }
        }
        tryMoveToSafeNeighbour(grid, bombs, curX, curY);
    }

    // Fuite après pose de bombe (niveau normal)
    private boolean fleeOwnBombNormal(Grid grid, List<Bomb> bombs, int curX, int curY) {
        if (fleeFromBomb(grid, bombs, curX, curY, true)) {
            if (dangerLevelAt(getX(), getY(), bombs, grid, false) == Integer.MAX_VALUE) {
                mustFleeOwnBombNormal = false;
            }
            return true;
        }
        return false;
    }

    // --- IA DIFFICILE ---
    private void updateHardAI(Grid grid, List<Bomb> bombs, List<Player> allPlayers) {
        int curX = getX(), curY = getY();

        if (mustFleeOwnBomb) {
            fleeOwnBomb(grid, bombs, curX, curY);
            return;
        }

        int myDanger = dangerLevelAt(curX, curY, bombs, grid, false);
        if (myDanger < Integer.MAX_VALUE) {
            tryMoveToSafeNeighbour(grid, bombs, curX, curY);
            return;
        }

        // Recherche la cible la plus proche
        Player target = findNearestTarget(allPlayers, curX, curY);

        // Attaque si la cible est à portée
        if (target != null && Math.abs(target.getX() - curX) + Math.abs(target.getY() - curY) <= 2) {
            if (!hasBombAt(bombs, curX, curY)) {
                if (canReallyEscapeAfterBomb(grid, bombs, curX, curY, Bomb.DEFAULT_TIMER) || random.nextDouble() < 0.08) {
                    Bomb bomb = dropBomb(Bomb.DEFAULT_TIMER, bombs);
                    if (bomb != null) {
                        bombs.add(bomb);
                        mustFleeOwnBomb = true;
                        lastBombX = curX;
                        lastBombY = curY;
                        lastBombTimer = bomb.getTimer();
                    }
                }
            }
            if (mustFleeOwnBomb) {
                fleeOwnBomb(grid, bombs, curX, curY);
            }
            return;
        }

        // Se rapproche de la cible par un chemin sûr
        if (target != null) {
            int[] nextMove = findSafePathToTarget(grid, bombs, curX, curY, target.getX(), target.getY());
            if (nextMove != null) {
                move(nextMove[0], nextMove[1], grid);
                int nx = curX + nextMove[0], ny = curY + nextMove[1];
                if (grid.isInBounds(nx, ny) && grid.getCell(nx, ny) == Grid.CellType.DESTRUCTIBLE && !hasBombAt(bombs, curX, curY)) {
                    if (canReallyEscapeAfterBomb(grid, bombs, curX, curY, Bomb.DEFAULT_TIMER) || random.nextDouble() < 0.08) {
                        Bomb bomb = dropBomb(Bomb.DEFAULT_TIMER, bombs);
                        if (bomb != null) {
                            bombs.add(bomb);
                            mustFleeOwnBomb = true;
                            lastBombX = curX;
                            lastBombY = curY;
                            lastBombTimer = bomb.getTimer();
                        }
                    }
                }
                return;
            }
        }

        // Tente de casser un bloc adjacent
        if (tryBombAdjacentDestructible(grid, bombs, curX, curY)) {
            fleeOwnBomb(grid, bombs, curX, curY);
            return;
        }

        tryMoveToSafeNeighbour(grid, bombs, curX, curY);
    }

    // Recherche la cible vivante la plus proche
    private Player findNearestTarget(List<Player> allPlayers, int curX, int curY) {
        Player target = null;
        int minDist = Integer.MAX_VALUE;
        for (Player p : allPlayers) {
            if (p != this && p.isAlive()) {
                int dist = Math.abs(p.getX() - curX) + Math.abs(p.getY() - curY);
                if (dist < minDist) {
                    minDist = dist;
                    target = p;
                }
            }
        }
        return target;
    }

    // Vérifie s'il y a déjà une bombe à la position donnée
    private boolean hasBombAt(List<Bomb> bombs, int x, int y) {
        for (Bomb b : bombs) {
            if (b.getX() == x && b.getY() == y) return true;
        }
        return false;
    }

    // Tente de poser une bombe sur un bloc destructible adjacent
    private boolean tryBombAdjacentDestructible(Grid grid, List<Bomb> bombs, int curX, int curY) {
        boolean bombed = false;
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] dir : dirs) {
            int nx = curX + dir[0], ny = curY + dir[1];
            if (grid.isInBounds(nx, ny) && grid.getCell(nx, ny) == Grid.CellType.DESTRUCTIBLE && !hasBombAt(bombs, curX, curY)) {
                if (canReallyEscapeAfterBomb(grid, bombs, curX, curY, Bomb.DEFAULT_TIMER) || random.nextDouble() < 0.08) {
                    Bomb bomb = dropBomb(Bomb.DEFAULT_TIMER, bombs);
                    if (bomb != null) {
                        bombs.add(bomb);
                        mustFleeOwnBomb = true;
                        lastBombX = curX;
                        lastBombY = curY;
                        lastBombTimer = bomb.getTimer();
                        bombed = true;
                    }
                }
            }
        }
        return bombed;
    }

    /**
     * Renvoie le premier pas d'un chemin sûr vers la cible (aucun danger sur le chemin), ou null si aucun n'existe.
     */
    private int[] findSafePathToTarget(Grid grid, List<Bomb> bombs, int startX, int startY, int goalX, int goalY) {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        int w = grid.getWidth(), h = grid.getHeight();
        boolean[][] visited = new boolean[w][h];
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(n -> n.fCost));
        queue.add(new Node(startX, startY, null, 0, Math.abs(startX - goalX) + Math.abs(startY - goalY)));
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            if (node.x == goalX && node.y == goalY) {
                Node prev = node;
                while (prev.parent != null && prev.parent.parent != null)
                    prev = prev.parent;
                // Vérifie que le chemin est sûr
                for (Node check = node; check != null; check = check.parent) {
                    if (dangerLevelAt(check.x, check.y, bombs, grid, false) != Integer.MAX_VALUE) {
                        return null;
                    }
                }
                return new int[]{prev.x - startX, prev.y - startY};
            }
            visited[node.x][node.y] = true;
            for (int[] dir : directions) {
                int nx = node.x + dir[0], ny = node.y + dir[1];
                if (grid.isInBounds(nx, ny) && !visited[nx][ny]) {
                    Grid.CellType cell = grid.getCell(nx, ny);
                    if ((cell == Grid.CellType.EMPTY || cell == Grid.CellType.DESTRUCTIBLE) &&
                            dangerLevelAt(nx, ny, bombs, grid, false) == Integer.MAX_VALUE) {
                        int gCost = node.gCost + 1;
                        int hCost = Math.abs(nx - goalX) + Math.abs(ny - goalY);
                        queue.add(new Node(nx, ny, node, gCost, hCost));
                    }
                }
            }
        }
        return null;
    }

    // Fuite intelligente après avoir posé une bombe (ignore la bombe fraîchement posée)
    private boolean fleeOwnBomb(Grid grid, List<Bomb> bombs, int curX, int curY) {
        if (fleeFromBomb(grid, bombs, curX, curY, true)) {
            if (dangerLevelAt(getX(), getY(), bombs, grid, false) == Integer.MAX_VALUE) {
                mustFleeOwnBomb = false;
            }
            return true;
        }
        return false;
    }

    // Fuite générique d'une bombe (selon ignoreOwnLastBomb)
    private boolean fleeFromBomb(Grid grid, List<Bomb> bombs, int curX, int curY, boolean ignoreOwnLastBomb) {
        int[][] directions = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}};
        List<int[]> escapeDirs = new ArrayList<>();
        for (int[] dir : directions) {
            int nx = curX + dir[0], ny = curY + dir[1];
            if (grid.isInBounds(nx, ny)
                    && grid.getCell(nx, ny) == Grid.CellType.EMPTY
                    && dangerLevelAt(nx, ny, bombs, grid, ignoreOwnLastBomb) == Integer.MAX_VALUE) {
                escapeDirs.add(dir);
            }
        }
        if (!escapeDirs.isEmpty()) {
            int[] dir = escapeDirs.get(random.nextInt(escapeDirs.size()));
            move(dir[0], dir[1], grid);
            return true;
        }
        // Sinon, tente la case la moins risquée
        int bestDanger = Integer.MIN_VALUE;
        int[] bestDir = null;
        for (int[] dir : directions) {
            int nx = curX + dir[0], ny = curY + dir[1];
            if (grid.isInBounds(nx, ny) && grid.getCell(nx, ny) == Grid.CellType.EMPTY) {
                int danger = dangerLevelAt(nx, ny, bombs, grid, ignoreOwnLastBomb);
                if (danger > bestDanger) {
                    bestDanger = danger;
                    bestDir = dir;
                }
            }
        }
        if (bestDir != null) {
            move(bestDir[0], bestDir[1], grid);
        }
        return false;
    }

    // Déplacement vers une case voisine vraiment sûre
    private boolean tryMoveToSafeNeighbour(Grid grid, List<Bomb> bombs, int curX, int curY) {
        int[][] directions = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}};
        List<int[]> choices = new ArrayList<>();
        for (int[] dir : directions) {
            int nx = curX + dir[0], ny = curY + dir[1];
            if (grid.isInBounds(nx, ny)
                    && grid.getCell(nx, ny) == Grid.CellType.EMPTY
                    && dangerLevelAt(nx, ny, bombs, grid, false) == Integer.MAX_VALUE) {
                choices.add(dir);
            }
        }
        if (!choices.isEmpty()) {
            int[] dir = choices.get(random.nextInt(choices.size()));
            move(dir[0], dir[1], grid);
            return true;
        }
        return false;
    }

    // Vérifie si l'IA peut atteindre une case sûre avant explosion de sa bombe
    private boolean canReallyEscapeAfterBomb(Grid grid, List<Bomb> bombs, int startX, int startY, int bombTimer) {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        List<Bomb> bombsWithNew = new ArrayList<>(bombs);
        bombsWithNew.add(new Bomb(startX, startY, bombTimer, getBombRange(), this));

        Queue<int[]> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.add(new int[]{startX, startY, 0});
        visited.add(startX + "," + startY + ",0");

        while (!queue.isEmpty()) {
            int[] node = queue.poll();
            int x = node[0], y = node[1], tick = node[2];
            if (tick < bombTimer && dangerLevelAt(x, y, bombsWithNew, grid, true) == Integer.MAX_VALUE) {
                return true;
            }
            if (tick >= bombTimer) continue;
            for (int[] dir : directions) {
                int nx = x + dir[0], ny = y + dir[1];
                String state = nx + "," + ny + "," + (tick + 1);
                if (grid.isInBounds(nx, ny)
                        && grid.getCell(nx, ny) == Grid.CellType.EMPTY
                        && !visited.contains(state)) {
                    if (!(nx == startX && ny == startY && tick > 0)) {
                        visited.add(state);
                        queue.add(new int[]{nx, ny, tick + 1});
                    }
                }
            }
        }
        return false;
    }

    /**
     * Calcule le niveau de danger sur une case donnée.
     * @param x position X
     * @param y position Y
     * @param bombs liste des bombes
     * @param grid grille de jeu
     * @param ignoreOwnLastBomb true si on ignore la bombe fraîchement posée par l'IA
     * @return nombre de ticks avant explosion si danger, sinon Integer.MAX_VALUE
     */
    private int dangerLevelAt(int x, int y, List<Bomb> bombs, Grid grid, boolean ignoreOwnLastBomb) {
        int minTick = Integer.MAX_VALUE;
        for (Bomb bomb : bombs) {
            int bx = bomb.getX(), by = bomb.getY(), range = bomb.getRange(), timer = bomb.getTimer();
            if (ignoreOwnLastBomb && bx == lastBombX && by == lastBombY && timer == lastBombTimer) continue;
            if (x == bx && y == by) minTick = Math.min(minTick, timer);
            if (y == by && Math.abs(x - bx) <= range) {
                boolean blocked = false;
                for (int ix = Math.min(x, bx) + 1; ix < Math.max(x, bx); ix++) {
                    if (grid.getCell(ix, y) == Grid.CellType.INDESTRUCTIBLE) { blocked = true; break; }
                }
                if (!blocked) minTick = Math.min(minTick, timer);
            }
            if (x == bx && Math.abs(y - by) <= range) {
                boolean blocked = false;
                for (int iy = Math.min(y, by) + 1; iy < Math.max(y, by); iy++) {
                    if (grid.getCell(x, iy) == Grid.CellType.INDESTRUCTIBLE) { blocked = true; break; }
                }
                if (!blocked) minTick = Math.min(minTick, timer);
            }
        }
        return minTick;
    }

    /** Noeud pour le calcul de chemin (A*) */
    private static class Node {
        int x, y, gCost, hCost, fCost;
        Node parent;
        Node(int x, int y, Node parent, int gCost, int hCost) {
            this.x = x; this.y = y; this.parent = parent;
            this.gCost = gCost; this.hCost = hCost; this.fCost = gCost + hCost;
        }
    }
}