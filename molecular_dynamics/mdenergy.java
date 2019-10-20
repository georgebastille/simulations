// Molecular Dynamics Simulation in a Lenard Jones Potential
// Much code modified or used as is from Graeme Ackland
// Richard Hanes
// 16/01/05

import java.io.*;
import java.awt.*;
import java.util.*;
import gov.noaa.pmel.sgt.cplab.*;

/**
 *  Description of the Class
 *
 * @author     richie
 * @created    24 January 2005
 */
class dynamicsSimulator {

	int N, type, bc;
	double spacing, T, dt, finalT, ke, pe;
	Ball[] balls;
	Vector3[] forces;
	Vector3 zeroVector = new Vector3(0.0, 0.0, 0.0);
	Vector3 forceij;
	SimpleGraph energy = new SimpleGraph("System Energy", "Time", "Energy");
	DataSet pedata = new DataSet();
	DataSet kedata = new DataSet();
	DataSet totdata = new DataSet();

	double boxSize = 10;
	Random r;


	/**
	 *  Description of the Method
	 *
	 * @param  N        Description of the Parameter
	 * @param  type     Description of the Parameter
	 * @param  spacing  Description of the Parameter
	 * @param  T        Description of the Parameter
	 * @param  dt       Description of the Parameter
	 * @param  finalT   Description of the Parameter
	 * @param  bc       Description of the Parameter
	 */
	public dynamicsSimulator(int N, int type, int bc, double spacing, double T, double dt, double finalT) {
		this.N = N;
		this.type = type;
		this.bc = bc;
		this.spacing = spacing;
		this.T = T;
		this.dt = dt;
		this.finalT = finalT;

		r = new Random();
		balls = initialiseParticles(N, spacing, type);
		forces = new Vector3[N];
		for (int i = 0; i < N; i++) {
			forces[i] = zeroVector;
		}
		switch (bc) {
			case 1:
				System.out.println("No Boundary");
				break;
			case 2:
				System.out.println("Elastic Boundary");
				break;
			case 3:
				System.out.println("Periodic Boundary");
				break;
		}
		System.out.println(N + " particles");

	}


	/**
	 *  Description of the Method
	 */
	void runSimulation() {

		ParticleFrame f = new ParticleFrame(balls, 0, 0, boxSize, boxSize);
		f.setVisible(true);
		int m = 0;
		Vector3 sep;

		for (double time = dt; time < finalT; time += dt) {

			pe = 0.0;

			for (int i = 0; i < N; i++) {
				for (int j = (i + 1); j < N; j++) {
					if (bc == 3) {
						sep = getSep(balls[i], balls[j], boxSize);
						forceij = ljforce(sep);
						pe += ljenergy(sep);

					} else {
						sep = Vector3.subtract(balls[i].pos, balls[j].pos);
						forceij = ljforce(sep);
						pe += ljenergy(sep);
					}
					forces[i] = Vector3.add(forces[i], forceij);
					forces[j] = Vector3.add(forces[j], forceij.multiply(-1.0));
				}
			}
			ke = Particle.getKEtot(balls);
			pedata.addPoint(time, pe);
			kedata.addPoint(time, ke);
			totdata.addPoint(time, (pe + ke));

			for (int i = 0; i < N; i++) {

				balls[i] = (Ball) vverlet(balls[i], forces[i], dt);
				forces[i] = new Vector3(0.0, 0.0, 0.0);

				if (bc == 3) {
					balls[i].periodic(boxSize, boxSize, boxSize);
				}
				if (bc == 2) {
					balls[i].wall(boxSize, boxSize, boxSize);
				}
			}

			if (m % 100 == 0) {
				for (int i = 0; i < N; i++) {
					balls[i].setSize(boxSize);
				}
				f.canv.repaint();
				System.out.println(time);

				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {}
			}
			m++;
		}
		pedata.setColor(Color.red);
		kedata.setColor(Color.green);
		totdata.setColor(Color.blue);
		energy.addData(pedata, "Potential");
		energy.addData(kedata, "Kinetic");
		energy.addData(totdata, "Total");

		energy.showGraph();
	}


	/**
	 *  Description of the Method
	 *
	 * @param  part   Description of the Parameter
	 * @param  force  Description of the Parameter
	 * @param  dt     Description of the Parameter
	 * @return        Description of the Return Value
	 */
	Particle vverlet(Particle part, Vector3 force, double dt) {
		part.acc = Vector3.multiply(force, 1.0 / part.mass);
		Vector3 vtemp = Vector3.add(part.vel, Vector3.multiply(part.acc, dt / 2.0));
		part.pos = Vector3.add(Vector3.add(part.pos,
				Vector3.multiply(vtemp, dt))
				, Vector3.multiply(part.acc, dt * dt / 2.0));
		part.vel = Vector3.add(vtemp, Vector3.multiply(part.acc, dt / 2.0));
		part.time = part.time + dt;
		return (part);
	}


	// Velcity Verlet method with a stepwise temperature change
	/**
	 *  Description of the Method
	 *
	 * @param  part   Description of the Parameter
	 * @param  force  Description of the Parameter
	 * @param  dt     Description of the Parameter
	 * @param  T      Description of the Parameter
	 * @return        Description of the Return Value
	 */
	Particle vverlet(Particle part, Vector3 force, double dt, double T) {
		part.acc = Vector3.multiply(force, 1.0 / part.mass);
		Vector3 vtemp = Vector3.add(part.vel, Vector3.multiply(part.acc, dt / 2.0));
		part.pos = Vector3.add(Vector3.add(part.pos,
				Vector3.multiply(vtemp, dt))
				, Vector3.multiply(part.acc, dt * dt / 2.0));
		part.vel = Vector3.add(vtemp.multiply(Math.sqrt(T / instantaneousTemperature())), Vector3.multiply(part.acc, dt / 2.0));
		part.time = part.time + dt;
		return (part);
	}


	/**
	 *  Gets the sep attribute of the dynamicsSimulator object
	 *
	 * @param  a  Description of the Parameter
	 * @param  b  Description of the Parameter
	 * @param  L  Description of the Parameter
	 * @return    The sep value
	 */
	Vector3 getSep(Particle a, Particle b, double L) {
		Vector3 sep = Vector3.subtract(a.pos, b.pos);
		if (Math.abs(sep.x) > (0.5 * L)) {
			if (sep.x > 0) {
				sep.x -= L;
			} else {
				sep.x += L;
			}
		}
		if (Math.abs(sep.y) > (0.5 * L)) {
			if (sep.y > 0) {
				sep.y -= L;
			} else {
				sep.y += L;
			}
		}
		if (Math.abs(sep.z) > (0.5 * L)) {
			if (sep.z > 0) {
				sep.z -= L;
			} else {
				sep.z += L;
			}
		}
		return sep;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  sep  Description of the Parameter
	 * @return      Description of the Return Value
	 */
	Vector3 ljforce(Vector3 sep) {

		double len2 = Vector3.dot(sep, sep);
		double len6 = len2 * len2 * len2;
		double lj = -4 * (-12 / len6 / len6 / len2 + 6 / len6 / len2);
		return (Vector3.multiply(sep, lj));
	}


	/**
	 *  Description of the Method
	 *
	 * @param  sep  Description of the Parameter
	 * @return      Description of the Return Value
	 */
	double ljenergy(Vector3 sep) {
		double len2 = Vector3.dot(sep, sep);
		double len6 = len2 * len2 * len2;
		double lj = -4 * (-1 / len6 / len6 + 1 / len6);
		return (lj);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  noOfBalls  Description of the Parameter
	 * @param  spacing    Description of the Parameter
	 * @param  type       Description of the Parameter
	 * @return            Description of the Return Value
	 */
	Ball[] initialiseParticles(int noOfBalls, double spacing, int type) {

		// Initialise Particles on a regular 2D Lattice with small perturbations in position
		// if type = 1 -> square lattice, if type = 2 -> 2d FCC lattice, if type = 3 cubic lattice, if type = 4 3d fcc

		Ball[] balls = new Ball[noOfBalls];
		double mass = 1.0;
		double time = 0.0;
		double charge = 0.0;

		if (type == 1) {

			System.out.println("2D Square Lattice");

			int latticeSize = Math.round((float) Math.sqrt(noOfBalls));

			for (int i = 0; i < noOfBalls; i++) {

				double xpos = (i % latticeSize) * spacing;
				double ypos = (i / latticeSize) * spacing;
				//xpos += ((Math.random() * 2) - 1) * (spacing / 20);
				//ypos += ((Math.random() * 2) - 1) * (spacing / 20);

				Particle a = new Particle(new Vector3(xpos, ypos, 0.0), zeroVector, zeroVector, mass, charge, time);
				balls[i] = new Ball(a, 15, Color.red);
			}
			return balls;
		} else if (type == 2) {
			System.out.println("2D FCC Lattice");

			int latticeSize = Math.round((float) (0.5 * (1 + Math.sqrt(2 * noOfBalls - 1))));
			int faceSize = latticeSize - 1;

			for (int i = 0; i < noOfBalls; i++) {
				int j = latticeSize * latticeSize;
				if (i < j) {

					double xpos = (i % latticeSize) * spacing;
					double ypos = (i / latticeSize) * spacing;
					xpos += ((Math.random() * 2) - 1) * (spacing / 100);
					ypos += ((Math.random() * 2) - 1) * (spacing / 100);

					Particle a = new Particle(new Vector3(xpos, ypos, 0.0), zeroVector, zeroVector, mass, charge, time);
					balls[i] = new Ball(a, 15, Color.blue);
				} else {
					double xpos = ((i - j) % faceSize) * spacing + spacing / 2;
					double ypos = ((i - j) / faceSize) * spacing + spacing / 2;
					xpos += ((Math.random() * 2) - 1) * (spacing / 100);
					ypos += ((Math.random() * 2) - 1) * (spacing / 100);

					Particle a = new Particle(new Vector3(xpos, ypos, 0.0), zeroVector, zeroVector, mass, charge, time);
					balls[i] = new Ball(a, 15, Color.red);
				}
			}
			return balls;
		} else if (type == 3) {

			System.out.println("3D Cube Lattice");
			int M = (int) Math.pow(noOfBalls * spacing, 1.0 / 3.0);
			/*
			 *  int M = 1;
			 *  while (M * M * M < noOfBalls) {
			 *  ++M;
			 *  }
			 */
			//double a = latticeSize / M;
			double xpos;
			double ypos;
			double zpos;

			int n = 0;
			for (int x = 0; x < M; x++) {
				for (int y = 0; y < M; y++) {
					for (int z = 0; z < M; z++) {
						if (n < noOfBalls) {
							xpos = x * spacing;
							ypos = y * spacing;
							zpos = z * spacing;

							xpos += ((Math.random() * 2) - 1) * (spacing / 100);
							ypos += ((Math.random() * 2) - 1) * (spacing / 100);
							zpos += ((Math.random() * 2) - 1) * (spacing / 100);

							Particle c = new Particle(new Vector3(xpos, ypos, zpos), zeroVector, zeroVector, mass, charge, time);
							balls[n] = new Ball(c, 8, Color.red);

							++n;
						}
					}
				}
			}

			return balls;
		} else {

			System.out.println("3D FCC Lattice");

			double latticeSize = Math.pow(noOfBalls * spacing, 1.0 / 3.0);
			int M = 1;
			while (4 * M * M * M < noOfBalls) {
				++M;
			}

			double a = latticeSize / M;

			double[] xCell = {0.25, 0.75, 0.75, 0.25};
			double[] yCell = {0.25, 0.75, 0.25, 0.75};
			double[] zCell = {0.25, 0.25, 0.75, 0.75};
			double xpos;
			double ypos;
			double zpos;

			int n = 0;
			for (int x = 0; x < M; x++) {
				for (int y = 0; y < M; y++) {
					for (int z = 0; z < M; z++) {
						for (int k = 0; k < 4; k++) {
							// 4 atoms in one unit cell
							if (n < noOfBalls) {
								xpos = (x + xCell[k]) * a;
								ypos = (y + yCell[k]) * a;
								zpos = (z + zCell[k]) * a;

								xpos += ((Math.random() * 2) - 1) * (spacing / 100);
								ypos += ((Math.random() * 2) - 1) * (spacing / 100);
								zpos += ((Math.random() * 2) - 1) * (spacing / 100);

								Particle c = new Particle(new Vector3(xpos, ypos, zpos), zeroVector, zeroVector, mass, charge, time);
								balls[n] = new Ball(c, 8, Color.red);

								++n;
							}
						}
					}
				}
			}
			return balls;
		}
	}


	/**
	 *  Description of the Method
	 */
	void findBoundary() {
		double x = 0.0;
		double y = 0.0;
		double z = 0.0;
		for (int i = 0; i < N; i++) {
			if (balls[i].pos.x > x) {
				x = balls[i].pos.x;
			}
			if (balls[i].pos.y > y) {
				y = balls[i].pos.y;
			}
			if (balls[i].pos.z > z) {
				z = balls[i].pos.z;
			}
		}
		boxSize = Math.max(x, Math.max(y, z)) + spacing / 2;
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	double instantaneousTemperature() {
		double sum = 0;
		double velx;
		double vely;
		double velz;
		for (int i = 0; i < N; i++) {
			velx = balls[i].vel.x;
			vely = balls[i].vel.y;
			velz = balls[i].vel.z;

			sum += (velx * velx) + (vely * vely) + (velz * velz);
		}
		return sum / (3 * N);
	}
}

/**
 *  Description of the Class
 *
 * @author     richie
 * @created    24 January 2005
 */
public class mdenergy {


	/**
	 *  Description of the Method
	 *
	 * @param  args  Description of the Parameter
	 */
	public static void main(String args[]) {

		int experimentNo = 3;
		/*
		 *  1 - 3D Cubic No Boundary
		 *  2 - 3D Cubic Elastic Boundary
		 *  3 - 3D Cubic Periodic Boundary
		 *  4 - 3D FCC No Boundary
		 *  5 - 3D FCC Elastic Boundary
		 *  6 - 3D FCC Periodic Boundary
		 */
		 
		 int latticeType=1, bc=1, numberOfUnitCells=1, numberOfParticles=1;
		 double latticespacing=1;


		switch (experimentNo) {

			case 1:

				latticeType = 3;
				// 1 - 2d square, 2 - 2d FCC, 3 - 3d cubic, 4 - 3d FCC
				bc = 1;
				// 1 - no boundary, 2 - elastic boundary, 3 - periodic boundary
				 latticespacing = 1.2;

				 numberOfUnitCells = 3;
				 numberOfParticles = 10;
				switch (latticeType) {
					case 1:
						numberOfParticles = numberOfUnitCells * numberOfUnitCells;
						break;
					case 2:
						numberOfParticles = numberOfUnitCells * numberOfUnitCells * 2;
						break;
					case 3:
						numberOfParticles = numberOfUnitCells * numberOfUnitCells * numberOfUnitCells;
						break;
					case 4:
						numberOfParticles = (int) (4.0 * Math.pow(numberOfUnitCells, 3.0));
						break;
				}
				break;
			case 2:
				 latticeType = 3;
				// 1 - 2d square, 2 - 2d FCC, 3 - 3d cubic, 4 - 3d FCC
				 bc = 2;
				// 1 - no boundary, 2 - elastic boundary, 3 - periodic boundary
				 latticespacing = 1.0;

				 numberOfUnitCells = 3;
				 numberOfParticles = 10;
				switch (latticeType) {
					case 1:
						numberOfParticles = numberOfUnitCells * numberOfUnitCells;
						break;
					case 2:
						numberOfParticles = numberOfUnitCells * numberOfUnitCells * 2;
						break;
					case 3:
						numberOfParticles = numberOfUnitCells * numberOfUnitCells * numberOfUnitCells;
						break;
					case 4:
						numberOfParticles = (int) (4.0 * Math.pow(numberOfUnitCells, 3.0));
						break;
				}
				break;
			case 3:
				latticeType = 3;
				// 1 - 2d square, 2 - 2d FCC, 3 - 3d cubic, 4 - 3d FCC
				
				 bc = 3;
				// 1 - no boundary, 2 - elastic boundary, 3 - periodic boundary
				 latticespacing = 1.5;

				 numberOfUnitCells = 3;
				 numberOfParticles = 10;
				switch (latticeType) {
					case 1:
						numberOfParticles = numberOfUnitCells * numberOfUnitCells;
						break;
					case 2:
						numberOfParticles = numberOfUnitCells * numberOfUnitCells * 2;
						break;
					case 3:
						numberOfParticles = numberOfUnitCells * numberOfUnitCells * numberOfUnitCells;
						break;
					case 4:
						numberOfParticles = (int) (4.0 * Math.pow(numberOfUnitCells, 3.0));
						break;
				}
				break;
			case 4:
				 latticeType = 4;
				// 1 - 2d square, 2 - 2d FCC, 3 - 3d cubic, 4 - 3d FCC
				 bc = 1;
				// 1 - no boundary, 2 - elastic boundary, 3 - periodic boundary
				 latticespacing = 0.8;

				 numberOfUnitCells = 2;
				 numberOfParticles = 10;
				switch (latticeType) {
					case 1:
						numberOfParticles = numberOfUnitCells * numberOfUnitCells;
						break;
					case 2:
						numberOfParticles = numberOfUnitCells * numberOfUnitCells * 2;
						break;
					case 3:
						numberOfParticles = numberOfUnitCells * numberOfUnitCells * numberOfUnitCells;
						break;
					case 4:
						numberOfParticles = (int) (4.0 * Math.pow(numberOfUnitCells, 3.0));
						break;
				}
				break;
			case 5:
				 latticeType = 4;
				// 1 - 2d square, 2 - 2d FCC, 3 - 3d cubic, 4 - 3d FCC
				 bc = 2;
				// 1 - no boundary, 2 - elastic boundary, 3 - periodic boundary
				 latticespacing = 0.8;

				 numberOfUnitCells = 2;
				 numberOfParticles = 10;
				switch (latticeType) {
					case 1:
						numberOfParticles = numberOfUnitCells * numberOfUnitCells;
						break;
					case 2:
						numberOfParticles = numberOfUnitCells * numberOfUnitCells * 2;
						break;
					case 3:
						numberOfParticles = numberOfUnitCells * numberOfUnitCells * numberOfUnitCells;
						break;
					case 4:
						numberOfParticles = (int) (4.0 * Math.pow(numberOfUnitCells, 3.0));
						break;
				}
				break;
			case 6:
				 latticeType = 4;
				// 1 - 2d square, 2 - 2d FCC, 3 - 3d cubic, 4 - 3d FCC
				 bc = 3;
				// 1 - no boundary, 2 - elastic boundary, 3 - periodic boundary
				 latticespacing = 0.8;

				 numberOfUnitCells = 2;
				 numberOfParticles = 10;
				switch (latticeType) {
					case 1:
						numberOfParticles = numberOfUnitCells * numberOfUnitCells;
						break;
					case 2:
						numberOfParticles = numberOfUnitCells * numberOfUnitCells * 2;
						break;
					case 3:
						numberOfParticles = numberOfUnitCells * numberOfUnitCells * numberOfUnitCells;
						break;
					case 4:
						numberOfParticles = (int) (4.0 * Math.pow(numberOfUnitCells, 3.0));
						break;
				}
				break;
		}

		double finalTime = 1.0;
		double dt = 0.0001;
		double T = 0.0;

		dynamicsSimulator cp1 = new dynamicsSimulator(numberOfParticles, latticeType, bc, latticespacing, T, dt, finalTime);
		cp1.findBoundary();
		//cp1.setTemperature(T);
		cp1.runSimulation();
	}
}

