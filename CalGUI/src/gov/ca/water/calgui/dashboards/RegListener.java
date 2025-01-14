package gov.ca.water.calgui.dashboards;

import gov.ca.water.calgui.utils.DataFileTableModel;
import gov.ca.water.calgui.utils.GUILinks;
import gov.ca.water.calgui.utils.GUIUtils;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

public class RegListener implements ItemListener {
	private final SwingEngine swix;
	private Boolean[] RegUserEdits;
	private final DataFileTableModel[] dTableModels;
	private final GUILinks gl;
	private final ButtonGroup reg_btng1;
	private static Logger log = Logger.getLogger(RegListener.class.getName());
	private int[] RegFlags;

	public RegListener(SwingEngine swix, Boolean[] RegUserEdits, DataFileTableModel[] dTableModels, GUILinks gl,
	        ButtonGroup reg_btng1, int[] RegFlags) {
		this.swix = swix;
		this.RegUserEdits = RegUserEdits;
		this.dTableModels = dTableModels;
		this.gl = gl;
		this.reg_btng1 = reg_btng1;
		this.RegFlags = RegFlags;

	}

	@Override
	public void itemStateChanged(ItemEvent ie) {

		JComponent component = (JComponent) ie.getItem();
		// TODO: EXTERNALIZE
		// was "e.getItemSelected"
		String cName = component.getName();
		if (cName != null) {

			if (cName.startsWith("rdbRegQS")) {

				// Quick Select Radio Button Selected

				if (ie.getStateChange() == ItemEvent.SELECTED) {

					boolean enabled = (cName.equals("rdbRegQS_UD"));

					JButton btn = (JButton) swix.find("btnRegCopy");
					btn.setEnabled(enabled);
					btn = (JButton) swix.find("btnRegPaste");
					btn.setEnabled(enabled);

					if (RegFlags == null) {
						// RegFlags = new int[40];
						RegFlags = new int[50];
					}

					if (!cName.startsWith("rdbRegQS_UD")) {

						// Handling to set SJR controls based on LOD
						JRadioButton hydrdb = (JRadioButton) swix.find("hyd_rdb2005");
						if (hydrdb.isSelected()) {
							JRadioButton regrdb = (JRadioButton) swix.find("SJR_interim");
							regrdb.setSelected(true);
						} else {
							JRadioButton regrdb = (JRadioButton) swix.find("SJR_full");
							regrdb.setSelected(true);
						}
						GUIUtils.toggleEnComponentAndChildren(swix.find("regpan2c"), false);

						int iSel = 0;

						if (cName.startsWith("rdbRegQS_D1641")) {
							iSel = 1;
						} else if (cName.startsWith("rdbRegQS_1641BO")) {
							iSel = 2;
						} else if (cName.startsWith("rdbRegQS_D1485")) {
							iSel = 3;
						}

						Scanner input = null;
						try {
							input = new Scanner(new FileReader("Config\\GUI_LinksReg.table"));
						} catch (FileNotFoundException e) {
							log.debug("Cannot open input file Config\\GUI_LinksReg.table " + e.getMessage());
						}

						Vector<String> allLookups = new Vector<String>();

						int lineCount = 0;
						input.nextLine(); // Skip header line
						while (input.hasNextLine()) {
							String line = input.nextLine();
							allLookups.add(line);
							lineCount++;
						}
						input.close();

						String lookups[][];
						lookups = new String[lineCount][6];
						for (int i = 0; i < lineCount; i++) {
							String[] parts = allLookups.get(i).split("[\t]+");
							for (int j = 0; j < 6; j++) {
								if (parts[j].equals("null"))
									parts[j] = "";
								lookups[i][j] = parts[j];
							}
						}

						String ckbName;
						Boolean b;
						int rID;
						// ((JRadioButton) swix.find("SJR_full")).setSelected(true); // Hard Coded Selection P.Ho
						for (int i = 0; i < lineCount; i++) {
							ckbName = lookups[i][0];
							JCheckBox ckb = (JCheckBox) swix.find(ckbName);
							b = Boolean.valueOf(lookups[i][iSel + 1]);
							ckb.setSelected(b);
							rID = Integer.parseInt(gl.RIDForCtrl(ckbName));

							if (iSel == 1) {
								RegFlags[rID] = 1;
							} else if (iSel == 2) {
								RegFlags[rID] = 1;
							} else {
								RegFlags[rID] = 3;
							}

						}

						// Special Handling for Antioch and Chips
						rID = Integer.parseInt(gl.RIDForCtrl("ckbReg_AN"));
						RegFlags[rID - 1] = RegFlags[rID];

						((JCheckBox) swix.find("Dynamic_SJR")).setEnabled(enabled);

					} else {

						// GUIUtils.toggleEnComponentAndChildren(swix.find("regpan2c"), true);

						Scanner input = null;
						try {
							input = new Scanner(new FileReader("Config\\GUI_LinksReg.table"));
						} catch (FileNotFoundException e) {
							System.out.println("Cannot open input file Config\\GUI_LinksReg.table");
						}

						Vector<String> allLookups = new Vector<String>();

						int lineCount = 0;
						input.nextLine(); // Skip header line
						while (input.hasNextLine()) {
							String line = input.nextLine();
							allLookups.add(line);
							lineCount++;
						}
						input.close();

						String lookups[][];
						lookups = new String[lineCount][6];
						for (int i = 0; i < lineCount; i++) {
							String[] parts = allLookups.get(i).split("[\t]+");
							for (int j = 0; j < 6; j++) {
								if (parts[j].equals("null"))
									parts[j] = "";
								lookups[i][j] = parts[j];
							}
						}

						String ckbName;
						int rID;
						for (int i = 0; i < lineCount; i++) {
							ckbName = lookups[i][0];
							rID = Integer.parseInt(gl.RIDForCtrl(ckbName));

							if (RegFlags[rID] == 1) {
								String D1641 = lookups[i][2];
								String D1641BO = lookups[i][3];
								// check if option is not a valid option
								if (D1641.equals("FALSE") && D1641BO.equals("FALSE")) {
									RegFlags[rID] = 3;
								}
							} else if (RegFlags[rID] == 3) {
								String D1485 = lookups[i][4];
								// check if option is not a valid option
								if (D1485.equals("FALSE")) {
									RegFlags[rID] = 1;
								}
							} else {
								RegFlags[rID] = RegFlags[rID];
							}
						}

						// Special Handling for Antioch and Chips
						rID = Integer.parseInt(gl.RIDForCtrl("ckbReg_AN"));
						RegFlags[rID] = 3;
						RegFlags[rID - 1] = 3;

						JTable table = (JTable) swix.find("tblRegValues");
						Object obj = table.getModel();
						if (obj == DataFileTableModel.class) {

							DataFileTableModel tm = (DataFileTableModel) table.getModel();
							int tID = tm.tID;
							cName = gl.ctrlFortableID(Integer.toString(tID));
							Boolean isSelect = ie.getStateChange() == ItemEvent.SELECTED;
							RegulationSetup.SetRegCheckBoxes(swix, RegUserEdits, dTableModels, gl, reg_btng1, cName, isSelect,
							        "null", RegFlags);
						}
					}
					GUIUtils.toggleEnComponentAndChildren(swix.find("regpan1"), enabled);
					GUIUtils.toggleEnComponentAndChildren(swix.find("regpan2"), enabled);
					GUIUtils.toggleEnComponentAndChildren(swix.find("regpan2b"), false);
					GUIUtils.toggleEnComponentAndChildren(swix.find("regpan3"), enabled);
					GUIUtils.toggleVisComponentAndChildren(swix.find("tblRegValues"), enabled);
					GUIUtils.toggleEnComponentAndChildren(swix.find("regpan2b"), true, JLabel.class);
					((JCheckBox) swix.find("Dynamic_SJR")).setEnabled(enabled);
					((JRadioButton) swix.find("btnReg1641")).setEnabled(enabled);
					((JRadioButton) swix.find("btnReg1485")).setEnabled(enabled);
					((JRadioButton) swix.find("btnRegUD")).setEnabled(enabled);
				}
				JCheckBox jbtn = (JCheckBox) swix.find("ckbReg_TRNTY");
				jbtn.setEnabled(false);
				jbtn = (JCheckBox) swix.find("ckbReg_PUMP");
				jbtn.setEnabled(false);

				JPanel pan = (JPanel) swix.find("reg_panTab");
				TitledBorder title = BorderFactory.createTitledBorder("");

				pan.setBorder(title);
				GUIUtils.toggleEnComponentAndChildren(swix.find("regpan2b"), true, JLabel.class);

			} else if (cName.startsWith("Dynamic_SJR")) {
				// 'Grey out all radio button options on this SJR panel if Dynamic SJR is OFF'
				JCheckBox ckb = (JCheckBox) swix.find("Dynamic_SJR");
				Boolean b = ckb.isSelected();
				if (b == false) {
					GUIUtils.toggleSelComponentAndChildren(swix.find("regpan2b"), b, JCheckBox.class);
				}
				GUIUtils.toggleEnComponentAndChildren(swix.find("regpan2b"), b);
				GUIUtils.toggleEnComponentAndChildren(swix.find("regpan2c"), b);
				ckb.setEnabled(true);

			} else if (cName.startsWith("ckbReg")) {
				// CheckBox in Regulations panel changed
				Boolean isSelect = ie.getStateChange() == ItemEvent.SELECTED;

				// if (cName.equals("ckbReg_TRNTY")) {
				// } else if (cName.equals("ckbReg_PUMP")) {
				// } else {

				// DKR 19Sept2014 Added logic to only allow Roe trigger when X2 requirements are enabled
				if (cName.equals("ckbReg_X2")) {
					JCheckBox jbtn = (JCheckBox) swix.find("ckbReg_X2");
					boolean b = jbtn.isSelected();
					jbtn = (JCheckBox) swix.find("ckbReg_X2ROE");
					jbtn.setSelected(b);
					jbtn.setEnabled(b);

				}

				RegulationSetup
				        .SetRegCheckBoxes(swix, RegUserEdits, dTableModels, gl, reg_btng1, cName, isSelect, "null", RegFlags);
				// }

				/*
				 * JRadioButton btn = (JRadioButton) swix.find("rdbRegQS_UD"); boolean enabled = btn.isEnabled(); ((JRadioButton)
				 * swix.find("btnReg1641")).setEnabled(enabled); ((JRadioButton) swix.find("btnReg1485")).setEnabled(enabled);
				 * ((JRadioButton) swix.find("btnRegUD")).setEnabled(enabled); ((JRadioButton)
				 * swix.find("btnReg1641")).setVisible(enabled); ((JRadioButton) swix.find("btnReg1485")).setVisible(enabled);
				 * //((JRadioButton) swix.find("btnRegUD")).setVisible(enabled);
				 */

			} else if (cName.startsWith("btnRegUD")) {
				// do not allow user edits to tables
				JTable table = (JTable) swix.find("tblRegValues");

				if (ie.getStateChange() == ItemEvent.SELECTED) {
					JComponent scr = (JComponent) swix.find("scrRegValues");
					if (scr.isVisible()) {
						table.setCellSelectionEnabled(true);
						table.setEnabled(true);

						DataFileTableModel tm = (DataFileTableModel) table.getModel();
						int tID = tm.tID;
						if (RegUserEdits == null) {
							RegUserEdits = new Boolean[20];
						}
						RegUserEdits[tID] = true;

						String cName1 = gl.ctrlFortableID(Integer.toString(tID));
						JCheckBox ckb = (JCheckBox) swix.find(cName1);
						String ckbtext = ckb.getText();
						String[] ckbtext1 = ckbtext.split(" - ");
						ckbtext = ckbtext1[0];
						ckb.setText(ckbtext + " - User Def.");

						int rID = Integer.parseInt(gl.RIDForCtrl(cName1));
						RegFlags[rID] = 2;

						((JRadioButton) swix.find("btnReg1641")).setEnabled(true);
						((JRadioButton) swix.find("btnReg1485")).setEnabled(true);
						((JRadioButton) swix.find("btnRegUD")).setEnabled(true);

						RegulationSetup.SetRegCheckBoxes(swix, RegUserEdits, dTableModels, gl, reg_btng1, cName1, true, "null",
						        RegFlags);

					} else {

						JPanel pan = (JPanel) swix.find("reg_panTab");
						TitledBorder b = (TitledBorder) pan.getBorder();
						String title = b.getTitle();
						JPanel mainmenu = (JPanel) swix.find("mainmenu");
						JOptionPane.showMessageDialog(mainmenu, title + " inputs are not user-specifiable at this time.");
						JRadioButton rdb1 = (JRadioButton) swix.find("reg_rdbD1641");
						rdb1.setSelected(true);
						rdb1.revalidate();

					}
				}
			}
		}
		GUIUtils.toggleEnComponentAndChildren(swix.find("regpan2b"), true, JLabel.class);

	}
}
