package game;

import java.util.List;

public interface VG {
    enum difficulty {EASY, MID, HARD};
    VG play(int column);
    VG undo();
    void setDifficulty(difficulty d);
    VG bestMove();
    List<Integer> getHistory();
    boolean hasWon();
    default boolean isGameOver() {
        return hasWon() || getHistory().size() == 42;
    }
    default boolean getPlayer() {
        return getHistory().size() % 2 == 0;
    }
}

