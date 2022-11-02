/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.inference.temporalevolution;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.FileOutputStream;
import java.io.IOException;

public class TemporalEvolutionReport {

	// Methods

	/**
	 * creates a new xlsx book with temporal evolution of a variable
	 *
	 * @throws IOException
	 */
	public void write(String filename, JTable jtable) throws IOException {
		XSSFWorkbook hwb = new XSSFWorkbook();
		String sheetName = filename;
		XSSFSheet sheetTable = hwb.createSheet("Temporal Evolution Report");
		// first row, column names
		Row rowIndexes = sheetTable.createRow(0);

		for (int i = 0; i < jtable.getColumnCount(); i++) {
			rowIndexes.createCell(i).setCellValue(jtable.getColumnModel().getColumn(i).getHeaderValue().toString());
		}
		// fill data
		for (int i = 0; i < jtable.getRowCount(); i++) {
			Row row = sheetTable.createRow(i + 1);
			for (int j = 0; j < jtable.getColumnCount(); j++) {
				if (jtable.getValueAt(i, j) instanceof String) {
					row.createCell(j).setCellValue((String) jtable.getValueAt(i, j));
				} else if (jtable.getValueAt(i, j) instanceof Integer) {
					row.createCell(j).setCellValue((Integer) jtable.getValueAt(i, j));
				} else {
					row.createCell(j).setCellValue((Double) jtable.getValueAt(i, j));
				}
			}

		}

		String targetFilename = filename.endsWith(".xlsx") ? filename : filename + ".xlsx";
		FileOutputStream fileOut = new FileOutputStream(targetFilename);
		hwb.write(fileOut);
		fileOut.close();
	}

	/**
	 * creates a new xls book with temporal evolution of a variable
	 *
	 * @throws IOException
	 */
//	public void write(String filename, JTable jtable) throws IOException {
//		HSSFWorkbook hwb = new HSSFWorkbook();
//		String sheetName = filename;
//		HSSFSheet sheetTable = hwb.createSheet("Temporal Evolution Report");
//		// first row, column names
//		HSSFRow rowIndexes = sheetTable.createRow(0);
//		rowIndexes.createCell(0).setCellValue("");
//
//		for (int i = 1; i < jtable.getColumnCount(); i++) {
//			rowIndexes.createCell(i + 1).setCellValue(jtable.getColumnModel().getColumn(i).getHeaderValue().toString());
//		}
//		// fill data
//		for (int i = 0; i < jtable.getRowCount(); i++) {
//			HSSFRow row = sheetTable.createRow(i + 1);
//			for (int j = 0; j < jtable.getColumnCount(); j++) {
//				if (jtable.getValueAt(i, j) instanceof String) {
//					row.createCell(j).setCellValue((String) jtable.getValueAt(i, j));
//				} else if (jtable.getValueAt(i, j) instanceof Integer) {
//					row.createCell(j).setCellValue((Integer) jtable.getValueAt(i, j));
//				} else {
//					row.createCell(j).setCellValue((Double) jtable.getValueAt(i, j));
//				}
//			}
//
//		}
//
//		String targetFilename = filename.endsWith(".xls") ? filename : filename + ".xls";
//		FileOutputStream fileOut = new FileOutputStream(targetFilename);
//		hwb.write(fileOut);
//		fileOut.close();
//	}
}
