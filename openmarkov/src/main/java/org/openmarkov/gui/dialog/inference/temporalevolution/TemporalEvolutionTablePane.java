/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.inference.temporalevolution;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.gui.localize.StringDatabase;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Table to show temporal evolution of temporal variables
 *
 * @author myebra
 */
@SuppressWarnings("serial") public class TemporalEvolutionTablePane extends JScrollPane {

	private JTable table;

	public TemporalEvolutionTablePane(Map<Variable, TablePotential> temporalEvolution, ProbNet expandedNetwork,
			Variable variableOfInterest, List<Variable> conditioningVariables, int numSlices, boolean isUtility,
			boolean isCumulative) {
		super();

		Map<Integer, double[]> temporalEvolutionValues = new LinkedHashMap<>();
		Set<Variable> variables = temporalEvolution.keySet();
		int timeSlice = 0;
		for (int i = 0; i < variables.size(); i++) {
			boolean found = false;
			while (!found) {
				for (Variable variable : variables) {
					if (variable.getTimeSlice() == timeSlice) {
						found = true;
						temporalEvolutionValues.put(timeSlice, temporalEvolution.get(variable).getValues());
					}
				}
				timeSlice++;
			}
		}
		int numColumns = timeSlice + conditioningVariables.size() + 1;
		if (isUtility) {
			numColumns--;
		}

		int numRows = variableOfInterest.getNumStates();
		for (int i = 0; i < conditioningVariables.size(); i++) {
			numRows *= conditioningVariables.get(i).getNumStates();
		}

		NonEditableModel model = new NonEditableModel();
		table = new JTable(model);

		model.setColumnCount(numColumns);
		model.setNumRows(numRows);

		final Object[][] info = new Object[numRows][numColumns];

		// Fill conditioning variables
		int lastColumnIndex = 0;
		for (lastColumnIndex = 0; lastColumnIndex < conditioningVariables.size(); lastColumnIndex++) {
			String columnName = conditioningVariables.get(lastColumnIndex).getName();
			table.getColumnModel().getColumn(lastColumnIndex).setHeaderValue(columnName);

			for (int i = 0; i < numRows; i++) {
				int stateIndex = (i / variableOfInterest.getNumStates()) % conditioningVariables.get(lastColumnIndex)
						.getNumStates();
				String stateName = conditioningVariables.get(lastColumnIndex).getStateName(stateIndex);
				model.setValueAt(stateName, i, lastColumnIndex);
			}
		}

		if (!isUtility) {
			// States of the Variable of Interest
			table.getColumnModel().getColumn(lastColumnIndex).setHeaderValue(StringDatabase.getUniqueInstance().
					getString("TemporalEvolutionResultDialog.States.Label"));
			for (int i = 0; i < numRows; i++) {
				info[i][lastColumnIndex] = variableOfInterest.getStateName(i % variableOfInterest.getNumStates());
				model.setValueAt(variableOfInterest.getStateName(i % variableOfInterest.getNumStates()), i, lastColumnIndex);
			}
			lastColumnIndex++;
		}

		TableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
			private DecimalFormat formatter = new DecimalFormat("0.###", new DecimalFormatSymbols(Locale.US));

			@Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				if (value instanceof Double) {
					if ((Double) value <= 100.0) {
						value = formatter.format((Double) value);
					} else {
						value = Math.round((Double) value);
					}
				}
				if (column == 0) {
					setBackground(new Color(220, 220, 220));
				} else {
					setBackground(Color.WHITE);
				}
				setForeground(Color.BLACK);
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
		};

		// Fill data
		Double values[] = new Double[numRows];
		for (int i = 0; i < values.length; i++) {
			values[i] = 0.0;
		}
		for (int cycle = 0; cycle < timeSlice; ++cycle) { // column
			int columnIndex = lastColumnIndex + cycle;
			table.getColumnModel().getColumn(columnIndex).setHeaderValue(cycle);
			table.getColumnModel().getColumn(columnIndex).setCellRenderer(cellRenderer);
			for (int i = 0; i < numRows; i++) {// row
				if (isUtility && isCumulative) {
					if (temporalEvolutionValues.containsKey(cycle)) {
						values[i] += temporalEvolutionValues.get(cycle)[i];
					}
					// cell(row, column) = cell(i+1, j+1)
					info[i][columnIndex] = values[i];
					model.setValueAt(values[i], i, columnIndex);
				} else {
					if (temporalEvolutionValues.containsKey(cycle)) {
						values[i] = temporalEvolutionValues.get(cycle)[i];
					} else {
						values[i] = 0.0;
					}
					// cell(row, column) = cell(i+1, j+1)
					info[i][columnIndex] = values[i];
					model.setValueAt(values[i], i, columnIndex);
				}
			}
		}

		//table.getTableHeader().setReorderingAllowed(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setViewportView(table);
		setAutoscrolls(true);
	}

	public JTable getTable() {
		return table;
	}

	public class NonEditableModel extends DefaultTableModel {
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}
}
