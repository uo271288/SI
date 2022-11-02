/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.stochasticPropagationOutput;


import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.*;

import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.gui.configuration.OpenMarkovPreferences;
import org.openmarkov.gui.localize.LocalizedException;
import org.openmarkov.gui.plugin.ToolPlugin;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.gui.localize.StringDatabase;
import org.openmarkov.inference.huginPropagation.ClusterPropagation;
import org.openmarkov.inference.huginPropagation.HuginPropagation;
import org.openmarkov.inference.likelihoodWeighting.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;

/**
 * Creates the plugin dialog for exporting stochastic propagation data.
 *
 * @author iagoparis - spring 2018
 */
@ToolPlugin(name = "StochasticPropagationOutput", command = "Tools.StochasticPropagationOutput")
public class StochasticPropagationOutputFrame extends JDialog implements ActionListener {

    private int DEFAULT_SAMPLES = 10000;
    protected ProbNet probNet;
    private EvidenceCase preResolutionEvidence;
    private EvidenceCase postResolutionEvidence;
    String algorithmName;

    // Gui components
    private ButtonGroup algorithms;
    private JTextField sampleSizeNumber;

    /**
     * Localized stringDatabase
     */
    StringDatabase stringDatabase = StringDatabase.getUniqueInstance();

    /**
     * The constructor creates the dialog's window.
     *
     * @param owner window that owns the dialog.
     */
    public StochasticPropagationOutputFrame(JFrame owner) {
        super(owner);
        this.setTitle(stringDatabase.getString("StochasticPropagationOutput.Window"));

        // Get data from the net
        try {
            probNet = MainPanel.getUniqueInstance().getMainPanelListenerAssistant().getCurrentNetworkPanel()
                    .getProbNet();
            preResolutionEvidence = MainPanel.getUniqueInstance().
                    getMainPanelMenuAssistant().getCurrentNetworkPanel().getEditorPanel().getPreResolutionEvidence();
            postResolutionEvidence = MainPanel.getUniqueInstance().
                    getMainPanelMenuAssistant().getCurrentNetworkPanel().getEditorPanel().getCurrentEvidenceCase();
            // Stop if other than bayesian net
            if (!(probNet.getNetworkType() instanceof BayesianNetworkType)) {
                LocalizedException InvalidNetTypeException =
                        new LocalizedException(new OpenMarkovException("Exception.InvalidNetType"), this);
                InvalidNetTypeException.showException();
                return;
            }
            // Stop if no nodes in the net
            if (probNet.getNumNodes() == 0) {
                LocalizedException EmptyNetException =
                        new LocalizedException(new OpenMarkovException("Exception.EmptyNet"), this);
                EmptyNetException.showException();
            }
        // Stop if there is no net opened yet
        } catch (NullPointerException e) {
            // The string is the token that refers to the string database
            LocalizedException noNetException = new LocalizedException(new OpenMarkovException("Exception.NoNet"), this);
            noNetException.showException();
            return;
        }

        // Window basics
        JPanel content = new JPanel();
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        content.setBorder(padding);
        this.setContentPane(content);
        this.setLocation(300, 300);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.setVisible(true);


        /*"********
         * Dialog *
         **********/
        GridBagLayout mainLayout = new GridBagLayout();
        content.setLayout(mainLayout);
        GridBagConstraints constraints = new GridBagConstraints();

        // Algorithm buttons
        algorithms = new ButtonGroup(); // The round checkboxes that allow only one option enabled.
        JPanel algorithmsPanel = new JPanel();
        algorithmsPanel.setBorder(BorderFactory.createTitledBorder(stringDatabase.getString("Dialog.Algorithms")));
        algorithmsPanel.setLayout(new GridLayout(2, 1, 1, 1));


        JRadioButton logicSampling = new JRadioButton(stringDatabase.getString("Algorithms.LogicSampling"));
        logicSampling.setActionCommand(stringDatabase.getString("Algorithms.LogicSampling"));
        algorithms.add(logicSampling);
        algorithmsPanel.add(logicSampling);

        JRadioButton likelihoodWeighting = new JRadioButton(stringDatabase.
                getString("Algorithms.LikelihoodWeighting"));
        likelihoodWeighting.setActionCommand(stringDatabase.getString("Algorithms.LikelihoodWeighting"));
        algorithms.add(likelihoodWeighting);
        algorithmsPanel.add(likelihoodWeighting);



        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weighty = 1;
        content.add(algorithmsPanel, constraints);

        likelihoodWeighting.setSelected(true); // Likelihood weighting by default


        // Sample text field
        JPanel sampleSize = new JPanel();
        JLabel NumberOfSamples = new JLabel(stringDatabase.getString("Dialog.NSamples"));
        sampleSize.add(NumberOfSamples);
        sampleSizeNumber = new JTextField(Integer.toString(DEFAULT_SAMPLES), 6);
        sampleSize.add(sampleSizeNumber);
        sampleSize.setAlignmentX(Component.LEFT_ALIGNMENT);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weighty = 0.5;
        content.add(sampleSize, constraints);

        // Save to xlsx button
        JButton xlsxOutput = new JButton(stringDatabase.getString("Dialog.PrintXlsx"));
        xlsxOutput.addActionListener(this); // Only component listened

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weighty = 2;
        content.add(xlsxOutput, constraints);

        this.pack();  // Sizes window to preferred size of contents.
    }

    // Listener for the dialog events. It only listens to the xlsx button
    // and retrieves data from attributes of the dialog.
    @Override
    public void actionPerformed(ActionEvent actionEvent) {

        ButtonModel algorithmButton = algorithms.getSelection();
        ClusterPropagation exactAlgorithm;
        algorithmName = algorithmButton.getActionCommand();

        // Create the selected algorithms
        StochasticPropagation algorithm;
        try {
            if (algorithmName.equals(stringDatabase.getString("Algorithms.LikelihoodWeighting"))) {
                algorithm = new LikelihoodWeighting(probNet);
            } else if (algorithmName.equals(stringDatabase.getString("Algorithms.LogicSampling"))) {
                algorithm = new LogicSampling(probNet);
            } else { // Never happens
                LocalizedException NoAlgorithmException =
                        new LocalizedException(new OpenMarkovException("Exception.NoAlgorithm"), this);
                NoAlgorithmException.showException();
                return;
            }
            exactAlgorithm = new HuginPropagation(probNet);

        } catch (NotEvaluableNetworkException e) {
            LocalizedException InvalidNetException =
                    new LocalizedException(new OpenMarkovException("Exception.InvalidNet"), this);
            InvalidNetException.showException();
            return;
        } catch (NullPointerException e) { // Node without potential
            // The e.getMessage retrieves the variable without potential
            LocalizedException NodeWithoutPotentialException =
                    new LocalizedException(new OpenMarkovException("Exception.NodeWithoutPotential", e.getMessage()), this);
            NodeWithoutPotentialException.showException();
            return;
        }

        // Prepare the exact algorithm
        exactAlgorithm.setStorageLevel(ClusterPropagation.StorageLevel.MEDIUM);
        exactAlgorithm.compilePriorPotentials();

        // Set the evidence in the algorithms
        try {
            algorithm.setPreResolutionEvidence(preResolutionEvidence);
        } catch (IncompatibleEvidenceException e) {
            // Do nothing since the implementation of this method for this object don't throw that
        }
        algorithm.setPostResolutionEvidence(postResolutionEvidence);
        try {
            exactAlgorithm.setPreResolutionEvidence(preResolutionEvidence);
        } catch (IncompatibleEvidenceException e) {
            LocalizedException prePostEvidenceConflictException =
                    new LocalizedException(new OpenMarkovException("Exception.PrePostEvidenceConflict"), this);
            prePostEvidenceConflictException.showException();
            return;
        }
        exactAlgorithm.setPostResolutionEvidence(postResolutionEvidence);

        // Set the sample size from the GUI text field into the stochastic algorithm
        int sampleSize;
        try {
            sampleSize = Integer.parseInt(sampleSizeNumber.getText());
            if (sampleSize < 0) {
                throw new NumberFormatException(); //
            }
            algorithm.setSampleSize(sampleSize);

        } catch (NumberFormatException e) {
            LocalizedException WrongSamplesException =
                    new LocalizedException(new OpenMarkovException("Exception.WrongSamples"), this);
            WrongSamplesException.showException();
            return;
        }

        // Set the stochastic algorithm to store the samples
        algorithm.setStoringSamples(true);

        // Propagate
        HashMap<Variable, TablePotential> exactPosteriorValues;
        try {
            algorithm.getPosteriorValues();
            exactPosteriorValues = exactAlgorithm.getPosteriorValues();
        } catch (IncompatibleEvidenceException e) {
            LocalizedException InvalidEvidenceException =
                    new LocalizedException(new OpenMarkovException("Exception.InvalidEvidence"), this);
            InvalidEvidenceException.showException();
            // This error only happens when a normalization fails (either every value in a potential is 0 or every
            // sample has been weighted 0.
            return;
        }

        // Name for the spreadsheet created.
        String fileName;

        /* Save dialog */
        File lastDirectoryUsed = new File(OpenMarkovPreferences
                .get(OpenMarkovPreferences.LAST_OPEN_DIRECTORY, OpenMarkovPreferences.OPENMARKOV_DIRECTORIES, "."));

        JFileChooser fileChooser = new JFileChooser(lastDirectoryUsed){ // Set starting directory
            // Modify the fileChooser class on creation to ask for overwrite confirmation
            @Override
            public void approveSelection(){
                File f = getSelectedFile();
                if(f.exists() && getDialogType() == SAVE_DIALOG){
                    int result = JOptionPane.showConfirmDialog(this, stringDatabase.getString("Warnings.Overwrite"),
                            stringDatabase.getString("Warnings.OverwriteTitle"),JOptionPane.YES_NO_CANCEL_OPTION);
                    switch(result){
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }
        };

        // Choose where to save the file

        fileChooser.setSelectedFile(new File(formatCleaner(probNet.getName()) + " - "
                + algorithmName + ".xlsx"));
        fileChooser.setDialogTitle(stringDatabase.getString("SaveDialog.Title"));
        int optionChosen = fileChooser.showSaveDialog(this);
        if (optionChosen == JFileChooser.APPROVE_OPTION) {
            fileName = fileChooser.getSelectedFile().getAbsolutePath();
            if (!fileName.endsWith(".xlsx")) {
                fileName += ".xlsx";
            }
            dispose(); // Closes the dialog
        } else {
            return; // If x button or cancel, close dialog and don't do anything more.
        }
        // Write to xlsx
        Thread writingThread = new Thread(new XlsxWrite(this, algorithm, exactPosteriorValues, fileName));
        writingThread.start();



    } // end of actionPerformed




    // Given a file name. Deletes the (4-1) characters after and last dot (usually the format)
    private String formatCleaner(String name) {

        if (name.contains(".")) {

            int lastPointPosition;
            String restOfName = name;

            do {
                lastPointPosition = restOfName.indexOf(".");
                restOfName = restOfName.substring(lastPointPosition + 1, restOfName.length());
            } while (restOfName.contains("."));

            if (restOfName.length() <= 4 && restOfName.length() > 0) {
                return name.substring(0, name.length() - (restOfName.length() + 1)); // One for the point
            } else {
                return name;
            }
        } else {
            return name;
        }
    }
}
