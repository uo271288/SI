/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.openmarkov.core.action.DecisionCriteriaEdit;
import org.openmarkov.core.action.DecisionCriterionUnitEdit;
import org.openmarkov.core.action.StateAction;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.dialog.node.StandardDomainsDialog;
import org.openmarkov.gui.localize.StringDatabase;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

@SuppressWarnings("serial") public class DecisionCriteriaTablePanel extends AdvancedPropertiesTablePanel {

	JButton standardCriteriaButton;
	private ProbNet probNet;
	private Window owner;
	/**
	 * Each time an agent has been edited the corresponding edit would be stored
	 */
	//private List<PNEdit> edits = new ArrayList<PNEdit>();

	private StringDatabase stringDatabase = StringDatabase.getUniqueInstance();

	public DecisionCriteriaTablePanel(String[] newColumns, ProbNet probNet, Window owner) {
		super(newColumns, new Object[0][0], StringDatabase.getUniqueInstance().
				getString("NetworkAdvancedPanel.DecisionCriteria.ValuesTable.Columns.Id.Prefix"));

		this.probNet = probNet;
		this.owner = owner;
	}

	@Override public void tableChanged(TableModelEvent tableEvent) {
		int column = tableEvent.getColumn();
		int row = tableEvent.getLastRow();
		if (tableEvent.getType() == TableModelEvent.UPDATE) {

			switch (column) {
			case 1:
				String criterionName = (String) dataTable[row][column - 1];
				String newName = (String) ((AdvancedPropertiesTableModel) tableEvent.getSource())
						.getValueAt(row, column);
				dataTable[row][column - 1] = newName;
				if (criterionName != newName) {
					DecisionCriteriaEdit criteriaEdit = new DecisionCriteriaEdit(probNet, StateAction.RENAME,
							probNet.getDecisionCriteria().get(row), newName);
					try {
						probNet.doEdit(criteriaEdit);
						//edits.add(criteriaEdit);
					} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e1) {
						JOptionPane.showMessageDialog(this, stringDatabase.getString(e1.getMessage()),
								stringDatabase.getString("ConstraintViolationException"), JOptionPane.ERROR_MESSAGE);
						dataTable[row][column - 1] = criterionName;
					}
				}

				break;
			case 2:
				criterionName = (String) dataTable[row][column - 2];
				String unitName = (String) dataTable[row][column - 1];
				String newUnitName = (String) ((AdvancedPropertiesTableModel) tableEvent.getSource())
						.getValueAt(row, column);
				dataTable[row][column - 1] = newUnitName;
				if (unitName != newUnitName) {
					DecisionCriterionUnitEdit criterionUnitEdit = new DecisionCriterionUnitEdit(probNet, criterionName,
							newUnitName);
					try {
						probNet.doEdit(criterionUnitEdit);
						//edits.add(criterionUnitEdit);
					} catch (DoEditException | ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;

			}

			setData(dataTable);
			valuesTable.getSelectionModel().setSelectionInterval(row, row);
		}
	}

	@Override protected void actionPerformedAddValue() {

		String option = JOptionPane.showInputDialog(this, stringDatabase.getString("AddCriterion.Text"),
				stringDatabase.getString("AddCriterion.Title"), JOptionPane.QUESTION_MESSAGE);

		if (option != null) {
			int newIndex = valuesTable.getRowCount();

			DecisionCriteriaEdit criteriaEdit = new DecisionCriteriaEdit(probNet, StateAction.ADD,
					new Criterion(option), null);

			// doEdit
			try {
				probNet.doEdit(criteriaEdit);
				//edits.add(criteriaEdit);

			} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e1) {
				JOptionPane.showMessageDialog(this, stringDatabase.getString(e1.getMessage()),
						stringDatabase.getString("ConstraintViolationException"), JOptionPane.ERROR_MESSAGE);

			}
			/*
			 * getTableModel().insertRow(newIndex, new Object[]
			 * {getKeyString(newIndex), option });
			 * valuesTable.getSelectionModel().setSelectionInterval(newIndex,
			 * newIndex);
			 */

			// StringsWithProperties agents = probNet.getAgents();
			// setDataFromNetworkAgents(agents);
			List<Criterion> criteria = probNet.getDecisionCriteria();
			setDataFromCriteria(criteria);
			// getTableModel().insertRow(newIndex, new Object[]
			// {getKeyString(newIndex), option });
			valuesTable.getSelectionModel().setSelectionInterval(newIndex, newIndex);

			dataTable = new Object[valuesTable.getRowCount()][2];
			for (int i = 0; i < valuesTable.getRowCount(); i++) {
				dataTable[i][0] = valuesTable.getValueAt(i, 1);
				dataTable[i][1] = valuesTable.getValueAt(i, 2);
			}
			/*
			 * getTableModel().insertRow(newIndex, new Object[]
			 * {getKeyString(newIndex), option });
			 * //valuesTable.getSelectionModel().setSelectionInterval(newIndex,
			 * newIndex); valuesTable.setValueAt(option, newIndex, 1);
			 */
		}
	}

	@Override protected void actionPerformedRemoveValue() {
		int selectedRow = valuesTable.getSelectedRow();
		String criteriaName = (String) valuesTable.getValueAt(selectedRow, 1);

		DecisionCriteriaEdit criteriaEdit = new DecisionCriteriaEdit(probNet, StateAction.REMOVE,
				probNet.getDecisionCriteria().get(selectedRow), null);

		try {
			probNet.doEdit(criteriaEdit);
			//edits.add(criteriaEdit);
		} catch (DoEditException | ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// StringsWithProperties agents = probNet.getAgents();
		List<Criterion> criterias = probNet.getDecisionCriteria();
		setDataFromCriteria(criterias);
		valuesTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
		// dataTable = new Object [agents.getNames().size()][1];
		if (criterias != null) {
			dataTable = new Object[criterias.size()][2];
			for (int i = 0; i < valuesTable.getRowCount(); i++) {
				dataTable[i][0] = valuesTable.getValueAt(i, 1);
				dataTable[i][1] = valuesTable.getValueAt(i, 2);
			}
		}
	}

	@Override protected void actionPerformedUpValue() {
		int selectedRow = valuesTable.getSelectedRow();
		Object swapName = null, swapUnit = null;
		swapName = dataTable[selectedRow][0];
		dataTable[selectedRow][0] = dataTable[selectedRow - 1][0];
		dataTable[selectedRow - 1][0] = swapName;

		swapUnit = dataTable[selectedRow][1];
		dataTable[selectedRow][1] = dataTable[selectedRow - 1][1];
		dataTable[selectedRow - 1][1] = swapUnit;

		DecisionCriteriaEdit criteriaEdit = new DecisionCriteriaEdit(probNet, StateAction.UP,
				probNet.getDecisionCriteria().get(selectedRow), null);

		try {
			probNet.doEdit(criteriaEdit);
			//edits.add(criteriaEdit);
			setData(dataTable);
			/*
			 * swap = valuesTable.getValueAt(selectedRow, 1);
			 * valuesTable.setValueAt( valuesTable.getValueAt(selectedRow - 1,
			 * 1), selectedRow, 1); valuesTable.setValueAt(swap, selectedRow -
			 * 1, 1);
			 */
			valuesTable.getSelectionModel().setSelectionInterval(selectedRow - 1, selectedRow - 1);
		} catch (DoEditException | ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < valuesTable.getRowCount(); i++) {
			dataTable[i][0] = valuesTable.getValueAt(i, 1);
			dataTable[i][1] = valuesTable.getValueAt(i, 2);
		}

	}

	@Override protected void actionPerformedDownValue() {
		int selectedRow = valuesTable.getSelectedRow();
		Object swapName = null, swapUnit = null;
		swapName = dataTable[selectedRow][0];
		dataTable[selectedRow][0] = dataTable[selectedRow + 1][0];
		dataTable[selectedRow + 1][0] = swapName;

		swapUnit = dataTable[selectedRow][1];
		dataTable[selectedRow][1] = dataTable[selectedRow + 1][1];
		dataTable[selectedRow + 1][1] = swapUnit;

		DecisionCriteriaEdit criteriaEdit = new DecisionCriteriaEdit(probNet, StateAction.DOWN,
				probNet.getDecisionCriteria().get(selectedRow), null);
		try {
			probNet.doEdit(criteriaEdit);
			//edits.add(criteriaEdit);
			setData(dataTable);
			/*
			 * swap = valuesTable.getValueAt(selectedRow, 1);
			 * valuesTable.setValueAt( valuesTable.getValueAt(selectedRow + 1,
			 * 1), selectedRow, 1); valuesTable.setValueAt(swap, selectedRow +
			 * 1, 1);
			 */
			valuesTable.getSelectionModel().setSelectionInterval(selectedRow + 1, selectedRow + 1);
		} catch (DoEditException | ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < valuesTable.getRowCount(); i++) {
			dataTable[i][0] = valuesTable.getValueAt(i, 1);
			dataTable[i][1] = valuesTable.getValueAt(i, 2);
		}
	}

	/*
    Fixing issue https://bitbucket.org/cisiad/org.openmarkov.issues/issue/221/button-delete-in-node-properties-parents
    The remove button was always set to disabled, unless more than two parents were present
    We need to override the method from KeyTablePanel
    as in it we are not able to determine in which panel we are located and thus
    if the button needs to be enabled or not.
     */
	@Override public void valueChanged(ListSelectionEvent e) {
		super.valueChanged(e);

		// If there are two criteria, one can be deleted
		if (valuesTable.getRowCount() == 2) {
			removeValueButton.setEnabled(true);
		}

	}

	/**
	 * This method initializes buttonPanel.
	 *
	 * @return a new button panel.
	 */
	@Override protected JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.setName("DiscretizeTablePanel.buttonPanel");
			final GroupLayout groupLayout = new GroupLayout((JComponent) buttonPanel);
			groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(
					groupLayout.createSequentialGroup().addGroup(
							groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
									.addComponent(getStandardCriteriaButton(), GroupLayout.DEFAULT_SIZE, 55,
											Short.MAX_VALUE)
									.addComponent(getAddValueButton(), GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
									.addComponent(getDownValueButton(), GroupLayout.Alignment.LEADING,
											GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
									.addComponent(getUpValueButton(), GroupLayout.Alignment.LEADING,
											GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
									.addComponent(getRemoveValueButton(), GroupLayout.Alignment.LEADING,
											GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)).addContainerGap()));
			groupLayout.setVerticalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
					groupLayout.createSequentialGroup().addComponent(getStandardCriteriaButton()).addGap(5, 5, 5)
							.addComponent(getAddValueButton()).addGap(5, 5, 5).addComponent(getRemoveValueButton())
							.addGap(5, 5, 5).addComponent(getUpValueButton()).addGap(5, 5, 5)
							.addComponent(getDownValueButton()).addGap(5, 5, 5).addGap(48, 48, 48)));
			buttonPanel.setLayout(groupLayout);
		}
		return buttonPanel;
	}

	private Component getStandardCriteriaButton() {
		if (standardCriteriaButton == null) {
			standardCriteriaButton = new JButton();
			standardCriteriaButton.setName("KeyTablePanel.standardDomainButton");
			standardCriteriaButton.setText(StringDatabase.getUniqueInstance().getString("StandardCriteria.Text"));
			standardCriteriaButton.setVisible(true);
			standardCriteriaButton.setEnabled(true);
			standardCriteriaButton.addActionListener(new ActionListener() {

				@Override public void actionPerformed(ActionEvent e) {
					StandardCriteriaDialog dialog = new StandardCriteriaDialog(owner, probNet);

					if (dialog.requestValues() == StandardDomainsDialog.OK_BUTTON) {
						setDataFromCriteria(probNet.getDecisionCriteria());
					}

				}
			});
		}
		return standardCriteriaButton;
	}

}
