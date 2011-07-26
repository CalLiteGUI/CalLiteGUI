package com.limno.calgui;

/*
 * ProgressFrame - simple frame to show progress passed by method setText();
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.*;

import org.swixml.SwingEngine;

import rma.awt.JButtonGroup;

public class ScenarioFrame extends JFrame {
	private JLabel lblscen1;
	private JButtonGroup btngrp;
	private JRadioButton rdbAll;
	private JRadioButton rdbDiff;
	private JButton btnComp;
	private JList lstScen;

	public ScenarioFrame(String title, final SwingEngine swix) {

		super();

		// setUndecorated(true);
		// getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setPreferredSize(new Dimension(400, 300));
		setMinimumSize(new Dimension(400, 300));
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		setTitle(title);

		lblscen1 = new JLabel("Select Scenarios to Compare", SwingConstants.HORIZONTAL);
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		add(lblscen1, c);

		lstScen = new JList();
		lstScen.setVisibleRowCount(7);
		// populate with file list.
		File dir = new File(".//Scenarios");
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".cls");
			}
		};
		Vector filelist = new Vector(Arrays.asList(dir.list(filter)));
		lstScen.setListData(filelist);
		JScrollPane scrollingList = new JScrollPane(lstScen);
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 200;
		c.anchor = GridBagConstraints.WEST;
		add(scrollingList, c);
		scrollingList.setMinimumSize(new Dimension(100, 100));

		rdbAll = new JRadioButton("List All");
		rdbAll.setSelected(true);
		c.gridx = 0;
		c.gridy = 2;
		c.ipadx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.EAST;
		add(rdbAll, c);

		rdbDiff = new JRadioButton("List Differences");
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.EAST;
		add(rdbDiff, c);

		btngrp = new JButtonGroup();
		btngrp.add(rdbAll);
		btngrp.add(rdbDiff);

		btnComp = new JButton("Compare");
		c.gridx = 0;
		c.gridy = 4;
		c.fill = GridBagConstraints.NONE;
		c.ipadx = 00;
		c.anchor = GridBagConstraints.WEST;
		btnComp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Object[] selected = lstScen.getSelectedValues();
				String[] controls;
				String[] headers = null;
				String[][] scenmatrix = null;
				if (selected.length < 2) {
					JOptionPane
							.showMessageDialog(null, "You must select at least two scenario files to compare.", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					int option;
					if (rdbAll.isSelected()) {
						option = 1;
					} else {
						option = 2;
					}
					StringBuffer[] sbScen = new StringBuffer[selected.length];
					for (int i = 0; i < selected.length; i++) {
						File f = new File(System.getProperty("user.dir") + "\\Scenarios\\" + selected[i].toString());
						sbScen[i] = GUI_Utils.ReadScenarioFile(f);
					}

					// Populate Cross Tab Matrix (control info columns first)
					@SuppressWarnings("unused")
					String delims = "[|]";
					String NL = System.getProperty("line.separator");
					String sFull = sbScen[0].toString();
					String[] lines = sFull.split(NL);

					controls = new String[lines.length];
					scenmatrix = new String[lines.length][selected.length + 2];
					headers = new String[selected.length + 2];
					headers[0] = "Control";
					headers[1] = "Location";
					headers[2] = selected[0].toString();

					int i = 0;
					while (true) {
						String textinLine = lines[i];
						String comptext = "";
						if (textinLine == null | textinLine.equals("DATATABLEMODELS"))
							break;
						String[] tokens = textinLine.split(delims);

						String comp = tokens[0];
						String value = tokens[1];
						JComponent component = (JComponent) swix.find(comp);
						StringBuffer sbparents = new StringBuffer();
						sbparents = GUI_Utils.GetControlParents(component, sbparents);

						sbparents = GUI_Utils.ReverseStringBuffer(sbparents, "[|]");

						controls[i] = comp;

						if (component instanceof JCheckBox || component instanceof JRadioButton) {
							AbstractButton jab = (AbstractButton) component;
							comptext = comp + "| " + jab.getText();
						} else {
							if (component instanceof JTextField || component instanceof NumericTextField) {
								JLabel label = (JLabel) swix.find(comp + "_t");
								if (label != null)
									comptext = comp + "| " + label.getText();
								else
									comptext = comp;
							} else

								comptext = comp;
						}
						scenmatrix[i][0] = comptext;
						scenmatrix[i][1] = sbparents.toString();
						scenmatrix[i][2] = value;

						i++;
					}

					// Populate Cross Tab Matrix (other selected scenarios)
					for (i = 1; i < selected.length; i++) {
						sFull = sbScen[i].toString();
						lines = sFull.split(NL);

						int j = 0;
						while (true) {
							String textinLine = lines[j];
							if (j == lines.length - 1 | textinLine.equals("DATATABLEMODELS"))
								break;
							String[] tokens = textinLine.split(delims);

							String comp = tokens[0];
							String value = tokens[1];

							// Search for control index
							int index = GUI_Utils.FindInArray(controls, comp);
							// System.out.println(index);

							scenmatrix[index][i + 2] = value;

							j++;
						}
						headers[i + 2] = selected[i].toString();

					}

				}

				// Post-process scenmatrix to create final table
				
				
				ScenarioTable ScenTable = new ScenarioTable("CalLite 2.0 GUI - Scenario Comparison", scenmatrix, headers);
				ScenTable.setVisible(true);

			}
		});
		add(btnComp, c);

		pack();
		// setAlwaysOnTop(true);

		// TODO - Add close/cancel button

	}

}
