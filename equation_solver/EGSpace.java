class EGSpace {

	Vector3[][][] vField;
	Vector3[][][] aField;
	Vector3[][][] jField;
	Vector3[][][] bField;
	double[][][] sField;
	double[][][] rhoField;
	double rhoMax = 0.0;
	double rhoMin = 0.0;
	double sAvg = 0.0;
	int L;

	public EGSpace(int L) {
		this.L = L;
		vField = new Vector3[L][L][L];
		aField = new Vector3[L][L][L];
		jField = new Vector3[L][L][L];
		bField = new Vector3[L][L][L];
		sField = new double[L][L][L];
		rhoField = new double[L][L][L];
		initialise(L);
	}

	void initialise(int L) {
		Vector3 zero = new Vector3(0.0, 0.0, 0.0);
		rhoMax = rhoMin = 0.0;

		for (int i = 0; i < L; i++) {
			for (int j = 0; j < L; j++) {
				for (int k = 0; k < L; k++) {
					vField[i][j][k] = aField[i][j][k] = jField[i][j][k] = bField[i][j][k] = zero;
					sField[i][j][k] = rhoField[i][j][k] = 0.0;
				}
			}
		}
	}

	void initJField(int a) {
		for (int i = 0; i < L; i++) {
			jField[i][L / 2 - a][L / 2 - a] = new Vector3(1.0, 0.0, 0.0);
			jField[i][L / 2 + a][L / 2 + a] = new Vector3(-1.0, 0.0, 0.0);
		}
	}


	// Velocities in x y & z have to be 1, 0 or -1 for this to work
	void moveCharge(int xVel, int yVel, int zVel) {

		if (xVel == 1) {
			for (int j = 0; j < L; j++) {
				for (int k = 0; k < L; k++) {
					double xLast = rhoField[L - 1][j][k];
					for (int i = (L - 1); i > 0; i--) {
						rhoField[i][j][k] = rhoField[i - 1][j][k];
					}
					rhoField[0][j][k] = xLast;
				}
			}
		}

		if (xVel == -1) {
			for (int j = 0; j < L; j++) {
				for (int k = 0; k < L; k++) {
					double xFirst = rhoField[0][j][k];
					for (int i = 0; i < (L - 2); i++) {
						rhoField[i][j][k] = rhoField[i + 1][j][k];
					}
					rhoField[L - 1][j][k] = xFirst;
				}
			}
		}

		if (yVel == 1) {
			for (int i = 0; i < L; i++) {
				for (int k = 0; k < L; k++) {
					double yLast = rhoField[i][L - 1][k];
					for (int j = (L - 1); j > 0; j--) {
						rhoField[i][j][k] = rhoField[i][j - 1][k];
					}
					rhoField[i][0][k] = yLast;
				}
			}
		}

		if (yVel == -1) {
			for (int i = 0; i < L; i++) {
				for (int k = 0; k < L; k++) {
					double yFirst = rhoField[i][0][k];
					for (int j = 0; j < (L - 2); j++) {
						rhoField[i][j][k] = rhoField[i][j + 1][k];
					}
					rhoField[i][L - 1][k] = yFirst;
				}
			}
		}

		if (zVel == 1) {
			for (int i = 0; i < L; i++) {
				for (int j = 0; j < L; j++) {
					double zLast = rhoField[i][j][L - 1];
					for (int k = (L - 1); k > 0; k--) {
						rhoField[i][j][k] = rhoField[i][j][k - 1];
					}
					rhoField[i][j][0] = zLast;
				}
			}
		}

		if (zVel == -1) {
			for (int i = 0; i < L; i++) {
				for (int j = 0; j < L; j++) {
					double zFirst = rhoField[i][j][0];
					for (int k = 0; k < (L - 2); k++) {
						rhoField[i][j][k] = rhoField[i][j][k + 1];
					}
					rhoField[i][j][L - 1] = zFirst;
				}
			}
		}
	}


	void setDipole() {
		setRhoElement(L / 2, L / 2, L / 2);
		minusRhoElement((L / 2) + 1, L / 2, L / 2);
	}

	void setQuadrupole() {
		setRhoElement(L / 2, L / 2, L / 2, 2);
		minusRhoElement((L / 2) + 1, L / 2, L / 2);
		minusRhoElement((L / 2) - 1, L / 2, L / 2);
	}

	void setRhoElement(int x, int y, int z, double value) {
		rhoField[x][y][z] = value;
		if (value > rhoMax) {
			rhoMax = value;
		}

		if (value < rhoMin) {
			rhoMin = value;
		}
	}

	void setRhoElement(int x, int y, int z) {
		boolean increasedMinimum = false;
		if (rhoField[x][y][z] <= rhoMin) {
			increasedMinimum = true;
		}
		rhoField[x][y][z]++;
		if (rhoField[x][y][z] > rhoMax) {
			rhoMax = rhoField[x][y][z];
		}

		if (increasedMinimum) {
			findRhoMax(1);
		}
	}

	void minusRhoElement(int x, int y, int z) {
		boolean decreasedMaximum = false;
		if (rhoField[x][y][z] >= rhoMax) {
			decreasedMaximum = true;
		}
		rhoField[x][y][z]--;

		if (rhoField[x][y][z] < rhoMin) {
			rhoMin = rhoField[x][y][z];
		}

		if (decreasedMaximum) {
			findRhoMax(2);
		}
	}

	void findRhoMax(int maxMin) {
		if (maxMin == 1) {
			rhoMin = 0.0;
		}
		if (maxMin == 2) {
			rhoMax = 0.0;
		}

		for (int i = 0; i < L; i++) {
			for (int j = 0; j < L; j++) {
				for (int k = 0; k < L; k++) {
					if (rhoField[i][j][k] > rhoMax) {
						rhoMax = rhoField[i][j][k];
					}

					if (rhoField[i][j][k] < rhoMin) {
						rhoMin = rhoField[i][j][k];
					}
				}
			}
		}
	}


	void setRhoField(double[][][] newsField) {
		this.rhoField = newsField;
	}

	void setsField(double[][][] newsField) {
		this.sField = newsField;
	}

	void setvField(Vector3[][][] newvField) {
		this.vField = newvField;
	}

	/*
	 *  Periodic Boundary Conditions
	 *  Performed by conditional operator
	 *  Using index substitution
	 */
	Vector3[][][] gradP() {

		double x;

		double y;

		double z;
		Vector3[][][] newvField = new Vector3[L][L][L];
		int xPrev;
		int yPrev;
		int zPrev;
		int xNext;
		int yNext;
		int zNext;

		for (int i = 0; i < L; i++) {
			xPrev = (i == 0) ? (L - 1) : (i - 1);
			xNext = (i == (L - 1)) ? 0 : (i + 1);

			for (int j = 0; j < L; j++) {
				yPrev = (j == 0) ? (L - 1) : (j - 1);
				yNext = (j == (L - 1)) ? 0 : (j + 1);

				for (int k = 0; k < L; k++) {
					zPrev = (k == 0) ? (L - 1) : (k - 1);
					zNext = (k == (L - 1)) ? 0 : (k + 1);

					x = (sField[xNext][j][k] - sField[xPrev][j][k]) * 0.5;
					y = (sField[i][yNext][k] - sField[i][yPrev][k]) * 0.5;
					z = (sField[i][j][zNext] - sField[i][j][zPrev]) * 0.5;

					newvField[i][j][k] = new Vector3(x, y, z);
				}
			}
		}

		return newvField;
	}

	/*
	 *  Static Boundary Conditions
	 *  Use Periodic scheme to avoid array out of bounds exceptions
	 *  And a conditional multiplier to set boundary cells to zero
	 */
	Vector3[][][] gradS() {

		double x;

		double y;

		double z;
		Vector3[][][] newvField = new Vector3[L][L][L];

		int xPrev;

		int yPrev;

		int zPrev;
		int xNext;
		int yNext;
		int zNext;
		int a;
		int b;
		int c;
		int d;
		int e;
		int f;

		for (int i = 0; i < L; i++) {
			xPrev = (i == 0) ? (L - 1) : (i - 1);
			a = (xPrev == (L - 1)) ? 0 : 1;
			xNext = (i == (L - 1)) ? 0 : (i + 1);
			b = (xNext == 0) ? 0 : 1;

			for (int j = 0; j < L; j++) {
				yPrev = (j == 0) ? (L - 1) : (j - 1);
				c = (yPrev == (L - 1)) ? 0 : 1;
				yNext = (j == (L - 1)) ? 0 : (j + 1);
				d = (yNext == 0) ? 0 : 1;

				for (int k = 0; k < L; k++) {
					zPrev = (k == 0) ? (L - 1) : (k - 1);
					e = (zPrev == (L - 1)) ? 0 : 1;
					zNext = (k == (L - 1)) ? 0 : (k + 1);
					f = (zNext == 0) ? 0 : 1;

					x = (b * sField[xNext][j][k] - a * sField[xPrev][j][k]) * 0.5;
					y = (d * sField[i][yNext][k] - c * sField[i][yPrev][k]) * 0.5;
					z = (f * sField[i][j][zNext] - e * sField[i][j][zPrev]) * 0.5;

					newvField[i][j][k] = new Vector3(x, y, z);
				}
			}
		}

		return newvField;
	}

	Vector3[][][] gradS2() {

		double x;

		double y;

		double z;
		Vector3[][][] newvField = new Vector3[L][L][L];

		int xPrev;

		int yPrev;

		int zPrev;
		int xNext;
		int yNext;
		int zNext;
		int a;
		int b;
		int c;
		int d;
		int e;
		int f;

		for (int i = 0; i < L; i++) {
			xPrev = i - 1;
			xNext = i + 1;
			a = b = 1;
			if (i == 0) {
				xPrev = (L - 1);
				a = 0;
			}
			if (i == (L - 1)) {
				xNext = 0;
				b = 0;
			}

			for (int j = 0; j < L; j++) {
				yPrev = j - 1;
				yNext = j + 1;
				c = d = 1;
				if (j == 0) {
					yPrev = (L - 1);
					c = 0;
				}
				if (j == (L - 1)) {
					yNext = 0;
					d = 0;
				}

				for (int k = 0; k < L; k++) {
					zPrev = k - 1;
					zNext = k + 1;
					e = f = 1;
					if (k == 0) {
						zPrev = (L - 1);
						e = 0;
					}
					if (k == (L - 1)) {
						zNext = 0;
						f = 0;
					}

					x = (b * sField[xNext][j][k] - a * sField[xPrev][j][k]) * 0.5;
					y = (d * sField[i][yNext][k] - c * sField[i][yPrev][k]) * 0.5;
					z = (f * sField[i][j][zNext] - e * sField[i][j][zPrev]) * 0.5;

					newvField[i][j][k] = new Vector3(x, y, z);
				}
			}
		}

		return newvField;
	}

	double[][][] laplacianP() {

		double[][][] newsField = new double[L][L][L];
		int xPrev;
		int yPrev;
		int zPrev;
		int xNext;
		int yNext;
		int zNext;

		for (int i = 0; i < L; i++) {
			xPrev = (i == 0) ? (L - 1) : (i - 1);
			xNext = (i == (L - 1)) ? 0 : (i + 1);

			for (int j = 0; j < L; j++) {
				yPrev = (j == 0) ? (L - 1) : (j - 1);
				yNext = (j == (L - 1)) ? 0 : (j + 1);

				for (int k = 0; k < L; k++) {
					zPrev = (k == 0) ? (L - 1) : (k - 1);
					zNext = (k == (L - 1)) ? 0 : (k + 1);

					newsField[i][j][k] = (sField[xNext][j][k] + sField[xPrev][j][k] + sField[i][yNext][k] + sField[i][yPrev][k] + sField[i][j][zNext] + sField[i][j][zPrev] - 6 * sField[i][j][k]);
				}
			}
		}
		return newsField;
	}

	double[][][] laplacianS() {

		double[][][] newsField = new double[L][L][L];
		int xPrev;
		int yPrev;
		int zPrev;
		int xNext;
		int yNext;
		int zNext;
		int a;
		int b;
		int c;
		int d;
		int e;
		int f;

		for (int i = 0; i < L; i++) {
			xPrev = (i == 0) ? (L - 1) : (i - 1);
			a = (xPrev == (L - 1)) ? 0 : 1;
			xNext = (i == (L - 1)) ? 0 : (i + 1);
			b = (xNext == 0) ? 0 : 1;

			for (int j = 0; j < L; j++) {
				yPrev = (j == 0) ? (L - 1) : (j - 1);
				c = (yPrev == (L - 1)) ? 0 : 1;
				yNext = (j == (L - 1)) ? 0 : (j + 1);
				d = (yNext == 0) ? 0 : 1;

				for (int k = 0; k < L; k++) {
					zPrev = (k == 0) ? (L - 1) : (k - 1);
					e = (zPrev == (L - 1)) ? 0 : 1;
					zNext = (k == (L - 1)) ? 0 : (k + 1);
					f = (zNext == 0) ? 0 : 1;

					newsField[i][j][k] = (b * sField[xNext][j][k] + a * sField[xPrev][j][k] + d * sField[i][yNext][k] + c * sField[i][yPrev][k] + f * sField[i][j][zNext] + e * sField[i][j][zPrev] - 6 * sField[i][j][k]);
				}
			}
		}
		return newsField;
	}

	double[][][] laplacianS2() {

		double[][][] newsField = new double[L][L][L];
		int xPrev;
		int yPrev;
		int zPrev;
		int xNext;
		int yNext;
		int zNext;
		int a;
		int b;
		int c;
		int d;
		int e;
		int f;
		for (int i = 0; i < L; i++) {
			xPrev = i - 1;
			xNext = i + 1;
			a = b = 1;
			if (i == 0) {
				xPrev = (L - 1);
				a = 0;
			}
			if (i == (L - 1)) {
				xNext = 0;
				b = 0;
			}

			for (int j = 0; j < L; j++) {
				yPrev = j - 1;
				yNext = j + 1;
				c = d = 1;
				if (j == 0) {
					yPrev = (L - 1);
					c = 0;
				}
				if (j == (L - 1)) {
					yNext = 0;
					d = 0;
				}

				for (int k = 0; k < L; k++) {
					zPrev = k - 1;
					zNext = k + 1;
					e = f = 1;
					if (k == 0) {
						zPrev = (L - 1);
						e = 0;
					}
					if (k == (L - 1)) {
						zNext = 0;
						f = 0;
					}

					newsField[i][j][k] = (b * sField[xNext][j][k] + a * sField[xPrev][j][k] + d * sField[i][yNext][k] + c * sField[i][yPrev][k] + f * sField[i][j][zNext] + e * sField[i][j][zPrev] - 6 * sField[i][j][k]);
				}
			}
		}
		return newsField;
	}


	double[][][] divP() {

		double[][][] newsField = new double[L][L][L];

		int xPrev;

		int yPrev;

		int zPrev;
		int xNext;
		int yNext;
		int zNext;

		for (int i = 0; i < L; i++) {
			xPrev = (i == 0) ? (L - 1) : (i - 1);
			xNext = (i == (L - 1)) ? 0 : (i + 1);

			for (int j = 0; j < L; j++) {
				yPrev = (j == 0) ? (L - 1) : (j - 1);
				yNext = (j == (L - 1)) ? 0 : (j + 1);

				for (int k = 0; k < L; k++) {
					zPrev = (k == 0) ? (L - 1) : (k - 1);
					zNext = (k == (L - 1)) ? 0 : (k + 1);

					newsField[i][j][k] = (vField[xNext][j][k].x - vField[xPrev][j][k].x + vField[i][yNext][k].y - vField[i][yPrev][k].y + vField[i][j][zNext].z - vField[i][j][zPrev].z) * 0.5;
				}
			}
		}
		return newsField;
	}

	double[][][] divS() {

		double[][][] newsField = new double[L][L][L];

		int xPrev;

		int yPrev;

		int zPrev;
		int xNext;
		int yNext;
		int zNext;
		int a;
		int b;
		int c;
		int d;
		int e;
		int f;

		for (int i = 0; i < L; i++) {
			xPrev = (i == 0) ? (L - 1) : (i - 1);
			a = (xPrev == (L - 1)) ? 0 : 1;
			xNext = (i == (L - 1)) ? 0 : (i + 1);
			b = (xNext == 0) ? 0 : 1;

			for (int j = 0; j < L; j++) {
				yPrev = (j == 0) ? (L - 1) : (j - 1);
				c = (yPrev == (L - 1)) ? 0 : 1;
				yNext = (j == (L - 1)) ? 0 : (j + 1);
				d = (yNext == 0) ? 0 : 1;

				for (int k = 0; k < L; k++) {
					zPrev = (k == 0) ? (L - 1) : (k - 1);
					e = (zPrev == (L - 1)) ? 0 : 1;
					zNext = (k == (L - 1)) ? 0 : (k + 1);
					f = (zNext == 0) ? 0 : 1;

					newsField[i][j][k] = (b * vField[xNext][j][k].x - a * vField[xPrev][j][k].x + d * vField[i][yNext][k].y - c * vField[i][yPrev][k].y + f * vField[i][j][zNext].z - e * vField[i][j][zPrev].z) * 0.5;
				}
			}
		}
		return newsField;
	}

	double[][][] divS2() {

		double[][][] newsField = new double[L][L][L];

		int xPrev;

		int yPrev;

		int zPrev;
		int xNext;
		int yNext;
		int zNext;
		int a;
		int b;
		int c;
		int d;
		int e;
		int f;
		for (int i = 0; i < L; i++) {
			xPrev = i - 1;
			xNext = i + 1;
			a = b = 1;
			if (i == 0) {
				xPrev = (L - 1);
				a = 0;
			}
			if (i == (L - 1)) {
				xNext = 0;
				b = 0;
			}

			for (int j = 0; j < L; j++) {
				yPrev = j - 1;
				yNext = j + 1;
				c = d = 1;
				if (j == 0) {
					yPrev = (L - 1);
					c = 0;
				}
				if (j == (L - 1)) {
					yNext = 0;
					d = 0;
				}

				for (int k = 0; k < L; k++) {
					zPrev = k - 1;
					zNext = k + 1;
					e = f = 1;
					if (k == 0) {
						zPrev = (L - 1);
						e = 0;
					}
					if (k == (L - 1)) {
						zNext = 0;
						f = 0;
					}

					newsField[i][j][k] = (b * vField[xNext][j][k].x - a * vField[xPrev][j][k].x + d * vField[i][yNext][k].y - c * vField[i][yPrev][k].y + f * vField[i][j][zNext].z - e * vField[i][j][zPrev].z) * 0.5;
				}
			}
		}
		return newsField;
	}

	Vector3[][][] curlP() {

		double x;

		double y;

		double z;
		Vector3[][][] newvField = new Vector3[L][L][L];
		int xPrev;
		int yPrev;
		int zPrev;
		int xNext;
		int yNext;
		int zNext;

		for (int i = 0; i < L; i++) {
			xPrev = (i == 0) ? (L - 1) : (i - 1);
			xNext = (i == (L - 1)) ? 0 : (i + 1);

			for (int j = 0; j < L; j++) {
				yPrev = (j == 0) ? (L - 1) : (j - 1);
				yNext = (j == (L - 1)) ? 0 : (j + 1);

				for (int k = 0; k < L; k++) {
					zPrev = (k == 0) ? (L - 1) : (k - 1);
					zNext = (k == (L - 1)) ? 0 : (k + 1);

					x = ((vField[i][yNext][k].z - vField[i][yPrev][k].z) - (vField[i][j][zNext].y - vField[i][j][zPrev].y)) * 0.5;
					y = ((vField[i][j][zNext].x - vField[i][j][zPrev].x) - (vField[xNext][j][k].z - vField[xPrev][j][k].z)) * 0.5;
					z = ((vField[xNext][j][k].y - vField[xPrev][j][k].y) - (vField[i][yNext][k].x - vField[i][yPrev][k].x)) * 0.5;
					newvField[i][j][k] = new Vector3(x, y, z);
				}
			}
		}
		return newvField;
	}

	Vector3[][][] curlP(Vector3[][][] safe) {

		double x;

		double y;

		double z;
		Vector3[][][] newvField = new Vector3[L][L][L];
		int xPrev;
		int yPrev;
		int zPrev;
		int xNext;
		int yNext;
		int zNext;

		for (int i = 0; i < L; i++) {
			xPrev = (i == 0) ? (L - 1) : (i - 1);
			xNext = (i == (L - 1)) ? 0 : (i + 1);

			for (int j = 0; j < L; j++) {
				yPrev = (j == 0) ? (L - 1) : (j - 1);
				yNext = (j == (L - 1)) ? 0 : (j + 1);

				for (int k = 0; k < L; k++) {
					zPrev = (k == 0) ? (L - 1) : (k - 1);
					zNext = (k == (L - 1)) ? 0 : (k + 1);

					x = ((safe[i][yNext][k].z - safe[i][yPrev][k].z) - (safe[i][j][zNext].y - safe[i][j][zPrev].y)) * 0.5;
					y = ((safe[i][j][zNext].x - safe[i][j][zPrev].x) - (safe[xNext][j][k].z - safe[xPrev][j][k].z)) * 0.5;
					z = ((safe[xNext][j][k].y - safe[xPrev][j][k].y) - (safe[i][yNext][k].x - safe[i][yPrev][k].x)) * 0.5;
					newvField[i][j][k] = new Vector3(x, y, z);
				}
			}
		}
		return newvField;
	}

	Vector3[][][] curlS() {

		double x;

		double y;

		double z;
		Vector3[][][] newvField = new Vector3[L][L][L];
		int xPrev;
		int yPrev;
		int zPrev;
		int xNext;
		int yNext;
		int zNext;
		int a;
		int b;
		int c;
		int d;
		int e;
		int f;

		for (int i = 0; i < L; i++) {
			xPrev = (i == 0) ? (L - 1) : (i - 1);
			a = (xPrev == (L - 1)) ? 0 : 1;
			xNext = (i == (L - 1)) ? 0 : (i + 1);
			b = (xNext == 0) ? 0 : 1;

			for (int j = 0; j < L; j++) {
				yPrev = (j == 0) ? (L - 1) : (j - 1);
				c = (yPrev == (L - 1)) ? 0 : 1;
				yNext = (j == (L - 1)) ? 0 : (j + 1);
				d = (yNext == 0) ? 0 : 1;

				for (int k = 0; k < L; k++) {
					zPrev = (k == 0) ? (L - 1) : (k - 1);
					e = (zPrev == (L - 1)) ? 0 : 1;
					zNext = (k == (L - 1)) ? 0 : (k + 1);
					f = (zNext == 0) ? 0 : 1;

					x = ((d * vField[i][yNext][k].z - c * vField[i][yPrev][k].z) - (f * vField[i][j][zNext].y - e * vField[i][j][zPrev].y)) * 0.5;
					y = ((f * vField[i][j][zNext].x - e * vField[i][j][zPrev].x) - (b * vField[xNext][j][k].z - a * vField[xPrev][j][k].z)) * 0.5;
					z = ((b * vField[xNext][j][k].y - a * vField[xPrev][j][k].y) - (d * vField[i][yNext][k].x - c * vField[i][yPrev][k].x)) * 0.5;
					newvField[i][j][k] = new Vector3(x, y, z);
				}
			}
		}
		return newvField;
	}


	Vector3[][][] curlS(Vector3[][][] vField) {

		double x;

		double y;

		double z;
		Vector3[][][] newvField = new Vector3[L][L][L];
		int xPrev;
		int yPrev;
		int zPrev;
		int xNext;
		int yNext;
		int zNext;
		int a;
		int b;
		int c;
		int d;
		int e;
		int f;

		for (int i = 0; i < L; i++) {
			xPrev = (i == 0) ? (L - 1) : (i - 1);
			a = (xPrev == (L - 1)) ? 0 : 1;
			xNext = (i == (L - 1)) ? 0 : (i + 1);
			b = (xNext == 0) ? 0 : 1;

			for (int j = 0; j < L; j++) {
				yPrev = (j == 0) ? (L - 1) : (j - 1);
				c = (yPrev == (L - 1)) ? 0 : 1;
				yNext = (j == (L - 1)) ? 0 : (j + 1);
				d = (yNext == 0) ? 0 : 1;

				for (int k = 0; k < L; k++) {
					zPrev = (k == 0) ? (L - 1) : (k - 1);
					e = (zPrev == (L - 1)) ? 0 : 1;
					zNext = (k == (L - 1)) ? 0 : (k + 1);
					f = (zNext == 0) ? 0 : 1;

					x = ((d * vField[i][yNext][k].z - c * vField[i][yPrev][k].z) - (f * vField[i][j][zNext].y - e * vField[i][j][zPrev].y)) * 0.5;
					y = ((f * vField[i][j][zNext].x - e * vField[i][j][zPrev].x) - (b * vField[xNext][j][k].z - a * vField[xPrev][j][k].z)) * 0.5;
					z = ((b * vField[xNext][j][k].y - a * vField[xPrev][j][k].y) - (d * vField[i][yNext][k].x - c * vField[i][yPrev][k].x)) * 0.5;
					newvField[i][j][k] = new Vector3(x, y, z);
				}
			}
		}
		return newvField;
	}


	Vector3[][][] curlS2() {

		double x;

		double y;

		double z;
		Vector3[][][] newvField = new Vector3[L][L][L];
		int xPrev;
		int yPrev;
		int zPrev;
		int xNext;
		int yNext;
		int zNext;
		int a;
		int b;
		int c;
		int d;
		int e;
		int f;
		for (int i = 0; i < L; i++) {
			xPrev = i - 1;
			xNext = i + 1;
			a = b = 1;
			if (i == 0) {
				xPrev = (L - 1);
				a = 0;
			}
			if (i == (L - 1)) {
				xNext = 0;
				b = 0;
			}

			for (int j = 0; j < L; j++) {
				yPrev = j - 1;
				yNext = j + 1;
				c = d = 1;
				if (j == 0) {
					yPrev = (L - 1);
					c = 0;
				}
				if (j == (L - 1)) {
					yNext = 0;
					d = 0;
				}

				for (int k = 0; k < L; k++) {
					zPrev = k - 1;
					zNext = k + 1;
					e = f = 1;
					if (k == 0) {
						zPrev = (L - 1);
						e = 0;
					}
					if (k == (L - 1)) {
						zNext = 0;
						f = 0;
					}

					x = ((d * vField[i][yNext][k].z - c * vField[i][yPrev][k].z) - (f * vField[i][j][zNext].y - e * vField[i][j][zPrev].y)) * 0.5;
					y = ((f * vField[i][j][zNext].x - e * vField[i][j][zPrev].x) - (b * vField[xNext][j][k].z - a * vField[xPrev][j][k].z)) * 0.5;
					z = ((b * vField[xNext][j][k].y - a * vField[xPrev][j][k].y) - (d * vField[i][yNext][k].x - c * vField[i][yPrev][k].x)) * 0.5;
					newvField[i][j][k] = new Vector3(x, y, z);
				}
			}
		}
		return newvField;
	}

	double[][][] jacobiLaplacianP(double[][][] uNMinusOne) {
		int L = uNMinusOne.length;
		double[][][] uN = new double[L][L][L];
		double LplusU;
		int xPrev;
		int yPrev;
		int zPrev;
		int xNext;
		int yNext;
		int zNext;

		for (int i = 0; i < L; i++) {
			xPrev = (i == 0) ? (L - 1) : (i - 1);
			xNext = (i == (L - 1)) ? 0 : (i + 1);

			for (int j = 0; j < L; j++) {
				yPrev = (j == 0) ? (L - 1) : (j - 1);
				yNext = (j == (L - 1)) ? 0 : (j + 1);

				for (int k = 0; k < L; k++) {
					zPrev = (k == 0) ? (L - 1) : (k - 1);
					zNext = (k == (L - 1)) ? 0 : (k + 1);

					LplusU = (uNMinusOne[xNext][j][k] + uNMinusOne[xPrev][j][k] + uNMinusOne[i][yNext][k] + uNMinusOne[i][yPrev][k] + uNMinusOne[i][j][zNext] + uNMinusOne[i][j][zPrev]);
					uN[i][j][k] = (LplusU + rhoField[i][j][k]) * 0.16666667;
				}
			}
		}
		return uN;
	}

	double[][][] jacobiLaplacianS(double[][][] uNMinusOne) {
		int L = uNMinusOne.length;
		double[][][] uN = new double[L][L][L];
		double LplusU;
		int xPrev;
		int yPrev;
		int zPrev;
		int xNext;
		int yNext;
		int zNext;
		int a;
		int b;
		int c;
		int d;
		int e;
		int f;

		for (int i = 0; i < L; i++) {
			xPrev = (i == 0) ? (L - 1) : (i - 1);
			a = (xPrev == (L - 1)) ? 0 : 1;
			xNext = (i == (L - 1)) ? 0 : (i + 1);
			b = (xNext == 0) ? 0 : 1;

			for (int j = 0; j < L; j++) {
				yPrev = (j == 0) ? (L - 1) : (j - 1);
				c = (yPrev == (L - 1)) ? 0 : 1;
				yNext = (j == (L - 1)) ? 0 : (j + 1);
				d = (yNext == 0) ? 0 : 1;

				for (int k = 0; k < L; k++) {
					zPrev = (k == 0) ? (L - 1) : (k - 1);
					e = (zPrev == (L - 1)) ? 0 : 1;
					zNext = (k == (L - 1)) ? 0 : (k + 1);
					f = (zNext == 0) ? 0 : 1;

					LplusU = (b * uNMinusOne[xNext][j][k] + a * uNMinusOne[xPrev][j][k] + d * uNMinusOne[i][yNext][k] + c * uNMinusOne[i][yPrev][k] + f * uNMinusOne[i][j][zNext] + e * uNMinusOne[i][j][zPrev]);
					uN[i][j][k] = (LplusU + rhoField[i][j][k]) * 0.16666667;
				}
			}
		}
		return uN;
	}


	double[][][] gaussSeidelLaplacianP(double[][][] uN) {
		int L = uN.length;
		sAvg = 0.0;
		double LplusU;
		int xPrev;
		int yPrev;
		int zPrev;
		int xNext;
		int yNext;
		int zNext;

		for (int i = 0; i < L; i++) {
			xPrev = (i == 0) ? (L - 1) : (i - 1);
			xNext = (i == (L - 1)) ? 0 : (i + 1);

			for (int j = 0; j < L; j++) {
				yPrev = (j == 0) ? (L - 1) : (j - 1);
				yNext = (j == (L - 1)) ? 0 : (j + 1);

				for (int k = 0; k < L; k++) {
					zPrev = (k == 0) ? (L - 1) : (k - 1);
					zNext = (k == (L - 1)) ? 0 : (k + 1);
					LplusU = (uN[xNext][j][k] + uN[xPrev][j][k] + uN[i][yNext][k] + uN[i][yPrev][k] + uN[i][j][zNext] + uN[i][j][zPrev]);
					sAvg += uN[i][j][k] = (LplusU + rhoField[i][j][k]) * 0.16666667;
				}
			}
		}
		sAvg /= L * L * L;
		return uN;
	}

	double[][][] gaussSeidelLaplacianS(double[][][] uN) {
		int L = uN.length;
		sAvg = 0.0;
		double LplusU;
		int xPrev;
		int yPrev;
		int zPrev;
		int xNext;
		int yNext;
		int zNext;
		int a;
		int b;
		int c;
		int d;
		int e;
		int f;

		for (int i = 0; i < L; i++) {
			xPrev = (i == 0) ? (L - 1) : (i - 1);
			a = (xPrev == (L - 1)) ? 0 : 1;
			xNext = (i == (L - 1)) ? 0 : (i + 1);
			b = (xNext == 0) ? 0 : 1;

			for (int j = 0; j < L; j++) {
				yPrev = (j == 0) ? (L - 1) : (j - 1);
				c = (yPrev == (L - 1)) ? 0 : 1;
				yNext = (j == (L - 1)) ? 0 : (j + 1);
				d = (yNext == 0) ? 0 : 1;

				for (int k = 0; k < L; k++) {
					zPrev = (k == 0) ? (L - 1) : (k - 1);
					e = (zPrev == (L - 1)) ? 0 : 1;
					zNext = (k == (L - 1)) ? 0 : (k + 1);
					f = (zNext == 0) ? 0 : 1;

					LplusU = (b * uN[xNext][j][k] + a * uN[xPrev][j][k] + d * uN[i][yNext][k] + c * uN[i][yPrev][k] + f * uN[i][j][zNext] + e * uN[i][j][zPrev]);
					sAvg += uN[i][j][k] = (LplusU + rhoField[i][j][k]) * 0.16666667;
				}
			}
		}
		sAvg /= L * L * L;
		return uN;
	}

	Vector3[][][] gaussSeidelMagneticP(Vector3[][][] uN) {
		int L = uN.length;
		double LplusU;
		Vector3 current;
		double xLU;
		double yLU;
		double zLU;
		double x;
		double y;
		double z;
		int xPrev;
		int yPrev;
		int zPrev;
		int xNext;
		int yNext;
		int zNext;

		for (int i = 0; i < L; i++) {
			xPrev = (i == 0) ? (L - 1) : (i - 1);
			xNext = (i == (L - 1)) ? 0 : (i + 1);

			for (int j = 0; j < L; j++) {
				yPrev = (j == 0) ? (L - 1) : (j - 1);
				yNext = (j == (L - 1)) ? 0 : (j + 1);

				for (int k = 0; k < L; k++) {
					zPrev = (k == 0) ? (L - 1) : (k - 1);
					zNext = (k == (L - 1)) ? 0 : (k + 1);
					xLU = (uN[xNext][j][k].x + uN[xPrev][j][k].x + uN[i][yNext][k].x + uN[i][yPrev][k].x + uN[i][j][zNext].x + uN[i][j][zPrev].x);
					yLU = (uN[xNext][j][k].y + uN[xPrev][j][k].y + uN[i][yNext][k].y + uN[i][yPrev][k].y + uN[i][j][zNext].y + uN[i][j][zPrev].y);
					zLU = (uN[xNext][j][k].z + uN[xPrev][j][k].z + uN[i][yNext][k].z + uN[i][yPrev][k].z + uN[i][j][zNext].z + uN[i][j][zPrev].z);

					x = (xLU + jField[i][j][k].x) * 0.16666667;
					y = (yLU + jField[i][j][k].y) * 0.16666667;
					z = (zLU + jField[i][j][k].z) * 0.16666667;

					uN[i][j][k] = new Vector3(x, y, z);

				}
			}
		}
		return uN;
	}

	Vector3[][][] gaussSeidelMagneticS(Vector3[][][] uN) {
		int L = uN.length;
		double LplusU;
		Vector3 current;
		double xLU;
		double yLU;
		double zLU;
		double x;
		double y;
		double z;
		int xPrev;
		int yPrev;
		int zPrev;
		int xNext;
		int yNext;
		int zNext;
		int a;
		int b;
		int c;
		int d;
		int e;
		int f;

		for (int i = 0; i < L; i++) {
			xPrev = (i == 0) ? (L - 1) : (i - 1);
			a = (xPrev == (L - 1)) ? 0 : 1;
			xNext = (i == (L - 1)) ? 0 : (i + 1);
			b = (xNext == 0) ? 0 : 1;

			for (int j = 0; j < L; j++) {
				yPrev = (j == 0) ? (L - 1) : (j - 1);
				c = (yPrev == (L - 1)) ? 0 : 1;
				yNext = (j == (L - 1)) ? 0 : (j + 1);
				d = (yNext == 0) ? 0 : 1;

				for (int k = 0; k < L; k++) {
					zPrev = (k == 0) ? (L - 1) : (k - 1);
					e = (zPrev == (L - 1)) ? 0 : 1;
					zNext = (k == (L - 1)) ? 0 : (k + 1);
					f = (zNext == 0) ? 0 : 1;

					xLU = (b * uN[xNext][j][k].x + a * uN[xPrev][j][k].x + d * uN[i][yNext][k].x + c * uN[i][yPrev][k].x + f * uN[i][j][zNext].x + e * uN[i][j][zPrev].x);
					yLU = (b * uN[xNext][j][k].y + a * uN[xPrev][j][k].y + d * uN[i][yNext][k].y + c * uN[i][yPrev][k].y + f * uN[i][j][zNext].y + e * uN[i][j][zPrev].y);
					zLU = (b * uN[xNext][j][k].z + a * uN[xPrev][j][k].z + d * uN[i][yNext][k].z + c * uN[i][yPrev][k].z + f * uN[i][j][zNext].z + e * uN[i][j][zPrev].z);

					x = (xLU + jField[i][j][k].x) * 0.16666667;
					y = (yLU + jField[i][j][k].y) * 0.16666667;
					z = (zLU + jField[i][j][k].z) * 0.16666667;

					uN[i][j][k] = new Vector3(x, y, z);

				}
			}
		}
		return uN;
	}
}


