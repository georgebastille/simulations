import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

class expFrame extends JFrame implements ActionListener {

    int dynamicsType = 0, orderType = 0, latticeSize = 100;
    int iterationCounter = 0, observableWindow = 100, eqSweeps;
    int[] energy = new int[observableWindow];
    double startTemp, endTemp, dT, temp;
    int x;
    int y;
    int noOfSpins = latticeSize * latticeSize;
    Lattice isingLattice;
    String filename;
	
    JPanel expPanel;
    JLabel currentTV;
    JTextField sizeV, startTV, endTV, dTV, eqSweepsV, filenameV, windowV;
    JComboBox algorithmB, latticeB, updateB;
    JButton goB;
	
    public expFrame() {
		
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	isingLattice = new Lattice(100, 0.0);
	setTitle("Experiment Lab");
	expPanel = new JPanel(new GridLayout(8, 3));
	addWidgets();
	this.add(expPanel);
	this.pack();
    }
	
    private void addWidgets() {
	JLabel sizeL, startTL, endTL, dTL, algorithmL, latticeL;
	JLabel updateL, eqSweepsL, filenameL, currentTL, windowL;
		
	String[] dynamics = {"Metropolis", "Wolff", "Kawasaki", "Swensen-Wang"};
	String[] order = {"Checkerboard", "Stohastic"};
	String[] lattice = {"Square", "Triangular"};
		
	algorithmB = new JComboBox(dynamics);
	latticeB = new JComboBox(lattice);
	updateB = new JComboBox(order);

	sizeL = new JLabel("LinearSize:");
	startTL = new JLabel("Initial Temp:");
	endTL = new JLabel("Final Temp:");
	dTL = new JLabel("Temperature Step:");
	algorithmL = new JLabel("Algorithm:");
	latticeL = new JLabel("Lattice Type:");
	updateL = new JLabel("Update Type:");
	eqSweepsL = new JLabel("Equilibrium Its:");
	filenameL = new JLabel("Log File Name:");
	currentTL = new JLabel("Current Temp");
	currentTV = new JLabel("0.0");
	windowL = new JLabel("Observable Wdw:");
		
	sizeV = new JTextField(""+latticeSize+"");
	startTV = new JTextField("0.0");
	endTV = new JTextField("5.0");
	dTV = new JTextField("0.01");
	eqSweepsV = new JTextField("20");
	filenameV = new JTextField("isingdata.dat");
	windowV = new JTextField("100");

	goB = new JButton("Run Experiment");

	expPanel.add(sizeL);
	expPanel.add(algorithmL);
	expPanel.add(windowL);
	expPanel.add(sizeV);
	expPanel.add(algorithmB);
	expPanel.add(windowV);
		
	expPanel.add(startTL);
	expPanel.add(latticeL);
	expPanel.add(filenameL);
	expPanel.add(startTV);
	expPanel.add(latticeB);
	expPanel.add(filenameV);

	expPanel.add(endTL);
	expPanel.add(updateL);
	expPanel.add(currentTL);
	expPanel.add(endTV);
	expPanel.add(updateB);
	expPanel.add(currentTV);

	expPanel.add(dTL);
	expPanel.add(eqSweepsL);
	expPanel.add(new JLabel());
	expPanel.add(dTV);
	expPanel.add(eqSweepsV);
	expPanel.add(goB);

	goB.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == goB) {

	    latticeSize = Integer.parseInt(sizeV.getText());
	    noOfSpins = latticeSize * latticeSize;
	    isingLattice.changeLatticeSize(latticeSize);
	    startTemp = Double.parseDouble(startTV.getText());
	    endTemp = Double.parseDouble(endTV.getText());
	    dT = Double.parseDouble(dTV.getText());
	    eqSweeps = Integer.parseInt(eqSweepsV.getText());
	    observableWindow = Integer.parseInt(windowV.getText());
	    energy = new int[observableWindow];

	    dynamicsType = algorithmB.getSelectedIndex();
	    orderType = updateB.getSelectedIndex();
	    isingLattice.latticeType = latticeB.getSelectedIndex();

	    filename = filenameV.getText();

	    runExperiment();
	}
    }

    public void runSweep(){
	if (dynamicsType == 0) {
	    if (orderType == 0) {
		isingLattice.runMetropolisSweep(); 
	    } else {
		for (int i = 0; i < noOfSpins; i++) {
		    x = (int) (Math.random() * latticeSize);
		    y = (int) (Math.random() * latticeSize);
		    
		    isingLattice.runMetropolis(x, y); 
		}
	    }
	} else if (dynamicsType == 1) {
	    isingLattice.runWolff();
	} else if (dynamicsType == 2) {
	    if (orderType == 0) {
		isingLattice.runKawasakiSweep();
	    } else {
		for (int i = 0; i < noOfSpins; i++) {
		    x = (int) (Math.random() * latticeSize);
		    y = (int) (Math.random() * latticeSize);
		    isingLattice.runKawasaki(x, y);
		}
	    }
	} else {
	    isingLattice.runSwendsenWang();
	}
    }

    public void runExperiment(){

	FileWriter log;
	BufferedWriter output = null;
	int iterationCounter = 0;

	isingLattice.resetSimulation();

	try {
	    log = new FileWriter(filename);
	    output = new BufferedWriter(log);
	} catch (IOException e){
	    System.out.println(e);
	}

  if (startTemp < endTemp){ 
	for(temp = startTemp; temp <= endTemp; temp+=dT) {

	    isingLattice.setTemp(temp);
	    currentTV.setText(""+temp+"");
	    iterationCounter = 0;
	    
	    for (int p = 0; p < eqSweeps; p++){

		runSweep();
	    }
	    for (int p = 0; p < observableWindow; p++){

		runSweep();
		energy[iterationCounter] = isingLattice.systemEnergy;
		iterationCounter++;
	    }
	    
	    isingLattice.calculateHeatCapacity(energy);

	    try {
		output.write(temp + " " + (2.0*(double)isingLattice.spinUp/noOfSpins - 1) + " " + isingLattice.eBar + " " + isingLattice.heatCapacity);
		output.newLine();
	    } catch (IOException e) {}
	}
  } else {
	for(temp = startTemp; temp >= endTemp; temp+=dT) { // dT will be negative when cooling

	    isingLattice.setTemp(temp);
	    currentTV.setText(""+temp+"");
	    iterationCounter = 0;
	    
	    for (int p = 0; p < eqSweeps; p++){

		runSweep();
	    }
	    for (int p = 0; p < observableWindow; p++){

		runSweep();
		energy[iterationCounter] = isingLattice.systemEnergy;
		iterationCounter++;
	    }
	    
	    isingLattice.calculateHeatCapacity(energy);

	    try {
		output.write(temp + " " + (2.0*(double)isingLattice.spinUp/noOfSpins - 1) + " " + isingLattice.eBar + " " + isingLattice.heatCapacity);
		output.newLine();
	    } catch (IOException e) {}
	  }
  	}
	try{
	    output.close();
	} catch (IOException e) {}
    }
}

public class IsingGUI implements ActionListener {

    // Simulation Variables
    int dynamicsType = 0, orderType = 0;
    int latticeSize = 100;
    int observableWindow = 100;
    int[] energy = new int[observableWindow];
    int iterationCounter = 0;
    double temp = 1.0;
    double heatCapacity = 0.0, eBar = 0.0;

    // GUI Update Variables
    boolean running = false;
    boolean sizeChanged = false;
    boolean reset = false;

    // Simulation Lattice
    Lattice isingLattice;

    // GUI Global Objects
    JPanel mainPanel, infoPanel, controlPanel;
    JLatticePanel latticePanel;
    JSlider tempSlider;
    JTextField tempField, sizeField;
    JLabel spinUpValue, spinDownValue, magValue, nrgValue, eBarValue, cVValue;
    JButton startButton, resetButton, stepButton, expButton;
    JComboBox dynamicsChoices, orderChoices, latticeChoices;


    public IsingGUI() {

	// Make the experimental lattice
	isingLattice = new Lattice(latticeSize, temp);

	//Create the display panels.
	latticePanel = new JLatticePanel();
	infoPanel = new JPanel();
	controlPanel = new JPanel();

	//Add various widgets to the sub panels.
	addWidgets();

	//Create the main panel to contain the three sub panels.
	mainPanel = new JPanel();
	mainPanel.setLayout(new BorderLayout());

	//Add the graphics and control panels to the main panel.
	mainPanel.add(infoPanel, BorderLayout.SOUTH);
	mainPanel.add(controlPanel, BorderLayout.EAST);
	mainPanel.add(latticePanel, BorderLayout.CENTER);

    }

    /*
     *  Set up the GUI Objects
     */
    private void addWidgets() {

	// Instansiate slider variables (slider thinks temp is 4x)
	final int minTemp = 0;
	final int maxTemp = 20;
	final int initialTemp = (int) temp * 4;

	int sizeSquared = isingLattice.size * isingLattice.size;

	String[] dynamics = {"Metropolis", "Wolff", "Kawasaki", "Swensen-Wang"};
	String[] order = {"Checkerboard", "Stohastic"};
	String[] lattice = {"Square", "Triangular"};

	JPanel controlBoxPanel = new JPanel(new GridLayout(13, 1));
	JPanel tempPanel = new JPanel(new BorderLayout());

	JLabel dynamicsLabel;
	JLabel orderLabel;
	JLabel latticeLabel;
	JLabel sizeLabel;
	JLabel tempLabel;
	JLabel blankLabel;
	JLabel spinUpLabel;
	JLabel spinDownLabel;
	JLabel magLabel;
	JLabel nrgLabel;
	JLabel eBarLabel;
	JLabel cVLabel;

	tempSlider = new JSlider(JSlider.VERTICAL, minTemp, maxTemp, initialTemp);
	tempSlider.setMajorTickSpacing(5);
	tempSlider.setMinorTickSpacing(1);
	tempSlider.setPaintTicks(true);

	dynamicsChoices = new JComboBox(dynamics);
	orderChoices = new JComboBox(order);
	latticeChoices = new JComboBox(lattice);

	startButton = new JButton("Start");
	resetButton = new JButton("Reset");
	stepButton = new JButton("Step");
	expButton = new JButton("Experiment");

	sizeField = new JTextField("" + isingLattice.size + "");
	tempField = new JTextField("" + temp + "");

	dynamicsLabel = new JLabel("Update Method:");
	orderLabel = new JLabel("Update Order:");
	latticeLabel = new JLabel("Lattice Type:");
	sizeLabel = new JLabel("Linear Size:");
	tempLabel = new JLabel("Temperature:");
	blankLabel = new JLabel("");
	spinUpLabel = new JLabel("Spin UP:");
	spinUpValue = new JLabel("" + isingLattice.spinUp + "");
	spinDownLabel = new JLabel("Spin DOWN:");
	spinDownValue = new JLabel("" + (sizeSquared - isingLattice.spinUp) + "");
	magLabel = new JLabel("Mag per Spin:");
	magValue = new JLabel("");
	nrgLabel = new JLabel("Energy per Spin:");
	nrgValue = new JLabel("" + ((double) isingLattice.systemEnergy / (double) sizeSquared) + "");
	eBarLabel = new JLabel("Mean Energy:");
	eBarValue = new JLabel("" + eBar + "");
	cVLabel = new JLabel("Heat Capacity:");
	cVValue = new JLabel("" + heatCapacity + "");

	dynamicsChoices.setSelectedIndex(dynamicsType);
	latticeChoices.setSelectedIndex(isingLattice.latticeType);

	controlBoxPanel.add(dynamicsLabel);
	controlBoxPanel.add(dynamicsChoices);
	controlBoxPanel.add(orderLabel);
	controlBoxPanel.add(orderChoices);
	controlBoxPanel.add(latticeLabel);
	controlBoxPanel.add(latticeChoices);
	controlBoxPanel.add(sizeLabel);
	controlBoxPanel.add(sizeField);
	controlBoxPanel.add(blankLabel);
	controlBoxPanel.add(startButton);
	controlBoxPanel.add(stepButton);
	controlBoxPanel.add(resetButton);
	controlBoxPanel.add(expButton);

	tempPanel.add(tempLabel, BorderLayout.NORTH);
	tempPanel.add(tempSlider, BorderLayout.CENTER);
	tempPanel.add(tempField, BorderLayout.SOUTH);

	controlPanel.add(controlBoxPanel, BorderLayout.CENTER);
	controlPanel.add(tempPanel, BorderLayout.EAST);

	infoPanel.setLayout(new GridLayout(2, 6));

	infoPanel.add(spinUpLabel);
	infoPanel.add(spinUpValue);
	infoPanel.add(nrgLabel);
	infoPanel.add(nrgValue);
	infoPanel.add(eBarLabel);
	infoPanel.add(cVLabel);
	infoPanel.add(spinDownLabel);
	infoPanel.add(spinDownValue);
	infoPanel.add(magLabel);
	infoPanel.add(magValue);
	infoPanel.add(eBarValue);
	infoPanel.add(cVValue);

	dynamicsChoices.addActionListener(this);
	orderChoices.addActionListener(this);
	latticeChoices.addActionListener(this);
	startButton.addActionListener(this);
	resetButton.addActionListener(this);
	stepButton.addActionListener(this);
	expButton.addActionListener(this);
	sizeField.addActionListener(this);
	tempField.addActionListener(this);
	tempSlider.addChangeListener(new ScrollBarListener());

    }

    class ScrollBarListener implements ChangeListener {
	public void stateChanged(ChangeEvent e) {
	    isingLattice.setTemp((double) tempSlider.getValue() / 4.0);
	    tempField.setText("" + isingLattice.getTemp() + "");
	}
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == startButton) {
	    if (running) {
		startButton.setText("Start");
		running = false;
	    } else {
		startButton.setText("Pause");
		running = true;
	    }

	} else if (e.getSource() == sizeField) {
	    sizeChanged = true;
	    latticeSize = Integer.parseInt(sizeField.getText());

	} else if (e.getSource() == resetButton) {
	    reset = true;
			
	} else if (e.getSource() == expButton) {
	    running = false;
	    expFrame lab = new expFrame();
	    lab.setVisible(true);

	} else if (e.getSource() == dynamicsChoices) {
	    dynamicsType = dynamicsChoices.getSelectedIndex();
	    isingLattice.calculateTotalSystemEnergy();

	} else if (e.getSource() == orderChoices) {
	    orderType = orderChoices.getSelectedIndex();

	} else if (e.getSource() == latticeChoices) {
	    isingLattice.latticeType = latticeChoices.getSelectedIndex();
	    isingLattice.calculateTotalSystemEnergy();

	} else if ((e.getSource() == stepButton) && (running == false)) {
	    goStep();
	} else if (e.getSource() == tempField) {
	    temp = Double.parseDouble(tempField.getText());
	    int sliderTemp = (int) (temp * 4.0);
	    if (sliderTemp > 20) {
		sliderTemp = 20;
	    }
	    if (sliderTemp < 0) {
		sliderTemp = 0;
	    }
	    tempSlider.setValue(sliderTemp);
	}
    }

    public void goStep() {
	if (dynamicsType == 0) {
	    if (orderType == 0) {
		isingLattice.runMetropolisSweep();
		latticePanel.repaint();
	    } else {
		int noOfSpins = latticeSize * latticeSize;
		int x;
		int y;
		for (int i = 0; i < noOfSpins; i++) {
		    x = (int) (Math.random() * latticeSize);
		    y = (int) (Math.random() * latticeSize);
		    if (isingLattice.runMetropolis(x, y)) {
			latticePanel.paintSpin(x, y);
		    }
		}
	    }
	} else if (dynamicsType == 1) {
	    latticePanel.paintCluster(isingLattice.runWolff());
	} else if (dynamicsType == 2) {
	    if (orderType == 0) {
		isingLattice.runKawasakiSweep();
		latticePanel.repaint();
	    } else {
		int noOfSpins = latticeSize * latticeSize;
		int x;
		int y;
		int[] pair;
		for (int i = 0; i < noOfSpins; i++) {
		    x = (int) (Math.random() * latticeSize);
		    y = (int) (Math.random() * latticeSize);
		    pair = isingLattice.runKawasaki(x, y);
		    if (pair[2] != 0) {
			latticePanel.paintPair(x, y, pair);
		    }
		}
	    }
	} else {
	    isingLattice.runSwendsenWang();
	    latticePanel.repaint();
	}
	energy[iterationCounter] = isingLattice.systemEnergy;
	iterationCounter++;
	if (iterationCounter > (observableWindow - 1)) {
	    iterationCounter = 0;
	    isingLattice.calculateHeatCapacity(energy);
	}
	updateInfo();

    }

    public void go() {

	int[] pair;
	int x;
	int y;
	int noOfSpins;

	while (true) {
	    if (reset) {
		isingLattice.resetSimulation();
		reset = false;
		mainPanel.repaint();
	    }
	    if (sizeChanged) {
		isingLattice.changeLatticeSize(latticeSize);
		sizeChanged = false;
		mainPanel.repaint();
	    }
	    if (running) {
		if (dynamicsType == 0) {
		    if (orderType == 0) {
			for (int k = 0; k < 2; k++) {
			    for (int j = 0; j < isingLattice.size; j++) {
				for (int i = (j + k) % 2; i < isingLattice.size; i += 2) {
				    if (isingLattice.runMetropolis(i, j)) {
					latticePanel.paintSpin(i, j);
				    }
				}
			    }
			}
		    } else {
			noOfSpins = latticeSize * latticeSize;
			for (int i = 0; i < noOfSpins; i++) {
			    x = (int) (Math.random() * latticeSize);
			    y = (int) (Math.random() * latticeSize);
			    if (isingLattice.runMetropolis(x, y)) {
				latticePanel.paintSpin(x, y);
			    }
			}
		    }
		} else if (dynamicsType == 1) {
		    latticePanel.paintCluster(isingLattice.runWolff());
		} else if (dynamicsType == 2) {
		    if (orderType == 0) {
			for (int k = 0; k < 2; k++) {
			    for (int j = 0; j < isingLattice.size; j++) {
				for (int i = (j + k) % 2; i < isingLattice.size; i += 2) {
				    pair = isingLattice.runKawasaki(i, j);
				    if (pair[2] != 0) {
					latticePanel.paintPair(i, j, pair);
				    }
				}
			    }
			}
		    } else {
			noOfSpins = latticeSize * latticeSize;
			for (int i = 0; i < noOfSpins; i++) {
			    x = (int) (Math.random() * latticeSize);
			    y = (int) (Math.random() * latticeSize);
			    pair = isingLattice.runKawasaki(x, y);
			    if (pair[2] != 0) {
				latticePanel.paintPair(x, y, pair);
			    }
			}
		    }
		} else {
		    isingLattice.runSwendsenWang();
		    latticePanel.repaint();
		}
		energy[iterationCounter] = isingLattice.systemEnergy;
		iterationCounter++;
		if (iterationCounter > (observableWindow - 1)) {
		    iterationCounter = 0;
		    isingLattice.calculateHeatCapacity(energy);
		}
		updateInfo();

	    } else {
		try {
		    Thread.sleep(50);
		} catch (InterruptedException e) {}
	    }
	}
    }

    private void updateInfo() {
	int noOfSpins = latticeSize * latticeSize;
	int spinUp = isingLattice.spinUp;
	int spinDown = noOfSpins - spinUp;
	double energyPerSpin = (double) isingLattice.systemEnergy / noOfSpins;
	double magPerSpin = (double) (spinUp - spinDown) / noOfSpins;

	spinUpValue.setText("" + spinUp + "");
	spinDownValue.setText("" + spinDown + "");
	nrgValue.setText("" + energyPerSpin + "");
	magValue.setText("" + magPerSpin + "");

	if (iterationCounter == 0) {
	    cVValue.setText("" + isingLattice.heatCapacity + "");
	}
	eBarValue.setText("" + isingLattice.eBar + "");
    }


    class JLatticePanel extends JPanel {
	Graphics h;
	Color p;
	int size, width, height;

	public JLatticePanel() { }

	public void paintSpin(int x, int y) {
	    h = getGraphics();
	    size = isingLattice.size;
	    height = this.getHeight() / size;
	    width = this.getWidth() / size;

	    p = (isingLattice.latticeArray[x][y] == 1) ? Color.red : Color.blue;
	    h.setColor(p);
	    h.fillRect(x * width, y * height, width, height);
	}

	public void paintPair(int x, int y, int[] pair) {
	    h = getGraphics();
	    size = isingLattice.size;
	    height = this.getHeight() / size;
	    width = this.getWidth() / size;

	    p = (pair[2] == 1) ? Color.red : Color.blue;
	    h.setColor(p);
	    h.fillRect(pair[0] * width, pair[1] * height, width, height);

	    p = (pair[2] == -1) ? Color.red : Color.blue;
	    h.setColor(p);
	    h.fillRect(x * width, y * height, width, height);
	}

	public void paintCluster(int state) {
	    h = getGraphics();
	    size = isingLattice.size;
	    height = this.getHeight() / size;
	    width = this.getWidth() / size;
	    p = (state == 1) ? Color.red : Color.blue;
	    h.setColor(p);

	    for (int i = 0; i < size; i++) {
		for (int j = 0; j < size; j++) {
		    if (isingLattice.cluster[i][j] == true) {
			h.fillRect(i * width, j * height, width, height);
		    }
		}
	    }
	}

	public void paint(Graphics g) {
	    size = isingLattice.size;
	    height = this.getHeight() / size;
	    width = this.getWidth() / size;

	    for (int i = 0; i < size; i++) {
		for (int j = 0; j < size; j++) {
		    p = (isingLattice.latticeArray[i][j] == 1) ? Color.red : Color.blue;
		    g.setColor(p);
		    g.fillRect(i * width, j * height, width, height);
		}
	    }
	}
    }

    public static void main(String[] args) {

	//Create a new instance of the Ising GUI
	IsingGUI cp4 = new IsingGUI();

	//Create and set up the window.
	JFrame isingFrame = new JFrame("The Ising Model");
	isingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	isingFrame.setContentPane(cp4.mainPanel);

	//Display the window.
	isingFrame.pack();
	isingFrame.setVisible(true);
	cp4.go();
    }
}

	
