/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.dt;

import org.openmarkov.core.dt.DecisionTreeBranch;
import org.openmarkov.core.dt.DecisionTreeElement;
import org.openmarkov.core.dt.DecisionTreeNode;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.inference.MulticriteriaOptions.Type;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.model.network.type.NetworkType;

import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.gui.menutoolbar.menu.ContextualMenuFactory;
import org.openmarkov.gui.dialog.costeffectiveness.CEPDialog;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.menutoolbar.menu.TreeContextualMenu;
import org.openmarkov.gui.util.TreeNodeToDot;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.inference.decompositionIntoSymmetricDANs.DecompositionGenerateDecisionTree;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.TreeModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

@SuppressWarnings("serial") public class DecisionTreePanel extends JScrollPane {
	protected DecisionTree jTree;
	public DecisionTree getJTree() {
		return jTree;
	}


	private ContextualMenuFactory contextualMenuFactory;
	private TreePanelListener listener;

	public DecisionTreePanel(ProbNet probNet) throws NotEvaluableNetworkException {
		listener = new TreePanelListener();
		contextualMenuFactory = new ContextualMenuFactory(listener);

		//DecisionTreeElement root = DecisionTreeBuilder.buildDecisionTree (probNet);
		DecisionTreeElement root = buildDecisionTree(probNet);
		updateVisualInformation(root);

	}

	private void updateVisualInformation(DecisionTreeElement root) {
		DecisionTreeModel model = new DecisionTreeModel(root);
		jTree = new DecisionTree(model);
		jTree.addMouseListener(listener);
		for (int i = 0; i < jTree.getRowCount(); i++) {
			jTree.expandRow(i);
		}
		setViewportView(jTree);
		setBackground(Color.white);

	}
	
	public static DecisionTreeElement buildDecisionTree(ProbNet probNet) throws NotEvaluableNetworkException {
		return buildDecisionTree(probNet,5);
	}


	public static DecisionTreeElement buildDecisionTree(ProbNet probNet,int depth) throws NotEvaluableNetworkException {
		return buildDecisionTree(probNet,depth,new EvidenceCase());
	}
	
	/**
	 * @param probNet
	 * @param depth
	 * @param branchEvidence
	 * @return a decision tree branch with the decision tree built
	 * @throws NotEvaluableNetworkException
	 */
	private static DecisionTreeBranch buildDecisionTree(ProbNet probNet, int depth, EvidenceCase branchEvidence) throws NotEvaluableNetworkException {
		DecisionTreeBranch root = null;
		NetworkType networkType = probNet.getNetworkType();
		if (networkType instanceof InfluenceDiagramType || networkType instanceof DecisionAnalysisNetworkType) {
			root = new DecisionTreeBranch(probNet);
			DecompositionGenerateDecisionTree genDT = new DecompositionGenerateDecisionTree(probNet, depth);
			try {
				genDT.setPreResolutionEvidence(branchEvidence);
			} catch (IncompatibleEvidenceException e) {
				e.printStackTrace();
			}
			((DecisionTreeBranch) root).setChild(genDT.getDecisionTree());
		} 
		return root;
	}

	/**
	 * Returns the zoom.
	 *
	 * @return the zoom.
	 */
	protected double getZoom() {
		return jTree.getZoom();
	}

	/**
	 * Sets the zoom.
	 *
	 * @param zoom the zoom to set.
	 */
	protected void setZoom(Double zoom) {
		jTree.setZoom(zoom);
		repaint();
	}
	
	public void inferenceExpandNextLevel() throws NotEvaluableNetworkException {
		inferenceExpandLevels(1);
	}
	
	public void inferenceExpandLevels(int n) throws NotEvaluableNetworkException {
		DecisionTreeModel auxModel = (DecisionTreeModel)jTree.getModel();
		DecisionTreeBranchPanel root = (DecisionTreeBranchPanel) auxModel.getRoot();
		inferenceExpandLevels(root.getTreeBranch(),null,n, new EvidenceCase());
		updateVisualInformation(root.getTreeBranch());			
	}
	
	private void inferenceExpandLevels(DecisionTreeElement root,DecisionTreeNode parent, int n, EvidenceCase branchEvidence) throws NotEvaluableNetworkException {
		if (root instanceof DecisionTreeBranch || ((DecisionTreeNode)root).getNodeType()!= NodeType.UTILITY) {
			if (root instanceof DecisionTreeNode) {
				parent = (DecisionTreeNode) root;
			}
			for (DecisionTreeElement branch : root.getChildren()) {
				EvidenceCase newEvi;
				if (root instanceof DecisionTreeBranch) {
					newEvi = createEvidenceBranchPath(branchEvidence, (DecisionTreeBranch) root);
				}
				else {
					newEvi = branchEvidence;
				}
				inferenceExpandLevels(branch,parent, n, newEvi);						
			}
		}
		else {
			DecisionTreeNode rootDT = (DecisionTreeNode)root;
			DecisionTreeNode auxRoot = ((DecisionTreeBranch) buildDecisionTree(rootDT.getNetwork(), n, branchEvidence)).getChild();
			if (parent != null) {
				if (parent.getNodeType() == NodeType.DECISION
						|| (!(parent.getVariable().getName().equalsIgnoreCase(auxRoot.getVariable().getName())))) {
					rootDT.copy(auxRoot);
				}
			}
		}
	}
	
	

	private EvidenceCase createEvidenceBranchPath(EvidenceCase branchEvidence, DecisionTreeBranch branch) {
		EvidenceCase newEvi = new EvidenceCase(branchEvidence);
		try {
			if (branch != null) {
				Variable branchVariable = branch.getBranchVariable();
				if (branchVariable != null && (!branchVariable.getName().equalsIgnoreCase("OD")) && !newEvi.contains(branchVariable)
						) 
				{
					newEvi.addFinding(new Finding(branchVariable, branch.getBranchState()));
				}
			}
		} catch (InvalidStateException | IncompatibleEvidenceException e) {
			e.printStackTrace();
		}
		return newEvi;
	}


	public void inferenceExpandAllLevels() throws NotEvaluableNetworkException {
		inferenceExpandLevels(Integer.MAX_VALUE);		
	}


	private class TreePanelListener implements ActionListener, MouseListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String actionCommand = e.getActionCommand();
			switch (actionCommand) {
				case ActionCommands.TREE_EXPAND_NEXT:
					System.out.println("Expanding some levels");
					// Expand N levels
				try {
					inferenceExpandNextLevel();
				} catch (NotEvaluableNetworkException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
					break;
				case ActionCommands.TREE_EXPAND_ALL:
					System.out.println("Expanding all levels");
					// Expand all levels
				try {
					inferenceExpandAllLevels();
				} catch (NotEvaluableNetworkException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
					break;
				case ActionCommands.TREE_OPEN_NETWORK:
					System.out.println("Opening associated network");
					openAssociatedNetwork();
					// Open tree
					break;
				case ActionCommands.TREE_SHOW_CEP:
					System.out.println("Opening associated CEP");
					openAssociatedCEP();

					break;
                case ActionCommands.TREE_SAVE_GRAPHVIZ:
                    System.out.println("Doing something wonderful");
                    // Show CEP or utility
                    TreeNodeToDot tree2dot = new TreeNodeToDot();
                    Object selectedComponent = jTree.getLastSelectedPathComponent();
                    if (selectedComponent instanceof DecisionTreeNodePanel) {
                        DecisionTreeNodePanel treeNodePanel = (DecisionTreeNodePanel) selectedComponent;
                        DecisionTreeNode treeNode = treeNodePanel.getTreeNode();
                        tree2dot.paintDTNode(treeNode);
                    }
                    break;
				default:

			}
		}
		

		private void openAssociatedCEP() {
			Object selectedComponent = jTree.getLastSelectedPathComponent();
			if (selectedComponent instanceof DecisionTreeNodePanel) {
				DecisionTreeNodePanel treeNodePanel = (DecisionTreeNodePanel) selectedComponent;
				DecisionTreeNode treeNode = treeNodePanel.getTreeNode();
				CEPDialog cepDialog = new CEPDialog(null, (CEP)(treeNode.getUtility()), treeNode.getNetwork());
				cepDialog.setVisible(true);
			}
		}


		private void openAssociatedNetwork() {
			
			Object selectedComponent = jTree.getLastSelectedPathComponent();
			if (selectedComponent instanceof DecisionTreeNodePanel) {				
				MainPanel.getUniqueInstance().getMainPanelListenerAssistant().openNetwork(getNetwork(selectedComponent));	
			}
			
		}

		
		
		
		public ProbNet getNetwork(Object selectedComponent) {
			
			DecisionTreeNodePanel treeNodePanel = (DecisionTreeNodePanel) selectedComponent;
			DecisionTreeNode treeNode = treeNodePanel.getTreeNode();
			return treeNode.getNetwork();
		}
			
		

	

		/* Listener methods */
		// Open tree contextual menu on right click
		@Override
		public void mouseClicked(MouseEvent e) {

			if (SwingUtilities.isRightMouseButton(e)) {

				int row = jTree.getClosestRowForLocation(e.getX(), e.getY());
				jTree.setSelectionRow(row); // Select the right-clicked component

				/* Show menu only if the tree element:
					1. is a node
					2. is a chance or decision one
				*/
				Object selectedComponent = jTree.getLastSelectedPathComponent();
				if (selectedComponent instanceof DecisionTreeNodePanel) {
					NodeType nodeType = ((DecisionTreeNodePanel) selectedComponent).getNodeType();
					//if (nodeType == NodeType.CHANCE || nodeType == NodeType.DECISION) {
					if (nodeType == NodeType.CHANCE || nodeType == NodeType.DECISION || nodeType == NodeType.UTILITY) {
						// Get menu from the contextualMenuFactory
						Type type = getNetwork(selectedComponent).getInferenceOptions().getMultiCriteriaOptions().getMulticriteriaType();
						TreeContextualMenu treeMenu = (TreeContextualMenu) contextualMenuFactory.getTreeContextualMenu(type == Type.COST_EFFECTIVENESS);
						treeMenu.show(e.getComponent(), e.getX(), e.getY());
					}
				}

			}
		}

		public void mousePressed(MouseEvent mouseEvent) { }
		public void mouseReleased(MouseEvent mouseEvent) { }
		public void mouseEntered(MouseEvent mouseEvent) { }
		public void mouseExited(MouseEvent mouseEvent) { }
	}


	public DecisionTreeNode getDecisionTreeNode() {
		DecisionTree dt = (DecisionTree)getJTree();				
		TreeModel model = dt.getModel();
		DecisionTreeBranchPanel branchPanel = (DecisionTreeBranchPanel) model.getRoot();
		DecisionTreeBranch root = branchPanel.getTreeBranch();
		return root.getChild();
	}
}
