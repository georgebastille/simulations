import java.io.*;
import java.awt.*;
import javax.swing.*;

public class TwoDGraph extends JFrame {
		
	int type;
	int[][] results;
	int n;
	double p, k;
	Network network;
	
	int nMin = 5;
	int nMax = 125;
	int nStep = 5;
	int nResults = (nMax-nMin) / nStep;
	
	double pMin = 0.001;
	double pMax = 0.05;
	double pStep = 0.002;
	int pResults = (int)((pMax-pMin) / pStep);
	
	double kMin = 0.0;
	double kMax = 2.4;
	double kStep = 0.1;
	int kResults = (int)((kMax-kMin) / kStep);
	
	public TwoDGraph(int type){
		
		this.type = type;
		calculateResults();
	}

	public void calculateResults() {
		
		if (type == 3) {

			kMax = 5.0;
			kStep = 0.2;
			kResults = (int)((kMax-kMin) / kStep);
		}
			
		if (type == 1){
			setTitle("2d Graph: N vs p - Cluster Size = Colour");
			results = new int[nResults][pResults];
			for (int i = 0; i < nResults; i++) {
				n = i*nStep + nMin;
				network = new Network(n, pMin, kMin, type);
				
				for (int j = 0; j < pResults; j++){
					p = j*pStep + pMin;
					network.p = p;
					network.reset(type);
					network.findCluster();
					results[i][j]=network.largestCluster;
				}
			}
			
		} else {
			setTitle("2d Graph: N vs K - Cluster Size = Colour");
			results = new int[nResults][kResults];
			for (int i = 0; i < nResults; i++) {
				n = i*nStep + nMin;
				network = new Network(n, pMin, kMin, type);
				
				for (int j = 0; j < kResults; j++){
					k = j*kStep + kMin;
					network.K = k;
					network.reset(type);
					network.findCluster();
					results[i][j]=network.largestCluster;
				}
			}
		}
	}

	public void paint(Graphics g) {
		int xSteps = results.length;
		int ySteps = results[0].length;
		int width = getWidth();
		int height = getHeight();
		int xBoxSize = width / xSteps;
		int yBoxSize = height / ySteps;

		int n;

		for (int i = 0; i < xSteps; i++) {
			n = i*nStep + nMin;
			for (int j = 0; j < ySteps; j++) {
				g.setColor(colourMap(n, results[i][j]));
				g.fillRect(i*xBoxSize, j*yBoxSize, xBoxSize, yBoxSize);
			}
		}
	}

	private Color colourMap(int n, int result) {
		float h, s, b;
		h = 0.0f;
		s = 1.0f;
		b = (float) result / n;

		return Color.getHSBColor(h,s,b);
		
	}
}
