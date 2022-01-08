package test;

import java.util.ArrayList;
import java.util.List;

public class SimpleAnomalyDetector implements TimeSeriesAnomalyDetector {

	private List<CorrelatedFeatures> correlatedList;
	float threshHold = 0.9f;
	TimeSeries timeSeries;

	@Override
	public void learnNormal(TimeSeries ts) {
		this.timeSeries = ts;
		int nNumberofCol = ts.getHashKeys().length;
		correlatedList = new ArrayList<>();
		float fMaxPearson;
		float fCurrPearson;
		float[] x;
		float[] y;
		float fMaxDev;
		float fCurrentDev;
		Line lrLine;
		float correlation;
		Point points[];
		int nNextCol;
		for (int nCol = 0; nCol < nNumberofCol - 1; nCol++) {
			fMaxPearson = 0;

			correlation = -1;
			x = ArrayListToArray(ts.getHashTimeSeries().get(ts.getHashKeys()[nCol]));
			for (nNextCol = nCol + 1; nNextCol < nNumberofCol; nNextCol++) {
				y = ArrayListToArray(ts.getHashTimeSeries().get(ts.getHashKeys()[nNextCol]));
				fCurrPearson = Math.abs(StatLib.pearson(x, y));
				if (fCurrPearson > fMaxPearson) {
					fMaxPearson = fCurrPearson;
					correlation = nNextCol;
				}
			}

			if (correlation != -1 && fMaxPearson > threshHold) {
				y = ArrayListToArray(ts.getHashTimeSeries().get(ts.getHashKeys()[(int) correlation]));
				points = FloatArraytoPointArray(x, y);
				fMaxDev = 0;
				lrLine = StatLib.linear_reg(x, y);
				for (int nIndex = 0; nIndex < x.length; nIndex++) {
					fCurrentDev = StatLib.dev(points[nIndex], lrLine);
					if (fCurrentDev > fMaxDev) {
						fMaxDev = fCurrentDev;
					}
				}

				correlatedList.add(new CorrelatedFeatures(ts.getHashKeys()[nCol], ts.getHashKeys()[(int) correlation],
						fMaxPearson, lrLine, (float) (fMaxDev * 1.1)));
			}
		}
	}

	@Override
	public List<AnomalyReport> detect(TimeSeries ts) {
		List<AnomalyReport> lstReport = new ArrayList<>();

		for (CorrelatedFeatures cfList : correlatedList) {

			for (int nPointIndex = 0; nPointIndex < ts.getHashTimeSeries().get(cfList.feature1).size(); nPointIndex++) {
				if (Math.abs(ts.getHashTimeSeries().get(cfList.feature2).get(nPointIndex) -
						cfList.lin_reg.f(ts.getHashTimeSeries().get(cfList.feature1).get(nPointIndex))) > cfList.threshold) {
					lstReport.add(new AnomalyReport(cfList.feature1 + "-" + cfList.feature2, nPointIndex + 1));
				}
			}
		}

		return lstReport;
	}

	public List<CorrelatedFeatures> getNormalModel() {
		return correlatedList;
	}

	private float[] ArrayListToArray(ArrayList<Float> list) {
		float[] fArray = new float[list.size()];
		for (int nIndex = 0; nIndex < list.size(); nIndex++) {
			fArray[nIndex] = list.get(nIndex);
		}
		return fArray;
	}

	// Creates a points array from two float arrays
	private static Point[] FloatArraytoPointArray(float[] x, float[] y) {
		Point[] points = new Point[x.length];

		for (int nIndex = 0; nIndex < points.length; nIndex++) {
			points[nIndex] = new Point(x[nIndex], y[nIndex]);
		}

		return points;
	}

	public void setThreshHold(float threshHold) {
		this.threshHold = threshHold;
	}
	public float getThreshHold() {
		return this.threshHold;
	}
}