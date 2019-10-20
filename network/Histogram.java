import java.io.*;
import java.awt.*;
import javax.swing.*;

public class Histogram extends JFrame{

	GraphPanel main;
	LogPanel log;
	LogLogPanel loglog;
	JPanel histC, logC, loglogC; 
	XAxis xaxis;
	YAxis yaxis;
	Network network;
	int[] results;
	int maxHeight = 0;
	int maxNoOfLinks = 0;
	int noOfLinks;
	int N;

	public Histogram (Network network) {

		setTitle("No of Links vs Frequency");
		setLayout(new GridLayout(1, 2));
		calculateResults(network);

		addWidgets();

		add(histC);
		add(logC);
		add(loglogC);
	}

	private void addWidgets() {

		main = new GraphPanel();
		log = new LogPanel();
		loglog = new LogLogPanel();
		histC = new JPanel(new BorderLayout());
		logC = new JPanel(new BorderLayout());
		loglogC = new JPanel(new BorderLayout());
		
		// Axis code Here
		xaxis = new XAxis();
		//yaxis = new YAxis();
		histC.add(main, BorderLayout.CENTER);
		histC.add(xaxis, BorderLayout.SOUTH);
		logC.add(log, BorderLayout.CENTER);
		loglogC.add(loglog, BorderLayout.CENTER);
			
	}

	private void calculateResults(Network network) {
		
		this.network = network;
		this.N = network.N;
		results = new int[N];
		
		for (int i = 0; i < N; i++)
			results[i] = 0;
		
		for (int i = 0; i < N; i++) {
			
			noOfLinks = network.nodes[i].noLinks();
			results[noOfLinks]++;
			
			if (results[noOfLinks] > maxHeight)
				maxHeight = results[noOfLinks];

			if (noOfLinks > maxNoOfLinks)
				maxNoOfLinks = noOfLinks;
		}
	
	
	}

	class GraphPanel extends JPanel {

		public GraphPanel() {
		}

		public void paint(Graphics g) {
			int width = getWidth();
			int height = getHeight();
			int xBlock = width / (maxNoOfLinks+1);
			int yBlock = height / (maxHeight);
			int current;

			g.setColor(Color.RED);

			for (int i = 0; i < N; i++) { // This does not need to go up to N
				current = results[i];
				g.setColor(Color.RED);
				g.fillRect(i*xBlock, (height-yBlock*current), xBlock, yBlock*current);
				g.setColor(Color.BLACK);
				g.drawRect(i*xBlock, (height-yBlock*current-1), xBlock, yBlock*current);
				g.drawString((""+current+""), (int)(i*xBlock + 0.3*xBlock), ((height-yBlock*current)+12));
			}
		}
	}

	class LogPanel extends JPanel {

		double xMax, yMax;
		int x, y;

		public LogPanel() {

			xMax = maxNoOfLinks;
			yMax = Math.log((double)maxHeight);
		}
	
		public void paint(Graphics g) {
			int width = getWidth();
			int height = getHeight();
			int xBlock = (int)(width/xMax);
			int yBlock = (int)(height/yMax);
			int xSize = 10;
			int ySize = 10;

			g.drawLine(0, 0, 0, height);
			g.drawLine(0, height-1, width, height-1);

			for (int i = 0; i <= maxNoOfLinks; i++) {
				if (results[i] != 0) {
				x = (int)((i * xBlock)/1.05);
				y = (int)((height - Math.log((double)results[i])*yBlock)/1.05);
				g.fillOval(x, y, xSize, ySize);
				}
			}	
		}
	}

	class LogLogPanel extends JPanel {

		double xMax, yMax;
		int x, y;

		public LogLogPanel() {

			xMax = Math.log((double)maxNoOfLinks);
			yMax = Math.log((double)maxHeight);
		}
	
		public void paint(Graphics g) {
			int width = getWidth();
			int height = getHeight();
			int xBlock = (int)(width/xMax);
			int yBlock = (int)(height/yMax);
			int xSize = 10;
			int ySize = 10;

			g.drawLine(0, 0, 0, height);
			g.drawLine(0, height-1, width, height-1);

			for (int i = 0; i <= maxNoOfLinks; i++) { // Start at 1 to avoid -infinity
				if (results[i] != 0) {
				x = (int)((Math.log((double)i)*xBlock)/1.05);
				y = (int)((height - Math.log((double)results[i])*yBlock)/1.05);
				g.fillOval(x, y, xSize, ySize);
				}
			}	
		}
	}


	class XAxis extends JPanel {

		public XAxis() {
		}

		public void paint(Graphics g) {
			// x axis paint code here
			int width = getWidth();
			int height = getHeight();
			int xBlock = width / (maxNoOfLinks + 1);
			
			g.setColor(Color.BLACK);
			
			for (int i = 0; i < maxNoOfLinks+1; i++) {
				g.drawString((""+(i)+""), (int)(i*xBlock + 0.3*xBlock), (height));
			}

		}
	}

	class YAxis extends JPanel {

		public YAxis() {
		}

		public void paint(Graphics g) {
			// y axis code here
			int width = getWidth();
			int height = getHeight();
			
		}
	}	
}
