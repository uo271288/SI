/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window;

import org.apache.commons.io.FilenameUtils;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.NotRecognisedNetworkFileExtensionException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.io.database.CaseDatabase;
import org.openmarkov.core.io.database.CaseDatabaseReader;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.TemporalNetOperations;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.oopn.Instance.ParameterArity;
import org.openmarkov.core.oopn.OOPNet;
import org.openmarkov.gui.configuration.LastOpenFiles;
import org.openmarkov.gui.configuration.OpenMarkovPreferences;
import org.openmarkov.gui.dialog.AboutBox;
import org.openmarkov.gui.dialog.LanguageDialog;
import org.openmarkov.gui.dialog.SelectZoomDialog;
import org.openmarkov.gui.dialog.ShortcutsBox;
import org.openmarkov.gui.dialog.common.CommentHTMLScrollPane;
import org.openmarkov.gui.dialog.configuration.PreferencesDialog;
import org.openmarkov.gui.dialog.inference.common.InferenceOptionsDialog;
import org.openmarkov.gui.dialog.io.DBReaderFileChooser;
import org.openmarkov.gui.dialog.io.FileChooser;
import org.openmarkov.gui.dialog.io.FileFilterAll;
import org.openmarkov.gui.dialog.io.FileFilterBasic;
import org.openmarkov.gui.dialog.io.NetsIO;
import org.openmarkov.gui.dialog.io.NetworkFileChooser;
import org.openmarkov.gui.dialog.io.SaveOptions;
import org.openmarkov.gui.dialog.io.URLNetworkChooserDialog;
import org.openmarkov.gui.dialog.network.NetworkPropertiesDialog;
import org.openmarkov.gui.dialog.network.OptimalStrategyDialog;
import org.openmarkov.gui.localize.LocalizedException;
import org.openmarkov.gui.localize.StringDatabase;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.plugin.ToolPluginManager;
import org.openmarkov.gui.util.PropertyNames;
import org.openmarkov.gui.util.Utilities;
import org.openmarkov.gui.window.dt.DecisionTreeWindow;
import org.openmarkov.gui.window.edition.NetworkPanel;
import org.openmarkov.gui.window.mdi.FrameContentPanel;
import org.openmarkov.gui.window.mdi.MDIListener;
import org.openmarkov.gui.window.message.MessageWindow;
import org.openmarkov.inference.decompositionIntoSymmetricDANs.evaluation.DANEvaluation;
import org.openmarkov.inference.decompositionIntoSymmetricDANs.evaluation.DANDecompositionIntoSymmetricDANsEvaluation;
import org.openmarkov.inference.variableElimination.tasks.VEOptimalIntervention;

import javax.swing.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
//TODO: remove just because reference to cost-effectiveness was removed
//import org.openmarkov.costeffectiveness.id.inference.VariableEliminationCE;

/**
 * This class receives the main events of the application and helps the class
 * MainMenu to carry out this task.
 *
 * @author jmendoza
 * @version 1.6 - carmenyago - Modify saveNetworkActions method to support several ProbModelXML formats
 */
public class MainPanelListenerAssistant extends WindowAdapter
		implements ActionListener, MDIListener, PropertyNames, ComponentListener {
	/**
	 * Value for the Zoom increment/decrement
	 */
	private static final double zoomChangeValue = 0.2;
	/**
	 * Counter incremented each time a network frame is created.
	 */
	private static int frameIndex = 1;
	/**
	 * Main panel which this object helps.
	 */
	private MainPanel mainPanel = null;
	/**
	 * last open files instance
	 */
	private LastOpenFiles lastOpenFiles = new LastOpenFiles();
	/**
	 * Messages string resource.
	 */
	// private StringResource stringResource;
	private List<NetworkPanel> networkPanels;
	private StringDatabase stringDatabase = null;

	/**
	 * Constructor that save the references to the objects that this class
	 * needs.
	 *
	 * @param mainPanel - main panel which this listener helps.
	 */
	public MainPanelListenerAssistant(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
		this.mainPanel.setName(mainPanel.getName());
		// stringResource = StringResourceLoader.getUniqueInstance()
		// .getBundleMessages();
		this.networkPanels = new ArrayList<NetworkPanel>();
		this.stringDatabase = StringDatabase.getUniqueInstance();
	}

	/**
	 * commodity method to provide the path directory for the network file name
	 *
	 * @param fileName - name of the file to obtain the short name
	 * @return the directory of the file
	 */
	private static String getDirectoryFileName(String fileName) {
		return (new File(fileName)).getAbsolutePath();
	}

	/**
	 * Invoked when a window is in the process of being closed.
	 *
	 * @param e - event information.
	 */
	@Override public void windowClosing(WindowEvent e) {
		closeApplication();
	}

	/**
	 * This method listens to the user actions on the main menu.
	 *
	 * @param e menu event information.
	 */
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		if (actionCommand.equals(ActionCommands.NEW_NETWORK)) {
			createNewNetwork();
		} else if (actionCommand.equals(ActionCommands.OPEN_NETWORK)) {
			openNetwork();
		} else if (actionCommand.equals(ActionCommands.OPEN_NETWORK_URL)) {
			openNetworkURL();
		} else if (actionCommand.equals(ActionCommands.OPEN_LAST_1_FILE)) {
			openNetwork(lastOpenFiles.getFileNameAt(1));
		} else if (actionCommand.equals(ActionCommands.OPEN_LAST_2_FILE)) {
			openNetwork(lastOpenFiles.getFileNameAt(2));
		} else if (actionCommand.equals(ActionCommands.OPEN_LAST_3_FILE)) {
			openNetwork(lastOpenFiles.getFileNameAt(3));
		} else if (actionCommand.equals(ActionCommands.OPEN_LAST_4_FILE)) {
			openNetwork(lastOpenFiles.getFileNameAt(4));
		} else if (actionCommand.equals(ActionCommands.OPEN_LAST_5_FILE)) {
			openNetwork(lastOpenFiles.getFileNameAt(5));
		} else if (actionCommand.equals(ActionCommands.SAVE_NETWORK)) {
			saveNetwork(getCurrentNetworkPanel());
		} else if (actionCommand.equals(ActionCommands.SAVE_OPEN_NETWORK)) {
			saveOpenNetwork(getCurrentNetworkPanel());
		} else if (actionCommand.equals(ActionCommands.SAVEAS_NETWORK)) {
			saveNetworkAs(getCurrentNetworkPanel());
			// If the file was opened from a URL, the 'save' and 'save and reopen' button are disabled,
			// but this is not longer the scenario
			mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkOpenedURL(false);
		} else if (actionCommand.equals(ActionCommands.CLOSE_NETWORK)) {
			closeCurrentNetwork();
		} else if (actionCommand.equals(ActionCommands.LOAD_EVIDENCE)) {
			loadEvidence(getCurrentNetworkPanel());
		} else if (actionCommand.equals(ActionCommands.SAVE_EVIDENCE)) {
			saveEvidence(getCurrentNetworkPanel());
		} else if (actionCommand.equals(ActionCommands.NETWORK_PROPERTIES)) {
			getCurrentNetworkPanel().changeNetworkProperties();
		} else if (actionCommand.equals(ActionCommands.EXPAND_NETWORK)) {
			expandNetwork(getCurrentNetworkPanel().getProbNet(),
					getCurrentNetworkPanel().getEditorPanel().getPreResolutionEvidence());
		} else if (actionCommand.equals(ActionCommands.EXPAND_NETWORK_CE)) {
			expandNetworkCE(getCurrentNetworkPanel().getProbNet(),
					getCurrentNetworkPanel().getEditorPanel().getPreResolutionEvidence());
		} else if (actionCommand.equals(ActionCommands.EXIT_APPLICATION)) {
			closeApplication();
		} else if (actionCommand.equals(ActionCommands.CLIPBOARD_COPY)) {
			getCurrentNetworkPanel().exportToClipboard(false);
			mainPanel.getMainPanelMenuAssistant().setOptionEnabled(ActionCommands.CLIPBOARD_PASTE, true);
		} else if (actionCommand.equals(ActionCommands.CLIPBOARD_CUT)) {
			getCurrentNetworkPanel().exportToClipboard(true);
			mainPanel.getMainPanelMenuAssistant().setOptionEnabled(ActionCommands.CLIPBOARD_PASTE, true);
		} else if (actionCommand.equals(ActionCommands.CLIPBOARD_PASTE)) {
			getCurrentNetworkPanel().pasteFromClipboard();
		} else if (actionCommand.equals(ActionCommands.UNDO)) {
			undo();
		} else if (actionCommand.equals(ActionCommands.REDO)) {
			redo();
		} else if (actionCommand.equals(ActionCommands.SELECT_ALL)) {
			getCurrentNetworkPanel().selectAllObjects();
		} else if (actionCommand.equals(ActionCommands.OBJECT_REMOVAL)) {
			getCurrentNetworkPanel().removeSelectedObjects();
		} else if (actionCommand.startsWith(ActionCommands.EDITION_MODE_PREFIX)) {
			activateEditionMode(actionCommand);
		} else if (actionCommand.equals(ActionCommands.CHANGE_WORKING_MODE)) {
			setNewWorkingMode();
		} else if (actionCommand.equals(ActionCommands.CHANGE_TO_INFERENCE_MODE)) {
			setNewWorkingMode();
		} else if (actionCommand.equals(ActionCommands.CHANGE_TO_EDITION_MODE)) {
			setNewWorkingMode();
		} else if (actionCommand.equals(ActionCommands.SET_NEW_EXPANSION_THRESHOLD)) {
			setNewExpansionThreshold((Double) e.getSource());
		} else if (actionCommand.equals(ActionCommands.CREATE_NEW_EVIDENCE_CASE)) {
			evidenceCasesNavigationOption("CREATE_NEW_EVIDENCE_CASE");
		} else if (actionCommand.equals(ActionCommands.GO_TO_FIRST_EVIDENCE_CASE)) {
			evidenceCasesNavigationOption("GO_TO_FIRST_EVIDENCE_CASE");
		} else if (actionCommand.equals(ActionCommands.GO_TO_PREVIOUS_EVIDENCE_CASE)) {
			evidenceCasesNavigationOption("GO_TO_PREVIOUS_EVIDENCE_CASE");
		} else if (actionCommand.equals(ActionCommands.GO_TO_NEXT_EVIDENCE_CASE)) {
			evidenceCasesNavigationOption("GO_TO_NEXT_EVIDENCE_CASE");
		} else if (actionCommand.equals(ActionCommands.GO_TO_LAST_EVIDENCE_CASE)) {
			evidenceCasesNavigationOption("GO_TO_LAST_EVIDENCE_CASE");
		} else if (actionCommand.equals(ActionCommands.CLEAR_OUT_ALL_EVIDENCE_CASES)) {
			evidenceCasesNavigationOption("CLEAR_OUT_ALL_EVIDENCE_CASES");
		} else if (actionCommand.equals(ActionCommands.PROPAGATE_EVIDENCE)) {
			getCurrentNetworkPanel().propagateEvidence(mainPanel.getMainPanelMenuAssistant());
		} else if (actionCommand.equals(ActionCommands.ABSORB_NODE)) {
            this.getCurrentNetworkPanel().absorbNode();
		} else if (actionCommand.equals(ActionCommands.ABSORB_PARENTS)) {
            this.getCurrentNetworkPanel().absorbParents();
        } else if (actionCommand.equals(ActionCommands.NODE_PROPERTIES)) {
			getCurrentNetworkPanel().changeNodeProperties();
		} else if (actionCommand.equals(ActionCommands.EDIT_POTENTIAL)) {
			getCurrentNetworkPanel().changePotential();
		} else if (actionCommand.equals(ActionCommands.DECISION_IMPOSE_POLICY)) {
			getCurrentNetworkPanel().imposePolicyInNode();
		} else if (actionCommand.equals(ActionCommands.DECISION_EDIT_POLICY)) {
			getCurrentNetworkPanel().editNodePolicy();
		} else if (actionCommand.equals(ActionCommands.DECISION_REMOVE_POLICY)) {
			getCurrentNetworkPanel().removePolicyFromNode();
		} else if (actionCommand.equals(ActionCommands.DECISION_SHOW_EXPECTED_UTILITY)) {
			getCurrentNetworkPanel().showExpectedUtilityOfNode();
		} else if (actionCommand.equals(ActionCommands.DECISION_SHOW_OPTIMAL_POLICY)) {
			getCurrentNetworkPanel().showOptimalPolicyOfNode();
		} else if (actionCommand.equals(ActionCommands.NODE_EXPANSION)) {
			getCurrentNetworkPanel().expandNode();
		} else if (actionCommand.equals(ActionCommands.NODE_CONTRACTION)) {
			getCurrentNetworkPanel().contractNode();
		} else if (actionCommand.equals(ActionCommands.NODE_ADD_FINDING)) {
			getCurrentNetworkPanel().addFinding();
		} else if (actionCommand.equals(ActionCommands.NODE_REMOVE_FINDING)) {
			getCurrentNetworkPanel().removeFinding();
		} else if (actionCommand.equals(ActionCommands.NODE_REMOVE_ALL_FINDINGS)) {
			getCurrentNetworkPanel().removeAllFindings();
		} else if (actionCommand.equals(ActionCommands.BYTITLE_NODES)) {
			activateByTitle(true);
		} else if (actionCommand.equals(ActionCommands.BYNAME_NODES)) {
			activateByTitle(false);
		} else if (actionCommand.startsWith(ActionCommands.VIEW_TOOLBARS)) {
			MainPanel.getUniqueInstance().getToolbarManager()
					.addToolbar(actionCommand.replace(ActionCommands.VIEW_TOOLBARS + ".", ""));
		} else if (actionCommand.equals(ActionCommands.ZOOM_IN)) {
			incrementZoom(getCurrentPanel());
		} else if (actionCommand.equals(ActionCommands.ZOOM_OUT)) {
			decrementZoom(getCurrentPanel());
		} else if (actionCommand.equals(ActionCommands.ZOOM_OTHER)) {
			setZoom(true, getCurrentPanel(), 0);
		} else if (ActionCommands.isZoomActionCommand(actionCommand)) {
			setZoom(false, getCurrentPanel(), ActionCommands.getValueZoomActionCommand(actionCommand));
		} else if (actionCommand.equals(ActionCommands.MESSAGE_WINDOW)) {
			showMessageWindow();
		}
		//        else if (actionCommand.equals(ActionCommands.COST_EFFECTIVENESS_DETERMINISTIC)) {
		////        	ProbNet probNet = getCurrentNetworkPanel().getProbNet();
		////            showCostEffectivenessResults(probNet, getCurrentNetworkPanel().getEditorPanel().getPreResolutionEvidence());
		//
		//        }
		//        else if (actionCommand.equals(ActionCommands.COST_EFFECTIVENESS_SENSITIVITY)) {
		//            ProbNet probNet = getCurrentNetworkPanel().getProbNet();
		//            showCostEffectivenessSensitivityResults(probNet,
		//                    getCurrentNetworkPanel().getEditorPanel().getPreResolutionEvidence());
		//        }
		 else if (actionCommand.equals(ActionCommands.CONFIGURATION)) {
			showUserConfigurationDialog();
		} else if (actionCommand.equals(ActionCommands.PROPAGATION_OPTIONS)) {
			setPropagationOptions();
		} else if (actionCommand.equals(ActionCommands.INFERENCE_OPTIONS)) {
			setInferenceOptions(getCurrentNetworkPanel());
		} else if (actionCommand.equals(ActionCommands.HELP_CHANGE_LANGUAGE)) {
			showLanguageChangeDialog();
		} else if (actionCommand.equals(ActionCommands.HELP_SHORTCUTS)) {
			showShortcuts();
		 } else if (actionCommand.equals(ActionCommands.HELP_ABOUT)) {
			showAbout();
		} else if (actionCommand.equals(ActionCommands.INVERT_LINK_AND_UPDATE_POTENTIALS)) {
			this.getCurrentNetworkPanel().invertLinkAndUpdatePotentials();
		} else if (actionCommand.equals(ActionCommands.LINK_RESTRICTION_ENABLE_PROPERTIES)) {
			this.getCurrentNetworkPanel().enableLinkRestriction();
		} else if (actionCommand.equals(ActionCommands.LINK_RESTRICTION_EDIT_PROPERTIES)) {
			this.getCurrentNetworkPanel().enableLinkRestriction();
		} else if (actionCommand.equals(ActionCommands.LINK_RESTRICTION_DISABLE_PROPERTIES)) {
			this.getCurrentNetworkPanel().disableLinkRestriction();
		} else if (actionCommand.equals(ActionCommands.LINK_REVELATIONARC_PROPERTIES)) {
			this.getCurrentNetworkPanel().enableRevelationArc();
			// TODO OOPN start
		} else if (actionCommand.equals(ActionCommands.TEMPORAL_EVOLUTION_ACTION)) {
			this.getCurrentNetworkPanel().temporalEvolution();
		} else if (actionCommand.equals(ActionCommands.MARK_AS_INPUT)) {
			this.getCurrentNetworkPanel().markSelectedAsInput();
		} else if (actionCommand.equals(ActionCommands.EDIT_CLASS)) {
			this.getCurrentNetworkPanel().editClass();
		} else if (actionCommand.equals(ActionCommands.EDIT_INSTANCE_NAME)) {
			this.getCurrentNetworkPanel().editInstanceName();
		} else if (actionCommand.equals(ActionCommands.SET_ARITY_ONE)) {
			this.getCurrentNetworkPanel().setParameterArity(ParameterArity.ONE);
		} else if (actionCommand.equals(ActionCommands.SET_ARITY_MANY)) {
			this.getCurrentNetworkPanel().setParameterArity(ParameterArity.MANY);
			// TODO OOPN end
		} else if (actionCommand.equals(ActionCommands.DECISION_TREE)) {
			showDecisionTree(this.getCurrentNetworkPanel().getProbNet());
		} else if (actionCommand.equals(ActionCommands.DECISION_SHOW_OPTIMAL_STRATEGY)) {
			showOptimalStrategy(this.getCurrentNetworkPanel());
		} else if (actionCommand.equals(ActionCommands.NEXT_SLICE_NODE)) {
			this.getCurrentNetworkPanel().createNextSliceNode();
		} else {
			// TODO - Get ToolPluginManager Exceptions
			ToolPluginManager.getInstance().processCommand(actionCommand, mainPanel.getMainFrame());
		}
	}

	/**
	 * Create a Frame for a Change Language dialog
	 */
	private void showLanguageChangeDialog() {
		LanguageDialog.getUniqueInstance(mainPanel.getMainFrame()).setVisible(true);
	}

	/**
	 * Create a Frame for the User Configuration dialog
	 *
	 * @return a UserConfiguration dialog
	 */
	private PreferencesDialog showUserConfigurationDialog() {
		return new PreferencesDialog(mainPanel.getMainFrame());
	}

	/**
	 * Create a Frame for shortcuts information
	 *
	 * @return a JDialog (shortcutsBox with shortcut information
	 */
	private ShortcutsBox showShortcuts() {
		return new ShortcutsBox(mainPanel.getMainFrame());
	}

	/**
	 * Create a Frame for About information
	 *
	 * @return aboutBox the AboutBox dialog
	 */
	private AboutBox showAbout() {
		return new AboutBox(mainPanel.getMainFrame());
	}

	/**
	 * Returns the current network panel of the current frame.
	 *
	 * @return the current network panel.
	 */
	public NetworkPanel getCurrentNetworkPanel() {
		return mainPanel.getMainPanelMenuAssistant().getCurrentNetworkPanel();
	}

	/**
	 * Returns the current network panel of the current frame.
	 *
	 * @return the current network panel.
	 */
	public FrameContentPanel getCurrentPanel() {
		return mainPanel.getMdi().getCurrentPanel();
	}

	/**
	 * Returns a value indicating if the network can be closed. If the network
	 * has not been saved, this method offers to the users the possibility of
	 * save it. If the user answers 'yes', the network is saved and can be
	 * closed. If the user answers 'no', the network isn't saved and can be
	 * closed. If the user answers 'cancel', the network can't be closed.
	 *
	 * @param networkPanel network panel to be checked.
	 * @return true, if the network can be closed; otherwise, false.
	 */
	private boolean networkCanBeClosed(NetworkPanel networkPanel) {
		int response = 0;
		boolean canClose = true;
		if (networkPanel.getModified()) {
			String title = stringDatabase.getFormattedString("NetworkNotSaved.Title.Label", networkPanel.getTitle());
			String message = stringDatabase.getFormattedString("NetworkNotSaved.Text.Label", networkPanel.getTitle());
			response = JOptionPane
					.showConfirmDialog(Utilities.getOwner(mainPanel), message, title, JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE);
			switch (response) {
			case JOptionPane.YES_OPTION: {
				canClose = saveNetwork(networkPanel);
				break;
			}
			case JOptionPane.NO_OPTION: {
				canClose = true;
				break;
			}
			default: {
				return false;
			}
			}
		}
		if (canClose) {
			networkPanels.remove(networkPanel);
		}
		return canClose;
	}

	/**
	 * This method executes when a network frame is going to be closed.
	 *
	 * @param contentPanel content panel of the frame that is trying to be closed.
	 * @return true, if the frame that contents the panel can be closed;
	 * otherwise, false.
	 */
	public boolean frameClosing(FrameContentPanel contentPanel) {
		contentPanel.close();
		if (NetworkPanel.class.isAssignableFrom(contentPanel.getClass())) {
			return networkCanBeClosed((NetworkPanel) contentPanel);
		} else {
			return true;
		}
	}

	/**
	 * This method executes when a frame has been closed.
	 *
	 * @param contentPanel content panel of the frame that has been closed.
	 */
	public void frameClosed(FrameContentPanel contentPanel) {
		if (networkPanels.size() == 0) {
			mainPanel.setToolBarPanel(NetworkPanel.EDITION_WORKING_MODE);
			mainPanel.getMainPanelMenuAssistant().updateOptionsAllNetworkClosed();
		}
	}

	//CMI
	//    /**
	//     * Saves a network in a file and makes the rest of actions in the
	//     * environment (menus, messages, etc.).
	//     *
	//     * @param networkPanel
	//     *            network panel which contains the network to be saved.
	//     * @param fileName
	//     *            file where save the network.
	//     * @param saveOptions
	//     * @return true if the network could be saved; otherwise, false.
	//     */
	//    private boolean saveNetworkActions(NetworkPanel networkPanel,
	//            String fileName,
	//            SaveOptions saveOptions) {
	//        boolean result = false;
	//        mainPanel.getMessageWindow().getNormalMessageStream().println(stringDatabase.getValuesInAString("SavingNetwork.Text.Label")
	//                + " "
	//                + fileName);
	//        try {
	//            if (saveOptions != null && saveOptions.isSavePlainNetwork()) {
	//                networkPanel.showPlainNetwork();
	//            }
	//            if (saveOptions != null
	//                    && saveOptions.isSaveClassesInFile()
	//                    && networkPanel.getProbNet() instanceof OOPNet) {
	//                ((OOPNet) networkPanel.getProbNet()).fillClassList();
	//            }
	//
	//            NetsIO.saveNetworkFile(networkPanel.getProbNet(),
	//                    networkPanel.getEditorPanel().getEvidence(),
	//                    fileName);
	//            // networkPanel.getNetwork().backupProbNet.saveToFile( fileName );
	//            networkPanel.setModified(false);
	//            networkPanel.setNetworkFile(fileName);
	//            mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkSaved();
	//            lastOpenFiles.setLastFileName(fileName);
	//            OpenMarkovPreferences.set(OpenMarkovPreferences.LAST_OPEN_DIRECTORY,
	//                    getDirectoryFileName(fileName),
	//                    OpenMarkovPreferences.OPENMARKOV_DIRECTORIES);
	//            mainPanel.getMessageWindow().getNormalMessageStream().println(stringDatabase.getValuesInAString("NetworkSaved.Text.Label"));
	//            mainPanel.getMainMenu().rechargeLastOpenFiles();
	//            result = true;
	//        } catch (NotRecognisedNetworkFileExtensionException e) {
	//            JOptionPane.showMessageDialog(Utilities.getOwner(mainPanel),
	//                    stringDatabase.getValuesInAString("CanNotRecognisedFileExtension.Text.Label"),
	//                    stringDatabase.getValuesInAString("ErrorWindow.Title.Label"),
	//                    JOptionPane.ERROR_MESSAGE);
	//        } catch (CanNotWriteNetworkToFileException e) {
	//            JOptionPane.showMessageDialog(Utilities.getOwner(mainPanel),
	//                    stringDatabase.getValuesInAString("ErrorSavingNetwork.Text.Label") + ": " + e.getMessage(),
	//                    stringDatabase.getValuesInAString("ErrorWindow.Title.Label"),
	//                    JOptionPane.ERROR_MESSAGE);
	//        } catch (Exception e) {
	//            JOptionPane.showMessageDialog(Utilities.getOwner(mainPanel),
	//                    stringDatabase.getValuesInAString("Generic I/O error"),
	//                    stringDatabase.getValuesInAString("ErrorWindow.Title.Label"),
	//                    JOptionPane.ERROR_MESSAGE);
	//        }
	//        return result;
	//    }

	/**
	 * This method executes when a network frame has been selected.
	 *
	 * @param contentPanel content panel of the frame that has been selected.
	 */
	public void frameSelected(FrameContentPanel contentPanel) {
		if (contentPanel instanceof NetworkPanel) {
			mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkDependent((NetworkPanel) contentPanel);
			mainPanel.getInferenceToolBar().setCurrentEvidenceCaseName(getCurrentNetworkPanel().getCurrentCase());
			mainPanel.getMainPanelMenuAssistant().updateOptionsWindowSelected(true);
		} else if (contentPanel instanceof MessageWindow) {
			mainPanel.getMainPanelMenuAssistant().updateOptionsWindowSelected(false);
		} else if (contentPanel instanceof DecisionTreeWindow) {
			mainPanel.getMainPanelMenuAssistant().updateOptionsDecisionTree((DecisionTreeWindow) contentPanel);
		}
	}

	//CMF

	/**
	 * Saves a network in a file considering the file format chosen. Also it makes the rest of actions in the
	 * environment (menus, messages, etc.).
	 *
	 * @param networkPanel network panel which contains the network to be saved.
	 * @param fileName     file where save the network.
	 * @param saveOptions
	 * @return true if the network could be saved; otherwise, false.
	 * @author carmenyago
	 */
	private boolean saveNetworkActions(NetworkPanel networkPanel, String fileName, String fileFormat,
			SaveOptions saveOptions) {
		boolean result = false;
		mainPanel.getMessageWindow().getNormalMessageStream()
				.println(stringDatabase.getString("SavingNetwork.Text.Label") + " " + fileName);
		try {
			if (saveOptions != null && saveOptions.isSavePlainNetwork()) {
				networkPanel.showPlainNetwork();
			}
			if (saveOptions != null && saveOptions.isSaveClassesInFile() && networkPanel
					.getProbNet() instanceof OOPNet) {
				((OOPNet) networkPanel.getProbNet()).fillClassList();
			}
			//CMI
/*
            NetsIO.saveNetworkFile(networkPanel.getProbNet(),
                    networkPanel.getEditorPanel().getEvidence(),
                    fileName);
*/
			NetsIO.saveNetworkFile(networkPanel.getProbNet(), networkPanel.getEditorPanel().getEvidence(), fileName,
					fileFormat);

			//CMF
			// networkPanel.getNetwork().backupProbNet.saveToFile( fileName );
			networkPanel.setModified(false);
			networkPanel.setNetworkFile(fileName);
			//CMI
			networkPanel.setNetworkFileFormat(fileFormat);
			//CMF
			mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkSaved();
			lastOpenFiles.setLastFileName(fileName);
			OpenMarkovPreferences.set(OpenMarkovPreferences.LAST_OPEN_DIRECTORY, getDirectoryFileName(fileName),
					OpenMarkovPreferences.OPENMARKOV_DIRECTORIES);
			mainPanel.getMessageWindow().getNormalMessageStream()
					.println(stringDatabase.getString("NetworkSaved.Text.Label"));
			mainPanel.getMainMenu().rechargeLastOpenFiles();
			result = true;
		} catch (NotRecognisedNetworkFileExtensionException e) {
			LocalizedException extensionException = new LocalizedException(new OpenMarkovException(
					"NotRecognisedNetworkFileExtensionException", stringDatabase.getString("CanNotRecognisedFileExtension.Text.Label") + e.getToken()), null);
			extensionException.showException();
//			JOptionPane.showMessageDialog(Utilities.getOwner(mainPanel),
//					stringDatabase.getString("CanNotRecognisedFileExtension.Text.Label"),
//					stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
		} catch (OpenMarkovException e) {
			LocalizedException someBadThingHappenedException = new LocalizedException(new OpenMarkovException(
					"GenericException", stringDatabase.getString("ErrorSavingNetwork.Text.Label") + ": " + e.getMessage()), null);
			someBadThingHappenedException.showException();
//			JOptionPane.showMessageDialog(Utilities.getOwner(mainPanel),
//					stringDatabase.getString("ErrorSavingNetwork.Text.Label") + ": " + e.getMessage(),
//					stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
			LocalizedException someBadThingHappenedException = new LocalizedException(new OpenMarkovException(
					"GenericException", stringDatabase.getString("Generic I/O error")), null);
			someBadThingHappenedException.showException();
//			JOptionPane.showMessageDialog(Utilities.getOwner(mainPanel), stringDatabase.getString("Generic I/O error"),
//					stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
		}
		return result;
	}

	/**
	 * Saves a network in the file given by
	 * @param networkPanel
	 * @param fileName	the file where the network is stored
	 * @return true iff the network could be saved
	 */

	private boolean saveNetworkActions(NetworkPanel networkPanel, String fileName) {
		String fileFormat = OpenMarkovPreferences
				.get(OpenMarkovPreferences.LAST_OPENED_FORMAT, OpenMarkovPreferences.OPENMARKOV_FORMATS,
						FileChooser.DEFAULT_FILE_FORMAT);
		return saveNetworkActions(networkPanel, fileName, fileFormat, null);
	}

	/**
	 * Save a network. First it requests the file in which save the network and
	 * then saves the network.
	 *
	 * @param networkPanel network panel that contains the network to be saved.
	 * @return true if the network has been saved; otherwise, false.
	 */
	private boolean saveNetwork(NetworkPanel networkPanel) {
		String fileName = networkPanel.getNetworkFile();
		if (fileName != null) {
			createBackUpNetworkFile(fileName, toBakExtension(networkPanel.getNetworkFile()));
		}
		return (fileName != null) ? saveNetworkActions(networkPanel, fileName) : saveNetworkAs(networkPanel);
	}

	/**
	 * Save a network. First it requests the file in which save the network and
	 * then saves the network.
	 *
	 * @param networkPanel network panel that contains the network to be saved.
	 */
	private void saveOpenNetwork(NetworkPanel networkPanel) {
		String fileName = networkPanel.getNetworkFile();
		if (fileName != null) {
			createBackUpNetworkFile(fileName, toBakExtension(networkPanel.getNetworkFile()));
		}
		saveNetwork(networkPanel);
		fileName = networkPanel.getNetworkFile();
		closeCurrentNetwork();
		openNetwork(fileName);
	}

	private void createBackUpNetworkFile(String fileName, String newFileName) {
		try {
			File inFile = new File(fileName);
			File outFile = new File(newFileName);
			FileInputStream in = new FileInputStream(inFile);
			FileOutputStream out = new FileOutputStream(outFile);
			int c;
			while ((c = in.read()) != -1)
				out.write(c);
			in.close();
			out.close();
		} catch (IOException e) {
			LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
					"WriterException", stringDatabase.getString("NetworkBackupError.Text.Label")), null);
			localizedException.showException();
//			mainPanel.getMessageWindow().getNormalMessageStream()
//					.println(stringDatabase.getString("NetworkBackupError.Text.Label"));
		}
		mainPanel.getMessageWindow().getNormalMessageStream()
				.println(stringDatabase.getString("NetworkBackup.Text.Label"));
	}

	private String toBakExtension(String nameFile) {
		String newName;
		int index = nameFile.lastIndexOf(".");
		if (index > 0) {
			newName = nameFile.substring(0, index);
		} else
			newName = nameFile;
		return newName + ".bak";
	}

	/**
	 * Save a network in a different file. First it requests the file in which
	 * save the network and then saves the network.
	 *
	 * @param networkPanel network panel that contains the network to be saved.
	 * @return true if the network has been saved; otherwise, false.
	 */
	private boolean saveNetworkAs(NetworkPanel networkPanel) {
		String fileName = networkPanel.getNetworkFile();
		//CMI
        /*
        fileName = requestNetworkFileToSave((fileName != null) ? fileName
                : networkPanel.getProbNet().getName());
        */
		String fileFormat;
		ArrayList<String> fileNameAndFormat = requestNetworkFileAndFormatToSave(
				(fileName != null) ? fileName : networkPanel.getProbNet().getName());
		fileName = fileNameAndFormat.get(0);
		fileFormat = fileNameAndFormat.get(1);
		//CMF
		SaveOptions saveOptions = null;
		if (fileName != null) {
			networkPanel.setNetworkFile(fileName);
			networkPanel.setNetworkFileFormat(fileFormat);
			networkPanel.getProbNet().setName(new File(fileName).getName());
			MainPanel mainPanel = MainPanel.getUniqueInstance();
			saveOptions = new SaveOptions(null, networkPanel.getProbNet(), true);
			if (saveOptions.isWorthShowing()) {
				saveOptions.setLocation(mainPanel.getLocation().x + (mainPanel.getWidth() - saveOptions.getWidth()) / 2,
						mainPanel.getLocation().y + (mainPanel.getHeight() - saveOptions.getHeight()) / 2);
				saveOptions.setVisible(true);
			}
		}
		//CMI
        /*
        return (fileName != null) ? saveNetworkActions(networkPanel, fileName, saveOptions) : false;
        */
		return (fileName != null) ? saveNetworkActions(networkPanel, fileName, fileFormat, saveOptions) : false;
		//CMF
	}

	//CMI

	/**
	 * It asks the user to choose a file by means of a save-file dialog box.
	 *
	 * @param suggestedFileName name of the file where the net can be saved as default.
	 * @return complete path of the file, or null if the user selects cancel.
	 */
	private String requestNetworkFileToSave(String suggestedFileName) {
		NetworkFileChooser fileChooser = new NetworkFileChooser(false, false);
		String title = stringDatabase.getString("SaveNetwork.Title.Label");
		fileChooser.setDialogTitle(title);
		fileChooser.setSelectedFile(new File(suggestedFileName));
		String filename = null;
		if (fileChooser.showSaveDialog(Utilities.getOwner(mainPanel)) == JFileChooser.APPROVE_OPTION) {
			filename = fileChooser.getSelectedFile().getAbsolutePath();
			String chosenFilterExtension = ((FileFilterBasic) fileChooser.getFileFilter()).getFilterExtension();
			if (!filename.toLowerCase().endsWith("." + chosenFilterExtension.toLowerCase())) {
				filename += "." + chosenFilterExtension.toLowerCase();
			}
		}
		return filename;
	}
	//CMF

	/**
	 * @param suggestedFileName
	 * @return a list with the absolute path of of the chosen filename and the file format chosen
	 */
	private ArrayList<String> requestNetworkFileAndFormatToSave(String suggestedFileName) {
		NetworkFileChooser fileChooser = new NetworkFileChooser(false, false);
		String title = stringDatabase.getString("SaveNetwork.Title.Label");
		fileChooser.setDialogTitle(title);
		fileChooser.setSelectedFile(new File(suggestedFileName));
		ArrayList<String> fileNameAndFormat = new ArrayList<String>();
		String filename = null;
		String fileFormat = null;
		if (fileChooser.showSaveDialog(Utilities.getOwner(mainPanel)) == JFileChooser.APPROVE_OPTION) {
			filename = fileChooser.getSelectedFile().getAbsolutePath();
			String chosenFilterExtension = ((FileFilterBasic) fileChooser.getFileFilter()).getFilterExtension();
			if (!filename.toLowerCase().endsWith("." + chosenFilterExtension.toLowerCase())) {
				filename += "." + chosenFilterExtension.toLowerCase();
				File selectedFile = new File(filename);
				if (selectedFile.exists()) {
					int response = JOptionPane.showConfirmDialog(this.getCurrentPanel(), "The file " + selectedFile.getName()
									+ " already exists. The file will be renamed to " + selectedFile.getName() + " (1)." + chosenFilterExtension.toLowerCase(), "Network renamed",
							JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE);

					filename = fileChooser.getSelectedFile().getAbsolutePath() + " (1)." + chosenFilterExtension.toLowerCase();
				}

			}
			fileFormat = ((FileFilterAll) fileChooser.getFileFilter()).getFileDescription();
		}
		fileNameAndFormat.add(filename);
		fileNameAndFormat.add(fileFormat);
		return fileNameAndFormat;
	}

	/**
	 * Creates a new network in the workspace. First, it requests the
	 * additionalProperties of the new network and, if the user accepts the
	 * dialog box, a new network is created.
	 */
	private void createNewNetwork() {
		NetworkPropertiesDialog dialogProperties = new NetworkPropertiesDialog(Utilities.getOwner(mainPanel));
		if (dialogProperties.showProperties() == NetworkPropertiesDialog.OK_BUTTON) {
			ProbNet probNet = dialogProperties.getProbNet();

			// If the probNet has not the OnlyChanceNodes constraint and not has any criterion, we
			// create the default criterion.
			if (!probNet.hasConstraint(OnlyChanceNodes.class) && (
					probNet.getDecisionCriteria() == null || probNet.getDecisionCriteria().isEmpty()
			)) {
				List<Criterion> criteria = new ArrayList<Criterion>();
				criteria.add(new Criterion());
				probNet.setDecisionCriteria(criteria);
			}
			String networkName = stringDatabase.getString("InternalFrame.Title.Label") + " " + frameIndex;
			probNet.setName(networkName);
			probNet.getPNESupport().setWithUndo(true);
			networkPanels.add(createNewFrame(probNet));
			frameIndex++;
			// mainPanelMenuAssistant is added as listener to probNet
			// for menus updated purposes.
			probNet.getPNESupport().addUndoableEditListener(mainPanel.getMainPanelMenuAssistant());
		}
	}

	/**
	 * Creates a new frame in the workspace, suppling the network to be painted
	 * into the frame.
	 *
	 * @param probNet network to be painted into the frame
	 * @return the network panel that is created.
	 */
	public NetworkPanel createNewFrame(ProbNet probNet) {
		NetworkPanel networkPanel = null;
		try {
			networkPanel = new NetworkPanel(probNet, mainPanel);
			mainPanel.getMdi().createNewFrame(networkPanel);
			networkPanel.setContextualMenuFactory(mainPanel.getContextualMenuFactory());
			// networkPanel.addEditionListener( mainPanel
			// .getMainPanelMenuAssistant() );
			networkPanel.addSelectionListener(mainPanel.getMainPanelMenuAssistant());
			mainPanel.getMainPanelMenuAssistant().updateOptionsNewNetworkOpen();
			mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkDependent(networkPanel);
			// mainPanel.getMainPanelMenuAssistant().updateNetworkAgents(networkPanel);
			mainPanel.getInferenceToolBar().setCurrentEvidenceCaseName(getCurrentNetworkPanel().getCurrentCase());
		} catch (UnsupportedOperationException e) {
			LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
					"OpenMarkovException", stringDatabase.getString("ErrorWindow.Title.Label")), null);
			localizedException.showException();
//			JOptionPane.showMessageDialog(Utilities.getOwner(mainPanel), e.getMessage(),
//					stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
		}
		return networkPanel;
	}

	/**
	 * Open a network.
	 */
	private void openNetwork() {
		openNetwork("");
	}

	/**
	 * Open a existing network in a new network frame. If it is not a recently
	 * closed network (registered in the menu), it requests the file which
	 * contains the network and then opens a new network frame.
	 *
	 * @param fileName - for the network
	 */
	public void openNetwork(String fileName) {
		if (fileName.equals("")) {
			fileName = requestNetworkFileToOpen();
		}
		ProbNet netReadFromFile = null;
		NetworkPanel networkPanel = null;
		if (fileName != null) {
			try {
				mainPanel.getMessageWindow().getNormalMessageStream()
						.println(stringDatabase.getString("LoadingNetwork.Text.Label") + " " + fileName);
				ProbNetInfo probNetInfo = NetsIO.openNetworkFile(fileName);
				netReadFromFile = probNetInfo.getProbNet();
				netReadFromFile.getPNESupport().addUndoableEditListener(mainPanel.getMainPanelMenuAssistant());
				netReadFromFile.getPNESupport().setWithUndo(true);
				netReadFromFile.setName(new File(fileName).getName());
				networkPanel = createNewFrame(netReadFromFile);
				networkPanel.setNetworkFile(fileName);
				List<EvidenceCase> evidence = probNetInfo.getEvidence();
				if (evidence != null && !evidence.isEmpty()) {
					EvidenceCase preResolutionEvidence = evidence.get(0);
					evidence.remove(0);
					networkPanel.getEditorPanel().setEvidence(preResolutionEvidence, evidence);
				}
				networkPanels.add(networkPanel);
				lastOpenFiles.setLastFileName(fileName);
				if (getDirectoryFileName(fileName) != null) {
					OpenMarkovPreferences.set(OpenMarkovPreferences.LAST_OPEN_DIRECTORY, getDirectoryFileName(fileName),
							OpenMarkovPreferences.OPENMARKOV_DIRECTORIES);
					// If the file was opened from a URL, the 'save' and 'save and reopen' button are disabled,
					// but it is not longer the scenario
					mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkOpenedURL(false);
				}
				mainPanel.getMessageWindow().getNormalMessageStream()
						.println(stringDatabase.getString("NetworkLoaded.Text.Label"));
				mainPanel.getMainMenu().rechargeLastOpenFiles();

				if (netReadFromFile.getShowCommentWhenOpening()) {
					CommentHTMLScrollPane commentHTMLScrollPaneNetworkComment = new CommentHTMLScrollPane();

					commentHTMLScrollPaneNetworkComment.setEditable(false);
					commentHTMLScrollPaneNetworkComment.setCommentHTMLTextPaneText(netReadFromFile.getComment());
					commentHTMLScrollPaneNetworkComment.setPreferredSize(new Dimension(500, 300));
					JOptionPane networkMessagePane = new JOptionPane(commentHTMLScrollPaneNetworkComment,
							JOptionPane.INFORMATION_MESSAGE);
					JDialog networkMessageDialog = networkMessagePane.createDialog(Utilities.getOwner(mainPanel),
							stringDatabase.getString("NetworkCommentWindow.Title.Label"));
					networkMessageDialog.setResizable(true);
					networkMessageDialog.setMinimumSize(new Dimension(500, 300));
					networkMessageDialog.setVisible(true);
				}
			} catch (Exception e) {
				mainPanel.getMessageWindow().getErrorMessageStream().println(e.getMessage());
				LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
						"ParserException", stringDatabase.getString("ErrorLoadingNetwork.Text.Label") + ": " + e.getMessage()), null);
				localizedException.showException();
//				JOptionPane.showMessageDialog(Utilities.getOwner(mainPanel),
//						stringDatabase.getString("ErrorLoadingNetwork.Text.Label") + ": " + e.getMessage(),
//						stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
//				e.printStackTrace();
			}
		}
	}

	public void openNetwork(ProbNet probNet) {
		NetworkPanel newNetworkPanel = createNewFrame(probNet);
		networkPanels.add(newNetworkPanel);
	}

	/**
	 * Open a network from a URL.
	 */
	//TODO: generalize... It's almost the same as openNetwork...
	public void openNetworkURL() {
		URL url = requestURLFileToOpen();
		ProbNet netReadFromURL;
		NetworkPanel networkPanel;
		if (url != null) {
			String urlFile = url.getFile();
			try {
				mainPanel.getMessageWindow().getNormalMessageStream()
						.println(stringDatabase.getString("LoadingNetworkURL.Text.Label") + " " + url);
				ProbNetInfo probNetInfo = NetsIO.openNetworkURL(url);
				netReadFromURL = probNetInfo.getProbNet();
				netReadFromURL.getPNESupport().addUndoableEditListener(mainPanel.getMainPanelMenuAssistant());
				netReadFromURL.getPNESupport().setWithUndo(true);
				netReadFromURL.setName(new File(urlFile).getName());
				networkPanel = createNewFrame(netReadFromURL);
				networkPanel.setNetworkFile(urlFile);
				List<EvidenceCase> evidence = probNetInfo.getEvidence();
				if (evidence != null && !evidence.isEmpty()) {
					EvidenceCase preResolutionEvidence = evidence.get(0);
					evidence.remove(0);
					networkPanel.getEditorPanel().setEvidence(preResolutionEvidence, evidence);
				}
				networkPanels.add(networkPanel);
				lastOpenFiles.setLastFileName(urlFile);
				// If the file was opened from a URL, the 'save' and 'save and reopen' buttons have to be disabled
				mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkOpenedURL(true);
				mainPanel.getMessageWindow().getNormalMessageStream()
						.println(stringDatabase.getString("NetworkLoaded.Text.Label"));
				mainPanel.getMainMenu().rechargeLastOpenFiles();

				if (netReadFromURL.getShowCommentWhenOpening()) {
					CommentHTMLScrollPane commentHTMLScrollPaneNetworkComment = new CommentHTMLScrollPane();

					commentHTMLScrollPaneNetworkComment.setEditable(false);
					commentHTMLScrollPaneNetworkComment.setCommentHTMLTextPaneText(netReadFromURL.getComment());
					commentHTMLScrollPaneNetworkComment.setPreferredSize(new Dimension(500, 300));
					JOptionPane networkMessagePane = new JOptionPane(commentHTMLScrollPaneNetworkComment,
							JOptionPane.INFORMATION_MESSAGE);
					JDialog networkMessageDialog = networkMessagePane.createDialog(Utilities.getOwner(mainPanel),
							stringDatabase.getString("NetworkCommentWindow.Title.Label"));
					networkMessageDialog.setResizable(true);
					networkMessageDialog.setMinimumSize(new Dimension(500, 300));
					networkMessageDialog.setVisible(true);
				}
			} catch (Exception e) {
				mainPanel.getMessageWindow().getErrorMessageStream().println(e.getMessage());
				LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
						"ParserException", stringDatabase.getString("ErrorLoadingNetworkURL.Text.Label") + ": " + e.getMessage()), null);
				localizedException.showException();
//				JOptionPane.showMessageDialog(Utilities.getOwner(mainPanel),
//						stringDatabase.getString("ErrorLoadingNetworkURL.Text.Label") + ": " + e.getMessage(),
//						stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
//				e.printStackTrace();
			}
		}
	}

	/**
	 * It asks the user to choose a file by means of a open-file dialog box.
	 *
	 * @return complete path of the file, or null if the user selects cancel.
	 */
	private String requestNetworkFileToOpen() {
		NetworkFileChooser fileChooser = new NetworkFileChooser();
		fileChooser.setDialogTitle(stringDatabase.getString("OpenNetwork.Title.Label"));
		String fileName = null;
		if (fileChooser.showOpenDialog(Utilities.getOwner(mainPanel)) == JFileChooser.APPROVE_OPTION) {
			fileName = fileChooser.getSelectedFile().getAbsolutePath();
		}
		return fileName;
	}

	/**
	 * It asks the user to choose a file by means of a open-file dialog box.
	 *
	 * @return complete path of the file, or null if the user selects cancel.
	 */
	private URL requestURLFileToOpen() {
		URLNetworkChooserDialog urlNetworkChooserDialog = new URLNetworkChooserDialog(Utilities.getOwner(mainPanel));
		if (urlNetworkChooserDialog.requestNetworkURL() == SelectZoomDialog.OK_BUTTON) {
			return urlNetworkChooserDialog.getNetworkURL();
		} else {
			return null;
		}

	}

	/**
	 * Closes the current network frame.
	 *
	 * @return true if the network has been closed; otherwise, false.
	 */
	private boolean closeCurrentNetwork() {
		boolean canClose = true;
		if (getCurrentNetworkPanel() != null) {
			canClose = networkCanBeClosed(getCurrentNetworkPanel());
			if (canClose) {
				mainPanel.getMdi().closeCurrentFrame();
				if (networkPanels.size() == 0) {
					mainPanel.setToolBarPanel(NetworkPanel.EDITION_WORKING_MODE);
					mainPanel.getMainPanelMenuAssistant().updateOptionsAllNetworkClosed();
				}
			}
		}
		return canClose;
	}

	/**
	 * Process that executes when the user is trying to close the application.
	 */
	private void closeApplication() {
		boolean allClosed = true;
		while (allClosed && networkPanels.size() > 0) {
			allClosed = closeCurrentNetwork();
		}
		if (allClosed) {
			System.exit(0);
		}
	}

	/**
	 * Creates an expanded network from current network
	 */
	private void expandNetwork(ProbNet probNet, EvidenceCase preResolutionEvidence) {
		InferenceOptionsDialog costEffectivenessDialog = new InferenceOptionsDialog(probNet,
				Utilities.getOwner(mainPanel), null);
		if (costEffectivenessDialog.getSelectedButton() == InferenceOptionsDialog.CANCEL_BUTTON) {
			return;
		}

		ProbNet expandedNetwork = TemporalNetOperations.expandNetwork(probNet);
		String fileName = probNet.getName() + "_expanded";
		expandedNetwork.setName(fileName);
		NetworkPanel networkPanel = createNewFrame(expandedNetwork);
		networkPanel.setNetworkFile(fileName);
		networkPanel.getEditorPanel().setEvidence(preResolutionEvidence, new ArrayList<EvidenceCase>());
		networkPanels.add(networkPanel);
	}

	/**
	 * expand the network like it would be done in CE analysis to show it in the
	 * GUI
	 */
	private void expandNetworkCE(ProbNet probNet, EvidenceCase preResolutionEvidence) {
		if (!getCurrentNetworkPanel().getProbNet().getInferenceOptions().getMultiCriteriaOptions()
				.isCeOptionsShowed()) {
			InferenceOptionsDialog costEffectivenessDialog = new InferenceOptionsDialog(probNet,
					Utilities.getOwner(mainPanel), MulticriteriaOptions.Type.COST_EFFECTIVENESS);
			if (costEffectivenessDialog.getSelectedButton() == InferenceOptionsDialog.CANCEL_BUTTON) {
				return;
			}
		}

		EvidenceCase evidence = new EvidenceCase(preResolutionEvidence);

		ProbNet probNetCopy = probNet.deepCopy();

		EvidenceCase evidenceCase = new EvidenceCase();
		for (Finding finding : evidence.getFindings()) {
			String baseName = finding.getVariable().getBaseName();
			int slice = finding.getVariable().getTimeSlice();
			Variable variable = null;
			try {
				variable = probNetCopy.getVariable(baseName, slice);
				if (variable.getVariableType().equals(VariableType.NUMERIC)) {
					Finding findingCopy = new Finding(variable, finding.getNumericalValue());
					findingCopy.setStateIndex(finding.getStateIndex());
					evidenceCase.addFinding(findingCopy);

				} else if (variable.getVariableType().equals(VariableType.DISCRETIZED) || variable.getVariableType()
						.equals(VariableType.FINITE_STATES)) {
					Finding findingCopy = new Finding(variable, variable.getState(finding.getState()));
					evidenceCase.addFinding(findingCopy);
				}
			} catch (NodeNotFoundException e) {
				LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
						"NodeNotFoundException", "Variable " + baseName + " not found."), null);
				localizedException.showException();
			} catch (IncompatibleEvidenceException e) {
				LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
						"IncompatibleEvidenceException", "Conflict in evidence variables."), null);
				localizedException.showException();
			} catch (InvalidStateException e) {
				LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
						"InvalidStateException", variable.getName(), finding.getState()), null);
				localizedException.showException();
			}
		}
		double maxX = 0.0;
		for (Node node : probNetCopy.getNodes()) {
			if (node.getCoordinateX() > maxX) {
				maxX = node.getCoordinateX();
			}
		}
		ProbNet expandedNetwork = TemporalNetOperations.expandNetwork(probNetCopy);
		try {
			evidenceCase.extendEvidence(expandedNetwork);
		} catch (IncompatibleEvidenceException e) {
			LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
					"IncompatibleEvidenceException", "Conflict in evidence variables."), null);
			localizedException.showException();
		} catch (InvalidStateException e) {
			LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
					"InvalidStateException"), null);
			localizedException.showException();
		} catch (WrongCriterionException e) {
			LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
					"WrongCriterionException", e.getCause()), null);
			localizedException.showException();
		}
		//            expandedNetwork = CostEffectivenessAnalysis.adaptMIDforCE(expandedNetwork, evidenceCase);

		// TODO apply changes for transitions at cycle start, end or half cycle.
		TemporalNetOperations.applyDiscountToUtilityNodes(expandedNetwork);
		TemporalNetOperations.transformToID(expandedNetwork);

		String fileName = probNetCopy.getName() + "_expandedCE";
		expandedNetwork.setName(fileName);
		NetworkPanel networkPanel = createNewFrame(expandedNetwork);
		networkPanel.setNetworkFile(fileName);
		networkPanel.getEditorPanel().setEvidence(evidenceCase, new ArrayList<EvidenceCase>());
		networkPanels.add(networkPanel);
	}

	/**
	 * This method saves the evidence of the current network to a file
	 *
	 * @param currentNetworkPanel
	 */
	private void saveEvidence(NetworkPanel currentNetworkPanel) {
		// TODO Implement
		List<EvidenceCase> evidence = currentNetworkPanel.getEditorPanel().getEvidence();
		evidence.add(0, currentNetworkPanel.getEditorPanel().getPreResolutionEvidence());
		JFileChooser fileChooser = new JFileChooser();
		File currentDirectory = new File(OpenMarkovPreferences
				.get(OpenMarkovPreferences.LAST_OPEN_DIRECTORY, OpenMarkovPreferences.OPENMARKOV_DIRECTORIES, "."));
		fileChooser.setCurrentDirectory(currentDirectory);
		String suggestedFileName = currentNetworkPanel.getTitle().replaceFirst("^*", "");
		fileChooser.setSelectedFile(new File(suggestedFileName));
		fileChooser.setAcceptAllFileFilterUsed(false);
		if (fileChooser.showSaveDialog(Utilities.getOwner(mainPanel)) == JFileChooser.APPROVE_OPTION) {
			// save the selected file
			System.out.println("Save evidence file " + fileChooser.getSelectedFile().getAbsolutePath());
		}
	}

	/**
	 * This method tries to load evidence into the current network
	 *
	 * @param currentNetworkPanel
	 */
	private void loadEvidence(NetworkPanel currentNetworkPanel) {
		FileChooser evidenceFileChooser = new DBReaderFileChooser();
		evidenceFileChooser.setDialogTitle(stringDatabase.getString("LoadEvidence.Title.Label"));
		// Set last used evidence format as default
		String lastFileFilter = OpenMarkovPreferences
				.get(OpenMarkovPreferences.LAST_LOADED_EVIDENCE_FORMAT, OpenMarkovPreferences.OPENMARKOV_FORMATS,
						"xls");
		evidenceFileChooser.setFileFilter(lastFileFilter);
		if ((evidenceFileChooser.showOpenDialog(Utilities.getOwner(mainPanel)) == JFileChooser.APPROVE_OPTION)) {
			// load the selected file
			System.out.println("Load evidence file " + evidenceFileChooser.getSelectedFile().getAbsolutePath());
			CaseDatabaseManager caseDbManager = new CaseDatabaseManager();
			CaseDatabaseReader caseDbReader = caseDbManager
					.getReader(FilenameUtils.getExtension(evidenceFileChooser.getSelectedFile().getName()));
			ProbNet currentNet = currentNetworkPanel.getProbNet();
			try {
				CaseDatabase caseDatabase = caseDbReader.load(evidenceFileChooser.getSelectedFile().getAbsolutePath());
				List<Variable> variables = caseDatabase.getVariables();
				int[][] cases = caseDatabase.getCases();
				for (int i = 0; i < cases.length; ++i) {
					EvidenceCase newEvidenceCase = new EvidenceCase();
					for (int j = 0; j < cases[i].length; ++j) {
						Variable variable = null;
						try {
							// Ignore missing values
							if (!variables.get(j).getStateName(cases[i][j]).isEmpty() && !variables.get(j)
									.getStateName(cases[i][j]).equals("?")) {
								variable = currentNet.getVariable(variables.get(j).getName());
								try {
									newEvidenceCase.addFinding(new Finding(variable,
											variable.getStateIndex(variables.get(j).getStateName(cases[i][j]))));
								} catch (InvalidStateException e) {
									LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
											"InvalidStateException", stringDatabase.getString("LoadEvidence.Error.InvalidState.Text") + e
											.getMessage(), stringDatabase.getString("ErrorWindow.Title.Label")), null);
									localizedException.showException();

//									JOptionPane.showMessageDialog(Utilities.getOwner(mainPanel),
//											stringDatabase.getString("LoadEvidence.Error.InvalidState.Text") + e
//													.getMessage(), stringDatabase.getString("ErrorWindow.Title.Label"),
//											JOptionPane.ERROR_MESSAGE);
								}
							}
						} catch (NodeNotFoundException e) {
							LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
									"NodeNotFoundException", stringDatabase.getString("LoadEvidence.Error.UnknownVariable.Text") +
									": " + variables.get(j).getName()), null);
							localizedException.showException();
//							JOptionPane.showMessageDialog(Utilities.getOwner(mainPanel),
//									stringDatabase.getString("LoadEvidence.Error.UnknownVariable.Text") + ": "
//											+ variables.get(j).getName(),
//									stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
						} catch (IncompatibleEvidenceException e) {
							LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
									"IncompatibleEvidenceException", "Conflict in evidence variables."), null);
							localizedException.showException();
//							JOptionPane.showMessageDialog(Utilities.getOwner(mainPanel),
//									stringDatabase.getString("LoadEvidence.Error.IncompatibleEvidence.Text"),
//									stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
						}
					}
					currentNetworkPanel.getEditorPanel().addNewEvidenceCase(newEvidenceCase);
				}
				// save format extension in preferences
				OpenMarkovPreferences.set(OpenMarkovPreferences.LAST_LOADED_EVIDENCE_FORMAT,
						((FileFilterBasic) evidenceFileChooser.getFileFilter()).getFilterExtension(),
						OpenMarkovPreferences.OPENMARKOV_FORMATS);
				OpenMarkovPreferences.set(OpenMarkovPreferences.LAST_OPEN_DIRECTORY,
						getDirectoryFileName(evidenceFileChooser.getSelectedFile().getAbsolutePath()),
						OpenMarkovPreferences.OPENMARKOV_DIRECTORIES);
			} catch (IOException e) {
				LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
						"ParserException", stringDatabase.getString("LoadEvidence.Error.Text")), null);
				localizedException.showException();
//				JOptionPane.showMessageDialog(Utilities.getOwner(mainPanel),
//						stringDatabase.getString("LoadEvidence.Error.Text"),
//						stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
//				e.printStackTrace();
			}
		}
	}

	/**
	 * This method undoes the last operation on the actual network.
	 */
	private void undo() {
		try {
			undoRedo(true);
		} catch (CannotUndoException e) {
			LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
					"UndoException", stringDatabase.getString("CannotUndo.Text.Label")), null);
			localizedException.showException();
//			JOptionPane
//					.showMessageDialog(Utilities.getOwner(mainPanel), stringDatabase.getString("CannotUndo.Text.Label"),
//							stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * This method re-does the last undone operation on the actual network.
	 */
	private void redo() {
		try {
			undoRedo(false);
		} catch (CannotRedoException e) {
			LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
					"RedoException", stringDatabase.getString("CannotRedo.Text.Label")), null);
			localizedException.showException();
//			JOptionPane
//					.showMessageDialog(Utilities.getOwner(mainPanel), stringDatabase.getString("CannotRedo.Text.Label"),
//							stringDatabase.getString("ErrorWindow.Title.Label"), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * This method undoes or re-does an operation on the actual network.
	 *
	 * @param undoOperation - if true, an undo must be performed; if false, a redo will be
	 *                      performed.
	 * @throws CannotUndoException - if undo can't be performed.
	 * @throws CannotRedoException - if redo can't be performed.
	 */
	private void undoRedo(boolean undoOperation) throws CannotUndoException, CannotRedoException {
		NetworkPanel networkPanel = null;
		networkPanel = getCurrentNetworkPanel();
		if (undoOperation) {
			networkPanel.undo();
			networkPanel.repaint();
		} else {
			networkPanel.redo();
			networkPanel.repaint();
		}
	}

	/**
	 * This method activates an edition option for the current network.
	 *
	 * @param newEditionMode new edition mode to set.
	 */
	private void activateEditionMode(String newEditionMode) {
		NetworkPanel networkPanel = null;
		networkPanel = getCurrentNetworkPanel();
		networkPanel.setEditionMode(newEditionMode);
		mainPanel.getMainPanelMenuAssistant().setEditionOption(newEditionMode, networkPanel.isThereDataStored());
	}

	/**
	 * This method establishes the network working mode (edition or inference),
	 * by setting the opposite to the current one.
	 */
	private void setNewWorkingMode() {
		int currentWorkingMode = getCurrentNetworkPanel().getWorkingMode();
		int newWorkingMode;
		boolean performInference = true;
		if (currentWorkingMode == NetworkPanel.EDITION_WORKING_MODE) {
			newWorkingMode = NetworkPanel.INFERENCE_WORKING_MODE;

			// Show multicriteria dialog if the probnet has at least two criteria and have utility nodes
			InferenceOptionsDialog dialog = new InferenceOptionsDialog(getCurrentNetworkPanel().getProbNet(),
					Utilities.getOwner(mainPanel), MulticriteriaOptions.Type.UNICRITERION);

			if (dialog.getSelectedButton() == InferenceOptionsDialog.CANCEL_BUTTON) {
				newWorkingMode = NetworkPanel.EDITION_WORKING_MODE;
				performInference = false;
			}
			// Set as launched
			//getCurrentNetworkPanel().getProbNet().getInferenceOptions().setLaunchedBefore(performInference);

		} else {
			newWorkingMode = NetworkPanel.EDITION_WORKING_MODE;
		}
		mainPanel.setToolBarPanel(newWorkingMode);
		mainPanel.changeWorkingModeButton(newWorkingMode);
		if (getNetworkPanels().size() > 0) {
			getCurrentNetworkPanel().setWorkingMode(newWorkingMode);
		}
		getCurrentNetworkPanel().setSelectedAllObjects(false);
		mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkDependent(getCurrentNetworkPanel());

		if (performInference) {
			if (newWorkingMode == NetworkPanel.INFERENCE_WORKING_MODE) {

				getCurrentNetworkPanel().updateIndividualProbabilitiesAndUtilities();
				mainPanel.getInferenceToolBar().setCurrentEvidenceCaseName(getCurrentNetworkPanel().getCurrentCase());
			} else {
				// getCurrentNetworkPanel().removeAllFindings(); //Suppressed the elimination of findings on returning to Edition Mode
				//TODO: has the following piece of code sense with the task scenario?
				//TODO: review inferenceAlgorithm variable in EditorPanel, specially in removeNodeEvidenceInAllCases
				//if (getCurrentNetworkPanel().getInferenceAlgorithm() != null) {
				//    getCurrentNetworkPanel().setInferenceAlgorithm(null);
				//}
			}
		}
		getCurrentNetworkPanel().updateNodesExpansionState(newWorkingMode);
		mainPanel.adaptToolBarSize();
	}

	/**
	 * This method establishes the new expansion threshold of the network.
	 *
	 * @param newValue new value for expansion threshold
	 */
	private void setNewExpansionThreshold(Double newValue) {
		getCurrentNetworkPanel().setExpansionThreshold(newValue);
		getCurrentNetworkPanel().setSelectedAllNodes(false);
		mainPanel.getMainPanelMenuAssistant()
				.updateOptionsNewWorkingMode(NetworkPanel.INFERENCE_WORKING_MODE, getCurrentNetworkPanel());
		getCurrentNetworkPanel().updateNodesExpansionState(NetworkPanel.INFERENCE_WORKING_MODE);
	}

	/**
	 * This method responds to the navigation among the evidence cases option
	 * selected by the user.
	 *
	 * @param command the Action Command corresponding to the selected option
	 */
	private void evidenceCasesNavigationOption(String command) {
		if (command.equals("CREATE_NEW_EVIDENCE_CASE")) {
			getCurrentNetworkPanel().createNewEvidenceCase();
		} else if (command.equals("GO_TO_FIRST_EVIDENCE_CASE")) {
			getCurrentNetworkPanel().goToFirstEvidenceCase();
		} else if (command.equals("GO_TO_PREVIOUS_EVIDENCE_CASE")) {
			getCurrentNetworkPanel().goToPreviousEvidenceCase();
		} else if (command.equals("GO_TO_NEXT_EVIDENCE_CASE")) {
			getCurrentNetworkPanel().goToNextEvidenceCase();
		} else if (command.equals("GO_TO_LAST_EVIDENCE_CASE")) {
			getCurrentNetworkPanel().goToLastEvidenceCase();
		} else if (command.equals("CLEAR_OUT_ALL_EVIDENCE_CASES")) {
			getCurrentNetworkPanel().clearOutAllEvidenceCases();
		}
		mainPanel.getMainPanelMenuAssistant().updateOptionsEvidenceCasesNavigation(getCurrentNetworkPanel());
		mainPanel.getMainPanelMenuAssistant().updateOptionsPropagationTypeDependent(getCurrentNetworkPanel());
	}

	/**
	 * This method sets the inference options.
	 */
	private void setPropagationOptions() {
		getCurrentNetworkPanel().setInferenceOptions();
		mainPanel.getMainPanelMenuAssistant().updatePropagateEvidenceButton();
	}

	/**
	 * This method sets the multicriteria options
	 *
	 * @param networkPanel
	 */
	private void setInferenceOptions(NetworkPanel networkPanel) {
		InferenceOptionsDialog dialog = new InferenceOptionsDialog(networkPanel.getProbNet(),
				Utilities.getOwner(mainPanel), null);
		//MulticriteriaDialog dialog = new MulticriteriaDialog(networkPanel.getProbNet(), Utilities.getOwner(mainPanel));
	}

	/**
	 * Sets the mode of painting the nodes.
	 *
	 * @param byTitle if true, then the texts that appear into the nodes will be
	 *                their titles; if false, these texts will be their name.
	 */
	private void activateByTitle(boolean byTitle) {
		NetworkPanel actualNetwork = null;
		actualNetwork = getCurrentNetworkPanel();
		if (actualNetwork.getByTitle() != byTitle) {
			actualNetwork.setByTitle(byTitle);
			mainPanel.getMainPanelMenuAssistant().setByTitle(byTitle);
		}
	}

	/**
	 * This method restores (if minimized) and shows the message window.
	 */
	private void showMessageWindow() {
		if (!mainPanel.getMessageWindow().isVisible()) {
			mainPanel.getMdi().createNewFrame(mainPanel.getMessageWindow(), false);
			mainPanel.getMessageWindow().setVisible(true);
		} else {
			mainPanel.getMdi().selectFrame(mainPanel.getMessageWindow());
		}
	}

	/**
	 * This method increments the zoom of the current panel.
	 *
	 * @param frameContentPanel network whose zoom will be changed.
	 */
	private void incrementZoom(FrameContentPanel frameContentPanel) {
		setZoom(false, frameContentPanel, frameContentPanel.getZoom() + zoomChangeValue);
	}

	/**
	 * This method decrements the zoom of the current panel.
	 *
	 * @param frameContentPanel network whose zoom will be changed.
	 */
	private void decrementZoom(FrameContentPanel frameContentPanel) {
		setZoom(false, frameContentPanel, frameContentPanel.getZoom() - zoomChangeValue);
	}

	/**
	 * Sets the zoom of the current panel and updates the menu and the toolbar.
	 *
	 * @param dialogBox         if true, the parameter 'value' is ignored and this value is
	 *                          requested to user.
	 * @param frameContentPanel network whose zoom will be changed.
	 * @param value             new zoom value.
	 */
	private void setZoom(boolean dialogBox, FrameContentPanel frameContentPanel, double value) {
		double newZoom = 0.0;
		if (dialogBox) {
			requestZoomToUser(Utilities.getOwner(mainPanel), frameContentPanel);
		} else {
			frameContentPanel.setZoom(value);
		}
		newZoom = frameContentPanel.getZoom();
		mainPanel.getMainPanelMenuAssistant().setZoom(newZoom);
	}

	/**
	 * This method requests to the user a new value of zoom for the actual
	 * network.
	 *
	 * @param owner window that owns the dialog box.
	 */
	public void requestZoomToUser(Window owner, FrameContentPanel frameContentPanel) {
		SelectZoomDialog dialogZoom = new SelectZoomDialog(owner);
		if (dialogZoom.requestZoom(frameContentPanel.getZoom()) == SelectZoomDialog.OK_BUTTON) {
			frameContentPanel.setZoom(dialogZoom.getZoom());
		}
	}

	/**
	 * Returns current list of opened network panels
	 *
	 * @return current list of opened network panels
	 */
	public List<NetworkPanel> getNetworkPanels() {
		return networkPanels;
	}

	public void frameTitleChanged(FrameContentPanel contentPanel, String oldName, String newName) {
		// TODO Auto-generated method stub
	}

	public void frameOpened(FrameContentPanel contentPanel) {
		// TODO Auto-generated method stub
	}

	private void showDecisionTree(ProbNet probNet) {
		try {
			InferenceOptionsDialog costEffectivenessDialog = new InferenceOptionsDialog(probNet,
					Utilities.getOwner(mainPanel));
			if (costEffectivenessDialog.getSelectedButton() == InferenceOptionsDialog.CANCEL_BUTTON) {
				return;
			} else if (costEffectivenessDialog.getMulticriteriaOptions().getMulticriteriaType()
					== MulticriteriaOptions.Type.UNICRITERION){
				// Do something for show unicriterion decision tree
			} else if (costEffectivenessDialog.getMulticriteriaOptions().getMulticriteriaType()
					== MulticriteriaOptions.Type.COST_EFFECTIVENESS){
				// Do something for show cost-effectiveness decision tree
			}
			DecisionTreeWindow decisionTree = new DecisionTreeWindow(probNet);
			mainPanel.getMdi().createNewFrame(decisionTree);
			mainPanel.getMainPanelMenuAssistant().updateOptionsDecisionTree(decisionTree);
		} catch (OutOfMemoryError e) {
			LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
					"OutOfMemoryException", stringDatabase.getString("ExceptionNotEnoughMemory.Text.Label")), null);
			localizedException.showException();
//			JOptionPane.showMessageDialog(Utilities.getOwner(mainPanel),
//					stringDatabase.getString("ExceptionNotEnoughMemory.Text.Label"),
//					stringDatabase.getString("ExceptionNotEnoughMemory.Title.Label"), JOptionPane.ERROR_MESSAGE);
		}
	}

	private void showOptimalStrategy(NetworkPanel networkPanel) {
        /*
        22/10/2014
        Solving issue 195
        https://bitbucket.org/cisiad/org.openmarkov.issues/issue/195/exception-after-deleting-dan-node-and
        When the strategy was calculated for a network, then, if the network was modified, the strategy was not updated.
        The reason is that the algorithm, VariableEliminationDAN, was in 'postresolution' mode. Thus, the new strategy
        was not being calculated. Now, if the network is modified, we set the algorithm to null, as when
        the mode is changed from inference to edition.
         */
		if (networkPanel.getModified()) {
			//TODO: revise this piece of code under the new task paradigm
			//networkPanel.setInferenceAlgorithm(null);
		}

		ProbNet probNet = networkPanel.getProbNet();
		InferenceOptionsDialog costEffectivenessDialog = new InferenceOptionsDialog(probNet,
				Utilities.getOwner(mainPanel), MulticriteriaOptions.Type.UNICRITERION);
		if (costEffectivenessDialog.getSelectedButton() == InferenceOptionsDialog.CANCEL_BUTTON) {
			return;
		}
		if (networkPanel.getProbNet().getNetworkType().equals(DecisionAnalysisNetworkType.getUniqueInstance())) {
			DANEvaluation eval = null;
			try {
				eval = new DANDecompositionIntoSymmetricDANsEvaluation(probNet,networkPanel.getEditorPanel().getPreResolutionEvidence());
			} catch (NotEvaluableNetworkException e1) {
				JOptionPane.showMessageDialog(Utilities.getOwner(mainPanel),
						"An error occurred when trying to show the optimal strategy: " + e1.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
			StrategyTree strategyTree = null;
			strategyTree = eval.getUtility().strategyTrees[0];

			try {
				//OptimalStrategyDialog optimalStrategyDialog = new OptimalStrategyDialog(Utilities.getOwner(mainPanel), probNet, inferenceAlgorithm);
				strategyTree.pruneAndGraftNode("OD");
				OptimalStrategyDialog optimalStrategyDialog = new OptimalStrategyDialog(Utilities.getOwner(mainPanel),
						probNet, strategyTree);
				optimalStrategyDialog.setVisible(true);
			} catch (IncompatibleEvidenceException e) {
                LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
                        "IncompatibleEvidenceException", "An error occurred when trying to show the optimal strategy"), null);
                localizedException.showException();
            } catch (UnexpectedInferenceException e) {
                LocalizedException localizedException = new LocalizedException(new OpenMarkovException(
                        "IncompatibleEvidenceException", "An error occurred when trying to show the optimal strategy"), null);
                localizedException.showException();
			}

			// MID or ID
		} else {

			VEOptimalIntervention veOptimalStrategy = null;
			try {
				veOptimalStrategy = new VEOptimalIntervention(probNet,
						networkPanel.getEditorPanel().getPreResolutionEvidence());
			} catch (NotEvaluableNetworkException | IncompatibleEvidenceException e) {
				JOptionPane.showMessageDialog(Utilities.getOwner(mainPanel),
						"An error occurred when trying to show the optimal strategy: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}

			try {
				//OptimalStrategyDialog optimalStrategyDialog = new OptimalStrategyDialog(Utilities.getOwner(mainPanel), probNet, inferenceAlgorithm);
				OptimalStrategyDialog optimalStrategyDialog = new OptimalStrategyDialog(Utilities.getOwner(mainPanel),
						probNet, veOptimalStrategy);
				optimalStrategyDialog.setVisible(true);
			} catch (IncompatibleEvidenceException | UnexpectedInferenceException e) {
				JOptionPane.showMessageDialog(Utilities.getOwner(mainPanel),
						"An error occurred when trying to show the optimal strategy", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	/**
	 * @param buffer    <code>StringBuffer</code>
	 * @param mainPanel <code>MainPanel</code>
	 */
	private void showTextWindow(StringBuilder buffer, MainPanel mainPanel) {
		JFrame frame = new JFrame("Cost-Effectiveness analysis");
		String text = buffer.toString();
		JTextArea textArea = new JTextArea(40, getMaxCharsInALine(text));
		frame.getContentPane().add(textArea, BorderLayout.CENTER);
		JScrollPane scroll = new JScrollPane(textArea);
		frame.getContentPane().add(scroll, BorderLayout.CENTER);
		textArea.setText(text);
		frame.setLocationRelativeTo(mainPanel);
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * @param text <code>String</code>
	 * @return <code>int</code>
	 */
	private int getMaxCharsInALine(String text) {
		int maxLengthLine = 0;
		if (text != null) {
			int position = 0;
			int nextEndLine;
			int textLength = text.length();
			do {
				nextEndLine = text.indexOf('\n', position);
				if (nextEndLine > 0) {
					int lengthLine = nextEndLine - position;
					if (lengthLine > maxLengthLine) {
						maxLengthLine = lengthLine;
					}
					position = nextEndLine + 1;
				}
			} while (nextEndLine != -1 && position < textLength);
		}
		return maxLengthLine;
	}

	public void componentResized(ComponentEvent e) {
		mainPanel.adaptToolBarSize();
	}

	@Override public void componentMoved(ComponentEvent e) {

	}

	@Override public void componentShown(ComponentEvent e) {

	}

	@Override public void componentHidden(ComponentEvent e) {

	}

}
