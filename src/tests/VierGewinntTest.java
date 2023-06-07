package tests;

import game.VG;
import game.VierGewinnt;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class VierGewinntTest {

    private static final Logger logger = LogManager.getLogger(VierGewinntTest.class);
    private static final Level TEST = Level.forName("TEST", 550);

    VierGewinnt vg;

    @AfterAll
    static void end() {
        logger.log(TEST, "Tests erfolgreich durchgefuehrt.");
    }

    @Test
    void history() {
        for (int x = 0; x < 7; x++) {
            vg = VierGewinnt.start();
            for (int y = 0; y < 6; y++) {
                vg = vg.play(x);
                assertEquals(((x * 6) + (5 - y)), (int) vg.getHistory().get(vg.getHistory().size() - 1));
            }
        }
        logger.log(TEST, "<getHistory()> Erfolgreich getestet.");
    }

    @Test
    void play() {
        vg = VierGewinnt.start();

        vg = VierGewinnt.start();
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 6; y++) {
                logger.log(TEST, "<play(int column)> Spiele in Spalte " + x + ".");
                vg = vg.play(x);
                vg.printField();
                assertTrue((vg.getHistory().get(vg.getHistory().size() - 1) == ((x * 6) + (5 - y))));
            }
        }
        for (int y = 0; y < 6; y++) {
            for (int x = 6; x > 3; x--) {
                logger.log(TEST, "<play(int column)> Spiele in Spalte " + x + ".");
                vg = vg.play(x);
                vg.printField();
                assertTrue((vg.getHistory().get(vg.getHistory().size() - 1) == ((x * 6) + (5 - y))));
            }
        }
        logger.log(TEST, "<play(int column)> Erfolgreich getestet.");
    }

    @Test
    void undo() {
        play();
        for (int i = 0; i < 18; i++) {
            while (vg.getHistory().size() > 1) {
                int last = vg.getHistory().get(vg.getHistory().size() - 2);
                vg = vg.undo();
                vg.printField();
                assertEquals(last, (int) vg.getHistory().get(vg.getHistory().size() - 1));
            }
            if (vg.getHistory().size() == 1) {
                vg = vg.undo();
                vg.printField();
                assertTrue(vg.getHistory().isEmpty());
            }
        }
        try {
            vg = vg.undo();
        } catch (AssertionError assertionError) {
            logger.log(TEST, "<undo()> Assertion Error.");
        }
    }

    @Test
    void verticalWin() {
        for (int pos = 0; pos <= 6; pos++) {
            vg = VierGewinnt.start();
            vg = vg.play(pos);
            vg = vg.play(pos + ((pos < 6) ? 1 : -1));
            vg = vg.play(pos);
            vg = vg.play(pos + ((pos < 6) ? 1 : -1));
            vg = vg.play(pos);
            vg = vg.play(pos + ((pos < 6) ? 1 : -1));
            vg = vg.play(pos);
            assertTrue(vg.hasWon());
        }
        logger.log(TEST, "<hasWon()> Fuer vertikale Siegespositionen erfolgreich getestet.");
    }

    @Test
    void horizontalWin() {
        for (int pos = 0; pos <= 3; pos++) {
            vg = VierGewinnt.start();
            vg = vg.play(pos);
            vg = vg.play(pos);
            vg = vg.play(1 + pos);
            vg = vg.play(1 + pos);
            vg = vg.play(2 + pos);
            vg = vg.play(2 + pos);
            vg = vg.play(3 + pos);
            assertTrue(vg.hasWon());
        }
        logger.log(TEST, "<hasWon()> Fuer horizontale Siegespositionen erfolgreich getestet.");
    }

    @Test
    void diagonalUpWin() {
        for (int pos = 0; pos <= 3; pos++) {
            vg = VierGewinnt.start();
            vg = vg.play(pos);
            vg = vg.play(1 + pos);
            vg = vg.play(1 + pos);
            vg = vg.play(2 + pos);
            vg = vg.play(2 + pos);
            vg = vg.play(3 + pos);
            vg = vg.play(2 + pos);
            vg = vg.play(3 + pos);
            vg = vg.play(3 + pos);
            vg = vg.play(pos);
            vg = vg.play(3 + pos);
            assertTrue(vg.hasWon());
        }
        logger.log(TEST, "<hasWon()> Fuer diagonal-aufsteigende Siegespositionen erfolgreich getestet.");
    }

    @Test
    void diagonalDownWin() {
        for (int pos = 0; pos <= 3; pos++) {
            vg = VierGewinnt.start();
            vg = vg.play(pos);
            vg = vg.play(pos);
            vg = vg.play(pos);
            vg = vg.play(1 + pos);
            vg = vg.play(pos);
            vg = vg.play(1 + pos);
            vg = vg.play(1 + pos);
            vg = vg.play(2 + pos);
            vg = vg.play(2 + pos);
            vg = vg.play(1 + pos);
            vg = vg.play(3 + pos);
            assertTrue(vg.hasWon());
        }
        logger.log(TEST, "<hasWon()> Fuer diagonal-absteigende Siegespositionen erfolgreich getestet.");
    }

    @Test
    void bestMoveVerticalWin() {
        for (int pos = 0; pos <= 6; pos++) {
            vg = VierGewinnt.start();
            vg.setDifficulty(VG.difficulty.HARD);
            vg = vg.play(pos);
            vg = vg.play(pos + ((pos < 6) ? 1 : -1));
            vg = vg.play(pos);
            vg = vg.play(pos + ((pos < 6) ? 1 : -1));
            vg = vg.play(pos);
            vg = vg.play(pos + ((pos < 6) ? 1 : -1));
            vg = vg.bestMove();
            vg.printField();
            assertTrue(vg.hasWon());
        }
        logger.log(TEST, "<bestMove()> Fuer vertikale Siegespositionen erfolgreich getestet.");
    }

    @Test
    void bestMoveHorizontalWin() {
        for (int pos = 0; pos <= 3; pos++) {
            vg = VierGewinnt.start();
            vg.setDifficulty(VG.difficulty.HARD);
            vg = vg.play(pos);
            vg = vg.play(pos);
            vg = vg.play(1 + pos);
            vg = vg.play(1 + pos);
            vg = vg.play(2 + pos);
            vg = vg.play(2 + pos);
            vg = vg.bestMove();
            vg.printField();
            assertTrue(vg.hasWon());
        }
        logger.log(TEST, "<bestMove()> Fuer horizontale Siegespositionen erfolgreich getestet.");
    }

    @Test
    void bestMoveDiagonalUpWin() {
        for (int pos = 0; pos <= 3; pos++) {
            vg = VierGewinnt.start();
            vg.setDifficulty(VG.difficulty.HARD);
            vg = vg.play(pos);
            vg = vg.play(1 + pos);
            vg = vg.play(1 + pos);
            vg = vg.play(2 + pos);
            vg = vg.play(2 + pos);
            vg = vg.play(3 + pos);
            vg = vg.play(2 + pos);
            vg = vg.play(3 + pos);
            vg = vg.play(3 + pos);
            vg = vg.play(pos);
            vg = vg.bestMove();
            vg.printField();
            assertTrue(vg.hasWon());
        }
        logger.log(TEST, "<bestMove()> Fuer diagonal-aufsteigende Siegespositionen erfolgreich getestet.");
    }

    @Test
    void bestMoveDiagonalDownWin() {
        for (int pos = 0; pos <= 3; pos++) {
            vg = VierGewinnt.start();
            vg.setDifficulty(VG.difficulty.HARD);
            vg = vg.play(pos);
            vg = vg.play(pos);
            vg = vg.play(pos);
            vg = vg.play(1 + pos);
            vg = vg.play(pos);
            vg = vg.play(1 + pos);
            vg = vg.play(1 + pos);
            vg = vg.play(2 + pos);
            vg = vg.play(2 + pos);
            vg = vg.play(1 + pos);
            vg = vg.bestMove();
            vg.printField();
            assertTrue(vg.hasWon());
        }
        logger.log(TEST, "<bestMove()> Fuer diagonal-absteigende Siegespositionen erfolgreich getestet.");
    }
}