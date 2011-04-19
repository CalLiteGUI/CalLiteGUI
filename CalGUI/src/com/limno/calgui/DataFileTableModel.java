package com.limno.calgui;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import java.io.*;
import java.util.*;

public class DataFileTableModel extends AbstractTableModel {

	protected Vector<String> data;
	protected Vector<String> columnNames ;  
	protected String datafile;
	protected String[] datafiles;
	protected EventListenerList listenerList = new EventListenerList();


	public DataFileTableModel(String f){
		//check if multiple file names included
		datafiles = f.split("[|]");
		int size = datafiles.length;
		
		if (size == 1) {
			// CASE 1: 1 file specified
			datafile = f;
			initVectors();  
		} else if (size == 2) {
			// CASE 2: 2 files specified
			initVectors2();  
		}

	}

	public void initVectors2() {

		String aLine ;
		data = new Vector<String>();
		columnNames = new Vector<String>();	
		String firstColumnName="";
		String secondColumnName="";
		ArrayList<String> allValues = new ArrayList<String>();
		ArrayList<String> allValues1 = new ArrayList<String>();

		for (int i = 0; i < datafiles.length; i++)
		{

			try {

				FileInputStream fin =  new FileInputStream(datafiles[i]);
				BufferedReader br = new BufferedReader(new InputStreamReader(fin));
			
				// Read until first non-comment line

				aLine = br.readLine();
				while (aLine.startsWith("!") && aLine != null ) {
					aLine = br.readLine(); 
				}

				aLine = br.readLine();// Skip title line;

				if (aLine != null){

					// Extract column names from second line

					StringTokenizer st1 = new StringTokenizer(aLine, "\t| ");
					if (st1.countTokens() < 3) {
						while(st1.hasMoreTokens()){
							columnNames.addElement(st1.nextToken());}
					} else{
						firstColumnName = (String) st1.nextToken();
						secondColumnName = (String) st1.nextToken();
						st1.nextToken();
						
					}
					// Extract data - first pass. Assumes we are reading in column-major order


					aLine = br.readLine();
					st1 = 	new StringTokenizer(aLine, "\t| ");
					if (st1.countTokens() < 3) {

						// CASE 1: TWO COLUMNS (month, value)

						while (aLine != null) {  
							StringTokenizer st2 = new StringTokenizer(aLine, "\t| ");
							
							
							//data.addElement(st2.nextToken());
							//data.addElement(st2.nextToken());
							allValues1.add(st2.nextToken());
							allValues1.add(st2.nextToken());
							aLine = br.readLine();
						}
						
					}

					else { 

						// CASE 2: THREE COLUMNS (year type, month, value) 
						//colct=columnNames.size();
						//String firstColumnName = (String) columnNames.get(colct-3);
						//String secondColumnName = (String) columnNames.get(colct-2);
						
						//columnNames.clear();
						
						//columnNames.addElement(firstColumnName);

						String lastColID = "-1";
						int rowCount = 0;

						while (aLine != null) {

							StringTokenizer st2 = new StringTokenizer(aLine, "\t| ");
							if (st2.countTokens() >= 3) {

								st2.nextToken();
								String aColID = st2.nextToken();
								String aValue = st2.nextToken();
								//System.out.println(Boolean.toString(lastColID == aColID)+" " + lastColID + ":" + aColID + ":" + aRowID + " " + aValue+ " " + Integer.toString(rowCount)+ " " + Integer.toString(columnNames.size()));
								if (Integer.parseInt(lastColID) < Integer.parseInt(aColID)) {
									columnNames.addElement(secondColumnName + aColID);
									lastColID = aColID;
									rowCount = 0;
								}

								rowCount++;
								allValues.add(aValue);
							}
							aLine = br.readLine();
						}
						for (int r = 0; r < rowCount; r ++) {
							data.addElement(allValues1.get(r*2));
							data.addElement(allValues1.get(r*2+1));
							//data.addElement(Integer.toString(r+1));
							for (int c = 0; c < columnNames.size() - 2 ; c++) {
								//System.out.println(Integer.toString(r)+":"+Integer.toString(c)+":"+Integer.toString(r)+":"+Integer.toString(c*rowCount)+"="+Integer.toString(allValues.size()));

								data.addElement(allValues.get(c*rowCount+r));
							}}

					}

					br.close();  

				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	
	public void initVectors() {

		String aLine ;
		data = new Vector<String>();
		columnNames = new Vector<String>();	

		try {

			FileInputStream fin =  new FileInputStream(datafile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fin));


			// Read until first non-comment line

			aLine = br.readLine();
			while (aLine.startsWith("!") && aLine != null ) {
				aLine = br.readLine(); 
			}

			aLine = br.readLine();// Skip title line;

			if (aLine != null){

				// Extract column names from second line

				StringTokenizer st1 = new StringTokenizer(aLine, "\t| ");
				while(st1.hasMoreTokens())
					columnNames.addElement(st1.nextToken());

				// Extract data - first pass. Assumes we are reading in column-major order


				aLine = br.readLine();
				st1 = 	new StringTokenizer(aLine, "\t| ");
				if (st1.countTokens() < 3) {

					// CASE 1: TWO COLUMNS (month, value)

					while (aLine != null) {  
						StringTokenizer st2 = new StringTokenizer(aLine, "\t| ");
						data.addElement(st2.nextToken());
						data.addElement(st2.nextToken());
						aLine = br.readLine();
					}
				}

				else { 

					// CASE 2: THREE COLUMNS (year type, month, value) 

					String firstColumnName = (String) columnNames.get(1);
					String secondColumnName = (String) columnNames.get(0);
					columnNames.clear();
					columnNames.addElement(firstColumnName);

					String lastColID = "-1";
					int rowCount = 0;

					ArrayList<String> allValues = new ArrayList<String>();
					while (aLine != null) {

						StringTokenizer st2 = new StringTokenizer(aLine, "\t| ");
						if (st2.countTokens() >= 3) {
							
							String aColID = st2.nextToken();
							st2.nextToken();
							String aValue = st2.nextToken();
							//System.out.println(Boolean.toString(lastColID == aColID)+" " + lastColID + ":" + aColID + ":" + aRowID + " " + aValue+ " " + Integer.toString(rowCount)+ " " + Integer.toString(columnNames.size()));
							if (Integer.parseInt(lastColID) < Integer.parseInt(aColID)) {
								columnNames.addElement(secondColumnName + aColID);
								lastColID = aColID;
								rowCount = 0;
							}

							rowCount++;
							allValues.add(aValue);
						}
						aLine = br.readLine();
					}
					for (int r = 0; r < rowCount; r ++) {

						data.addElement(Integer.toString(r+1));
						for (int c = 0; c < columnNames.size() - 1 ; c++) {
							//System.out.println(Integer.toString(r)+":"+Integer.toString(c)+":"+Integer.toString(r)+":"+Integer.toString(c*rowCount)+"="+Integer.toString(allValues.size()));

							data.addElement(allValues.get(c*rowCount+r));
						}}

				}

				br.close();  

			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setVectors(String aLine) {

		if (aLine != null){
			//data = new Vector<String>();
			int rowid=0;
			int colid=0;

			StringTokenizer st1 = new StringTokenizer(aLine, ";");
			while(st1.hasMoreTokens()){
				
				StringTokenizer st2 = 	new StringTokenizer(st1.nextToken(), ",");
				while(st2.hasMoreTokens()){
					setValueAt(st2.nextToken(),rowid,colid);
					colid++;
					//data.addElement(st2.nextToken());
					//data.addElement(st2.nextToken());
				}
				rowid++;
				colid=0;
			}
		}
	}
	

	public int getRowCount() {
		return data.size() / getColumnCount();
	}

	public int getColumnCount(){
		return columnNames.size();
	}

	public String getColumnName(int columnIndex) {
		String colName = "";

		if (columnIndex <= getColumnCount())
			colName = (String)columnNames.elementAt(columnIndex);

		return colName;
	}

	public Class getColumnClass(int columnIndex){
		return String.class;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return (columnIndex != 0);
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return (String)data.elementAt
		( (rowIndex * getColumnCount()) + columnIndex);
	}

	public void addTableModelListener(TableModelListener l) {
		listenerList.add(TableModelListener.class, l);
	}

	public void removeTableModelListener(TableModelListener l) {
		listenerList.remove(TableModelListener.class, l);
	}

	public TableModelListener[] getTableModelListeners() {
		return (TableModelListener[])listenerList.getListeners(
				TableModelListener.class);
	}


	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		data.setElementAt((String) aValue, ( (rowIndex * getColumnCount()) + columnIndex));        
		fireTableCellUpdated(rowIndex, columnIndex);
		//return;
	}

	public void fireTableDataChanged() {
		fireTableChanged(new TableModelEvent(this));
	}
	public void fireTableCellUpdated(int row, int column) {
		fireTableChanged(new TableModelEvent(this, row, row, column));
	}

	public void fireTableChanged(TableModelEvent e) {
		// Guaranteed to return a non-null array
		Object  [] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==TableModelListener.class) {
				((TableModelListener)listeners[i+1]).tableChanged(e);
			}
		}
	}


	public void writeToFile(String outputFileName) {

		OutputStream outputStream;
		try {
			//outputStream = new FileOutputStream("Config_and_Lookup\\Lookup\\"+outputFileName+".table2");
			outputStream = new FileOutputStream(outputFileName);
		}
		catch (FileNotFoundException e2) {
			System.out.println("Cannot open output file");
			return;
		} 

		try {

			PrintStream output = new PrintStream(outputStream);

			output.println(outputFileName);
			if (columnNames.size() == 2) {
				output.println(columnNames.elementAt(0)+" "+columnNames.elementAt(1));
				for (int i = 1; i <= data.size() / 2; i++) {
					output.println(data.elementAt(i*2-2)+ " " + data.elementAt(i*2-1));
				}

			} else if (columnNames.size() == 6){
				output.println("year_type month day");
				for (int i = 1; i <= 5; i++) 
					for (int j = 0; j < data.size()/6; j++){
						output.println(Integer.toString(i) + " " + Integer.toString(j+1)+ " " + data.elementAt(j*6+i));
					}

				output.close();
				outputStream.close();
			}
		}
		catch (IOException ioe) {
			System.out.println("IOException");
		}

	}

	public void writeToFile2(String outputFileName1, String outputFileName2) {

		OutputStream outputStream1;
		OutputStream outputStream2;
		try {
			//outputStream = new FileOutputStream("Config_and_Lookup\\Lookup\\"+outputFileName+".table2");
			outputStream1 = new FileOutputStream(outputFileName1);
			outputStream2 = new FileOutputStream(outputFileName2);
		}
		catch (FileNotFoundException e2) {
			System.out.println("Cannot open output file");
			return;
		} 

		try {

			PrintStream output1 = new PrintStream(outputStream1);
			PrintStream output2 = new PrintStream(outputStream2);

			output1.println(outputFileName1);
			output2.println(outputFileName2);

				output1.println(columnNames.elementAt(0)+" "+columnNames.elementAt(1));
				for (int i = 1; i <= data.size() / 7; i++) {
					output1.println(data.elementAt(i*7-7)+ " " + data.elementAt(i*7-6));
				}

				output2.println("year_type month day");
				for (int i = 2; i <= 6; i++) 
					for (int j = 0; j < data.size()/7; j++){
						output2.println(Integer.toString(i-1) + " " + Integer.toString(j+1)+ " " + data.elementAt(j*7+i));
					}

				output1.close();
				outputStream1.close();
				output2.close();
				outputStream2.close();
		}
		catch (IOException ioe) {
			System.out.println("IOException");
		}

	}
	
	public Object[][] getTableData () {
		
		int nCol = columnNames.size();
	    int nRow = data.size()/nCol; 
	    Object[][] tableData = new Object[nRow][nCol];
	    for (int i = 0 ; i < nRow ; i++)
	        for (int j = 0 ; j < nCol ; j++)
	            tableData[i][j] = data.elementAt(i*nCol+j);
	    return tableData;
	}

}



