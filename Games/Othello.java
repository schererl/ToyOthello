package Games;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/* TODO: APPLY (virar tudo) */
public class Othello extends Game {
    public final static int PLAYERS_COUNT = 2;

    public final int size;
    public int[][] grid;

    private int pass = 0;
    private double[] ut;

    public Othello(final int sz, final int startPlayer) {
        super(startPlayer, 2);
        size = sz;
        mover = startPlayer;
        grid = new int[size][size];
        numTurn = 0;
        this.ut = new double[PLAYERS_COUNT];
        initPos();
    }

    public Othello(final int sz, final int mover, int turns) {
        super(turns, mover, 2);
        size = sz;
        grid = new int[size][size];
        this.ut = new double[PLAYERS_COUNT];
        initPos();
    }

    public Othello(final int sz, final int startPlayer, int[][] grid) {
        super(startPlayer, 2);
        size = sz;
        mover = startPlayer;
        this.grid = new int[size][size];
        this.ut = new double[PLAYERS_COUNT];
        numTurn = 0;

        if (sz > grid.length) {
            int offset = sz - grid.length;

            for (int i = 0; i < grid.length; i++) {
                for (int j = 0; j < grid.length; j++) {
                    this.grid[i][j] = grid[i + offset][j + offset];
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    this.grid[i][j] = grid[i][j];
                }
            }
        }
    }

    @Override
    public int playerIndex() {
        return mover - 1;
    }

    @Override
    public Game copy() {
        Othello instance = new Othello(this.size, mover, this.numTurn);
        instance.pass = this.pass;
        instance.numTurn = this.numTurn;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                instance.grid[i][j] = this.grid[i][j];
            }
        }
        return instance;
    }

    public void initPos() {
        final int W1i = size / 2 + 1;
        final int W1j = size / 2;
        final int W2i = size / 2;
        final int W2j = size / 2 + 1;
        grid[W1i - 1][W1j - 1] = BLACK_UP;
        grid[W2i - 1][W2j - 1] = BLACK_UP;

        final int B1i = size / 2;
        final int B1j = size / 2;
        final int B2i = size / 2 + 1;
        final int B2j = size / 2 + 1;
        grid[B1i - 1][B1j - 1] = WHITE_UP;
        grid[B2i - 1][B2j - 1] = WHITE_UP;
    }

    @Override
    public ArrayList<Move> moves() {
        ArrayList<Move> movs = new ArrayList<Move>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == 0 && validMove(i, j)) {
                    Move m = new Move();
                    m.setPosInsertion(i, j);
                    movs.add(m);
                }
            }
        }
        // if no move can be done, pass.
        if (movs.size() == 0) {
            Move m = new Move();
            m.setPosInsertion(-1, -1);
            movs.add(m);
        } // PASS

        return movs;
    }

    @Override
    public void apply(final Move m) {
        this.numTurn++;

        int[] move = m.posInsertion;
        if (move[0] == -1 && move[1] == -1) {
            pass++;
            mover = mover == 1 ? 2 : 1;
            return;
        }

        grid[move[0]][move[1]] = mover;
        int oppCol = mover == BLACK_UP ? WHITE_UP : BLACK_UP;

        int row = move[0];
        int col = move[1];

        if (row + 1 < size && col + 1 < size && grid[row + 1][col + 1] == oppCol) {
            flip(row + 1, col + 1, 1, 1, oppCol);
        }

        // :: DOWN
        if (row + 1 < size && grid[row + 1][col] == oppCol) {
            flip(row + 1, col, 1, 0, oppCol);
        }

        // :: RIGHT
        if (col + 1 < size && grid[row][col + 1] == oppCol) {
            flip(row, col + 1, 0, 1, oppCol);
        }

        // :: LEFT
        if (col - 1 > -1 && grid[row][col - 1] == oppCol) {
            flip(row, col - 1, 0, -1, oppCol);
        }

        if (row - 1 > -1 && col - 1 > -1 && grid[row - 1][col - 1] == oppCol) {
            flip(row - 1, col - 1, -1, -1, oppCol);
        }

        // :: UP
        if (row - 1 > -1 && grid[row - 1][col] == oppCol) {
            flip(row - 1, col, -1, 0, oppCol);
        }

        if (row - 1 > -1 && col + 1 < size && grid[row - 1][col + 1] == oppCol) {
            flip(row - 1, col + 1, -1, 1, oppCol);
        }

        if (row + 1 < size && col - 1 > -1 && grid[row + 1][col - 1] == oppCol) {
            flip(row + 1, col - 1, 1, -1, oppCol);
        }
        mover = mover == 1 ? 2 : 1;
        pass = 0;
    }

    @Override
    public Boolean isTerminal() {
        int countBlack = 0;
        int countWhite = 0;

        boolean terminal = false;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == BLACK_UP) {
                    countBlack++;
                } else if (grid[i][j] == WHITE_UP) {
                    countWhite++;
                }
            }
        }
        if (countBlack == size * size || countWhite == size * size || pass >= 2) {
            terminal = true;

            if (countBlack > countWhite) {
                ut[Othello.BLACK_UP - 1] = 1;
                ut[Othello.WHITE_UP - 1] = -1;
            } else if (countBlack < countWhite) {
                ut[Othello.BLACK_UP - 1] = -1;
                ut[Othello.WHITE_UP - 1] = 1;
            } else {
                ut[Othello.BLACK_UP - 1] = 0;
                ut[Othello.WHITE_UP - 1] = 0;
            }
        }
        return terminal;
    }

    @Override
    public double[] utilities() {
        return this.ut;
    }

    private boolean validMove(int row, int col) {
        int oppCol = mover == BLACK_UP ? WHITE_UP : BLACK_UP;

        // current
        if (grid[row][col] == EMPTY) {

            if (row + 1 < size && col + 1 < size && grid[row + 1][col + 1] == oppCol
                    && direction(row + 1, col + 1, 1, 1)) {
                return true;
            }

            // :: DOWN
            else if (row + 1 < size && grid[row + 1][col] == oppCol && direction(row + 1, col, 1, 0)) {
                return true;
            }

            // :: RIGHT
            else if (col + 1 < size && grid[row][col + 1] == oppCol && direction(row, col + 1, 0, 1)) {
                return true;
            }

            // :: LEFT
            else if (col - 1 > -1 && grid[row][col - 1] == oppCol && direction(row, col - 1, 0, -1)) {
                return true;
            }

            else if (row - 1 > -1 && col - 1 > -1 && grid[row - 1][col - 1] == oppCol
                    && direction(row - 1, col - 1, -1, -1)) {
                return true;
            }

            // :: UP
            else if (row - 1 > -1 && grid[row - 1][col] == oppCol && direction(row - 1, col, -1, 0)) {
                return true;
            }

            else if (row - 1 > -1 && col + 1 < size && grid[row - 1][col + 1] == oppCol
                    && direction(row - 1, col + 1, -1, 1)) {
                return true;
            }

            else if (row + 1 < size && col - 1 > -1 && grid[row + 1][col - 1] == oppCol
                    && direction(row + 1, col - 1, 1, -1)) {
                return true;
            }
        }
        return false;
    }

    public void flip(int i, int j, int dirI, int dirJ, int op) {
        int tmpI = i;
        int tmpJ = j;

        ArrayList<int[]> flipLst = new ArrayList<int[]>();
        boolean flip = false;
        while (tmpI >= 0 && tmpI < size && tmpJ >= 0 && tmpJ < size) {
            if (grid[tmpI][tmpJ] == op) {
                flipLst.add(new int[] { tmpI, tmpJ });
            } else if (grid[tmpI][tmpJ] == this.mover) {
                flip = true;
                break;
            }

            tmpI += dirI;
            tmpJ += dirJ;
        }
        if (flip) {
            for (int[] t : flipLst) {
                grid[t[0]][t[1]] = mover;
            }
        }
    }

    private Boolean direction(int i, int j, int dirI, int dirJ) {
        int tmpI = i;
        int tmpJ = j;
        while (tmpI >= 0 && tmpI < size && tmpJ >= 0 && tmpJ < size) {
            if (grid[tmpI][tmpJ] == mover)
                return true;
            else if (grid[tmpI][tmpJ] == EMPTY)
                break;
            tmpI += dirI;
            tmpJ += dirJ;
        }
        return false;
    }

    public void printfBoard() {
        Colorize c = new Colorize();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == BLACK_UP) {
                    c.printBlackPiece();
                } else if (grid[i][j] == WHITE_UP) {
                    c.printWhitekPiece();
                } else {
                    c.printEmpty();
                }
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    @Override
    public String toString() {
        String output = "";
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == BLACK_UP) {
                    output += "B ";
                } else if (grid[i][j] == WHITE_UP) {
                    output += "W ";
                } else {
                    output += "0 ";
                }
            }
            output += "\n";
        }
        return output;

    }

}