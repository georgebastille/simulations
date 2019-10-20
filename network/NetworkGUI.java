import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class NetworkGUI implements ActionListener {

    JPanel mainPanel, controlPanel, tempPanel;
    JNetworkPanel display;
    JButton minimise, findCluster, reset, histogram, twodgraph, startPause;
    JComboBox typeBox;
    JTextField probField, sizeField;
    JSlider tempSlider;
    JLabel tempLabel, probLabel;
    Network network;
    Boolean running = false;
    int N = 144, L, type = 1;
    int initialTemp = 1, sliderScale = 4;
    int condenseIts = 1000;
    int maxTemp = 100;
    double p = 0.01, K = 2.0, temp = (double)initialTemp;
	
    public NetworkGUI() {
	    
	network = new Network(N, p, K, type);
	this.L = network.L;
	mainPanel = new JPanel();
	controlPanel = new JPanel();
	tempPanel = new JPanel();
	display = new JNetworkPanel();

	addWidgets();

        mainPanel.setLayout(new BorderLayout());
	mainPanel.add(display, BorderLayout.CENTER);
	mainPanel.add(controlPanel, BorderLayout.SOUTH);
	mainPanel.add(tempPanel, BorderLayout.EAST);

    }
	
    private void addWidgets() {

	minimise = new JButton("Jiggle");
	findCluster = new JButton("Find Cluster");
	reset = new JButton("Reset");
	histogram = new JButton("Histogram");
	twodgraph = new JButton("2D Graph");
	startPause = new JButton("Condense");

	String[] types = {"Random", "Evolution", "Popularity"};
	typeBox = new JComboBox(types);

	sizeField = new JTextField(""+N+"");
	probField = new JTextField(""+p+"");

	probLabel = new JLabel("Link Probability:");
	
	controlPanel.setLayout(new GridLayout(2,5));
	controlPanel.add(new JLabel("Number of Nodes:"));
	controlPanel.add(sizeField);
	controlPanel.add(typeBox);
	controlPanel.add(reset);
	controlPanel.add(histogram);
	controlPanel.add(probLabel);
	controlPanel.add(probField);
	controlPanel.add(minimise);
	controlPanel.add(findCluster);
	controlPanel.add(twodgraph);
	
	tempSlider = new JSlider(JSlider.VERTICAL, 0, maxTemp*sliderScale, initialTemp*sliderScale);
	tempSlider.setMajorTickSpacing(2*sliderScale);
	tempSlider.setMinorTickSpacing(1*sliderScale);
	tempSlider.setPaintTicks(true);
	
        tempPanel.setLayout(new BorderLayout());
	tempLabel = new JLabel("Temperature:", JLabel.CENTER);
	tempPanel.add(tempLabel, BorderLayout.NORTH);
	tempPanel.add(tempSlider, BorderLayout.CENTER);
	tempPanel.add(startPause, BorderLayout.SOUTH);

	minimise.addActionListener(this);
	findCluster.addActionListener(this);
	reset.addActionListener(this);
	startPause.addActionListener(this);
	typeBox.addActionListener(this);
	sizeField.addActionListener(this);
	probField.addActionListener(this);
	twodgraph.addActionListener(this);
	histogram.addActionListener(this);
	tempSlider.addChangeListener(new ScrollBarListener());
    }

    class ScrollBarListener implements ChangeListener {
	public void stateChanged(ChangeEvent e) {
	    temp = (double)tempSlider.getValue() / sliderScale;
	    tempLabel.setText(""+temp+"");
	    
	}
    }

    public void actionPerformed(ActionEvent e) {

	if (e.getSource() == minimise) {
	    network.minimise();
	    display.repaint();
	} 
	else if (e.getSource() == reset) {
	    network.reset(type);
	    display.repaint();
	} 
	else if (e.getSource() == findCluster) {
	    network.findCluster();
	    display.repaint();
	} 
	else if (e.getSource() == typeBox) {
	    type = (typeBox.getSelectedIndex() + 1);
	    network.reset(type);
	    if (type == 1) {
		probLabel.setText("Link Probability:");
		probField.setText(""+p+"");
	    } else {
		probLabel.setText("Avg No of Links:");
		probField.setText(""+K+"");
	    }	    
	    display.repaint();
	} 
	else if (e.getSource() == sizeField) {
	    N = Integer.parseInt(sizeField.getText());
	    network.resize(N);
	    L = (int)Math.ceil(Math.sqrt(N));
	    network.reset(type);
	    display.repaint();
	}  
	else if (e.getSource() == probField) {
	    if (type == 1) {
	    	network.p = p = Double.parseDouble(probField.getText());	
	    } else {
	    	network.K = K = Double.parseDouble(probField.getText());
	    }
	    network.reset(type);
	    display.repaint();
	} 
	else if (e.getSource() == startPause) {
		if (running) {
			running = false;
		} else {
			running = true;
			condense();
		}
	} 
	else if (e.getSource() == twodgraph) {
		TwoDGraph graph = new TwoDGraph(type);
		graph.setSize(400,400);
		graph.setVisible(true);
	} 
	else if (e.getSource() == histogram) {
		Histogram hist = new Histogram(network);
		hist.setSize(400, 400);
		hist.setVisible(true);
	}
    }

    public void condense() {
	    /* Pick a random Node and a random link from that node
	     * Move the link to a new node with prob exp((Lj-Li)/T)
	     * where Ln represents no of links the node had and T is
	     * the Network 'Temperature'
	     */

	    int from, to, iL, jL, link, end;
	    int iterator = 0;
	    double swapProb;

	    while (running) {
		    do {
		       from = (int)(Math.random() * N);
		       iL = network.nodes[from].noLinks();
		       iterator++;
		       if (iterator > condenseIts*N){
		          running = false;
		          display.repaint();
		          return;
		       }

		    } while (iL == 0);
		    
		    do {
		       to = (int)(Math.random() * N);
		       link = (int)(Math.random() * iL);
		       jL = network.nodes[to].noLinks();
		       end = network.nodes[from].links.get(link);
		       iterator++;
		       if (iterator > condenseIts*N){
			   running = false;
			   display.repaint();
			   return;
		       }
		       // 3 conditions, don't swap with yourself, don't swap with the end point of the link
		       // and don't swap with a node that already has a link to the endpoint (findLink() returns
		       // -1 if the node does not have a link to the ID given)
		    } while ((to == from) || (to == end) || ((jL != 0) && (network.nodes[end].findLink(to) != -1)));

		    swapProb = Math.exp((double)(jL-iL)/temp);
		    
		    if (Math.random() < swapProb) {
		        network.swapLink(from, to, link);
		    }   
		}
	    }
    		    

    class JNetworkPanel extends JPanel {

	int w, h, nodeX, nodeY, noLinks;
	Node start, end;
	Graphics v;
	    
	public JNetworkPanel() { 
	    setBackground(Color.BLACK);
	}

	public void paint(Graphics g) {
		
	    w = getWidth() / L;
	    h = getHeight() / L;
	    nodeX = (int)(0.5 * w);
	    nodeY = (int)(0.5 * h);

	    g.setColor(Color.BLACK);
	    g.fillRect(0,0,getWidth(),getHeight());
		
	    // Draw the links
	    g.setColor(Color.GREEN);

	    for (int i = 0; i < N; i++){
		start = network.nodes[i];
		noLinks = start.noLinks();
		for (int j = 0; j < noLinks; j++) {
		    end  = network.nodes[start.links.get(j)];
		    g.drawLine(start.x*w + nodeX/2, start.y*h + nodeY/2, end.x*w + nodeX/2, end.y*h + nodeY/2);
		}
	    }

	    // Draw the Nodes
	    for (int i = 0; i < N; i++){
		start = network.nodes[i];
		if (start.inBigCluster){
		    g.setColor(Color.BLUE);
		} else {
		    g.setColor(Color.RED);
		}

		g.fillOval(start.x*w, start.y*h, nodeX, nodeY);

		String IDtext = "" + start.ID + "";

		g.setColor(Color.YELLOW);
		g.drawString(IDtext, start.x*w + (int)(nodeX/4.0), start.y*h + (int)(nodeY/1.2));
	    }
			
	}		
    }
    

    public static void main(String[] args) {

	//Create a new instance of the NetworkGUI
	NetworkGUI cp5 = new NetworkGUI();

	//Create and set up the window.
	JFrame networkFrame = new JFrame("Network Theory Experiment");
	networkFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	networkFrame.setContentPane(cp5.mainPanel);

	//Display the window.
	//networkFrame.pack();
	networkFrame.setSize(600,600);
	networkFrame.setVisible(true);
    }
}

	
