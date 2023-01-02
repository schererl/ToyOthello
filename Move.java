public class Move {
    int[] pos;
    public Move(int i, int j){
        pos = new int[2];
        pos[0] = i;
        pos[1] = j;
    }

    @Override
    public String toString(){
        return String.format("INSERT (%d %d)", pos[0], pos[1]);
    }
}
