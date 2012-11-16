package gov.ca.water.calgui;

import gov.ca.water.calgui.results.DSSGrabber;
import gov.ca.water.calgui.results.Report;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.swixml.SwingEngine;

public class ReportAction implements ActionListener {
	private final JFrame desktop;
	private final SwingEngine swix;
	private final DSSGrabber dss_Grabber;
	private final JList lstScenarios;

	public ReportAction(JFrame desktop, SwingEngine swix) {
		this.desktop = desktop;
		this.swix = swix;

		lstScenarios = (JList) swix.find("SelectedList");
		dss_Grabber = new DSSGrabber(lstScenarios);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int displayCount;
		// TODO Auto-generated method stub
		if (e.getActionCommand().startsWith("AC_PresetClear")) {

			JPanel presets = (JPanel) swix.find("presets");
			Component[] components = presets.getComponents();
			for (int i = 0; i < components.length; i++) {
				if (components[i] instanceof JCheckBox) {
					JCheckBox c = (JCheckBox) components[i];
					c.setSelected(false);
				}
			}

			JPanel WMA = (JPanel) swix.find("WMA");
			components = WMA.getComponents();
			for (int i = 0; i < components.length; i++) {
				if (components[i] instanceof JCheckBox) {
					JCheckBox c = (JCheckBox) components[i];
					c.setSelected(false);
				}
			}

		} else if (e.getActionCommand().startsWith("AC_ShortageClear")) {

			JPanel shortage = (JPanel) swix.find("shortage");
			Component[] components = shortage.getComponents();
			for (int i = 0; i < components.length; i++) {
				if (components[i] instanceof JCheckBox) {
					JCheckBox c = (JCheckBox) components[i];
					c.setSelected(false);
				}
			}

			// / delta flow criteria
		} else if (e.getActionCommand().startsWith("AC_DfcClear")) {

			JPanel delta_flow_criteria = (JPanel) swix.find("delta_flow_criteria");
			Component[] components = delta_flow_criteria.getComponents();
			for (int i = 0; i < components.length; i++) {
				if (components[i] instanceof JCheckBox) {
					JCheckBox c = (JCheckBox) components[i];
					c.setSelected(false);
				}
			}

		} else if (e.getActionCommand().startsWith("AC_GenReport")) {

			if (((JTextField) swix.find("tfReportFILE1")).getText().isEmpty()
			        || ((JTextField) swix.find("tfReportFILE2")).getText().isEmpty()
			        || ((JTextField) swix.find("tfReportFILE3")).getText().isEmpty()) {
				JOptionPane.showMessageDialog(null, "You must specify the source DSS files and the output PDF file", "Error",
				        JOptionPane.ERROR_MESSAGE);
			} else {
				Report report = null;
				// dialog.setVisible(false);
				try {
					// Create an inputstream from template file;
					FileInputStream fin = new FileInputStream(((JTextField) swix.find("tfTemplateFILE")).getToolTipText());
					BufferedReader br = new BufferedReader(new InputStreamReader(fin));
					// Open the template file
					System.out.println(((JTextField) swix.find("tfTemplateFILE")).getToolTipText());
					String theText = br.readLine() + "\n";
					theText = theText + br.readLine() + "\n";
					theText = theText + br.readLine() + "\n";
					br.readLine();
					theText = theText + "FILE_BASE\t" + ((JTextField) swix.find("tfReportFILE1")).getToolTipText() + "\n";
					br.readLine();
					theText = theText + "NAME_BASE\t\"" + ((JTextField) swix.find("tfReportNAME1")).getText() + "\"\n";
					br.readLine();
					theText = theText + "FILE_ALT\t" + ((JTextField) swix.find("tfReportFILE2")).getToolTipText() + "\n";
					br.readLine();
					theText = theText + "NAME_ALT\t\"" + ((JTextField) swix.find("tfReportNAME2")).getText() + "\"\n";
					br.readLine();
					theText = theText + "OUTFILE\t" + ((JTextField) swix.find("tfReportFILE3")).getToolTipText() + "\n";
					br.readLine();
					theText = theText + "NOTE\t\"" + ((JTextArea) swix.find("taReportNOTES")).getText() + "\"\n";
					br.readLine();
					theText = theText + "ASSUMPTIONS\t\"" + ((JTextArea) swix.find("taReportASSUMPTIONS")).getText() + "\"\n";
					br.readLine();
					theText = theText + "MODELER\t\"" + ((JTextField) swix.find("tfReportMODELER")).getText() + "\"\n";

					theText = theText + "TABLE_FONT_SIZE\t" + ((JTextField) swix.find("tfFontSize")).getText() + "\n";

					System.out.println(theText);
					String aLine = br.readLine();
					while (aLine != null) {
						theText = theText + aLine + "\n";
						aLine = br.readLine();
					}
					theText = theText + "\n";
					ByteArrayInputStream bs = new ByteArrayInputStream(theText.getBytes());
					try {
						System.out.println(theText);
						report = new Report(bs, ((JTextField) swix.find("tfReportFILE3")).getToolTipText(), desktop);
						report.execute();
						// report = new Report(bs);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

		} else if (e.getActionCommand().startsWith("Rep_All")) {
			JPanel controls2 = (JPanel) swix.find("controls2");
			Component[] components = controls2.getComponents();
			for (int i = 0; i < components.length; i++) {
				if (components[i] instanceof JCheckBox) {
					JCheckBox c = (JCheckBox) components[i];
					String cName = c.getName();
					if (cName != null) {
						if (cName.startsWith("RepchkMon")) {
							c.setSelected(true);
						}
					}
				}
			}
		} else if (e.getActionCommand().startsWith("Rep_ClearMonths")) {
			JPanel controls2 = (JPanel) swix.find("controls2");
			Component[] components = controls2.getComponents();
			for (int i = 0; i < components.length; i++) {
				if (components[i] instanceof JCheckBox) {
					JCheckBox c = (JCheckBox) components[i];
					String cName = components[i].getName();
					if (cName != null) {
						if (cName.startsWith("RepchkMon")) {
							c.setSelected(false);
						}
					}
				}
			}
		}

		else if (e.getActionCommand().startsWith("Rep_AddList")) {

			// Store previous list items
			JList lstReports = (JList) swix.find("lstReports");
			int size = lstReports.getModel().getSize(); // 4
			int n;
			n = 0;
			String[] lstArray = new String[size];
			for (int i = 0; i < size; i++) {
				Object item = lstReports.getModel().getElementAt(i);
				if (item.toString() != " ") {
					lstArray[n] = item.toString();
					n = n + 1;
				}
			}

			String[] lstArray1 = new String[n + 1];
			for (int i = 0; i < n; i++) {
				lstArray1[i] = lstArray[i];
			}

			String cSTOR = ";Locs-";
			String cSTORIdx = ";Index-";
			JPanel presets = (JPanel) swix.find("presets");
			Component[] components = presets.getComponents();
			for (int i = 0; i < components.length; i++) {
				if (components[i] instanceof JCheckBox) {
					JCheckBox c = (JCheckBox) components[i];
					String cName = c.getName();
					if (cName.startsWith("ckbp")) {
						boolean b = c.isSelected();
						if (b == true) {
							cSTOR = cSTOR + c.getText().trim() + ",";
							cSTORIdx = cSTORIdx + cName + ",";
						}
					}
				}

				lstArray1[n] = DisplayFrame.QuickState(swix) + cSTOR + cSTORIdx;

				// String[] reportNamesEG = {cDate};
				lstReports.setListData(lstArray1);
			}
			JPanel shortage = (JPanel) swix.find("shortage");
			components = shortage.getComponents();
			for (int i = 0; i < components.length; i++) {
				if (components[i] instanceof JCheckBox) {
					JCheckBox c = (JCheckBox) components[i];
					String cName = c.getName();
					if (cName.startsWith("ckbp")) {
						boolean b = c.isSelected();
						if (b == true) {
							cSTOR = cSTOR + c.getText().trim() + ",";
							cSTORIdx = cSTORIdx + cName + ",";
						}
					}
				}

				lstArray1[n] = DisplayFrame.QuickState(swix) + cSTOR + cSTORIdx;

				// String[] reportNamesEG = {cDate};
				lstReports.setListData(lstArray1);
			}

		}

		else if (e.getActionCommand().startsWith("Rep_ClearList")) {

			JList lstReports = (JList) swix.find("lstReports");
			String[] lstArray1 = new String[0];
			lstReports.setListData(lstArray1);

		}

		else if (e.getActionCommand().startsWith("Rep_LoadList")) {

			String aLine;
			Vector<String> data = new Vector<String>();

			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new CGRFileFilter());
			fc.setCurrentDirectory(new File(".//Config"));

			String dirname = ".//Config";
			File file = null;
			String filename = null;
			JPanel mainmenu = (JPanel) swix.find("mainmenu");
			int retval = fc.showOpenDialog(mainmenu);
			if (retval == JFileChooser.APPROVE_OPTION) {
				// ... The user selected a file, get it, use it.
				file = fc.getSelectedFile();
				filename = file.toString();

				try {

					// FileInputStream fin = new
					// FileInputStream(System.getProperty("user.dir") +
					// "\\Config\\reportlist.cgr");
					FileInputStream fin = new FileInputStream(filename);
					BufferedReader br = new BufferedReader(new InputStreamReader(fin));

					// Read until first non-comment line
					aLine = br.readLine();
					while (aLine.startsWith("!") && aLine != null) {
						aLine = br.readLine();
					}
					// aLine = br.readLine();// Skip title line;
					while (aLine != null) {
						data.add(aLine);
						aLine = br.readLine();
					}
					br.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				JList lstReports = (JList) swix.find("lstReports");
				lstReports.setListData(data);
			}

		}

		else if (e.getActionCommand().startsWith("Rep_SaveList")) {

			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new CGRFileFilter());
			fc.setCurrentDirectory(new File(".//Config"));

			File file = null;
			String filename = null;
			JPanel mainmenu = (JPanel) swix.find("mainmenu");
			int retval = fc.showSaveDialog(mainmenu);
			if (retval == JFileChooser.APPROVE_OPTION) {

				// ... The user selected a file, get it, use it.

				file = fc.getSelectedFile();
				filename = file.toString();
				if (!filename.toUpperCase().endsWith(".CGR") && !filename.endsWith("."))
					filename = filename + ".cgr";

				boolean saveFlag = true;
				if (new File(filename).exists())
					saveFlag = (JOptionPane.showConfirmDialog(mainmenu, "The display list file '" + filename
					        + "' already exists. Press OK to overwrite.", "CalLite GUI", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION);

				if (saveFlag) {
					OutputStream outputStream;
					try {
						// outputStream = new
						// FileOutputStream(System.getProperty("user.dir") +
						// "\\Config\\reportlist.cgr");
						outputStream = new FileOutputStream(filename);
					} catch (FileNotFoundException e2) {
						System.out.println("Cannot open " + filename);
						return;
					}

					// Store previous list items
					JList lstReports = (JList) swix.find("lstReports");
					int size = lstReports.getModel().getSize(); // 4
					int n;
					n = 0;
					String[] lstArray = new String[size];
					for (int i = 0; i < size; i++) {
						Object item = lstReports.getModel().getElementAt(i);
						if (item.toString() != " ") {
							lstArray[n] = item.toString();
							n = n + 1;
						}
					}
					try {

						PrintStream output = new PrintStream(outputStream);
						for (int i = 0; i < n; i++) {
							output.println(lstArray[i]);
						}

						output.close();
						outputStream.close();
					} catch (IOException ioe) {
						System.out.println("IOException");
					}
				}
			}
		}

		else if (e.getActionCommand().startsWith("Rep_DispAll")) {

			if (lstScenarios.getModel().getSize() == 0) {
				JOptionPane.showMessageDialog(null, "No scenarios loaded", "Error", JOptionPane.ERROR_MESSAGE);
			} else {
				displayCount = 0;
				JList list = (JList) swix.find("lstReports");
				for (int i = 0; i < list.getModel().getSize(); i++)
					DisplayFrame.displayFrame((String) (list.getModel().getElementAt(i)), swix, dss_Grabber, lstScenarios, desktop,
					        displayCount);

			}
		}

		else if (e.getActionCommand().startsWith("Rep_DispCur")) {

			if (lstScenarios.getModel().getSize() == 0) {
				JOptionPane.showMessageDialog(null, "No scenarios loaded", "Error", JOptionPane.ERROR_MESSAGE);
			} else if (((JList) swix.find("lstReports")).getSelectedValue() == null) {
				JOptionPane.showMessageDialog(null, "No display group selected", "Error", JOptionPane.ERROR_MESSAGE);
			} else {
				displayCount = 0;
				DisplayFrame.displayFrame((String) ((JList) swix.find("lstReports")).getSelectedValue(), swix, dss_Grabber,
				        lstScenarios, desktop, displayCount);
			}
		}
	}

}
