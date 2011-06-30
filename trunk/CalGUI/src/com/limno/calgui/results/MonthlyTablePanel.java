package com.limno.calgui.results;

import hec.heclib.util.HecTime;
import hec.io.TimeSeriesContainer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

public class MonthlyTablePanel extends JPanel implements ActionListener, ComponentListener {
	JPanel panel;
	JScrollPane scrollPane;

	final String LINE_BREAK = "\n";
	final String CELL_BREAK = "\t";
	final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();

	public MonthlyTablePanel(String title, TimeSeriesContainer[] tscs, TimeSeriesContainer[] stscs, DSS_Grabber dss_Grabber, String sName) {

		super();

		panel = new JPanel();
		panel.setLayout((LayoutManager) (new BoxLayout(panel, BoxLayout.PAGE_AXIS)));

		scrollPane = new JScrollPane(panel);
		scrollPane.setPreferredSize(new Dimension(750, 600));

		DecimalFormat df1 = new DecimalFormat("#.#");
		HecTime ht = new HecTime();
		// Count forward to right month - hardcoded to 10 for now
		// TODO - match to input
		Vector<String> columns = new Vector<String>();
		columns.addElement("WY");
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
		if (dss_Grabber.originalUnits.equals("CFS")) {
			columns.addElement("Ann (TAF)");
		}

		for (int s = 0; s < tscs.length + (stscs == null ? 0 : stscs.length); s++) {

			String sLabel = sName;
			TimeSeriesContainer tsc;
			if (s < tscs.length) {
				tsc = tscs[s];
				sLabel = title;
			} else {
				tsc = stscs[s - tscs.length];
				if (sName.equals("")) {
					String[] parts = tsc.fullName.split("/");
					sLabel = parts[2] + "/" + parts[3];
				} else
					sLabel = sName;
			}
			JLabel label = new JLabel();
			label.setText(sLabel + " (" + tsc.units + ") - " + tsc.fileName);
			panel.add(label);

			int first = 0;
			ht.set(tsc.times[first]);
			while (ht.month() != 10) {
				first++;
				ht.set(tsc.times[first]);
			}

			Vector<String> data = new Vector<String>();
			double sum = 0;
			int wy = 0;
			for (int i = first; i < tsc.numberValues; i++) {
				ht.set(tsc.times[i]);
				int y = ht.year();
				int m = ht.month();
				wy = (m < 10) ? y : y + 1;
				if ((i - first) % 12 == 0) {
					if (i != first && dss_Grabber.originalUnits.equals("CFS"))
						data.addElement(df1.format(dss_Grabber.getAnnualTAF(s, wy - 1)));
					data.addElement(Integer.toString(wy));
				}
				sum = sum + tsc.values[i];
				data.addElement(df1.format(tsc.values[i]));
			}
			if (dss_Grabber.originalUnits.equals("CFS")) {
				data.addElement(df1.format(dss_Grabber.getAnnualTAF(s, wy)));
			}

			SimpleTableModel2 model = new SimpleTableModel2(data, columns);
			JTable table = new JTable(model);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			for (int c = 0; c < table.getColumnCount(); c++)
				table.getColumnModel().getColumn(c).setPreferredWidth((c == 0) ? 50 : 30);

			table.setCellSelectionEnabled(true);
			DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) table.getDefaultRenderer(String.class);
			renderer.setHorizontalAlignment(JLabel.RIGHT);

			addComponentListener((ComponentListener) this);
			panel.add(table.getTableHeader(), BorderLayout.NORTH);
			panel.add(table);

		}

		Box box = Box.createVerticalBox();

		box.add(scrollPane);
		JButton copy = new JButton("Copy to Clipboard");
		copy.setAlignmentX(LEFT_ALIGNMENT);
		copy.addActionListener((ActionListener) this);
		box.add(copy);
		add(box);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		JComponent component = (JComponent) e.getSource();
		if (component instanceof JButton) {
			JButton btn = (JButton) component;
			String cName = btn.getText();
			if (cName != null) {
				if (cName.startsWith("Copy")) {
					StringBuffer excelStr = new StringBuffer();

					Component[] components = panel.getComponents();

					for (int i = 0; i < components.length; i++) {
						if (components[i] instanceof JTable) {
							JTable table = (JTable) components[i];
							int numCols = table.getColumnCount();
							int numRows = table.getRowCount();

							// get column headers
							for (int k = 0; k < numCols; k++) {
								excelStr.append(table.getColumnModel().getColumn(k).getHeaderValue());
								if (k < numCols - 1) {
									excelStr.append(CELL_BREAK);
								}
							}
							excelStr.append(LINE_BREAK);

							// get cell values
							for (int j = 0; j < numRows; j++) {
								for (int k = 0; k < numCols; k++) {
									excelStr.append(escape(table.getValueAt(j, k)));
									if (k < numCols - 1) {
										excelStr.append(CELL_BREAK);
									}
								}
								excelStr.append(LINE_BREAK);
							}

							StringSelection sel = new StringSelection(excelStr.toString());
							CLIPBOARD.setContents(sel, sel);
						} else if (components[i] instanceof JLabel) {
							JLabel label = (JLabel) components[i];
							excelStr.append(label.getText());
							excelStr.append(LINE_BREAK);
						}
					}
				}
			}
		}
	}

	private String escape(Object cell) {
		return cell.toString().replace(LINE_BREAK, " ").replace(CELL_BREAK, " ");
	}

	class SimpleTableModel2 extends AbstractTableModel {
		protected Vector<String> data;
		protected Vector<String> columnNames;

		public SimpleTableModel2(Vector<String> datain, Vector<String> columnin) {
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

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentResized(ComponentEvent e) {
		// TODO Auto-generated method stub

		Dimension dim = super.getSize();
		int width = (int) (dim.width * 0.99);
		int height = (int) (dim.height * 0.90);
		scrollPane.setPreferredSize(new Dimension(width, height));
		scrollPane.revalidate();
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub

	}
}