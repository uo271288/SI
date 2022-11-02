/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.action.NodeAlwaysObservedEdit;
import org.openmarkov.core.action.NodeCommentEdit;
import org.openmarkov.core.action.NodeNameEdit;
import org.openmarkov.core.action.PurposeEdit;
import org.openmarkov.core.action.RelevanceEdit;
import org.openmarkov.core.action.TimeSliceEdit;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.StringWithProperties;
import org.openmarkov.core.model.network.Util;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.gui.action.NodeAgentEdit;
import org.openmarkov.gui.action.NodeDecisionCriteriaEdit;
import org.openmarkov.gui.constraint.AlwaysObservedPropertyValidator;
import org.openmarkov.gui.dialog.CommentListener;
import org.openmarkov.gui.dialog.common.CommentHTMLScrollPane;
import org.openmarkov.gui.localize.LocalizedException;
import org.openmarkov.gui.localize.StringDatabase;
import org.openmarkov.gui.util.Purpose;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.MessageFormat;
import java.util.List;

/**
 * Panel to set the definition of a node.
 *
 * @author jlgozalo
 * @version 1.5 mpalacios
 */
public class NodeDefinitionPanel extends JPanel
		implements FocusListener, ItemListener, CommentListener, ActionListener {
	/**
	 * serial uid
	 */
	private static final long serialVersionUID = 1047978130482205148L;
	/**
	 * String database
	 */
	protected StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
	private JComboBox<String> jComboBoxNetworkAgents;
	private JLabel jLabelTimeSlice;
	private JComboBox<String> jComboBoxTimeSlice;
	private JLabel jLabelDecisionCriteria;
	private JComboBox<Criterion> jComboBoxDecisionCriteria;
	/**
	 * Variable to store the previous selectd item in the combobox
	 */
	private Object comboBoxPreviousSelectedItem = null;
	/**
	 * The Node Name Label
	 */
	private JLabel jLabelNodeName = null;
	/**
	 * The Node Name Text Field
	 */
	private JTextField jTextFieldNodeName = null;
	/**
	 * The always observed property label
	 */
	private JLabel jLabelAlwaysObserved = null;
	/**
	 * The always observed checkbox
	 */
	private JCheckBox jCheckboxAlwaysObserved = null;
	/**
	 * Network agents label
	 */
	private JLabel jLabelNetworkAgents = null;
	/**
	 * internal node type item for convenience purpose
	 */
	private VariableType variableType = null;
	/**
	 * the Node Relevance Label
	 */
	private JLabel jLabelNodeRelevance = null;
	/**
	 * The Node Relevance Combo Box
	 */
	private JComboBox<Double> jComboBoxNodeRelevance = null;
	/**
	 * The Node Purpose Label
	 */
	private JLabel jLabelNodePurpose = null;
	/**
	 * The Node Purpose Combo Box
	 */
	private JComboBox<String> jComboBoxNodePurpose = null;
	/**
	 * The Node Definition Comment Label
	 */
	private JTextArea jTextAreaLabelNodeDefinitionComment;
	/**
	 * The Node Comment Scroll Panel box
	 */
	private CommentHTMLScrollPane commentHTMLScrollPaneNodeDefinitionComment = null;
	/**
	 * Object where all information will be saved.
	 */
	private Node node = null;
	/**
	 * Specifies if the node whose additionalProperties are edited is new.
	 */
	private boolean newNode = false;

	/**
	 * constructor without construction parameters
	 */
	public NodeDefinitionPanel() {
		this(true);// , new ElementObservable() );
	}

	/**
	 * Constructor
	 *
	 * @param node
	 */
	public NodeDefinitionPanel(Node node) {
		this(true);// , notifier );
		this.node = node;
		try {
			initialize();
		} catch (Throwable e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
					stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
		}
		if (node.getProbNet().getAgents() != null) {
			getJComboBoxNetworkAgents().setEnabled(true);
			getJComboBoxNetworkAgents().setVisible(true);
			getJLabelNetworkAgents().setVisible(true);
		} else if (node.getProbNet().getAgents() == null) {
			getJComboBoxNetworkAgents().setEnabled(false);
			getJComboBoxNetworkAgents().setVisible(false);
			getJLabelNetworkAgents().setVisible(false);
		}
		// Check if the network has associated Only
		// AtemporalVariablesConstranint
		if (node.getProbNet().variablesCouldBeTemporal()) {
			getJComboBoxTimeSlice().setEnabled(true);
			getJComboBoxTimeSlice().setVisible(true);
			getJLabelTimeSlice().setVisible(true);
		} else {
			getJComboBoxTimeSlice().setEnabled(false);
			getJComboBoxTimeSlice().setVisible(false);
			getJLabelTimeSlice().setVisible(false);
		}
		if (node.getNodeType() == NodeType.UTILITY) {
			getJComboBoxDecisionCriteria().setEnabled(true);
			getJComboBoxDecisionCriteria().setVisible(true);
			getJLabelDecisionCriteria().setVisible(true);
		} else {
			getJComboBoxDecisionCriteria().setEnabled(false);
			getJComboBoxDecisionCriteria().setVisible(false);
			getJLabelDecisionCriteria().setVisible(false);
		}
		getJComboBoxNodePurpose().setEnabled(true);
		getJComboBoxNodeRelevance().setEnabled(true);
		if (!AlwaysObservedPropertyValidator.validate(node)) {
			getJLabelAlwaysObserved().setVisible(false);
			getJCheckBoxAlwaysObserved().setVisible(false);
		}
	}

	/**
	 * This method initialises this instance.
	 *
	 * @param newNode - true if the node is a new node; otherwise false
	 */
	public NodeDefinitionPanel(final boolean newNode) {// , ElementObservable
		// notifier) {
		this.newNode = newNode;
		// this.notifier = notifier;
	}

	/**
	 * Get the node Properties in this panel
	 *
	 * @return the nodeProperties
	 */
	public Node getNodeProperties() {
		return node;
	}

	/**
	 * Set the node additionalProperties in this panel with the provided ones
	 *
	 * @param nodeProperties the nodeProperties to set
	 */
	public void setNodeProperties(final Node nodeProperties) {
		this.node = nodeProperties;
	}

	/**
	 * @return the newNode
	 */
	public boolean isNewNode() {
		return newNode;
	}

	/**
	 * @param newNode the newNode to set
	 */
	public void setNewNode(boolean newNode) {
		this.newNode = newNode;
	}

	/**
	 * <code>Initialize</code>
	 * <p>
	 * initialize the layout for this panel
	 */
	private void initialize() throws Exception {
		this.getCommentHTMLScrollPaneNodeDefinitionComment();
		setName("NodeDefinitionPanel");
		setFocusable(false);
		setDoubleBuffered(false);
		setMinimumSize(new Dimension(500, 245));
		setMaximumSize(new Dimension(500, 245));
		setPreferredSize(new Dimension(500, 245));
		setFocusCycleRoot(true);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
				groupLayout.createSequentialGroup().addContainerGap().addGroup(
						groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup()
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false).addGroup(
										groupLayout.createSequentialGroup().addComponent(getJLabelNodeName())
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(getJTextFieldNodeName(), GroupLayout.PREFERRED_SIZE, 203,
														GroupLayout.PREFERRED_SIZE).addGap(18)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(getJLabelTimeSlice())
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(getJComboBoxTimeSlice(), GroupLayout.PREFERRED_SIZE, 85,
														GroupLayout.PREFERRED_SIZE)).addGroup(
										groupLayout.createSequentialGroup().addComponent(getJLabelNodePurpose())
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(getJComboBoxNodePurpose(), GroupLayout.PREFERRED_SIZE,
														203, GroupLayout.PREFERRED_SIZE).addGap(18)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(getJLabelNodeRelevance())
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(getJComboBoxNodeRelevance(), GroupLayout.PREFERRED_SIZE,
														85, GroupLayout.PREFERRED_SIZE)).addGroup(
										groupLayout.createSequentialGroup()
												.addComponent(getAgentsOrDecisionCriteriaOrObservedLabel())
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(getAgentsOrDecisionCriteriaOrObserved(),
														GroupLayout.PREFERRED_SIZE, 203, GroupLayout.PREFERRED_SIZE)

								).addGroup(groupLayout.createSequentialGroup()
										.addComponent(getJTextAreaLabelNodeDefinitionComment())
										.addComponent(getCommentHTMLScrollPaneNodeDefinitionComment(), 30, 560,
												Short.MAX_VALUE))).addContainerGap()))));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
				groupLayout.createSequentialGroup().addContainerGap().addGroup(
						groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(getJLabelNodeName())
								.addComponent(getJTextFieldNodeName(), GroupLayout.PREFERRED_SIZE, /* 20 */
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(getJLabelTimeSlice()).addComponent(getJComboBoxTimeSlice()))
						.addPreferredGap(ComponentPlacement.RELATED).addGroup(
						groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(getJComboBoxNodePurpose(), GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(getJLabelNodePurpose())
								.addComponent(getJLabelNodeRelevance(), GroupLayout.PREFERRED_SIZE, /* 25 */
										GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(getJComboBoxNodeRelevance())).addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
										.addComponent(getAgentsOrDecisionCriteriaOrObservedLabel())
										.addComponent(getAgentsOrDecisionCriteriaOrObserved(), GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								/*
								 * . addComponent ( getJLabelAlwaysObserved ( ) ) . addComponent (
								 * getJCheckBoxAlwaysObserved ( ) , GroupLayout . PREFERRED_SIZE ,
								 * GroupLayout . DEFAULT_SIZE , GroupLayout . PREFERRED_SIZE )
								 */).addGap(21)
						// .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(getJTextAreaLabelNodeDefinitionComment())
								.addComponent(getCommentHTMLScrollPaneNodeDefinitionComment(), GroupLayout.DEFAULT_SIZE,
										62, 150)).addContainerGap(77, Short.MAX_VALUE)));
		Component[] components = new Component[3];
		components[0] = getJComboBoxNodePurpose();
		components[1] = getJTextFieldNodeName();
		components[2] = getAgentsOrDecisionCriteriaOrObserved();
		groupLayout.linkSize(components);
		Component[] components2 = new Component[5];
		components2[0] = getAgentsOrDecisionCriteriaOrObservedLabel();
		components2[1] = getJLabelNodeName();
		components2[2] = getJLabelNodePurpose();
		components2[3] = getJLabelNodeRelevance();
		components2[4] = getJLabelTimeSlice();
		groupLayout.linkSize(components2);
		Component[] components3 = new Component[2];
		components3[0] = getJComboBoxNodeRelevance();
		components3[1] = getJComboBoxTimeSlice();
		groupLayout.linkSize(components3);
		setLayout(groupLayout);
	}

	/**
	 * This method initialises jLabelTimeSlice
	 *
	 * @return a new name label.
	 */
	private JLabel getJLabelTimeSlice() {
		if (jLabelTimeSlice == null) {
			jLabelTimeSlice = new JLabel();
			jLabelTimeSlice.setHorizontalAlignment(SwingConstants.LEFT);
			jLabelTimeSlice.setHorizontalTextPosition(SwingConstants.LEFT);
			jLabelTimeSlice.setName("jLabelTimeSlice");
			jLabelTimeSlice.setText("a Label");
			jLabelTimeSlice.setText(stringDatabase.getString("NodeDefinitionPanel.jLabelTimeSlice.Text"));
			jLabelTimeSlice.setLabelFor(getJComboBoxTimeSlice());
		}
		return jLabelTimeSlice;
	}

	/**
	 * initialize the content of the Combo box for the tme slice for temporal
	 * variables
	 *
	 * @return the JComboBoxNodeRelevance
	 */
	private JComboBox<String> getJComboBoxTimeSlice() {
		if (jComboBoxTimeSlice == null) {
			jComboBoxTimeSlice = new JComboBox<>();
			jComboBoxTimeSlice.setName("jComboBoxTimeSlice");
			jComboBoxTimeSlice.setEditable(false);
			jComboBoxTimeSlice.setSize(60, 40);
			if (!node.getProbNet().onlyTemporal()) {
				// It corresponds with no time slice, atemporal selection
				// timeSlice = Integer.MIN
				jComboBoxTimeSlice.addItem(stringDatabase.getString("NodeDefinitionPanel.Atemporal.Text"));
			}

			// Get max time slice in the network
			int maxTimeSlice = 0;
			for (Node otherNode : node.getProbNet().getNodes()) {
				if (otherNode.getVariable().isTemporal() && otherNode.getVariable().getTimeSlice() > maxTimeSlice) {
					maxTimeSlice = otherNode.getVariable().getTimeSlice();
				}
			}
			for (int i = 0; i <= maxTimeSlice + 1; ++i) {
				jComboBoxTimeSlice.addItem(String.valueOf(i));
			}
			String timeSlice = String.valueOf(node.getVariable().getTimeSlice());
			jComboBoxTimeSlice.setSelectedItem(timeSlice);
			jComboBoxTimeSlice.addItemListener(this);
			// jComboBoxTimeSlice.setEnabled(false);
		}
		return jComboBoxTimeSlice;
	}

	/**
	 * This method initialises jLabelNodeName
	 *
	 * @return a new name label.
	 */
	private JLabel getJLabelNodeName() {
		if (jLabelNodeName == null) {
			jLabelNodeName = new JLabel();
			jLabelNodeName.setHorizontalAlignment(SwingConstants.LEFT);
			jLabelNodeName.setHorizontalTextPosition(SwingConstants.LEFT);
			jLabelNodeName.setName("jLabelNodeName");
			jLabelNodeName.setText("a Label");
			jLabelNodeName.setText(stringDatabase.getString("NodeDefinitionPanel.jLabelNodeName.Text"));
			jLabelNodeName.setDisplayedMnemonic(
					stringDatabase.getString("NodeDefinitionPanel.jLabelNodeName.Mnemonic").charAt(0));
			jLabelNodeName.setLabelFor(getJTextFieldNodeName());
		}
		return jLabelNodeName;
	}

	/**
	 * This method initialises jTextFieldNodeName
	 *
	 * @return a new name field.
	 */
	public JTextField getJTextFieldNodeName() {
		int dis = 15;
		if (jTextFieldNodeName == null) {
			jTextFieldNodeName = new JTextField();
			jTextFieldNodeName.setName("jTextFieldNodeName");
			jTextFieldNodeName.setPreferredSize(new Dimension(50, dis));
			// jTextFieldNodeName.addActionListener( this );
			jTextFieldNodeName.addFocusListener(this);
		}
		return jTextFieldNodeName;
	}

	/**
	 * This method initialises jLabelAlwaysObserved
	 *
	 * @return a new label for the always observed property.
	 */
	private JLabel getJLabelAlwaysObserved() {
		if (jLabelAlwaysObserved == null) {
			jLabelAlwaysObserved = new JLabel();
			jLabelAlwaysObserved.setHorizontalAlignment(SwingConstants.LEFT);
			jLabelAlwaysObserved.setHorizontalTextPosition(SwingConstants.LEFT);
			jLabelAlwaysObserved.setVerticalAlignment(SwingConstants.CENTER);
			jLabelAlwaysObserved.setVerticalTextPosition(SwingConstants.CENTER);
			jLabelAlwaysObserved.setName("jLabelAlwaysObserved");
			jLabelAlwaysObserved.setText("a Label");
			jLabelAlwaysObserved.setText(stringDatabase.getString("NodeDefinitionPanel.jLabelAlwaysObserved.Text"));
			jLabelAlwaysObserved.setDisplayedMnemonic(
					stringDatabase.getString("NodeDefinitionPanel.jLabelAlwaysObserved.Mnemonic").charAt(0));
			jLabelAlwaysObserved.setLabelFor(getJTextFieldNodeName());
		}
		return jLabelAlwaysObserved;
	}

	/**
	 * This method initialises jCheckBoxAlwaysObserved
	 *
	 * @return a new checkbox
	 */
	public JCheckBox getJCheckBoxAlwaysObserved() {
		if (jCheckboxAlwaysObserved == null) {
			jCheckboxAlwaysObserved = new JCheckBox();
			jCheckboxAlwaysObserved.setName("jCheckboxAlwaysObserved");
			jCheckboxAlwaysObserved.setVerticalAlignment(SwingConstants.CENTER);
			jCheckboxAlwaysObserved.addActionListener(this);
			jCheckboxAlwaysObserved.addFocusListener(this);
		}
		return jCheckboxAlwaysObserved;
	}

	/**
	 * This method initialises jLabelNodeName
	 *
	 * @return a new name label.
	 */
	private JLabel getJLabelNodeRelevance() {
		if (jLabelNodeRelevance == null) {
			jLabelNodeRelevance = new JLabel();
			jLabelNodeRelevance.setHorizontalTextPosition(SwingConstants.LEFT);
			jLabelNodeRelevance.setHorizontalAlignment(SwingConstants.LEFT);
			jLabelNodeRelevance.setName("jLabelNodeRelevance");
			jLabelNodeRelevance.setText("a Label");
			jLabelNodeRelevance.setText(stringDatabase.getString("NodeDefinitionPanel.jLabelNodeRelevance.Text"));
			jLabelNodeRelevance.setDisplayedMnemonic(
					stringDatabase.getString("NodeDefinitionPanel.jLabelNodeRelevance.Mnemonic").charAt(0));
			jLabelNodeRelevance.setLabelFor(getJComboBoxNodeRelevance());
		}
		return jLabelNodeRelevance;
	}

	/**
	 * This method initialises jLabelNodeName
	 *
	 * @return a new name label.
	 */
	private JLabel getJLabelNetworkAgents() {
		if (jLabelNetworkAgents == null) {
			jLabelNetworkAgents = new JLabel();
			jLabelNetworkAgents.setHorizontalTextPosition(SwingConstants.LEFT);
			jLabelNetworkAgents.setHorizontalAlignment(SwingConstants.LEFT);
			jLabelNetworkAgents.setName("jLabelNetworkAgent");
			jLabelNetworkAgents.setText("a Label");
			jLabelNetworkAgents.setText(stringDatabase.getString("NodeDefinitionPanel.jLabelNetworkAgents.Text"));
			/*
			 * jLabelNetworkAgents.setDisplayedMnemonic(dialogStringResource
			 * .getValuesInAString( "NodeDefinitionPanel.jLabelNodeRelevance.Mnemonic")
			 * .charAt(0));
			 */
			jLabelNetworkAgents.setLabelFor(getJComboBoxNetworkAgents());
		}
		return jLabelNetworkAgents;
	}

	/**
	 * initialize the content of the Combo box for the Node Relevance
	 *
	 * @return the JComboBoxNodeRelevance
	 */
	private JComboBox<Double> getJComboBoxNodeRelevance() {
		if (jComboBoxNodeRelevance == null) {
			jComboBoxNodeRelevance = new JComboBox<>();
			jComboBoxNodeRelevance.setName("jComboBoxNodeRelevance");
			jComboBoxNodeRelevance.setEditable(true);
			jComboBoxNodeRelevance.setSize(60, 40);
			fillJComboBoxNodeRelevanceWithoutDecimals();
			jComboBoxNodeRelevance.setEnabled(false);
		}
		return jComboBoxNodeRelevance;
	}

	/**
	 * fill the jComboBoxNodeRelevance with the appropriate values with an
	 * increment of 0.1. If not used, mathematical addition to avoid the
	 * Precision problems with the proccesors Therefore, it is using a "string"
	 * concatenation with integers and then a conversion to doubles
	 */
	@SuppressWarnings("unused") private void fillJComboBoxNodeRelevance() {
		String number;
		if (jComboBoxNodeRelevance != null) {
			for (int realPart = 0; realPart < 10; realPart++) {
				for (int decimalPart = 0; decimalPart < 10; decimalPart++) {
					number = Integer.toString(realPart) + "." + Integer.toString(decimalPart);
					jComboBoxNodeRelevance.addItem(Double.valueOf(number));
				}
			}
		}
	}

	/**
	 * fill the jComboBoxNodeRelevance with the appropriate values with an
	 * increment of 1.0. The values appear in reverse order.
	 */
	private void fillJComboBoxNodeRelevanceWithoutDecimals() {
		if (jComboBoxNodeRelevance != null) {
			for (int value = 10; value >= 0; value--) {
				jComboBoxNodeRelevance.addItem(Double.valueOf(value));
			}
		}
	}

	/**
	 * This method initialises jLabelNodeName
	 *
	 * @return a new name label.
	 */
	private JLabel getJLabelNodePurpose() {
		if (jLabelNodePurpose == null) {
			jLabelNodePurpose = new JLabel();
			jLabelNodePurpose.setName("jLabelNodePurpose");
			jLabelNodePurpose.setHorizontalTextPosition(SwingConstants.LEFT);
			jLabelNodePurpose.setHorizontalAlignment(SwingConstants.LEFT);
			jLabelNodePurpose.setText("a Label");
			jLabelNodePurpose.setText(stringDatabase.getString("NodeDefinitionPanel.jLabelNodePurpose.Text"));
			jLabelNodePurpose.setDisplayedMnemonic(
					stringDatabase.getString("NodeDefinitionPanel.jLabelNodePurpose.Mnemonic").charAt(0));
			jLabelNodePurpose.setLabelFor(getJTextFieldNodeName());
		}
		return jLabelNodePurpose;
	}

	/**
	 * initialize the content of the Combo box for the Node Purpose
	 *
	 * @return the JComboBoxNodePurpose
	 */
	private JComboBox<String> getJComboBoxNodePurpose() {
		if (jComboBoxNodePurpose == null) {
			jComboBoxNodePurpose = new JComboBox<>(Purpose.getListStrings(false));
			jComboBoxNodePurpose.setName("jComboBoxNodePurpose");
			jComboBoxNodePurpose.setSelectedIndex(0);
			jComboBoxNodePurpose.setMaximumRowCount(9);
			// jComboBoxNodePurpose.addItemListener( this );
			jComboBoxNodePurpose.setEditable(true);
		}
		return jComboBoxNodePurpose;
	}

	/**
	 * initialize the content of the Combo box for the Node Purpose
	 *
	 * @return the JComboBoxNodePurpose
	 */
	private JComboBox<String> getJComboBoxNetworkAgents() {
		if (jComboBoxNetworkAgents == null) {
			// StringsWithProperties agents = node.getProbNet().getAgents();
			List<StringWithProperties> agents = node.getProbNet().getAgents();
			String[] agentNames = null;
			if (agents != null) {
				// Set<String> names = agents.getNames();
				// agentNames = names.toArray(new String[names.size()]);
				// String []auxAgentNames = names.toArray(new
				// String[names.size()]);
				// agentNames = new String [names.size()+1];
				agentNames = new String[agents.size() + 1];
				agentNames[0] = "";
				for (int i = 1; i < agents.size() + 1; i++) {
					// agentNames[i] = auxAgentNames[i-1];
					agentNames[i] = agents.get(i - 1).getString();
				}
			} else if (agents == null /*
			 * && node.getVariable().getAgent() ==
			 * null
			 */) {
				agentNames = new String[1];
				agentNames[0] = "";
			}/*
			 * else if (agents == null && node.getVariable().getAgent() !=
			 * null) { // Dec-POMDP --&gt; POMDP an agent has been already
			 * assigned to current variable agentNames = new String[2];
			 * agentNames[0] = ""; agentNames[1] =
			 * node.getVariable().getAgent().getValuesInAString(); }
			 */
			jComboBoxNetworkAgents = new JComboBox<>(agentNames);
			jComboBoxNetworkAgents.setName("jComboBoxAgents");
			jComboBoxNetworkAgents.setPreferredSize(new Dimension(50, 15));
			if (node.getVariable().getAgent() != null && agents != null) {
				String name = node.getVariable().getAgent().getString();
				int i;
				for (i = 0; i < agentNames.length; i++) {
					if (name == agentNames[i]) {
						break;
					}
				}
				jComboBoxNetworkAgents.setSelectedIndex(i);
			} else {
				jComboBoxNetworkAgents.setSelectedIndex(0);
			}
			jComboBoxNetworkAgents.setEditable(false);
			jComboBoxNetworkAgents.addItemListener(this);
		}
		return jComboBoxNetworkAgents;
	}

	// TODO decision criteria comboBox getter
	private JComponent getAgentsOrDecisionCriteriaOrObserved() {
		if (node.getNodeType() == NodeType.DECISION) {
			return getJComboBoxNetworkAgents();
		} else if (node.getNodeType() == NodeType.UTILITY) {
			return getJComboBoxDecisionCriteria();
		} else if (node.getNodeType() == NodeType.CHANCE) {
			return getJCheckBoxAlwaysObserved();
		}
		// default
		return getJComboBoxNetworkAgents();
	}

	private JLabel getAgentsOrDecisionCriteriaOrObservedLabel() {
		if (node.getNodeType() == NodeType.DECISION) {
			return getJLabelNetworkAgents();
		} else if (node.getNodeType() == NodeType.UTILITY) {
			return getJLabelDecisionCriteria();
		} else if (node.getNodeType() == NodeType.CHANCE) {
			return getJLabelAlwaysObserved();
		}
		// default
		return getJLabelNetworkAgents();
	}

	private JLabel getJLabelDecisionCriteria() {
		if (jLabelDecisionCriteria == null) {
			jLabelDecisionCriteria = new JLabel();
			jLabelDecisionCriteria.setName("jLabelDecisionDriteria");
			jLabelDecisionCriteria.setHorizontalTextPosition(SwingConstants.LEFT);
			jLabelDecisionCriteria.setHorizontalAlignment(SwingConstants.LEFT);
			jLabelDecisionCriteria.setText("a Label");
			jLabelDecisionCriteria.setText(stringDatabase.getString("NodeDefinitionPanel.jLabelDecisionDriteria.Text"));
			/*
			 * jLabelDecisionCriteria
			 * .setDisplayedMnemonic(dialogStringResource.getValuesInAString(
			 * "NodeDefinitionPanel.jLabelNodePurpose.Mnemonic") .charAt(0));
			 */
			jLabelDecisionCriteria.setLabelFor(getJComboBoxDecisionCriteria());
		}
		return jLabelDecisionCriteria;
	}

	private JComboBox<Criterion> getJComboBoxDecisionCriteria() {
		if (jComboBoxDecisionCriteria == null) {
			List<Criterion> decisionCriteria = node.getProbNet().getDecisionCriteria();
			jComboBoxDecisionCriteria = new JComboBox<>();

			for (Criterion criterion : decisionCriteria) {
				jComboBoxDecisionCriteria.addItem(criterion);
			}
			jComboBoxDecisionCriteria.setName("jComboBoxDecisionCriteria");
			jComboBoxDecisionCriteria.setPreferredSize(new Dimension(50, 15));
			if (node.getVariable().getDecisionCriterion() != null && decisionCriteria != null) {
				jComboBoxDecisionCriteria.setSelectedItem(node.getVariable().getDecisionCriterion());
				jComboBoxDecisionCriteria.addItemListener(this);
			} else {
				jComboBoxDecisionCriteria.setEnabled(false);
			}
			jComboBoxDecisionCriteria.addItemListener(this);
		}
		return jComboBoxDecisionCriteria;
	}

	/**
	 * This method initialises jLabelNodeDefinitionComment
	 *
	 * @return a new label for the comment
	 */
	protected JTextArea getJTextAreaLabelNodeDefinitionComment() {
		if (jTextAreaLabelNodeDefinitionComment == null) {
			jTextAreaLabelNodeDefinitionComment = new JTextArea();
			jTextAreaLabelNodeDefinitionComment.setLineWrap(true);
			jTextAreaLabelNodeDefinitionComment.setOpaque(false);
			jTextAreaLabelNodeDefinitionComment.setName("jTextAreaLabelNodeDefinitionComment");
			jTextAreaLabelNodeDefinitionComment.setFocusable(false);
			jTextAreaLabelNodeDefinitionComment.setEditable(false);
			jTextAreaLabelNodeDefinitionComment.setFont(getJLabelNodeName().getFont());
			jTextAreaLabelNodeDefinitionComment.setText("an Extended Label");
			MessageFormat messageForm = new MessageFormat(
					stringDatabase.getString("NodeDefinitionPanel.jTextAreaLabelNodeDefinitionComment.Text"));
			Object[] labelArgs = new Object[] { getJTextFieldNodeName().getText() };
			jTextAreaLabelNodeDefinitionComment.setText(messageForm.format(labelArgs));
		}
		return jTextAreaLabelNodeDefinitionComment;
	}

	/**
	 * This method initialises commentHTMLScrollPaneNodeDefinitionComment
	 *
	 * @return a new comment HTML scroll pane.
	 */
	private CommentHTMLScrollPane getCommentHTMLScrollPaneNodeDefinitionComment() {
		if (commentHTMLScrollPaneNodeDefinitionComment == null) {
			commentHTMLScrollPaneNodeDefinitionComment = new CommentHTMLScrollPane();
			commentHTMLScrollPaneNodeDefinitionComment.setName("commentHTMLScrollPaneNodeDefinitionComment");
			commentHTMLScrollPaneNodeDefinitionComment.addCommentListener(this);
		}
		return commentHTMLScrollPaneNodeDefinitionComment;
	}

	/**
	 * @return the variableType
	 */
	public VariableType getVariableType() {
		return variableType;
	}

	/**
	 * Invoked when an item has been selected.
	 *
	 * @param e event information.
	 */
	@SuppressWarnings("unchecked") public void itemStateChanged(ItemEvent e) {
		int optionDeselected = 0;
		ItemSelectable itemSelectable = e.getItemSelectable();
		Object selected[] = itemSelectable.getSelectedObjects();
		String itemSelected = selected.length == 0 ? "null" : selected[0].toString();
		JComboBox<String> comboBox = (JComboBox<String>) e.getSource();
		if (e.getStateChange() == ItemEvent.DESELECTED) {
			optionDeselected = comboBox.getSelectedIndex();
			comboBoxPreviousSelectedItem = e.getItem();
		}
		if (comboBox.equals(jComboBoxNodePurpose)) {
			if (!(itemSelected == null) && e.getStateChange() == ItemEvent.SELECTED) {
				PurposeEdit purposeEdit = null;
				for (String purposeString : Purpose.getListStrings(true)) {
					if (itemSelected.equals(Purpose.getString(purposeString))) {
						purposeEdit = new PurposeEdit(node, purposeString);
						break;
					}
				}
				try {
					node.getProbNet().doEdit(purposeEdit);
				} catch (ConstraintViolationException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(),
							stringDatabase.getString("ConstraintViolationException"), JOptionPane.ERROR_MESSAGE);
					comboBox.setSelectedIndex(optionDeselected);
					comboBox.requestFocus();
				} catch (NonProjectablePotentialException | WrongCriterionException | DoEditException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					JOptionPane.showMessageDialog(this, e1.getMessage(), e1.getMessage(), JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if (comboBox.equals(jComboBoxNodeRelevance)) {
			if (!(itemSelected == null) && e.getStateChange() == ItemEvent.SELECTED) {
				RelevanceEdit relevanceEdit = null;
				relevanceEdit = new RelevanceEdit(node, Double.valueOf(itemSelected));
				try {
					node.getProbNet().doEdit(relevanceEdit);
				} catch (ConstraintViolationException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(),
							stringDatabase.getString("ConstraintViolationException"), JOptionPane.ERROR_MESSAGE);
					comboBox.setSelectedIndex(optionDeselected);
					comboBox.requestFocus();
				} catch (NonProjectablePotentialException | WrongCriterionException | DoEditException e1) {
					// TODO Auto-generated catch block
					JOptionPane.showMessageDialog(this, e1.getMessage(), e1.getMessage(), JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if (comboBox.equals(jComboBoxTimeSlice)) {
			if (!(itemSelected == null) && e.getStateChange() == ItemEvent.SELECTED) {
				TimeSliceEdit timeSliceEdit = null;
				if (itemSelected.equals(stringDatabase.getString("NodeDefinitionPanel.Atemporal.Text"))) {
					timeSliceEdit = new TimeSliceEdit(node, Integer.MIN_VALUE);
				} else {
					timeSliceEdit = new TimeSliceEdit(node, Integer.valueOf(itemSelected));
				}
				try {
					node.getProbNet().doEdit(timeSliceEdit);
					// comboBox.setSelectedIndex(optionSelected);
				} catch (DoEditException | ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException e1) {
                    /*
                    Fixing issue 203
                    https://bitbucket.org/cisiad/org.openmarkov.issues/issue/203/two-time-related-variables-with-identical
                    If the selected time slice already exists in the network, the previous selected item
                    is re-selected and an error is displayed
                     */
					jComboBoxTimeSlice.setSelectedItem(comboBoxPreviousSelectedItem);
					jComboBoxTimeSlice.hidePopup();
					e1.printStackTrace();
					JOptionPane.showMessageDialog(this, e1.getMessage(), e1.getMessage(), JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if (comboBox.equals(jComboBoxNetworkAgents)) {
			if (!(itemSelected == null)
				/* && e.getStateChange() == ItemEvent.SELECTED */) {
				StringWithProperties agent = new StringWithProperties(itemSelected);
				NodeAgentEdit nodeAgentEdit = new NodeAgentEdit(node, agent);
				try {
					node.getProbNet().doEdit(nodeAgentEdit);
					// comboBox.setSelectedIndex(optionSelected);
				} catch (DoEditException | ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} else if (comboBox.equals(jComboBoxDecisionCriteria)) {
			if (!(itemSelected == null)) {

				// Search the criterion into probNets decision criteria
				Criterion decisionCriteria = (Criterion) jComboBoxDecisionCriteria.getSelectedItem();
				NodeDecisionCriteriaEdit nodeDecisionCriteriaEdit = new NodeDecisionCriteriaEdit(node,
						decisionCriteria);
				try {
					node.getProbNet().doEdit(nodeDecisionCriteriaEdit);
				} catch (DoEditException | ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * Invoked when a focus lost action occurs.
	 *
	 * @param e - event information
	 */
	public void focusLost(FocusEvent e) {
		if (e.getSource().equals(this.jTextFieldNodeName)) {
			// actionPerformedNodeNameChangeValue();
			if (!node.getName().equals(this.jTextFieldNodeName.getText())) {
				NodeNameEdit nodeNameEdit = new NodeNameEdit(node, this.jTextFieldNodeName.getText());
				try {
					node.getProbNet().doEdit(nodeNameEdit);
				} catch (ConstraintViolationException e1) {
					// TODO Auto-generated catch block
					// e1.printStackTrace();
					LocalizedException localizedException = new LocalizedException(e1);
					localizedException.showException();

					jTextFieldNodeName.setText(node.getName());
					jTextFieldNodeName.requestFocus();
				} catch (DoEditException | NonProjectablePotentialException | WrongCriterionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					JOptionPane.showMessageDialog(this, e1.getMessage(), e1.getMessage(), JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	/**
	 * Invoked when a focus gained action occurs.
	 *
	 * @param e - event information
	 */
	public void focusGained(FocusEvent e) {
		if (e.getSource().equals(this.jTextFieldNodeName)) {
			this.getJTextFieldNodeName().selectAll();
		}
	}

	/**
	 * This method fills the content of the fields from a NodeProperties object.
	 *
	 * @param node <code>Node</code>
	 */
	public void setFieldsFromProperties(Node node) {
		jTextFieldNodeName.setText(node.getVariable().getBaseName());
		// node variable type
		// relevance
		// if (properties.getVariable().getVariableType() ==
		// VariableType.FINITE_STATES){
		jComboBoxNodeRelevance.removeItemListener(this);
		jComboBoxNodePurpose.removeItemListener(this);
		jComboBoxNodeRelevance.setSelectedItem(node.getRelevance());
		jComboBoxNodeRelevance.setEnabled(true);
		// purpose
		jComboBoxNodePurpose.setSelectedIndex(Purpose.getIndex(node.getPurpose()));
		jComboBoxNodePurpose.setEnabled(true);
		jComboBoxNodeRelevance.addItemListener(this);
		jComboBoxNodePurpose.addItemListener(this);
		// }
		// node comment title
		MessageFormat messageForm = new MessageFormat(
				stringDatabase.getString("NodeDefinitionPanel.commentHTMLScrollPaneNodeDefinitionComment.Text"));
		String shortNodeName = getJTextFieldNodeName().getText();
		Object[] labelArgs = new Object[] { shortNodeName };
		commentHTMLScrollPaneNodeDefinitionComment.setTitle(messageForm.format(labelArgs));
		// node def comment
		commentHTMLScrollPaneNodeDefinitionComment.setCommentHTMLTextPaneText(node.getComment());
		jCheckboxAlwaysObserved.setSelected(node.isAlwaysObserved());
	}

	/**
	 * This method checks that the name field is filled and there isn't any node
	 * with the same name.
	 *
	 * @return true, if the name field isn't empty and there isn't any node with
	 * this name; otherwise, false.
	 */
	public boolean checkName() {
		String name = jTextFieldNodeName.getText();
		boolean result = true;
		if ((name == null) || name.equals("")) {
			result = false;
		} else if (!node.getName().equals(name) && Util.existNode(node.getProbNet(), name)) {
			result = false;
		}
		if (!result) {
			jTextFieldNodeName.requestFocus();
			return false;
		}
		return true;
	}

	/**
	 * This method checks that the purpose field is filled if this field is
	 * enabled.
	 *
	 * @return true, if the purpose field isn't empty; otherwise, false.
	 */
	public boolean checkPurpose() {
		return true;
	}

	public void commentHasChanged() {

		// check if the comment is empty
		String comment = getCommentHTMLScrollPaneNodeDefinitionComment().isEmpty() ?
				"" :
				getCommentHTMLScrollPaneNodeDefinitionComment().getCommentText();

		NodeCommentEdit nodeCommentEdit = new NodeCommentEdit(node, comment, "DefinitionComment");
		try {
			node.getProbNet().doEdit(nodeCommentEdit);
		} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage(), e.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
	}

	/****
	 * Starts the edit event to change the alwaysObserved property
	 */
	public void alwaysObservedPropertyHasChanged() {
		NodeAlwaysObservedEdit edit = new NodeAlwaysObservedEdit(this.node, this.jCheckboxAlwaysObserved.isSelected());
		try {
			node.getProbNet().doEdit(edit);
		} catch (DoEditException | ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage(), e.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(this.jCheckboxAlwaysObserved)) {
			alwaysObservedPropertyHasChanged();
		}
	}
}