//  This solution should be used as a Model for checkpoint 5
//  Consider how it could be used to do both two and three particle
//  problems.
/**
 * In addition to comprehending and  commenting, you should carefully investigate the behaviour of the orbits by using the code (no code writing is needed to do this).  In particular,  investigate the stability of each of these in two dimensions, by playing with the starting conditions and seeing if one particle escapes at large time.  Try to understand what causes the particle to become unstable, in terms of energy conservation.
 *
 * @author     richie
 * @created    24 January 2005
 */

class Particle {
	double mass, charge, time;
	double speed, ke;
	Vector3 pos, vel, acc;
	String print;


	/**
	 *Constructor for the Particle object
	 *
	 * @param  p  Description of the Parameter
	 * @param  v  Description of the Parameter
	 * @param  a  Description of the Parameter
	 * @param  m  Description of the Parameter
	 * @param  c  Description of the Parameter
	 * @param  t  Description of the Parameter
	 */
	public Particle(Vector3 p, Vector3 v, Vector3 a, double m, double c, double t) {
		this.pos = new Vector3(0.0, 0.0, 0.0);
		this.vel = new Vector3(0.0, 0.0, 0.0);
		this.acc = new Vector3(0.0, 0.0, 0.0);
		this.pos = p;
		this.vel = v;
		this.acc = a;
		mass = m;
		charge = c;
		time = t;
	}


	/**
	 *Constructor for the Particle object
	 */
	public Particle() {
		this.pos = new Vector3(0.0, 0.0, 0.0);
		this.vel = new Vector3(0.0, 0.0, 0.0);
		this.acc = new Vector3(0.0, 0.0, 0.0);
		mass = 1.0;
		charge = 0.0;
		time = 0.0;
	}


	/**
	 *  Sets the pos attribute of the Particle object
	 *
	 * @param  p  The new pos value
	 */
	void setPos(Vector3 p) {
		this.pos = p;
	}


	/**
	 *  Sets the vel attribute of the Particle object
	 *
	 * @param  v  The new vel value
	 */
	void setVel(Vector3 v) {
		this.vel = v;
	}


	/**
	 *  Sets the acc attribute of the Particle object
	 *
	 * @param  a  The new acc value
	 */
	void setAcc(Vector3 a) {
		this.acc = a;
	}


	/**
	 *  Gets the pos attribute of the Particle object
	 *
	 * @return    The pos value
	 */
	Vector3 getPos() {
		return this.pos;
	}


	/**
	 *  Gets the vel attribute of the Particle object
	 *
	 * @return    The vel value
	 */
	Vector3 getVel() {
		return this.vel;
	}


	/**
	 *  Gets the acc attribute of the Particle object
	 *
	 * @return    The acc value
	 */
	Vector3 getAcc() {
		return this.acc;
	}


	/**
	 *  Sets the mass attribute of the Particle object
	 *
	 * @param  mass  The new mass value
	 */
	void setMass(double mass) {
		this.mass = mass;
	}


	/**
	 *  Sets the time attribute of the Particle object
	 *
	 * @param  time  The new time value
	 */
	void setTime(double time) {
		this.time = time;
	}


	/**
	 *  Gets the time attribute of the Particle object
	 *
	 * @return    The time value
	 */
	double getTime() {
		return this.time;
	}


	/**
	 *  Sets the speed attribute of the Particle object
	 */
	void setSpeed() {
		this.speed = Math.sqrt(Vector3.dot(vel, vel));
	}


	/**
	 *  Sets the kE attribute of the Particle object
	 */
	void setKE() {
		this.ke = mass * Vector3.dot(vel, vel) / 2.0;
	}


	/**
	 *  Gets the kE attribute of the Particle object
	 *
	 * @return    The kE value
	 */
	double getKE() {
		this.setKE();
		return this.ke;
	}


	/**
	 *  Gets the kEtot attribute of the Particle class
	 *
	 * @param  a  Description of the Parameter
	 * @return    The kEtot value
	 */
	public static double getKEtot(Particle a[]) {
		double kenergy = 0.0;
		for (int iatom = 0; iatom < a.length; iatom++) {
			kenergy = kenergy + a[iatom].getKE();
		}
		return kenergy;
	}


	/**
	 *  Description of the Method
	 */
	void printString() {
		this.setKE();
		this.setSpeed();
		System.out.println("KE  " + this.ke);
		System.out.println("Speed  " + this.speed);
		System.out.println("Mass  " + this.mass + " Charge  " + this.charge);
		System.out.println("Time  " + this.time);
		this.pos.printString();
		this.vel.printString();
		this.acc.printString();
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public String toString() {
		this.setKE();
		this.setSpeed();
		String myString = "[" + this.pos + "\n " + this.vel + "]";
		return (myString);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  a         Description of the Parameter
	 * @param  b         Description of the Parameter
	 * @param  periodic  Description of the Parameter
	 * @param  L         Description of the Parameter
	 * @return           Description of the Return Value
	 */



	/**
	 *  Description of the Method
	 *
	 * @param  a         Description of the Parameter
	 * @param  b         Description of the Parameter
	 * @param  periodic  Description of the Parameter
	 * @param  L         Description of the Parameter
	 * @return           Description of the Return Value
	 */



	/**
	 *  Description of the Method
	 *
	 * @param  part   Description of the Parameter
	 * @param  force  Description of the Parameter
	 * @param  dt     Description of the Parameter
	 * @return        Description of the Return Value
	 */



	/**
	 *  Description of the Method
	 *
	 * @param  x0  Description of the Parameter
	 * @param  y0  Description of the Parameter
	 * @param  z0  Description of the Parameter
	 */
	void periodic(double x0, double y0, double z0) {
		if (this.pos.x <= 0) {
			this.pos.x = this.pos.x + x0;
		}
		if (this.pos.y <= 0) {
			this.pos.y = this.pos.y + y0;
		}
		if (this.pos.z <= 0) {
			this.pos.z = this.pos.z + z0;
		}
		if (this.pos.x >= x0) {
			this.pos.x = this.pos.x - x0;
		}
		if (this.pos.y >= y0) {
			this.pos.y = this.pos.y - y0;
		}
		if (this.pos.z >= z0) {
			this.pos.z = this.pos.z - z0;
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  x0  Description of the Parameter
	 * @param  y0  Description of the Parameter
	 * @param  z0  Description of the Parameter
	 */
	void wall(double x0, double y0, double z0) {

		if (this.pos.x < 0) {
			if (this.vel.x < 0) {
				this.vel.x = -this.vel.x;
			}
		}
		if (this.pos.y < 0) {
			if (this.vel.y < 0) {
				this.vel.y = -this.vel.y;
			}
		}
		if (this.pos.z < 0) {
			if (this.vel.z < 0) {
				this.vel.z = -this.vel.z;
			}
		}
		if (this.pos.x > x0) {
			if (this.vel.x > 0) {
				this.vel.x = -this.vel.x;
			}
		}
		if (this.pos.y > y0) {
			if (this.vel.y > 0) {
				this.vel.y = -this.vel.y;
			}
		}
		if (this.pos.z > z0) {
			if (this.vel.z > 0) {
				this.vel.z = -this.vel.z;
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  thisa  Description of the Parameter
	 */
	static void zeroCoM(Particle thisa[]) {
		double momx = 0.0;
		double momy = 0.0;
		double momz = 0.0;
		double m = 0.0;
		for (int iatom = 0; iatom < thisa.length - 1; iatom++) {
			momx = momx + thisa[iatom].mass * thisa[iatom].vel.x;
			momy = momy + thisa[iatom].mass * thisa[iatom].vel.y;
			momz = momz + thisa[iatom].mass * thisa[iatom].vel.z;
			m = m + thisa[iatom].mass;
		}
		for (int iatom = 0; iatom < thisa.length - 1; iatom++) {
			thisa[iatom].vel.x = thisa[iatom].vel.x - momx / m;
			thisa[iatom].vel.y = thisa[iatom].vel.y - momy / m;
			thisa[iatom].vel.z = thisa[iatom].vel.z - momz / m;
		}
	}
}





