public class TicTacToeGame {

    private static final char PLAYERX = 'X';     // Helper constant for X player
    private static final char PLAYERO = 'O';     // Helper constant for O player
    private static final char SPACE = ' ';       // Helper constant for spaces

    /*
    Sample TicTacToe Board
      0 | 1 | 2
     -----------
      3 | 4 | 5
     -----------
      6 | 7 | 8
     */


    // TODO 4: Implement necessary methods to manage the games of Tic Tac Toe

    private String otherPlayer;
    private String hostPlayer;
    private char[][] board;

    public TicTacToeGame(String otherPlayer, String hostPlayer){
        this.otherPlayer = otherPlayer;
        this.hostPlayer = hostPlayer;

        this.board = new char[3][3];
        for(int i =0; i<3; i++){
            for (int a =0; a<3; a++){
                this.board[i][a] = SPACE;
            }
        }
    }
    public int takeTurn(int index, String player){
        char tempChar = ' ';
        if(player.equals(hostPlayer)){
            tempChar = PLAYERX;
        }else if(player.equals(otherPlayer)){
            tempChar = PLAYERO;
        }
        if(getSpace(index) == SPACE){
            if(index<=2 && index>=0){
                this.board[0][index] = tempChar;
                return index;
            }if(index<=5 && index>2){
                this.board[1][index-3] = tempChar;
                return index;
            }if(index>5){
                this.board[2][index-6] = tempChar;
                return index;

            }else {
                return -1;
            }
        }else{
            return -1;
        }

    }
    private char getSpace(int index){
        if(index>8 || index<0){
            return '-';
        }else{
            if(index<=2){
                return this.board[0][index];
            }else if(index<=5){
                return this.board[1][index-3];
            }else{
                return this.board[2][index-6];

            }
        }
    }

    @Override
    public String toString() {
        String str = "\n";
        for(int a=0; a<3; a++) {

            str = str + " " + getSpace(a) + " |";
        }
            str = str.substring(0,str.length()-1);
            str = str + "\n";
            str = str + "-----------" + "\n";

        for(int a=0; a<3; a++) {

            str = str + " " + getSpace(3 + a) + " |";
        }
            str = str.substring(0,str.length()-1);
            str = str + "\n";
            str = str + "-----------" + "\n";

        for(int a=0; a<3; a++) {

            str = str + " " + getSpace(6 + a) + " |";
        }
            str = str.substring(0,str.length()-1);
            str = str + "\n";
            str = str + "-----------" + "\n";

        str = str.substring(0, str.length()-12);
        return str;
    }

    public char getGameState(){
        for(int a=0; a<3;a++){
            if(board[a][0] == board[a][1] && board[a][1] == board[a][2] && board[a][1] != SPACE){
                if (getSpace(3*a) == PLAYERX){
                    return PLAYERX;
                }else{
                    return PLAYERO;
                }

            }else if(board[0][a] == board[1][a] && board[1][a] == board[2][a] && board[1][a] != SPACE){
                if(getSpace(3*a) == PLAYERX){
                    return PLAYERX;
                }else{
                    return PLAYERO;
                }
            }
        }
        if(board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[1][1] != SPACE){
            if(board[0][0] == PLAYERX){
                return PLAYERX;
            }else{
                return PLAYERO;
            }

        }else if(board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[1][1] != SPACE){
            if(board[1][1] == PLAYERX){
                return PLAYERX;
            }else{
                return PLAYERO;
            }
        }else{
            for(int a=0; a<3; a++){
                for(int i=0; i<3; i++){
                    if(board[a][i] == SPACE){
                        return '-'; //continue
                    }
                }
            }
            return '/'; //its a tie
        }
    }

    public String getHostPlayer() {
        return hostPlayer;
    }

    public String getOtherPlayer() {
        return otherPlayer;
    }
}