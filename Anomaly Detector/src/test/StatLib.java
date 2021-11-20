package test;

public class StatLib {


	// simple average
	public static float avg(float[] x) {
		if (x.length == 0)
			return 0;

		float fAvg = 0;

		// sums the values of the given array
		for (float fIter: x)
			fAvg += fIter;

		return fAvg / x.length;
	}

	// returns the variance of X and Y
	public static float var(float[] x) {
		return cov(x,x);
	}

	// returns the covariance of X and Y
	public static float cov(float[] x, float[] y) {
		float fCovCalc = 0;

		for (int nIndex = 0; nIndex < x.length; nIndex++) {
			fCovCalc += ((x[nIndex]) * (y[nIndex]));
		}

		return (fCovCalc / x.length) - (avg(x) * avg(y));
	}


	// returns the Pearson correlation coefficient of X and Y
	public static float pearson(float[] x, float[] y) {
		return (float) (cov(x, y) / (Math.sqrt(var(x)) * Math.sqrt(var(y))));
	}

	// performs a linear regression and returns the line equation
	public static Line linear_reg(Point[] points) {
		float[] x = subPointArray(points, false);
		float[] y = subPointArray(points, true);
		float a = cov(x, y) / var(x);
		float b = avg(y) - (a * avg(x));

		return new Line(a, b);
	}

	// returns the deviation between point p and the line equation of the points
	public static float dev(Point p, Point[] points) {
		return dev(p, linear_reg(points));
	}

	// returns the deviation between point p and the line
	public static float dev(Point p, Line l) {
		return (float) (Math.sqrt(Math.pow(l.f(p.x) - p.y, 2)));
	}

	// splits point arrays into an array of x or array of y, boolean false is x true is y
	private static float[] subPointArray(Point[] points, boolean b) {
		float[] coordinate = new float[points.length];

		for (int nIndex = 0; nIndex < points.length; nIndex++) {
			if (b)
				coordinate[nIndex] = points[nIndex].y;
			else
				coordinate[nIndex] = points[nIndex].x;
		}

		return coordinate;
	}
}