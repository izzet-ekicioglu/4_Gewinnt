package game;

import java.util.*;
import java.util.stream.*;
import org.apache.logging.log4j.*;

public class VierGewinnt implements VG {

    private static final String version = "0.001";

    private static final Logger logger = LogManager.getLogger(VierGewinnt.class);
    private static final boolean debug = true;

    private static Random r;

    private static int difficulty;
    public static boolean player;
    private static boolean gameOver;

    private final List<Integer> columns;
    private final List<Integer> history;

    public static VierGewinnt start() {
        if (debug) logger.info("<start()> VierGewinnt V" + version + " gestartet.");
        r = new Random();
        difficulty = 0;
        player = true;
        return new VierGewinnt(Collections.nCopies(42, 0).stream(), Stream.empty());
    }

    private static VierGewinnt of(Stream<Integer> columns, Stream<Integer> history) {
        return new VierGewinnt(columns, history);
    }

    private VierGewinnt(Stream<Integer> columns, Stream<Integer> history) {
        this.columns = columns.collect(Collectors.toList());
        this.history = history.collect(Collectors.toList());
        if (debug) logger.info("<Constructor> Neues Spielfeld erstellt.");
        gameOver = isGameOver();
        if (debug && gameOver) logger.info("<Constructor> Spiel beendet.");
        if (debug && !gameOver) logger.info("<Constructor> " + (player ? "Spieler 1 " : "Spieler 2 ") + "am Zug.");
    }

    public void setDifficulty(difficulty d) {
        if (d == VG.difficulty.EASY) difficulty = 10;
        if (d == VG.difficulty.MID) difficulty = 100;
        if (d == VG.difficulty.HARD) difficulty = 300;
        if (debug) logger.info("<setDifficulty()> Anzahl Iterationen fuer bestMove() auf " + difficulty + " gesetzt.");
    }

    public VierGewinnt bestMove() {
        assert (difficulty != 0) : "Schwierigkeitsstufe nicht festgelegt";

        if (debug) logger.info("<bestMove()> Gestartet.");
        long startTime = System.currentTimeMillis();

        ArrayList<Integer> results = new ArrayList<>();
        IntStream.range(0, 7).forEach(i -> results.add(0));

        List<Integer> possibleTurns = possibleTurns(this.columns).collect(Collectors.toList());

        possibleTurns.stream().parallel().forEach(turn -> {
            List<Integer> tmp_columns = new ArrayList<>(columns);
            tmp_columns = play(tmp_columns.stream(), turn, player).collect(Collectors.toList());
            results.set(turn, min(!player, tmp_columns, 2));
            logger.info("<bestMove()> Gewinnbewertung fuer Spiel in Spalte " + turn + " entspricht: " + results.get(turn));
        });

        if (debug) logger.info("<bestMove()> Gewinnmoeglichkeiten: -> " + actMoves(results));
        int toPlay = results.indexOf(Collections.max(results));
        if (results.stream().allMatch(i -> i == 0)) toPlay = possibleTurns.get(0);

        long endTime = System.currentTimeMillis();
        if (debug) logger.info("<bestMove()> Ergebnis: Reihe " + toPlay + ". Benoetigte Rechenzeit " + (endTime - startTime) + "ms.");
        return play(toPlay);
    }

    private int max(boolean act_player, List<Integer> tmp_columns, int depth) {
        List<Integer> possibleTurns = possibleTurns(tmp_columns).collect(Collectors.toList());

        int evaluation = evaluateMinMax(act_player, tmp_columns, possibleTurns, depth);
        if (evaluation >= 0) return evaluation;

        ArrayList<Integer> maxValue = new ArrayList<>();
        maxValue.add(0);

        possibleTurns.stream().parallel().forEach(turn -> {
            if (maxValue.get(0) != 100) {
                List<Integer> act_columns = new ArrayList<>(tmp_columns);
                act_columns = play(act_columns.stream(), turn, act_player).collect(Collectors.toList());
                int newValue = min(!act_player, act_columns, depth - 1);
                if (newValue > maxValue.get(0)) maxValue.set(0, newValue);
            }
        });

        return maxValue.get(0);
    }

    private int min(boolean act_player, List<Integer> tmp_columns, int depth) {
        List<Integer> possibleTurns = possibleTurns(tmp_columns).collect(Collectors.toList());

        int evaluation = evaluateMinMax(act_player, tmp_columns, possibleTurns, depth);
        if (evaluation >= 0) return evaluation;

        ArrayList<Integer> minValue = new ArrayList<>();
        minValue.add(100);

        possibleTurns.stream().parallel().forEach(turn -> {
            if (minValue.get(0) != 0) {
                List<Integer> act_columns = new ArrayList<>(tmp_columns);
                act_columns = play(act_columns.stream(), turn, act_player).collect(Collectors.toList());
                int newValue = max(!act_player, act_columns, depth - 1);
                if (newValue < minValue.get(0)) minValue.set(0, newValue);
            }
        });

        return minValue.get(0);
    }

    private int evaluateMinMax(boolean act_player, List<Integer> tmp_columns, List<Integer> possibleTurns, int depth) {
        if (hasWon(tmp_columns, true)) return player == !act_player ? (100 - (2 - depth)) : 0;
        if (tmp_columns.stream().allMatch(x -> x != 0)) return 50;
        if (depth == 0) return monteCarlo(act_player, tmp_columns, possibleTurns);

        depth--;
        for (int turn : possibleTurns) {
            List<Integer> act_columns = new ArrayList<>(tmp_columns);
            act_columns = play(act_columns.stream(), turn, act_player).collect(Collectors.toList());
            if (hasWon(act_columns, true)) {
                return player == act_player ? (100 - (2 - depth)) : 0;
            }
        }
        return -1;
    }

    private int monteCarlo(boolean act_player, List<Integer> tmp_columns, List<Integer> possibleTurns) {
        ArrayList<Integer> moves = new ArrayList<>();
        IntStream.range(0, 7).forEach(i -> moves.add(0));

        IntStream.range(0, difficulty).parallel().forEach(turn -> {
            List<Integer> act_columns = new ArrayList<>(tmp_columns);
            int selected_column = possibleTurns.get(r.nextInt(possibleTurns.size()));
            act_columns = play(act_columns.stream(), selected_column, act_player).collect(Collectors.toList());
            if (monteCarloCalc(act_player, act_columns)) {
                moves.set(selected_column, moves.get(selected_column) + 1);
            }
        });

        if (debug) logger.info("<monteCarlo()> Gewinnmoeglichkeiten in Prozent: -> " + actMoves(moves));
        if (debug) logger.info("<monteCarlo()> Durchschnitt: -> " +
                Math.round((moves.stream().mapToInt(Integer::intValue).sum() * 100 / (double) difficulty)) + "%");
        return (int) Math.round((moves.stream().mapToInt(Integer::intValue).sum() * 100 / (double) difficulty));
    }

    private boolean monteCarloCalc(boolean act_player, List<Integer> tmp_columns) {
        List<Integer> possibleTurns = possibleTurns(tmp_columns).collect(Collectors.toList());
        int selected_column = possibleTurns.get(r.nextInt(possibleTurns.size()));
        tmp_columns = play(tmp_columns.stream(), selected_column, act_player).collect(Collectors.toList());

        if (hasWon(tmp_columns, true)) return player == act_player;
        if (tmp_columns.stream().allMatch(i -> i != 0)) return false;

        return monteCarloCalc(!act_player, tmp_columns);
    }

    private String actMoves(ArrayList<Integer> moves) {
        assert moves != null : "actMoves() -> Uebergebenes Array leer";
        assert moves.size() == 7 : "actMoves() -> Groesse des uebergebenen Arrays passt nicht";
        StringBuilder out = new StringBuilder();
        for (int m : moves) out.append("| ").append(m).append(" |");
        return out.toString();
    }

    public VierGewinnt undo() {
        assert !getHistory().isEmpty() : "undo() -> Noch keine Spielzuege!";

        player = !player;
        List<Integer> tmp_columns = new ArrayList<>(this.columns);
        int index = this.history.get(this.history.size() - 1);
        tmp_columns.set(index, 0);
        if (debug) {
            int column = (int) Math.floor(index / 6.0);
            int row = index % 6;
            logger.info("<undo()> Setze Position (" + column + " | " + row + ") von " +
                    this.columns.get(index) + " zurueck auf " + tmp_columns.get(index) + ".");
            logger.info("<undo()> " + (player ? "Spieler 1 " : "Spieler 2 ") + "am Zug.");
        }
        return VierGewinnt.of(tmp_columns.stream(), this.history.stream().limit(this.history.size() - 1));
    }

    private Stream<Integer> play(Stream<Integer> columns, int column, boolean player) {
        assert (column >= 0 && column < 7) : "play() -> Spalte " + column + " ungueltig.";
        List<Integer> tmp_columns = columns.collect(Collectors.toList());
        assert (IntStream.range(0, 5).anyMatch(iteration -> (tmp_columns.get((column * 6) + iteration) == 0))) :
                "play() -> Alle Felder in Spalte " + column + " voll.";

        IntStream.range(0, 6).filter(i -> tmp_columns.get((column * 6) + i) == 0).max().
                stream().forEach(max -> tmp_columns.set((column * 6) + max, player ? 1 : -1));
        return tmp_columns.stream();
    }

    public VierGewinnt play(int column) {
        assert !gameOver : "play() -> Spiel bereits beendet.";
        assert (column >= 0 && column < 7) : "play() -> Spalte " + column + " ungueltig.";
        assert (IntStream.range(0, 5).anyMatch(iteration -> (columns.get((column * 6) + iteration) == 0))) :
                "play() -> Alle Felder in Spalte " + column + " voll.";

        List<Integer> tmp_columns = new ArrayList<>(columns);
        List<Integer> tmp_history = new ArrayList<>(history);
        IntStream.range(0, 6).filter(i -> tmp_columns.get((column * 6) + i) == 0).max().stream().forEach(max -> {
            tmp_columns.set((column * 6) + max, player ? 1 : -1);
            tmp_history.add((column * 6) + max);
            if (debug) logger.info("<play()> Setze Position (" + column + " | " + max + ") auf "
                    + tmp_columns.get((column * 6) + max) + ".");
        });
        player = !player;
        return VierGewinnt.of(tmp_columns.stream(), tmp_history.stream());
    }

    private boolean hasWon(List<Integer> columns, boolean bestMove) {
        assert columns.size() == 42 : "hasWon() -> Groesse des uebergebenen Arrays stimmt nicht";
        assert columns.stream().allMatch(i -> i == 0 || i == 1 || i == -1) : "hasWon() -> Falsche Werte im Array";

        if (columns.stream().filter(pos -> pos != 0).count() < 7) return false;

        if (IntStream.range(0, 42).parallel().filter(i -> i % 6 == 0 || i % 6 == 1 || i % 6 == 2).filter(f -> columns.get(f) != 0).
                anyMatch(j -> (IntStream.range(1, 4).allMatch(k -> Objects.equals(columns.get(j), columns.get(j + k)))))) {
            if (debug && !bestMove) logger.info("<hasWon()> Vertikale Siegesposition gefunden. " +
                    (player ? "Spieler 2 " : "Spieler 1 ") + "hat gewonnen.");
            return true;
        }
        if (IntStream.range(0, 24).parallel().filter(f -> columns.get(f) != 0).
                anyMatch(i -> (IntStream.range(1, 4).allMatch(k -> Objects.equals(columns.get(i), columns.get(i + (k * 6))))))) {
            if (debug && !bestMove) logger.info("<hasWon()> Horizontale Siegesposition gefunden. " +
                    (player ? "Spieler 2 " : "Spieler  ") + "hat gewonnen.");
            return true;
        }
        if (IntStream.range(0, 24).parallel().filter(i -> i % 6 == 3 || i % 6 == 4 || i % 6 == 5).filter(f -> columns.get(f) != 0).
                anyMatch(j -> (IntStream.range(1, 4).allMatch(k -> Objects.equals(columns.get(j), columns.get(j + ((k * 6) - k))))))) {
            if (debug && !bestMove) logger.info("<hasWon()> Diagonal-Aufsteigende Siegesposition gefunden. " +
                    (player ? "Spieler 2 " : "Spieler 1 ") + "hat gewonnen.");
            return true;
        }
        if (IntStream.range(0, 24).parallel().filter(i -> i % 6 == 0 || i % 6 == 1 || i % 6 == 2).filter(f -> columns.get(f) != 0).
                anyMatch(j -> (IntStream.range(1, 4).allMatch(k -> Objects.equals(columns.get(j), columns.get(j + ((k * 6) + k))))))) {
            if (debug && !bestMove) logger.info("<hasWon()> Diagonal-Absteigende Siegesposition gefunden. " +
                    (player ? "Spieler 2 " : "Spieler 1 ") + "hat gewonnen.");
            return true;
        }
        return false;
    }

    public boolean hasWon() {
        return hasWon(this.columns, false);
    }

    public List<Integer> getHistory() {
        return new ArrayList<>(history);
    }

    private Stream<Integer> possibleTurns(List<Integer> columns) {
        assert columns.size() == 42 : "possibleTurns() -> Spielfeldgroesse stimmt nicht ueberein";
        return IntStream.range(0, 7).parallel().filter(i -> IntStream.range(0, 6).parallel().anyMatch(j -> columns.get((i * 6) + j) == 0)).boxed();
    }

    public void printField() {
        List<String> strings = new ArrayList<>();
        IntStream.range(0, 6).forEach(i -> IntStream.range(0, 42).filter(j -> j % 6 == i).
                forEach(k -> strings.add(k < 36 ? (this.columns.get(k) + "\t") : (this.columns.get(k) + "\n"))));
        String field = String.join("", strings);
        logger.info("<printField()> Aktuelles Feld:\n" + field);
    }
}