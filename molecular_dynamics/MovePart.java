import java.io.*;
import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.awt.image.*;

/**
 *  Description of the Class
 *
 * @author     richie
 * @created    24 January 2005
 */
class Ball extends Particle implements Cloneable{
	double diameter;
	boolean inplay;
	Color colour;
	String name;
	Vector3 zero = new Vector3(0.0, 0.0, 0.0);


	/**
	 *Constructor for the Ball object
	 *
	 * @param  partin  Description of the Parameter
	 * @param  d       Description of the Parameter
	 * @param  col     Description of the Parameter
	 */
	public Ball(Particle partin, double d, Color col) {
		this.diameter = d;
		this.pos = partin.pos;
		this.colour = col;
		this.vel = partin.vel;
		this.acc = partin.acc;
		this.mass = partin.mass;
		this.time = partin.time;
		this.charge = partin.charge;
		this.inplay = true;
	}


	/**
	 *Constructor for the Ball object
	 *
	 * @param  d    Description of the Parameter
	 * @param  col  Description of the Parameter
	 */
	public Ball(double d, Color col) {
		this.diameter = d;
		this.colour = col;
		this.pos = zero;
		this.vel = zero;
		this.acc = zero;
		this.mass = 1.0;
		this.time = 0.0;
		this.charge = 0.0;
		this.inplay = true;
	}


	/**
	 *Constructor for the Ball object
	 *
	 * @param  p  Description of the Parameter
	 */
	public Ball(Particle p) {
		this.pos = p.pos;
		if (p.charge > 0) {
			this.colour = Color.red;
		} else if (p.charge < 0) {
			this.colour = Color.green;
		} else {
			this.colour = Color.blue;
		}
		this.diameter = 20.0;
		this.vel = p.vel;
		this.acc = p.acc;
		this.charge = p.charge;
		this.mass = p.mass;
		this.time = p.time;
		this.inplay = true;
	}


	/**
	 *Constructor for the Ball object
	 */
	public Ball() {
		Particle p = new Particle();
		this.pos = p.pos;
		if (p.charge > 0) {
			this.colour = Color.red;
		} else if (p.charge < 0) {
			this.colour = Color.green;
		} else {
			this.colour = Color.blue;
		}
		this.diameter = 20.0;
		this.vel = p.vel;
		this.acc = p.acc;
		this.charge = p.charge;
		this.mass = p.mass;
		this.time = p.time;
		this.inplay = true;
	}
    public Object clone(){
	try{

	    Ball b = (Ball)super.clone();
	    return (Object)b;
	} catch (CloneNotSupportedException e){
	throw new Error("This should not happen");}
    }

	/**
	 *  Sets the size attribute of the Ball object
	 */
	void setSize(double boxsize) {

		this.diameter = ((this.pos.z/boxsize)*15 + 5);

		/*
		 *  this method uses the hsb color model
		 *  to set the colour depending on ratio
		 *  of i to n
		 *
		 *  hue h is specified here between 0.0 and 1.0
		 *  this gives a colour on the colour wheel
		 *  between red at 0.0
		 *  ( = 0 degrees on the wheel)
		 *  and red again at 1.0
		 *  ( = 360 degrees on the wheel)
		 *
		 *  saturation s is in the range 0.0 to 1.0
		 *  corresponding to the % difference
		 *  from neutral grey
		 *
		 *  brightness is ialso between 0.0 and 1.0
		 *  this gives the % illumination
		 *
		 */
		float h;
		float s;
		float b;
		h = 1.0f;
		s = 1.0f;
		b = (float)this.pos.z / (float)boxsize;
		this.colour = Color.getHSBColor(h, s, b);

	}


	/**
	 *  Description of the Method
	 *
	 * @param  partin  Description of the Parameter
	 */
	void resetParticle(Particle partin) {
		this.pos = partin.pos;
		this.vel = partin.vel;
		this.acc = partin.acc;
		this.mass = partin.mass;
		this.time = partin.time;
		this.charge = partin.charge;
	}

}

/**
 *  Description of the Class
 *
 * @author     richie
 * @created    24 January 2005
 */
class DoubleBuffer extends Canvas {

	//  constructor

	/**
	 *Constructor for the DoubleBuffer object
	 */
	public DoubleBuffer() {
		super();
	}


	// Overrride update method

	/**
	 *  Description of the Method
	 *
	 * @param  g  Description of the Parameter
	 */
	public void update(Graphics g) {
		paint(g);
	}


	//      class variables
	private int bufferWidth;
	private int bufferHeight;
	private Image bufferImage;
	private Graphics bufferGraphics;


	/**
	 *  Description of the Method
	 *
	 * @param  g  Description of the Parameter
	 */
	public void paint(Graphics g) {
		//      checks the buffersize with the current panelsize
		//      or initialises the image with the first paint
		if (bufferWidth != getSize().width || bufferHeight != getSize().height || bufferImage == null || bufferGraphics == null) {
			resetBuffer();
		}

		if (bufferGraphics != null) {
			//      this clears the offscreen image, not the onscreen one
			bufferGraphics.clearRect(0, 0, bufferWidth, bufferHeight);

			//      calls the paintbuffer method with the offscreen graphics as a param
			paintBuffer(bufferGraphics);

			//      finally paint the offscreen image onto the onscreen image
			g.drawImage(bufferImage, 0, 0, this);
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  g  Description of the Parameter
	 */
	public void paintBuffer(Graphics g) {
		//      in classes extended from this one, add something to paint here!
		//      always remember, g is the offscreen graphics
	}


	/**
	 *  Description of the Method
	 */
	private void resetBuffer() {
		// always keep track of the image size
		bufferWidth = getSize().width;
		bufferHeight = getSize().height;
		//      clean up the previous image
		if (bufferGraphics != null) {
			bufferGraphics.dispose();
			bufferGraphics = null;
		}
		if (bufferImage != null) {
			bufferImage.flush();
			bufferImage = null;
		}
		System.gc();

		//      create the new image with the size of the panel
		bufferImage = createImage(bufferWidth, bufferHeight);
		bufferGraphics = bufferImage.getGraphics();
	}
}
/*
 *  Ball Games without the Flicker: uses DoubleBuffering
 *  of the canvas
 */
/**
 *  Description of the Class
 *
 * @author     richie
 * @created    24 January 2005
 */
class ParticleFrame extends Frame implements ActionListener {

	// size of frame

	int X = 400;
	int Y = 400;
	Ball b[];
	XCanvas canv;
	double Xmin = 0.0;
	double Ymin = 0.0;
	double Xmax = 1.0;
	double Ymax = 1.0;
	static int offX = 100;
	static int offY = 100;

	//  Particle frame of balls

	/**
	 *Constructor for the ParticleFrame object
	 *
	 * @param  b   Description of the Parameter
	 * @param  x0  Description of the Parameter
	 * @param  y0  Description of the Parameter
	 * @param  x1  Description of the Parameter
	 * @param  y1  Description of the Parameter
	 */
	public ParticleFrame(Ball b[], double x0, double y0, double x1, double y1) {
		Xmin = x0;
		Ymin = y0;
		Xmax = x1;
		Ymax = y1;
		double aspect = (x0 - x1) / (y0 - y1);
		this.b = b;
		setTitle("Graeme Ackland's Ball Viewer");
		// set size of frame
		if (aspect > 1.0) {
			Y = (int) (X / aspect);
		} else {
			X = (int) (Y * aspect);
		}
		setSize(X, Y);
		setBackground(Color.blue);
		// and set its position on the screen
		setLocation(offX, offY);
		// update static variables offX and offY in case we create further viewers
		offX = offX + X / 5;
		offY = offY + Y / 5;
		canv = new XCanvas(this.getSize(), b, Xmin, Ymin, Xmax, Ymax);
		add(canv);
		// this code enables you to close the window
		addWindowListener(
			new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					e.getWindow().dispose();
					System.exit(0);
				}
			});
	}

	//  Seem to need this

	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void actionPerformed(ActionEvent e) { }
}
//  This is the canvas.  DoubleBuffer extends Canvas
/**
 *  Description of the Class
 *
 * @author     richie
 * @created    24 January 2005
 */
class XCanvas extends DoubleBuffer {


	static Ball b[];
	static int border = 50;
	static int w2;
	static int h2;
	static double xscale, yscale;
	static int xzero, yzero;

	//  Constructor

	/**
	 *Constructor for the XCanvas object
	 *
	 * @param  d   Description of the Parameter
	 * @param  bx  Description of the Parameter
	 * @param  x0  Description of the Parameter
	 * @param  y0  Description of the Parameter
	 * @param  x1  Description of the Parameter
	 * @param  y1  Description of the Parameter
	 */
	public XCanvas(Dimension d, Ball bx[], double x0, double y0, double x1, double y1) {
		b = bx;
		// Area size in pixels
		w2 = d.width - border * 2;
		h2 = d.height - border * 2;
		// Fit particles into screen, so that circle doesn't go OOB
		xscale = w2 / (x1 - x0);
		yscale = h2 / (y1 - y0);
		xzero = (int) (x0 * xscale) - border;
		yzero = (int) (y0 * yscale) - border;
		// set the background colour of the Canvas
		System.out.println(x0 + " " + y0 + " " + x1 + " " + y1);
		System.out.println(xscale + " " + yscale + " " + xzero + " " + yzero);
		setBackground(Color.white);
	}


// over-ride of paint() method of Canvas class happens in doubleBuffer
	// we need to paint graphics objects into paintBuffer

	/**
	 *  Description of the Method
	 *
	 * @param  g  Description of the Parameter
	 */
	public void paintBuffer(Graphics g) {
		try {
			// Draw a circle on the screen
			for (int ib = 0; ib < b.length; ib++) {
				int w = (int) b[ib].diameter;
				g.setColor(b[ib].colour);
				int cx = (int) (b[ib].pos.x * xscale - xzero);
				int cy = (int) (b[ib].pos.y * yscale - yzero);
				g.fillOval(cx - w / 2, cy - w / 2, w, w);
			}
		} catch (Exception e) {
			System.out.println("Ball off screen");
		}
	}
}

