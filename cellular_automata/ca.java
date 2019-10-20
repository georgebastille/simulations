/*
 *  Cellular Automata - Forest Fire
 *
 *  This java program simulates a forest Fire
 *  It has 3 states, 1 = Healthy
 *  2 = On Fire
 *  3 = Burnt out
 *  And a set of rules which update it.
 *  The nearest Neighbours can be set to 4, 6 or 8
 *  with 6 corresponding to a hexagonal lattice
 *
 *  Author: Richard Hanes 	Date: 02/02/05
 */
import java.io.*;

public class ca {
	public static void main(String[] args) {
		CAWindow cp2 = new CAWindow(755,462);
		cp2.setVisible(true);
		cp2.run();
	}
}


