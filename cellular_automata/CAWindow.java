import java.awt.*;
import java.awt.event.*;
import java.io.*;

class CAWindow extends Frame implements ActionListener {

	int[][] lattice1, lattice2;
	int L = 100;
	int N = 8;
	// no of neighbours 4, 6 or 8;
	int xspace, yspace, cX = 1, cY = 0;
	// L = linear size and cX, cY are counters for the typewriter method
	int time = 0;

	double p = 0.90;
	double q = 1.0;
	double infected = 0.02;
	double immune = 0.1;

	boolean using1 = true;
	boolean periodic = false;
	boolean stohastic = false;
	boolean running = true;
	boolean reset;
	boolean neighboursChanged = false;
	int[] stats = {0, 0, 0, 0};
	// store the number of sites in each state

	Graphics f;
	CACanvas screen;
	CAControls controls;
	CAInfo info;


	public CAWindow(int x, int y) {
		lattice1 = new int[L][L];
		lattice2 = new int[L][L];
		initialise();

		setTitle("SIR Model Simulator - Richard Hanes");
		setBackground(Color.black);
		screen = new CACanvas();
		controls = new CAControls();
		info = new CAInfo();

		controls.periodicB.addActionListener(this);
		controls.staticB.addActionListener(this);
		controls.stohasticB.addActionListener(this);
		controls.simultaneousB.addActionListener(this);
		controls.neighboursF.addActionListener(this);
		controls.pF.addActionListener(this);
		controls.qF.addActionListener(this);
		controls.resetB.addActionListener(this);
		controls.startB.addActionListener(this);
		controls.pauseB.addActionListener(this);
		controls.stepB.addActionListener(this);

		setSize(x, y);
		this.add(screen, BorderLayout.CENTER);
		this.add(controls, BorderLayout.EAST);
		this.add(info, BorderLayout.SOUTH);
		this.pack();

		// code to close the simulation if the close button is pushed
		addWindowListener(
			new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
	}


	void initialise() {
		int immuneCount = (int) (L * L * immune);
		int infectedCount = (int) (L * L * infected);

		for (int i = 0; i < 4; i++) {
			stats[i] = 0;
		}

		for (int i = 0; i < L; i++) {
			for (int j = 0; j < L; j++) {
				setCell(i, j, 1);
			}
		}

		for (int i = 0; i < immuneCount; i++) {
			int x = (int) (Math.random() * L);
			int y = (int) (Math.random() * L);

			while (lattice1[x][y] == 0) {
				x = (int) (Math.random() * L);
				y = (int) (Math.random() * L);
			}
			setCell(x, y, 0);
		}

		for (int i = 0; i < infectedCount; i++) {
			int x = (int) (Math.random() * L);
			int y = (int) (Math.random() * L);

			while (lattice1[x][y] == 0 || lattice1[x][y] == 2) {
				x = (int) (Math.random() * L);
				y = (int) (Math.random() * L);
			}
			setCell(x, y, 2);
		}

		for (int i = 0; i < L; i++) {
			for (int j = 0; j < L; j++) {
				stats[lattice1[i][j]]++;

			}
		}
	}


	void run() {
		while (true) {
			if (reset) {
				reset = false;
				infected = Double.parseDouble(controls.infectedF.getText());
				immune = Double.parseDouble(controls.immuneF.getText());
				p = Double.parseDouble(controls.pF.getText());
				q = Double.parseDouble(controls.qF.getText());

				initialise();
				info.updateinfo();
			}

			if (neighboursChanged) {
				neighboursChanged = false;
				int Ntemp = Integer.parseInt(controls.neighboursF.getText());
				if ((Ntemp != 4) && (Ntemp != 6) && (Ntemp != 8)){
					N = 8;
					controls.neighboursF.setText("8");
				} else {
					N = Ntemp;
				}
			}

			if (running) {
				if (stohastic) {
					randStep();
				} else {
					typewriterStep();
				}
				
				if (time % (L*L) == 0)
					info.updateinfo();
				
				time++;
				
			} else {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {}
			}
		}
	}


	void setCell(int x, int y, int state) {
		lattice1[x][y] = lattice2[x][y] = state;
	}


	void printStats() {
		System.out.println("Healthy: " + stats[1] + "\tBurning: " + stats[2] + "\tDead: " + stats[3]);
	}


	void randStep() {
		int x = (int) (Math.random() * L);
		int y = (int) (Math.random() * L);

		if (using1) {
			if (processRules(x, y, lattice1)) {
				paintSquare(x, y, lattice1[x][y]);
			}
		} else {
			if (processRules(x, y, lattice2)) {
				paintSquare(x, y, lattice2[x][y]);
			}
		}
		//try{Thread.sleep(1);}catch(InterruptedException e){}
	}


	void typewriterStep() {

		if (using1) {
			processRules(cX, cY, lattice1, lattice2);
		} else {
			processRules(cX, cY, lattice2, lattice1);
		}

		if (cX < (L - 1)) {
			cX++;
		} else if (cY < (L - 1)) {
			cX = 0;
			cY++;
		} else {
			cX = cY = 0;
		}

		if (cY == 0 && cX == 0) {
			if (using1) {
				using1 = false;
				screen.repaint();
				try{Thread.sleep(50);}catch(InterruptedException e){}

			} else {
				using1 = true;
				screen.repaint();
				try{Thread.sleep(50);}catch(InterruptedException e){}

			}
		}
	}


	int[] findNeighbours(int x, int y, int[][] lattice) {
		int[] neighbours = new int[N];
		// 0-N 1-W 2-E 3-S 4-NE 5-SW 6-NW 7-SE
		if (x == 0) {
			neighbours[1] = 0;
		} else {
			neighbours[1] = lattice[x - 1][y];
		}

		if (x == (L - 1)) {
			neighbours[2] = 0;
		} else {
			neighbours[2] = lattice[x + 1][y];
		}

		if (y == 0) {
			neighbours[0] = 0;
		} else {
			neighbours[0] = lattice[x][y - 1];
		}

		if (y == (L - 1)) {
			neighbours[3] = 0;
		} else {
			neighbours[3] = lattice[x][y + 1];
		}

		if (N == 4) {
			return neighbours;
		}

		if (x == (L - 1) || y == 0) {
			neighbours[4] = 0;
		} else {
			neighbours[4] = lattice[x + 1][y - 1];
		}

		if (x == 0 || y == (L - 1)) {
			neighbours[5] = 0;
		} else {
			neighbours[5] = lattice[x - 1][y + 1];
		}

		if (N == 6) {
			return neighbours;
		}

		if (x == 0 || y == 0) {
			neighbours[6] = 0;
		} else {
			neighbours[6] = lattice[x - 1][y - 1];
		}

		if (x == (L - 1) || y == (L - 1)) {
			neighbours[7] = 0;
		} else {
			neighbours[7] = lattice[x + 1][y + 1];
		}
		return neighbours;
	}


	int[] findNeighboursPeriodic(int x, int y, int[][] lattice) {
		int[] neighbours = new int[N];
		// 0-N 1-W 2-E 3-S 4-NE 5-SW 6-NW 7-SE
		if (x == 0) {
			neighbours[1] = lattice[L - 1][y];
		} else {
			neighbours[1] = lattice[x - 1][y];
		}

		if (x == (L - 1)) {
			neighbours[2] = lattice[0][y];
		} else {
			neighbours[2] = lattice[x + 1][y];
		}

		if (y == 0) {
			neighbours[0] = lattice[x][L - 1];
		} else {
			neighbours[0] = lattice[x][y - 1];
		}

		if (y == (L - 1)) {
			neighbours[3] = lattice[x][0];
		} else {
			neighbours[3] = lattice[x][y + 1];
		}

		if (N == 4) {
			return neighbours;
		}

		if (x == (L - 1) || y == 0) {
			if (x == (L - 1) && y != 0) {
				neighbours[4] = lattice[0][y - 1];
			} else if (y == 0 && x != (L - 1)) {
				neighbours[4] = lattice[x + 1][L - 1];
			} else {
				neighbours[4] = lattice[0][L - 1];
			}
		} else {
			neighbours[4] = lattice[x + 1][y - 1];
		}

		if (x == 0 || y == (L - 1)) {
			if (x == 0 && y != (L - 1)) {
				neighbours[5] = lattice[L - 1][y + 1];
			} else if (y == (L - 1) && x != 0) {
				neighbours[5] = lattice[x - 1][0];
			} else {
				neighbours[5] = lattice[L - 1][0];
			}

		} else {
			neighbours[5] = lattice[x - 1][y + 1];
		}

		if (N == 6) {
			return neighbours;
		}

		if (x == 0 || y == 0) {
			if (x == 0 && y != 0) {
				neighbours[6] = lattice[L - 1][y - 1];
			} else if (y == 0 && x != 0) {
				neighbours[6] = lattice[x - 1][L - 1];
			} else {
				neighbours[6] = lattice[L - 1][L - 1];
			}
		} else {
			neighbours[6] = lattice[x - 1][y - 1];
		}

		if (x == (L - 1) || y == (L - 1)) {
			if (x == (L - 1) && y != (L - 1)) {
				neighbours[7] = lattice[0][y + 1];
			} else if (y == (L - 1) && x != (L - 1)) {
				neighbours[7] = lattice[x + 1][0];
			} else {
				neighbours[7] = lattice[0][0];
			}
		} else {
			neighbours[7] = lattice[x + 1][y + 1];
		}
		return neighbours;
	}


	// For the random selection method, only needs on one lattice
	boolean processRules(int x, int y, int[][] lattice) {
		int state = lattice[x][y];

		if (state == 2) {
			lattice[x][y] = 3;
			stats[2]--;
			stats[3]++;
			return true;
		}

		if (state == 0) {
			return true;
		}

		boolean changed = false;
		int[] neighbours;
		if (periodic) {
			neighbours = findNeighboursPeriodic(x, y, lattice);
		} else {
			neighbours = findNeighbours(x, y, lattice);
		}

		if (state == 1) {
			boolean infected = false;
			for (int i = 0; i < neighbours.length; i++) {
				if (neighbours[i] == 2) {
					infected = true;
					break;
				}
			}
			if (infected && (Math.random() < p)) {
				lattice[x][y] = 2;
				stats[1]--;
				stats[2]++;
				changed = true;

			}
		} else {
			boolean safe = true;
			for (int i = 0; i < neighbours.length; i++) {
				if (neighbours[i] == 2) {
					safe = false;
					break;
				}
			}
			if (safe && (Math.random() < q)) {
				lattice[x][y] = 1;
				stats[3]--;
				stats[1]++;
				changed = true;
			}
		}
		return changed;
	}


	// For the typewriter, looks at one, updates another
	void processRules(int x, int y, int[][] lattice1, int[][] lattice2) {
		int state = lattice1[x][y];

		if (state == 2) {
			lattice2[x][y] = 3;
			stats[2]--;
			stats[3]++;
			return;
		}

		if (state == 0) {
			return;
		}

		int[] neighbours;
		if (periodic) {
			neighbours = findNeighboursPeriodic(x, y, lattice1);
		} else {
			neighbours = findNeighbours(x, y, lattice1);
		}

		if (state == 1) {
			boolean infected = false;
			for (int i = 0; i < neighbours.length; i++) {
				if (neighbours[i] == 2) {
					infected = true;
					break;
				}
			}
			if (infected && (Math.random() < p)) {
				lattice2[x][y] = 2;
				stats[1]--;
				stats[2]++;
			} else {
				lattice2[x][y] = 1;
			}

			return;
		} else {
			boolean safe = true;
			for (int i = 0; i < neighbours.length; i++) {
				if (neighbours[i] == 2) {
					safe = false;
					break;
				}
			}
			if (safe && (Math.random() < q)) {
				lattice2[x][y] = 1;
				stats[3]--;
				stats[1]++;
			} else {
				lattice2[x][y] = 3;
			}

			return;
		}
	}


	public void actionPerformed(ActionEvent e) {
		try {
			if (e.getSource() == controls.startB) {
				running = true;
			}

			if (e.getSource() == controls.pauseB) {
				running = false;
			}
			if (e.getSource() == controls.stepB) {
				if (running == false) {
					if (stohastic) {
						int steps = L * L;
						for (int i = 0; i < steps; i++) {
							randStep();
						}
					} else {
						int steps = L * L;
						for (int i = 0; i < steps; i++) {
							typewriterStep();
						}
					}
				}
			}
			if (e.getSource() == controls.resetB) {
				reset = true;
			}

			if (e.getSource() == controls.periodicB) {
				periodic = true;
			}

			if (e.getSource() == controls.staticB) {
				periodic = false;
			}

			if (e.getSource() == controls.stohasticB) {
				stohastic = true;
			}

			if (e.getSource() == controls.simultaneousB) {
				stohastic = false;
			}

			if (e.getSource() == controls.pF) {
				p = Double.parseDouble(controls.pF.getText());
			}

			if (e.getSource() == controls.qF) {
				q = Double.parseDouble(controls.qF.getText());
			}

			if (e.getSource() == controls.neighboursF) {
				neighboursChanged = true;
			}


		} catch (Exception ex) {
			System.out.println("Error in the Input");
			System.exit(1);
		}
	}


	public void paint(Graphics g) {
		screen.repaint();
	}


	public void paintSquare(int x, int y, int curstate) {
		f = screen.getGraphics();

		xspace = screen.getWidth() / L;
		yspace = screen.getHeight() / L;
		 {
			if (curstate == 1) {
				f.setColor(Color.green);
			} else if (curstate == 2) {
				f.setColor(Color.red);
			} else if (curstate == 3) {
				f.setColor(Color.black);
			} else {
				f.setColor(Color.blue);
			}

			f.fillRect(x * xspace, y * yspace, xspace, yspace);
		}
	}


	class CAControls extends Panel {

		Button periodicB, staticB, stohasticB, simultaneousB, startB, pauseB, stepB, resetB;
		TextField neighboursF, infectedF, immuneF, pF, qF;
		Label neighboursL, infectedL, immuneL, pL, qL, boundaryL, updateL, blank1L, blank2L, blank3L, blank4L;


		public CAControls() {
			setBackground(Color.gray);
			setLayout(new GridLayout(12, 2));

			//Initialise controls

			periodicB = new Button("Periodic");
			staticB = new Button("Static");
			stohasticB = new Button("Stohastic");
			simultaneousB = new Button("Simultaneous");
			startB = new Button("Start");
			pauseB = new Button("Pause");
			stepB = new Button("Step");
			resetB = new Button("Reset");

			neighboursL = new Label("Neighbours (4,6,8)", Label.RIGHT);
			infectedL = new Label("% Infected* (0-1)", Label.RIGHT);
			immuneL = new Label("% Immune* (0-1)", Label.RIGHT);
			pL = new Label("p (0-1)", Label.RIGHT);
			qL = new Label("q (0-1)", Label.RIGHT);
			boundaryL = new Label("Boundary", Label.CENTER);
			updateL = new Label("Update Method", Label.CENTER);
			blank1L = new Label("");
			blank2L = new Label("");
			blank3L = new Label("");
			blank4L = new Label("");

			neighboursF = new TextField(String.valueOf(N), 4);
			infectedF = new TextField(String.valueOf(infected), 4);
			immuneF = new TextField(String.valueOf(immune), 4);
			pF = new TextField(String.valueOf(p), 4);
			qF = new TextField(String.valueOf(q), 4);

			//Add controls to panel

			add(infectedL);
			add(infectedF);
			add(immuneL);
			add(immuneF);
			add(neighboursL);
			add(neighboursF);
			add(pL);
			add(pF);
			add(qL);
			add(qF);
			add(boundaryL);
			add(blank1L);
			add(periodicB);
			add(staticB);
			add(updateL);
			add(blank2L);
			add(stohasticB);
			add(simultaneousB);
			add(blank3L);
			add(blank4L);
			add(startB);
			add(pauseB);
			add(stepB);
			add(resetB);
		}

	}


	class CAInfo extends Panel {

		Label healthyL, healthyV, infectedL, infectedV, immuneL, immuneV;


		public CAInfo() {
			setBackground(Color.gray);

			setLayout(new GridLayout(1, 6));

			//Initialise info boxes

			healthyL = new Label("Healthy: ", Label.RIGHT);
			healthyV = new Label(String.valueOf(stats[1]), Label.LEFT);
			infectedL = new Label("Infected: ", Label.RIGHT);
			infectedV = new Label(String.valueOf(stats[2]), Label.LEFT);
			immuneL = new Label("Immune: ", Label.RIGHT);
			immuneV = new Label(String.valueOf(stats[3] + stats[0]), Label.LEFT);

			add(healthyL);
			add(healthyV);
			add(infectedL);
			add(infectedV);
			add(immuneL);
			add(immuneV);

		}
		
		void updateinfo(){
			healthyV.setText(String.valueOf(stats[1]));
			infectedV.setText(String.valueOf(stats[2]));
			immuneV.setText(String.valueOf(stats[3] + stats[0]));
		}
			
	}



	class CACanvas extends Canvas {
		public CACanvas() {
			setBackground(Color.black);
			//setSize(x, y);

		}


		public void update(Graphics g) {
			paint(g);
		}


		public void paint(Graphics g) {
			xspace = getWidth() / L;
			yspace = getHeight() / L;
			int curstate;
			int [][] lattice;
			if (using1) lattice = lattice1;
			else lattice = lattice2;

			for (int i = 0; i < L; i++) {
				for (int j = 0; j < L; j++) {
					curstate = lattice[i][j];

					if (curstate == 1) {
						g.setColor(Color.green);
					} else if (curstate == 2) {
						g.setColor(Color.red);
					} else if (curstate == 3) {
						g.setColor(Color.black);
					} else {
						g.setColor(Color.blue);
					}

					g.fillRect(i * xspace, j * yspace, xspace, yspace);

				}
			}
		}
	}
}


