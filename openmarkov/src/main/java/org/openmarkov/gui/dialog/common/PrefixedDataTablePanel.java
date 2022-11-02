/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.RemoveLinkEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.localize.StringDatabase;
import org.openmarkov.gui.util.Utilities;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a key table with the following features:
 * <ul>
 * <li>Its elements aren't modifiable.</li>
 * <li>New elements can be added selecting them of a prefixed set.</li>
 * <li>An element of the prefixed set can be added only once.</li>
 * <li>The first column is treated as the rest of columns.</li>
 * <li>The information of a row (except the first column) can't be taken up or
 * down.</li>
 * <li>The rows can be removed.</li>
 * </ul>
 *
 * @author jmendoza
 * @author jlgozalo
 * @version 1.0 jlgozalo - change class modifier to public
 */
public class PrefixedDataTablePanel extends KeyTablePanel {

	/**
	 * Static field for serializable class.
	 */
	private static final long serialVersionUID = 2127072068749928448L;
	ArrayList<PNEdit> edits = new ArrayList<PNEdit>();
	/**
	 * Prefixed data.
	 */
	private Object[][] prefixedData = null;
	/**
	 * Array that contains the prefixed data that is not in the table.
	 */
	private Object[][] absentData = null;
	/**
	 * String that appears in the titlebar of the dialog box showed to add new
	 * rows.
	 */
	private String titleToSelectRows;
	private Node node;

	/**
	 * This is the default constructor
	 *
	 * @param newColumns           array of texts that appear in the header of the columns.
	 * @param newData              content of the cells (subset of prefixedData).
	 * @param newPrefixedData      content that can appears into the cells.
	 * @param newTitleToSelectRows title of the window where the user can select new rows.
	 */
	public PrefixedDataTablePanel(Node node, String[] newColumns, Object[][] newData, Object[][] newPrefixedData,
			String newTitleToSelectRows, boolean firstColumnHidden) {

		super(newColumns, new Object[0][0], false, false);
		this.node = node;
		prefixedData = newPrefixedData.clone();
		titleToSelectRows = newTitleToSelectRows;
		initialize();
		valuesTable.setFirstColumnHidden(firstColumnHidden);
		setData(newData);
	}

	private static Object[][] fillArrayWithNodes(List<Node> nodes) {

		int i, l;
		Object[][] result;
		l = nodes.size();
		result = new Object[l][2];
		for (i = 0; i < l; i++) {
			result[i][0] = "p_" + i; //internal name for the parent
			result[i][1] = nodes.get(i).getName();
		}

		return result;
	}

	/**
	 * Sets a new table model with new data.
	 *
	 * @param newData new data for the table.
	 */
	@Override public void setData(Object[][] newData) {

		data = newData.clone();
		tableModel = null;
		valuesTable.setModel(getTableModel());
		absentData = absentPrefixedData();
		setEnabledAddValue(absentData.length != 0);

	}

	/**
	 * Invoked when the button 'add' is pressed.
	 */
	@Override protected void actionPerformedAddValue() {

		int newIndex = 0;
		int i = 0;
		int l = 0;
		Object[][] newData = null;

		newIndex = valuesTable.getRowCount();
		if (absentData == null) {
			JOptionPane.showMessageDialog(Utilities.getOwner(this), "Ningún nodo disponible",
					stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.INFORMATION_MESSAGE);
		} else {
			newData = requestNewData();
			if (newData != null) {
				l = newData.length;
				for (i = 0; i < l; i++) {
					String name = (String) newData[i][1];
					for (PNEdit edit : edits) {
						if (((AddLinkEdit) edit).getNode1().getName().equals(name)) {
							try {
								node.getProbNet().getPNESupport().doEdit((AddLinkEdit) edit);
								tableModel.insertRow(newIndex + i, newData[i]);
								edits.remove(edit);
								break;
							} catch (DoEditException | NonProjectablePotentialException | WrongCriterionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								JOptionPane.showMessageDialog(Utilities.getOwner(this), e.getMessage(),
										stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
							}
						}

					}
				}
				valuesTable.getSelectionModel().setSelectionInterval(newIndex, newIndex);
				absentData = absentPrefixedData();
				setEnabledAddValue(absentData.length != 0);
			}
		}

	}

	/**
	 * This method request the user to select one or more new elements to add.
	 * The new elements are the subset of the prefixed set that aren't in the
	 * array 'data'.
	 *
	 * @return the elements that the user has selected or null if he/she has
	 * selected nothing.
	 */
	private Object[][] requestNewData() {

		Object[][] possibleData = absentData;
		KeyListSelectionDialog dialog = null;
		dialog = new KeyListSelectionDialog(Utilities.getOwner(this), titleToSelectRows, possibleData, columns);

		return (dialog.requestSelectRows() == KeyListSelectionDialog.OK_BUTTON) ? dialog.getSelectedRows() : null;

	}

	/**
	 * This method returns an array of arrays of strings whose elements are the
	 * prefixed ones that aren't in the array 'data'.
	 *
	 * @return the prefixed data that aren't in the array 'data'.
	 */
	private Object[][] absentPrefixedData() {
		List<Node> allNodes = node.getProbNet().getNodes();
		List<Node> nodes = new ArrayList<Node>();
		edits.clear();

		for (Node otherNode : allNodes) {
			if (!node.getParents().contains(otherNode) && otherNode != node) {

				//LinkEdit linkEdit = new LinkEdit(node.getProbNet(),pNode.getName(), node.getName(), true, true);
				AddLinkEdit linkEdit = new AddLinkEdit(node.getProbNet(), otherNode.getVariable(), node.getVariable(),
						true);

				try {
					node.getProbNet().getPNESupport().announceEdit(linkEdit);
					edits.add(linkEdit);
					nodes.add(otherNode);
				} catch (ConstraintViolationException ignore) {
				} catch (NonProjectablePotentialException | WrongCriterionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
							stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
				}

			}

		}
		return fillArrayWithNodes(nodes);
	}

	/**
	 * Invoked when the button 'remove' is pressed.
	 */
	@Override protected void actionPerformedRemoveValue() {

		int selectedRow = valuesTable.getSelectedRow();
		int rowCount = 0;

		String name = (String) valuesTable.getValueAt(selectedRow, 1);
		
		/*LinkEdit linkEdit;
		linkEdit = new LinkEdit(node.getProbNet(), name,
				node.getName(), true, 
				false);*/
		ProbNet probNet = node.getProbNet();
		RemoveLinkEdit linkEdit;
		try {
			linkEdit = new RemoveLinkEdit(probNet, probNet.getVariable(name), node.getVariable(), true);
			node.getProbNet().doEdit(linkEdit);

			tableModel.removeRow(selectedRow);
			rowCount = valuesTable.getRowCount();
			// Fixing issue #249
			// https://bitbucket.org/cisiad/org.openmarkov.issues/issue/249/removing-the-two-parents-of-a-node
			// Removed the "if" clause
			// No parent is selected after a removal
			/* if ((rowCount > 0) && (selectedRow >= rowCount)) {
				valuesTable.getSelectionModel().setSelectionInterval(
					selectedRow - 1, selectedRow - 1);
			}*/
			absentData = absentPrefixedData();
			setEnabledAddValue(true);
			// After deleting an item from the list,
			// the remove value button is disabled
			// till a new element is selected from the list
			setEnabledRemoveValue(false);

		} catch (DoEditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(Utilities.getOwner(this), e.getMessage(),
					stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
		} catch (ConstraintViolationException e) {
			e.printStackTrace();
		} catch (NonProjectablePotentialException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
					stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
		} catch (WrongCriterionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
					stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
		} catch (NodeNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			JOptionPane.showMessageDialog(this, stringDatabase.getString(e1.getMessage()),
					stringDatabase.getString(e1.getMessage()), JOptionPane.ERROR_MESSAGE);
		}

	}

	// ESCA-JAVA0025:

	/**
	 * Invoked when the button 'up' is pressed.
	 */
	@Override protected void actionPerformedUpValue() {

	}

	// ESCA-JAVA0025:

	/**
	 * Invoked when the button 'down' is pressed.
	 */
	@Override protected void actionPerformedDownValue() {

	}

	/**
	 * Invoked when the row selection changes.
	 *
	 * @param e selection event information.
	 */
    /*
    Fixing issue https://bitbucket.org/cisiad/org.openmarkov.issues/issue/221/button-delete-in-node-properties-parents
    The remove button was always set to disabled, unless more than two parents were present
    We need to override the method from KeyTablePanel
    as in it we are not able to determine in which panel we are located and thus
    if the button needs to be enabled or not.
     */
	@Override public void valueChanged(ListSelectionEvent e) {
		super.valueChanged(e);

		boolean removeValueButtonEnabled = true;
		int rowCount = valuesTable.getRowCount();

		// If there are less than two rows
		if (rowCount <= 2) {
			// But at least there is one, it has not to be the nodes parent table, as
			// one parent may be removable
			if (rowCount >= 1 && this.titleToSelectRows != StringDatabase.getUniqueInstance()
					.getString("NodeParentsPanel.prefixedDataTablePanelParentsTable.Title")) {
				removeValueButtonEnabled = false;
			}
		}
		// The button is enabled or disabled accordingly
		removeValueButton.setEnabled(removeValueButtonEnabled);
	}
}
