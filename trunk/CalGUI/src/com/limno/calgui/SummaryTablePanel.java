package com.limno.calgui;

import hec.heclib.util.HecTime;
import hec.io.TimeSeriesContainer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.*;
import javax.swing.plaf.basic.*;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;

import com.limno.calgui.table.ColumnGroup;
import com.limno.calgui.table.GroupableTableHeader;

public class SummaryTablePanel extends JPanel {
	int n[][][];
	double x[][][];
	double xx[][][];
	double avg[][][];
	double sdev[][][];
	double min[][][];
	double max[][][];
	double med[][][];
	double medx[][][][];

	private static int ylt[][] = { { 1920, 2, 2, 1, 1, 0, 3, 2, 0, }, { 1921, 2, 2, 1, 1, 0, 3, 2, 0, },
			{ 1922, 2, 1, 1, 1, 0, 4, 2, 0, }, { 1923, 3, 2, 3, 1, 0, 4, 3, 0, }, { 1924, 5, 5, 4, 2, 1, 5, 6, 0, },
			{ 1925, 4, 3, 1, 1, 0, 2, 5, 0, }, { 1926, 4, 4, 3, 1, 0, 4, 5, 0, }, { 1927, 1, 2, 1, 1, 0, 2, 1, 0, },
			{ 1928, 2, 3, 1, 1, 0, 3, 2, 1, }, { 1929, 5, 5, 3, 1, 0, 5, 6, 1, }, { 1930, 4, 5, 2, 1, 0, 4, 5, 1, },
			{ 1931, 5, 5, 4, 2, 1, 5, 6, 1, }, { 1932, 4, 2, 4, 1, 0, 4, 5, 1, }, { 1933, 5, 4, 4, 1, 0, 4, 6, 1, },
			{ 1934, 5, 5, 4, 2, 1, 5, 6, 1, }, { 1935, 3, 2, 1, 1, 0, 4, 3, 0, }, { 1936, 3, 2, 1, 1, 0, 3, 3, 0, },
			{ 1937, 3, 1, 2, 1, 0, 4, 3, 0, }, { 1938, 1, 1, 1, 1, 0, 1, 1, 0, }, { 1939, 4, 4, 3, 2, 0, 5, 5, 0, },
			{ 1940, 2, 2, 1, 1, 0, 2, 2, 0, }, { 1941, 1, 1, 1, 1, 0, 1, 1, 0, }, { 1942, 1, 1, 1, 1, 0, 2, 1, 0, },
			{ 1943, 1, 1, 1, 1, 0, 3, 1, 0, }, { 1944, 4, 3, 3, 1, 0, 5, 5, 0, }, { 1945, 3, 2, 1, 1, 0, 3, 3, 0, },
			{ 1946, 3, 2, 1, 1, 0, 2, 3, 0, }, { 1947, 4, 4, 3, 1, 0, 4, 5, 0, }, { 1948, 3, 3, 1, 1, 0, 3, 3, 0, },
			{ 1949, 4, 3, 2, 1, 0, 3, 5, 0, }, { 1950, 3, 3, 2, 1, 0, 4, 3, 0, }, { 1951, 2, 2, 1, 1, 0, 2, 2, 0, },
			{ 1952, 1, 1, 1, 1, 0, 2, 1, 0, }, { 1953, 1, 3, 1, 1, 0, 2, 1, 0, }, { 1954, 2, 3, 1, 1, 0, 2, 2, 0, },
			{ 1955, 4, 4, 2, 1, 0, 4, 5, 0, }, { 1956, 1, 1, 1, 1, 0, 1, 1, 0, }, { 1957, 2, 3, 1, 1, 0, 3, 2, 0, },
			{ 1958, 1, 1, 1, 1, 0, 1, 1, 0, }, { 1959, 3, 4, 1, 1, 0, 3, 3, 0, }, { 1960, 4, 5, 1, 1, 0, 3, 5, 0, },
			{ 1961, 4, 5, 1, 1, 0, 3, 5, 0, }, { 1962, 3, 3, 1, 1, 0, 3, 3, 0, }, { 1963, 1, 2, 1, 1, 0, 2, 1, 0, },
			{ 1964, 4, 4, 3, 1, 0, 4, 5, 0, }, { 1965, 1, 1, 1, 1, 0, 2, 1, 0, }, { 1966, 3, 3, 1, 1, 0, 3, 3, 0, },
			{ 1967, 1, 1, 1, 1, 0, 2, 1, 0, }, { 1968, 3, 4, 1, 1, 0, 3, 3, 0, }, { 1969, 1, 1, 1, 1, 0, 1, 1, 0, },
			{ 1970, 1, 2, 1, 1, 0, 2, 1, 0, }, { 1971, 1, 3, 1, 1, 0, 2, 1, 0, }, { 1972, 3, 4, 1, 1, 0, 3, 3, 0, },
			{ 1973, 2, 2, 1, 1, 0, 2, 2, 0, }, { 1974, 1, 1, 1, 1, 0, 1, 1, 0, }, { 1975, 1, 1, 1, 1, 0, 2, 1, 0, },
			{ 1976, 5, 5, 3, 2, 0, 4, 6, 2, }, { 1977, 5, 5, 4, 2, 1, 5, 7, 2, }, { 1978, 2, 1, 1, 1, 0, 1, 2, 0, },
			{ 1979, 3, 2, 2, 1, 0, 4, 3, 0, }, { 1980, 2, 1, 1, 1, 0, 2, 2, 0, }, { 1981, 4, 4, 2, 2, 0, 4, 5, 0, },
			{ 1982, 1, 1, 1, 1, 0, 1, 1, 0, }, { 1983, 1, 1, 1, 1, 0, 1, 1, 0, }, { 1984, 1, 2, 1, 1, 0, 2, 1, 0, },
			{ 1985, 4, 4, 3, 1, 0, 4, 5, 0, }, { 1986, 1, 1, 1, 1, 0, 2, 1, 3, }, { 1987, 4, 5, 3, 2, 0, 4, 5, 3, },
			{ 1988, 5, 5, 3, 2, 1, 4, 6, 3, }, { 1989, 4, 5, 1, 1, 0, 3, 5, 3, }, { 1990, 5, 5, 3, 2, 0, 4, 6, 3, },
			{ 1991, 5, 5, 4, 1, 1, 5, 6, 3, }, { 1992, 5, 5, 4, 2, 0, 4, 6, 3, }, { 1993, 2, 1, 1, 1, 0, 2, 2, 0, },
			{ 1994, 5, 5, 4, 2, 0, 5, 6, 0, }, { 1995, 1, 1, 1, 1, 0, 1, 0, 0, }, { 1996, 1, 1, 1, 1, 0, 2, 0, 0, },
			{ 1997, 1, 1, 1, 1, 0, 2, 0, 0, }, { 1998, 1, 1, 1, 1, 0, 1, 0, 0, }, { 1999, 1, 2, 1, 1, 0, 2, 0, 0, },
			{ 2000, 2, 2, 1, 1, 0, 2, 0, 0, }, { 2001, 4, 4, 1, 2, 0, 4, 0, 0, }, { 2002, 4, 4, 1, 1, 0, 3, 0, 0, },
			{ 2003, 2, 3, 1, 1, 0, 2, 0, 0, } };

	private void update(int i1, int i2, double value, int m) {
		x[i1][i2][m] += value;
		xx[i1][i2][m] += (value * value);
		if (min[i1][i2][m] > value)
			min[i1][i2][m] = value;
		if (max[i1][i2][m] < value)
			max[i1][i2][m] = value;
		medx[i1][i2][m][n[i1][i2][m]] = value;
		n[i1][i2][m]++;

		x[i1][i2][0] += value;
		xx[i1][i2][0] += (value * value);
		if (min[i1][i2][0] > value)
			min[i1][i2][0] = value;
		if (max[i1][i2][0] < value)
			max[i1][i2][0] = value;
		medx[i1][i2][0][n[i1][i2][0]] = value;
		n[i1][i2][0]++;
	}

	Vector<String> columns;

	SummaryTablePanel(String title, TimeSeriesContainer tscs[], String tagString) {

		super();

		// Determine target rows and columns

		Vector<String> data[] = new Vector[tscs.length];
		columns = new Vector<String>(15);
		columns.addElement("Year Group");
		columns.addElement("Statistic");
		columns.addElement("Oct");
		columns.addElement("Nov");
		columns.addElement("Dec");
		columns.addElement("Jan");
		columns.addElement("Feb");
		columns.addElement("Mar");
		columns.addElement("Apr");
		columns.addElement("May");
		columns.addElement("Jun");
		columns.addElement("Jul");
		columns.addElement("Aug");
		columns.addElement("Sep");
		columns.addElement("Annual");

		int cols = 15;
		int rows = 0;

		// loop over all Primary datasets

		JScrollPane scrollPane = new JScrollPane();
		JPanel panel = new JPanel();
		// panel.setPreferredSize(new Dimension(70, 600));
		panel.setLayout((LayoutManager) (new BoxLayout(panel, BoxLayout.PAGE_AXIS)));

		for (int t = 0; t < tscs.length; t++) {

			// Initialize accumulators

			n = new int[6][6][13];
			x = new double[6][6][13];
			xx = new double[6][6][13];
			min = new double[6][6][13];
			max = new double[6][6][13];
			for (int i1 = 0; i1 < 6; i1++)
				for (int i2 = 0; i2 < 6; i2++)
					for (int i3 = 0; i3 < 13; i3++) {

						n[i1][i2][i3] = 0;
						x[i1][i2][i3] = 0;
						xx[i1][i2][i3] = 0;
						min[i1][i2][i3] = 1e20;
						max[i1][i2][i3] = 0;
					}

			med = new double[6][6][13];
			medx = new double[6][6][13][tscs[t].numberValues]; // TODO - adjust
																// for
																// subset of
																// date

			// Loop through timeseries

			HecTime ht = new HecTime();

			for (int i = 0; i < tscs[t].numberValues; i++) {

				ht.set(tscs[t].times[i]);
				int y = ht.year();
				int m = ht.month();
				int wy = (m < 10) ? y : y - 1;
				if (wy >= 1920) { // TODO - replace temporary filter with values
									// based on controls
					int ySac403030 = (m < 2) ? y - 1 : y;
					int ySHASTAindex = (m < 3) ? y - 1 : y;
					int yFEATHERindex = (m < 2) ? y - 1 : y;
					int ySJRindex = (m < 2) ? y - 1 : y;

					update(0, 0, tscs[t].values[i], m);
					update(1, ylt[ySac403030 - 1920][1], tscs[t].values[i], m);
					update(2, ylt[ySHASTAindex - 1920][3], tscs[t].values[i], m);
					update(3, ylt[yFEATHERindex - 1920][5], tscs[t].values[i], m);
					update(4, ylt[ySJRindex - 1920][2], tscs[t].values[i], m);

					if (ylt[wy - 1920][8] != 0) {
						update(5, ylt[wy - 1920][8], tscs[t].values[i], m);
						update(5, 0, tscs[t].values[i], m);
					}
				}

			}

			avg = new double[6][6][13];
			sdev = new double[6][6][13];
			data[t] = new Vector<String>();
			String[] leftPart = { "All", "Sac 40-30-30", "Shasta", "Feather", "SJR", "Dry" };
			String[] rightPartsclimate = { "", "Wet", "Above", "Normal", "Dry", "Extreme" };
			String[] rightPartsDry = { "All dry periods", "1928-1934", "1976-1977", "1986-1992" };
			DecimalFormat df1 = new DecimalFormat("#.#");
			DecimalFormat df2 = new DecimalFormat("#.##");

			// Calculate results
			for (int i1 = 0; i1 < 6; i1++)
				for (int i2 = 0; i2 < 6; i2++)
					for (int i3 = 0; i3 < 13; i3++)

						if ((((i1 == 0) && tagString.contains("All years") && (i2 == 0))
								|| ((i1 == 1) && tagString.contains("40-30-30"))
								|| ((i1 == 2) && tagString.contains("Shasta"))
								|| ((i1 == 3) && tagString.contains("Feather"))
								|| ((i1 == 4) && tagString.contains("SJR Index"))
								|| ((i1 == 5) && tagString.contains("All dry"))
								|| ((i1 == 5) && (i2 == 1) && tagString.contains("1928"))
								|| ((i1 == 5) && (i2 == 2) && tagString.contains("1976")) || ((i1 == 5) && (i2 == 3) && tagString
								.contains("1986"))) && (n[i1][i2][i3] != 0)) {

							avg[i1][i2][i3] = x[i1][i2][i3] / n[i1][i2][i3];
							sdev[i1][i2][i3] = Math.sqrt(Math.abs(xx[i1][i2][i3] / n[i1][i2][i3] - avg[i1][2][i3]
									* avg[i1][2][i3]));

							int nmed = n[i1][i2][i3];
							double[] medx2 = new double[nmed];
							for (int i4 = 0; i4 < nmed; i4++)
								medx2[i4] = medx[i1][i2][i3][i4];
							Arrays.sort(medx2);
							// TODO fix logic for even sizes
							nmed = (int) nmed / 2;
							med[i1][i2][i3] = medx2[nmed];
						}

			// Put into table
			String[] tagStringList = { "Avg", "StdDev", "Median", "Min", "Max" };

			for (int tag = 0; tag < tagStringList.length; tag++)
				if (tagString.contains(tagStringList[tag]))
					for (int i1 = 0; i1 < 6; i1++)
						for (int i2 = 0; i2 < 6; i2++)

							if ((((i1 == 0) && tagString.contains("All years") && (i2 == 0))
									|| ((i1 == 1) && tagString.contains("40-30-30"))
									|| ((i1 == 2) && tagString.contains("Shasta"))
									|| ((i1 == 3) && tagString.contains("Feather"))
									|| ((i1 == 4) && tagString.contains("SJR Index"))
									|| ((i1 == 5) && tagString.contains("All dry"))
									|| ((i1 == 5) && (i2 == 1) && tagString.contains("1928"))
									|| ((i1 == 5) && (i2 == 2) && tagString.contains("1976")) || ((i1 == 5)
									&& (i2 == 3) && tagString.contains("1986")))) {

								String rightPart;

								if (i1 == 0) {
									rightPart = "";
								} else if (i1 == 1 || i1 == 4) {
									rightPart = " (" + rightPartsclimate[i2] + ")";
								} else if (i1 <= 4)
									rightPart = " " + Integer.toString(i2);
								else
									rightPart = " (" + rightPartsDry[i2] + ")";

								// System.out.print(leftPart[i1] + rightPart);
								// System.out.print("\t");
								// System.out.print(n[i1][i2]);
								// System.out.print(" ");
								// System.out.print(df1.format(avg[i1][i2]));
								// System.out.print(" ");
								// System.out.print(df2.format(sdev[i1][i2]));
								// System.out.print(" ");
								// System.out.print(df1.format(min[i1][i2]));
								// System.out.print(" ");
								// System.out.println(df1.format(max[i1][i2]));

								data[t].addElement(leftPart[i1] + rightPart);
								data[t].addElement(tagStringList[tag]);
								for (int i3 = 0; i3 < 13; i3++) {
									
									int i3m;
									if (i3 < 3)
										i3m = i3 + 10;
									else if (i3 < 12)
										i3m = i3 - 2;
									else
										i3m = 0;

									switch (tag) {
									case 0:
										data[t].addElement(df1.format(avg[i1][i2][i3m]));
										break;
									case 1:
										data[t].addElement(df1.format(sdev[i1][i2][i3m]));
										break;
									case 2:
										data[t].addElement(df1.format(med[i1][i2][i3m]));
										break;
									case 3:
										data[t].addElement(df1.format(min[i1][i2][i3m]));
										break;
									case 4:
										data[t].addElement(df1.format(max[i1][i2][i3m]));
										break;
									default:
										;
									}
								}
							}

			SimpleTableModel model = new SimpleTableModel(data[t], columns);
			JTable table = new JTable(model);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			for (int c = 0; c < 15; c++) 
				{ TableColumn col = table.getColumnModel().getColumn(0);
			col.setPreferredWidth((c==0) ? 200:75 ); }
			
			
			JLabel label = new JLabel();
			label.setText(title + " (" + tscs[t].units + ") - " + tscs[t].fileName);
			panel.add(label);
			panel.add(table.getTableHeader(), BorderLayout.NORTH);
			panel.add(table);
			
			
		}
//		JLabel label = new JLabel();
		//label.setText(tscs[0].fileName + " (" + tscs[0].units + ")");
		//panel.add(label);

		scrollPane.setViewportView(panel);
		scrollPane.setMinimumSize(new Dimension(790, 550));
		scrollPane.setPreferredSize(new Dimension(790, 550));
		add(scrollPane);
		scrollPane.validate();
		
	}
}

class SimpleTableModel extends AbstractTableModel {

	protected Vector<String> data;
	protected Vector<String> columnNames;

	public SimpleTableModel(Vector<String> datain, Vector<String> columnin) {
		data = datain;
		columnNames = columnin;
	}

	public int getRowCount() {
		return data.size() / getColumnCount();
	}

	public int getColumnCount() {
		return columnNames.size();
	}

	public String getColumnName(int columnIndex) {
		String colName = "";

		if (columnIndex <= getColumnCount())
			colName = (String) columnNames.elementAt(columnIndex);

		return colName;
	}

	public Class getColumnClass(int columnIndex) {
		return String.class;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return (String) data.elementAt((rowIndex * getColumnCount()) + columnIndex);
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		data.setElementAt((String) aValue, ((rowIndex * getColumnCount()) + columnIndex));
		// return;

	}

}
