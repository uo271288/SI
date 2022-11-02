/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.edition;

import org.apache.logging.log4j.LogManager;
import org.openmarkov.core.action.AddNodeEdit;
import org.openmarkov.core.action.AbsorbParentsEdit;
import org.openmarkov.core.action.AbsorbNodeEdit;

import org.openmarkov.core.action.InvertLinkAndUpdatePotentialsEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;

import org.openmarkov.core.exception.NoFindingException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceAlgorithm;
import org.openmarkov.core.inference.annotation.InferenceManager;
import org.openmarkov.core.inference.tasks.OptimalPolicies;
import org.openmarkov.core.inference.tasks.Propagation;
import org.openmarkov.core.inference.tasks.TaskUtilities;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.*;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.core.oopn.Instance.ParameterArity;
import org.openmarkov.gui.action.PasteEdit;
import org.openmarkov.gui.action.RemoveSelectedEdit;
import org.openmarkov.gui.dialog.PropagationOptionsDialog;
import org.openmarkov.gui.dialog.inference.temporalevolution.TemporalEvolutionDialog;
import org.openmarkov.gui.dialog.link.LinkRestrictionEditDialog;
import org.openmarkov.gui.dialog.link.RevelationArcEditDialog;
import org.openmarkov.gui.dialog.network.NetworkPropertiesDialog;
import org.openmarkov.gui.dialog.node.AddFindingDialog;
import org.openmarkov.gui.dialog.node.CommonNodePropertiesDialog;
import org.openmarkov.gui.dialog.node.NodePropertiesDialog;
import org.openmarkov.gui.dialog.node.PotentialEditDialog;
import org.openmarkov.gui.graphic.FSVariableBox;
import org.openmarkov.gui.graphic.InnerBox;
import org.openmarkov.gui.graphic.NumericVariableBox;
import org.openmarkov.gui.graphic.SelectionListener;
import org.openmarkov.gui.graphic.VisualDecisionNode;
import org.openmarkov.gui.graphic.VisualElement;
import org.openmarkov.gui.graphic.VisualLink;
import org.openmarkov.gui.graphic.VisualNetwork;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.graphic.VisualState;
import org.openmarkov.gui.localize.LocalizedException;
import org.openmarkov.gui.localize.StringDatabase;
import org.openmarkov.gui.menutoolbar.menu.ContextualMenu;
import org.openmarkov.gui.menutoolbar.menu.ContextualMenuFactory;
import org.openmarkov.gui.util.Utilities;
import org.openmarkov.gui.window.MainPanelMenuAssistant;
import org.openmarkov.gui.window.edition.mode.EditionMode;
import org.openmarkov.gui.window.edition.mode.EditionModeManager;
import org.openmarkov.inference.dlimidevaluation.StrategyManager;
import org.openmarkov.inference.variableElimination.tasks.VEEvaluation;
import org.openmarkov.inference.variableElimination.tasks.VEExpectedUtilityDecision;
import org.openmarkov.inference.variableElimination.tasks.VEPropagation;

import javax.swing.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * This class implements the behaviour of a panel where a network will be
 * edited.
 *
 * @author jmendoza
 * @author jlgozalo
 * @version 1.3 - asaez - Functionality added: - Explanation capabilities, -
 *          Management of working modes (edition/inference), - Expansion and
 *          contraction of nodes, - Introduction and elimination of evidence -
 *          Management of multiple evidence cases.
 */
public class EditorPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
	/**
	 * Static field for serializable class.
	 */
	private static final long serialVersionUID = 2789011585460326400L;
	/**
	 * Maximum width of the panel.
	 */
	private static final double MAX_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().getWidth() * 20;
	/**
	 * Maximum height of the panel.
	 */
	private static final double MAX_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 20;
	/**
	 * Constant that indicates the value of the Expansion Threshold by default.
	 */
	// This should be in a future a configuration option that should be read on
	// start
	private static final int DEFAULT_THRESHOLD_VALUE = 5;
	protected ProbNet probNet;
	/**
	 * Object to convert coordinates of the screen to the panel and vice versa.
	 */
	protected Zoom zoom;
	/**
	 * Visual representation of the network
	 */
	protected VisualNetwork visualNetwork = null;
	/**
	 * Position of the mouse cursor when it is pressed.
	 */
	protected Point2D.Double cursorPosition = new Point2D.Double();
	/**
	 * String database
	 */
	protected StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
	/**
	 * Object Dialog for potentials edition
	 */
	PotentialEditDialog potentialsDialog = null;
	/****
	 * Dialog for link restriction edition
	 */
	LinkRestrictionEditDialog linkRestrictionDialog = null;
	/***
	 * Dialog for revelation arc edition
	 */
	RevelationArcEditDialog revelationArcDialog = null;
	/**
	 * Maximum width of the panel.
	 */
	private double currentWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth() * 20;
	/**
	 * Maximum height of the panel.
	 */
	private double currentHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 20;
	/**
	 * Current edition mode.
	 */
	private EditionMode editionMode = null;
	/**
	 * This variable indicates which is the expansion threshold of the network
	 */
	private double currentExpansionThreshold = DEFAULT_THRESHOLD_VALUE;
	/**
	 * Network panel associated to this editor panel
	 */
	private NetworkPanel networkPanel = null;
	/**
	 * Pre resolution evidence
	 */
	private EvidenceCase preResolutionEvidence;
	/**
	 * Array of Evidence cases treated for this editor panel
	 */
	private List<EvidenceCase> postResolutionEvidence;
	/**
	 * Each position of this array indicates if the corresponding evidence case is
	 * currently compiled (if true) or not (if false)
	 */
	private List<Boolean> evidenceCasesCompilationState;
	/**
	 * Minimum value of the range of each utility node.
	 */
	private HashMap<Variable, Double> minUtilityRange;
	/**
	 * Maximum value of the range of each utility node.
	 */
	private HashMap<Variable, Double> maxUtilityRange;
	/**
	 * This variable indicates which is the evidence case that is currently being
	 * treated
	 */
	private int currentCase;
	/**
	 * Inference manager
	 */
	private InferenceManager inferenceManager = null;
	/**
	 * Inference algorithm used to evaluate this network
	 */
	private InferenceAlgorithm inferenceAlgorithm = null;
	/**
	 * This variable indicates if the propagation mode is automatic or manual.
	 */
	private boolean automaticPropagation;
	/**
	 * This variable indicates if propagation should be done right now (if being in
	 * Inference Mode).
	 */
	private boolean propagationActive;
	/**
	 * This variable indicates if it has been a change in the properties or in the
	 * potential values in some node.
	 */
	private boolean networkChanged = true;
	/**
	 * Listener that listen to the changes of size.
	 */
	private HashSet<EditorPanelSizeListener> sizeListeners = new HashSet<EditorPanelSizeListener>();
	/**
	 * Object that creates the contextual menus.
	 */
	private ContextualMenuFactory contextualMenuFactory = null;
	/**
	 * Object that assists this panel in the operations with the clipboard.
	 */
	private EditorPanelClipboardAssistant clipboardAssistant = null;
	private EditionModeManager editionModeManager;
	private boolean approximateInferenceWarningGiven = false;
	private boolean canBeExpanded = false;

	/**
	 * Constructor that creates the instance.
	 *
	 * @param networkPanel network that will be edited.
	 */
	public EditorPanel(NetworkPanel networkPanel, VisualNetwork visualNetwork) {
		zoom = new Zoom();
		// super();
		this.networkPanel = networkPanel;
		this.probNet = networkPanel.getProbNet();
		this.visualNetwork = visualNetwork;
		automaticPropagation = true;
		propagationActive = true;
		preResolutionEvidence = new EvidenceCase();
		postResolutionEvidence = new ArrayList<EvidenceCase>(1);
		currentCase = 0;
		EvidenceCase evidenceCase = new EvidenceCase();
		postResolutionEvidence.add(currentCase, evidenceCase);
		evidenceCasesCompilationState = new ArrayList<Boolean>(1);
		evidenceCasesCompilationState.add(currentCase, false);
		minUtilityRange = new HashMap<Variable, Double>();
		maxUtilityRange = new HashMap<Variable, Double>();
		initialize();
		inferenceManager = new InferenceManager();
		editionModeManager = new EditionModeManager(this, probNet);
		editionMode = editionModeManager.getDefaultEditionMode();
	}

	/**
	 * This method requests to the user the additionalProperties of a network.
	 *
	 * @param owner   window that owns the dialog box.
	 * @param probNet the network from where the properties are retrieved
	 * @return true, if the user has made changes on the additionalProperties;
	 *         otherwise, false.
	 */
	public static boolean requestNetworkProperties(Window owner, ProbNet probNet) {
		NetworkPropertiesDialog dialogProperties = new NetworkPropertiesDialog(owner, probNet);
		return (dialogProperties.showProperties() == NetworkPropertiesDialog.OK_BUTTON);
	}

	/**
	 * This method initializes this instance.
	 */
	private void initialize() {
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		this.setBackground(Color.white);
		// adjustPanelDimension ();
		setZoomToFitNetwork();
		clipboardAssistant = new EditorPanelClipboardAssistant();
	}

	/**
	 * Returns the presentation mode of the text of the nodes.
	 *
	 * @return true if the title of the nodes is the name or false if it is the
	 *         name.
	 */
	public boolean getByTitle() {
		return visualNetwork.getByTitle();
	}

	/**
	 * Changes the presentation mode of the text of the nodes.
	 *
	 * @param value new value of the presentation mode of the text of the nodes.
	 */
	public void setByTitle(boolean value) {
		visualNetwork.setByTitle(value);
		adjustPanelDimension();
		repaint();
	}

	/**
	 * Overwrite 'paint' method to avoid to call it explicitly.
	 *
	 * @param g the graphics context in which to paint.
	 */
	@Override
	public void paint(Graphics g) {
		Graphics2D g2D = (Graphics2D) g;
		super.paint(g);
		g2D.scale(zoom.getZoom(), zoom.getZoom());
		visualNetwork.paint(g2D);
	}

	/**
	 * Returns the edition mode.
	 *
	 * @return edition mode.
	 */
	public EditionMode getEditionMode() {
		return editionMode;
	}

	/**
	 * Changes the state of the edition and carries out the necessary actions in
	 * each case.
	 *
	 * @param newEditionModeName new edition mode state.
	 */
	public void setEditionMode(String newEditionModeName) {
		EditionMode newEditionMode = editionModeManager.getEditionMode(newEditionModeName);
		if (!editionMode.equals(newEditionMode)) {
			setCursor(editionModeManager.getCursor(newEditionModeName));
			visualNetwork.setSelectedAllObjects(false);
			editionMode = newEditionMode;
			repaint();
		}
	}

	/**
	 * Selects all nodes and links.
	 */
	public void selectAllObjects() {
		visualNetwork.setSelectedAllObjects(true);
		repaint();
	}

	/**
	 * Notifies to the registered size listener (if any) that the panel's size has
	 * changed.
	 *
	 * @param incrLeft   increase for the left side.
	 * @param incrTop    increase overhead.
	 * @param incrRight  increase for the right side.
	 * @param incrBottom increase for below.
	 */
	@SuppressWarnings("unused")
	private void notifySizeChanged(double incrLeft, double incrTop, double incrRight, double incrBottom) {
		for (EditorPanelSizeListener listener : sizeListeners) {
			listener.sizeChanged(incrLeft, incrTop, incrRight, incrBottom);
		}
	}

	/**
	 * Invoked when a mouse button has been clicked (pressed and released) on the
	 * component.
	 *
	 * @param e mouse event information.
	 */
	public void mouseClicked(MouseEvent e) {
	}

	/**
	 * Invoked when a mouse button has been pressed on the component.
	 *
	 * @param e mouse event information.
	 */
	public void mousePressed(MouseEvent e) {
		// requestFocusInWindow(); Activate if nodes can't be moved by arrows.
		Graphics2D g = (Graphics2D) getGraphics();
		cursorPosition.setLocation(zoom.screenToPanel(e.getX()), zoom.screenToPanel(e.getY()));
		// Specific functionality depending on the edition mode;
		editionMode.mousePressed(e, cursorPosition, g);
		// Generic functionality regardless of the edition mode
		VisualNode node = null;
		VisualLink link = null;
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (e.getClickCount() == 2) {
				if (Utilities.noMouseModifiers(e)) {
					if (networkPanel.getWorkingMode() == NetworkPanel.EDITION_WORKING_MODE) {
						// If we are in Edition Mode a double click must open
						// the corresponding properties dialog (for node, link
						// or network)
						if ((node = visualNetwork.whatNodeInPosition(cursorPosition, g)) != null) {
							changeNodeProperties(node);
						} else if ((link = visualNetwork.whatLinkInPosition(cursorPosition, g)) != null) {
							changeLinkProperties(link);
						} else {
							changeNetworkProperties();
						}
					} else {
						// If we are in Inference Mode a double click inside a
						// visual state of a node without pre-resolution finding
						// must introduce evidence in that node.
						// If the double click is inside a node but outside its
						// inner box (in its 'expanded external shape'), its
						// properties dialog should be open
						if (visualNetwork.whatStateInPosition(cursorPosition, g) != null) {
							VisualNode visualNode = visualNetwork.whatNodeInPosition(cursorPosition, g);
							if (visualNode.isPreResolutionFinding()) {
								JOptionPane.showMessageDialog(Utilities.getOwner(this),
										"This node has a Pre-Resolution Finding that cannot be modified in Inference Mode.",
										stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
								// TODO //...asaez...Internacionalizar la
								// sentencia
							} else {
								VisualState visualState = visualNetwork.whatStateInPosition(cursorPosition, g);
								toggleFinding(visualNode, visualState);
							}
						} else {
							if ((visualNetwork.whatNodeInPosition(cursorPosition, g) != null)
									&& (visualNetwork.whatInnerBoxInPosition(cursorPosition, g) == null)) {
								changeNodeProperties();
							}
						}
					}
				}
			} else if (e.isAltDown()) {
				if ((node = visualNetwork.whatNodeInPosition(cursorPosition, g)) != null) {
					if (!node.isSelected()) {
						visualNetwork.setSelectedAllObjects(false);
						visualNetwork.setSelectedNode(node, true);
					}
					showPotentialDialog(networkPanel.getWorkingMode() != NetworkPanel.EDITION_WORKING_MODE);
				}
			}
		} else if (SwingUtilities.isRightMouseButton(e)) {
			showContextualMenu(e, g);
		}
		repaint();
	}

	/**
	 * Shows contextual menu
	 *
	 * @param e MouseEvent
	 * @param g Graphics2D
	 */
	private void showContextualMenu(MouseEvent e, Graphics2D g) {
		VisualElement selectedElement = visualNetwork.getElementInPosition(cursorPosition, g);
		ContextualMenu contextualMenu = null;
		if (selectedElement != null) {
			contextualMenu = getContextualMenu(selectedElement, this);
			visualNetwork.selectElement(selectedElement);
		} else {
			canBeExpanded = probNet.thereAreTemporalNodes();
			contextualMenu = contextualMenuFactory.getNetworkContextualMenu(canBeExpanded);
		}
		contextualMenu.show(this, e.getX(), e.getY());
	}

	/**
	 * Invoked when a mouse button is pressed on a component and then dragged.
	 *
	 * @param e mouse event information.
	 */
	public void mouseDragged(MouseEvent e) {
		Graphics2D g = (Graphics2D) getGraphics();
		Point2D.Double point = new Point2D.Double(zoom.screenToPanel(e.getX()), zoom.screenToPanel(e.getY()));
		double diffX = point.getX() - cursorPosition.getX();
		double diffY = point.getY() - cursorPosition.getY();
		cursorPosition.setLocation(point);
		editionMode.mouseDragged(e, point, diffX, diffY, g);
	}

	/**
	 * Invoked when a mouse button has been released on the component.
	 *
	 * @param e mouse event information.
	 */
	public void mouseReleased(MouseEvent e) {
		Graphics2D g = (Graphics2D) getGraphics();
		Point2D.Double position = new Point2D.Double(zoom.screenToPanel(e.getX()), zoom.screenToPanel(e.getY()));
		editionMode.mouseReleased(e, position, g);
	}

	/**
	 * Invoked when the mouse button enters the component.
	 *
	 * @param e mouse event information.
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * Invoked when the mouse button exits the component.
	 *
	 * @param e mouse event information.
	 */
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Invoked when the mouse cursor has been moved onto a component but no buttons
	 * have been pushed.
	 *
	 * @param e mouse event information.
	 */
	public void mouseMoved(MouseEvent e) {
	}

	/**
	 * This method allows to an object to be registered as size listener.
	 *
	 * @param l size listener.
	 */
	public void addEditorPanelSizeListener(EditorPanelSizeListener l) {
		sizeListeners.add(l);
	}

	/**
	 * Return the height of the panel after applying the zoom.
	 *
	 * @return height of the panel after applying the zoom.
	 */
	double getNewHeight() {
		return zoom.panelToScreen(currentHeight);
	}

	/**
	 * Return the width of the panel after applying the zoom.
	 *
	 * @return width of the panel after applying the zoom.
	 */
	double getNewWidth() {
		return zoom.panelToScreen(currentWidth);
	}

	/**
	 * Returns the value of the zoom.
	 *
	 * @return actual value of zoom.
	 */
	public double getZoom() {
		return zoom.getZoom();
	}

	/**
	 * Changes the value of the zoom.
	 *
	 * @param value new zoom.
	 */
	public void setZoom(double value) {
		Dimension newDimension = null;
		Double dd = new Double(zoom.getZoom());
		Double dd1 = new Double(value);
		if (dd.compareTo(dd1) != 0) { // jlgozalo. 24/08 fix condition to !=
			zoom.setZoom(value);
			newDimension = new Dimension((int) Math.round(getNewWidth()), (int) Math.round(getNewHeight()));
			setPreferredSize(newDimension);
			setSize(newDimension);
			adjustPanelDimension();
			repaint();
		}
	}

	/**
	 * This method performs a undo or redo operation.
	 *
	 * @param undoOperation if true, an undo must be performed; if false, a redo
	 *                      will be performed.
	 * @throws CannotUndoException if undo can't be performed.
	 * @throws CannotRedoException if redo can't be performed.
	 */
	private void undoRedo(boolean undoOperation) throws CannotUndoException, CannotRedoException {
		visualNetwork.setSelectedAllObjects(false);
		if (undoOperation) {
			probNet.getPNESupport().undo();
			// undoManager.undo();
		} else {
			// undoManager.redo();
			probNet.getPNESupport().redo();
		}
		adjustPanelDimension();
		repaint();
	}

	/**
	 * This method performs a undo operation.
	 *
	 * @throws CannotUndoException if undo can't be performed.
	 */
	public void undo() throws CannotUndoException {
		undoRedo(true);
	}

	/**
	 * This method performs a redo operation.
	 *
	 * @throws CannotRedoException if redo can't be performed.
	 */
	public void redo() throws CannotRedoException {
		undoRedo(false);
	}

	/**
	 * Sets a new selection listener.
	 *
	 * @param listener listener to be set.
	 */
	public void addSelectionListener(SelectionListener listener) {
		visualNetwork.addSelectionListener(listener);
	}

	/**
	 * Sets a new contextual menu factory.
	 *
	 * @param newContextualMenuFactory contextual menu factory to be set.
	 */
	public void setContextualMenuFactory(ContextualMenuFactory newContextualMenuFactory) {
		contextualMenuFactory = newContextualMenuFactory;
	}

	/**
	 * Retrieves the contextual menu that corresponds to the selectedElement.
	 *
	 * @return the contextual menu corresponding the the parameter.
	 */
	private ContextualMenu getContextualMenu(VisualElement selectedElement, EditorPanel panel) {
		return (contextualMenuFactory != null) ? contextualMenuFactory.getContextualMenu(selectedElement, panel) : null;
	}

	/**
	 * Returns the number of selected nodes.
	 *
	 * @return number of selected nodes.
	 */
	public int getSelectedNodesNumber() {
		return visualNetwork.getSelectedNodesNumber();
	}

	/**
	 * Returns the number of selected links.
	 *
	 * @return number of selected links.
	 */
	public int getSelectedLinksNumber() {
		return visualNetwork.getSelectedLinksNumber();
	}

	/**
	 * Returns a list containing the selected nodes.
	 *
	 * @return a list containing the selected nodes.
	 */
	public List<VisualNode> getSelectedNodes() {
		return visualNetwork.getSelectedNodes();
	}

	/**
	 * Returns a list containing the selected links.
	 *
	 * @return a list containing the selected links.
	 */
	public List<VisualLink> getSelectedLinks() {
		return visualNetwork.getSelectedLinks();
	}

	/**
	 * Selects or deselects all nodes of the network.
	 *
	 * @param selected new selection state.
	 */
	public void setSelectedAllNodes(boolean selected) {
		visualNetwork.setSelectedAllNodes(selected);
	}

	/**
	 * Selects or deselects all objects of the network.
	 *
	 * @param selected new selection state.
	 */
	public void setSelectedAllObjects(boolean selected) {
		visualNetwork.setSelectedAllObjects(selected);
	}

	/**
	 * This method absorbs a node into the rest of the net arc-reversal style. This
	 * means updating the only utility child it might have and removing it next.
	 */
	public void absorbNode() {
		Node node = getSelectedNode();
		try {
			AbsorbNodeEdit absorbNode = new AbsorbNodeEdit(probNet, node.getVariable());
			probNet.doEdit(absorbNode);
		} catch (DoEditException | ConstraintViolationException | WrongCriterionException
				| NonProjectablePotentialException e) {
			e.printStackTrace();
		}

		repaint();

	}

	private Node getSelectedNode() {
		List<VisualNode> selectedNodes = visualNetwork.getSelectedNodes();
		Node node;
		if (selectedNodes.size() == 1) { // Always happens
			node = selectedNodes.get(0).getNode();
		} else {
			throw new RuntimeException();
		}
		return node;
	}

	/**
	 * TODO: Fill as desired
	 */
	public void absorbParents() {

		Node node = getSelectedNode();
		try {
			AbsorbParentsEdit absorbParents = new AbsorbParentsEdit(probNet, node);
			probNet.doEdit(absorbParents);
		} catch (DoEditException | ConstraintViolationException | WrongCriterionException
				| NonProjectablePotentialException e) {
			e.printStackTrace();
		}

		repaint();
	}

	/**
	 * This method shows a dialog box with the additionalProperties of a node. If
	 * some property has changed, insert a new undo point into the network undo
	 * manager.
	 *
	 * @param selectedNode
	 */
	public void changeNodeProperties(VisualNode selectedNode) {
		if (requestNodePropertiesToUser2(Utilities.getOwner(this), selectedNode.getNode(), false)) {
			adjustPanelDimension();
			selectedNode.update(postResolutionEvidence.size());
			repaint();
			networkChanged = true;
			removeNodeEvidenceInAllCases(selectedNode.getNode());
		} else
			probNet.getPNESupport().undoAndDelete();
	}

	public void changeNodeProperties() {
		List<VisualNode> selectedNodes = visualNetwork.getSelectedNodes();
		if (selectedNodes.size() == 1) {
			changeNodeProperties(selectedNodes.get(0));
		}
	}

	/**
	 *
	 */
	public void showPotentialDialog(boolean readOnly) {
		List<VisualNode> selectedNodes = visualNetwork.getSelectedNodes();
		Node node = selectedNodes.get(0).getNode();
		// Before showing the dialog, we check if the network was alreday modified
		Boolean alreadyModifiedNetwork = networkPanel.getModified();
		/*
		 * Potential oldPotential = node.getPotentials().get(0); PotentialEditDialog
		 * dialog = new PotentialEditDialog(owner, oldPotential, newElement); Potential
		 * newPotential = dialog.getNewPotential(); if ( newPotential != null ) { new
		 * edit = new ChangeNodePotentialEdit(newPotential);//sets the potential in the
		 * node pNESupport.doedit //probnet PNESuport, inside panels PNESupports will be
		 * owned by the edit dialog adjustPanelDimension(); repaint(); networkChanged =
		 * true; }
		 */
		if (requestPotentialValues(Utilities.getOwner(this), node, false, readOnly)) {
			// if the user has selected the ok button when closing the dialog
			adjustPanelDimension();
			repaint();
			networkChanged = true;
			removeNodeEvidenceInAllCases(node);
		} else {
			probNet.getPNESupport().undoAndDelete();
			// We restore the network state to not modified, if it was not already modified
			if (!alreadyModifiedNetwork) {
				setNetworkChangedWithOutEdit(false);
				setSelectedAllNodes(false);
				repaint();
			}
		}
	}

	/**
	 * This method requests to the user the additionalProperties of a node.
	 *
	 * @param owner   owner window that shows the dialog box.
	 * @param node    object that contains the additionalProperties of the node and
	 *                where changes will be saved.
	 * @param newNode specifies if the node whose additionalProperties are going to
	 *                be edited is new.
	 * @return true, if the user save the changes on node; otherwise, false.
	 */
	private boolean requestNodePropertiesToUser2(Window owner, Node node, boolean newNode) {
		NodePropertiesDialog nodePropertiesDialog = new CommonNodePropertiesDialog(owner, node, newNode);
		return (nodePropertiesDialog.requestProperties() == NodePropertiesDialog.OK_BUTTON);
	}

	private boolean requestPotentialValues(Window owner, Node node, boolean newNode, boolean readOnly) {
		potentialsDialog = new PotentialEditDialog(owner, node, newNode, readOnly);
		return (potentialsDialog.requestValues()// to know if the user has
				// selected the ok button when
				// closing the dialog
				== NodePropertiesDialog.OK_BUTTON);
	}

	/**
	 * This method requests to the user the link restriction properties of a link.
	 *
	 * @param owner owner window that shows the dialog box.
	 * @param link  object that contains the link restriction properties of the link
	 *              and where changes will be saved.
	 * @return true, if the user save the changes on node; otherwise, false.
	 */
	private boolean requestLinkRestrictionValues(Window owner, Link<Node> link) {
		linkRestrictionDialog = new LinkRestrictionEditDialog(owner, link);
		return (linkRestrictionDialog.requestValues() == NodePropertiesDialog.OK_BUTTON);
	}

	// private boolean requestCostEffectiveness(Window owner,
	// String suffixTypeAnalysis, boolean isProbabilistic) {
	// costEffectivenessDialog = new CostEffectivenessDialog(owner);
	// costEffectivenessDialog.showSimulationsNumberElements(isProbabilistic);
	// return (costEffectivenessDialog.requestData(probNet.getName(),
	// suffixTypeAnalysis) == CostEffectivenessDialog.OK_BUTTON);
	// }

	/**
	 * This method requests to the user the revelation arc properties of a link.
	 *
	 * @param owner owner window that shows the dialog box.
	 * @param link  object that contains the revelation arc properties of the link
	 *              and where changes will be saved.
	 * @return true, if the user save the changes on node; otherwise, false.
	 */
	private boolean requestRevelationArcValues(Window owner, Link<Node> link) {
		revelationArcDialog = new RevelationArcEditDialog(owner, link);
		return (revelationArcDialog.requestValues() == NodePropertiesDialog.OK_BUTTON);
	}

	/**
	 * This method shows a dialog box with the additionalProperties of a link. If
	 * some property has changed, insert a new undo point into the network undo
	 * manager.
	 *
	 * @param link
	 */
	public void changeLinkProperties(VisualLink link) {
		/*
		 * This method must be implemented to activate the possibility of editing the
		 * additionalProperties of a link in future versions.
		 */
	}

	/**
	 * This method shows a dialog box with the additionalProperties of the network.
	 * If some property has changed, insert a new undo point into the network undo
	 * manager.
	 */
	public void changeNetworkProperties() {
		// TODO be careful with local pNESupport and extern pNESupport
		if (!requestNetworkProperties(Utilities.getOwner(this), probNet)) {
			probNet.getPNESupport().undoAndDelete();
		}
	}

	/**
	 * This method copies the selected nodes to the clipboard.
	 *
	 * @param cut if true, the nodes copied to the clipboard are also removed.
	 */
	public void exportToClipboard(boolean cut) {
		if (clipboardAssistant == null) {
			JOptionPane.showMessageDialog(Utilities.getOwner(this),
					stringDatabase.getString("ClipboardNotSet.Text.Label"),
					stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
		} else {
			List<Node> selectedNodes = new ArrayList<Node>();
			for (VisualNode visualNode : visualNetwork.getSelectedNodes()) {
				selectedNodes.add(visualNode.getNode());
			}
			List<Link<Node>> selectedLinks = new ArrayList<>();
			for (VisualLink visualLink : visualNetwork.getSelectedLinks()) {
				selectedLinks.add(visualLink.getLink());
			}
			SelectedContent copiedContent = new SelectedContent(selectedNodes, selectedLinks);
			if (!copiedContent.isEmpty()) {
				clipboardAssistant.copyToClipboard(copiedContent);
				if (cut) {
					removeSelectedObjects();
				}
			}
		}
	}

	/**
	 * This method imports the content from the clipboard and creates it in the
	 * network.
	 */
	public void pasteFromClipboard() {
		if (clipboardAssistant == null) {
			JOptionPane.showMessageDialog(Utilities.getOwner(this),
					stringDatabase.getString("ClipboardNotSet.Text.Label"),
					stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
		} else {
			if (clipboardAssistant.isThereDataStored()) {
				visualNetwork.setSelectedAllObjects(false);
				SelectedContent clipboardContent = clipboardAssistant.paste();
				PasteEdit pasteEdit = new PasteEdit(visualNetwork, clipboardContent);
				try {
					probNet.doEdit(pasteEdit);
					// Set the nodes and links we just pasted as selected
					SelectedContent pastedContent = pasteEdit.getPastedContent();
					for (Node node : pastedContent.getNodes()) {
						visualNetwork.setSelectedNode(node.getName(), true);
					}
					for (Link<Node> link : pastedContent.getLinks()) {
						visualNetwork.setSelectedLink(link, true);
					}
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(Utilities.getOwner(this),
							stringDatabase.getString("CannotPasteAllNodes.Text.Label"),
							stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.WARNING_MESSAGE);
				}
				adjustPanelDimension();
				repaint();
			}
		}
	}

	/**
	 * This method says if there is data stored in the clipboard.
	 *
	 * @return true if there is data stored in the clipboard; otherwise, false.
	 */
	public boolean isThereDataStored() {
		return (clipboardAssistant != null) ? clipboardAssistant.isThereDataStored() : false;
	}

	/**
	 * This method imposes a policy in a decision node.
	 */
	public void imposePolicyInNode() {
		VisualNode visualNode = null;
		List<VisualNode> selectedNode = visualNetwork.getSelectedNodes();
		if (selectedNode.size() == 1) {
			visualNode = selectedNode.get(0);
			if (visualNode.getNode().getNodeType() == NodeType.DECISION) {
				Node node = visualNode.getNode();
				// TODO manage other kind of policy types from the interface
				node.setPolicyType(PolicyType.OPTIMAL);
				List<Variable> variables = new ArrayList<Variable>();
				// it is added first conditioned variable
				variables.add(node.getVariable());
				/*
				 * List<Node> nodes = node.getProbNet ().getNodes (); for (Node possibleParent :
				 * nodes) { if (node.isParent (possibleParent)) { variables.add
				 * (possibleParent.getVariable ()); } }
				 */
				for (Node parent : node.getParents()) {
					variables.add(parent.getVariable());
				}
				UniformPotential policy = new UniformPotential(variables, PotentialRole.POLICY);
				List<Potential> policies = new ArrayList<Potential>();
				policies.add(policy);
				node.setPotentials(policies);
				PotentialEditDialog imposePolicyDialog = new PotentialEditDialog(Utilities.getOwner(this), node, false);
				imposePolicyDialog.setTitle("ImposePolicydialog.Title.Label");
				if (imposePolicyDialog.requestValues() == NodePropertiesDialog.OK_BUTTON) {
					// change its color
					((VisualDecisionNode) visualNode).setHasPolicy(true);
					// networkChanged = true;
					setNetworkChangedWithOutEdit(true);
				} else { // if user cancels policy imposition then no potential is
					// restored to the node
					List<Potential> noPolicy = new ArrayList<Potential>();
					node.setPotentials(noPolicy);
				}
			}
		}
		setSelectedAllNodes(false);
		repaint();
	}

	/**
	 * This method edits an imposed policy of a decision node.
	 */
	public void editNodePolicy() {
		VisualNode visualNode = null;
		List<VisualNode> selectedNode = visualNetwork.getSelectedNodes();
		if (selectedNode.size() == 1) {
			visualNode = selectedNode.get(0);
			if (visualNode.getNode().getNodeType() == NodeType.DECISION) {
				Node node = visualNode.getNode();
				// TODO manage other kind of policy types from the interface
				// node.setPolicyType(PolicyType.OPTIMAL);
				// Potential imposedPolicy = node.getPotentials ().get (0);
				PotentialEditDialog imposePolicyDialog = new PotentialEditDialog(Utilities.getOwner(this), node, false);
				if (imposePolicyDialog.requestValues() == NodePropertiesDialog.OK_BUTTON) {
					// change it color
					((VisualDecisionNode) visualNode).setHasPolicy(true);
					networkChanged = true;
				}
			}
		}
		setSelectedAllNodes(false);
		repaint();
	}

	/**
	 * This method removes an imposed policy from a decision node.
	 */
	public void removePolicyFromNode() {
		VisualNode visualNode = null;
		List<VisualNode> selectedNode = visualNetwork.getSelectedNodes();
		if (selectedNode.size() == 1) {
			visualNode = selectedNode.get(0);
			if (visualNode.getNode().getNodeType() == NodeType.DECISION) {
				Node node = visualNode.getNode();
				List<Potential> noPolicy = new ArrayList<>();
				node.setPotentials(noPolicy);
				((VisualDecisionNode) visualNode).setHasPolicy(false);
			}
		}
		/*
		 * 05/11/2014 Solving issue #2112
		 * https://bitbucket.org/cisiad/org.openmarkov.issues/issue/212/when-a-policy-
		 * imposed-is-removed-the-gui
		 */
		// networkChanged = true;
		setNetworkChangedWithOutEdit(true);
		setSelectedAllNodes(false);
		repaint();
	}

	/**
	 * This method shows the expected utility of a decision node.
	 */
	public void showExpectedUtilityOfNode() {
		VisualNode visualNode = null;
		List<VisualNode> selectedNode = visualNetwork.getSelectedNodes();
		if (selectedNode.size() == 1) {
			visualNode = selectedNode.get(0);
			Node node = visualNode.getNode();
			try {
				Potential expectedUtility = null;

				VEExpectedUtilityDecision veExpectedUtilityDecision = new VEExpectedUtilityDecision(probNet,
						node.getVariable());
				expectedUtility = veExpectedUtilityDecision.getExpectedUtility();

				Node dummyNode = new Node(new ProbNet(), node.getVariable(), node.getNodeType());
				dummyNode.setPotential(expectedUtility);
				PotentialEditDialog expectedUtilityDialog = new PotentialEditDialog(Utilities.getOwner(this), dummyNode,
						false, true);
				expectedUtilityDialog.setTitle("ExpectedUtilityDialog.Title.Label");
				expectedUtilityDialog.requestValues();
			} catch (IncompatibleEvidenceException | UnexpectedInferenceException | NotEvaluableNetworkException e) {
				JOptionPane.showMessageDialog(Utilities.getOwner(this), "ERROR\n" + e.getMessage(), e.getMessage(),
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
		networkChanged = false;
		setSelectedAllNodes(false);
		repaint();
		setSelectedAllNodes(false);
		repaint();
	}

	/**
	 * This method shows the optimal policy for a decision node.
	 */
	public void showOptimalPolicyOfNode() {
		VisualNetwork n = getVisualNetwork();
		VisualNode visualNode = null;
		List<VisualNode> selectedNodes = visualNetwork.getSelectedNodes();
		if (selectedNodes.size() == 1) {
			visualNode = selectedNodes.get(0);
			ProbNet dummyProbNet = new ProbNet();
			Node dummy = null;
			Potential optimalPolicy = null;

			try {
				OptimalPolicies veOptimalPolicy = new VEEvaluation(probNet);
				optimalPolicy = veOptimalPolicy.getOptimalPolicy(visualNode.getNode().getVariable());
			} catch (IncompatibleEvidenceException e) {
				JOptionPane.showMessageDialog(null,
						StringDatabase.getUniqueInstance().getString("LoadEvidence.Error.IncompatibleEvidence.Text"),
						StringDatabase.getUniqueInstance().getString("ExceptionGeneric.Title.Label"),
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			} catch (UnexpectedInferenceException e) {
				JOptionPane.showMessageDialog(null, StringDatabase.getUniqueInstance().getString("GenericError.Text"),
						StringDatabase.getUniqueInstance().getString("ExceptionGeneric.Title.Label"),
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			} catch (NotEvaluableNetworkException e) {
				JOptionPane.showMessageDialog(null,
						StringDatabase.getUniqueInstance().getString("ExceptionNotEvaluableNetwork.Text.Label"),
						StringDatabase.getUniqueInstance().getString("ExceptionNotEvaluableNetwork.Title.Label"),
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}

			dummyProbNet.addPotential(optimalPolicy);
			Variable conditionedVariable = optimalPolicy.getVariable(0);
			dummy = dummyProbNet.getNode(conditionedVariable);
			dummy.setNodeType(NodeType.DECISION);
			dummy.setPolicyType(PolicyType.OPTIMAL);
			for (Variable variable : optimalPolicy.getVariables()) {
				if (variable.equals(conditionedVariable)) {
					continue;
				}
				try {
					dummyProbNet.addLink(variable, conditionedVariable, true);
				} catch (NodeNotFoundException e) {
					throw new RuntimeException("Node not found: " + e.getMessage());
				}
			}
			PotentialEditDialog optimalPolicyDialog = new PotentialEditDialog(Utilities.getOwner(this), dummy, false,
					true);
			optimalPolicyDialog.setTitle("OptimalPolicyDialog.Title.Label");
			optimalPolicyDialog.requestValues();

		}
		networkChanged = false;
		setSelectedAllNodes(false);
		repaint();
		setSelectedAllNodes(false);
		repaint();
	}

	/**
	 * This method expands a node.
	 */
	public void expandNode() {
		VisualNode visualNode = null;
		List<VisualNode> selectedNodes = visualNetwork.getSelectedNodes();
		if (selectedNodes.size() > 0) {
			for (int i = 0; i < selectedNodes.size(); i++) {
				visualNode = selectedNodes.get(i);
				if (!(visualNode.isExpanded())) {
					visualNode.setExpanded(true);
					visualNetwork.setSelectedNode(visualNode, false);
				}
				repaint();
			}
		}
	}

	/**
	 * This method contracts a node.
	 */
	public void contractNode() {
		VisualNode visualNode = null;
		List<VisualNode> selectedNodes = visualNetwork.getSelectedNodes();
		if (selectedNodes.size() > 0) {
			for (int i = 0; i < selectedNodes.size(); i++) {
				visualNode = selectedNodes.get(i);
				if (visualNode.isExpanded()) {
					visualNode.setExpanded(false);
					visualNetwork.setSelectedNode(visualNode, false);
				}
				repaint();
			}
		}
	}

	/**
	 * This method adds a finding in a node.
	 */
	public void addFinding() {
		setPropagationActive(isAutomaticPropagation());
		Graphics2D g = (Graphics2D) getGraphics();
		VisualNode node = null;
		List<VisualNode> selectedNode = visualNetwork.getSelectedNodes();
		if (selectedNode.size() == 1) {
			node = selectedNode.get(0);
			EvidenceCase currentEvidence = (networkPanel.getWorkingMode() == NetworkPanel.INFERENCE_WORKING_MODE)
					? getCurrentEvidenceCase()
					: preResolutionEvidence;
			Finding finding = currentEvidence.getFinding(node.getNode().getVariable());
			AddFindingDialog nodeAddFinding = new AddFindingDialog(Utilities.getOwner(this), node, finding, g, this);
			nodeAddFinding.setVisible(true);
		}
		repaint();
		setSelectedAllNodes(false);
		networkPanel.getMainPanel().getInferenceToolBar().setCurrentEvidenceCaseName(currentCase);
		networkPanel.getMainPanel().getMainPanelMenuAssistant().updateOptionsFindingsDependent(networkPanel);
	}

	/**
	 * This method removes findings from selected nodes.
	 */
	public void removeFinding() {
		setPropagationActive(isAutomaticPropagation());
		VisualNode node = null;
		List<VisualNode> selectedNodes = visualNetwork.getSelectedNodes();
		for (int i = 0; i < selectedNodes.size(); i++) {
			node = selectedNodes.get(i);
			Variable variable = node.getNode().getVariable();
			try {
				if (networkPanel.getWorkingMode() == NetworkPanel.EDITION_WORKING_MODE) {
					if (node.isPreResolutionFinding() && preResolutionEvidence.getFinding(variable) != null) {
						preResolutionEvidence.removeFinding(variable);
						node.setPreResolutionFinding(false);
					}
				} else {
					if (node.isPreResolutionFinding()) {
						JOptionPane.showMessageDialog(Utilities.getOwner(this),
								"This node has a Pre-Resolution Finding that cannot be modified in Inference Mode.",
								stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
						// TODO //...asaez...Internacionalizar la sentencia
					} else if (node.isPostResolutionFinding()
							&& postResolutionEvidence.get(currentCase).getFinding(variable) != null) {
						postResolutionEvidence.get(currentCase).removeFinding(variable);
						node.setPostResolutionFinding(false);
					}
				}
			} catch (NoFindingException exc) {
				JOptionPane.showMessageDialog(Utilities.getOwner(this),
						"ERROR\n" + stringDatabase.getString("ExceptionNoFinding.Text.Label") + "\n\n"
								+ exc.getMessage(),
						stringDatabase.getString("ExceptionNoFinding.Title.Label"), JOptionPane.ERROR_MESSAGE);
			}
		}
		if ((propagationActive) && (networkPanel.getWorkingMode() == NetworkPanel.INFERENCE_WORKING_MODE)) {
			/*
			 * 23/10/2014 Solving issue 226
			 * https://bitbucket.org/cisiad/org.openmarkov.issues/issue/226/remove-finding-
			 * in-inference-mode-displays The previously code was setting the propagation
			 * active to false when the propagation was actually success, and thus the
			 * propagation was not being performed
			 */
			if (!doPropagation(postResolutionEvidence.get(currentCase), currentCase)) {
				setPropagationActive(false);
			}
		}
		networkPanel.getMainPanel().getInferenceToolBar().setCurrentEvidenceCaseName(currentCase);
		networkPanel.getMainPanel().getMainPanelMenuAssistant().updateOptionsFindingsDependent(networkPanel);
		setSelectedAllNodes(false);
		repaint();
	}

	/**
	 * This method returns the current Evidence Case.
	 *
	 * @return the current Evidence Case.
	 */
	public EvidenceCase getCurrentEvidenceCase() {
		return postResolutionEvidence.get(currentCase);
	}

	/**
	 * This method returns the Evidence Case.
	 *
	 * @param caseNumber the number of the case to be returned.
	 * @return the selected Evidence Case.
	 */
	public EvidenceCase getEvidenceCase(int caseNumber) {
		return postResolutionEvidence.get(caseNumber);
	}

	/**
	 * This method returns list of evidence cases
	 *
	 * @return the list of Evidence Cases.
	 */
	public List<EvidenceCase> getEvidence() {
		List<EvidenceCase> evidence = new ArrayList<EvidenceCase>();
		for (EvidenceCase postResolutionEvidenceCase : postResolutionEvidence) {
			if (!postResolutionEvidenceCase.isEmpty()) {
				evidence.add(postResolutionEvidenceCase);
			}
		}
		if (!evidence.isEmpty() || !preResolutionEvidence.isEmpty()) {
			evidence.add(0, preResolutionEvidence);
		}
		return evidence;
	}

	/**
	 * This method returns the number of the Evidence Case that is currently
	 * selected
	 *
	 * @return the number of the current Evidence Case.
	 */
	public int getCurrentCase() {
		return currentCase;
	}

	/**
	 * This method sets which is the current evidence case.
	 *
	 * @param currentCase new value for the current evidence case.
	 */
	public void setCurrentCase(int currentCase) {
		this.currentCase = currentCase;
	}

	public EvidenceCase getPreResolutionEvidence() {
		return preResolutionEvidence;
	}

	/**
	 * This method returns the number of Evidence Cases that the ArrayList is
	 * currently holding .
	 *
	 * @return the number of Evidence Cases in the ArrayList.
	 */
	public int getNumberOfCases() {
		return postResolutionEvidence.size();
	}

	/**
	 * This method returns a boolean indicating if the case number passed as
	 * parameter is currently compiled.
	 *
	 * @param caseNumber number of the evidence case.
	 * @return the compilation state of the case.
	 */
	public boolean getEvidenceCasesCompilationState(int caseNumber) {
		return evidenceCasesCompilationState.get(caseNumber);
	}

	/**
	 * This method sets which is the compilation state of the case.
	 *
	 * @param caseNumber number of the evidence case to be set.
	 * @param value      true if compiled; false otherwise.
	 */
	public void setEvidenceCasesCompilationState(int caseNumber, boolean value) {
		this.evidenceCasesCompilationState.set(caseNumber, value);
	}

	/**
	 * This method sets the list of evidence cases
	 *
	 * @param preResolutionEvidence   pre-resolution evidence.
	 * @param postResolutionInference a list of evidence case.
	 */
	public void setEvidence(EvidenceCase preResolutionEvidence, List<EvidenceCase> postResolutionInference) {
		this.postResolutionEvidence = (postResolutionInference == null) ? new ArrayList<EvidenceCase>()
				: postResolutionInference;
		this.preResolutionEvidence = (preResolutionEvidence == null) ? new EvidenceCase() : preResolutionEvidence;
		if (postResolutionEvidence.isEmpty()) {
			this.postResolutionEvidence.add(new EvidenceCase());
		}
		currentCase = this.postResolutionEvidence.size() - 1;
		// Update visual info on evidence
		for (VisualNode node : visualNetwork.getAllNodes()) {
			node.setPostResolutionFinding(false);
		}
		for (EvidenceCase evidenceCase : postResolutionEvidence) {
			for (Finding finding : evidenceCase.getFindings()) {
				for (VisualNode node : visualNetwork.getAllNodes()) {
					if (node.getNode().getVariable().equals(finding.getVariable())) {
						node.setPostResolutionFinding(true);
					}
				}
			}
		}
		for (VisualNode node : visualNetwork.getAllNodes()) {
			node.setPreResolutionFinding(false);
		}
		for (Finding finding : preResolutionEvidence.getFindings()) {
			for (VisualNode node : visualNetwork.getAllNodes()) {
				if (node.getNode().getVariable().equals(finding.getVariable())) {
					node.setPreResolutionFinding(true);
				}
			}
		}
		// Update evidenceCasesCompilationState
		evidenceCasesCompilationState.clear();
		for (int i = 0; i < postResolutionEvidence.size(); ++i) {
			evidenceCasesCompilationState.add(false);
		}
	}

	/**
	 * This method returns true if propagation type currently set is automatic;
	 * false if manual.
	 *
	 * @return true if the current propagation type is automatic.
	 */
	public boolean isAutomaticPropagation() {
		return automaticPropagation;
	}

	/**
	 * This method sets the current propagation type.
	 *
	 * @param automaticPropagation new value of the propagation type.
	 */
	public void setAutomaticPropagation(boolean automaticPropagation) {
		this.automaticPropagation = automaticPropagation;
	}

	/**
	 * This method returns the propagation status: true if propagation should be
	 * done right now; false otherwise.
	 *
	 * @return true if propagation should be done right now.
	 */
	public boolean isPropagationActive() {
		return propagationActive;
	}

	/**
	 * This method sets the propagation status.
	 *
	 * @param propagationActive new value of the propagation status.
	 */
	public void setPropagationActive(boolean propagationActive) {
		this.propagationActive = propagationActive;
		this.visualNetwork.setPropagationActive(propagationActive);
	}

	/**
	 * This method returns the associated network panel.
	 *
	 * @return the associated network panel.
	 */
	public NetworkPanel getNetworkPanel() {
		return networkPanel;
	}

	/**
	 * This method returns the current expansion threshold.
	 *
	 * @return the value of the current expansion threshold.
	 */
	public double getExpansionThreshold() {
		return currentExpansionThreshold;
	}

	/**
	 * This method changes the current expansion threshold.
	 *
	 * @param expansionThreshold new value of the expansion threshold.
	 */
	public void setExpansionThreshold(double expansionThreshold) {
		this.currentExpansionThreshold = expansionThreshold;
	}

	/**
	 * This method updates the expansion state (expanded/contracted) of the nodes.
	 * It is used in transitions from edition to inference mode and vice versa, and
	 * also when the user modifies the current expansion threshold in the Inference
	 * tool bar
	 *
	 * @param newWorkingMode new value of the working mode.
	 */
	public void updateNodesExpansionState(int newWorkingMode) {
		if (newWorkingMode == NetworkPanel.EDITION_WORKING_MODE) {
			VisualNode visualNode = null;
			List<VisualNode> allNodes = visualNetwork.getAllNodes();
			if (allNodes.size() > 0) {
				for (int i = 0; i < allNodes.size(); i++) {
					visualNode = allNodes.get(i);
					if (visualNode.isExpanded()) {
						visualNode.setExpanded(false);
					}
					repaint();
				}
				repaint();
			}
		} else if (newWorkingMode == NetworkPanel.INFERENCE_WORKING_MODE) {
			VisualNode visualNode = null;
			List<VisualNode> allNodes = visualNetwork.getAllNodes();
			if (allNodes.size() > 0) {
				for (int i = 0; i < allNodes.size(); i++) {
					visualNode = allNodes.get(i);
					if (visualNode.getNode().getRelevance() >= getExpansionThreshold()) {
						visualNode.setExpanded(true);
					} else {
						visualNode.setExpanded(false);
					}
					repaint();
				}
			}
		}
	}

	/**
	 * This method updates the value of each state for each node in the network with
	 * the current individual probabilities.
	 */
	public void updateIndividualProbabilitiesAndUtilities() {
		// if some visualNode has a number of values different from the
		// number of evidence cases in memory, we need to recreate its
		// visual states and consider that the network has been changed.
		for (VisualNode visualNode : visualNetwork.getAllNodes()) {
			InnerBox innerBox = visualNode.getInnerBox();
			VisualState visualState = null;
			if (innerBox instanceof FSVariableBox || innerBox instanceof NumericVariableBox) {
				if (innerBox instanceof FSVariableBox) {
					visualState = ((FSVariableBox) innerBox).getVisualState(0);
				} else { // (innerBox instanceof NumericVariableBox)
					visualState = ((NumericVariableBox) innerBox).getVisualState();
				}
				updateVisualStateAndEvidence(innerBox, visualState);
			}

		}
		if ((propagationActive) && (networkPanel.getWorkingMode() == NetworkPanel.INFERENCE_WORKING_MODE)) {
			// if the network has been changed, propagation must be done in
			// each evidence case in memory. Otherwise, only propagation in
			// current case is needed.
			/*
			 * TODO - Always true, remove the or condition and set the network as changed
			 * when the criteria is modified for example, when the scale change
			 */
			if (networkChanged || true) {
				for (int i = 0; i < postResolutionEvidence.size(); i++) {
					doPropagation(getEvidenceCase(i), i);
				}
				updateNodesFindingState(postResolutionEvidence.get(currentCase));
				networkChanged = false;
			} else {
				if (evidenceCasesCompilationState.get(currentCase) == false) {
					if (!doPropagation(postResolutionEvidence.get(currentCase), currentCase))
						setPropagationActive(false);
				}
			}
		} else if (evidenceCasesCompilationState.get(currentCase) == false) {
			// Even if propagation mode is manual, a propagation should be
			// done the first time that inference mode is selected
			doPropagation(postResolutionEvidence.get(currentCase), currentCase);
		}
		updateAllVisualStates("", currentCase);
		repaint();
	}

	/**
	 * @param innerBox
	 * @param visualState
	 */
	private void updateVisualStateAndEvidence(InnerBox innerBox, VisualState visualState) {
		if (visualState.getNumberOfValues() != postResolutionEvidence.size()) {
			innerBox.update(postResolutionEvidence.size());
			networkChanged = true;
			for (int i = 0; i < postResolutionEvidence.size(); i++) {
				evidenceCasesCompilationState.set(i, false);
			}
		}
	}

	/**
	 * This method removes all the findings established in the current evidence
	 * case.
	 */
	public void removeAllFindings() {
		setPropagationActive(isAutomaticPropagation());
		List<VisualNode> visualNodes = visualNetwork.getAllNodes();
		for (int i = 0; i < visualNodes.size(); i++) {
			visualNodes.get(i).setPostResolutionFinding(false);
		}
		List<Finding> findings = postResolutionEvidence.get(currentCase).getFindings();
		for (int i = 0; i < findings.size(); i++) {
			try {
				postResolutionEvidence.get(currentCase).removeFinding(findings.get(i).getVariable());
			} catch (NoFindingException exc) {
				JOptionPane.showMessageDialog(Utilities.getOwner(this),
						"ERROR\n" + stringDatabase.getString("ExceptionNoFinding.Text.Label") + "\n\n"
								+ exc.getMessage(),
						stringDatabase.getString("ExceptionNoFinding.Title.Label"), JOptionPane.ERROR_MESSAGE);
			}
		}
		if (!doPropagation(postResolutionEvidence.get(currentCase), currentCase))
			setPropagationActive(false);
		networkPanel.getMainPanel().getInferenceToolBar().setCurrentEvidenceCaseName(currentCase);
		setSelectedAllNodes(false);
		networkPanel.getMainPanel().getMainPanelMenuAssistant().updateOptionsFindingsDependent(networkPanel);
	}

	/**
	 * This method removes the findings that a node could have in all the evidence
	 * cases in memory. It is invoked when a change takes place in properties or
	 * probabilities of a the node
	 *
	 * @param node the node in which to remove the findings.
	 */
	public void removeNodeEvidenceInAllCases(Node node) {
		for (int i = 0; i < postResolutionEvidence.size(); i++) {
			List<Finding> findings = postResolutionEvidence.get(i).getFindings();
			for (int j = 0; j < findings.size(); j++) {
				try {
					if (node.getVariable() == (findings.get(j).getVariable())) {
						postResolutionEvidence.get(i).removeFinding(findings.get(j).getVariable());
						if (isAutomaticPropagation() && (inferenceAlgorithm != null)) {
							if (!doPropagation(postResolutionEvidence.get(i), i))
								setPropagationActive(false);
						}
						if (i == currentCase) {
							List<VisualNode> visualNodes = visualNetwork.getAllNodes();
							for (int k = 0; k < visualNodes.size(); k++) {
								if (visualNodes.get(k).getNode() == node) {
									visualNodes.get(k).setPostResolutionFinding(false);// ...asaez....PENDIENTE........
								}
							}
						}
					}
				} catch (NoFindingException exc) {
					JOptionPane.showMessageDialog(Utilities.getOwner(this),
							"ERROR\n" + stringDatabase.getString("ExceptionNoFinding.Text.Label") + "\n\n"
									+ exc.getMessage(),
							stringDatabase.getString("ExceptionNoFinding.Title.Label"), JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		setSelectedAllNodes(false);
		networkPanel.getMainPanel().getMainPanelMenuAssistant().updateOptionsFindingsDependent(networkPanel);
		repaint();
	}

	/**
	 * This method returns true if there are any finding in the current evidence
	 * case.
	 *
	 * @return true if the current evidence case has at least one finding.
	 */
	public boolean areThereFindingsInCase() {
		boolean areFindings = false;
		List<Finding> findings = postResolutionEvidence.get(currentCase).getFindings();
		if (findings != null) {
			if (findings.size() > 0) {
				areFindings = true;
			}
		}
		return areFindings;
	}

	/**
	 * This method returns the number of the Evidence Case that is currently
	 * selected
	 *
	 * @param visualNode a node
	 * @param state      the visual state in which the finding is going to be set.
	 */
	public void toggleFinding(VisualNode visualNode, VisualState state) {
		setNewFinding(visualNode, new Finding(visualNode.getNode().getVariable(), state.getStateIndex()), true);
	}

	/**
	 * This method returns the number of the Evidence Case that is currently
	 * selected
	 *
	 * @param visualNode a node.
	 * @param finding    a finding.
	 * @param toggle     a boolean value.
	 */
	public void setNewFinding(VisualNode visualNode, Finding finding, boolean toggle) {
		Variable variable = visualNode.getNode().getVariable();

		boolean isInferenceMode = networkPanel.getWorkingMode() == NetworkPanel.INFERENCE_WORKING_MODE;
		EvidenceCase evidenceCase = (isInferenceMode) ? postResolutionEvidence.get(currentCase) : preResolutionEvidence;
		setPropagationActive(isAutomaticPropagation());
		boolean alreadyHasFinding = evidenceCase.contains(variable);
		Finding oldFinding = null;
		if (alreadyHasFinding) {
			// There is already a finding. Remove it
			try {
				oldFinding = evidenceCase.removeFinding(variable);
			} catch (NoFindingException exc) {
				// Ignore. Not possible
			}
		}
		// Add finding (unless we were toggling evidence)
		if (!alreadyHasFinding || !toggle || oldFinding.getState() != finding.getState()) {
			try {
				evidenceCase.addFinding(finding);
				if (isInferenceMode) {
					visualNode.setPostResolutionFinding(true);
				} else {
					visualNode.setPreResolutionFinding(true);
				}
			} catch (InvalidStateException exc) {
				JOptionPane.showMessageDialog(Utilities.getOwner(this),
						"ERROR\n" + stringDatabase.getString("ExceptionInvalidState.Text.Label") + "\n\n"
								+ exc.getMessage(),
						stringDatabase.getString("ExceptionInvalidState.Title.Label"), JOptionPane.ERROR_MESSAGE);
			} catch (IncompatibleEvidenceException exc) {
				JOptionPane.showMessageDialog(Utilities.getOwner(this),
						"ERROR\n" + stringDatabase.getString("ExceptionIncompatibleEvidence.Text.Label") + "\n\n"
								+ exc.getMessage(),
						stringDatabase.getString("ExceptionIncompatibleEvidence.Title.Label"),
						JOptionPane.ERROR_MESSAGE);
			} catch (Exception exc) {
				JOptionPane.showMessageDialog(Utilities.getOwner(this), "ERROR" + "\n\n" + exc.getMessage(),
						stringDatabase.getString("ExceptionGeneric.Title.Label"), JOptionPane.ERROR_MESSAGE);
			}
		}
		// Flag current case as not compiled
		if (isInferenceMode) {
			evidenceCasesCompilationState.set(currentCase, false);
		} else {
			for (int i = 0; i < evidenceCasesCompilationState.size(); ++i) {
				evidenceCasesCompilationState.set(i, false);
			}
		}
		setSelectedAllNodes(false);
		networkPanel.getMainPanel().getInferenceToolBar().setCurrentEvidenceCaseName(currentCase);

		// If propagation is active, do propagation
		if ((propagationActive) && (evidenceCasesCompilationState.get(currentCase) == false) && (isInferenceMode)) {
			if (!doPropagation(evidenceCase, currentCase))
			// if propagation does not succeed, restore previous state
			{
				if (alreadyHasFinding) {
					try {
						evidenceCase.removeFinding(variable);
					} catch (NoFindingException e) {/* Not possible */
					}
					try {
						evidenceCase.addFinding(oldFinding);
					} catch (InvalidStateException e) {/* Not possible */
					} catch (IncompatibleEvidenceException e) {/* Not possible */
					}
				} else {
					try {
						evidenceCase.removeFinding(variable);
					} catch (NoFindingException e) { /* Not possible */
					}
				}
				if (isInferenceMode) {
					visualNode.setPostResolutionFinding(alreadyHasFinding);
				} else {
					visualNode.setPreResolutionFinding(alreadyHasFinding);
				}
			}
		}
		networkPanel.getMainPanel().getMainPanelMenuAssistant().updateOptionsFindingsDependent(networkPanel);
		networkPanel.getMainPanel().getMainPanelMenuAssistant().updateOptionsPropagationTypeDependent(networkPanel);// ..
		repaint();
	}

	/**
	 * Returns the inference algorithm assigned to the panel.
	 *
	 * @return the inference algorithm assigned to the panel.
	 */
	public InferenceAlgorithm getInferenceAlgorithm() {
		try {
			if (inferenceAlgorithm == null) {
				inferenceAlgorithm = inferenceManager.getDefaultInferenceAlgorithm(probNet);
				if (inferenceAlgorithm == null) {
					throw new UnsupportedOperationException();
				}
				// TODO - Delete this definition
				// inferenceAlgorithm.setHeuristicFactory(new HeuristicFactory() {
				// @Override
				// public EliminationHeuristic getHeuristic(ProbNet probNet,
				// List<List<Variable>> variables) {
				// return new SimpleElimination(probNet, variables);
				// }
				// });
			}
		} catch (NotEvaluableNetworkException e) {
			JOptionPane.showMessageDialog(Utilities.getOwner(this), e.getMessage());
		}
		return inferenceAlgorithm;
	}

	/**
	 * Sets the inference algorithm assigned to the panel.
	 *
	 * @param inferenceAlgorithm the inference Algorithm to be assigned to the
	 *                           panel.
	 */
	public void setInferenceAlgorithm(InferenceAlgorithm inferenceAlgorithm) {
		this.inferenceAlgorithm = inferenceAlgorithm;
	}

	/**
	 * This method does the propagation of the evidence in the network
	 *
	 * @param evidenceCase the evidence case with which the propagation must be
	 *                     done.
	 * @param caseNumber   number of this evidence case.
	 */
	public boolean doPropagation(EvidenceCase evidenceCase, int caseNumber) {
		Map<Variable, TablePotential> individualProbabilities = null;
		boolean propagationSucceded = false;
		try {
			long start = System.currentTimeMillis();
			try {
				// inferenceAlgorithm = getInferenceAlgorithm();
				// inferenceAlgorithm.setPreResolutionEvidence(preResolutionEvidence);
				// inferenceAlgorithm.setPostResolutionEvidence(evidenceCase);

				calculateMinAndMaxUtilityRanges();
				// individualProbabilities = inferenceAlgorithm.getProbsAndUtilities ();
				Propagation vePosteriorValues = new VEPropagation(probNet);
				vePosteriorValues.setVariablesOfInterest(probNet.getVariables());
				vePosteriorValues.setPreResolutionEvidence(preResolutionEvidence);
				vePosteriorValues.setPostResolutionEvidence(evidenceCase);
				individualProbabilities = vePosteriorValues.getPosteriorValues();
			} catch (OutOfMemoryError e) {
				if (!approximateInferenceWarningGiven) {
					JOptionPane.showMessageDialog(Utilities.getOwner(this),
							stringDatabase.getString("NotEnoughMemoryForExactInference.Text"),
							stringDatabase.getString("NotEnoughMemoryForExactInference.Title"),
							JOptionPane.WARNING_MESSAGE);
					approximateInferenceWarningGiven = true;
				}
				inferenceAlgorithm = inferenceManager.getDefaultApproximateAlgorithm(probNet);
				// TODO - Check these lines
				// ((VEPropagation) inferenceAlgorithm).setPostResolutionEvidence(evidenceCase);
				// individualProbabilities = inferenceAlgorithm.getProbsAndUtilities();
			}
			long elapsedTimeMillis = System.currentTimeMillis() - start;
			System.out.println("Inference took " + elapsedTimeMillis + " milliseconds.");
			updateNodesFindingState(evidenceCase);
			paintInferenceResults(caseNumber, individualProbabilities, evidenceCase);
			propagationSucceded = true;

			// Show probability of evidence
//			System.out.format("Probability of evidence %.4f (%s)\n", getProbabilityOfEvidence(evidenceCase),
//					evidenceCase.toString().replace('\n', ' '));
		} catch (IncompatibleEvidenceException e) {
			JOptionPane.showMessageDialog(Utilities.getOwner(this), "Incompatible evidence", "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			JOptionPane.showMessageDialog(null,
					"ERROR\n" + stringDatabase.getString("NoPropagationCanBeDoneMessage1.Text.Label") + "\n"
							+ stringDatabase.getString("NoPropagationCanBeDoneMessage2.Text.Label") + "\n\n"
							+ probNet.getNetworkType(),
					stringDatabase.getString("NoPropagationCanBeDoneMessage.Title.Label"), JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
			LocalizedException localizedException = new LocalizedException(e);
			localizedException.showException();
			LogManager.getLogger().debug(e.getLocalizedMessage());

//			JOptionPane
//					.showMessageDialog(Utilities.getOwner(this), "Error during inference: " + e.getMessage(), "Error",
//							JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		evidenceCasesCompilationState.set(caseNumber, propagationSucceded);
		repaint();
		return propagationSucceded;
	}

	public double getProbabilityOfEvidence(EvidenceCase evidence) {

		List<TablePotential> potentials = new ArrayList<TablePotential>();
		for (Potential potential : probNet.getPotentials()) {
			if (potential instanceof TablePotential)
				potentials.add((TablePotential) potential);
			else
				throw new RuntimeException("Network with non table potentials");
		}

		TablePotential jointProbability = (TablePotential) DiscretePotentialOperations
				.multiplyAndMarginalize(potentials, evidence.getVariables());

		return jointProbability.getValue(evidence);
	}

	// This commented method computes the exact ranges of the utility functions.
	// However, we are using an approximation in the method currently offered by
	// this class.
	/*
	 * Calculates minUtilityRange and maxUtilityRange fields.
	 */
	/*
	 * /* private void () { TablePotential auxF; ArrayList<Variable>
	 * utilityVariables = probNet .getVariables(NodeType.UTILITY); for (Variable
	 * utility : utilityVariables) { auxF = probNet.getUtilityFunction(utility);
	 * minUtilityRange.put(utility, Tools.min(auxF.values));
	 * maxUtilityRange.put(utility, Tools.max(auxF.values)); } }
	 */

	/**
	 * Calculates minUtilityRange and maxUtilityRange fields. It is an approximate
	 * implementation. The correct computation is given by a method with the same
	 * name, but commented above.
	 *
	 * @throws NonProjectablePotentialException
	 */
	private void calculateMinAndMaxUtilityRanges() throws NonProjectablePotentialException {
		List<Variable> utilityVariables = probNet.getVariables(NodeType.UTILITY);
		for (Variable utility : utilityVariables) {
			ProbNet newNet = probNet.copy();
			newNet = TaskUtilities.extendPreResolutionEvidence(newNet, getPreResolutionEvidence());
			Node node = newNet.getNode(utility);
			// minUtilityRange.put(utility,
			// node.getApproximateMaxMinimumUtilityFunction(false, preResolutionEvidence));
			// maxUtilityRange.put(utility,
			// node.getApproximateMaxMinimumUtilityFunction(true, preResolutionEvidence));
			minUtilityRange.put(utility, node.getApproximateMinimumUtilityFunction());
			maxUtilityRange.put(utility, node.getApproximateMaximumUtilityFunction());
		}
	}

	/**
	 * This method fills the visualStates with the proper values to be represented
	 * after the evaluation of the evidence case
	 *
	 * @param caseNumber              number of this evidence case.
	 * @param individualProbabilities the results of the evaluation for each
	 *                                variable.
	 */
	private void paintInferenceResults(int caseNumber, Map<Variable, TablePotential> individualProbabilities,
			EvidenceCase evidence) {
		for (VisualNode visualNode : visualNetwork.getAllNodes()) {
			Node node = visualNode.getNode();
			switch (node.getNodeType()) {
			case CHANCE:
			case DECISION:
				paintInferenceResultsChanceOrDecisionNode(caseNumber, individualProbabilities, evidence, visualNode);
				break;
			case UTILITY:
				paintInferenceResultsUtilityNode(caseNumber, individualProbabilities, visualNode);
				break;
			}
		}
		repaint();
	}

	/**
	 * This method fills the visualStates of a utility node with the proper values
	 * to be represented after the evaluation of the evidence case
	 *
	 * @param caseNumber              number of this evidence case.
	 * @param individualProbabilities the results of the evaluation for each
	 *                                variable.
	 * @param visualNode              a node.
	 */
	private void paintInferenceResultsUtilityNode(int caseNumber, Map<Variable, TablePotential> individualProbabilities,
			VisualNode visualNode) {
		// It is a utility node
		Variable variable = visualNode.getNode().getVariable();
		NumericVariableBox innerBox = (NumericVariableBox) visualNode.getInnerBox();
		VisualState visualState = innerBox.getVisualState();
		visualState.setStateValue(caseNumber, individualProbabilities.get(variable).values[0]);
		innerBox.setMinValue(minUtilityRange.get(variable));
		innerBox.setMaxValue(maxUtilityRange.get(variable));
	}

	/**
	 * This method fills the visualStates of a chance or decision node with the
	 * proper values to be represented after the evaluation of the evidence case
	 *
	 * @param caseNumber              number of this evidence case.
	 * @param individualProbabilities the results of the evaluation for each
	 *                                variable.
	 * @param evidence                evidence.
	 * @param visualNode              a node.
	 */
	private void paintInferenceResultsChanceOrDecisionNode(int caseNumber,
			Map<Variable, TablePotential> individualProbabilities, EvidenceCase evidence, VisualNode visualNode) {
		Variable variable = visualNode.getNode().getVariable();
		Potential potential = individualProbabilities.get(variable);
		TablePotential tablePotential = (TablePotential) potential;
		if (variable.getVariableType() != VariableType.NUMERIC) {
			if (tablePotential.getNumVariables() == 1) {
				double[] values = tablePotential.getValues();
				if ((visualNode.getInnerBox()) instanceof FSVariableBox) {
					FSVariableBox innerBox = (FSVariableBox) visualNode.getInnerBox();
					for (int i = 0; i < innerBox.getNumStates(); i++) {
						VisualState visualState = innerBox.getVisualState(i);
						visualState.setStateValue(caseNumber, values[i]);
					}
				}
				// PROVISIONAL2: Currently the propagation
				// algorithm is returning a TablePotential
				// with 0 variables when the node has a Uniform
				// relation
			} else if (tablePotential.getNumVariables() == 0) {
				if ((visualNode.getInnerBox()) instanceof FSVariableBox) {
					FSVariableBox innerBox = (FSVariableBox) visualNode.getInnerBox();
					for (int i = 0; i < innerBox.getNumStates(); i++) {
						VisualState visualState = innerBox.getVisualState(i);
						visualState.setStateValue(caseNumber, (1.0 / innerBox.getNumStates()));
					}
				}
				visualNode.setPostResolutionFinding(false);
				// END OF
				// PROVISIONAL2.............asaez...Comprobar si es innecesario
				// este Provisional2............
			} else {
				JOptionPane.showMessageDialog(Utilities.getOwner(this),
						"ERROR\n" + "Table Potential of " + variable.getName() + " has "
								+ tablePotential.getNumVariables() + " variables.\n It cannot be treated by now",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		} else // if numeric variable
		{
			double value = (evidence.contains(variable)) ? evidence.getNumericalValue(variable) : Double.NaN;
			value = (preResolutionEvidence.contains(variable)) ? preResolutionEvidence.getNumericalValue(variable)
					: value;
			NumericVariableBox innerBox = (NumericVariableBox) visualNode.getInnerBox();
			innerBox.getVisualState().setStateValue(caseNumber, value);
		}

	}

	/**
	 * This method updates the "finding state" of each node
	 *
	 * @param evidenceCase the evidence case with which the update must be done.
	 */
	public void updateNodesFindingState(EvidenceCase evidenceCase) {
		for (VisualNode visualNode : visualNetwork.getAllNodes()) {
			visualNode.setPreResolutionFinding(false);
			visualNode.setPostResolutionFinding(false);
		}
		for (Finding finding : evidenceCase.getFindings()) {
			Variable variable = finding.getVariable();
			for (VisualNode visualNode : visualNetwork.getAllNodes()) {
				if (variable.getName().equals(visualNode.getNode().getName())) {
					visualNode.setPostResolutionFinding(true);
				}
			}
		}
		for (Finding finding : preResolutionEvidence.getFindings()) {
			Variable variable = finding.getVariable();
			for (VisualNode visualNode : visualNetwork.getAllNodes()) {
				if (variable.getName().equals(visualNode.getNode().getName())) {
					visualNode.setPreResolutionFinding(true);
				}
			}
		}
		repaint();
	}

	public void temporalEvolution() {
		VisualNode node = null;
		List<VisualNode> selectedNode = visualNetwork.getSelectedNodes();
		if (selectedNode.size() == 1) {
			node = selectedNode.get(0);
			new TemporalEvolutionDialog(Utilities.getOwner(this), node.getNode(), preResolutionEvidence);
			setSelectedAllNodes(false);
			repaint();
			// TODO - Change code
		} /*
			 * else if(selectedNode == null){ new CostEffectivenessDialog(Utilities.getOwner
			 * (this), probNet, true, true).setVisible(true); }
			 */
	}

	/**
	 * This method creates a new evidence case
	 */
	public void createNewEvidenceCase() {
		try {
			EvidenceCase newEvidenceCase = new EvidenceCase();
			EvidenceCase currentEvidenceCase = getCurrentEvidenceCase();
			List<Finding> currentFindings = currentEvidenceCase.getFindings();
			for (int i = 0; i < currentFindings.size(); i++) {
				newEvidenceCase.addFinding(currentFindings.get(i));
			}
			addNewEvidenceCase(newEvidenceCase);
		} catch (InvalidStateException exc) {
			JOptionPane.showMessageDialog(Utilities.getOwner(this),
					"ERROR\n" + stringDatabase.getString("ExceptionInvalidState.Text.Label") + "\n\n"
							+ exc.getMessage(),
					stringDatabase.getString("ExceptionInvalidState.Title.Label"), JOptionPane.ERROR_MESSAGE);
		} catch (IncompatibleEvidenceException exc) {
			JOptionPane.showMessageDialog(Utilities.getOwner(this),
					"ERROR\n" + stringDatabase.getString("ExceptionIncompatibleEvidence.Text.Label") + "\n\n"
							+ exc.getMessage(),
					stringDatabase.getString("ExceptionIncompatibleEvidence.Title.Label"), JOptionPane.ERROR_MESSAGE);
		} catch (Exception exc) {
			JOptionPane.showMessageDialog(Utilities.getOwner(this), "ERROR" + "\n\n" + exc.getMessage(),
					stringDatabase.getString("ExceptionGeneric.Title.Label"), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * This method adds a new evidence case
	 */
	public void addNewEvidenceCase(EvidenceCase newEvidenceCase) {
		setPropagationActive(isAutomaticPropagation());
		postResolutionEvidence.add(newEvidenceCase);
		currentCase = (postResolutionEvidence.size() - 1);
		evidenceCasesCompilationState.add(currentCase, false);
		updateAllVisualStates("new", currentCase);
		networkPanel.getMainPanel().getInferenceToolBar().setCurrentEvidenceCaseName(currentCase);
		setSelectedAllNodes(false);
		if (isPropagationActive() && networkPanel.getWorkingMode() == NetworkPanel.INFERENCE_WORKING_MODE) {
			if (!doPropagation(postResolutionEvidence.get(currentCase), currentCase))
				setPropagationActive(false);
		}
	}

	/**
	 * This method makes the first evidence case to be the current
	 */
	public void goToFirstEvidenceCase() {
		currentCase = 0;
		updateAllVisualStates("", currentCase);
		networkPanel.getMainPanel().getInferenceToolBar().setCurrentEvidenceCaseName(currentCase);
		setSelectedAllNodes(false);
		if ((propagationActive) && (evidenceCasesCompilationState.get(currentCase) == false)
				&& (networkPanel.getWorkingMode() == NetworkPanel.INFERENCE_WORKING_MODE)) {
			if (!doPropagation(postResolutionEvidence.get(currentCase), currentCase))
				setPropagationActive(false);
		} else {
			updateNodesFindingState(postResolutionEvidence.get(currentCase));
		}
	}

	/**
	 * This method makes the previous evidence case to be the current
	 */
	public void goToPreviousEvidenceCase() {
		if (currentCase > 0) {
			currentCase--;
			updateAllVisualStates("", currentCase);
			networkPanel.getMainPanel().getInferenceToolBar().setCurrentEvidenceCaseName(currentCase);
			setSelectedAllNodes(false);
			if ((propagationActive) && (evidenceCasesCompilationState.get(currentCase) == false)
					&& (networkPanel.getWorkingMode() == NetworkPanel.INFERENCE_WORKING_MODE)) {
				if (!doPropagation(postResolutionEvidence.get(currentCase), currentCase))
					setPropagationActive(false);
			} else {
				updateNodesFindingState(postResolutionEvidence.get(currentCase));
			}
		} else {
			JOptionPane.showMessageDialog(Utilities.getOwner(this),
					"ERROR\n" + stringDatabase.getString("NoPreviousEvidenceCaseMessage.Text.Label"),
					stringDatabase.getString("NoPreviousEvidenceCaseMessage.Title.Label"), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * This method makes the next evidence case to be the current
	 */
	public void goToNextEvidenceCase() {
		if (currentCase < (postResolutionEvidence.size() - 1)) {
			currentCase++;
			updateAllVisualStates("", currentCase);
			networkPanel.getMainPanel().getInferenceToolBar().setCurrentEvidenceCaseName(currentCase);
			setSelectedAllNodes(false);
			if ((propagationActive) && (evidenceCasesCompilationState.get(currentCase) == false)
					&& (networkPanel.getWorkingMode() == NetworkPanel.INFERENCE_WORKING_MODE)) {
				if (!doPropagation(postResolutionEvidence.get(currentCase), currentCase))
					setPropagationActive(false);
			} else {
				updateNodesFindingState(postResolutionEvidence.get(currentCase));
			}
		} else {
			JOptionPane.showMessageDialog(Utilities.getOwner(this),
					"ERROR\n" + stringDatabase.getString("NoNextEvidenceCaseMessage.Text.Label"),
					stringDatabase.getString("NoNextEvidenceCaseMessage.Title.Label"), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * This method makes the last evidence case to be the current
	 */
	public void goToLastEvidenceCase() {
		currentCase = (postResolutionEvidence.size() - 1);
		updateAllVisualStates("", currentCase);
		networkPanel.getMainPanel().getInferenceToolBar().setCurrentEvidenceCaseName(currentCase);
		setSelectedAllNodes(false);
		if ((propagationActive) && (evidenceCasesCompilationState.get(currentCase) == false)
				&& (networkPanel.getWorkingMode() == NetworkPanel.INFERENCE_WORKING_MODE)) {
			if (doPropagation(postResolutionEvidence.get(currentCase), currentCase))
				setPropagationActive(false);
		} else {
			updateNodesFindingState(postResolutionEvidence.get(currentCase));
		}
	}

	/**
	 * This method clears out all the evidence cases. It returns to an 'initial
	 * state' in which there is only an initial evidence case with no findings
	 * (corresponding to prior probabilities)
	 */
	public void clearOutAllEvidenceCases() {
		setPropagationActive(isAutomaticPropagation());
		postResolutionEvidence.clear();
		evidenceCasesCompilationState.clear();
		EvidenceCase newEvidenceCase = new EvidenceCase();
		postResolutionEvidence.add(newEvidenceCase);
		currentCase = 0;
		evidenceCasesCompilationState.add(currentCase, false);
		updateAllVisualStates("clear", currentCase);
		networkPanel.getMainPanel().getInferenceToolBar().setCurrentEvidenceCaseName(currentCase);
		setSelectedAllNodes(false);
		if (!doPropagation(postResolutionEvidence.get(currentCase), currentCase))
			setPropagationActive(false);
	}

	/**
	 * This method updates all visual states of all visual nodes when it is needed
	 * for a navigation operation among the existing evidence cases, a creation of a
	 * new case or when all cases are cleared out.
	 *
	 * @param option the specific operation to be done over the visual states.
	 */
	public void updateAllVisualStates(String option, int caseNumber) {
		List<VisualNode> allVisualNodes = visualNetwork.getAllNodes();
		for (VisualNode visualNode : allVisualNodes) {
			InnerBox innerBox = visualNode.getInnerBox();
			VisualState visualState = null;
			for (int i = 0; i < innerBox.getNumStates(); i++) {
				if (innerBox instanceof FSVariableBox) {
					visualState = ((FSVariableBox) innerBox).getVisualState(i);
				} else if (innerBox instanceof NumericVariableBox) {
					visualState = ((NumericVariableBox) innerBox).getVisualState();
				}
				if (option.equals("new")) {
					visualState.createNewStateValue();
				} else if (option.equals("clear")) {
					visualState.clearAllStateValues();
				}
				visualState.setCurrentStateValue(caseNumber);
			}
		}
		repaint();
	}

	/**
	 * This method does the propagation of the evidence for all the evidence cases
	 * in memory.
	 *
	 * @param mainPanelMenuAssistant the menu assistant associated to the main
	 *                               panel.
	 */
	public void propagateEvidence(MainPanelMenuAssistant mainPanelMenuAssistant) {
		setPropagationActive(true);
		if (networkPanel.getWorkingMode() == NetworkPanel.INFERENCE_WORKING_MODE) {
			for (int i = 0; i < getNumberOfCases(); i++) {
				if (evidenceCasesCompilationState.get(i) == false) {
					if (doPropagation(getEvidenceCase(i), i))
						setPropagationActive(false);
				}
			}
			setSelectedAllNodes(false);
			updateAllVisualStates("", currentCase);
			networkPanel.getMainPanel().getInferenceToolBar().setCurrentEvidenceCaseName(currentCase);
			updateNodesFindingState(postResolutionEvidence.get(currentCase));
		}
		mainPanelMenuAssistant.updateOptionsEvidenceCasesNavigation(networkPanel);
		mainPanelMenuAssistant.updateOptionsPropagationTypeDependent(networkPanel);
		mainPanelMenuAssistant.updateOptionsFindingsDependent(networkPanel);
	}

	/**
	 * This method sets the inference options for this panel.
	 */
	public void setInferenceOptions() {
		PropagationOptionsDialog inferenceOptionsDialog = new PropagationOptionsDialog(Utilities.getOwner(this), this,
				networkPanel.getMainPanel().getInferenceToolBar());
		inferenceOptionsDialog.setVisible(true);
	}

	/**
	 * Removes selected objects
	 */
	public void removeSelectedObjects() {
		RemoveSelectedEdit cutEdit = new RemoveSelectedEdit(visualNetwork);
		visualNetwork.setSelectedAllObjects(false);
		try {
			probNet.doEdit(cutEdit);
			propagationActive = isAutomaticPropagation();
			networkChanged = true;
			repaint();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(Utilities.getOwner(this), e.getMessage(),
					stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * This methods evaluates a POMDP or dLIMID strategy, for a predetermined 60000
	 * strategies first with brute force and then with random walk.
	 **/
	/*
	 * This method works with StrategyManager of the dlimidEvaluation package. That
	 * package is not completed (and will remain like that for a long time). As for
	 * now, the method is not called from anywhere. Ask {@author IagoParis} about
	 * details.
	 */
	public void evaluatePolicy() {

		// Network is expanded before calling to evaluatePolicy

		// To test if the network is expanded it uses the file name. This produces a lot
		// of false negatives and should
		// be changed.
		if (!networkPanel.getNetworkFile().endsWith("_expanded.pgmx")
				&& !networkPanel.getNetworkFile().endsWith("_expanded")) {
			JOptionPane.showMessageDialog(this, "Did you expand the network?");
			return;
		}

		int horizon = probNet.getInferenceOptions().getTemporalOptions().getHorizon();
		System.out.println("Horizon: " + horizon);

		// Create a strategy manager and random walk for the best strategy of the net
		StrategyManager strategyManager = new StrategyManager(probNet, horizon);
		List<Potential> bestStrategy = strategyManager.randomWalk(60000);

		/* Set the strategy into the nodes */

		Map<Variable, VisualNode> visualDecisionNodes = new HashMap<>();
		for (VisualNode visualNode : visualNetwork.getAllNodes()) {
			if (visualNode.getNode().getNodeType() == NodeType.DECISION) {
				visualDecisionNodes.put(visualNode.getNode().getVariable(), visualNode);
			}
		}
		// Use the variable of the potential to know which policy goes into which node
		for (int policy = 0; policy < bestStrategy.size(); policy++) {
			Variable decisionVariable = bestStrategy.get(policy).getVariable(0);
			visualDecisionNodes.get(decisionVariable).getNode().setPotentials(bestStrategy.subList(policy, policy + 1));
			((VisualDecisionNode) visualDecisionNodes.get(decisionVariable)).setHasPolicy(true);
		}

		// Recreate the visual net to see the changes
		setNetworkChangedWithOutEdit(true);
		repaint();
	}

	/**
	 * This method inverts the selected link arc-reversal style
	 */
	public void invertLinkAndUpdatePotentials() {
		List<VisualLink> links = visualNetwork.getSelectedLinks();
		if (!links.isEmpty()) {
			try {
				Link<Node> link = links.get(0).getLink();
				Node node1 = link.getNode1();
				Node node2 = link.getNode2();
				InvertLinkAndUpdatePotentialsEdit invertLink = new InvertLinkAndUpdatePotentialsEdit(probNet,
						node1.getVariable(), node2.getVariable());
				probNet.doEdit(invertLink);
			} catch (Exception e) {
				String message = e.getMessage();
				if (message.equals("Child") || message.equals("Parent")) {
					message = stringDatabase.getString("LinkNotReversible.Text." + message + ".Label");
				}
				JOptionPane.showMessageDialog(Utilities.getOwner(this), message,
						stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
			}

			repaint();
		}
	}

	/***
	 * Initializes the link restriction potential of a link
	 */
	public void enableLinkRestriction() {
		List<VisualLink> links = visualNetwork.getSelectedLinks();
		if (!links.isEmpty()) {
			Link<Node> link = links.get(0).getLink();
			if (!link.hasRestrictions()) {
				link.initializesRestrictionsPotential();
			}
			if (!requestLinkRestrictionValues(Utilities.getOwner(this), link)) {
				probNet.getPNESupport().undoAndDelete();
			}
			link.resetRestrictionsPotential();
			repaint();
		}
	}

	/***
	 * Resets the link restriction potential of a link
	 */
	public void disableLinkRestriction() {
		List<VisualLink> links = visualNetwork.getSelectedLinks();
		if (!links.isEmpty()) {
			Link<Node> link = links.get(0).getLink();
			link.setRestrictionsPotential(null);
			/*
			 * 27/10/2014 Solving issue #165
			 * https://bitbucket.org/cisiad/org.openmarkov.issues/issue/165/when-a-
			 * restriction-is-removed-the-network The next three lines mark the network as
			 * changed and modify the network panel status
			 */
			setNetworkChangedWithOutEdit(true);
			repaint();
		}
	}

	/***
	 * Initializes the revelation arc properties of a link
	 */
	public void enableRevelationArc() {
		List<VisualLink> links = visualNetwork.getSelectedLinks();
		if (!links.isEmpty()) {
			Link<Node> link = links.get(0).getLink();
			try {
				if (!requestRevelationArcValues(Utilities.getOwner(this), link)) {
					probNet.getPNESupport().undoAndDelete();
				}
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(Utilities.getOwner(this), e.getMessage(),
						stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
			}
			repaint();
		}
	}

	/**
	 * Returns the visualNetwork.
	 *
	 * @return the visualNetwork.
	 */
	public VisualNetwork getVisualNetwork() {
		return visualNetwork;
	}

	/**
	 * Sets a new visualNetwork.
	 *
	 * @param visualNetwork
	 */
	public void setVisualNetwork(VisualNetwork visualNetwork) {
		this.visualNetwork = visualNetwork;
	}

	public void setProbNet(ProbNet probNet) {
		networkChanged = true;
		this.probNet = probNet;
		visualNetwork.setProbNet(probNet);
	}

	/**
	 * Sets workingMode
	 *
	 * @param newWorkingMode
	 */
	public void setWorkingMode(int newWorkingMode) {
		visualNetwork.setWorkingMode(newWorkingMode);
		if (newWorkingMode == NetworkPanel.INFERENCE_WORKING_MODE) {
			editionMode = editionModeManager.getDefaultEditionMode();
			setCursor(editionModeManager.getDefaultCursor());
		}
	}

	// TODO OOPN start
	public void markSelectedAsInput() {
		visualNetwork.markSelectedAsInput();
		repaint();
	}

	public void editClass() {
		visualNetwork.editClass();
	}

	public void setParameterArity(ParameterArity arity) {
		visualNetwork.setParameterArity(arity);
	}

	public void editInstanceName() {
		visualNetwork.editInstanceName();
	}

	// TODO OOPN end
	protected double[] getBounds(Graphics2D graphics) {
		return visualNetwork.getNetworkBounds(graphics);
	}

	/**
	 * If the dimensions of the network are greater than the dimensions of the
	 * panel, changes the dimensions of the panel in order to accommodate the whole
	 * network.
	 */
	public void adjustPanelDimension() {
		double[] bounds = getBounds((Graphics2D) getGraphics());
		Dimension newDimension = null;
		currentWidth = Math.min(MAX_WIDTH, bounds[1]);
		currentHeight = Math.min(MAX_HEIGHT, bounds[3]);
		newDimension = new Dimension((int) Math.round(getNewWidth()), (int) Math.round(getNewHeight()));
		setPreferredSize(newDimension);
		setSize(newDimension);
	}

	/**
	 * Sets the zoom so the displayed network fits in the panel.
	 */
	public void setZoomToFitNetwork() {
		double[] networkBounds = getBounds((Graphics2D) getGraphics());
		Dimension panelBounds = networkPanel.getMainPanel().getMdi().getSize();
		double zoom = 1;

		while (((networkBounds[1] * zoom) > panelBounds.getWidth())
				|| ((networkBounds[3] * zoom) > panelBounds.getHeight()) && zoom > 0.1) {
			zoom -= 0.1;
		}
		setZoom(zoom);
	}

	public void createNextSliceNode() {
		Node selectedNode = visualNetwork.getSelectedNodes().get(0).getNode();
		Variable selectedVariable = selectedNode.getVariable();
		Variable newVariable = new Variable(selectedVariable);
		newVariable.setTimeSlice(selectedVariable.getTimeSlice() + 1);
		Point2D.Double position = new Point2D.Double(selectedNode.getCoordinateX() + 200,
				selectedNode.getCoordinateY());
		AddNodeEdit addNodeEdit = new AddNodeEdit(probNet, newVariable, selectedNode.getNodeType(), position);
		try {
			probNet.doEdit(addNodeEdit);
		} catch (Exception e1) {
			System.err.println(e1.toString());
			e1.printStackTrace();
			JOptionPane.showMessageDialog(this, e1.toString(), "Error creating node", JOptionPane.ERROR_MESSAGE);
		}
		adjustPanelDimension();
		repaint();
	}

	/**
	 * This method sets the network as changed but also modifies the menu so the
	 * save button is enabled This is usually done through edit, but some actions in
	 * this class do not use edits TODO: check whether some actions like
	 * removePolicyFromNode should be refactored as edits or not
	 */
	private void setNetworkChangedWithOutEdit(boolean networkChanged) {
		this.networkChanged = networkChanged;
		networkPanel.setModified(networkChanged);
		if (networkChanged) {
			networkPanel.getMainPanel().getMainPanelMenuAssistant().updateOptionsNetworkModified(false, false);
		} else {
			networkPanel.getMainPanel().getMainPanelMenuAssistant().updateOptionsNetworkSaved();
		}
	}

	// The key listener needs a focusable object to listen
	@Override
	public boolean isFocusable() {
		return true;
	}

	// Moves the selected nodes when pressing the arrows
	@Override
	public void keyPressed(KeyEvent keyEvent) {
		int key = keyEvent.getKeyCode();
		switch (key) {
		case KeyEvent.VK_UP:
			visualNetwork.moveSelectedElements(0, -2);
			break;
		case KeyEvent.VK_RIGHT:
			visualNetwork.moveSelectedElements(+2, 0);
			break;
		case KeyEvent.VK_DOWN:
			visualNetwork.moveSelectedElements(0, +2);
			break;
		case KeyEvent.VK_LEFT:
			visualNetwork.moveSelectedElements(-2, 0);
			break;
		}
		repaint();
	}

	@Override
	public void keyReleased(KeyEvent keyEvent) {

	}

	@Override
	public void keyTyped(KeyEvent keyEvent) {
	}
}
