/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.VariableType;

import javax.swing.event.ChangeEvent;
import java.awt.*;

/**
 * This class implements the chance node additionalProperties dialog box adding more
 * fields.
 *
 * @author jlgozalo
 * @version 1.10
 */
public class CommonNodePropertiesDialog extends NodePropertiesDialog {

	/**
	 * Static field for serializable class.
	 */
	private static final long serialVersionUID = 5777866419377968128L;

	/**
	 * This method initializes this instance.
	 *
	 * @param owner    window that owns this dialog.
	 * @param newNode  if true, it indicates that a new network is being created; if
	 *                 false, an existing network is being modified.
	 * @param readOnly if true, values inside the dialog will not be editable
	 */
	public CommonNodePropertiesDialog(Window owner, Node node, boolean newNode, boolean readOnly) {

		super(owner, node, newNode, readOnly);
		node.getProbNet().getPNESupport().openParenthesis();
		initialize();
		getTabbedPane().addChangeListener(this);
		setLocationRelativeTo(owner);

	}

	/**
	 * This method initializes this instance.
	 *
	 * @param owner   window that owns this dialog.
	 * @param newNode if true, it indicates that a new network is being created; if
	 *                false, an existing network is being modified.
	 */
	public CommonNodePropertiesDialog(Window owner, Node node, boolean newNode) {

		this(owner, node, newNode, false);
	}

	/**
	 * This method carries out the checks of the specific fields. This specific
	 * fields depend on the type of the node.
	 *
	 * @return true if all the fields are correct.
	 */
	@Override protected boolean specificChecks() {

		boolean result = false;

		if (getNodeProperties().getNodeType() == NodeType.UTILITY) {
			result = true;
		} else {
			// node variable type
			VariableType varType = ((NodeDefinitionPanel) getNodeDefinitionPanel()).getVariableType();
			if (varType != null) {
				if (varType.equals(VariableType.FINITE_STATES) || varType.equals(VariableType.DISCRETIZED)) {
					//changed by mpalacios
					//result = ((DiscreteValuesTablePanel) getNodeDiscreteValuesTablePanel())
					//			.checkStates();
					result = ((NodeDomainValuesTablePanel) getNodeDomainValuesTablePanel()).checkStates();
				} else if (varType.equals(VariableType.DISCRETIZED)) {
					result = ((NodeDomainValuesTablePanel) getNodeDomainValuesTablePanel()).checkStates();
				} else if (varType.equals(VariableType.NUMERIC)) {
					// TODO this must be set when continuos will be implemented
					result = true;
				}
			}
		}
		return result;

	}

	public void stateChanged(ChangeEvent e) {
		/*JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
		int index = sourceTabbedPane.getSelectedIndex();
		System.out.println ("Tab changed to: " +
		sourceTabbedPane.getTitleAt(index));
		
		if (sourceTabbedPane.getSelectedComponent() instanceof NodeProbsValuesTablePanel ){
		
			nodeProbsValuesTablePanel.setFieldsFromProperties(node);
		}*/
	}

}