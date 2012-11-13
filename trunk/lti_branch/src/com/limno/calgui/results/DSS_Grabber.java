package com.limno.calgui.results;

import hec.heclib.dss.HecDss;
import hec.heclib.util.HecTime;
import hec.io.TimeSeriesContainer;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JOptionPane;

import com.limno.calgui.Prefix;

/**
 * Class to grab (load) DSS time series for a set of scenarios passed in a JList. Each scenario is a string that corresponds to a
 * fully qualified (?) DSS file name. The DSS_Grabber instance provides access to the following:
 * <ul>
 * <li>Main time series (result, including necessary math) for each scenario<br>
 * <li>Secondary series (control) where indicated for each scenario<br>
 * <li>Difference for main time series for each scenario<br>
 * <li>Exceedance for main time series for each scenario<br>
 * </ul>
 * Typical usage sequence:
 * <ul>
 * <li>DSS_Grabber</li>
 * <li>setIsCFS</li>
 * <li>setBase</li>
 * <li>setLocation</li>
 * <li>setDateRange</li>
 * <li>getPrimarySeries</li>
 * <li>getSecondarySeries</li>
 * <li>Other calculations</li>
 * </ul>
 */
public class DSS_Grabber {

	static double cfs2TAFday = 0.001983471;

	private final JList lstScenarios;
	private String baseName;
	public String locationName;
	public String primaryDSSName;
	private String secondaryDSSName;

	private String title; // Chart title
	private String yLabel; // Y-axis label
	private String sLabel; // Label for secondary time series

	private boolean isCFS; // If true, display flows in CFS; otherwise TAFY.
	public String originalUnits; // Original units of results read in from DSS file

	private int startTime; // Start and end time of interest
	private int endTime;

	private int startWY; // USGS Water Year for start and end time.
	private int endWY;

	private int scenarios; // Number of scenarios passed in list parameter
	private double[][] annualTAFs;
	private double[][] annualTAFsDiff;

	/**
	 * DSS_Grabber constructor (?) is used to set the list of scenarios
	 * 
	 * @param list
	 *            a list of DSS file names corresponding to scenarios to be grabbed.
	 * 
	 */
	public DSS_Grabber(JList list) {
		lstScenarios = list;
		// TODO: Initialize privates here?
		// TODO: Check that necessary sequence was followed?
	}

	/**
	 * Sets the isCFS flag.
	 * 
	 * @param isCFS
	 *            set to True if flows are to be displayed in CFS, False for TAFY.
	 */
	public void setIsCFS(boolean isCFS) {
		this.isCFS = isCFS;
	}

	/**
	 * Sets the date range. Results are trimmed to the date range when read in GetOneSeries method.
	 * 
	 * @param dateRange
	 *            string describing the date range in format mmmyyyy-mmmyyyy. For example, the string "Apr1961-Mar1962" sets the
	 *            date range to run from April 1961 to March 1962.
	 */
	public void setDateRange(String dateRange) {

		HecTime ht = new HecTime();

		int m = monthToInt(dateRange.substring(0, 3));
		int y = new Integer(dateRange.substring(3, 7));
		ht.setYearMonthDay(m == 12 ? y + 1 : y, m == 12 ? 1 : m + 1, 1, 0); // TODO: confirm why this wraps?
		startTime = ht.value();
		startWY = (m < 10) ? y : y + 1; // Water year

		m = monthToInt(dateRange.substring(8, 11));
		y = new Integer(dateRange.substring(11, 15));
		ht.setYearMonthDay(m == 12 ? y + 1 : y, m == 12 ? 1 : m + 1, 1, 0);
		endTime = ht.value();
		endWY = ((m < 10) ? y : y + 1);
	}

	/**
	 * Sets base scenario for calculations and display. The base scenario is listed first in legends and used as the basis for
	 * difference calculations.
	 * 
	 * @param string
	 *            name of scenario/DSS file to use as base.
	 */
	public void setBase(String string) {
		baseName = string;
	}

	/**
	 * Gets name part of base scenario file in baseName.
	 * 
	 * @return name of the base scenario file WITHOUT the file extension
	 */
	public String getBase() {

		File file = new File(baseName);
		int dot = file.getName().lastIndexOf(".");
		return file.getName().substring(0, dot);
	}

	/**
	 * Sets dataset (DSS) names to read from scenario DSS files, title, and axis labels according to location specified using a
	 * coded string. The string is currently used as a lookup into either Schematic_DSS_Links4.table (if it starts with "SchVw") or
	 * into GUI_Links3.table. These tables may be combined in Phase 2.
	 * 
	 * @param string
	 *            index into GUI_Links3.table or Schematic_DSS_Link4.table
	 */
	public void setLocation(String string) {

		locationName = string;
		primaryDSSName = null;
		secondaryDSSName = null;

		// TODO: Combine lookup tables

		if (string.startsWith("SchVw")) {
			// Schematic view uses Table5 in mainMenu; this should be combined with GUI_Links3 table
			for (int i = 0; i < com.limno.calgui.MainMenu.getLookups5Length(); i++) {
				if (string.toUpperCase().endsWith(com.limno.calgui.MainMenu.getLookups5(i, 0))) {
					primaryDSSName = com.limno.calgui.MainMenu.getLookups5(i, 1);
					secondaryDSSName = com.limno.calgui.MainMenu.getLookups5(i, 2);
					yLabel = com.limno.calgui.MainMenu.getLookups5(i, 3);
					title = com.limno.calgui.MainMenu.getLookups5(i, 4);
					sLabel = com.limno.calgui.MainMenu.getLookups5(i, 5);
				}
			}
		} else

			for (int i = 0; i < com.limno.calgui.MainMenu.getLookupsLength(); i++) {
				if (string.endsWith(com.limno.calgui.MainMenu.getLookups(i, 0))) {
					primaryDSSName = com.limno.calgui.MainMenu.getLookups(i, 1);
					secondaryDSSName = com.limno.calgui.MainMenu.getLookups(i, 2);
					yLabel = com.limno.calgui.MainMenu.getLookups(i, 3);
					title = com.limno.calgui.MainMenu.getLookups(i, 4);
					sLabel = com.limno.calgui.MainMenu.getLookups(i, 5);
				}
			}
	}

	/**
	 * Sets dataset (DSS) names to read from scenario DSS files, title, and axis labels be parsing string defined in web viewer.
	 * 
	 * @param string
	 *            coded location string passed
	 */
	public void setLocationWeb(String string) {

		locationName = string;
		Prefix prefix = new Prefix();
		String type = prefix.getType(string);
		primaryDSSName = string + "/" + type;
		secondaryDSSName = "";
		yLabel = type;
		title = string;
		sLabel = "";

		// TODO: Confirm that new code works the same as original code
		// locationName = string;
		// primaryDSSName = null;
		// secondaryDSSName = null;
		// for (int i = 0; i < com.limno.calgui.MainMenu.getLookupsLength(); i++) {
		// Prefix prefix = new Prefix();
		// String type = prefix.getType(string);
		// primaryDSSName = string + "/" + type;
		// secondaryDSSName = "";
		// yLabel = type;
		// title = string;
		// sLabel = "";
		// }
	}

	/**
	 * Gets primary y-axis label assigned by DSS_Grabber to the results read in from DSS file.
	 * 
	 * @return string containing primary y-axis label
	 */
	public String getYLabel() {
		return yLabel;
	}

	/**
	 * Gets secondary y-axis label assigned by DSS_Grabber to the results read in from DSS file.
	 * 
	 * @return string containing secondary y-axis label
	 */
	public String getSLabel() {
		return sLabel;
	}

	/**
	 * Gets chart/table title assigned by DSS_Grabber to the results read in from DSS file. If no title is set, the name of the
	 * primary DSS dataset is returned instad.,
	 * 
	 * @return string containing title
	 */
	public String getTitle() {
		if (title != "")
			return title;
		else {
			return primaryDSSName;
		}
	}

	/**
	 * Reads a specified dataset from a specified HEC DSS file.
	 * 
	 * @param dssFilename
	 *            name of HEC DSS file from which to read results
	 * @param dssName
	 *            name(s) of dataset(s) to read from HEC DSS file. Multiple dataset names can be specified - separated by "+"; the
	 *            dataset results will be read and summed. if the first data set has the suffix (-1), the results read will be
	 *            shifted one month earlier.
	 * @return HEC TimeSeriesContainer with times, values, number of values, and file name.
	 */
	private TimeSeriesContainer getOneSeries(String dssFilename, String dssName) {

		HecDss hD;
		TimeSeriesContainer result = null;

		try {

			hD = HecDss.open(dssFilename);

			// Determine A-part and F-part directly from file - 10/4/2011 - assumes constant throughout

			@SuppressWarnings("unchecked")
			// TODO: is it better to write code that does not require suppresswarnings?
			Vector<String> aList = hD.getPathnameList();
			Iterator<String> it = aList.iterator();
			String aPath = it.next();
			String[] temp = aPath.split("/");
			String hecAPart = temp[1];
			String hecFPart = temp[6] + "/";

			// System.out.println(aPath);
			// System.out.println(hecFPart);

			String delims = "[+]";
			String[] dssNames = dssName.split(delims);

			// Check for time shift (-1 at end of name)

			boolean doTimeShift = false;
			if (dssNames[0].endsWith("(-1)")) {
				doTimeShift = true;
				dssNames[0] = dssNames[0].substring(0, dssNames[0].length() - 4);
			}

			// TODO: Note hard-coded D- and E-PART
			result = (TimeSeriesContainer) hD.get("/" + hecAPart + "/" + dssNames[0] + "/01JAN1930/1MON/" + hecFPart, true);

			if ((result == null) || (result.numberValues < 1)) {

				JOptionPane.showMessageDialog(null, "Could not find " + dssNames[0] + " in " + dssFilename, "Error",
				        JOptionPane.ERROR_MESSAGE);

			} else {

				// If no error, add results from other datasets in dssNames array.

				for (int i = 1; i < dssNames.length; i++) {
					// TODO: Note hard-coded D- and E-PART
					TimeSeriesContainer result2 = (TimeSeriesContainer) hD.get("/" + hecAPart + "/" + dssNames[i]
					        + "/01JAN2020/1MON/" + hecFPart, true);
					if (result2 == null) {
						JOptionPane.showMessageDialog(null, "Could not find " + dssNames[0] + " in " + dssFilename, "Error",
						        JOptionPane.ERROR_MESSAGE);
					} else {
						for (int j = 0; j < result2.numberValues; j++)
							result.values[j] = result.values[j] + result2.values[j];
					}
				}
			}

			// Trim to date range

			int first = 0; // Find starting index
			for (int i = 0; (i < result.numberValues) && (result.times[i] < startTime); i++)
				first = i + 1;

			int last = result.numberValues - 1; // find ending index
			for (int i = result.numberValues - 1; (i >= 0) && (result.times[i] > endTime); i--)
				last = i;

			if (first != 0) // Shift results in array to start
				for (int i = 0; i <= (last - first); i++) { // TODO: Think through change to <= done 11/9
					result.times[i] = result.times[i + first];
					result.values[i] = result.values[i + first];
				}

			result.numberValues = last - first + 1; // Adjust count of results

			// Do time shift where indicated (when a dataset has suffix "(-1)"

			if (doTimeShift) {
				for (int i = result.numberValues; i < result.numberValues - 1; i++)
					result.times[i] = result.times[i + 1];
				result.numberValues = result.numberValues - 1;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block - what's a good thing to do here? should it return null?
			e.printStackTrace();
		}

		// Store name portion of DSS file in TimeSeriesContainer

		String shortFileName = new File(dssFilename).getName();
		result.fileName = shortFileName;

		return result;
	}

	/**
	 * Reads the DSS results for the primary series for each scenario. Also stores for reference the units of measure for the
	 * primary series in the private variable originalUnits.
	 * 
	 * @return Array of HEC TimeSeriesContainer - one TSC for each scenario
	 */
	public TimeSeriesContainer[] getPrimarySeries() {

		TimeSeriesContainer[] results = null;

		if (locationName.contains("SchVw") && primaryDSSName.contains(",")) {

			// Special handling for DEMO of schematic view - treat multiple series as multiple scenarios
			// TODO: Longer-term approach is probably to add a rank to arrays storing all series

			String[] dssNames = primaryDSSName.split(",");
			scenarios = dssNames.length;
			results = new TimeSeriesContainer[scenarios];
			for (int i = 0; i < scenarios; i++)
				results[i] = getOneSeries(baseName, dssNames[i]);

			originalUnits = results[0].units;

		} else {

			// Store number of scenarios

			scenarios = lstScenarios.getModel().getSize();
			results = new TimeSeriesContainer[scenarios];

			// Base first

			results[0] = getOneSeries(baseName, primaryDSSName);
			originalUnits = results[0].units;

			// Then scenarios

			int j = 0;
			for (int i = 0; i < scenarios; i++) {
				String scenarioName = (String) lstScenarios.getModel().getElementAt(i);
				// String scenarioName = ((RBListItem) lstScenarios.getModel().getElementAt(i)).toString();
				if (!baseName.equals(scenarioName)) {
					j = j + 1;
					results[j] = getOneSeries(scenarioName, primaryDSSName);
				}
			}
		}
		return results;
	}

	/**
	 * Reads the DSS results for the secondary series for each scenario.
	 * 
	 * @return Array of HEC TimeSeriesContainer - one TSC for each scenario
	 */
	public TimeSeriesContainer[] getSecondarySeries() {

		if (secondaryDSSName.equals("") || secondaryDSSName.equals("null")) {
			return null;
		} else {

			scenarios = lstScenarios.getModel().getSize();
			TimeSeriesContainer[] results = new TimeSeriesContainer[scenarios];

			// Base first

			results[0] = getOneSeries(baseName, secondaryDSSName);

			// Then scenarios

			int j = 0;
			for (int i = 0; i < scenarios; i++) {
				String scenarioName = (String) lstScenarios.getModel().getElementAt(i);
				if (!baseName.equals(scenarioName)) {
					j = j + 1;
					results[j] = getOneSeries(scenarioName, secondaryDSSName);
				}
			}
			return results;
		}
	}

	/**
	 * Calculates the difference between scenarios and the base for a set of DSS results.
	 * 
	 * @param timeSeriesResults
	 *            array of HEC TimeSeriesContainer objects, each representing a set of results for a scenario. Base is in position
	 *            [0].
	 * @return array of HEC TimeSeriesContainer objects (size one less than timeSeriesResult. Position [0] contains difference
	 *         [1]-[0], position [1] contains difference [2]-[0], ...
	 */
	public TimeSeriesContainer[] getDifferenceSeries(TimeSeriesContainer[] timeSeriesResults) {

		TimeSeriesContainer[] results = new TimeSeriesContainer[scenarios - 1];
		for (int i = 0; i < scenarios - 1; i++) {

			results[i] = (TimeSeriesContainer) timeSeriesResults[i + 1].clone();
			for (int j = 0; j < results[i].numberValues; j++)
				results[i].values[j] = results[i].values[j] - timeSeriesResults[0].values[j];
		}
		return results;
	}

	/**
	 * Calculates annual volume in TAF for any CFS dataset, and replaces monthly values if TAF flag is checked.
	 * 
	 * @param primaryResults
	 * @param secondaryResults
	 */
	public void calcTAFforCFS(TimeSeriesContainer[] primaryResults, TimeSeriesContainer[] secondaryResults) {

		// Allocate and zero out

		int datasets = primaryResults.length;
		if (secondaryResults != null)
			datasets = datasets + secondaryResults.length;

		annualTAFs = new double[datasets][endWY - startWY + 2];

		for (int i = 0; i < datasets; i++)
			for (int j = 0; j < endWY - startWY + 1; j++)
				annualTAFs[i][j] = 0.0;

		// Calculate

		if (originalUnits.equals("CFS")) {

			HecTime ht = new HecTime();
			Calendar calendar = Calendar.getInstance();

			// Primary series

			for (int i = 0; i < primaryResults.length; i++) {
				for (int j = 0; j < primaryResults[i].numberValues; j++) {

					ht.set(primaryResults[i].times[j]);
					calendar.set(ht.year(), ht.month() - 1, 1);
					double monthlyTAF = primaryResults[i].values[j] * calendar.getActualMaximum(Calendar.DAY_OF_MONTH) * cfs2TAFday;
					int wy = ((ht.month() < 10) ? ht.year() : ht.year() + 1) - startWY;
					if (wy >= 0)
						annualTAFs[i][wy] += monthlyTAF;
					if (!isCFS)
						primaryResults[i].values[j] = monthlyTAF;
				}
				if (!isCFS)
					primaryResults[i].units = "TAF";
			}

			// Calculate differences if applicable (primary series only)

			if (primaryResults.length > 1) {
				annualTAFsDiff = new double[primaryResults.length - 1][endWY - startWY + 2];
				for (int i = 0; i < primaryResults.length - 1; i++)
					for (int j = 0; j < endWY - startWY + 1; j++)
						annualTAFsDiff[i][j] = annualTAFs[i + 1][j] - annualTAFs[0][j];
			}

			if (secondaryResults != null) {

				// Secondary series

				for (int i = 0; i < secondaryResults.length; i++) {
					for (int j = 0; j < secondaryResults[i].numberValues; j++) {

						ht.set(secondaryResults[i].times[j]);
						calendar.set(ht.year(), ht.month() - 1, 1);
						double monthlyTAF = secondaryResults[i].values[j] * calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
						        * cfs2TAFday;
						int wy = ((ht.month() < 10) ? ht.year() : ht.year() + 1) - startWY;
						annualTAFs[i + primaryResults.length][wy] += monthlyTAF;
						if (!isCFS)
							secondaryResults[i].values[j] = monthlyTAF;

					}
					if (!isCFS)
						secondaryResults[i].units = "TAF";
				}
			}
		}
	}

	public double getAnnualTAF(int i, int wy) {

		return annualTAFs[i][wy - startWY];
	}

	public double getAnnualTAFDiff(int i, int wy) {

		return annualTAFsDiff[i][wy - startWY];
	}

	/**
	 * Generates exceedance time series from monthly DSS results.
	 * 
	 * @param timeSeriesResults
	 *            array of HEC TimeSeriesContainer objects, each representing a set of results for a scenario.
	 * @return array of HEC TimeSeriesContainer objects - 14 for each input. Exceedances for all values are in [index=0], for each
	 *         month's values [1..12], and [13] for annual totals [Index=13]
	 */
	public TimeSeriesContainer[][] getExceedanceSeries(TimeSeriesContainer[] timeSeriesResults) {

		TimeSeriesContainer[][] results;
		if (timeSeriesResults == null)
			results = null;
		else {
			results = new TimeSeriesContainer[14][scenarios];

			for (int month = 0; month < 14; month++) {

				HecTime ht = new HecTime();
				for (int i = 0; i < scenarios; i++) {

					if (month == 13) {
						results[month][i] = (TimeSeriesContainer) timeSeriesResults[i].clone();
					} else {

						int n;
						int times2[];
						double values2[];

						results[month][i] = new TimeSeriesContainer();

						if (month == 12) {

							// Annual totals - grab from annualTAFs
							n = annualTAFs[i].length;
							times2 = new int[n];
							values2 = new double[n];
							for (int j = 0; j < n; j++) {
								ht.setYearMonthDay(j + startWY, 11, 1, 0);
								times2[j] = ht.value();
								values2[j] = annualTAFs[i][j];
							}

						} else {

							int[] times = timeSeriesResults[i].times;
							double[] values = timeSeriesResults[i].values;
							n = 0;
							for (int j = 0; j < times.length; j++) {
								ht.set(times[j]);
								if (ht.month() == month + 1)
									n = n + 1;
							}
							times2 = new int[n];
							values2 = new double[n];
							n = 0;
							for (int j = 0; j < times.length; j++) {
								ht.set(times[j]);
								if (ht.month() == month + 1) {
									times2[n] = times[j];
									values2[n] = values[j];
									n = n + 1;
								}
							}
						}
						results[month][i].times = times2;
						results[month][i].values = values2;
						results[month][i].numberValues = n;
						results[month][i].units = timeSeriesResults[i].units;
						results[month][i].fullName = timeSeriesResults[i].fullName;
						results[month][i].fileName = timeSeriesResults[i].fileName;
					}
					if (results[month][i].values != null) {
						double[] sortArray = results[month][i].values;
						Arrays.sort(sortArray);
						results[month][i].values = sortArray;
					}
				}
			}
		}
		return results;
	}

	public TimeSeriesContainer[][] getExceedanceSeries2(TimeSeriesContainer[] timeSeriesResults) {

		/*
		 * Copy of getExceedanceSeries to handle "exceedance of differences"
		 * 
		 * Calculates difference of annual TAFs to get proper results for [12]; should be recombined with getExceedanceSeries
		 */

		TimeSeriesContainer[][] results;
		if (timeSeriesResults == null)
			results = null;
		else {
			results = new TimeSeriesContainer[14][scenarios - 1];

			for (int month = 0; month < 14; month++) {

				HecTime ht = new HecTime();
				for (int i = 0; i < scenarios - 1; i++) {

					if (month == 13) {

						results[month][i] = (TimeSeriesContainer) timeSeriesResults[i + 1].clone();
						for (int j = 0; j < results[month][i].numberValues; j++)
							results[month][i].values[j] -= timeSeriesResults[0].values[j];

					} else {

						int n;
						int times2[];
						double values2[];

						results[month][i] = new TimeSeriesContainer();

						if (month == 12) {

							// Annual totals - grab from annualTAFs
							n = annualTAFs[i + 1].length;
							times2 = new int[n];
							values2 = new double[n];
							for (int j = 0; j < n; j++) {
								ht.setYearMonthDay(j + startWY, 11, 1, 0);
								times2[j] = ht.value();
								values2[j] = annualTAFs[i + 1][j] - annualTAFs[0][j];
							}

						} else {

							int[] times = timeSeriesResults[i + 1].times;
							double[] values = timeSeriesResults[i + 1].values;
							n = 0;
							for (int j = 0; j < times.length; j++) {
								ht.set(times[j]);
								if (ht.month() == month + 1)
									n = n + 1;
							}
							times2 = new int[n];
							values2 = new double[n];
							int nmax = n; // Added to trap Schematic View case where required flow has extra values
							n = 0;
							for (int j = 0; j < times.length; j++) {
								ht.set(times[j]);
								if ((ht.month() == month + 1) && (n < nmax) && (j < timeSeriesResults[0].values.length)) {
									times2[n] = times[j];
									values2[n] = values[j] - timeSeriesResults[0].values[j];
									n = n + 1;
								}
							}
						}
						results[month][i].times = times2;
						results[month][i].values = values2;
						results[month][i].numberValues = n;
						results[month][i].units = timeSeriesResults[i + 1].units;
						results[month][i].fullName = timeSeriesResults[i + 1].fullName;
						results[month][i].fileName = timeSeriesResults[i + 1].fileName;
					}
					if (results[month][i].values != null) {
						double[] sortArray = results[month][i].values;
						Arrays.sort(sortArray);
						results[month][i].values = sortArray;
					}
				}
			}
		}
		return results;
	}

	/**
	 * Converts three-letter month abbreviation to integer index
	 * 
	 * @param EndMon
	 *            three-letter abbreviation for month name
	 * @return month index ("Jan" = 1, ... "Dec" = 12)
	 */
	public int monthToInt(String EndMon) {

		int iEMon = 0;

		if (EndMon.equals("Jan")) {
			iEMon = 1;
		} else if (EndMon.equals("Feb")) {
			iEMon = 2;
		} else if (EndMon.equals("Mar")) {
			iEMon = 3;
		} else if (EndMon.equals("Apr")) {
			iEMon = 4;
		} else if (EndMon.equals("May")) {
			iEMon = 5;
		} else if (EndMon.equals("Jun")) {
			iEMon = 6;
		} else if (EndMon.equals("Jul")) {
			iEMon = 7;
		} else if (EndMon.equals("Aug")) {
			iEMon = 8;
		} else if (EndMon.equals("Sep")) {
			iEMon = 9;
		} else if (EndMon.equals("Oct")) {
			iEMon = 10;
		} else if (EndMon.equals("Nov")) {
			iEMon = 11;
		} else if (EndMon.equals("Dec")) {
			iEMon = 12;
		} /* TODO: Consider else to raise error condition? */

		return iEMon;
	}

}