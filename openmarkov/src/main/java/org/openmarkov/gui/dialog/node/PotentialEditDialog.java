/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.action.SetPotentialEdit;
import org.openmarkov.core.action.SetPotentialVariablesEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PolicyType;
import org.openmarkov.core.model.network.Util;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionManager;
import org.openmarkov.core.model.network.potential.AugmentedTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.core.model.network.potential.plugin.PotentialManager;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.gui.action.AugmentedPotentialValueEdit;
import org.openmarkov.gui.dialog.common.AugmentedTablePotentialPanel;
import org.openmarkov.gui.dialog.common.CommentHTMLScrollPane;
import org.openmarkov.gui.dialog.common.ICIPotentialsTablePanel;
import org.openmarkov.gui.dialog.common.OkCancelApplyUndoRedoHorizontalDialog;
import org.openmarkov.gui.dialog.common.PanelResizeEvent;
import org.openmarkov.gui.dialog.common.PanelResizeEventListener;
import org.openmarkov.gui.dialog.common.PolicyTypePanel;
import org.openmarkov.gui.dialog.common.PotentialPanel;
import org.openmarkov.gui.dialog.common.PotentialPanelManager;
import org.openmarkov.gui.dialog.common.ProbabilityTablePanel;
import org.openmarkov.gui.dialog.common.TablePotentialPanel;
import org.openmarkov.gui.dialog.common.UnivariateDistrPotentialPanel;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dialog box to edit all type of potentials ( TablePotential and TreeADDs ). If
 * the potential is a utility role or uniform type, then no Values panel is
 * displayed. If potential is TreeADDpotential, then graphic edition panel is
 * showed.
 *
 * @author mpalacios
 * @author jmendoza
 * @author ibermejo
 * @author carmenyago - adapted the class to the new utility treatment; minor changes
 * @version 1.2 jlgozalo - set class to use independent panels;
 */
public class PotentialEditDialog extends OkCancelApplyUndoRedoHorizontalDialog
		implements ActionListener, PanelResizeEventListener {
	/**
	 *
	 */
	private static final long serialVersionUID = -7344555059488539825L;
	/**
	 * The JComboBox object that shows all the potentials types
	 */
	private JComboBox<String> potentialTypeComboBox;
	/**
	 * The node edited
	 */
	private Node node;
	/**
	 * The panel that contains all the common option to potentials
	 */
	private JPanel potentialTypePanel;
	private PolicyTypePanel pnlPolicyType;
	/**
	 * Label for relation type
	 */
	private JLabel lblPotentialType;
	/**
	 * Relation Type Manager
	 */
	private PotentialManager potentialManager;
	/**
	 * Panel of the graphic editor
	 */
	private PotentialPanel potentialPanel;

	/**
	 * Option deselected in the jComboboxRelationType
	 */
	private int optionPreviouslySelected = 0;
	private String previouslySelectedPotentialType = "";
	/**
	 * If true, values inside the dialog will not be editable
	 */
	private boolean readOnly;
	private JButton reorderVariablesButton;

	//CMI
	//For Univariate
	/**
	 * The JComboBox object that shows all the potentials types
	 */

	private JComboBox<String> univariateDistrComboBox;
	/**
	 * Label for distribution type
	 */
	private JLabel lblUnivariateDistrComboBox;

	/**
	 *
	 */
	private JComboBox<String> univariateDistrParametrizationComboBox;

	/**
	 *
	 */
	private JLabel lblParametrizationComboBox;
	/**
	 *
	 */
	private String previouslySelectedDistributionName = "Exact";

	//CMF

	private CommentHTMLScrollPane commentPane;

	/**
	 * Creates the dialog.
	 */
	public PotentialEditDialog(Window owner, Node node, boolean newElement, boolean readOnly) {
		super(owner);
		this.node = node;
		this.readOnly = readOnly;
		node.getProbNet().getPNESupport().setWithUndo(true);
		node.getProbNet().getPNESupport().openParenthesis();
		initialize();
		List<Potential> potentials = node.getPotentials();
		if (!potentials.isEmpty() && potentials.get(0).getComment() != null && !potentials.get(0).getComment()
				.isEmpty()) {
			commentPane.setCommentHTMLTextPaneText(potentials.get(0).getComment());
		}
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		Rectangle bounds = owner.getBounds();
		int width = screenSize.width / 2;
		int height = screenSize.height / 2;
		// center point of the owner window
		int x = bounds.x / 2 - width / 2;
		int y = bounds.y / 2 - height / 2;
		this.setBounds(x, y, width, height);
		setLocationRelativeTo(null);
		setMinimumSize(new Dimension(width, height / 2));
		setResizable(true);
		pack();
	}

	/**
	 * Constructor
	 */
	public PotentialEditDialog(Window owner, Node node, boolean newElement) {
		this(owner, node, newElement, false);
	}

	/**
	 * This method configures the dialog box.
	 */
	private void initialize() {
		potentialManager = new PotentialManager();
		// Set default title
		setTitle("NodePotentialDialog.Title.Label");
		configureComponentsPanel();
		pack();
	}

	/**
	 * Sets up the panel where all components, except the buttons of the buttons
	 * panel, will be appear.
	 */
	private void configureComponentsPanel() {
		getComponentsPanel().setLayout(new BorderLayout(5, 5));
		// getComponentsPanel().setSize(294, 29);
		getComponentsPanel().setMaximumSize(new Dimension(180, 40));
		getComponentsPanel().add(getPotentialTypePanel(), BorderLayout.NORTH);
		getComponentsPanel().add(getPotentialPanel(), BorderLayout.CENTER);

		//CMI
		// For univariate
		if (showUnivariateDistrComboBox()) {
			getUnivariateDistrJCombobox().setVisible(true);
			getUnivariateDistrJCombobox().setEnabled(true);
			getUnivariateDistrParametrizationJCombobox().setVisible(true);
			getUnivariateDistrParametrizationJCombobox().setEnabled(true);
		} else {
			getUnivariateDistrJCombobox().setVisible(false);
			getUnivariateDistrJCombobox().setEnabled(false);
			getUnivariateDistrParametrizationJCombobox().setVisible(false);
			getUnivariateDistrParametrizationJCombobox().setEnabled(false);

		}
		//CMF

		if (enableReorderVariableButton()) {
			getReorderVariablesButton().setVisible(true);
			getReorderVariablesButton().setEnabled(true);
		} else {
			getReorderVariablesButton().setVisible(false);
			getReorderVariablesButton().setEnabled(false);
		}
		getComponentsPanel().add(getCommentPane(), BorderLayout.SOUTH);

	}

	/**
	 * @return label for the type of relations or policy
	 */
	protected JLabel getPotentialTypeJLabel() {
		if (lblPotentialType == null) {
			lblPotentialType = new JLabel();
			lblPotentialType.setName("jLabelRelationType");
			lblPotentialType.setText("a Label");
			lblPotentialType.setText(stringDatabase.getString("NodeProbsValuesTablePanel.jLabelRelationType.Text"));
		}
		return lblPotentialType;
	}

	/**
	 * @return ComboBox with the types of families of relation to be used
	 */
	protected JComboBox<String> getPotentialTypeJCombobox() {
		if (potentialTypeComboBox == null) {
			List<String> filteredPotentialNames = potentialManager.getFilteredPotentials(node);
			Collections.sort(filteredPotentialNames);
			potentialTypeComboBox = new JComboBox<>((String[]) filteredPotentialNames.toArray(new String[0]));
			String currentPotentialType = node.getPotentials().get(0).getClass().getAnnotation(PotentialType.class).name();
			potentialTypeComboBox.setSelectedItem(currentPotentialType);
			potentialTypeComboBox.setBorder(new LineBorder(UIManager.getColor("List.dropLineColor"), 1, false));
			potentialTypeComboBox.setName("jComboBoxRelationType");
			potentialTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					potentialTypeChanged(evt);
				}
			});
            // Compute the number of columns of the conditional probability table
            int tableColumns = 1;
            for (Node parent: node.getParents()) {
                tableColumns *= parent.getVariable().getNumStates();
            }
			System.out.println(tableColumns);
            // Show small uniform potentials as table potentials. Saves clicks
            if (currentPotentialType.equals("Uniform") && tableColumns <= 128) {
                potentialTypeComboBox.setSelectedItem("Table");
            }
			// Show small uniform potentials as 'Exact' potentials. Saves clicks
			if (node.getNodeType() == NodeType.UTILITY && currentPotentialType.equals("Uniform") && tableColumns <= 128) {
				potentialTypeComboBox.setSelectedItem("Exact");
			}
			potentialTypeComboBox.setEnabled(!readOnly);
		}
		return potentialTypeComboBox;
	}

	/**
	 * Enables or disables the potential type combo box
	 *
	 * @param enable To indicate if the Potential Type combobox should be enabled
	 */
	public void setEnabledPotentialTypeCombobox(boolean enable) {
		getPotentialTypeJCombobox().setEnabled(enable);
	}

	/**
	 * Gets the panel that matches the type of potential to be edited
	 *
	 * @return the potential panel matching the potential edited.
	 */
	private PotentialPanel getPotentialPanel() {
		if (potentialPanel == null) {
			String potentialName = (String) potentialTypeComboBox.getSelectedItem();
			String potentialFamily = potentialManager.getPotentialsFamily(potentialName);
			//CMI Adaptation to deal with ExactDistrPotential too
			if (potentialName.equals("Exact")) {
				potentialPanel = PotentialPanelManager.getInstance()
						.getPotentialPanel("Table", potentialManager.getPotentialsFamily("Table"), node);

			} else
				potentialPanel = PotentialPanelManager.getInstance()
						.getPotentialPanel(potentialName, potentialFamily, node);

           
            /*
            potentialPanel = PotentialPanelManager.getInstance ().getPotentialPanel (potentialName,
                                                                                     potentialFamily,
                                                                                     node);
            */
			//CMF
			potentialPanel.setReadOnly(readOnly);
			potentialPanel.suscribePanelResizeEventListener(this);
		}
		return potentialPanel;
	}

	@Override public void setTitle(String title) {
		String nodeName = (node == null) ? "" : node.getName();
		super.setTitle(stringDatabase.getString(title) + ": " + nodeName);
	}

	/**
	 * @return An integer indicating the button clicked by the user when closing
	 * this dialog
	 * @author carmenyago
	 * carmenyago only changed the treatment of the utility potentials
	 */
	public int requestValues() {
		// Shows the potentials' options table
		if (node.getNodeType() == NodeType.DECISION && node.getPolicyType() == PolicyType.OPTIMAL && readOnly) {
			setEnabledDecisionOptions(true);
		} else {
			showFields(node);
		}
		setVisible(true);
		return selectedButton;
	}

	/**
	 * This method fills the content of the fields from a Node object. In
	 * this method, when Elvira will be discontinued, the code for
	 * discriminating discrete and discretized variables must be eliminated
	 *
	 * @param node object from where load the information.
	 */
	// TODO Remove all this
	private void showFields(Node node) {
		// The element order in PotentialType object are same that
		// JComboBoxRelationType
		previouslySelectedPotentialType = node.getPotentials().get(0).getClass().getAnnotation(PotentialType.class)
				.name();
		getPotentialTypeJCombobox().setSelectedItem(previouslySelectedPotentialType);
		updatePotentialPanel();
		// Elvira do not distinguish between DISCRETE and DISCRETIZED
		// so here we will see if there are intervals in the states
		if (Util.hasLimitBracketSymbols(node.getVariable().getStates()) && (
				node.getVariable().getVariableType() == VariableType.FINITE_STATES
		)) {
			// really DISCRETIZED, so change the value of the VariableType
			node.getVariable().setVariableType(VariableType.DISCRETIZED);
		}
		// set the nodeProperties variable in this dialog and panels
		this.node = node;
		// *******
		getPotentialPanel().setData(node);
	}

	/**
	 * @return The panel that indicates the type of the table (and perhaps the
	 * type of policy (optimal or imposed))
	 */
	protected JPanel getPotentialTypePanel() {
		if (potentialTypePanel == null) {
			potentialTypePanel = new JPanel();
			// jPanelRelationTableType.setBorder( new LineBorder( UIManager
			// .getColor( "List.dropLineColor" ), 1, false ) );
			potentialTypePanel.setLayout(new FlowLayout());
			potentialTypePanel.setSize(294, 29);
			potentialTypePanel.setName("potentialTypePanel");
			potentialTypePanel.add(getPotentialTypeJLabel());
			potentialTypePanel.add(getPotentialTypeJCombobox());
			//CMI
			//For Univariate
			potentialTypePanel.add(getUnivariateDistrTypeJLabel());
			potentialTypePanel.add(getUnivariateDistrJCombobox());
			potentialTypePanel.add(getParametrizationComboBoxJLabel());
			potentialTypePanel.add(getUnivariateDistrParametrizationJCombobox());
			//CMF
			potentialTypePanel.add(getReorderVariablesButton());
			// potentialTypePanel.add( getPoliticyTypePanel() );
			// /getPoliticyTypePanel().setVisible(false);
			// getPotentialPanel().setEnabled(false);
		}
		return potentialTypePanel;
	}

	//CMI
	//For Univariate

	/**
	 * @return label for the type of relations or policy
	 */
	protected JLabel getUnivariateDistrTypeJLabel() {
		if (lblUnivariateDistrComboBox == null) {
			lblUnivariateDistrComboBox = new JLabel();
			lblUnivariateDistrComboBox.setName("jLabelDistrType");
			lblUnivariateDistrComboBox.setText("Distribution");
			//TODO
			//lblDistrType.setText (stringDatabase.getValuesInAString ("NodeProbsValuesTablePanel.jLabelRelationType.Text"));
		}
		return lblUnivariateDistrComboBox;
	}

	private boolean showUnivariateDistrComboBox() {
		boolean enable = false;
		// We retrieve the necessary data from the node

		if (getPotentialPanel() instanceof UnivariateDistrPotentialPanel) {
			ProbDensFunctionManager probDensFunctionManager = ProbDensFunctionManager.getUniqueInstance();
			List<String> distributionUnivariateNames = probDensFunctionManager.getDistributions();
			Collections.sort(distributionUnivariateNames);
			univariateDistrComboBox
					.setModel(new DefaultComboBoxModel<String>(distributionUnivariateNames.toArray(new String[0])));
			String univariateName = ((UnivariateDistrPotential) (node.getPotentials().get(0)))
					.getProbDensFunctionUnivariateName();
			univariateDistrComboBox.setSelectedItem(univariateName);
			enable = true;
		}
		// Finally, the value of enable is returned
		return enable;
	}

	/**
	 * @return The univariate distribution JComboBox
	 */
	protected JComboBox<String> getUnivariateDistrJCombobox() {

		if (univariateDistrComboBox == null) {

			univariateDistrComboBox = new JComboBox<String>();
			univariateDistrComboBox.setBorder(new LineBorder(UIManager.getColor("List.dropLineColor"), 1, false));
			univariateDistrComboBox.setName("jComboBoxDistr");
			univariateDistrComboBox.addActionListener(new java.awt.event.ActionListener() {
				@Override public void actionPerformed(java.awt.event.ActionEvent evt) {
					String univariateName = (String) univariateDistrComboBox.getSelectedItem();
					showUnivariateDistrParametrizationComboBox(univariateName);
				}
			});
			univariateDistrComboBox.setEnabled(!readOnly);
		}
		return univariateDistrComboBox;

	}

	/**
	 * @return The univariate distribution parametrization JComboBox
	 */
	protected JComboBox<String> getUnivariateDistrParametrizationJCombobox() {

		if (univariateDistrParametrizationComboBox == null) {

			univariateDistrParametrizationComboBox = new JComboBox<String>();
			univariateDistrParametrizationComboBox
					.setBorder(new LineBorder(UIManager.getColor("List.dropLineColor"), 1, false));
			univariateDistrParametrizationComboBox.setName("jComboBoxParametrization");
			univariateDistrParametrizationComboBox.addActionListener(new java.awt.event.ActionListener() {
				@Override public void actionPerformed(java.awt.event.ActionEvent evt) {
					distributionChanged();
				}
			});
			univariateDistrParametrizationComboBox.setEnabled(!readOnly);
		}
		return univariateDistrParametrizationComboBox;

	}

	protected void distributionChanged() {

		String distributionUnivariateName = (String) univariateDistrComboBox.getSelectedItem();
		String distributionParameters = (String) univariateDistrParametrizationComboBox.getSelectedItem();
		//When we are changing the distribution the first value should be selected

		String distributionName = ProbDensFunctionManager.getUniqueInstance()
				.getDistributionName(distributionUnivariateName, distributionParameters);
		if (!previouslySelectedDistributionName.equals(distributionName)) {

			AugmentedPotentialValueEdit nodePotentialEdit = new AugmentedPotentialValueEdit(node, distributionName);
			try {
				node.getProbNet().doEdit(nodePotentialEdit);
			} catch (ConstraintViolationException e1) {
				JOptionPane.showMessageDialog(this, stringDatabase.getString(e1.getMessage()),
						stringDatabase.getString("ConstraintViolationException"), JOptionPane.ERROR_MESSAGE);
				revertPotentialTypeChange();
				potentialTypeComboBox.requestFocus();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			updatePotentialPanel();
			previouslySelectedDistributionName = distributionName;

		}

	}

	/**
	 * @return The parametrization ComboBox JLabel
	 */
	protected JLabel getParametrizationComboBoxJLabel() {
		if (lblParametrizationComboBox == null) {
			lblParametrizationComboBox = new JLabel();
			lblParametrizationComboBox.setName("jLabelDistrType");
			lblParametrizationComboBox.setText("Parametrization");
			//TODO
			//lblParametrizationComboBox.setText (stringDatabase.getValuesInAString ("NodeProbsValuesTablePanel.jLabelRelationType.Text"));
		}
		return lblParametrizationComboBox;
	}

	/**
	 * @return True iff it is enabled
	 */
	private boolean showUnivariateDistrParametrizationComboBox(String univariateName) {
		boolean enable = false;
		// We retrieve the necessary data from the node
		//UNCLEAR this if is Necessary??
		ProbDensFunctionManager probDensFunctionManager = ProbDensFunctionManager.getUniqueInstance();
		List<String[]> parametrizationDataList = probDensFunctionManager.getParametrizations(univariateName);
		List<String> parametrizationNames = new ArrayList<String>();
		for (String[] parametrizationData : parametrizationDataList) {
			parametrizationNames.add(parametrizationData[0]);
		}
		Collections.sort(parametrizationNames);
		univariateDistrParametrizationComboBox
				.setModel(new DefaultComboBoxModel<String>(parametrizationNames.toArray(new String[0])));
		String parametrizationName;
		if (!univariateName
				.equals(((UnivariateDistrPotential) node.getPotentials().get(0)).getProbDensFunctionUnivariateName())) {
			parametrizationName = parametrizationNames.get(0);
		} else {
			parametrizationName = ((UnivariateDistrPotential) node.getPotentials().get(0))
					.getProbDensFunctionParametrizationName();
		}

		univariateDistrParametrizationComboBox.setSelectedItem(parametrizationName);
		enable = true;
		// Finally, the value of enable is returned
		return enable;
	}

	//CMF

	/**
	 * @return The panel that indicates the type of the table (and perhaps the
	 * type of policy (optimal or imposed))
	 */
	protected JButton getReorderVariablesButton() {
		if (reorderVariablesButton == null) {
			reorderVariablesButton = new JButton(stringDatabase.getString("PotentialEditDialog.ReorderVariables.Text"));
			reorderVariablesButton.setName("reorderVariablesButton");
			// reorderVariablesButton.setVisible(false);
			reorderVariablesButton.addActionListener(this);
		}
		return reorderVariablesButton;
	}

	/**
	 * This method initializes getCommentPane
	 *
	 * @return a new comment HTML scroll pane.
	 */
	private CommentHTMLScrollPane getCommentPane() {

		if (commentPane == null) {
			commentPane = new CommentHTMLScrollPane();
			commentPane.setName("commentPane");
			commentPane.setPreferredSize(new Dimension(10, 30));
		}
		return commentPane;
	}

	/**
	 * @return PolicyTypePanel with three radio buttons with the types of
	 * policy: optimal, deterministic, or probabilistic
	 */
	protected PolicyTypePanel getPoliticyTypePanel() {
		if (pnlPolicyType == null) {
			pnlPolicyType = new PolicyTypePanel(this, node);
		}
		return pnlPolicyType;
	}

	protected void potentialTypeChanged(ActionEvent evt) {
		String potentialType = (String) potentialTypeComboBox.getSelectedItem();
		if (!previouslySelectedPotentialType.equals(potentialType)) {
			SetPotentialEdit setPotentialEdit = new SetPotentialEdit(node, potentialType);
			try {
				node.getProbNet().doEdit(setPotentialEdit);
			} catch (ConstraintViolationException e1) {
				JOptionPane.showMessageDialog(this, stringDatabase.getString(e1.getMessage()),
						stringDatabase.getString("ConstraintViolationException"), JOptionPane.ERROR_MESSAGE);
				revertPotentialTypeChange();
				potentialTypeComboBox.requestFocus();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			updatePotentialPanel();
			previouslySelectedPotentialType = potentialType;
			optionPreviouslySelected = potentialTypeComboBox.getSelectedIndex();
			getComponentsPanel().add(getPotentialPanel(), BorderLayout.CENTER);
			getComponentsPanel().updateUI();
			getComponentsPanel().repaint();
			this.repaint();
			this.pack();

		}
	}

	/**
	 * This method carries out the actions when the user presses the OK button
	 * before hiding the dialog.
	 *
	 * @return true if all the fields are correct.
	 */
	@Override protected boolean doOkClickBeforeHide() {
		if (getPotentialPanel() instanceof TablePotentialPanel) {
			((TablePotentialPanel) getPotentialPanel()).getValuesTable().stopCellEditing();
		}
		if (getPotentialPanel() instanceof ICIPotentialsTablePanel) {
			((ICIPotentialsTablePanel) getPotentialPanel()).getICIValuesTable().stopCellEditing();
		}
		getPotentialPanel().saveChanges();
		if (commentPane.isChanged()) {
			// check if the comment is empty
			String comment = commentPane.isEmpty() ? "" : commentPane.getCommentText();
			node.getPotentials().get(0).setComment(comment);
		}
		node.getProbNet().getPNESupport().closeParenthesis();
		return true;
	}

	@Override protected void doCancelClickBeforeHide() {
		getPotentialPanel().close();
		node.getProbNet().getPNESupport().closeParenthesis();
	}

	/**
	 * Update potential panel
	 */
	public void updatePotentialPanel() {
		getComponentsPanel().remove(getPotentialPanel());
		potentialPanel.close();
		potentialPanel = null;
		//CMI
		// For Univariate
		if (showUnivariateDistrComboBox()) {
			getUnivariateDistrTypeJLabel().setVisible(true);
			getUnivariateDistrJCombobox().setVisible(true);
			getUnivariateDistrJCombobox().setEnabled(true);
			getParametrizationComboBoxJLabel().setVisible(true);
			getUnivariateDistrParametrizationJCombobox().setVisible(true);
			getUnivariateDistrParametrizationJCombobox().setEnabled(true);
		} else {
			getUnivariateDistrTypeJLabel().setVisible(false);
			getUnivariateDistrJCombobox().setVisible(false);
			getUnivariateDistrJCombobox().setEnabled(false);

			getParametrizationComboBoxJLabel().setVisible(false);
			getUnivariateDistrParametrizationJCombobox().setVisible(false);
			getUnivariateDistrParametrizationJCombobox().setEnabled(false);

		}
		//CMF

		if (enableReorderVariableButton()) {
			getReorderVariablesButton().setVisible(true);
			getReorderVariablesButton().setEnabled(true);
		} else {
			getReorderVariablesButton().setVisible(false);
			getReorderVariablesButton().setEnabled(false);
		}

		getComponentsPanel().add(getPotentialPanel(), BorderLayout.CENTER);
		getComponentsPanel().updateUI();
		getComponentsPanel().repaint();
		this.repaint();
		this.pack();
	}

	/**
	 * Shows and activates the options related to decision policy
	 *
	 * @param show To indicate whether the options have to be shown and enabled or not
	 */
	private void setEnabledDecisionOptions(boolean show) {
		if (show) {
			switch (node.getPolicyType()) {
			case OPTIMAL:
				getPotentialTypeJCombobox().setEnabled(false);
				break;
			case DETERMINISTIC:
				getPotentialTypeJCombobox().setEnabled(false);
				break;
			case PROBABILISTIC:
				Potential potential = node.getPotentials().get(0);
				// TODO definir el comportamiento para los demás tipos de potenciales
				if (potential instanceof UniformPotential || potential instanceof TablePotential) {
					getPotentialTypeJCombobox()
							.setSelectedItem(potential.getClass().getAnnotation(PotentialType.class).name());
					// getJComboBoxRelationType().setEnabled(false);
				}
				break;
			}
		}
		getPoliticyTypePanel().setEnabledDecisionOptions(show);
	}

	public void revertPotentialTypeChange() {
		getPotentialTypeJCombobox().setSelectedIndex(optionPreviouslySelected);
	}

	/**
	 * @return the readOnly
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	@Override public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(reorderVariablesButton)) {
			actionPerformedReorderVariables();
		}
	}

	protected void actionPerformedReorderVariables() {
		ReorderVariablesDialog reorderVariablesDialog = new ReorderVariablesDialog(this, node);
		if (reorderVariablesDialog.requestValues() == NodePropertiesDialog.OK_BUTTON) {
			List<Variable> newVariables = reorderVariablesDialog.getReorderVariablesPanel().getVariables();

			//CMI
			//For Univariate
			if (getPotentialPanel() instanceof UnivariateDistrPotentialPanel) {
				Potential potential = DiscretePotentialOperations
						.reorder((UnivariateDistrPotential) node.getPotentials().get(0), newVariables);
				SetPotentialEdit potentialEdit = new SetPotentialEdit(node, potential);
				try {
					node.getProbNet().doEdit(potentialEdit);
				} catch (DoEditException | ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				updatePotentialPanel();

			} else if (getPotentialPanel() instanceof AugmentedTablePotentialPanel) {
				Potential potential = DiscretePotentialOperations
						.reorder((AugmentedTablePotential) node.getPotentials().get(0), newVariables);
				SetPotentialEdit potentialEdit = new SetPotentialEdit(node, potential);
				try {
					node.getProbNet().doEdit(potentialEdit);
				} catch (DoEditException | ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				updatePotentialPanel();

			} else
				//CMF

				if (getPotentialPanel() instanceof TablePotentialPanel) {
					// if (node.getPotentials().get(0) instanceof
					// TablePotential) {
					Potential potential = DiscretePotentialOperations
							.reorder((TablePotential) node.getPotentials().get(0), newVariables);
					SetPotentialEdit potentialEdit = new SetPotentialEdit(node, potential);
					try {
						node.getProbNet().doEdit(potentialEdit);
					} catch (DoEditException | ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					updatePotentialPanel();
				} else if (getPotentialPanel() instanceof ICIPotentialsTablePanel) {
					SetPotentialVariablesEdit setPotentialVariables = new SetPotentialVariablesEdit(node, newVariables);
					try {
						node.getProbNet().doEdit(setPotentialVariables);
					} catch (DoEditException | ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					updatePotentialPanel();
				}
		}
	}

	@Override public void panelSizeChanged(PanelResizeEvent event) {
		pack();
		repaint();
	}

	/**
	 * This method computes if reorderVariableButton should be enabled
	 *
	 * @return true if the ReorderVariableButton should be enabled
	 */
	private boolean enableReorderVariableButton() {
		boolean enable = false;
		// We retrieve the necessary data from the node
		Potential potential = node.getPotentials().get(0);
		int numPotentialVariables = potential.getNumVariables();

		if ((numPotentialVariables > 2) && getPotentialPanel() instanceof ProbabilityTablePanel) {
			enable = true;
		}
		// Finally, the value of enable is returned
		return enable;
	}

}
