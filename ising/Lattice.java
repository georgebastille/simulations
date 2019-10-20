// Need to :-
// Figure out how to redraw clusters and boxes
//	Could have a draw method which access the lattice boolean array
//	before it is cleared, saves passing Cluster objects
// Check that some opposite spins exist before running kawasaki

class Lattice {
	public int size, systemEnergy, spinUp, latticeType;
	public int latticeArray[][];
    public double eBar, heatCapacity;
	public boolean[][] cluster;
	private double exponentials[];
	private double temp, addProb;

	public Lattice(int size, double temp) {
		this.size = size;
		this.temp = temp;
		this.latticeType = 0;
		this.latticeArray = new int[size][size];
		this.cluster = new boolean[size][size];
		this.exponentials = new double[24];
		this.addProb = (1.0 - Math.exp(-2.0 / temp));
		resetSimulation();
	}

	public void setTemp(double newTemp) {
		this.temp = newTemp;
		calculateExponentials();
	}

	public double getTemp() {
		return this.temp;
	}

	public void resetSimulation() {
		int state;
		systemEnergy = 0;
		spinUp = 0;

		calculateExponentials();

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				cluster[i][j] = false;
				state = latticeArray[i][j] = (Math.random() > 0.5) ? 1 : -1;
				systemEnergy += calculateEnergy(i, j);
				if (state == 1) {
					spinUp++;
				}
			}
		}
	}

	private void resetClusterArray() {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				cluster[i][j] = false;
			}
		}
	}

	private void calculateSystemMagnetisation() {
		spinUp = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (latticeArray[i][j] == 1) {
					spinUp++;
				}
			}
		}
	}

	public void calculateExponentials() {
		addProb = (1.0 - Math.exp(-2.0 / temp));
		for (int i = 1; i <= 24; i++) {
			exponentials[i - 1] = Math.exp(-i / temp);
		}
	}

	public void randomiseLattice() {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				// Load a random state into each lattice point
				this.latticeArray[i][j] = (Math.random() > 0.5) ? 1 : -1;
			}
		}
	}

	// Go through the entire lattice and calculate the energy from each point
	// NB: Do not want to do this each time, waste of processor time
	public void calculateTotalSystemEnergy() {
		systemEnergy = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				systemEnergy += calculateEnergy(i, j);
			}
		}
	}

	// Method to calculate the Energy of a single lattice point
	private int calculateEnergy(int x, int y) {

		int xPrev = (x == 0) ? (size - 1) : (x - 1);
		int yPrev = (y == 0) ? (size - 1) : (y - 1);
		int xNext = (x == (size - 1)) ? 0 : (x + 1);
		int yNext = (y == (size - 1)) ? 0 : (y + 1);

		// To include a triangular lattice need to modify this line
		if (latticeType == 0) {
			return (-1 * latticeArray[x][y] * (latticeArray[x][yPrev] + latticeArray[xPrev][y] + latticeArray[xNext][y] + latticeArray[x][yNext]));
		} else {
			return (-1 * latticeArray[x][y] * (latticeArray[x][yPrev] + latticeArray[xPrev][y] + latticeArray[xNext][y] + latticeArray[x][yNext] + latticeArray[xPrev][yNext] + latticeArray[xNext][yPrev]));
		}
	}

	public void changeLatticeSize(int newSize) {
		int[][] newLattice = new int[newSize][newSize];
		cluster = new boolean[newSize][newSize];

		if (newSize < size) {
			for (int i = 0; i < newSize; i++) {
				for (int j = 0; j < newSize; j++) {
					newLattice[i][j] = latticeArray[i][j];
				}
			}
			latticeArray = newLattice;
			size = newSize;
		} else {
			int[][] oldLattice = latticeArray;
			latticeArray = newLattice;
			int oldSize = size;
			size = newSize;
			randomiseLattice();
			for (int i = 0; i < oldSize; i++) {
				for (int j = 0; j < oldSize; j++) {
					latticeArray[i][j] = oldLattice[i][j];
				}
			}
		}
		calculateTotalSystemEnergy();
		calculateSystemMagnetisation();
	}

	public void calculateHeatCapacity(int [] energy) {
		eBar = 0;
		double eSquaredBar = 0;
		int currentEnergy = 0;
		int window = energy.length;

		for (int i = 0; i < window; i++) {
			currentEnergy = energy[i];
			eBar += currentEnergy;
			eSquaredBar += currentEnergy * currentEnergy;
		}

		eBar /= (double)window;
		eSquaredBar /= (double)window;
		heatCapacity = (eSquaredBar - (eBar * eBar)) / (temp * temp);
		heatCapacity /= (double)(size * size);
		eBar /= (double)(size * size);
	}


	public void runMetropolisSweep() {

		int state;
		int energyDifference;

		// Go through the lattice in Checkerboard Fashion
		for (int k = 0; k < 2; k++) {
			for (int j = 0; j < size; j++) {
				for (int i = (j + k) % 2; i < size; i += 2) {

					energyDifference = -2 * calculateEnergy(i, j);

					// If system is more stable accept the change
					if (energyDifference < 1) {
						state = (latticeArray[i][j] *= -1);
						systemEnergy += (2 * energyDifference);
						spinUp = (state == 1) ? spinUp + 1 : spinUp - 1;
					} else {
						// Otherwise flip with a probability based on temperature
						if (exponentials[energyDifference - 1] > Math.random()) {
							state = (latticeArray[i][j] *= -1);
							systemEnergy += (2 * energyDifference);
							spinUp = (state == 1) ? spinUp + 1 : spinUp - 1;
						}
					}
				}
			}
		}
	}

	public boolean runMetropolis(int i, int j) {
		int state;
		int energyDifference;
		boolean changed = false;

		energyDifference = -2 * calculateEnergy(i, j);

		// If system is more stable accept the change
		if (energyDifference < 1) {
			state = (latticeArray[i][j] *= -1);
			systemEnergy += (2 * energyDifference);
			spinUp = (state == 1) ? spinUp + 1 : spinUp - 1;
			changed = true;
		} else {
			// Otherwise flip with a probability based on temperature
			if (exponentials[energyDifference - 1] > Math.random()) {
				state = (latticeArray[i][j] *= -1);
				systemEnergy += (2 * energyDifference);
				spinUp = (state == 1) ? spinUp + 1 : spinUp - 1;
				changed = true;
			}
		}
		return changed;
	}


	public int runWolff() {

		resetClusterArray();

		int xToFlip = (int) (Math.random() * size);
		int yToFlip = (int) (Math.random() * size);

		int oldState = latticeArray[xToFlip][yToFlip];
		int newState = -1 * oldState;

		growCluster(xToFlip, yToFlip, oldState, newState);

		return newState;
	}

	private void growCluster(int x, int y, int oldState, int newState) {
		cluster[x][y] = true;

		systemEnergy += (-4 * calculateEnergy(x, y));

		latticeArray[x][y] = newState;
		spinUp = (newState == 1) ? spinUp + 1 : spinUp - 1;

		int xPrev = (x == 0) ? size - 1 : x - 1;
		int xNext = (x == size - 1) ? 0 : x + 1;
		int yPrev = (y == 0) ? size - 1 : y - 1;
		int yNext = (y == size - 1) ? 0 : y + 1;

		// For triangular lattice need to modify this bit too
		if (!cluster[xPrev][y]) {
			tryAdd(xPrev, y, oldState, newState);
		}
		if (!cluster[xNext][y]) {
			tryAdd(xNext, y, oldState, newState);
		}
		if (!cluster[x][yPrev]) {
			tryAdd(x, yPrev, oldState, newState);
		}
		if (!cluster[x][yNext]) {
			tryAdd(x, yNext, oldState, newState);
		}
		if (latticeType == 1) {
			if (!cluster[xPrev][yNext]) {
				tryAdd(x, yNext, oldState, newState);
			}
			if (!cluster[xNext][yPrev]) {
				tryAdd(x, yNext, oldState, newState);
			}
		}
	}

	private void tryAdd(int x, int y, int oldState, int newState) {
		if (latticeArray[x][y] == oldState) {
			if (Math.random() < addProb) {
				growCluster(x, y, oldState, newState);
			}
		}
	}


	public void runKawasakiSweep() {
		// Points to remember
		// Numbers are conserved so if lattice is cold, Kawasaki won't work
		// System Energy is not 2 * Energy diff if pair site is a neighbour
		int energyDifference;
		int xPair;
		int yPair;
		int state;

		// Go through the array in checkerboard fashion
		for (int k = 0; k < 2; k++) {
			for (int j = 0; j < size; j++) {
				for (int i = (j + k) % 2; i < size; i += 2) {

					state = latticeArray[i][j];
					// Find a random spin which is opposite to the current one
					// And not a neighbour of the current one
					do {
						xPair = (int) (Math.random() * size);
						yPair = (int) (Math.random() * size);
					} while (latticeArray[xPair][yPair] == state);
					
					energyDifference = -2 * (calculateEnergy(i, j) + calculateEnergy(xPair, yPair));

					if (isNeighbour(i, j, xPair, yPair))
						energyDifference += 4;

					// If system is more stable accept the change
					if (energyDifference < 1) {
						latticeArray[i][j] *= -1;
						latticeArray[xPair][yPair] *= -1;
						systemEnergy += (2 * energyDifference);
					} else {
						// Otherwise flip with a probability based on temperature
						if (exponentials[energyDifference - 1] > Math.random()) {
							latticeArray[i][j] *= -1;
							latticeArray[xPair][yPair] *= -1;
							systemEnergy += (2 * energyDifference);
						}
					}

				}
			}
		}
	}

	public int[] runKawasaki(int i, int j) {
		// Points to remember
		// Numbers are conserved so if lattice is cold, Kawasaki won't work
		// System Energy is not 2 * Energy diff if pair site is a neighbour
		int energyDifference;
		int xPair;
		int yPair;
		int state = latticeArray[i][j];
		int[] pair = new int[3];
		// Coordinates and state of the pair spin, for painting

		// Find a random spin which is opposite to the current one
		// And not a neighbour of the current one
		do {
			xPair = (int) (Math.random() * size);
			yPair = (int) (Math.random() * size);
		} while (latticeArray[xPair][yPair] == state);

		// If they are neighbours they affect each others energy by +1 from A to B and +1 from B to A
		// So they will also affect esch others energy when flipped by the same amount, giving a total diff of +4
		
		energyDifference = -2 * (calculateEnergy(i, j) + calculateEnergy(xPair, yPair));

		if (isNeighbour(i, j, xPair, yPair)) 
			energyDifference += 4;

		// If system is more stable accept the change
		if (energyDifference < 1) {
			latticeArray[xPair][yPair] *= -1;
			latticeArray[i][j] *= -1;
			systemEnergy += (2 * energyDifference);
			pair[0] = xPair;
			pair[1] = yPair;
			pair[2] = state;
		} else {
			// Otherwise flip with a probability based on temperature
			if (exponentials[energyDifference - 1] > Math.random()) {
				latticeArray[xPair][yPair] *= -1;
				latticeArray[i][j] *= -1;
				systemEnergy += (2 * energyDifference);
				pair[0] = xPair;
				pair[1] = yPair;
				pair[2] = state;
			} else {
				pair[2] = 0;
				// To tell the graphics it hasn't changed
			}
		}
		return pair;
	}


	private boolean isNeighbour(int i, int j, int x, int y) {
		int a = Math.abs(i-x);
		int b = Math.abs(j-y);
		
		if ((b == 0)&&((a == 1)||(a == (size-1))))
			return true;
		
		if ((a == 0)&&((b == 1)||(b == (size-1))))
			return true;
		
		return false;
	}

	public void runSwendsenWang() {
		// Points to consider
		// Pick a random box size and flip all spins in box
		// Need a cunning method so that the box can go over the boundary
		int boxSize;
		int xStart;
		int yStart;
		// Top Left coordinates of the box
		int oldEnergy = 0;
		// Top Left coordinates of the box
		int newEnergy = 0;
		// Top Left coordinates of the box
		int energyDifference;
		int state;

		xStart = (int) (Math.random() * size);
		yStart = (int) (Math.random() * size);

		// Make sure the box size is greater than zero, which is just silly
	
		do {
			boxSize = (int) (Math.random() * size);
		} while (boxSize < 1);

		// Calculate the old Energy
		for (int i = xStart; i <= (xStart + boxSize); i++) {
			// Allow the box to wrap around the boundary
			int x = (i > (size - 1)) ? (i - size) : i;
			for (int j = yStart; j <= (yStart + boxSize); j++) {
				int y = (j > (size - 1)) ? (j - size) : j;

				oldEnergy += calculateEnergy(x, y);
			}
		}

		// Flip all spins in the box
		for (int i = xStart; i <= (xStart + boxSize); i++) {
			// Allow the box to wrap around the boundary
			int x = (i > (size - 1)) ? (i - size) : i;
			for (int j = yStart; j <= (yStart + boxSize); j++) {
				int y = (j > (size - 1)) ? (j - size) : j;

				state = latticeArray[x][y] *= -1;
				spinUp = (state == 1) ? spinUp + 1 : spinUp - 1;

			}
		}

		// Calculate the new Energy
		for (int i = xStart; i <= (xStart + boxSize); i++) {
			// Allow the box to wrap around the boundary
			int x = (i > (size - 1)) ? (i - size) : i;
			for (int j = yStart; j <= (yStart + boxSize); j++) {
				int y = (j > (size - 1)) ? (j - size) : j;

				newEnergy += calculateEnergy(x, y);
			}
		}

		energyDifference = newEnergy - oldEnergy;
		//System.out.print(energyDifference);

		// If system is more stable accept the change
		if (energyDifference < 1) {
			systemEnergy += 2*energyDifference;
			//System.out.print(" - Flipped");
		} else {
			// Otherwise flip with a probability based on temperature
			if (Math.exp(-1 * (double)energyDifference / temp) > Math.random()) {
				systemEnergy += 2*energyDifference;
				//System.out.print(" - Flipped");

			} else {
				// Flip all spins back
				//System.out.print(" - Not Flipped");
				for (int i = xStart; i <= (xStart + boxSize); i++) {
					int x = (i > (size - 1)) ? (i - size) : i;
					for (int j = yStart; j <= (yStart + boxSize); j++) {
						int y = (j > (size - 1)) ? (j - size) : j;
						state = latticeArray[x][y] *= -1;
						spinUp = (state == 1) ? spinUp + 1 : spinUp - 1;
					}
				}
			}
		}
		//System.out.println();
	}
}

