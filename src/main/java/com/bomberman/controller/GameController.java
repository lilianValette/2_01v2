package com.bomberman.controller;

import com.bomberman.model.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.Image;
import com.bomberman.model.Game.ExplosionCell;
import com.bomberman.model.Game.ExplosionPartType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur principal de la partie.
 * Gère l'affichage, la logique du jeu et les interactions clavier.
 */
public class GameController {
    @FXML
    private Canvas gameCanvas;

    @FXML
    private StackPane rootPane;

    private Stage stage;

    // Constantes d'affichage
    public static final int DEFAULT_CELL_SIZE = 48;
    private static final int CELL_SIZE = DEFAULT_CELL_SIZE;
    private static final double BORDER_PIXEL_RATIO = 0.5;
    private static final double TOP_UI_HEIGHT_RATIO = 2.5;

    // Données du jeu
    private Game game;
    private Timeline gameTimeline;
    private Timeline timerTimeline;
    private int timerSeconds = 180; // 3 minutes

    // Ressources graphiques
    private static final String[] AVATAR_PATHS = {
            "/images/avatarsJoueurs/PBlanc-icon.png",
            "/images/avatarsJoueurs/PBleuCiel-icon.png",
            "/images/avatarsJoueurs/PRose-icon.png",
            "/images/avatarsJoueurs/PRouge-icon.png"
    };
    private final Image[] avatarsJoueurs = new Image[4];

    private static final String[][] PLAYER_SPRITE_PATHS = {
            {
                    "/images/Player/PBlanc/PBlanc-face.png",
                    "/images/Player/PBlanc/PBlanc-dos.png",
                    "/images/Player/PBlanc/PBlanc-gauche.png",
                    "/images/Player/PBlanc/PBlanc-droite.png"
            },
            {
                    "/images/Player/PBleuCiel/PBleuCiel-face.png",
                    "/images/Player/PBleuCiel/PBleuCiel-dos.png",
                    "/images/Player/PBleuCiel/PBleuCiel-gauche.png",
                    "/images/Player/PBleuCiel/PBleuCiel-droite.png"
            },
            {
                    "/images/Player/PRose/PRose-face.png",
                    "/images/Player/PRose/PRose-dos.png",
                    "/images/Player/PRose/PRose-gauche.png",
                    "/images/Player/PRose/PRose-droite.png"
            },
            {
                    "/images/Player/PRouge/PRouge-face.png",
                    "/images/Player/PRouge/PRouge-dos.png",
                    "/images/Player/PRouge/PRouge-gauche.png",
                    "/images/Player/PRouge/PRouge-droite.png"
            }
    };
    private final Image[][] playerSprites = new Image[4][4];

    private int[] playerDirections = new int[4];

    // Animation des joueurs
    private static class PlayerAnim {
        double visX, visY;
        int targetX, targetY;
        boolean moving = false;
    }
    private PlayerAnim[] playerAnims;

    private Level level;
    private int playerCount;
    private int iaCount;
    private AIDifficulty aiDifficulty = AIDifficulty.EASY;

    private Image wallIndestructibleImg;
    private Image wallDestructibleImg;
    private Image solImg;
    private Image bombImg;
    private Image explosionImg;
    private Image explosionV2CentreImg, explosionV2BrancheImg, explosionV2FinImg;
    private Image jacketBonusImg;
    private Image flameBonusImg;
    private Image lifeBonusImg;
    private boolean gameEnded = false;

    private static final Map<String, Image> imageCache = new HashMap<>();

    public void setStage(Stage stage) { this.stage = stage; }
    public void setLevel(Level level) { this.level = level; }
    public void setPlayerCount(int playerCount) { this.playerCount = playerCount; }
    public void setIaCount(int iaCount) { this.iaCount = iaCount; }
    public void setAIDifficulty(AIDifficulty aiDifficulty) { this.aiDifficulty = aiDifficulty; }

    @FXML
    public void initialize() {}

    /**
     * Démarre une partie et initialise l'affichage et la logique.
     */
    public void startGame() {
        game = new Game(15, 13, playerCount, iaCount, level, aiDifficulty);

        for (int i = 0; i < avatarsJoueurs.length; i++) {
            avatarsJoueurs[i] = safeImageFromResource(AVATAR_PATHS[i]);
        }
        for (int i = 0; i < 4; i++) {
            for (int d = 0; d < 4; d++) {
                try {
                    playerSprites[i][d] = safeImageFromResource(PLAYER_SPRITE_PATHS[i][d]);
                    if (playerSprites[i][d].isError()) playerSprites[i][d] = avatarsJoueurs[i];
                } catch (Exception e) {
                    playerSprites[i][d] = avatarsJoueurs[i];
                }
            }
        }

        jacketBonusImg = safeImageFromResource("/images/items/jacket_bonus.png");
        flameBonusImg = safeImageFromResource("/images/items/flame_bonus.png");
        lifeBonusImg = safeImageFromResource("/images/items/life_bonus.png");
        wallIndestructibleImg = safeImageFromResource(level.getWallIndestructibleImagePath());
        wallDestructibleImg   = safeImageFromResource(level.getWallDestructibleImagePath());
        solImg                = safeImageFromResource(level.getGroundImagePath());
        bombImg               = safeImageFromResource("/images/items/bombe.png");
        explosionImg = safeImageFromResource("/images/explosion/explosionV1.png");
        explosionV2CentreImg  = safeImageFromResource("/images/explosion/ExplosionV2-Centre.png");
        explosionV2BrancheImg = safeImageFromResource("/images/explosion/ExplosionV2.png");
        explosionV2FinImg     = safeImageFromResource("/images/explosion/ExplosionV2-fin.png");

        int gridWidth = game.getGrid().getWidth();
        int gridHeight = game.getGrid().getHeight();
        double borderPixel = CELL_SIZE * BORDER_PIXEL_RATIO;
        double topUiHeight = CELL_SIZE * TOP_UI_HEIGHT_RATIO;
        double canvasWidth = borderPixel * 2 + gridWidth * CELL_SIZE;
        double canvasHeight = topUiHeight + borderPixel * 2 + gridHeight * CELL_SIZE;
        gameCanvas.setWidth(canvasWidth);
        gameCanvas.setHeight(canvasHeight);
        if (stage != null) {
            stage.setWidth(canvasWidth);
            stage.setHeight(canvasHeight + 40);
            stage.setMinWidth(canvasWidth);
            stage.setMinHeight(canvasHeight + 40);
            stage.setMaxWidth(canvasWidth);
            stage.setMaxHeight(canvasHeight + 40);
        }

        playerDirections = new int[game.getPlayers().size()];
        for (int i = 0; i < playerDirections.length; i++) playerDirections[i] = 0;

        playerAnims = new PlayerAnim[game.getPlayers().size()];
        for (int i = 0; i < playerAnims.length; i++) {
            Player p = game.getPlayers().get(i);
            playerAnims[i] = new PlayerAnim();
            playerAnims[i].visX = p.getX();
            playerAnims[i].visY = p.getY();
            playerAnims[i].targetX = p.getX();
            playerAnims[i].targetY = p.getY();
        }

        drawGrid();

        // Ecoute clavier
        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(this::handleKeyPressed);

        // Ticks de jeu
        if (gameTimeline != null) gameTimeline.stop();
        gameTimeline = new Timeline(new KeyFrame(Duration.seconds(0.05), e -> updateIAAndGame()));
        gameTimeline.setCycleCount(Timeline.INDEFINITE);
        gameTimeline.play();

        // Timer décompte
        if (timerTimeline != null) timerTimeline.stop();
        timerSeconds = 180;
        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timerSeconds--;
            if (timerSeconds <= 0) {
                timerTimeline.stop();
                gameTimeline.stop();
                showEndGameScreen("Time's up!");
            }
            drawGrid();
        }));
        timerTimeline.setCycleCount(Timeline.INDEFINITE);
        timerTimeline.play();
    }

    /**
     * Affiche l'écran de fin de partie avec le message donné.
     * @param message Message à afficher
     */
    private void showEndGameScreen(String message) {
        if (gameTimeline != null) gameTimeline.stop();
        if (timerTimeline != null) timerTimeline.stop();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bomberman/view/EndGameScreen.fxml"));
            Parent root = loader.load();
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                System.err.println("Stage est null, impossible de changer de scène");
            }
            EndGameScreenController controller = loader.getController();
            controller.setMessage(message);
            controller.setOnReturnCallback(this::returnToMenu);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Vérifie si la partie est terminée.
     */
    private void checkGameOver() {
        if (gameEnded) return;
        long aliveCount = game.getPlayers().stream().filter(Player::isAlive).count();
        if (aliveCount <= 1) {
            gameEnded = true;
            Player winner = game.getPlayers().stream().filter(Player::isAlive).findFirst().orElse(null);
            String message;
            if (winner != null) {
                message = (winner.isHuman() ? "Player " : "AI ") + winner.getId() + " wins!";
            } else {
                message = "It's a tie!";
            }
            showEndGameScreen(message);
        }
    }

    /**
     * Met à jour la logique du jeu et les IA, puis vérifie la fin de partie.
     */
    private void updateIAAndGame() {
        int nbPlayers = game.getPlayers().size();
        int[] prevX = new int[nbPlayers];
        int[] prevY = new int[nbPlayers];
        for (int i = 0; i < nbPlayers; i++) {
            Player p = game.getPlayers().get(i);
            prevX[i] = p.getX();
            prevY[i] = p.getY();
        }

        // On ne fait updateAIs et updateBombs que toutes les 4 frames (pour l'animation)
        boolean doLogicTick = (System.currentTimeMillis() / 50) % 4 == 0;
        if (doLogicTick) {
            game.updateAIs();
            game.updateBombs();
        }

        for (int i = 0; i < nbPlayers; i++) {
            Player p = game.getPlayers().get(i);
            int dx = p.getX() - prevX[i];
            int dy = p.getY() - prevY[i];
            if ((dx != 0 || dy != 0) && playerAnims != null) {
                playerAnims[i].targetX = p.getX();
                playerAnims[i].targetY = p.getY();
                playerAnims[i].moving = true;
            }
            if (!p.isHuman()) {
                if      (dx ==  1) playerDirections[i] = 3; // droite
                else if (dx == -1) playerDirections[i] = 2; // gauche
                else if (dy ==  1) playerDirections[i] = 0; // bas
                else if (dy == -1) playerDirections[i] = 1; // haut
            }
        }
        drawGrid();
        checkGameOver();
    }

    /**
     * Retour au menu principal.
     */
    private void returnToMenu() {
        if (gameTimeline != null) gameTimeline.stop();
        if (timerTimeline != null) timerTimeline.stop();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bomberman/view/menu.fxml"));
            Parent root = loader.load();
            MenuController menuController = loader.getController();
            menuController.setStage(stage);
            if (stage != null) stage.setScene(new Scene(root));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Gère les entrées clavier pour les joueurs humains.
     * @param event Événement clavier
     */
    private void handleKeyPressed(KeyEvent event) {
        if (game.getPlayers().isEmpty()) return;
        for (int idx = 0; idx < game.getPlayers().size(); idx++) {
            Player p = game.getPlayers().get(idx);
            if (!p.isAlive() || !p.isHuman()) continue;
            switch (idx) {
                case 0 -> { // Player 1: arrows + space
                    switch (event.getCode()) {
                        case UP    -> {
                            playerDirections[0] = 1;
                            game.movePlayer(p, 0, -1);
                            updatePlayerAnim(0, p);
                        }
                        case DOWN  -> {
                            playerDirections[0] = 0;
                            game.movePlayer(p, 0, 1);
                            updatePlayerAnim(0, p);
                        }
                        case LEFT  -> {
                            playerDirections[0] = 2;
                            game.movePlayer(p, -1, 0);
                            updatePlayerAnim(0, p);
                        }
                        case RIGHT -> {
                            playerDirections[0] = 3;
                            game.movePlayer(p, 1, 0);
                            updatePlayerAnim(0, p);
                        }
                        case SPACE -> game.placeBomb(p);
                    }
                }
                case 1 -> { // Player 2: ZQSD + shift
                    switch (event.getCode()) {
                        case Z     -> {
                            playerDirections[1] = 1;
                            game.movePlayer(p, 0, -1);
                            updatePlayerAnim(1, p);
                        }
                        case S     -> {
                            playerDirections[1] = 0;
                            game.movePlayer(p, 0, 1);
                            updatePlayerAnim(1, p);
                        }
                        case Q     -> {
                            playerDirections[1] = 2;
                            game.movePlayer(p, -1, 0);
                            updatePlayerAnim(1, p);
                        }
                        case D     -> {
                            playerDirections[1] = 3;
                            game.movePlayer(p, 1, 0);
                            updatePlayerAnim(1, p);
                        }
                        case SHIFT -> game.placeBomb(p);
                    }
                }
            }
        }
        drawGrid();
    }

    /**
     * Met à jour l'animation pour le joueur donné.
     */
    private void updatePlayerAnim(int idx, Player p) {
        playerAnims[idx].targetX = p.getX();
        playerAnims[idx].targetY = p.getY();
        playerAnims[idx].moving = true;
    }

    /**
     * Affichage principal du plateau et de l'interface.
     */
    private void drawGrid() {
        if (game == null || game.getPlayers().isEmpty()) return;

        // Animation des joueurs
        if (playerAnims != null) {
            double speed = 0.18; // cases par frame
            for (int i = 0; i < game.getPlayers().size(); i++) {
                PlayerAnim anim = playerAnims[i];
                if (anim.moving) {
                    double dx = anim.targetX - anim.visX;
                    double dy = anim.targetY - anim.visY;
                    double dist = Math.hypot(dx, dy);
                    if (dist < speed) {
                        anim.visX = anim.targetX;
                        anim.visY = anim.targetY;
                        anim.moving = false;
                    } else {
                        anim.visX += dx * speed / dist;
                        anim.visY += dy * speed / dist;
                    }
                }
            }
        }

        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        var grid = game.getGrid();
        int gridWidth = grid.getWidth();
        int gridHeight = grid.getHeight();
        double borderPixel = CELL_SIZE * BORDER_PIXEL_RATIO;
        double topUiHeight = CELL_SIZE * TOP_UI_HEIGHT_RATIO;
        double canvasWidth = borderPixel * 2 + gridWidth * CELL_SIZE;
        double canvasHeight = topUiHeight + borderPixel * 2 + gridHeight * CELL_SIZE;

        // --- Barre du haut ---
        gc.setFill(Color.ORANGE);
        gc.fillRect(0, 0, canvasWidth, topUiHeight);

        int totalPlayers = game.getPlayers().size();
        double desiredSpacing = topUiHeight * 0.08;
        double minSpacing = 8;
        double iconSize = topUiHeight * 0.65;
        double counterSize = topUiHeight * 0.5;
        double timerWidth = topUiHeight * 1.15;
        double timerHeight = topUiHeight * 0.85;
        double spacing = Math.max(desiredSpacing, minSpacing);
        double margin = 14;

        double blocksWidth = totalPlayers * (iconSize + counterSize) + (totalPlayers + 1) * spacing + timerWidth;
        double x = (canvasWidth - blocksWidth) / 2.0;
        int leftPlayers = totalPlayers <= 2 ? 1 : totalPlayers / 2;

        // Joueurs gauche
        for (int i = 0; i < leftPlayers; i++) {
            drawPlayerBlock(gc, game.getPlayers().get(i), avatarsJoueurs[i], x, (topUiHeight - iconSize) / 2, iconSize, counterSize);
            x += iconSize + counterSize + spacing;
        }

        // Timer central
        double timerX = x;
        double timerY = (topUiHeight - timerHeight) / 2;
        gc.setFill(Color.BLACK);
        gc.fillRoundRect(timerX, timerY, timerWidth, timerHeight, 16, 16);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRoundRect(timerX, timerY, timerWidth, timerHeight, 16, 16);

        String timerStr = String.format("%d:%02d", Math.max(timerSeconds, 0) / 60, Math.max(timerSeconds, 0) % 60);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Consolas", timerHeight * 0.55));
        Text timerText = new Text(timerStr);
        timerText.setFont(gc.getFont());
        double timerTextWidth = timerText.getLayoutBounds().getWidth();
        double timerTextHeight = timerText.getLayoutBounds().getHeight();
        double timerTextX = timerX + (timerWidth - timerTextWidth) / 2;
        double timerTextY = timerY + timerHeight / 2 + timerTextHeight / 3;
        gc.fillText(timerStr, timerTextX, timerTextY);

        x += timerWidth + spacing;

        // Joueurs droite
        for (int i = leftPlayers; i < totalPlayers; i++) {
            drawPlayerBlock(gc, game.getPlayers().get(i), avatarsJoueurs[i], x, (topUiHeight - iconSize) / 2, iconSize, counterSize);
            x += iconSize + counterSize + spacing;
        }

        // --- Plateau ---
        gc.setFill(Color.rgb(34, 34, 34));
        gc.fillRect(0, topUiHeight, canvasWidth, borderPixel);
        gc.fillRect(0, topUiHeight + borderPixel + gridHeight * CELL_SIZE, canvasWidth, borderPixel);
        gc.fillRect(0, topUiHeight, borderPixel, borderPixel + gridHeight * CELL_SIZE);
        gc.fillRect(borderPixel + gridWidth * CELL_SIZE, topUiHeight, borderPixel, borderPixel + gridHeight * CELL_SIZE);

        // Affichage des cases
        for (int y = 0; y < gridHeight; y++) {
            for (int xg = 0; xg < gridWidth; xg++) {
                double drawX = borderPixel + xg * CELL_SIZE;
                double drawY = topUiHeight + borderPixel + y * CELL_SIZE;
                switch (grid.getCell(xg, y)) {
                    case INDESTRUCTIBLE -> gc.drawImage(wallIndestructibleImg, drawX, drawY, CELL_SIZE, CELL_SIZE);
                    case DESTRUCTIBLE   -> gc.drawImage(wallDestructibleImg, drawX, drawY, CELL_SIZE, CELL_SIZE);
                    case BOMB           -> gc.drawImage(solImg, drawX, drawY, CELL_SIZE, CELL_SIZE);
                    case EXPLOSION -> {
                        ExplosionCell exCell = game.getExplosionCell(xg, y);
                        if (exCell == null) {
                            gc.drawImage(explosionImg, drawX, drawY, CELL_SIZE, CELL_SIZE);
                        } else if (exCell.type == ExplosionPartType.CENTRE) {
                            gc.drawImage(explosionV2CentreImg, drawX, drawY, CELL_SIZE, CELL_SIZE);
                        } else {
                            gc.save();
                            double angle = switch (exCell.direction) {
                                case UP -> 0;
                                case RIGHT -> 90;
                                case DOWN -> 180;
                                case LEFT -> 270;
                                default -> 0;
                            };
                            gc.translate(drawX + CELL_SIZE / 2, drawY + CELL_SIZE / 2);
                            gc.rotate(angle);
                            gc.translate(-CELL_SIZE / 2, -CELL_SIZE / 2);
                            if (exCell.type == ExplosionPartType.BRANCH) {
                                gc.drawImage(explosionV2BrancheImg, 0, 0, CELL_SIZE, CELL_SIZE);
                            } else if (exCell.type == ExplosionPartType.END) {
                                gc.drawImage(explosionV2FinImg, 0, 0, CELL_SIZE, CELL_SIZE);
                            }
                            gc.restore();
                        }
                    }
                    default -> gc.drawImage(solImg, drawX, drawY, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        // Affichage des bonus
        for (Bonus bonus : game.getBonuses()) {
            double bx = borderPixel + bonus.getX() * CELL_SIZE;
            double by = topUiHeight  + borderPixel + bonus.getY() * CELL_SIZE;
            gc.drawImage(bonus.getSprite(), bx, by, CELL_SIZE, CELL_SIZE);
        }
        // Affichage des bombes
        for (Bomb b : game.getBombs()) {
            double bx = borderPixel + b.getX() * CELL_SIZE;
            double by = topUiHeight + borderPixel + b.getY() * CELL_SIZE;
            gc.drawImage(bombImg, bx, by, CELL_SIZE, CELL_SIZE);
        }
        // Affichage des joueurs (humains ET IA) avec sprite directionnel
        for (int idx = 0; idx < game.getPlayers().size(); idx++) {
            Player p = game.getPlayers().get(idx);
            if (p.isAlive()) {
                double px = borderPixel + playerAnims[idx].visX * CELL_SIZE;
                double py = topUiHeight + borderPixel + playerAnims[idx].visY * CELL_SIZE;
                int direction = playerDirections[idx];
                Image currentSprite = playerSprites[idx][direction];
                gc.drawImage(currentSprite, px, py, CELL_SIZE, CELL_SIZE);
            }
        }

        // Afficher le temps restant des bonus pour chaque joueur
        for (Player p : game.getPlayers()) {
            double textX;
            double baseY;
            int bonusIndex = 0;

            if (p.getId() == 1) {
                textX = margin + iconSize + spacing + counterSize + 8;
                baseY = topUiHeight * 0.6 + 12;
            } else {
                textX = canvasWidth - margin - iconSize - spacing - counterSize - 75;
                baseY = topUiHeight * 0.6 + 12;
            }

            double lineHeight = 18;
            double imgSize = iconSize * 0.5;

            for (ActiveBonus ab : p.getActiveBonuses()) {
                double y = baseY + bonusIndex * lineHeight;
                double imgY = y - imgSize + (lineHeight - imgSize) / 2 + imgSize * 0.1;
                Image bonusImg = switch (ab.getType()) {
                    case FLAME -> flameBonusImg;
                    case JACKET -> jacketBonusImg;
                    case LIFE -> null;
                    default -> null;
                };
                if (bonusImg != null) {
                    gc.drawImage(bonusImg, textX-40, imgY, imgSize, imgSize);
                }
                bonusIndex++;
            }
        }
    }

    /**
     * Affiche l'avatar d'un joueur, ses vies, et s'il est IA.
     */
    private void drawPlayerBlock(GraphicsContext gc, Player player, Image avatar, double x, double y, double iconSize, double counterSize) {
        gc.drawImage(avatar, x, y, iconSize, iconSize);
        double counterX = x + iconSize;
        double counterY = y + (iconSize - counterSize) / 2;
        gc.setFill(Color.BLACK);
        gc.fillRoundRect(counterX, counterY, counterSize, counterSize, 12, 12);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRoundRect(counterX, counterY, counterSize, counterSize, 12, 12);

        String vieStr = String.valueOf(player.getLives());
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Consolas", counterSize * 0.7));
        Text text = new Text(vieStr);
        text.setFont(gc.getFont());
        double textWidth = text.getLayoutBounds().getWidth();
        double textHeight = text.getLayoutBounds().getHeight();
        double textX = counterX + (counterSize - textWidth) / 2;
        double textY = counterY + counterSize / 2 + textHeight / 3;
        gc.fillText(vieStr, textX, textY);

        if (player.isAI()) {
            gc.setFill(Color.ORANGE);
            gc.setFont(Font.font("Consolas", counterSize * 0.20));
            gc.fillText("IA", counterX + counterSize - 24, counterY + 18);
        }
    }

    /**
     * Charge une image depuis les ressources, avec gestion du cache.
     * @param path Chemin de l'image
     * @return Image chargée
     */
    public static Image safeImageFromResource(String path) {
        String fixedPath = path;
        if (fixedPath != null && (fixedPath.contains(":\\") || fixedPath.contains(":/") || fixedPath.startsWith("\\") || fixedPath.startsWith("/"))) {
            int idx = fixedPath.lastIndexOf("images");
            if (idx != -1) {
                fixedPath = "/" + fixedPath.substring(idx).replace("\\", "/");
            }
        }
        if (imageCache.containsKey(fixedPath)) {
            return imageCache.get(fixedPath);
        }
        InputStream is = GameController.class.getResourceAsStream(fixedPath);
        if (is == null) {
            throw new IllegalArgumentException("Image introuvable dans les ressources : " + fixedPath + " (original : " + path + ")");
        }
        Image img = new Image(is);
        imageCache.put(fixedPath, img);
        return img;
    }

    /**
     * Génère un canvas de preview pour l'écran de sélection de niveau.
     */
    public static Canvas createLevelPreviewCanvas(Level level, int cellSize) {
        int[][] preview = level.getLayout();
        int w = preview[0].length;
        int h = preview.length;

        Canvas canvas = new Canvas(w * cellSize, h * cellSize);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Image solImg = safeImageFromResource(level.getGroundImagePath());
        Image murImg = safeImageFromResource(level.getWallIndestructibleImagePath());
        Image blocImg = safeImageFromResource(level.getWallDestructibleImagePath());

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double px = x * cellSize;
                double py = y * cellSize;
                switch (preview[y][x]) {
                    case 1 -> gc.drawImage(murImg, px, py, cellSize, cellSize);
                    case 2 -> gc.drawImage(blocImg, px, py, cellSize, cellSize);
                    default -> gc.drawImage(solImg, px, py, cellSize, cellSize);
                }
            }
        }
        gc.setFill(new Color(0, 0, 0, 0.4));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        return canvas;
    }

    public static int getCellSize() { return DEFAULT_CELL_SIZE; }
}