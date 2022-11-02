/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.inference.temporalevolution;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.inference.tasks.TemporalEvolution;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.gui.localize.StringDatabase;
import org.openmarkov.inference.variableElimination.tasks.VETemporalEvolution;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Plot of temporal evolution of variables in CEA
 *
 * @author myebra
 */
@SuppressWarnings("serial") public class TraceTemporalEvolutionDialog extends JDialog {
	private final Dimension legendsDimension = new Dimension(200, 450);
	private Map<Variable, TablePotential> temporalEvolution;
	private ChartPanel chartPanel;
	private TemporalEvolutionTablePane tablePane;
	private JTabbedPane tabbedPane;
	private Variable variableOfInterest;
	private ProbNet expandedNetwork;
	private boolean isUtility;
	private boolean isCumulative;
	private int numSlices;
	private StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
	private List<Variable> conditioningVariables;
	private JScrollPane checkBoxPanel;
	private List<XYSeries> arrayXYSeries;
	private List<JCheckBox> jcheckBoxList;
	private JPanel chartPanelWithCheckBox;
	private int numberOfCombinations;
	private JRadioButton radioIndividual;
	private JRadioButton radioSummatory;
	private JRadioButton radioInstantaneus;
	private JRadioButton radioAccumulate;
	private boolean isIndividual;
	private JFreeChart chart;
	private Node node;
	private List<JLabel> legendLabels;
	private JScrollPane legendPanel;

	public TraceTemporalEvolutionDialog(Window owner, Node node, EvidenceCase evidence) {
		this(owner, node, evidence, null);

	}

	public TraceTemporalEvolutionDialog(Window owner, Node node, EvidenceCase evidence, Variable decisionSelected) {
		super(owner);
		this.node = node;
		ProbNet probNet = node.getProbNet();
		isIndividual = true;

		// Check if all decision nodes have an imposed policy,
		// potential set in node, the nodes without an imposed policy will be added to
		// conditioningVariables
		conditioningVariables = new ArrayList<Variable>();
		if (decisionSelected != null) {
			conditioningVariables.add(decisionSelected);
		}

		this.isUtility = node.getNodeType() == NodeType.UTILITY;

		try {
			numSlices = probNet.getInferenceOptions().getTemporalOptions().getHorizon();

			this.variableOfInterest = node.getVariable();

			TemporalEvolution veTemporalEvolution = new VETemporalEvolution(probNet, node.getVariable());
			veTemporalEvolution.setPreResolutionEvidence(evidence);
			veTemporalEvolution.setDecisionVariable(decisionSelected);
			this.temporalEvolution = veTemporalEvolution.getTemporalEvolution();
			this.expandedNetwork = veTemporalEvolution.getExpandedNetwork();

			initialize();
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Dimension screenSize = toolkit.getScreenSize();
			Rectangle bounds = owner.getBounds();
			int width = screenSize.width / 2;
			int height = screenSize.height / 2;
			// center point of the owner window
			int x = bounds.x / 2 - width / 2;
			int y = bounds.y / 2 - height / 2;
			this.setBounds(x, y, width, height);
			setMinimumSize(new Dimension(width, height / 2));
			setLocationRelativeTo(owner);
			setResizable(true);
			repaint();
			pack();
			setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(owner, stringDatabase.getString("GenericError.Text"), "Error",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	private void createExcel(ProbNet probNet, EvidenceCase evidence, Variable decisionSelected) {

		JFileChooser fileChooser = new JFileChooser();
		String netName = probNet.getName();
		fileChooser.setSelectedFile(new File(netName + "-temporal_evolution.xlsx"));
		if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

			// This is like an if-else:
			// condition? run if true : run if false;
			String targetFilename = fileChooser.getSelectedFile().getAbsolutePath().endsWith(".xlsx") ?
					fileChooser.getSelectedFile().getAbsolutePath() :
					fileChooser.getSelectedFile().getAbsolutePath() + ".xlsx";

			List<Variable> temporalVariables = new ArrayList<>();
			for (Variable variable : probNet.getVariables()) {
				if (variable.isTemporal()) {
					if (variable.getVariableType().equals(VariableType.NUMERIC) && !probNet.getNode(variable)
							.getNodeType().equals(NodeType.UTILITY)) {
						continue;
					}

					boolean addedOtherSlice = false;
					for (int i = 0; i < temporalVariables.size(); i++) {
						if (temporalVariables.get(i).getBaseName().equals(variable.getBaseName())) {
							addedOtherSlice = true;
						}
					}
					if (!addedOtherSlice) {
						temporalVariables.add(variable);
					}
				}
			}

			HashMap<Variable, JTable> datasheet = new HashMap<>();
			for (Variable temporalVariable : temporalVariables) {
				System.out.println(temporalVariable.getBaseName());
				try {
					TemporalEvolution veTemporalEvolution = new VETemporalEvolution(probNet, temporalVariable);
					veTemporalEvolution.setPreResolutionEvidence(evidence);
					veTemporalEvolution.setDecisionVariable(decisionSelected);
					HashMap<Variable, TablePotential> result = veTemporalEvolution.getTemporalEvolution();
					JTable table = createJTable(temporalVariable, result);
					TemporalEvolutionReport report = new TemporalEvolutionReport();
					report.write(
							targetFilename.substring(0, targetFilename.length() - 5) + temporalVariable.getBaseName()
									+ ".xlsx", table);
					datasheet.put(temporalVariable, table);
				} catch (NotEvaluableNetworkException e) {
					e.printStackTrace();
				} catch (IncompatibleEvidenceException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (UnexpectedInferenceException e) {
					e.printStackTrace();
				}
			}

			XSSFWorkbook hwb = new XSSFWorkbook();
			for (Variable tabVariable : datasheet.keySet()) {
				JTable jtable = datasheet.get(tabVariable);
				XSSFSheet sheetTable = hwb.createSheet(tabVariable.getBaseName());
				// first row, column names
				Row rowIndexes = sheetTable.createRow(0);
				rowIndexes.createCell(0).setCellValue("");

				for (int i = 1; i < jtable.getColumnCount(); i++) {
					rowIndexes.createCell(i + 1)
							.setCellValue(jtable.getColumnModel().getColumn(i).getHeaderValue().toString());
				}
				// fill data
				for (int i = 0; i < jtable.getRowCount(); i++) {
					Row row = sheetTable.createRow(i + 1);
					for (int j = 0; j < jtable.getColumnCount(); j++) {
						if (jtable.getValueAt(i, j) instanceof String) {
							row.createCell(j).setCellValue((String) jtable.getValueAt(i, j));
						} else if (jtable.getValueAt(i, j) instanceof Integer) {
							row.createCell(j).setCellValue((Integer) jtable.getValueAt(i, j));
						} else {
							row.createCell(j).setCellValue((Double) jtable.getValueAt(i, j));
						}
					}

				}
			}

			FileOutputStream fileOut = null;
			try {
				fileOut = new FileOutputStream(targetFilename);
				hwb.write(fileOut);
				fileOut.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private JTable createJTable(Variable temporalVariable, HashMap<Variable, TablePotential> result) {
		int numRows = this.numSlices + conditioningVariables.size() + 2;
		int numColumns = temporalVariable.getNumStates();
		for (Variable conditioningVariable : conditioningVariables) {
			numColumns *= conditioningVariable.getNumStates();
		}
		numColumns += 1;

		JTable jtable = new JTable(numRows, numColumns);

		int row = 0;
		// Build conditioning variables names
		for (; row < conditioningVariables.size(); row++) {
			jtable.setValueAt(conditioningVariables.get(row).getBaseName(), row, 0);
		}

		//Build states
		jtable.setValueAt("States", row, 0);
		row++;

		// Build slices column
		int slice = 0;
		for (; row < numRows; row++) {
			jtable.setValueAt(slice, row, 0);
			slice++;
		}

		// Fill conditioning variables states
		row = 0;
		for (Variable conditioningVariable : conditioningVariables) {
			// Build headers
			for (int column = 1; column < numColumns; column++) {
				String stateName = conditioningVariable.getStateName((column - 1) / temporalVariable.getNumStates());
				jtable.setValueAt(stateName, row, column);
			}
			row++;
		}

		// Fill states of the temporal variable
		for (int column = 1; column < numColumns; column++) {
			String stateName = temporalVariable.getStateName((column - 1) % temporalVariable.getNumStates());
			jtable.setValueAt(stateName, row, column);
		}

		// Fill table potentials
		int slice0row = conditioningVariables.size() + 1;

		for (Variable variable : result.keySet()) {
			TablePotential tablePotential = result.get(variable);
			int rowVariable = variable.getTimeSlice() + slice0row;
			for (int column = 1; column < numColumns; column++) {
				jtable.setValueAt(tablePotential.getValues()[(column - 1) % temporalVariable.getNumStates()],
						rowVariable, column);
			}
		}

		return jtable;
	}

	private void initialize() {
		setTitle(stringDatabase.getString("TemporalEvolutionResultDialog.Title.Label") + " " + variableOfInterest
				.getBaseName());
		setContentPane(getJContentPane());
		pack();
	}

	/**
	 * This method initialises jContentPane.
	 *
	 * @return a new content panel.
	 */
	private JPanel getJContentPane() {
		JPanel jContentPane = new JPanel();
		jContentPane.setLayout(new BorderLayout());
		jContentPane.add(getComponentsPanel(), BorderLayout.CENTER);
		jContentPane.add(getBottomPanel(), BorderLayout.SOUTH);
		return jContentPane;
	}

	/**
	 * Get the bottom panel with buttons
	 * @return The bottom panel
	 */
	private JPanel getBottomPanel() {
		JPanel buttonsPanel = new JPanel();
		JButton jButtonSaveReport = new JButton();
		jButtonSaveReport.setName("jButtonSaveReport");
		jButtonSaveReport.setText(stringDatabase.getString("Dialog.SaveReport.Label"));
		jButtonSaveReport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveReport();
			}
		});
		buttonsPanel.add(jButtonSaveReport);
		JButton jButtonClose = new JButton();
		jButtonClose.setName("jButtonClose");
		jButtonClose.setText(stringDatabase.getString("Dialog.Close.Label"));
		jButtonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		buttonsPanel.add(jButtonClose);
		return buttonsPanel;
	}

	/**
	 * Gets the components panel
	 * @return The components panel
	 */
	private Component getComponentsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 5));
		panel.setMaximumSize(new Dimension(180, 40));
		panel.add(getTabbedPane());
		pack();
		return panel;
	}

	/**
	 * This method initialises tabbedPane.
	 *
	 * @return a new tabbed pane.
	 */
	protected JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane();
			tabbedPane.setName("TraceTemporalEvolutionTabbedPane");
			tabbedPane.addTab(stringDatabase.getString("TemporalEvolutionChart.Title.Label"), null,
					getChartsPanelWithCheckBoxes(), null);
			tabbedPane
					.addTab(stringDatabase.getString("TemporalEvolutionTable.Title.Label"), null, getTablePane(), null);
		}
		return tabbedPane;
	}

	/**
	 * Gets the ChartPanel with the control area
	 *
	 * @return JPanel with the ChartPanel and the Control Area
	 */
	private JPanel getChartsPanelWithCheckBoxes() {
		if (chartPanelWithCheckBox == null) {
			chartPanelWithCheckBox = new JPanel();
			chartPanelWithCheckBox.setLayout(new BorderLayout());
			chartPanelWithCheckBox.add(getChartsPanel(createDataset()), BorderLayout.CENTER);
			chartPanelWithCheckBox.add(getChartOptionsPanel(), BorderLayout.LINE_START);
			getLegendsPanel();
		}
		return chartPanelWithCheckBox;
	}

	/**
	 * Gets the Legends Panel and set it to the chartPanelWithCheckBoxes
	 */
	private void getLegendsPanel() {
		if (legendPanel != null) {
			chartPanelWithCheckBox.remove(legendPanel);
		}
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		TitledBorder border = new TitledBorder(
				stringDatabase.getString("TemporalEvolutionResultDialog.Legend.Title.Label"));

		panel.setBorder(border);
		panel.setBackground(Color.WHITE);

		for (JLabel legend : legendLabels) {
			panel.add(legend);
		}

		legendPanel = new JScrollPane(panel);
		legendPanel.setPreferredSize(legendsDimension);
		legendPanel.setMinimumSize(legendsDimension);

		legendPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		legendPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		if ((conditioningVariables != null && conditioningVariables.size() >= 1) || (!isUtility && isIndividual)) {
			chartPanelWithCheckBox.add(legendPanel, BorderLayout.LINE_END);
		}

	}

	/**
	 * Gets the Charts Panel with the JFreeChart
	 *
	 * @param dataset Dataset for the JFreeChart
	 * @return ChartsPanel
	 */
	private ChartPanel getChartsPanel(XYDataset dataset) {
		chart = ChartFactory.createXYLineChart("", "t", "value", dataset, PlotOrientation.VERTICAL, true, true, true);

		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		for (int i = 0; i < dataset.getSeriesCount(); i++) {
			renderer.setSeriesLinesVisible(i, true);
			renderer.setSeriesShapesVisible(i, true);
		}

		chart.getXYPlot().setRenderer(renderer);
		chart.getXYPlot().setDomainGridlinesVisible(true);
		chart.getXYPlot().setRangeGridlinesVisible(true);
		chart.getXYPlot().setDomainGridlinePaint(Color.darkGray);
		chart.getXYPlot().setRangeGridlinePaint(Color.darkGray);

		// Create the custom legend in an adjoined panel
		getLegendTitle();

		// Hide the default legend
		for (int i = 0; i < chart.getSubtitleCount(); i++) {
			chart.getSubtitle(i).setVisible(false);
		}

		chartPanel = new ChartPanel(chart);
		chartPanel.setAutoscrolls(true);
		chartPanel.setDisplayToolTips(true);
		chartPanel.setMouseZoomable(true);

		XYToolTipGenerator generator = new StandardXYToolTipGenerator("{0}: ({1}, {2})", new DecimalFormat("0.00"),
				new DecimalFormat("0.00"));
		renderer.setBaseToolTipGenerator(generator);

		return chartPanel;
	}

	private JPanel getChartOptionsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(getDisplayTypePanel(), BorderLayout.PAGE_START);
		panel.add(getCheckBoxesPanel(), BorderLayout.CENTER);

		if (variableOfInterest.getNumStates() <= 1 || isUtility) {
			getCheckBoxesPanel().setVisible(false);
		}
		panel.setPreferredSize(new Dimension(150, 450));
		return panel;
	}

	/**
	 * Control panel with checkBox controls
	 * @return The check boxes panel
	 */
	private JScrollPane getCheckBoxesPanel() {
		if (checkBoxPanel == null) {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

			jcheckBoxList = new ArrayList<JCheckBox>();

			// Add a checkbox for each state in the variable
			for (State state : variableOfInterest.getStates()) {
				JCheckBox checkBox = new JCheckBox(state.getName());
				checkBox.addActionListener(new ActionListener() {

					@Override public void actionPerformed(ActionEvent e) {
						checkBoxChanged();
					}
				});
				checkBox.setSelected(true);
				jcheckBoxList.add(checkBox);
				panel.add(checkBox);
			}

			panel.setBorder(new TitledBorder(stringDatabase.getString("TemporalEvolutionResultDialog.States.Label")));

			checkBoxPanel = new JScrollPane(panel);
			checkBoxPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			checkBoxPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			checkBoxPanel.setPreferredSize(new Dimension(150, 250));
			checkBoxPanel.setBorder(null);
		}
		return checkBoxPanel;
	}

	/**
	 * Panel with the radio button control that changes the display type
	 * @return The display type panel
	 */
	private JPanel getDisplayTypePanel() {
		JPanel displayType = new JPanel();
		displayType
				.setBorder(new TitledBorder(stringDatabase.getString("TemporalEvolutionResultDialog.Display.Label")));
		displayType.setPreferredSize(new Dimension(150, 150));
		displayType.setLayout(new BoxLayout(displayType, BoxLayout.PAGE_AXIS));

		// Create the custom radio button group. If we have an utility node
		// we can choose between "instaneus/accumulate" in other case
		// we can choose between "individual/summatory".
		ButtonGroup radioButtonGroup = new ButtonGroup();
		if (isUtility) {
			radioInstantaneus = new JRadioButton(
					stringDatabase.getString("TemporalEvolutionResultDialog.Display.Instantaneus"));
			radioInstantaneus.addActionListener(new ActionListener() {

				@Override public void actionPerformed(ActionEvent e) {
					radioButtonChanged(e);
				}
			});

			radioAccumulate = new JRadioButton(
					stringDatabase.getString("TemporalEvolutionResultDialog.Display.Accumulate"));
			radioAccumulate.addActionListener(new ActionListener() {

				@Override public void actionPerformed(ActionEvent e) {
					radioButtonChanged(e);
				}
			});

			radioButtonGroup.add(radioInstantaneus);
			radioButtonGroup.add(radioAccumulate);
			radioInstantaneus.setSelected(true);
			isCumulative = false;

			displayType.add(radioInstantaneus);
			displayType.add(radioAccumulate);
		} else {

			radioIndividual = new JRadioButton(
					stringDatabase.getString("TemporalEvolutionResultDialog.Display.Individual"));
			radioIndividual.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					radioButtonChanged(e);
				}
			});

			radioSummatory = new JRadioButton(
					stringDatabase.getString("TemporalEvolutionResultDialog.Display.Summatory"));
			radioSummatory.addActionListener(new ActionListener() {

				@Override public void actionPerformed(ActionEvent e) {
					radioButtonChanged(e);
				}
			});

			radioButtonGroup.add(radioIndividual);
			radioButtonGroup.add(radioSummatory);
			radioIndividual.setSelected(true);
			isIndividual = true;

			displayType.add(radioIndividual);
			displayType.add(radioSummatory);
		}
		return displayType;
	}

	/**
	 * Action that trigger a change in a radio button.
	 *
	 * @param e
	 */
	private void radioButtonChanged(ActionEvent e) {
		if (e.getSource().equals(radioIndividual)) {
			if (!isIndividual) {
				isIndividual = true;
				// Uses the same method as check box action performed.
				checkBoxChanged();
			}
		} else if (e.getSource().equals(radioSummatory)) {
			if (isIndividual) {
				isIndividual = false;
				checkBoxChanged();
			}
		} else if (e.getSource().equals(radioInstantaneus)) {
			if (isCumulative) {
				isCumulative = false;
				tabbedPane.removeTabAt(1);
				tabbedPane.addTab(stringDatabase.getString("TemporalEvolutionTable.Title.Label"), null, getTablePane(),
						null);
				checkBoxChanged();
			}
		} else if (e.getSource().equals(radioAccumulate)) {
			if (!isCumulative) {
				isCumulative = true;
				tabbedPane.removeTabAt(1);
				tabbedPane.addTab(stringDatabase.getString("TemporalEvolutionTable.Title.Label"), null, getTablePane(),
						null);
				checkBoxChanged();
			}
		}
	}

	/**
	 * Method triggered by a change in a checkbox.
	 */
	protected void checkBoxChanged() {
		boolean[] markedCheckBoxes = new boolean[jcheckBoxList.size()];
		for (int i = 0; i < jcheckBoxList.size(); i++) {
			if (jcheckBoxList.get(i).isSelected()) {
				markedCheckBoxes[i] = true;
			} else {
				markedCheckBoxes[i] = false;
			}
		}
		// Update the visual info and repaint
		if (isUtility) {
			showUtilitySeries();
		} else {
			showChartSeriesWithFilter(markedCheckBoxes);
		}
	}

	/**
	 * Allows to update the info in the chart. With the filter, the painted
	 * series will change
	 *
	 * @param markedCheckBoxes
	 */
	private void showChartSeriesWithFilter(boolean[] markedCheckBoxes) {
		XYSeriesCollection result = new XYSeriesCollection();

		boolean someCheckBoxMarked = false;
		for (int j = 0; j < markedCheckBoxes.length; j++) {
			if (markedCheckBoxes[j]) {
				someCheckBoxMarked = true;
			}
		}

		if (arrayXYSeries == null) {
			createSeries();
		}

		if (isIndividual) {
			if (someCheckBoxMarked) {
				for (int i = 0; i < numberOfCombinations / variableOfInterest.getNumStates(); i++) {
					for (int j = 0; j < markedCheckBoxes.length; j++) {
						if (markedCheckBoxes[j]) {
							result.addSeries(arrayXYSeries.get(i * variableOfInterest.getNumStates() + j));
						}
					}
				}
			}
		} else {
			if (someCheckBoxMarked) {
				for (int i = 0; i < numberOfCombinations / variableOfInterest.getNumStates(); i++) {
					ArrayList<XYSeries> seriesToBeAdded = new ArrayList<XYSeries>();
					for (int j = 0; j < markedCheckBoxes.length; j++) {
						if (markedCheckBoxes[j]) {
							seriesToBeAdded.add(arrayXYSeries.get(i * variableOfInterest.getNumStates() + j));
						}
					}
					result.addSeries(sumSeries(seriesToBeAdded));
				}
			}
		}

		// Remove the old Chart, calculate the new Chart and Add the new Chart. Then repaint to update the GUI
		chartPanelWithCheckBox.remove(chartPanel);
		chartPanel = getChartsPanel(result);
		chartPanelWithCheckBox.add(chartPanel, BorderLayout.CENTER);
		this.repaint();

	}

	/**
	 * Method that updates utility series. Really similar to "showChartSeriesWithFilter", but in this
	 * case, the checkboxes are irrelevant and we must know if the display is cumulative or individual
	 */
	private void showUtilitySeries() {
		XYSeriesCollection result = new XYSeriesCollection();

		if (arrayXYSeries == null) {
			createSeries();
		}

		if (isCumulative) {
			for (int i = 0; i < arrayXYSeries.size(); i++) {
				String nameOfSerie = (String) arrayXYSeries.get(i).getKey();
				XYSeries serie = new XYSeries(nameOfSerie);
				double value = 0.0;
				double slice = 0;
				for (int j = 0; j < arrayXYSeries.get(i).getItemCount(); j++) {
					value += (Double) arrayXYSeries.get(i).getY(j);
					slice = (Double) arrayXYSeries.get(i).getX(j);
					serie.add(slice, value);
				}
				result.addSeries(serie);
			}
		} else {
			for (int i = 0; i < arrayXYSeries.size(); i++) {
				String nameOfSerie = (String) arrayXYSeries.get(i).getKey();
				XYSeries serie = null;
				try {
					serie = (XYSeries) arrayXYSeries.get(i).clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				serie.setKey(nameOfSerie);
				result.addSeries(serie);
			}
		}

		chartPanelWithCheckBox.remove(chartPanel);
		chartPanel = getChartsPanel(result);
		chartPanelWithCheckBox.add(chartPanel, BorderLayout.CENTER);
		this.repaint();
	}

	/**
	 * Sum a list of series to obtain a new serie
	 * @param arraySeries
	 * @return The XY series
	 */
	private XYSeries sumSeries(ArrayList<XYSeries> arraySeries) {
		String seriesName = "";
		seriesName = (String) arraySeries.get(0).getKey();
		if (conditioningVariables.size() >= 1) {
			seriesName = seriesName.substring(seriesName.indexOf('[') + 1, seriesName.indexOf(']'));
		}

		XYSeries series = new XYSeries(seriesName);
		for (int i = 0; i < arraySeries.get(0).getItemCount(); i++) {
			double yCoordinate = 0;
			for (int j = 0; j < arraySeries.size(); j++) {
				yCoordinate += arraySeries.get(j).getY(i).doubleValue();
			}
			series.add(i, yCoordinate);
		}
		return series;
	}

	/**
	 * Create the series to be showed. This method only will be launched at the first time. In later
	 * modifications and filters the established series are used to get other combined data
	 */
	private void createSeries() {
		numberOfCombinations = variableOfInterest.getNumStates();

		if (conditioningVariables != null && !conditioningVariables.isEmpty()) {
			for (int i = 0; i < conditioningVariables.size(); i++) {
				numberOfCombinations *= conditioningVariables.get(i).getNumStates();
			}
		}

		List<TablePotential> listOfPotentials = new ArrayList<TablePotential>();
		for (int slice = 0; slice <= numSlices; slice++) {
			String basename = variableOfInterest.getBaseName();
			Variable variableInSliceJ = null;

			try {
				variableInSliceJ = expandedNetwork.getVariable(basename, slice);
			} catch (NodeNotFoundException e) {
				// If the variable not exist, jump to the next slice
				listOfPotentials.add(null);
				continue;
			}

			TablePotential tablePotential = null;

			tablePotential = temporalEvolution.get(variableInSliceJ);

			if (tablePotential.getValues().length < numberOfCombinations) {
				double[] values = new double[numberOfCombinations];
				for (int z = 0; z < numberOfCombinations; z++) {
					values[z] = tablePotential.getValues()[z % temporalEvolution.get(variableInSliceJ)
							.getValues().length];///(numberOfCombinations/variableOfInterest.getNumStates());
				}

				tablePotential.setValues(values);
			}
			listOfPotentials.add(tablePotential);
		}

		arrayXYSeries = new ArrayList<XYSeries>();

		double value = 0.0;
		for (int i = 0; i < numberOfCombinations; i++) {
			XYSeries series = null;
			if (isUtility) {
				String nameOfSerie = "";
				if (conditioningVariables != null && !conditioningVariables.isEmpty()) {
					int positionSelector = variableOfInterest.getNumStates();
					for (int j = conditioningVariables.size() - 1; j >= 0; j--) {
						String nameOfConditionalVariable = conditioningVariables.get(j).getName();
						String stateOfConditionalVariable = conditioningVariables.get(j)
								.getStateName((i / positionSelector) % conditioningVariables.get(j).getNumStates());
						positionSelector *= conditioningVariables.get(j).getNumStates();
						nameOfSerie += nameOfConditionalVariable + " = " + stateOfConditionalVariable + " ; ";
					}
					if (conditioningVariables.size() > 0) {
						nameOfSerie = nameOfSerie.substring(0, nameOfSerie.length() - 3);
					}
				} else {
					nameOfSerie = variableOfInterest.getStateName(i);
				}

				series = new XYSeries(nameOfSerie);
			} else {
				String nameOfSerie = "";
				if (conditioningVariables != null && !conditioningVariables.isEmpty()) {
					int stateIndex = i % variableOfInterest.getNumStates();
					nameOfSerie = variableOfInterest.getStateName(stateIndex) + " [";

					int positionSelector = variableOfInterest.getNumStates();
					for (int j = conditioningVariables.size() - 1; j >= 0; j--) {
						String nameOfConditionalVariable = conditioningVariables.get(j).getName();
						String stateOfConditionalVariable = conditioningVariables.get(j)
								.getStateName((i / positionSelector) % conditioningVariables.get(j).getNumStates());
						positionSelector *= conditioningVariables.get(j).getNumStates();
						nameOfSerie += nameOfConditionalVariable + " = " + stateOfConditionalVariable + " ; ";
					}
					if (conditioningVariables.size() > 0) {
						nameOfSerie = nameOfSerie.substring(0, nameOfSerie.length() - 3) + "]";
					} else {
						nameOfSerie = nameOfSerie.substring(0, nameOfSerie.length() - 2);
					}
				} else {
					nameOfSerie = variableOfInterest.getStateName(i);
				}

				series = new XYSeries(nameOfSerie);
			}

			for (int j = 0; j <= numSlices; j++) {

				if (listOfPotentials.get(j) != null) {
					value = listOfPotentials.get(j).getValues()[i];
					int time = j;
					series.add(time, value);
				}

			}
			arrayXYSeries.add(series);
		}
	}

	/**
	 * Gets the LegendTitles and updates the LegendPanel at the end
	 */
	private void getLegendTitle() {
		legendLabels = new ArrayList<>();
		LegendItemCollection legendItemsOld = chart.getPlot().getLegendItems();

		int listPointer = 0;
		while (listPointer < legendItemsOld.getItemCount()) {

			String subListTitle = legendItemsOld.get(listPointer).getLabel();
			if (subListTitle.indexOf('[') != -1) {
				subListTitle = subListTitle.substring(subListTitle.indexOf('[') + 1, subListTitle.indexOf(']'));
			}

			boolean isSamePolicy = true;
			if (isIndividual && !isUtility && conditioningVariables != null && conditioningVariables.size() >= 1) {
				JLabel groupLabel = new JLabel(subListTitle);
				Font font = groupLabel.getFont();
				groupLabel.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));

				// get metrics from the graphics
				FontMetrics metrics = groupLabel.getFontMetrics(groupLabel.getFont());
				// get the height of a line of text in this font and render context
				int hgt = metrics.getHeight();
				// get the advance of my text in this font and render context
				int adv = metrics.stringWidth(subListTitle);
				// calculate the size of a box to hold the text with some padding.
				Dimension size = new Dimension(adv + 5, hgt + 10);

				groupLabel.setMinimumSize(size);
				groupLabel.setMaximumSize(size);
				groupLabel.setPreferredSize(size);

				groupLabel.setToolTipText(subListTitle);

				font = null;
				legendLabels.add(groupLabel);
			}

			while (isSamePolicy && listPointer < legendItemsOld.getItemCount()) {
				String subListTitle2 = legendItemsOld.get(listPointer).getLabel();
				if (subListTitle2.indexOf('[') != -1) {
					subListTitle2 = subListTitle2.substring(subListTitle2.indexOf('[') + 1, subListTitle2.indexOf(']'));
				}

				if (!subListTitle.equals(subListTitle2)) {
					isSamePolicy = false;
				} else {
					LegendItem item = legendItemsOld.get(listPointer);
					String nameItem2 = item.getLabel();
					if (nameItem2.indexOf('[') != -1) {
						nameItem2 = nameItem2.substring(0, nameItem2.indexOf('[') - 1);
					}

					Image img = makeImage(item, (Color) chart.getXYPlot().getRenderer().getSeriesPaint(listPointer));
					JLabel itemLegendLabel = makeLegendLabel(nameItem2, img);

					// get metrics from the graphics
					FontMetrics metrics = itemLegendLabel.getFontMetrics(itemLegendLabel.getFont());
					// get the height of a line of text in this font and render context
					int hgt = metrics.getHeight();
					// get the advance of my text in this font and render context
					int adv = metrics.stringWidth(nameItem2);
					// calculate the size of a box to hold the text with some padding.

					Dimension size = new Dimension(adv + 30, hgt + 10);
					itemLegendLabel.setMinimumSize(size);
					itemLegendLabel.setMaximumSize(size);
					itemLegendLabel.setPreferredSize(size);
					itemLegendLabel.setToolTipText(nameItem2);

					legendLabels.add(itemLegendLabel);
					listPointer++;

				}
			}
		}
		getLegendsPanel();
	}

	/**
	 * Create the data set using the result given by createSeries method
	 * @return The data set created
	 */
	private XYDataset createDataset() {
		XYSeriesCollection result = new XYSeriesCollection();
		createSeries();
		for (int i = 0; i < arrayXYSeries.size(); i++) {
			result.addSeries(arrayXYSeries.get(i));
		}
		return result;
	}

	/**
	 * Get the TablePane constructed by the data in temporalEvolution
	 * @return The TablePane
	 */
	private JScrollPane getTablePane() {

		tablePane = new TemporalEvolutionTablePane(temporalEvolution, expandedNetwork, variableOfInterest,
				conditioningVariables, numSlices, isUtility, isCumulative);

		return tablePane;
	}

	/**
	 * Allows to save a file with the excel or the png of the information showed in the screem
	 */
	private void saveReport() {
		JFileChooser fileChooser = new JFileChooser();
		String netName = FilenameUtils.getBaseName(expandedNetwork.getName());
		if (tabbedPane.getSelectedIndex() == 0) {
			fileChooser.setSelectedFile(
					new File(netName + "-" + variableOfInterest.getBaseName() + "-temporal_evolution.png"));
		} else {
			fileChooser.setSelectedFile(
					new File(netName + "-" + variableOfInterest.getBaseName() + "-temporal_evolution.xlsx"));
		}
		if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String filename = fileChooser.getSelectedFile().getAbsolutePath();
			if (tabbedPane.getSelectedIndex() == 0) {
				try {

					// Shows the default subtitles, save the png and then hide again the default subtitles
					for (int i = 0; i < chart.getSubtitleCount(); i++) {
						chart.getSubtitle(0).setVisible(true);
					}
					ChartUtilities.saveChartAsPNG(new File(filename), chart, 1024, 768);
					for (int i = 0; i < chart.getSubtitleCount(); i++) {
						chart.getSubtitle(0).setVisible(false);
					}
				} catch (IOException e) {
					// TODO - Translate
					JOptionPane.showMessageDialog(this, "Error when trying to generate report in " + filename);
				}
			} else {
				try {
					createExcel(filename);
				} catch (IOException e) {
					// TODO - Translate
					JOptionPane.showMessageDialog(this, "Error when trying to generate report in " + filename);
				}
			}
		}
	}

	/**
	 * Allows to create an excel of a report
	 *
	 * @param filename
	 * @throws IOException
	 */
	private void createExcel(String filename) throws IOException {
		TemporalEvolutionReport report = new TemporalEvolutionReport();
		report.write(filename, tablePane.getTable());
	}

	/**
	 * Auxiliary method to make a legend JLabel with a title and an image
	 * @param title
	 * @param image
	 * @return The legend label made
	 */
	public JLabel makeLegendLabel(String title, Image image) {
		Icon icon = new ImageIcon(image);
		JLabel label = new JLabel(title);
		label.setIcon(icon);
		return label;
	}

	/**
	 * Auxiliary method to obtain an Image from an LegendItem of JFreeChart and a color
	 * @param item
	 * @param color
	 * @return The image made
	 */
	public Image makeImage(LegendItem item, Color color) {
		final int imgScale = 3;
		final int shapeScale = 2;
		Rectangle r = item.getShape().getBounds();
		BufferedImage image = new BufferedImage(r.width * imgScale, r.height * imgScale,
				BufferedImage.TYPE_BYTE_INDEXED);

		Graphics2D gr = image.createGraphics();
		gr.scale(shapeScale, shapeScale);
		gr.setBackground(Color.WHITE);
		gr.clearRect(0, 0, image.getWidth(), image.getHeight());
		gr.setColor(color);

		// move the shape in the region of the image
		gr.translate(-r.x * 1.5, -r.y * 1.5);
		gr.fill(item.getShape());
		gr.drawLine(-r.width, 0, r.width, 0);

		gr.dispose();

		return image;
	}
}
