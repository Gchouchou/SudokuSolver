# SudokuSolver
A Sudoku solver using Java.


The Rules of sudoku.
A 9x9 box is partitionned into nine non overlapping 3x3 boxes. The goal of the game is to fill the whole box, with numbers from 1 to 9 without ever having a number appear twice in the same row, column or 3x3 box.


I am trying to make a sudoku solver that only uses deductive techniques. So far, I have implemented 4 first techniques that are take from https://www.kristanix.com/sudokuepic/sudoku-solving-techniques.php. 


The first technique that I implemented is the sole candidate. The machine will scan every case in the puzzle, and if any empty case has ONLY one possible answer, it fills in that answer.

The second technique that I implimented is similar to the first one. If within any row, column or box, a specific number can only appear at one place, then said number has to be in that case. The machine then puts the number on that spot.

For this project, I practiced using inner classes. 

The important utility class is the SudokuBoard class. The class has an 9 by 9 int array that represents the numbers that the machine is certain of. If the case is empty, it has 0 as the default value.














