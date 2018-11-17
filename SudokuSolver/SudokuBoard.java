import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//mimics the Sudoku board
public class SudokuBoard {
    //x is the first coordinate
    //y is the second coordinate
    private int[][] board = new int[9][9];
    //arrays of rows, columns and boxes
    private Row[] rows = new Row[9];
    private Column[] cols = new Column[9];
    private Box[][] boxes = new Box[3][3];
    public int recursive = 0;

    //board of possibilities, if an already known number, we give it an empty array
    private Integer[][][] boardPoss = new Integer[9][9][];

    //region Constructors, initializers, initial purge
    //takes input a 2d array of tuples, the first coordinate represents what number to fill in
    public SudokuBoard(Tuple[][] inputValues) {
        if (inputValues.length != 9) {
            throw new IllegalArgumentException("Must have 9 rows for input");
        }
        //loop throw each row
        for (int number = 0; number < 9; number++) {
            //we want to make sure we don't have an null array
            if (inputValues[number] != null) {
                for (Tuple coordinate : inputValues[number]) {
                    board[coordinate.x][coordinate.y] = number + 1;
                }
            }
        }
        //create box and row objects
        createRowsColumnsBox();
        //we create the board of possibilities
        createPossBoard();
        firstPurge();
    }

    //take input a long 81 character string that represent all cases
    public SudokuBoard(String inputString) {
        //we want to make sure we input an 81 character string
        if (inputString.length() != 81) {
            throw new IllegalArgumentException("Must input a string of 81 characters");
        }
        for (int i = 0; i < 81; i++) {
            char input =  inputString.charAt(i);
            if (input == '.') {
                input = '0';
            }
            //we scale it up to what we want
            int x = i % 9;
            int y = i / 9;
            //we then convert the ascii value to the actual integers
            board[x][y] = (input) - '0';
        }
        //create box and row objects
        createRowsColumnsBox();
        //we create the board of possibilities
        createPossBoard();
        firstPurge();
    }

    private void createRowsColumnsBox () {
        for (int i = 0; i < 9; i++) {
            rows[i] = new Row(this, i);
            cols[i] = new Column(this, i);
            int x = i%3;
            int y = i/3;
            Tuple coordinate = new Tuple(x,y);
            boxes[x][y] = new Box(this, coordinate);
        }
    }

    //this method initializes all possibilities and initializes an array for each entry
    private void createPossBoard() {
        //iterate through all the arrays
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                //if its empty
                //else we already have a fixed value
                if (board[x][y] == 0) {
                    //we have to check all possibilities, array with 1,2,3,...9
                    boardPoss[x][y] = new Integer[9];
                    for (int i = 1; i <= 9; i++) {
                        boardPoss[x][y][i - 1] = i;
                    }
                } else {
                    //we create an empty array
                    boardPoss[x][y] = new Integer[0];
                }

            }
        }

    }

    //purge all rows and columns
    private void firstPurge () {
        for (Row row :rows) {
            row.purgeRow();
        }
        for (Column col :cols) {
            col.purgeCol();
        }
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                Box box = boxes[x][y];
                box.purgeBox();
            }
        }
    }
    //endregion

    //method returns the number at the input coordinate
    public int getNumber (Tuple coordinates) {
        return board[coordinates.x][coordinates.y];
    }

    //method that sets the number on the board and purges the number from the possibilities
    public void setNumber (Tuple coordinate, int number) {
        board[coordinate.x][coordinate.y] = number;
        //set an empty array to the spot
        boardPoss[coordinate.x][coordinate.y] = new Integer[0];
        //update the rows and columns and boxes objects + purge
        rows[coordinate.y].row[coordinate.x] = number;
        rows[coordinate.y].purgeRow(number);
        cols[coordinate.x].col[coordinate.y] = number;
        cols[coordinate.x].purgeCol(number);
        boxes[coordinate.x/3][coordinate.y/3].box[coordinate.x%3][coordinate.y%3] = number;
        boxes[coordinate.x/3][coordinate.y/3].purgeBox(number);
    }

    public Integer[] getPoss (Tuple coordinate) {
        return boardPoss[coordinate.x][coordinate.y];
    }

    //region inner class row, column and box to handle data better
    private class Row {
        final int rowNumber;
        int[] row = new int[9];
        SudokuBoard theBoard;
        //this attribute keeps track of how many places a specific number can appear
        int[] entryCounter = new int[9];

        //contains a list of purgedNumbers to avoid repeats
        List<Integer> purgedNumbers = new ArrayList<>();

        //takes a copy of the row
        Row(SudokuBoard theBoard, int rowNumber) {
            this.rowNumber = rowNumber;
            this.theBoard = theBoard;
            //create a copy of the row
            for (int x = 0; x < 9; x++) {
                row[x] = theBoard.board[x][rowNumber];
            }
        }

        //update and return the counter of entries
        int[] getEntryCounter () {
            //reset the array all back to zero
            entryCounter = new int[9];
            for (int n = 1; n <= 9; n++) {
                for (int x = 0; x < 9; x++) {
                    if (containsInt(boardPoss[x][rowNumber], n)) {
                        entryCounter[n - 1]++;
                    }
                }
            }
            return entryCounter;
        }

        //update and return the specifc entry at entry counter
        int getEntryCounter (int n) {
            entryCounter[n - 1] = 0;
            for (int x = 0; x < 9; x++) {
                if (containsInt(boardPoss[x][rowNumber], n)) {
                    entryCounter[n - 1]++;
                }
            }

            return entryCounter[n - 1];
        }

        //region Purge Methods
        //check if a number is inside the row
        boolean isNumberInside(int number) {
            return containsInt(row, number);
        }

        //return an array of all present numbers
        Integer[] presentNumbers() {
            List<Integer> numbersInside = new ArrayList<>();
            //go through all numbers from 1 to 9
            for (int n = 1; n <= 9; n++) {
                if (isNumberInside(n)) {
                    numbersInside.add(n);
                }
            }
            //convert to array
            Integer[] arrNumbers = new Integer[numbersInside.size()];
            arrNumbers = numbersInside.toArray(arrNumbers);
            return arrNumbers;
        }

        //purging every number insider when taking no input
        void purgeRow() {
            Integer[] removeNumbers = presentNumbers();
            for (int index = 0; index < 9; index++) {
                Tuple point = new Tuple(index, rowNumber);
                purgeCase(point, removeNumbers);
            }
        }

        //precise purging of a specific number
        void purgeRow(int removeNumber) {
            Integer[] removeNumbers = new Integer[1];
            removeNumbers[0] = removeNumber;
            for (int index = 0; index < 9; index++) {
                Tuple point = new Tuple(index, rowNumber);
                purgeCase(point, removeNumbers);
            }
        }

        //even softer precise purging of a specific number
        void purgeRow(int removeNumber, Tuple[] safe) {
            Integer[] removeNumbers = new Integer[1];
            removeNumbers[0] = removeNumber;
            //we create an int array that contain the safe indexes
            int[] safeIndex = new int[safe.length];
            for (int i = 0; i < safe.length; i++) {
                safeIndex[i] = safe[i].x;
            }
            for (int index = 0; index < 9; index++) {
                //if the index is NOT safe or not in the array
                if (!containsInt(safeIndex, index)) {
                    Tuple point = new Tuple(index, rowNumber);
                    purgeCase(point, removeNumbers);
                }
            }
        }
        //endregion

        //method returns the coordinates at which a specific number can be in a row
        Tuple[] numPoss (int num) {
            Tuple[] targets = new Tuple[9];
            for (int x = 0; x < 9; x++) {
                Tuple point = new Tuple(x, rowNumber);
                targets[x] = point;
            }
            return numPossTargeted(num, targets);
        }

    }

    private class Column {
        final int colNumber;
        int[] col = new int[9];
        SudokuBoard theBoard;
        //this attribute keeps track of how many places a specific number can appear
        int[] entryCounter = new int[9];

        //contains a list of purgedNumbers to avoid repeats
        List<Integer> purgedNumbers = new ArrayList<>();

        //takes a copy of the col
        Column(SudokuBoard theBoard, int colNumber) {
            this.colNumber = colNumber;
            this.theBoard = theBoard;
            //create a copy of the col, shallow copy not a reference
            System.arraycopy(theBoard.board[colNumber], 0, col, 0, 9);
        }

        //update and get the entry counter array
        int[] getEntryCounter () {
            //reset the array all back to zero
            entryCounter = new int[9];
            for (int n = 1; n <= 9; n++) {
                for (int y = 0; y < 9; y++) {
                    if (containsInt(boardPoss[colNumber][y], n)) {
                        entryCounter[n - 1]++;
                    }
                }
            }
            return entryCounter;
        }

        //return the specific value of the array at an index
        int getEntryCounter (int n) {
            entryCounter[n - 1] = 0;
            for (int y = 0; y < 9; y++) {
                if (containsInt(boardPoss[colNumber][y], n)) {
                    entryCounter[n - 1]++;
                }
            }

            return entryCounter[n - 1];
        }


        //region Purge Methods
        //check if a specific number is inside the column
        boolean isNumberCol(int number) {
            return containsInt(col, number);
        }

        //return an array of all present numbers
        Integer[] presentNumbers() {
            List<Integer> numbersInside = new ArrayList<>();
            //go through all numbers from 1 to 9
            for (int n = 1; n <= 9; n++) {
                if (isNumberCol(n)) {
                    numbersInside.add(n);
                }
            }
            //convert to array
            Integer[] arrNumbers = new Integer[numbersInside.size()];
            arrNumbers = numbersInside.toArray(arrNumbers);
            return arrNumbers;
        }

        //purging every number inside when taking no input
        void purgeCol() {
            Integer[] removeNumbers = presentNumbers();
            for (int index = 0; index < 9; index++) {
                Tuple point = new Tuple(colNumber, index);
                purgeCase(point, removeNumbers);
            }
        }

        //precise purging of a specific number
        void purgeCol(int removeNumber) {
            Integer[] removeNumbers = new Integer[1];
            removeNumbers[0] = removeNumber;
            for (int index = 0; index < 9; index++) {
                Tuple point = new Tuple(colNumber, index);
                purgeCase(point, removeNumbers);
            }
        }

        //soft purge saving a few indexes
        void purgeCol(int removeNumber, Tuple[] safe) {
            Integer[] removeNumbers = new Integer[1];
            removeNumbers[0] = removeNumber;
            //we create an int array that contain the safe indexes
            int[] safeIndex = new int[safe.length];
            for (int i = 0; i < safe.length; i++) {
                safeIndex[i] = safe[i].y;
            }
            for (int index = 0; index < 9; index++) {
                if (!containsInt(safeIndex,index)) {
                    Tuple point = new Tuple(colNumber, index);
                    purgeCase(point, removeNumbers);
                }
            }
        }
        //endregion

        //method returns the coordinates at which a specific number can be in a column
        Tuple[] numPoss (int num) {
            Tuple[] targets = new Tuple[9];
            for (int y = 0; y < 9; y++) {
                Tuple point = new Tuple(colNumber, y);
                targets[y] = point;
            }
            return numPossTargeted(num, targets);
        }

    }

    private class Box {
        final Tuple coordinate;
        int[][] box = new int[3][3];
        SudokuBoard theBoard;
        //this attribute keeps track of how many places a specific number can appear
        int[] entryCounter = new int[9];

        //contains a list of purgedNumbers to avoid repeats
        List<Integer> purgedNumbers = new ArrayList<>();

        //get copy of particular box
        Box(SudokuBoard theBoard, Tuple coordinate) {
            this.coordinate = coordinate;
            this.theBoard = theBoard;
            //create a copy of the box
            for (int x = 0; x < 3; x++) {
                System.arraycopy(theBoard.board[x + 3 * coordinate.x], 3 * coordinate.y, box[x], 0, 3);
            }
        }

        //get the number of appearances of each number
        int[] getEntryCounter () {
            //reset the array all back to zero
            entryCounter = new int[9];
            for (int n = 1; n <= 9; n++) {
                for (int x = 0; x < 3; x++) {
                    for (int y = 0; y < 3; y++) {
                        if (containsInt(boardPoss[x + 3*coordinate.x][y + 3*coordinate.y], n)) {
                            entryCounter[n - 1]++;
                        }
                    }
                }
            }
            return entryCounter;
        }

        //get and update a specific entry in the array
        int getEntryCounter (int n) {
            //set to zero
            entryCounter[n - 1] = 0;
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    if (containsInt(boardPoss[x + 3 * coordinate.x][y + 3 * coordinate.y], n)) {
                        entryCounter[n - 1]++;
                    }
                }
            }
            return entryCounter[n - 1];
        }

        //region Purge Methods
        //return true if the number is in the box
        boolean isNumberBox(int number) {
            //go through each array
            for (int x = 0; x < 3; x++) {
                if (containsInt(box[x], number)) {
                    return true;
                }
            }
            return false;
        }

        //returns array of numbers present
        Integer[] presentNumbers() {
            List<Integer> numbersInside = new ArrayList<>();
            //go through all numbers from 1 to 9
            for (int n = 1; n <= 9; n++) {
                if (isNumberBox(n)) {
                    numbersInside.add(n);
                }
            }
            //convert to array
            Integer[] arrNumbers = new Integer[numbersInside.size()];
            arrNumbers = numbersInside.toArray(arrNumbers);
            return arrNumbers;
        }

        //purging every number inside when taking no input
        void purgeBox() {
            Integer[] removeNumbers = presentNumbers();
            //iterate through every coordinate
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    //we need to scale it up using the coordinate of the box
                    Tuple target = new Tuple(x + 3*coordinate.x, y + 3*coordinate.y);
                    purgeCase(target, removeNumbers);
                }
            }
        }

        //precise purging of a specific number
        void purgeBox(int removeNumber) {
            Integer[] removeNumbers = new Integer[1];
            removeNumbers[0] = removeNumber;
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    Tuple target = new Tuple(x + 3*coordinate.x, y + 3*coordinate.y);
                    purgeCase(target, removeNumbers);
                }
            }
        }
        //precise purging of a specific number
        void purgeBox(int removeNumber, Tuple[] safe) {
            Integer[] removeNumbers = new Integer[1];
            removeNumbers[0] = removeNumber;
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    Tuple target = new Tuple(x + 3*coordinate.x, y + 3*coordinate.y);
                    //we need to make sure it is not in the safe zone
                    if (!containsTuple(safe, target)) {
                        purgeCase(target, removeNumbers);
                    }
                }
            }
        }
        //endregion

        //returns the tuples that contain the number in the box
        Tuple[] numPoss (int num) {
            //we first create of all tuples in our box
            Tuple[] targets = new Tuple[9];
            //create a counter so that we can easily populate the array
            int counter = 0;
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    Tuple point = new Tuple(x + 3*coordinate.x, y + 3*coordinate.y);
                    targets[counter] = point;
                    counter++;
                }
            }
            return numPossTargeted(num, targets);
        }
    }
    //endregion

    //region Helper Methods
    //methods that checks if an integer is inside in an integer array
    private static boolean containsInt(Integer[] array, Integer test) {
        for (int entry : array) {
            if (entry == test) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsInt(int[] possibility, Integer test) {
        for (Integer entry : possibility) {
            if (entry.equals(test)) {
                return true;
            }
        }
        return false;
    }

    //check if the tuple array contains the specific tuple
    private static boolean containsTuple(Tuple[] array, Tuple test) {
        //iterate through the array
        for (Tuple point:array) {
            //if it is equal
            if (point.equivTo(test)) {
                return true;
            }
        }
        return false;
    }

    //return the intersection of possibilities along all the tuples
    private Integer[] intersectionPoss (Tuple[] set) {
        List<Integer> initial = new ArrayList<>();
        for (Integer i: boardPoss[set[0].x][set[0].y]) {
            for (int x = 0; x < set.length; x++) {
                Tuple point = set[x];
                if (!containsInt(boardPoss[point.x][point.y], i)) {
                    break;
                }
                if (x == set.length - 1) {
                    initial.add(i);
                }
            }
        }
        Integer[] arr = new Integer[initial.size()];
        arr = initial.toArray(arr);
        return arr;
    }

    //this method gets the intersection then sets all the tuples to the intersection
    private void setIntersection (Tuple[] set) {
        Integer[] arrIntersection = intersectionPoss(set);
        for (Tuple point: set) {
            cleanCase(point, arrIntersection);
        }
    }

    //using reference types to remove elements from an Integer array
    private static Integer[] purgeArray(Integer[] target, Integer[] removeNumbers) {
        List<Integer> updatedTarget = new ArrayList<>();
        //we go through every possibility, and compare it with the numbers to be removed
        for (Integer present : target) {
            if (!containsInt(removeNumbers, present)) {
                //we add the present number if it is not to be removed
                updatedTarget.add(present);
            }
        }
        Integer[] newPoss = new Integer[updatedTarget.size()];
        newPoss = updatedTarget.toArray(newPoss);
        return newPoss;
    }

    //remove a set of numbers in a case at a coordinate
    private void purgeCase(Tuple coordinate, Integer[] removeNumbers) {
        boardPoss[coordinate.x][coordinate.y]
                = purgeArray(boardPoss[coordinate.x][coordinate.y], removeNumbers);
    }

    //we do the reverse of purge case we purge everything not in the array
    private void cleanCase (Tuple coordinate, Integer[] saveValues) {
        boardPoss[coordinate.x][coordinate.y] = saveValues;
    }

    //method that checks if the valid does not contain a duplicate number
    private boolean checkValidArray (int[] array) {
        //try each number from 1 to 9
        for (int n = 1; n <= 9; n++) {
            //setup a counter to keep track how many times it appears
            int counter = 0;
            //iterate through the array
            for (int entry : array) {
                if (entry == n) {
                    counter++;
                }
                if (counter > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkValidArray (int[][] array) {
        //iterate through the array
        for (int[] subArray : array) {
            //use previous method to check if the subArray is valid
            if (!checkValidArray(subArray)) {
                return false;
            }
        }
        //returns true when all are valid.
        return true;
    }

    //takes input a number and a set of Tuples, return the subset of tuples that contain the number in the array
    private Tuple[] numPossTargeted (int num, Tuple[] targets) {
        List<Tuple> coordinates = new ArrayList<>();
        for (Tuple target: targets) {
            if (containsInt(boardPoss[target.x][target.y], num)) {
                coordinates.add(target);
            }
        }
        Tuple[] possNum = new Tuple[coordinates.size()];
        possNum = coordinates.toArray(possNum);
        return possNum;
    }

    //check if the the tuples are in the same row
    private boolean sameRowTuples (Tuple[] points) {
        int sample = points[0].y;
        for (Tuple point: points) {
            //if any point is not in the same row
            if (point.y != sample) {
                return false;
            }
        }
        return true;
    }

    //check if the the tuples are in the same column
    private boolean sameColTuples (Tuple[] points) {
        int sample = points[0].x;
        for (Tuple point: points) {
            //if any point is not in the same column
            if (point.x != sample) {
                return false;
            }
        }
        return true;
    }

    //check if the tuples are in which interval, 0 or 1 or 2, else -1
    private int intervalTuples (Tuple[] points) {
        //check if it's in the same row or in the same column
        if (sameColTuples(points)) {
            //same column, we get a sample and then check if its all the same
            int sample = points[0].y/3;
            for (Tuple point: points) {
                //if any point is not in the same box
                if (point.y/3 != sample) {
                    return -1;
                }
            }
            //we return the sample if it satisfies every element in the loop
            return sample;
        } else {
            //same row, we get a sample and then check if its all the same
            int sample = points[0].x/3;
            for (Tuple point: points) {
                //if any point is not in the same box
                if (point.x/3 != sample) {
                    return -1;
                }
            }
            //same as above
            return sample;
        }
    }

    //endregion

    //region applying technique uniqueCandidate
    public boolean uniqueCandidate() {
        //begin by cycling through all the boxes
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                Box target = boxes[x][y];
                int num = uniqueCandidateCounter(target);
                //if we find one that is not zero
                if (num != 0) {
                    //since we know that there is one entry in the array
                    Tuple coordinate = target.numPoss(num)[0];
                    setNumber(coordinate,num);
                    printBoard();
                    return true;
                }
            }
        }
        //begin cycling through rows
        for (int rowNum = 0; rowNum < 9; rowNum++) {
            Row target = rows[rowNum];
            int num = uniqueCandidateCounter(target);
            //if we find one that is not zero
            if (num != 0) {
                //since we know that there is one entry in the array
                Tuple coordinate = target.numPoss(num)[0];
                setNumber(coordinate,num);
                printBoard();
                return true;
            }
        }
        //begin cycling through columns
        for (int colNum = 0; colNum < 9; colNum++) {
            Column target = cols[colNum];
            int num = uniqueCandidateCounter(target);
            //if we find one that is not zero
            if (num != 0) {
                //since we know that there is one entry in the array
                Tuple coordinate = target.numPoss(num)[0];
                setNumber(coordinate,num);
                printBoard();
                return true;
            }
        }

        return false;
    }

    //this method gives us the number that can only appear in one position in a box
    private int uniqueCandidateCounter(Box box) {
        //get the entries in box
        int[] entries = box.getEntryCounter();
        //check if one appears exactly once
        for (int n = 0; n < 9; n++) {
            if (entries[n] == 1) {
                return n + 1;
            }
        }
        //returns 0 otherwise
        return 0;
    }

    private int uniqueCandidateCounter (Row row) {
        //get the entries in box
        int[] entries = row.getEntryCounter();
        //check if one appears exactly once
        for (int n = 0; n < 9; n++) {
            if (entries[n] == 1) {
                return n + 1;
            }
        }
        //returns 0 otherwise
        return 0;
    }

    private int uniqueCandidateCounter (Column col) {
        //get the entries in box
        int[] entries = col.getEntryCounter();
        //check if one appears exactly once
        for (int n = 0; n < 9; n++) {
            if (entries[n] == 1) {
                //n + 1 since n is the index
                return n + 1;
            }
        }
        //returns 0 otherwise
        return 0;
    }
    //endregion


    //region Block interactions
    //regroup all of the block interaction
    public boolean blockInteractions() {
        //create a boolean that returns false if nothing ever gets updated
        boolean didSomething = false;
        //begin by cycling through all the boxes
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                Box target = boxes[x][y];
                if (blockInteractionBox(target)) {
                    didSomething = true;
                }
            }
        }
        //begin cycling through rows
        for (int rowNum = 0; rowNum < 9; rowNum++) {
            Row target = rows[rowNum];
            if (blockInteractionRow(target)) {
                didSomething = true;
            }
        }
        //begin cycling through columns
        for (int colNum = 0; colNum < 9; colNum++) {
            Column target = cols[colNum];
            blockInteractionCol(target);
            if (blockInteractionCol(target)) {
                didSomething = true;
            }
        }
        return didSomething;
    }

    private boolean blockInteractionBox (Box box) {
        //boolean that returns true if it did something
        boolean didSomething = false;
        //check if it appears between 3 and 2 times
        for (int n = 1; n <= 9; n++) {
            //we cannot purge the same number in a box twice
            if (box.purgedNumbers.contains(n)) {
                continue;
            }
            //we get then entry of the specific number
            int entry = box.getEntryCounter(n);
            if (entry > 0 && entry <= 3) {
                Tuple[] locations = box.numPoss(n);
                //we need to check if they are all in the same row or column
                if (sameRowTuples(locations)) {
                    //remember the purged number
                    box.purgedNumbers.add(n);
                    //we soft purge the row, saving our own array of tuples
                    rows[locations[0].y].purgeRow(n, locations);
                    didSomething = true;
                }
                if (sameColTuples(locations)) {
                    //remember the purged number
                    box.purgedNumbers.add(n);
                    //we soft purge the row, saving our own array of tuples
                    cols[locations[0].x].purgeCol(n, locations);
                    didSomething = true;
                }
            }
        }
        return didSomething;
    }

    //method that describes block interactions with row or columns
    private boolean blockInteractionRow (Row row) {
        //returns true if it ever updates something
        boolean didSomething = false;
        //check if it appears between 3 and 2 times
        for (int n = 1; n <= 9; n++) {
            //if it has not already been purged
            if (!row.purgedNumbers.contains(n)) {
                //we check it can appear in how many cases
                int entry = row.getEntryCounter(n);
                if (entry > 0 && entry <= 3) {
                    Tuple[] positions = row.numPoss(n);
                    //we need to check if they are all in the same box
                    int boxX = intervalTuples(positions);
                    //if it's valid
                    if (boxX != -1) {
                        //remember the purged number
                        row.purgedNumbers.add(n);
                        //soft purge the box
                        boxes[boxX][row.rowNumber / 3].purgeBox(n, positions);
                        didSomething = true;
                    }
                }
            }
        }
        return didSomething;
    }

    //method that describes block interactions with row or columns
    private boolean blockInteractionCol (Column col) {
        //boolean that returns true if it does something
        boolean didSomething = false;
        //check if it appears between 3 and 2 times
        for (int n = 1; n <= 9; n++) {
            //we need to make sure it didn't already get purged
            if (!col.purgedNumbers.contains(n)) {
                int entry = col.getEntryCounter(n);
                if (entry > 1 && entry <= 3) {
                    Tuple[] positions = col.numPoss(n);
                    //we need to check if they are all in the same box
                    int boxY = intervalTuples(positions);
                    //if it's a valid interval
                    if (boxY != -1) {
                        //remember the purged number
                        col.purgedNumbers.add(n);
                        //soft purge the box
                        boxes[col.colNumber / 3][boxY].purgeBox(n, positions);
                        didSomething = true;
                    }
                }
            }
        }
        return didSomething;
    }
    //endregion

    //region subset, naked and hidden
    //this is the 4th technique, if we have a clean subset we can purge
    public boolean subset () {
        //create a boolean that returns false if nothing ever gets updated
        boolean didSomething = false;
        //begin by cycling through all the boxes
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                Box target = boxes[x][y];
                if (subsetBox(target)) {
                    didSomething = true;
                }
            }
        }
        //begin cycling through rows
        for (int rowNum = 0; rowNum < 9; rowNum++) {
            Row target = rows[rowNum];
            if (subsetRow(target)) {
                didSomething = true;
            }
        }
        //begin cycling through columns
        for (int colNum = 0; colNum < 9; colNum++) {
            Column target = cols[colNum];
            blockInteractionCol(target);
            if (subsetCol(target)) {
                didSomething = true;
            }
        }
        return didSomething;
    }

    //finding subsets and purging
    //we need to find the hidden subset or the naked subset,
    private boolean subsetBox (Box box) {
        //keep track if we ever did anything
        boolean didSomething = false;
        //we create the set of coordinates
        Tuple[] set = new Tuple[9];
        //we also create a counter to help us throughout the loop
        int counter = 0;
        //populate the array
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                set[counter] = new Tuple(x + 3*box.coordinate.x,y + 3*box.coordinate.y);
                counter++;
            }
        }
        //we then check if there is any of them that is a naked subset
        for (Tuple target : set) {
            Tuple[] arrTuples = getNakedSubset(target, set, box);
            Tuple[] hiddenTuples = getHiddenSubset(target,set, box);
            if (arrTuples != null) {
                //we found a naked subset
                didSomething = true;
                // we will purge all the cases not in the subset of the numbers of the naked subset
                for (Integer poss : boardPoss[target.x][target.y]) {
                    box.purgeBox(poss, arrTuples);
                }
            }
            if (hiddenTuples != null) {
                //we found a hidden subset
                didSomething = true;
                //we must first set it to all the numbers that appear exactly n times
                for (Integer i: boardPoss[hiddenTuples[0].x][hiddenTuples[0].y]) {
                    if (box.entryCounter[i - 1] != hiddenTuples.length) {
                        Integer[] removed = new Integer[1];
                        removed[0] = i;
                        purgeCase(hiddenTuples[0],removed);
                    }
                }
                //we set the tuples to their intersection
                setIntersection(hiddenTuples);
            }
        }
        return didSomething;
    }

    private boolean subsetCol (Column col) {
        //keep track if we ever did anything
        boolean didSomething = false;
        //we create the set of coordinates
        Tuple[] set = new Tuple[9];
        //populate the array
        for (int y = 0; y < 9; y++) {
            Tuple point = new Tuple(col.colNumber, y);
            set[y] = point;
        }
        //we then check if there is any of them that is a naked subset
        for (Tuple target : set) {
            Tuple[] arrTuples = getNakedSubset(target, set, col);
            Tuple[] hiddenTuples = getHiddenSubset(target,set, col);
            if (arrTuples != null) {
                //we found a naked subset
                didSomething = true;
                // we will purge all the cases not in the subset of the numbers of the naked subset
                for (Integer poss : boardPoss[target.x][target.y]) {
                    col.purgeCol(poss, arrTuples);
                }
            }
            if (hiddenTuples != null) {
                //we found a hidden subset
                didSomething = true;
                //we must first set it to all the numbers that appear exactly n times
                for (Integer i: boardPoss[hiddenTuples[0].x][hiddenTuples[0].y]) {
                    if (col.entryCounter[i - 1] != hiddenTuples.length) {
                        Integer[] removed = new Integer[1];
                        removed[0] = i;
                        purgeCase(hiddenTuples[0],removed);
                    }
                }
                //set the hidden subset to the intersection
                setIntersection(hiddenTuples);
            }
        }
        return didSomething;
    }

    private boolean subsetRow (Row row) {
        //keep track if we ever did anything
        boolean didSomething = false;
        //we create the set of coordinates
        Tuple[] set = new Tuple[9];
        //populate the array
        for (int x = 0; x < 9; x++) {
            Tuple point = new Tuple(x, row.rowNumber );
            set[x] = point;
        }
        //we then check if there is any of them that is a naked subset
        for (Tuple target : set) {
            Tuple[] arrTuples = getNakedSubset(target, set, row);
            Tuple[] hiddenTuples = getHiddenSubset(target,set, row);
            if (arrTuples != null) {
                //we found a naked subset
                didSomething = true;
                // we will purge all the cases not in the subset of the numbers of the naked subset
                for (Integer poss : boardPoss[target.x][target.y]) {
                    row.purgeRow(poss, arrTuples);
                }
            }
            if (hiddenTuples != null) {
                //we found a hidden subset
                didSomething = true;
                //we must first set it to all the numbers that appear exactly n times
                for (Integer i: boardPoss[hiddenTuples[0].x][hiddenTuples[0].y]) {
                    if (row.entryCounter[i - 1] != hiddenTuples.length) {
                        Integer[] removed = new Integer[1];
                        removed[0] = i;
                        purgeCase(hiddenTuples[0],removed);
                    }
                }
                //we set it to the intersection
                setIntersection(hiddenTuples);
            }
        }
        return didSomething;
    }

    //method will check if the particular tuple is a naked subset in the whole set/box
    private Tuple[] getNakedSubset(Tuple target, Tuple[] set, Object object) {
        //we first get the number of possibilities of the tuple
        int numbPoss = boardPoss[target.x][target.y].length;
        //we then check if it is even worth purging
        if (numbPoss == 0) {
            return null;
        }
        //we iterate through every possibility in the tuple
        for (int entry = 0; entry < numbPoss; entry++) {
            //n is the entry
            int n = boardPoss[target.x][target.y][entry];
            int entryCounter;
            //we get the number of times that the entry appears in the set
            if (object instanceof Row) {
                entryCounter = ((Row) object).getEntryCounter(n);
            } else if (object instanceof Column) {
                entryCounter = ((Column) object).getEntryCounter(n);
            } else {
                entryCounter = ((Box) object).getEntryCounter(n);
            }
            //there is a chance that the naked subset is useful
            if (entryCounter > numbPoss) {
                break;
            }
            //naked subset not useful
            if (entry == numbPoss - 1) {
                return null;
            }
        }
        //we create a Tuple list that takes in all the similar sets
        List<Tuple> similarTuples = new ArrayList<>();
        //we create a counter to check if there is the same number of subset
        int counter = 0;
        //we go through every tuple of the set
        for (Tuple test : set) {
            //searching for another box with the same quantity of numbers
            if (boardPoss[test.x][test.y].length == numbPoss) {
                //and we check if they contain the same numbers
                for (int i = 0; i < numbPoss; i++) {
                    //if they have one number that is different we break through the loop
                    //this if statement works since we know we always put them in increasing order
                    if (!boardPoss[target.x][target.y][i].equals(boardPoss[test.x][test.y][i])) {
                        break;
                    }
                    //on the last iteration we say it is the same thing
                    if (i == numbPoss - 1) {
                        //so we add it to the list
                        similarTuples.add(test);
                        counter++;
                    }
                }
            }
        }
        //if it is a naked subset we also want to make sure that it has not already been purged
        if (counter == numbPoss) {
            //we create the Tuple array that will be returned
            Tuple[] arrTuples = new Tuple[similarTuples.size()];
            arrTuples = similarTuples.toArray(arrTuples);
            return arrTuples;
        }
        //since the counter does not match
        return null;
    }

    //check if the particular tuple contains a hidden subset
    //we will have to check every number in the case and see if they always reappear together
    private Tuple[] getHiddenSubset(Tuple target, Tuple[] set, Object object) {
        //we begin by doing the fastest check
        Integer[] targetPoss = boardPoss[target.x][target.y];
        //if it has nothing in it we just return null
        if (targetPoss.length == 0) {
            return null;
        }
        //we get the array that tells us how many times each element appear
        int[] entriesMinus1;
        //if it's a row, column or box
        if (object instanceof Row) {
            entriesMinus1 = ((Row) object).getEntryCounter();
        } else if (object instanceof Column) {
            entriesMinus1 = ((Column) object).getEntryCounter();
        } else {
            entriesMinus1 = ((Box) object).getEntryCounter();
        }
        //we begin by doing a quick check if there are any number that appear the same number of times
        for (Integer firstPoss : targetPoss) {
            //we get the number of times it appears in the set
            int appearances = entriesMinus1[firstPoss - 1];
            if (appearances >= targetPoss.length) {
                //we have a naked subset or something worse
                //because if it is a subset it has to be lower if its equal it's a naked subset
                continue;
            }
            //this counter keeps track the number of numbers that have the same appearances as the target
            int counter = 0;
            for (Integer test : targetPoss) {
                //if they appear the same number of times
                if (appearances == entriesMinus1[test - 1]) {
                    //we check if they always appear together
                    //we count the number of times one appears with the other one
                    int subCounter = 0;
                    //we also create an Tuple array to collect the tuples that the target appears
                    List<Tuple> subset = new ArrayList<>();
                    //iterate through the set and check if they appear together in the same places
                    for (Tuple coordinate : set) {
                        Integer[] possibilities = boardPoss[coordinate.x][coordinate.y];
                        //if one appears but the other one appears
                        if (containsInt(possibilities, firstPoss) && containsInt(possibilities,test)) {
                            //we add it to the subset
                            subset.add(coordinate);
                            subCounter++;
                        }
                        //we found everything for those two numbers
                        if (subCounter == appearances) {
                            //we increment the macro counter
                            counter++;
                            break;
                        }
                    }
                    //now everything lines up we have x numbers that appear in the same x slots
                    if (counter == appearances) {
                        Tuple[] arrSubset = new Tuple[subset.size()];
                        return subset.toArray(arrSubset);
                    }
                }
            }
        }
        //if we get ABSOLUTELY NOTHING
        return null;
    }
    //endregion

    //method will visually print a board
    public void printBoard() {
        //iterate the columns
        for (int y = 0; y < 9; y++) {
            //iterate the rows
            //create a string for the row using string builder
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < 9; x++) {
                //append the number
                row.append(board[x][y]);
                //then at each 3 elements draw a line
                if (x == 2 || x == 5) {
                    row.append("|");
                } else {
                    row.append(" ");
                }
            }
            //print it out
            System.out.println(row);
            //print the horizontal bars
            if (y == 2 || y == 5) {
                //print 18 of them
                for (int i = 0; i < 18; i++) {
                    System.out.print("-");
                }
                //then end the line
                System.out.println();
            }
        }
        //print lines to help space
        System.out.println();
    }
}
