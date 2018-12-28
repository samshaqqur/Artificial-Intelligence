/////////////////////////////////////////////////////////////////////////////////
// CS 430 - Artificial Intelligence
// Project 4 - Sudoku Solver w/ Variable Ordering and Forward Checking
// File: Sudoku.java
//
// Group Member Names:
// Due Date:
// 
//
// Description: A Backtracking program in Java to solve the Sudoku problem.
// Code derived from a C++ implementation at:
// http://www.geeksforgeeks.org/backtracking-set-7-suduku/
/////////////////////////////////////////////////////////////////////////////////

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public class Sudoku
{
	// Constants
	final static int N = 9;//N is used for size of Sudoku grid. Size will be NxN
	static int numBacktracks = 0;
	static int ForwardChecks = 0;

	// Class-wide variables
	static int variableOrderChoice = -1;
	static int inferenceChoice = -1;

	// Formatters
	final static DecimalFormat dfSec = new DecimalFormat("0.000s");
	final static DecimalFormat dfBigNum = new DecimalFormat("0,000");

	/////////////////////////////////////////////////////////////////////
	// Main function used to test solver.
	public static void main(String[] args) throws FileNotFoundException
	{
		// Prompt user for test case
		Scanner scan = new Scanner(System.in);
		System.out.println("Please select a test case by entering 1-3: ");
		int caseNum = scan.nextInt();
		String fileName = "Case" + caseNum + ".txt";

		// Reads in from TestCase.txt (sample sudoku puzzle) - 0 means unassigned cells
		// You can search the internet for more test cases.
		Scanner fileScan = new Scanner(new File(fileName));

		// Reads case into grid 2D int array
		SudokuCoord grid[][] = new SudokuCoord[9][9];
		for (int r = 0; r < 9; r++)
		{
			String row = fileScan.nextLine();
			String [] cols = row.split(",");
			for (int c = 0; c < cols.length; c++) {
				int value = Integer.parseInt(cols[c].trim());
				grid[r][c] = new SudokuCoord(r, c, value);
			}
		}

		// Prints out the unsolved sudoku puzzle (as is)
		System.out.println("\nUnsolved sudoku puzzle:");
		printGrid(grid);

		// Prompt user for variable ordering option
		System.out.println("Please select a variable ordering option from the following list: ");
		System.out.println("\t1: Default Static Ordering (top-left to bottom-right)");
		System.out.println("\t2: Your Static Ordering Idea");
		System.out.println("\t3: Random Ordering");
		System.out.println("\t4: MINimum Remaining Values");
		System.out.println("\t5: MAXimum Remaining Values");
		System.out.println("\nYour choice (1-5): ");
		variableOrderChoice = scan.nextInt();

		// Prompt user for variable ordering option
		System.out.println("\nPlease select an inference method: ");
		System.out.println("\t1: Standard Backtracking");
		System.out.println("\t2: Forward Checking");
		System.out.println("\t3: Arc Consistency (BONUS/OPTIONAL)");
		System.out.println("\nYour choice (1-3): ");
		inferenceChoice = scan.nextInt();

		// Setup timer - Obtain the time before solving
		long stopTime = 0L;
		long startTime = System.currentTimeMillis();

		// Attempts to solve and prints results
		if (solveSudoku(grid) == true)
		{
			System.out.println("\nSudoku Puzzle SOLVED - Details Below:");
			// Get stop time once the algorithm has completed solving the puzzle
			stopTime = System.currentTimeMillis();
			double runtimeSec = (stopTime - startTime) / 1000.0;
			System.out.println("\tAlgorithmic runtime: " + dfSec.format(runtimeSec));
			System.out.println("\tNumber of backtracks: " + dfBigNum.format(numBacktracks));
			System.out.println("\tNumber of Forward checks: " + dfBigNum.format(ForwardChecks));

			// Sanity check to make sure the computed solution really IS solved
			if (!isSolved(grid))
			{
				System.err.println("\tAn error has been detected in the solution.");
				System.exit(0);
			}
			System.out.println("\n\nSolved sudoku puzzle:");
			printGrid(grid);
		}
		else
			System.out.println("Sudoku Puzzle NOT Solved - No Solution Exists\nExiting Program. . .");
	}

	/////////////////////////////////////////////////////////////////////
	// Write code here which returns true if the sudoku puzzle was solved
	// correctly, and false otherwise. In short, it should check that each
	// row, column, and 3x3 square of 9 cells maintain the ALLDIFF constraint.
	private static boolean isSolved(SudokuCoord[][] grid)
	{
		
		//System.out.println("TODO: Update the code here to complete the method.");
		//System.out.println("The default test case in TestCase.txt IS valid and this method should return true for it.");
		//System.out.println("It is currently hardcoded to return false just so that it compiles.");
		
		for(int val=0; val<9;val++) {
			for(int row=0; row < 9; row++) {
				if(UsedInRow(grid,row,val) == false) {
					return true;
				}
			}
			for(int col=0; col < 9; col++) {
				if(UsedInCol(grid,col,val) == false) {
					return true;
				}
			}
			for(int row=0; row < 9; row+=3) {
				for(int col=0; col < 9; col+=3) {
					if(UsedInBox(grid,row,col,val) == false) {
						return true;
					}
				}	
			}
		}
		return false; 
	}

	/////////////////////////////////////////////////////////////////////
	// Takes a partially filled-in grid and attempts to assign values to
	// all unassigned locations in such a way to meet the requirements
	// for Sudoku solution (non-duplication across rows, columns, and boxes)
	/////////////////////////////////////////////////////////////////////
	static boolean solveSudoku(SudokuCoord grid[][])
	{
		switch (inferenceChoice) {
			case 1:
				return solveSudokuBacktracking(grid);
			case 2:
				return solveSudokuForwardChecking(grid);
			case 3:
				return solveSudokuArcConsistency(grid);
			default:
				System.err.println("Invalid inference method chosen.");
				System.exit(0);
				return false;
		}
	}

	/////////////////////////////////////////////////////////////////////
	// Solves the sudoku puzzle using a standard backtracking approach
	/////////////////////////////////////////////////////////////////////
	static boolean solveSudokuBacktracking(SudokuCoord[][] grid) {
		// Select the next unassigned variable by using one of the
		// 5 algorithms below, as specified by the user
		SudokuCoord variable = getUnassignedVariable(grid);
		
		// If there is no unassigned location, we are done
		if (variable == null)
			return true; // Success!
		else if (variable.isAssigned()) { // Sanity check
			System.err.println("The selected variable has already been assigned: " + variable);
			System.exit(0);
		}

		int row = variable.row;
		int col = variable.col;

		// Consider digits 1 to 9
		for (int num = 1; num <= 9; num++)
		{
			// If looks promising...
			if (isSafe(grid, row, col, num))
			{
				// ... then make tentative assignment
				variable.value = num;

				// Recursive call to assign next variable
				if (solveSudokuBacktracking(grid))
					return true; // Return if success, YAY!

				// If we make it here, it means the last IF statement returned
				// false...which means there was a failure (i.e., incompatible assignment,
				// need for backtrack, etc.); thus we un-assign the value we just assigned
				// and try again
				variable.value = SudokuCoord.UNASSIGNED;
			}
		}

		// Increment the number of backtracks
		numBacktracks++;
		return false; // This triggers backtracking
	}
	
	/////////////////////////////////////////////////////////////////////
	// TODO: Implement the following methods (forward checking is required
	// while arc consistency is optional for bonus), as specified in the
	// project description. You MAY feel free to add extra parameters if
	// you feel it is needed.
	/////////////////////////////////////////////////////////////////////
	
	
	//Method that returns a list of possible values in a cell
	public static LinkedList<Integer> GetPossibleValues(SudokuCoord[][] grid){
		LinkedList<Integer> list = new LinkedList<Integer>();
		
		SudokuCoord variable = getUnassignedVariable(grid);
		int row = variable.row;
		int col = variable.col;
		
		for (int num = 1; num <= 9; num++)
		{
			// If looks promising...
			if (isSafe(grid, row, col, num))
			{
				list.add(num);
			}
		}	
		return list;
	}
	
	/////////////////////////////////////////////////////////////////////
	// Solves the sudoku puzzle using a forward checking
	/////////////////////////////////////////////////////////////////////
	static boolean solveSudokuForwardChecking(SudokuCoord[][] grid) {
		// Select the next unassigned variable by using one of the
		// 5 algorithms below, as specified by the user
		SudokuCoord variable = getUnassignedVariable(grid);
		
		// If there is no unassigned location, we are done
		if (variable == null)
			return true; // Success!
		else if (variable.isAssigned()) { // Sanity check
			System.err.println("The selected variable has already been assigned: " + variable);
			System.exit(0);
		}
		
		// go through the list of possible values
		for( int i : GetPossibleValues(grid)) {

			variable.value = i;
			
			// Recursive call to assign next variable
			if (solveSudokuForwardChecking(grid))
				return true; // Return if success, YAY!
			
			// If we make it here, it means the last IF statement returned
			// false...which means there was a failure (i.e., incompatible assignment,
			// need for backtrack, etc.); thus we un-assign the value we just assigned
			// and try again
			variable.value = SudokuCoord.UNASSIGNED;
			
		}
		// Increment the number of backtracks
		ForwardChecks++;
		return false; // Dummy return for avoiding compiler error
	}
	
	/////////////////////////////////////////////////////////////////////
	// Solves the sudoku puzzle using arc consistency
	/////////////////////////////////////////////////////////////////////
	static boolean solveSudokuArcConsistency(SudokuCoord[][] grid) {
		System.err.println("solveSudokuArcConsistency not yet implemented. Please implement and remove this error/exit statement.");
		System.exit(0);
		return false; // Dummy return for avoiding compiler error
	}
	
	/////////////////////////////////////////////////////////////////////
	// Given the current grid, returns the next unassigned variable
	// according to the variableOrderChoice method selected by the user
	/////////////////////////////////////////////////////////////////////
	static SudokuCoord getUnassignedVariable(SudokuCoord[][] grid) {
		switch (variableOrderChoice) {
			case 1:
				return StaticOrderingOpt1_TopLeft_to_BottomRight(grid);
			case 2:
				return MyOriginalStaticOrderingOpt2(grid);
			case 3:
				return MyOriginalRandomOrderingOpt3(grid);
			case 4:
				return MyMinRemainingValueOrderingOpt4(grid);
			case 5:
				return MyMaxRemainingValueOrderingOpt5(grid);
			default:
				System.err.println("Invalid variable ordering case chosen.");
				System.exit(0);
				return null;
		}
	}

	/////////////////////////////////////////////////////////////////////
	// Searches the grid to find an entry that is still unassigned. If
	// found, the reference parameters row, col will be set the location
	// that is unassigned, and true is returned. If no unassigned entries
	// remain, null is returned.
	/////////////////////////////////////////////////////////////////////
	static SudokuCoord StaticOrderingOpt1_TopLeft_to_BottomRight(SudokuCoord grid[][])
	{
		for (int row = 0; row < N; row++)
			for (int col = 0; col < N; col++)
				if (grid[row][col].value == SudokuCoord.UNASSIGNED)
					return grid[row][col];
		return null;
	}

	/////////////////////////////////////////////////////////////////////
	// TODO: Implement the following orderings, as specified in the
	// project description. You MAY feel free to add extra parameters if
	// needed (you shouldn't need to for the first two, but it may prove
	// helpful for the last two methods).
	/////////////////////////////////////////////////////////////////////
	static SudokuCoord MyOriginalStaticOrderingOpt2(SudokuCoord grid[][])
	{
		// Using While loop to iterate
		int i = 0;
		while(i < grid.length){
		    int j = 0;
		    while(j < grid[i].length){
		    	if (grid[i][j].value == SudokuCoord.UNASSIGNED)
					return grid[i][j];
		        j++;
		    }
		    i++;
		}
		
		return null; // Dummy return for avoiding compiler error
	}
	static SudokuCoord MyOriginalRandomOrderingOpt3(SudokuCoord grid[][])
	{
		
		
		//Initializing two variables with random number from 0-8
		Random rand = new Random();
		int randomNumber1 = rand.nextInt(9);
		int randomNumber2 = rand.nextInt(9);
		
		while(isSolved(grid)) {
			//if this is an unassigned spot then return it
        	if (grid[randomNumber1][randomNumber2].value == SudokuCoord.UNASSIGNED) {
        		System.out.println("Found An Empty Spot");
    			return grid[randomNumber1][randomNumber2];
    			
    		//if this is an assigned spot then do random again
        	}else if (grid[randomNumber1][randomNumber2].isAssigned()) {
        		System.out.println("Not an empty spot");
         		randomNumber1 = rand.nextInt(9);
	    		randomNumber2 = rand.nextInt(9);
			}
		}
		
		
		return null; // Dummy return for avoiding compiler error
		
	}
	static SudokuCoord MyMinRemainingValueOrderingOpt4(SudokuCoord grid[][])
	{
		int min=10;
		int tmp=0;
		
		//Chooses the cell with the minimum possibilities.
		
		for (int row = 0; row < N; row++) {
			for (int col = 0; col < N; col++) {
				if (grid[row][col].value == SudokuCoord.UNASSIGNED) {
					tmp=(int)GetPossibleValues(grid).size();
					if(tmp<min) {
						min=tmp;
						return grid[row][col];
					}
				}
			}
		}
	
		return null; // Dummy return for avoiding compiler error
	}
	
	static SudokuCoord MyMaxRemainingValueOrderingOpt5(SudokuCoord grid[][])
	{
		ArrayList<Integer> list = new ArrayList<Integer>(81);
		ArrayList<Integer> listTwo = new ArrayList<Integer>(81);

		int assignedCellCount = 0;		

		for(int i = 0; i < 9; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				if(grid[i][j].value != SudokuCoord.UNASSIGNED)
				{
					assignedCellCount++;

				}
				/* at end of row, 
				 * add assignedCellCount at row position i 
				 * */
				if(j == 8)
				{
					list.add(i, assignedCellCount);
				}
			}
			/* start over the count for each row */
			assignedCellCount = 0;
		}
		
		int maxAssignmentValue = -1;
		int maxIndex = 0;
		
			/* finding row with most assigned cells */
			for(int i = 0; i < 9; i++)
			{
				if(maxAssignmentValue < list.get(i))
				{
					maxAssignmentValue = list.get(i);
					maxIndex = list.indexOf(i)+1;		
				}
			}
			
			for(int i = 0; i < list.size(); i++)
			{
				listTwo.add(list.get(i));
			}
			
			/* contains the row with most assigned values */	
			for(int col = 0; col < 9; col++)
			{
				if(grid[maxIndex][col].value == SudokuCoord.UNASSIGNED)
				{
					return grid[maxIndex][col];
				}
					list.remove(maxIndex);
			}
	
		return null; // Dummy return for avoiding compiler error

	}

	/////////////////////////////////////////////////////////////////////
	// Returns a boolean which indicates whether any assigned entry
	// in the specified row matches the given number.
	/////////////////////////////////////////////////////////////////////
	static boolean UsedInRow(SudokuCoord grid[][], int row, int num)
	{
		for (int col = 0; col < N; col++)
			if (grid[row][col].value == num)
				return true;
		return false;
	}

	/////////////////////////////////////////////////////////////////////
	// Returns a boolean which indicates whether any assigned entry
	// in the specified column matches the given number.
	/////////////////////////////////////////////////////////////////////
	static boolean UsedInCol(SudokuCoord grid[][], int col, int num)
	{
		for (int row = 0; row < N; row++)
			if (grid[row][col].value == num)
				return true;
		return false;
	}

	/////////////////////////////////////////////////////////////////////
	// Returns a boolean which indicates whether any assigned entry
	// within the specified 3x3 box matches the given number.
	/////////////////////////////////////////////////////////////////////
	static boolean UsedInBox(SudokuCoord grid[][], int boxStartRow, int boxStartCol, int num)
	{
		for (int row = 0; row < 3; row++)
			for (int col = 0; col < 3; col++)
				if (grid[row+boxStartRow][col+boxStartCol].value == num)
					return true;
		return false;
	}

	/////////////////////////////////////////////////////////////////////
	// Returns a boolean which indicates whether it will be legal to assign
	// num to the given row, col location.
	/////////////////////////////////////////////////////////////////////
	static boolean isSafe(SudokuCoord grid[][], int row, int col, int num)
	{
		// Check if 'num' is not already placed in current row,
		// current column and current 3x3 box
		return !UsedInRow(grid, row, num) &&
				!UsedInCol(grid, col, num) &&
				!UsedInBox(grid, row - row%3 , col - col%3, num);
	}

	/////////////////////////////////////////////////////////////////////
	// A utility function to print grid
	/////////////////////////////////////////////////////////////////////
	static void printGrid(SudokuCoord grid[][])
	{
		for (int row = 0; row < N; row++)
		{
			for (int col = 0; col < N; col++)
			{
				if (grid[row][col].value == SudokuCoord.UNASSIGNED)
					System.out.print("- ");
				else
					System.out.print(grid[row][col].value + " ");

				if ((col+1) % 3 == 0)
					System.out.print(" ");
			}	    	   
			System.out.print("\n");
			if ((row+1) % 3 == 0)
				System.out.println();
		}
	}
}