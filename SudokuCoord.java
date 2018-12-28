import java.util.HashSet;
import java.util.Set;

/////////////////////////////////////////////////////////////////////////////////
// CS 430 - Artificial Intelligence
// Project 4 - Sudoku Solver w/ Variable Ordering and Forward Checking
// File: SudokuCoord.java
//
// Description: This class represents a Sudoku coordinate (square), which acts
// as a variable for our constraint satisfaction problem (CSP)
/////////////////////////////////////////////////////////////////////////////////
public class SudokuCoord
{
	final static int UNASSIGNED = 0; //UNASSIGNED (0) is used for the value of an empty cell	
	public int row;
	public int col;
	public int value;
	public Set<Integer> domain; // Not needed until using forward checking or backtracking*
	// *Feel free to use another data structure to keep track of domains if you'd like
	
	SudokuCoord()
	{
		row = 0;
		col = 0;
		value = 0;
		domain = new HashSet<Integer>();
	}
	
	SudokuCoord(int r, int c, int val)
	{
		row = r;
		col = c;
		value = val;
		domain = new HashSet<Integer>();
	}
	
	boolean isAssigned() {
		return value != UNASSIGNED;
	}
	
	public String toString() {
		return "(" + row + ", " + col + ") = " + value;
	}
}
