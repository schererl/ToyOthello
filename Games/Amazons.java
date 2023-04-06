package Games;

import java.util.ArrayList;
import java.util.List;

public class Amazons extends Game {
    private static final int BOARD_SIZE = 10;
    public final static int PLAYERS_COUNT = 2;

    public int[][] grid;
    private double[] ut;
    public Amazons() {
        super(BLACK_UP, PLAYERS_COUNT);
        ut = new double[PLAYERS_COUNT];
        // Initialize the board with empty cells.
        grid = new int[BOARD_SIZE][BOARD_SIZE];
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                grid[x][y] = EMPTY;
            }
        }

        // Set up the starting positions of the Amazons for both players.
        grid[0][3] = BLACK_UP;
        grid[0][6] = BLACK_UP;
        grid[3][0] = BLACK_UP;
        grid[3][9] = BLACK_UP;

        grid[6][0] = WHITE_UP;
        grid[6][9] = WHITE_UP;
        grid[9][3] = WHITE_UP;
        grid[9][6] = WHITE_UP;

        // Set the first player's turn.
        mover = BLACK_UP;
    }

    
    @Override
    public ArrayList<Move> moves() {
        ArrayList<Move> movs = new ArrayList<>();
        int currentPlayer = mover;

        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                // Check if the current cell contains the current player's amazon.
                if (grid[x][y] == currentPlayer) {
                    List<int[]> moves = getPossibleMoves(x, y);

                    for (int[] move : moves) {
                        int newX = move[0];
                        int newY = move[1];

                        // Find possible arrow shots after the amazon move.
                        List<int[]> arrowShots = getPossibleMoves(newX, newY);

                        for (int[] shot : arrowShots) {
                            Move m = new Move();
                            m.setPosInit(x, y);
                            m.setPosFinal(newX, newY);
                            m.setPosArrow(shot[0], shot[1]);
                            movs.add(m);
                        }
                    }
                }
            }
        }

        return movs;
    }

    private List<int[]> getPossibleMoves(int x, int y) {
        List<int[]> moves = new ArrayList<>();

        // Check all 8 directions (horizontal, vertical, and diagonal) for possible
        // moves.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }

                int currentX = x + dx;
                int currentY = y + dy;

                // Keep moving in the chosen direction until an obstacle is found or the edge of
                // the board is reached.
                while (isValidCoordinate(currentX, currentY) && grid[currentX][currentY] == EMPTY) {
                    moves.add(new int[] { currentX, currentY });
                    currentX += dx;
                    currentY += dy;
                }
            }
        }

        return moves;
    }

    @Override
    public Game copy() {
        Amazons instance = new Amazons();
        instance.mover = this.mover;
        instance.numTurn = this.numTurn;
        for (int i = 0; i < this.BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                instance.grid[i][j] = this.grid[i][j];
            }
        }
        return instance;
    }

    @Override
    public int playerIndex() {
        return mover - 1;
    }

    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    @Override
    public void apply(Move m) {
        int currentPlayer = mover;
        grid[m.posInit[0]][m.posInit[1]] = EMPTY;
        grid[m.posFinal[0]][m.posFinal[1]] = currentPlayer;
        grid[m.posArrow[0]][m.posArrow[1]] = ARROW;
        mover = mover == 1 ? 2 : 1;
    }

    @Override
    public Boolean isTerminal() {
        List<Move> actions = moves();
        if (actions.isEmpty()) {
            if(mover == BLACK_UP){
                ut[Othello.BLACK_UP - 1] = -1;
                ut[Othello.WHITE_UP - 1] = 1;
            }else{
                ut[Othello.BLACK_UP - 1] = 1;
                ut[Othello.WHITE_UP - 1] = -1;
            }
            return true;
        }

        ut = new double[]{0,0,0};
        return false;
    }

    @Override
    public String toString() {
        String output = "";
        for (int i = 0; i < this.BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (grid[i][j] == BLACK_UP) {
                    output += "B ";
                } else if (grid[i][j] == WHITE_UP) {
                    output += "W ";
                } else if(grid[i][j] == ARROW){
                    output += "A ";
                } 
                else {
                    output += "  ";
                }
            }
            output += "\n";
        }
        return output;

    }

    @Override
    public double[] utilities() {
        return this.ut;
    }
}
