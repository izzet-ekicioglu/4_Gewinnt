package game;

import processing.core.*;
import org.apache.logging.log4j.*;
import java.util.*;
import java.util.stream.*;

public class GUI extends PApplet {

    private static final String version = "0.001";
    private static final boolean debug = true;

    private static final Logger logger = LogManager.getLogger(GUI.class);

    private enum screens {MENUE, PLAY, LEVEL, ERROR}
    private screens screen;

    private List<Circle> circles;
    private List<Box> columns;

    private VG vg;
    private boolean player;
    private int numberTurns;
    private int playedColumn;

    private boolean isGameOver;
    private int p_winner;

    private boolean comPlayer;
    private boolean autoPlay = false;

    private final int sWidth = 1800;
    private final int sHeight = 1200;

    private PFont f;
    private PImage icon;

    private final int cWhite = color(255, 255, 255);
    private final int cWhiteOp = color(255, 255, 255, 155);
    private final int cGrey = color(200, 200, 200);
    private final int cDarkGrey = color(100, 100, 100);
    private final int cRed = color(200, 0, 0);
    private final int cBrightRed = color(255, 100, 100);
    private final int cYellow = color(255, 255, 0);
    private final int cBrightYellow = color(255, 255, 200);
    private final int cBlue = color(0, 60, 140);
    private final int cOceanBlue = color(100, 150, 255);
    private final int cDarkBlue = color(0, 0, 140);

    private Text menue_title;
    private Text select_dif;

    private Textbox tb_p1_vs_p2;
    private Textbox tb_p1_vs_com;
    private Textbox tb_quit;
    private Textbox tb_spieler1;
    private Textbox tb_spieler2;
    private Textbox tb_com;
    private Textbox tb_back;
    private Textbox tb_undo;
    private Textbox tb_autoTurn;
    private Textbox tb_menue;
    private Text txt_error;
    private Textbox tb_win_p1;
    private Textbox tb_win_p2;
    private Textbox tb_win_com;
    private Textbox tb_draw;
    private Textbox tb_restart;
    private Textbox tb_easy;
    private Textbox tb_mid;
    private Textbox tb_hard;

    private Box b_board;
    private Box layer;

    public static void main(String[] args) {
        if (debug) logger.info("<main()> VierGewinnt-game.GUI V" + version + " started.");
        PApplet.runSketch(new String[]{""}, new GUI());
    }

    public void settings() {
        size(sWidth, sHeight);
        screen = screens.MENUE;
    }

    public void setup() {
        f = createFont("fonts/Trebuc.ttf", 60);
        icon = loadImage("images/ico.png");

        noStroke();
        surface.setTitle("Connect 4");
        surface.setIcon(icon);
        setupButtons();
    }

    // draw - methods
    public void draw() {
        background(cOceanBlue);
        switch (screen) {
            case MENUE -> drawMenue();
            case PLAY -> drawGame();
            case LEVEL -> drawLevel();
            case ERROR -> drawError();
        }
    }

    private void drawMenue() {
        menue_title.draw();
        tb_p1_vs_p2.draw();
        tb_p1_vs_com.draw();
        tb_quit.draw();
    }

    private void drawLevel() {
        select_dif.draw();
        tb_easy.draw();
        tb_mid.draw();
        tb_hard.draw();
    }

    private void drawGame() {
        b_board.draw();
        columns.forEach(Box::draw);
        circles.forEach(Circle::draw);

        tb_undo.draw();
        tb_back.draw();

        if (!isGameOver) {
            if (autoPlay) autoTurn();
            if (comPlayer) tb_autoTurn.draw();
            if (player) tb_spieler1.draw();
            if (!player) {
                if (!comPlayer) tb_spieler2.draw();
                if (comPlayer) tb_com.draw();
            }

        }
        if (numberTurns > 0) {
            Textbox tb_lastMove = new Textbox(
                    new Box(width(0.3), heigth(0.9), width(0.4), heigth(0.05), cDarkGrey),
                    new Text(((player ? (comPlayer ? "COM " : "PLAYER 2 ") : "PLAYER 1 ") + "PLAYED COLUMN " + playedColumn),
                            cWhite, heigth(0.04), (comPlayer ?  (player ? width(0.362) : width(0.335)) : width(0.335)), heigth(0.94))
            );
            tb_lastMove.draw();
        }
        if (isGameOver) {
            tb_restart.draw();
            switch (p_winner) {
                case 1 -> tb_win_p1.draw();
                case 0 -> tb_draw.draw();
                case -1 -> {
                    if (!comPlayer) tb_win_p2.draw();
                    if (comPlayer) tb_win_com.draw();
                }
            }
        }
    }

    private void drawError() {
        txt_error.draw();
        tb_menue.draw();
        tb_quit.draw();
    }


    public void mouseClicked() {
        if (debug) logger.info("<mouseClicked()> Position (" + mouseX + " | " + mouseY + ")");
        switch (screen) {
            case MENUE -> {
                if (hover(tb_p1_vs_p2.b)) {
                    if (debug) logger.info("<mouseClicked()> (SCREEN " + screen + ") Button 'P VS P' ausgewaehlt.");
                    vg = VierGewinnt.start();
                    setupGame();
                    comPlayer = false;
                    screen = screens.PLAY;
                }
                if (hover(tb_p1_vs_com.b)) {
                    if (debug) logger.info("<mouseClicked()> (SCREEN " + screen + ") Button 'P VS COM' ausgewaehlt.");
                    screen = screens.LEVEL;
                }
                if (hover(tb_quit.b)) {
                    if (debug) logger.info("<mouseClicked()> (SCREEN " + screen + ") Button 'BEENDEN' ausgewaehlt.");
                    if (debug) logger.info("<mouseClicked()> (SCREEN " + screen + ") Beende Programm.");
                    exit();
                }
            }
            case LEVEL -> {
                if (hover(tb_easy.b)) {
                    if (debug) logger.info("<mouseClicked()> (SCREEN " + screen + ") Button 'EASY' ausgewaehlt.");
                    vg = VierGewinnt.start();
                    vg.setDifficulty(VG.difficulty.EASY);
                    setupGame();
                    comPlayer = true;
                    screen = screens.PLAY;
                }
                if (hover(tb_mid.b))  {
                    if (debug) logger.info("<mouseClicked()> (SCREEN " + screen + ") Button 'MID' ausgewaehlt.");
                    vg = VierGewinnt.start();
                    vg.setDifficulty(VG.difficulty.MID);
                    setupGame();
                    comPlayer = true;
                    screen = screens.PLAY;
                }
                if (hover(tb_hard.b))  {
                    if (debug) logger.info("<mouseClicked()> (SCREEN " + screen + ") Button 'HARD' ausgewaehlt.");
                    vg = VierGewinnt.start();
                    vg.setDifficulty(VG.difficulty.HARD);
                    setupGame();
                    comPlayer = true;
                    screen = screens.PLAY;
                }
            }
            case PLAY -> {
                if (hover(tb_back.b)) {
                    if (debug) logger.info("<mouseClicked()> (SCREEN " + screen + ") Button 'Zurueck' ausgewaehlt.");
                    screen = screens.MENUE;
                }
                if (hover(tb_undo.b)) {
                    if (debug) logger.info("<mouseClicked()> (SCREEN " + screen + ") Button 'Rueckgaengig' ausgewaehlt.");
                    undo();
                }
                if (!isGameOver && !autoPlay) {
                    columns.stream().filter(this::hover).forEach(column -> move(columns.indexOf(column)));
                    if (hover(tb_autoTurn.b)) {
                        if (debug) logger.info("<mouseClicked()> (SCREEN " + screen + ") Button 'AUTO PLAY' ausgewaehlt.");
                        layer.draw();
                        autoPlay = true;
                    }
                }
                if (isGameOver && hover(tb_restart.b)) {
                    if (debug) logger.info("<mouseClicked()> (SCREEN " + screen + ") Button 'Restart' ausgewaehlt.");
                    if (!comPlayer) vg = VierGewinnt.start();
                    if (comPlayer) screen = screens.LEVEL;
                }
            }
            case ERROR -> {
                if (hover(tb_quit.b)) {
                    if (debug) logger.info("<mouseClicked()> (SCREEN " + screen + ") Button 'Beenden' ausgewaehlt.");
                    if (debug) logger.info("<mouseClicked()> (SCREEN " + screen + ") Beende Programm.");
                    exit();
                }
                if (hover(tb_menue.b)) {
                    if (debug) logger.info("<mouseClicked()> (SCREEN " + screen + ") Button 'Menue' ausgewaehlt.");
                    screen = screens.MENUE;
                }
            }
        }
    }


    // game - methods
    private void setupGame() {
        player = vg.getPlayer();
        numberTurns = 0;
        circles = new ArrayList<>();
        columns = new ArrayList<>();
        for (double d1 = 0.2; d1 <= 0.8; d1 += 0.1 ) {
            columns.add(new Box(width(d1 - 0.05), heigth(0.2), width(0.1), heigth(0.635), cBlue));
            columns.get(columns.size() - 1).setAltCol(cDarkBlue);
            for (double d2 = 0.27; d2 <= 0.77; d2 += 0.1) {
                circles.add(new Circle(width(d1), heigth(d2), width(0.06), cWhite));
            }
        }
        isGameOver = false;
        p_winner = 0;
    }

    private void move(int column) {
        if (debug) logger.info("<move()> Spalte " + column + " ausgewaehlt.");
        if (IntStream.range(0, 6).allMatch(i -> circles.get((column * 6) + i).col != cWhite)) {
            if (debug) logger.info("<move()> Spalte " + column + " bereits voll.");
            return;
        }
        this.vg = vg.play(column);
        IntStream.range(0, 6).filter(i -> circles.get((column * 6) + i).col == cWhite).max().
                stream().forEach(max -> circles.get((column * 6) + max).setCol(player ? cRed : cYellow));
        numberTurns++;
        playedColumn = column;
        player = !player;
        if (!checkGame()) screen = screens.ERROR;
        if (vg.isGameOver()) {
            isGameOver = true;
            p_winner = vg.hasWon() ? (player ? -1 : 1) : 0;
        }
    }

    private void autoTurn() {
        vg = vg.bestMove();
        int index = vg.getHistory().get(vg.getHistory().size() - 1);
        circles.get(index).setCol(player ? cRed : cYellow);
        numberTurns++;
        playedColumn = (int)Math.floor(index / 6.0);
        player = !player;
        if (!checkGame()) screen = screens.ERROR;
        if (vg.isGameOver()) {
            isGameOver = true;
            p_winner = vg.hasWon() ? (player ? -1 : 1) : 0;
        }
        autoPlay = false;
    }

    private void undo() {
        if (numberTurns == 0) return;
        circles.get(vg.getHistory().get(vg.getHistory().size() - 1)).setCol(cWhite);
        this.vg = vg.undo();
        numberTurns--;
        player = !player;
        if (numberTurns > 0) playedColumn = (int)Math.floor(vg.getHistory().get(vg.getHistory().size() - 1) / 6.0);
        if (!checkGame()) screen = screens.ERROR;
        if (isGameOver) {
            isGameOver = false;
            p_winner = 0;
        }
    }

    private boolean checkGame() {
        if (circles.stream().allMatch(circle -> circle.col == cWhite) && vg.getHistory().isEmpty() && numberTurns == 0) {
            if (debug) logger.info("<checkGame()> Gegenpruefung mit game.VG erfolgreich. Beide Felder leer.");
            return true;
        }
        if (circles.get(vg.getHistory().get(vg.getHistory().size() - 1)).col == (player ? cYellow : cRed) &&
                (vg.getHistory().size() == numberTurns) && (vg.getPlayer() == player)) {
            if (debug) logger.info("<checkGame()> Gegenpruefung mit game.VG erfolgreich. Beide Felder identisch.");
            return true;
        }
        return false;
    }

    // visual elements
    private boolean hover(Box b) {
        return mouseX >= b.x && mouseX <= (b.x + b.width) && mouseY >= b.y && mouseY <= (b.y + b.height);
    }

    private void setupButtons() {
        menue_title = new Text("CONNECT 4", cWhite, heigth(0.15), width(0.24), heigth(0.3));

        select_dif = new Text("Choose Level", cWhite, heigth(0.15), width(0.205), heigth(0.3));

        tb_easy = new Textbox(
                new Box(width(0.3), heigth(0.39), width(0.4), heigth(0.1), cBlue),
                new Text("EASY", cWhite, heigth(0.08), width(0.445), heigth(0.47))
        );
        tb_easy.b.setAltCol(cGrey);

        tb_mid = new Textbox(
                new Box(width(0.3), heigth(0.5), width(0.4), heigth(0.1), cBlue),
                new Text("MID", cWhite, heigth(0.08), width(0.462), heigth(0.58))
        );
        tb_mid.b.setAltCol(cGrey);

        tb_hard = new Textbox(
                new Box(width(0.3), heigth(0.61), width(0.4), heigth(0.1), cBlue),
                new Text("HARD", cWhite, heigth(0.08), width(0.44), heigth(0.69))
        );
        tb_hard.b.setAltCol(cGrey);

        tb_p1_vs_p2 = new Textbox(
                new Box(width(0.3), heigth(0.39), width(0.4), heigth(0.1), cBlue),
                new Text("P1 VS P2", cWhite, heigth(0.08), width(0.4), heigth(0.47))
        );
        tb_p1_vs_p2.b.setAltCol(cGrey);

        tb_p1_vs_com = new Textbox(
                new Box(width(0.3), heigth(0.5), width(0.4), heigth(0.1), cBlue),
                new Text("P1 VS COM", cWhite, heigth(0.08), width(0.375), heigth(0.58))
        );
        tb_p1_vs_com.b.setAltCol(cGrey);

        tb_quit = new Textbox(
                new Box(width(0.3), heigth(0.61), width(0.4), heigth(0.1), cBlue),
                new Text("QUIT", cWhite, heigth(0.08), width(0.44), heigth(0.69))
        );
        tb_quit.b.setAltCol(cGrey);

        tb_spieler1 = new Textbox(
                new Box(width(0.3), heigth(0.02), width(0.4), heigth(0.05), cBrightRed),
                new Text("PLAYER 1 TURN", cWhite, heigth(0.04), width(0.41), heigth(0.06))
        );

        tb_spieler2 = new Textbox(
                new Box(width(0.3), heigth(0.02), width(0.4), heigth(0.05), cBrightYellow),
                new Text("PLAYER 2 TURN", cDarkGrey, heigth(0.04), width(0.41), heigth(0.06))
        );

        tb_com = new Textbox(
                new Box(width(0.3), heigth(0.02), width(0.4), heigth(0.05), cBrightYellow),
                new Text("COM TURN", cDarkGrey, heigth(0.04), width(0.44), heigth(0.06))
        );

        tb_win_p1 = new Textbox(
                new Box(width(0.3), heigth(0.02), width(0.4), heigth(0.05), cRed),
                new Text("PLAYER 1 WON!", cWhite, heigth(0.04), width(0.41), heigth(0.06))
        );

        tb_win_p2 = new Textbox(
                new Box(width(0.3), heigth(0.02), width(0.4), heigth(0.05), cYellow),
                new Text("PLAYER 2 WON!", cDarkGrey, heigth(0.04), width(0.41), heigth(0.06))
        );

        tb_win_com = new Textbox(
                new Box(width(0.3), heigth(0.02), width(0.4), heigth(0.05), cYellow),
                new Text("COM WON!", cDarkGrey, heigth(0.04), width(0.44), heigth(0.06))
        );

        tb_draw = new Textbox(
                new Box(width(0.3), heigth(0.02), width(0.4), heigth(0.05), cBlue),
                new Text("DRAW!", cWhite, heigth(0.04), width(0.46), heigth(0.06))
        );

        tb_restart = new Textbox(
                new Box(width(0.41), heigth(0.09), width(0.18), heigth(0.05), cBlue),
                new Text("RESTART", cWhite, heigth(0.04), width(0.45), heigth(0.13))
        );
        tb_restart.b.setAltCol(cGrey);

        tb_back  = new Textbox(
                new Box(width(0.02), heigth(0.02), width(0.15), heigth(0.05), cBlue),
                new Text("BACK", cWhite, heigth(0.04), width(0.06), heigth(0.06))
        );
        tb_back.b.setAltCol(cGrey);

        tb_undo = new Textbox(
                new Box(width(0.83), heigth(0.02), width(0.15), heigth(0.05), cBlue),
                new Text("UNDO", cWhite, heigth(0.04), width(0.87), heigth(0.06))
        );
        tb_undo.b.setAltCol(cGrey);

        tb_autoTurn = new Textbox(
                new Box(width(0.83), heigth(0.09), width(0.15), heigth(0.05), cBlue),
                new Text("AUTO PLAY", cWhite, heigth(0.04), width(0.84), heigth(0.13))
        );
        tb_autoTurn.b.setAltCol(cGrey);

        tb_menue = new Textbox(
                new Box(width(0.3), heigth(0.5), width(0.4), heigth(0.1), cBlue),
                new Text("MENUE", cWhite, heigth(0.08), width(0.42), heigth(0.58))
        );
        tb_menue.b.setAltCol(cGrey);

        txt_error = new Text("ERROR", cRed, heigth(0.15), width(0.35), heigth(0.4));

        b_board = new Box(width(0.14), heigth(0.19), width(0.72), heigth(0.655), cDarkBlue);

        layer = new Box(0, 0, sWidth, sHeight, cWhiteOp);

        if (debug) logger.info("<setupButtons()> game.GUI-Objekte erstellt.");
    }

    private long heigth(double d) {
        return Math.round(d * sHeight);
    }

    private long width(double d) {
        return Math.round(d * sWidth);
    }

    // INNER CLASSES
    class Object {
        long x, y;
        int col;
        int col2;
        boolean altCol;
        Object(long x, long y, int col) {
            this.x = x;
            this.y = y;
            this.col = col;
            this.altCol = false;
        }
        void setCol(int col) {
            this.col = col;
        }
        void setAltCol(int col) {
            this.altCol = true;
            this.col2 = col;
        }
    }

    class Circle extends Object {
        long size;
        Circle(long x, long y, long size, int col) {
            super(x, y, col);
            this.size = size;
        }
        void draw() {
            fill(this.col);
            circle(x, y, size);
        }
    }

    class Textbox{
        Box b;
        Text t;
        Textbox(Box b, Text t) {
            this.b = b;
            this.t = t;
        }
        void draw() {
            b.draw();
            t.draw();
        }
    }

    class Box extends Object {
        long width, height;
        Box (long x, long y, long width, long height, int col) {
            super(x, y, col);
            this.width = width;
            this.height = height;
        }
        void draw() {
            fill(hover(this) ? (this.altCol ? this.col2 : this.col) : this.col);
            rect(this.x, this.y, this.width, this.height);
        }
    }

    class Text extends Object {
        String text;
        long size;
        Text(String text, int col, long size, long x, long y) {
            super(x, y, col);
            this.text = text;
            this.size = size;
        }
        void draw() {
            fill(this.col);
            textFont(f, this.size);
            text(this.text, this.x, this.y);
        }
    }

}
