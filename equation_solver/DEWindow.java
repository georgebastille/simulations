import java.awt.*;
import java.awt.event.*;
import java.io.*;

class DEWindow extends Frame implements ActionListener {

	int L = 100;
    int j = 0; //Convergence Limit counter
	int time = 0;
	int zSlice = 0;

	EGSpace space;

	boolean periodic = false;
	boolean running = true;
	boolean reset = false;
	boolean changedSlice = false;
	boolean laplace = true;

	graphicsPanel threeWayEDisplay;
	graphicsPanel2 threeWayBDisplay;
	rhoCanvas rhoDisplay;
	vCanvas vDisplay;
	eCanvas eDisplay;
	jCanvas jDisplay;
	aCanvas aDisplay;
	bCanvas bDisplay;
	DEControls controls;
	RichieMenuBar menu;
	Scrollbar scrollbar;


	public DEWindow(int x, int y, int L) {

		setTitle("Differential Equation Solver");
		setBackground(Color.black);
		this.L = L;
		space = new EGSpace(L);

		rhoDisplay = new rhoCanvas();
		vDisplay = new vCanvas();
		eDisplay = new eCanvas();
		jDisplay = new jCanvas();
		aDisplay = new aCanvas();
		bDisplay = new bCanvas();
		threeWayEDisplay = new graphicsPanel();
		threeWayBDisplay = new graphicsPanel2();
		controls = new DEControls();
		menu = new RichieMenuBar();
		scrollbar = new Scrollbar(Scrollbar.VERTICAL, 0, 1, 0, L);

		this.setMenuBar(menu);

		scrollbar.addAdjustmentListener(new ScrollBarListener());
		controls.solveB.addActionListener(this);
		controls.resetB.addActionListener(this);
		//controls.sizeF.addActionListener(this);
		menu.laplaceMI.addActionListener(this);
		menu.magneticMI.addActionListener(this);
		menu.staticMI.addActionListener(this);
		menu.periodicMI.addActionListener(this);
		menu.pointChargeMI.addActionListener(this);
		menu.dipoleMI.addActionListener(this);
		menu.quadrupoleMI.addActionListener(this);

		setSize(x, y);

		this.add(threeWayEDisplay, BorderLayout.CENTER);
		this.add(scrollbar, BorderLayout.EAST);
		this.add(controls, BorderLayout.SOUTH);

		// code to close the simulation if the close button is pushed
		addWindowListener(
			new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
	}

	class ScrollBarListener implements AdjustmentListener {
		public void adjustmentValueChanged(AdjustmentEvent e) {
			zSlice = scrollbar.getValue();
			if (laplace) {
				rhoDisplay.repaint();
				vDisplay.repaint();
				eDisplay.repaint();
			} else {
				jDisplay.repaint();
				aDisplay.repaint();
				bDisplay.repaint();
			}
		}
	}

	void solve() {
		double sAvgPrev = 0.0;
		double ratio = 0;
		int i = 0;
		do {
			sAvgPrev = space.sAvg;
			if (periodic) {
				space.setsField(space.gaussSeidelLaplacianP(space.sField));
			} else {
				space.setsField(space.gaussSeidelLaplacianS(space.sField));
			}
			i++;
			if (i > 300 ){
			    j++;
			    System.out.println("Convergence Limit Reached " + j + " times"); 
			    break;
			}

			ratio = sAvgPrev / space.sAvg;
			//System.out.println(ratio);
		} while (ratio < 0.9999 || ratio > 1.0001);

		if (periodic) {
			space.vField = space.gradP();
		} else {
			space.vField = space.gradS();
		}

		vDisplay.repaint();
		eDisplay.repaint();
	}

	void solveMag() {
		int i = 0;

		do {
			if (periodic){
			space.aField = space.gaussSeidelMagneticP(space.aField);
			} else {
			space.aField = space.gaussSeidelMagneticS(space.aField);
			}
	
			i++;
		} while (i < 50);
		
		if (periodic){
		space.bField = space.curlP(space.aField);
		} else {
		space.bField = space.curlS(space.aField);
		}

		jDisplay.repaint();
		aDisplay.repaint();
		bDisplay.repaint();
	}


	public void actionPerformed(ActionEvent e) {
		try {
			if (e.getSource() == controls.solveB) {
				space.moveCharge(1, 0, 0);
				rhoDisplay.repaint();
				solve();
			}
			if (e.getSource() == controls.resetB) {
				space.initialise(L);
				rhoDisplay.repaint();
				vDisplay.repaint();
				eDisplay.repaint();
			}
			if (e.getSource() == menu.magneticMI) {
				if (laplace) {
					space.initJField(L / 4);
					laplace = false;
					this.remove(threeWayEDisplay);
					this.add(threeWayBDisplay, BorderLayout.CENTER);
					solveMag();

				}
			}

			if (e.getSource() == menu.laplaceMI) {
				if (laplace == false) {
					this.remove(threeWayBDisplay);
					this.add(threeWayEDisplay, BorderLayout.CENTER);
					laplace = true;
				}
			}

			if (e.getSource() == menu.staticMI) {
				periodic = false;
				controls.boundaryTypeL.setText("Static");
			}
			if (e.getSource() == menu.periodicMI) {
				periodic = true;
				controls.boundaryTypeL.setText("Periodic");
			}

			if (e.getSource() == menu.pointChargeMI) {
				space.initialise(L);
				space.setRhoElement(L / 2, L / 2, L / 2);
				rhoDisplay.repaint();
				solve();
			}

			if (e.getSource() == menu.dipoleMI) {
				space.initialise(L);
				space.setDipole();
				rhoDisplay.repaint();
				solve();
			}

			if (e.getSource() == menu.quadrupoleMI) {
				space.initialise(L);
				space.setQuadrupole();
				rhoDisplay.repaint();
				solve();
			}
		} catch (Exception ex) {
			System.out.println("Error in the Input");
			System.exit(1);
		}

	}


	class DEControls extends Panel {

	    Button solveB, resetB;//, moveXB, moveYB, moveZB;
		Label equationL, equationTypeL, boundaryL, boundaryTypeL;


		public DEControls() {
			setBackground(Color.gray);
			setLayout(new GridLayout(1, 8));

			//Initialise controls

			solveB = new Button("Move X");
			resetB = new Button("Reset");

			//	sizeL = new Label("Size", Label.RIGHT);
			equationL = new Label("Equation:", Label.RIGHT);
			equationTypeL = new Label("Laplace", Label.LEFT);
			boundaryL = new Label("Boundary:", Label.RIGHT);
			boundaryTypeL = new Label("Static", Label.LEFT);

			//sizeF = new TextField(String.valueOf(L), 4);

			//Add controls to panel

			add(equationL);
			add(equationTypeL);
			add(boundaryL);
			add(boundaryTypeL);
			//add(sizeL);
			//add(sizeF);
			add(solveB);
			add(resetB);
		}

	}

	class RichieMenuBar extends MenuBar {

		Menu equationM, boundaryM, icM;
		MenuItem laplaceMI, magneticMI, staticMI, periodicMI, pointChargeMI, dipoleMI, quadrupoleMI;

		public RichieMenuBar() {
			laplaceMI = new MenuItem("Del^2Phi = Rho");
			magneticMI = new MenuItem("Curl B = J");
			staticMI = new MenuItem("Static");
			periodicMI = new MenuItem("Periodic");
			pointChargeMI = new MenuItem("Point Charge");
			dipoleMI = new MenuItem("Dipole");
			quadrupoleMI = new MenuItem("Quadrupole");

			equationM = new Menu("Equation");
			boundaryM = new Menu("Boundary");
			icM = new Menu("Initial Conditions");

			equationM.add(laplaceMI);
			equationM.add(magneticMI);
			boundaryM.add(staticMI);
			boundaryM.add(periodicMI);
			icM.add(pointChargeMI);
			icM.add(dipoleMI);
			icM.add(quadrupoleMI);

			add(equationM);
			add(boundaryM);
			add(icM);
		}
	}


	class graphicsPanel extends Panel {
		public graphicsPanel() {

			setBackground(Color.black);
			setLayout(new GridLayout(1, 3));
			this.add(rhoDisplay);
			this.add(vDisplay);
			this.add(eDisplay);
		}
	}

	class graphicsPanel2 extends Panel {
		public graphicsPanel2() {

			setBackground(Color.black);
			setLayout(new GridLayout(1, 3));
			this.add(jDisplay);
			this.add(aDisplay);
			this.add(bDisplay);
		}
	}


	class rhoCanvas extends Canvas {
		public rhoCanvas() {
			setBackground(Color.black);
			addMouseListener(
				new MouseAdapter() {
					// Adds a mouse listener using an anonymous inner class

					public void mouseClicked(MouseEvent e) {
						// Creates a mouse event e when clicked
						boolean changed = false;

						Dimension f = getSize();
						// Gets current dimensions
						int xGridSize = f.width / L;
						int yGridSize = f.height / L;

						int xPoint = e.getX() / xGridSize;
						int yPoint = e.getY() / yGridSize;

						if (e.getButton() == 1) {
							space.setRhoElement(xPoint, yPoint, zSlice);
							changed = true;

						} else if (e.getButton() == 3) {
							space.minusRhoElement(xPoint, yPoint, zSlice);
							changed = true;

						}
						if (changed) {
							repaint();
							solve();
							changed = false;
						}
						// redraws the new charges
					}
				});
		}

		public void update(Graphics g) {
			paint(g);
		}


		public void paint(Graphics g) {
			int xspace = this.getWidth() / L;
			int yspace = this.getHeight() / L;
			double curstate;

			for (int i = 0; i < L; i++) {
				for (int j = 0; j < L; j++) {
					curstate = space.rhoField[i][j][zSlice];

					g.setColor(colourMap(curstate, space.rhoMax, space.rhoMin));
					g.fillRect(i * xspace, j * yspace, xspace, yspace);

				}
			}
		}
	}

	class vCanvas extends Canvas {
		public vCanvas() {
			setBackground(Color.black);
		}

		public void update(Graphics g) {
			paint(g);
		}


		public void paint(Graphics g) {
			int xspace = this.getWidth() / L;
			int yspace = this.getHeight() / L;
			double curstate;

			for (int i = 0; i < L; i++) {
				for (int j = 0; j < L; j++) {
					curstate = space.sField[i][j][zSlice];

					g.setColor(colourMap(curstate, (space.rhoMax / 4.0), (space.rhoMin / 4.0)));
					g.fillRect(i * xspace, j * yspace, xspace, yspace);

				}
			}
		}
	}

	class eCanvas extends Canvas {
		public eCanvas() {
			setBackground(Color.gray);
		}


		public void update(Graphics g) {
			paint(g);
		}

		public void drawArrow(Graphics2D g2d, int xCenter, int yCenter, double xD, double yD, float stroke, double mag, boolean up) {
			g2d.setColor(colourMap(mag, space.rhoMax/8.0, space.rhoMin/8.0, up));
			int x = (int) xD;
			int y = (int) yD;
			double aDir = Math.atan2(xCenter - xD, yCenter - yD);
			//g2d.drawLine(x, y, xCenter, yCenter);
			g2d.setStroke(new BasicStroke(1f));
			// make the arrow head solid even if dash pattern has been specified
			Polygon tmpPoly = new Polygon();
			int i1 = 12 + (int) (stroke * 2);
			int i2 = 6 + (int) stroke;
			// make the arrow head the same size regardless of the length length
			/*
			 *  tmpPoly.addPoint(x, y);
			 *  / arrow tip
			 *  tmpPoly.addPoint(x + xCor(i1, aDir + .5), y + yCor(i1, aDir + .5));
			 *  tmpPoly.addPoint(x + xCor(i2, aDir), y + yCor(i2, aDir));
			 *  tmpPoly.addPoint(x + xCor(i1, aDir - .5), y + yCor(i1, aDir - .5));
			 *  tmpPoly.addPoint(x, y);
			 */
			tmpPoly.addPoint(xCenter, yCenter);
			// arrow tip
			tmpPoly.addPoint(xCenter + xCor(i1, aDir + .5), yCenter + yCor(i1, aDir + .5));
			tmpPoly.addPoint(xCenter + xCor(i2, aDir), yCenter + yCor(i2, aDir));
			tmpPoly.addPoint(xCenter + xCor(i1, aDir - .5), yCenter + yCor(i1, aDir - .5));
			tmpPoly.addPoint(xCenter, yCenter);
			// arrow tip
			g2d.drawPolygon(tmpPoly);
			g2d.fillPolygon(tmpPoly);
			// remove this line to leave arrow head unpainted
		}

		private int yCor(int len, double dir) {
			return (int) (len * Math.cos(dir));
		}

		private int xCor(int len, double dir) {
			return (int) (len * Math.sin(dir));
		}


		public void paint(Graphics g) {
			int xspace = getWidth() / L;
			int yspace = getHeight() / L;
			Vector3 curstate;
			g.setColor(Color.gray);
			g.fillRect(0, 0, getWidth(), getHeight());

			for (int i = 0; i < L; i++) {
				for (int j = 0; j < L; j++) {
					curstate = space.vField[i][j][zSlice];
					int x = i * xspace + (xspace / 2);
					int y = j * yspace + (yspace / 2);
					boolean up = (curstate.z > 0) ? true : false;
					//if (curstate.getModulus() > 0.0001) {
						drawArrow((Graphics2D) g, x, y, (-curstate.x) + x, (-curstate.y) + y, 0.5f, curstate.getModulus(), up);
						//}
				}
			}

		}
	}

	class jCanvas extends Canvas {
		public jCanvas() {
			setBackground(Color.white);
		}


		public void update(Graphics g) {
			paint(g);
		}

		public void drawArrow(Graphics2D g2d, int xCenter, int yCenter, double xD, double yD, float stroke, double mag) {
			g2d.setColor(Color.yellow);
			int x = (int) xD;
			int y = (int) yD;
			double aDir = Math.atan2(xCenter - xD, yCenter - yD);
			//g2d.drawLine(x, y, xCenter, yCenter);
			g2d.setStroke(new BasicStroke(1f));
			// make the arrow head solid even if dash pattern has been specified
			Polygon tmpPoly = new Polygon();
			int i1 = 12 + (int) (stroke * 2);
			int i2 = 6 + (int) stroke;
			// make the arrow head the same size regardless of the length length
			/*
			 *  tmpPoly.addPoint(x, y);
			 *  / arrow tip
			 *  tmpPoly.addPoint(x + xCor(i1, aDir + .5), y + yCor(i1, aDir + .5));
			 *  tmpPoly.addPoint(x + xCor(i2, aDir), y + yCor(i2, aDir));
			 *  tmpPoly.addPoint(x + xCor(i1, aDir - .5), y + yCor(i1, aDir - .5));
			 *  tmpPoly.addPoint(x, y);
			 */
			tmpPoly.addPoint(xCenter, yCenter);
			// arrow tip
			tmpPoly.addPoint(xCenter + xCor(i1, aDir + .5), yCenter + yCor(i1, aDir + .5));
			tmpPoly.addPoint(xCenter + xCor(i2, aDir), yCenter + yCor(i2, aDir));
			tmpPoly.addPoint(xCenter + xCor(i1, aDir - .5), yCenter + yCor(i1, aDir - .5));
			tmpPoly.addPoint(xCenter, yCenter);
			// arrow tip
			g2d.drawPolygon(tmpPoly);
			g2d.fillPolygon(tmpPoly);
			// remove this line to leave arrow head unpainted
		}

		private int yCor(int len, double dir) {
			return (int) (len * Math.cos(dir));
		}

		private int xCor(int len, double dir) {
			return (int) (len * Math.sin(dir));
		}


		public void paint(Graphics g) {
			int xspace = getWidth() / L;
			int yspace = getHeight() / L;
			Vector3 curstate;
			g.setColor(Color.white);
			g.fillRect(0, 0, getWidth(), getHeight());

			for (int i = 0; i < L; i++) {
				for (int j = 0; j < L; j++) {
					curstate = space.jField[i][j][zSlice];

					int x = i * xspace + (xspace / 2);
					int y = j * yspace + (yspace / 2);
					if (curstate.getModulus() > 0.0001) {
						drawArrow((Graphics2D) g, x, y, (-100 * curstate.x) + x, (-100 * curstate.y) + y, 0.5f, curstate.getModulus());
					}
				}
			}

		}
	}

	class aCanvas extends Canvas {
		public aCanvas() {
			setBackground(Color.white);
		}


		public void update(Graphics g) {
			paint(g);
		}

		public void drawArrow(Graphics2D g2d, int xCenter, int yCenter, double xD, double yD, float stroke, double mag, boolean up) {
			g2d.setColor(colourMap(mag, 1.0, -1.0, up));
			int x = (int) xD;
			int y = (int) yD;
			double aDir = Math.atan2(xCenter - xD, yCenter - yD);
			//g2d.drawLine(x, y, xCenter, yCenter);
			g2d.setStroke(new BasicStroke(1f));
			// make the arrow head solid even if dash pattern has been specified
			Polygon tmpPoly = new Polygon();
			int i1 = 12 + (int) (stroke * 2);
			int i2 = 6 + (int) stroke;
			// make the arrow head the same size regardless of the length length
			/*
			 *  tmpPoly.addPoint(x, y);
			 *  / arrow tip
			 *  tmpPoly.addPoint(x + xCor(i1, aDir + .5), y + yCor(i1, aDir + .5));
			 *  tmpPoly.addPoint(x + xCor(i2, aDir), y + yCor(i2, aDir));
			 *  tmpPoly.addPoint(x + xCor(i1, aDir - .5), y + yCor(i1, aDir - .5));
			 *  tmpPoly.addPoint(x, y);
			 */
			tmpPoly.addPoint(xCenter, yCenter);
			// arrow tip
			tmpPoly.addPoint(xCenter + xCor(i1, aDir + .5), yCenter + yCor(i1, aDir + .5));
			tmpPoly.addPoint(xCenter + xCor(i2, aDir), yCenter + yCor(i2, aDir));
			tmpPoly.addPoint(xCenter + xCor(i1, aDir - .5), yCenter + yCor(i1, aDir - .5));
			tmpPoly.addPoint(xCenter, yCenter);
			// arrow tip
			g2d.drawPolygon(tmpPoly);
			g2d.fillPolygon(tmpPoly);
			// remove this line to leave arrow head unpainted
		}

		public void drawArrow2() {
			double max = 0.0;
			for (int i = 0; i < L; i++) {
				for (int j = 0; j < L; j++) {
					for (int k = 0; k < L; k++) {
						//System.out.println("i:"+i+"j:"+j+"k:"+k);
						Vector3 x = space.aField[i][j][k];
						double mod = x.getModulus();
						if (mod > max) {
							max = mod;
						}
					}
				}
			}

			float scaling = (float) (1 / max);
			Graphics g = getGraphics();
			int width = getWidth();
			int height = getHeight();
			width = (int) (width / L);
			height = (int) (height / L);
			float h = 0.3f;
			float s = 1.0f;
			float b = 1.0f;
			Vector3 v;
			float vv;
			int x0 = width / 2;
			int y0 = height / 2;
			int x00;
			int y00;
			int x;
			int y;
			double scaleLength;
			if (height < width) {
				scaleLength = height * 0.8;
				//System.out.println("height smaller: scalelength="+scaleLength);
			} else {
				scaleLength = width * 0.8;
				//System.out.println("width smaller: scalelength="+scaleLength);
			}

			for (int i = 0; i < L; i++) {
				for (int j = 0; j < L; j++) {
					x00 = i * width + x0;
					y00 = j * height + y0;
					v = space.aField[i][j][zSlice];
					vv = (float) v.getModulus();
					s = vv * scaling;
					g.setColor(Color.getHSBColor(h, s, b));
					// need to scale the length of the vector such that it fits
					// into the box reserved to draw it: basically make unit vector
					// and multiply it by the smaller length of the box then divide by 2
					x = Math.round((float) ((v.x) * scaleLength / (2 * vv)));
					y = Math.round((float) ((v.y) * scaleLength / (2 * vv)));

					g.drawLine((x00 - x), (y00 - y), (x00 + x), (y00 + y));
					if (v.z > 0.0) {
						g.drawOval((x00 - x - 1), (y00 - y - 1), 3, 3);
					}
					if (v.z < 0.0) {
						g.fillRect((x00 - x - 1), (y00 - y - 1), 3, 3);
					}
				}
			}


		}

		private int yCor(int len, double dir) {
			return (int) (len * Math.cos(dir));
		}

		private int xCor(int len, double dir) {
			return (int) (len * Math.sin(dir));
		}


		public void paint(Graphics g) {
			int xspace = getWidth() / L;
			int yspace = getHeight() / L;
			Vector3 curstate;
			g.setColor(Color.gray);
			g.fillRect(0, 0, getWidth(), getHeight());

			for (int i = 0; i < L; i++) {
				for (int j = 0; j < L; j++) {
					curstate = space.aField[i][j][zSlice];
					int x = i * xspace + (xspace / 2);
					int y = j * yspace + (yspace / 2);
					boolean up = (curstate.z > 0) ? true : false;

					//if (curstate.getModulus() > 0.0001) {
						drawArrow((Graphics2D) g, x, y, (-100 * curstate.x) + x, (-100 * curstate.y) + y, 0.5f, curstate.getModulus(), up);
						//}
				}
			}
			//drawArrow2();

		}
	}

	class bCanvas extends Canvas {
		public bCanvas() {
			setBackground(Color.white);
		}


		public void update(Graphics g) {
			paint(g);
		}

		public void drawArrow(Graphics2D g2d, int xCenter, int yCenter, double xD, double yD, float stroke, double mag, boolean up) {
			g2d.setColor(colourMap(mag, 0.5, -0.5, up));
			int x = (int) xD;
			int y = (int) yD;
			double aDir = Math.atan2(xCenter - xD, yCenter - yD);
			//g2d.drawLine(x, y, xCenter, yCenter);
			g2d.setStroke(new BasicStroke(1f));
			// make the arrow head solid even if dash pattern has been specified
			Polygon tmpPoly = new Polygon();
			int i1 = 12 + (int) (stroke * 2);
			int i2 = 6 + (int) stroke;
			// make the arrow head the same size regardless of the length length
			/*
			 *  tmpPoly.addPoint(x, y);
			 *  / arrow tip
			 *  tmpPoly.addPoint(x + xCor(i1, aDir + .5), y + yCor(i1, aDir + .5));
			 *  tmpPoly.addPoint(x + xCor(i2, aDir), y + yCor(i2, aDir));
			 *  tmpPoly.addPoint(x + xCor(i1, aDir - .5), y + yCor(i1, aDir - .5));
			 *  tmpPoly.addPoint(x, y);
			 */
			tmpPoly.addPoint(xCenter, yCenter);
			// arrow tip
			tmpPoly.addPoint(xCenter + xCor(i1, aDir + .5), yCenter + yCor(i1, aDir + .5));
			tmpPoly.addPoint(xCenter + xCor(i2, aDir), yCenter + yCor(i2, aDir));
			tmpPoly.addPoint(xCenter + xCor(i1, aDir - .5), yCenter + yCor(i1, aDir - .5));
			tmpPoly.addPoint(xCenter, yCenter);
			// arrow tip
			g2d.drawPolygon(tmpPoly);
			g2d.fillPolygon(tmpPoly);
			// remove this line to leave arrow head unpainted
		}

		private int yCor(int len, double dir) {
			return (int) (len * Math.cos(dir));
		}

		private int xCor(int len, double dir) {
			return (int) (len * Math.sin(dir));
		}


		public void paint(Graphics g) {
			int xspace = getWidth() / L;
			int yspace = getHeight() / L;
			Vector3 curstate;
			g.setColor(Color.gray);
			g.fillRect(0, 0, getWidth(), getHeight());

			for (int i = 0; i < L; i++) {
				for (int j = 0; j < L; j++) {
					curstate = space.bField[i][j][zSlice];
					int x = i * xspace + (xspace / 2);
					int y = j * yspace + (yspace / 2);
					boolean up = (curstate.z > 0) ? true : false;

					//if (curstate.getModulus() > 0.0001) {
						drawArrow((Graphics2D) g, x, y, (-100 * curstate.x) + x, (-100 * curstate.y) + y, 0.5f, curstate.getModulus(), up);
						//}
				}
			}

		}
	}

	private Color colourMap(double n, double max, double min) {
		float h;
		float s;
		float b;

		if (max == 0.0) {
			// To avoid division by zero
			max = 0.0001;
		}
		if (min == 0.0) {
			min = -0.0001;
		}

		// Scale the colours based on the max/min charge in rhoSpace
		double mag;
		if (max > Math.abs(min)) {
			mag = max;
		} else {
			mag = Math.abs(min);
		}

		if (n > 0) {
			h = 1.0f;
			// +tive charge is red

		} else {
			h = 0.7f;
			// -tive charge is blue

		}
		s = (float) (Math.abs(n) / mag);

		b = 1.0f;
		return (Color.getHSBColor(h, s, b));
	}

	private Color colourMap(double n, double max, double min, boolean up) {
		float h;
		float s;
		float b;

		if (max == 0.0) {
			// To avoid division by zero
			max = 0.0001;
		}
		if (min == 0.0) {
			min = -0.0001;
		}

		// Scale the colours based on the max/min charge in rhoSpace
		double mag;
		if (max > Math.abs(min)) {
			mag = max;
		} else {
			mag = Math.abs(min);
		}

		if (up) {
			h = 1.0f;
			// +tive charge is red

		} else {
			h = 0.7f;
			// -tive charge is blue

		}
		s = (float) (Math.abs(n) / mag);

		b = 1.0f;
		return (Color.getHSBColor(h, s, b));
	}
}


