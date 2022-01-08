package test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Commands {

	private SharedState sharedState = new SharedState();

	// Default IO interface
	public interface DefaultIO {
		public String readText();

		public void write(String text);

		public float readVal();

		public void write(float val);

		public default void writeln(String text) {
			write(text);
			write("\n");
		}
	}

	// the default IO to be used in all commands
	// You expected Default IO BUT IT WAS ME
	DefaultIO dio;

	public Commands(DefaultIO dio) {
		this.dio = dio;
	}

	// you may add other helper classes here


	// the shared state of all commands
	private class SharedState {
		// implement here whatever you need
		public SimpleAnomalyDetector SAD;
		public List<AnomalyReport> reports;
	}


	// Command abstract class
	public abstract class Command {
		protected String description;

		public Command(String description) {
			this.description = description;
		}

		public abstract void execute();
	}

	public class CliCommand extends Command {
		private int commandType;
		private final String trainCSVFile = "trainCSV.csv";
		private final String testCSVFile = "testCSV.csv";

		public CliCommand(int nCommandNo) {
			super("No Command");

			commandType = nCommandNo;
		}

		@Override
		public void execute() {

			//dio.writeln(description);
			switch (this.commandType) {
				case 1:
					try {
						PrintWriter trainCSV = new PrintWriter(new FileWriter(trainCSVFile));
						PrintWriter testCSV = new PrintWriter(new FileWriter(testCSVFile));
						dio.writeln("Please upload your local train CSV file.");
						writeToCSV(trainCSV);
						dio.writeln("Please upload your local test CSV file.");
						writeToCSV(testCSV);

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case 2:
					sharedState.SAD = new SimpleAnomalyDetector();
					dio.writeln("The current correlation threshold is " + sharedState.SAD.getThreshHold() + "\n" +
							"Type a new threshold");
					sharedState.SAD.setThreshHold(Float.parseFloat(dio.readText()));
					break;
				case 3:
					TimeSeries ts = new TimeSeries(trainCSVFile);
					sharedState.SAD.learnNormal(ts);
					TimeSeries ts2 = new TimeSeries(testCSVFile);
					sharedState.reports = new ArrayList<>();
					sharedState.reports = sharedState.SAD.detect(ts2);
					dio.writeln("anomaly detection complete.");
					break;
				case 4:
					for (AnomalyReport ar : sharedState.reports) {
						dio.writeln(ar.timeStep + "\t " + ar.description);
					}
					dio.writeln("Done.");
					break;
				case 5:
					dio.writeln("Please upload your local anomalies file.");
					compareAnomalies();
					break;
				case 6:
					File trainCSV = new File(trainCSVFile);
					trainCSV.delete();
					File testCSV = new File(testCSVFile);
					testCSV.delete();
					break;
				default:
					dio.writeln("Wrong Type");
			}
		}

		public void compareAnomalies() {
			String strRead = dio.readText();
			String[] splitSteps;
			float TP = 0;
			float FP;
			float TN = sharedState.SAD.timeSeries.getHashTimeSeries().get
					(sharedState.SAD.getNormalModel().get(0).feature1).size();
			float FN = 0;
			int nNumofSections = 0;
			boolean isFound = false;

			// True Positive
			while (!strRead.equals("done")) {
				splitSteps = strRead.split(",");
				FN++;
				for (AnomalyReport ar : sharedState.reports) {
					for (long lIndex = Long.parseLong(splitSteps[0]); lIndex <= Long.parseLong(splitSteps[1]); lIndex++) {
						if (lIndex == ar.timeStep) {
							TP++;
							isFound = true;
							break;
						}
					}
					if (isFound) {
						isFound = false;
						break;
					}
				}
				strRead = dio.readText();
			}

			// Find the amount of sections
			for (AnomalyReport ar : sharedState.reports) {
				if (ar.timeStep == sharedState.reports.get(sharedState.reports.size() - 1).timeStep) {
					nNumofSections++;
					break;
				}
				if (ar.timeStep + 1 != sharedState.reports.get(sharedState.reports.indexOf(ar) + 1).timeStep) {
					nNumofSections++;
				}
			}

			// Calculate False Positive
			FP = nNumofSections - TP;
			FN -= TP;

			dio.writeln("Upload complete.");
			float TPR = (float) ((int) (TP / (TP + FN) * 1000)) / 1000;
			float FPR = (float) ((int) (FP / (TN) * 1000)) / 1000;
			dio.writeln("True Positive Rate: " + TPR);
			dio.writeln("False Positive Rate: " + FPR);
		}

		public void writeToCSV(PrintWriter CSV) {
			String strReader;
			do {
				strReader = dio.readText();
				if (!strReader.equals("done")) {
					CSV.println(strReader);
				}
			} while (!strReader.equals("done"));
			CSV.close();
			dio.writeln("Upload complete.");
		}

		public void changeCommandType(int command_type) {
			String commandDesc = "";
			switch (this.commandType) {
				case 1:
					commandDesc = "Upload Train CSV";
					break;
				case 2:
					commandDesc = "Change Thresh Hold";
					break;
				case 3:
					commandDesc = "Detect Anomalies";
					break;
				case 4:
					commandDesc = "Report Anomalies";
					break;
				case 5:
					commandDesc = "Compare Anomalies";
					break;
				case 6:
					commandDesc = "Exit The Anomalies Detection Server";
					break;
				default:
					commandDesc = "No Command";
					break;
			}

			this.description = commandDesc;
		}
	}
}
