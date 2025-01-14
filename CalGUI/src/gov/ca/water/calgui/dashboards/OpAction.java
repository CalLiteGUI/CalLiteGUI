package gov.ca.water.calgui.dashboards;

import gov.ca.water.calgui.CalLiteHelp;
import gov.ca.water.calgui.RunUtils;
import gov.ca.water.calgui.utils.DataFileTableModel;
import gov.ca.water.calgui.utils.GUILinks;
import gov.ca.water.calgui.utils.GUIUtils;
import gov.ca.water.calgui.utils.PopulateDTable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

public class OpAction implements ActionListener {
	private final SwingEngine swix;
	private final Boolean[] regUserEdits;
	private DataFileTableModel[] dTableModels;
	private final GUILinks gl;
	private static Logger log = Logger.getLogger(OpAction.class.getName());
	private final int[] regFlags;

	public OpAction(JFrame desktop, SwingEngine swix, Boolean[] regUserEdits, DataFileTableModel[] dTableModels, GUILinks gl,
	        int[] regFlags) {
		this.swix = swix;
		this.regUserEdits = regUserEdits;
		this.dTableModels = dTableModels;
		this.gl = gl;
		this.regFlags = regFlags;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {

		if (ae.getActionCommand().startsWith("Op_Generate")) {

			JPanel mainmenu = (JPanel) swix.find("mainmenu");
			String scen = ((JTextField) swix.find("run_txfScen")).getText();
			RunUtils.doSingleAsWSIDI(ae, mainmenu, scen);

		} else if (ae.getActionCommand().startsWith("Op_TableEdit")) {
			TitledBorder title = null;
			JComponent component = (JComponent) ae.getSource();
			if (ae.getSource() instanceof JButton) {
				JButton btn = (JButton) ae.getSource();
				String titlestr = btn.getText();
				titlestr = titlestr.substring(5);
				title = BorderFactory.createTitledBorder(titlestr);
			}
			String cName = component.getName();
			// CheckBox in Regulations panel changed
			JPanel pan = (JPanel) swix.find("op_panTab");
			pan.setBorder(title);
			component = (JComponent) swix.find("scrOpValues");
			JTable table = (JTable) swix.find("tblOpValues");

			component.setVisible(true);
			component.setEnabled(true);
			// String cID = cName.substring(6);
			PopulateDTable.populate(cName, table, component, swix, regUserEdits, dTableModels, gl, regFlags);

			// pan.setBorder(title);
			// JComponent box = (JComponent) swix.find("Op_Box");
			// box.setVisible(true);
			component.setEnabled(true);
			table.setVisible(true);
			JPanel mainmenu = (JPanel) swix.find("mainmenu");
			mainmenu.revalidate();

		} else if (ae.getActionCommand().startsWith("Op_Copy")) {

			JTable table = (JTable) swix.find("tblOpValues");
			GUIUtils.copyTableValues(table);

		} else if (ae.getActionCommand().startsWith("Op_Read")) {
			JLabel lab = (JLabel) swix.find("op_WSIDI_Status");
			String oldText = lab.getText();
			JPanel pan = (JPanel) swix.find("op_panTab");
			Boolean cont = true;
			if (!oldText.contains("Unedited")) {
				cont = okToContinue(pan);
			}

			if (cont) {
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileNameExtensionFilter("Data table File *.table", "table"));
				fc.setCurrentDirectory(new File(".//"));

				// Get SWP File
				fc.setDialogTitle("Select WSI/DI SWP data table file");
				int retval = fc.showOpenDialog(fc);
				if (retval == JFileChooser.APPROVE_OPTION) {

					File fileSWP = fc.getSelectedFile();

					// Check if fileformat is correct
					cont = checkFileFormat(fileSWP);
					if (!cont) {
						JOptionPane.showMessageDialog(fc, "The selected file is not a valid SWP data table file.", "CalLite Gui",
						        JOptionPane.INFORMATION_MESSAGE);
					} else {

						// Get CVP File
						fc.setDialogTitle("Select WSI/DI CVP data table file");
						retval = fc.showOpenDialog(fc);
						if (retval == JFileChooser.APPROVE_OPTION) {
							File fileCVP = fc.getSelectedFile();

							// Check if fileformat is correct
							cont = checkFileFormat(fileCVP);
							if (!cont) {
								JOptionPane.showMessageDialog(fc, "The selected file is not a valid CVP data table file.",
								        "CalLite Gui", JOptionPane.INFORMATION_MESSAGE);
							} else {

								int tID = Integer.parseInt(gl.tableIDForCtrl("op_btn1"));
								dTableModels[tID] = new DataFileTableModel(fileSWP.getAbsolutePath(), tID);

								int tID1 = Integer.parseInt(gl.tableIDForCtrl("op_btn2"));
								dTableModels[tID1] = new DataFileTableModel(fileCVP.getAbsolutePath(), tID1);

								JTable table = (JTable) swix.find("tblOpValues");
								pan = (JPanel) swix.find("op_panTab");
								pan.setBorder(BorderFactory.createTitledBorder("SWP"));
								JComponent component = (JComponent) swix.find("scrOpValues");
								component.setVisible(true);
								component.setEnabled(true);
								table.setVisible(true);
								JPanel mainmenu = (JPanel) swix.find("mainmenu");
								mainmenu.revalidate();
								JComponent component1 = (JComponent) swix.find("scrOpValues");

								dTableModels = PopulateDTable.populate("op_btn2", table, component1, swix, regUserEdits,
								        dTableModels, gl, regFlags);

								dTableModels = PopulateDTable.populate("op_btn1", table, component1, swix, regUserEdits,
								        dTableModels, gl, regFlags);

								// Need to add first part of string
								String newPart = "WSI/DI read from [";
								newPart = newPart + fileCVP.getName() + "," + fileSWP.getName() + "]";
								lab.setText(newPart + " (Unedited)");
							}
						}
					}
				}

			}

		} else if (ae.getActionCommand().startsWith("Op_Default")) {
			JLabel lab = (JLabel) swix.find("op_WSIDI_Status");
			String oldText = lab.getText();
			JPanel pan = (JPanel) swix.find("op_panTab");
			Boolean cont = true;
			if (!oldText.contains("Unedited")) {
				cont = okToContinue(pan);
			}

			if (cont) {

				/*
				 * String comp = "op_btn1"; String stID = gl.tableIDForCtrl(comp); int tID = Integer.parseInt(stID);
				 * 
				 * DataFileTableModel tm = dTableModels[tID];
				 * 
				 * int size = tm.datafiles.length; if (size == 1) { tm.initVectors(); } else if (size == 2) { tm.initVectors2(); }
				 * 
				 * JTable table = (JTable) swix.find("tblOpValues"); table.repaint();
				 * 
				 * regUserEdits[tID] = false;
				 * 
				 * comp = "op_btn2"; stID = gl.tableIDForCtrl(comp); tID = Integer.parseInt(stID);
				 * 
				 * tm = dTableModels[tID];
				 * 
				 * size = tm.datafiles.length; if (size == 1) { tm.initVectors(); } else if (size == 2) { tm.initVectors2(); }
				 * 
				 * table.repaint();
				 */
				String hydDSSStrings[] = GUIUtils.getHydDSSStrings(swix);
				String defDir_Lookup = GUIUtils.defaultLookupDirectoryString();

				JPanel mainmenu = (JPanel) swix.find("mainmenu");
				JTable table = (JTable) swix.find("tblOpValues");
				pan.setBorder(BorderFactory.createTitledBorder("SWP"));
				JComponent component = (JComponent) swix.find("scrOpValues");
				component.setVisible(true);
				component.setEnabled(true);
				table.setVisible(true);
				mainmenu.revalidate();
				JComponent component1 = (JComponent) swix.find("scrOpValues");

				File fileSWP = new File(defDir_Lookup + hydDSSStrings[6]);
				int tID = Integer.parseInt(gl.tableIDForCtrl("op_btn1"));
				dTableModels[tID] = new DataFileTableModel(fileSWP.getAbsolutePath(), tID);

				File fileCVP = new File(defDir_Lookup + hydDSSStrings[5]);
				int tID1 = Integer.parseInt(gl.tableIDForCtrl("op_btn2"));
				dTableModels[tID1] = new DataFileTableModel(fileCVP.getAbsolutePath(), tID1);

				dTableModels = PopulateDTable
				        .populate("op_btn2", table, component1, swix, regUserEdits, dTableModels, gl, regFlags);

				dTableModels = PopulateDTable
				        .populate("op_btn1", table, component1, swix, regUserEdits, dTableModels, gl, regFlags);

				regUserEdits[tID] = false;
				regUserEdits[tID1] = false;

				String selHyd = hydDSSStrings[8];
				lab.setText(selHyd + " (Unedited)");
			}

		} else if (ae.getActionCommand().startsWith("Op_Paste")) {

			JTable table = (JTable) swix.find("tblOpValues");
			GUIUtils.pasteTableValues(table);
		}

		else {

			if (ae.getActionCommand().equals("AC_Help")) {

				JTabbedPane jtp = (JTabbedPane) swix.find("tabbedPane1");
				String label = jtp.getTitleAt(jtp.getSelectedIndex());
				CalLiteHelp calLiteHelp = new CalLiteHelp();
				calLiteHelp.showHelp(label);
			}
		}
	}

	public Boolean okToContinue(JComponent comp) {
		Boolean result = true;

		int n = JOptionPane.showConfirmDialog(comp,
		        "WSI/DI data tables have been modified.  Are you sure you wish to overwrite these changes?", "CalLite Gui",
		        JOptionPane.YES_NO_CANCEL_OPTION);

		switch (n) {
		case JOptionPane.CANCEL_OPTION:
			result = false;
			break;

		case JOptionPane.NO_OPTION:
			result = false;
			break;

		case JOptionPane.YES_OPTION:
			result = true;
			break;
		}
		return result;
	}

	public Boolean checkFileFormat(File f) {
		Boolean result = true;
		String aLine;

		try {

			FileInputStream fin = new FileInputStream(f);
			BufferedReader br = new BufferedReader(new InputStreamReader(fin));
			aLine = br.readLine();// Skip title line;
			aLine = br.readLine();// Skip header line;

			if (aLine != null) {
				aLine = br.readLine(); // first data line
				StringTokenizer st1 = new StringTokenizer(aLine, "\t| ");
				if (st1.countTokens() == 2) {
					result = true;
				} else {
					result = false;
				}

			}
			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

}
