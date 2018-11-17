import java.util.Scanner;

//method will input deductive/logic techniques but also brute force if necessary.
public class SudokuSolver {
    private SudokuBoard board;

    public SudokuSolver (SudokuBoard board) {
        this.board = board;
    }

    //this method is the cycle from least complex technique to most complex
    public void solveCycle () {
        if (soleCandidate()) {
            solveCycle();
            return;
        }
        if (uniqueCandidate()) {
            solveCycle();
            return;
        }
        if (blockInteraction()) {
            solveCycle();
			return;
        }
        if (subset()) {
            solveCycle();
        }
    }

    //the first technique SoleCandidate if there's only one possibility for
    //a case it must be that one
    private boolean soleCandidate () {
        //go through every element on the board
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                Tuple coordinate = new Tuple(x,y);
                Integer[] poss = board.getPoss(coordinate);
                //if we have only one candidate, set the case to that candidate and then call this recursively
                if (poss.length == 1) {
                    board.setNumber(coordinate, poss[0]);
                    board.printBoard();
                    return true;
                }
            }
        }
        return false;
    }

    //the second technique is that if one number can only be at one spot, it has to be in
    //said spot
    private boolean uniqueCandidate () {
        return board.uniqueCandidate();
    }

    //third technique clear rows and columns using dependencies
    private boolean blockInteraction () {
        if (board.recursive == 400) {
            System.out.println();
        }
        return board.blockInteractions();
    }

    //fourth technique using naked subsets
    private boolean subset () {
        return board.subset();
    }


    public static void main (String[] args) {
        Tuple[][] empty = new Tuple[9][];
//        SudokuBoard mySudokuBoard = new SudokuBoard(empty);
        Scanner reader = new Scanner(System.in);
//        int num = 1;
//        while (true) {
//            mySudokuBoard.printBoard();
//            System.out.println("input one number to change target, 2numbers to set the number, none to break");
//            String input = reader.nextLine();
//            int length = input.length();
//            switch (length) {
//                case 1: num = Integer.parseInt(input);
//                        break;
//                case 3:Tuple coordinate = new Tuple(input);
//                        mySudokuBoard.setNumber(coordinate, num);
//            }
//            if (input.length() == 0) {
//                break;
//            }
//        }
        SudokuBoard mySudokuBoard = new SudokuBoard(reader.nextLine());
        mySudokuBoard.printBoard();
        SudokuSolver monkey = new SudokuSolver(mySudokuBoard);
        monkey.solveCycle();
    }
}
