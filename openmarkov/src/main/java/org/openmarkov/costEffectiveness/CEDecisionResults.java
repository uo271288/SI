/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.costEffectiveness;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.inference.tasks.CEAnalysis;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Util;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.gui.dialog.costeffectiveness.InterventionDialog;
import org.openmarkov.gui.loader.element.OpenMarkovLogoIcon;
import org.openmarkov.gui.localize.StringDatabase;
import org.openmarkov.inference.variableElimination.tasks.VECEAnalysis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventObject;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/**
 * @author jperez-martin
 */
public class CEDecisionResults extends JDialog {

	private final int COLUMN_STATE_NAME = 0;
	private final int COLUMN_COST = 1;
	private final int COLUMN_EFFECTIVENESS = 2;
	private final int COLUMN_INTERVENTION = 3;
	private final int COLUMN_ICER = 3;
	private final String CLICKABLE_COLUMN_COLOR = "#DDF5D8";
	private final int DEFAULT_NUM_SIGNIFICANT_NUMBERS = 5;
	/**
	 * ProbNet
	 */
	private ProbNet probNet;
	/**
	 * Cost-effectiveness task (conditioned on a DecisionVariable)
	 */
	private CEAnalysis veceAnalysis;
	/**
	 * Conditioning decision variable
	 */
	private Variable decisionVariable;
	/**
	 * Tabbed pane
	 */
	private JTabbedPane tabbedPane;
	/**
	 * Localized stringDatabase
	 */
	private StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
	/**
	 * Selected minimal threshold of the cost-effectiveness partition
	 */
	private double selectedMinThreshold;
	/**
	 * Selected maximal threshold of the cost-effectiveness partition
	 */
	private double selectedMaxThreshold;
	/**
	 * Mean of min and max selected thresholds
	 */
	private double meanThreshold;
	/**
	 * Resulting cepsForDecision (one per decision state)
	 */
	private CEP[] cepsForDecision;
	/**
	 * Resulting GTablePotential
	 */
	private GTablePotential gtablePotentialResult;
	/**
	 * Compacted? Thresholds?
	 */
	private List<Double> thresholdList;
	/**
	 * GUI controls
	 */
	private JRadioButton absoluteRadioButton;
	private JRadioButton relativeRadioButton;
	private JComboBox<State> relativeDecisionSelector;
	private List<JCheckBox> cePlaneShowHideCheckBoxes;
	private List<JCheckBox> frontierInterventionsShowHideCheckBoxes;
	private ChartPanel ceChartPanel;
	private List<JRadioButton> analysisThresholdsRadioButtons;
	private List<JRadioButton> cePlanethresholdsRadioButtons;
	private List<JRadioButton> frontierInterventionsthresholdsRadioButtons;
	private JPanel analysisTablePanel;
	private JPanel analysisPanel;
	private JPanel cePlanePanel;
	private JPanel frontierInterventionsPanel;
	private JPanel frontierInterventionsTablePanel;
	private boolean hasInterventions;

	public CEDecisionResults(Window owner, ProbNet probNet, EvidenceCase evidenceCase, Variable decisionVariable)
			throws NotEvaluableNetworkException, IncompatibleEvidenceException, UnexpectedInferenceException {
		super(owner);
		this.probNet = probNet;
		this.decisionVariable = decisionVariable;

		// Run the task

		veceAnalysis = new VECEAnalysis(probNet);
		veceAnalysis.setPreResolutionEvidence(evidenceCase);
		veceAnalysis.setDecisionVariable(decisionVariable);
		gtablePotentialResult = veceAnalysis.getUtility();

		hasInterventions = false;
		for (Object cep : gtablePotentialResult.elementTable) {
			StrategyTree[] strategyTrees = ((CEP) cep).getStrategyTrees();
			if (strategyTrees != null && strategyTrees.length != 0 && strategyTrees[0] != null) {
				hasInterventions = true;
				break;
			}
		}

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
		setLocationRelativeTo(owner);
		setMinimumSize(new Dimension(width, height / 2));
		setResizable(true);
		repaint();
		pack();
		this.setVisible(true);
	}

	/**
	 * Set title, icon and contentPane
	 */
	private void initialize() {
		this.setTitle(
				"OpenMarkov - " + stringDatabase.getString("CostEffectivenessResults.Title.Label") + " - " + probNet
						.getName());
		this.setIconImage(OpenMarkovLogoIcon.getUniqueInstance().getOpenMarkovLogoIconImage16());
		setContentPane(getJContentPane());
		pack();
	}

	/**
	 * Gets the content pane with the tabbed pane
	 *
	 * @return content pane
	 */
	private JPanel getJContentPane() {
		JPanel jContentPane = new JPanel();
		jContentPane.setLayout(new BorderLayout());
		jContentPane.add(getTabbedPane(), BorderLayout.CENTER);
		return jContentPane;
	}

	/**
	 * Gets the tabbed pane with tornado and spider panels
	 *
	 * @return tabbed pane
	 */
	private JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane();
			tabbedPane
					.addTab(stringDatabase.getString("CostEffectivenessResults.Analysis.Tab"), null, getAnalysisPanel(),
							null);
			tabbedPane.addTab(stringDatabase.getString("CostEffectivenessResults.Plane.Tab"), null, getCEPlanePanel(),
					null);

			tabbedPane.addTab(stringDatabase.getString("CostEffectivenessResults.FrontierInterventions.Tab"), null,
					getFrontierInterventionsPanel(), null);
		}
		return tabbedPane;
	}

	/**
	 * Gets a JPanel with the cost-effectiveness results
	 *
	 * @return cost-effectiveness results
	 */
	private JPanel getAnalysisPanel() {
		analysisPanel = new JPanel();
		analysisPanel.setLayout(new BorderLayout());
		analysisPanel.add(getIntervalsPanel(AnalysisTab.ANALYSIS), BorderLayout.WEST);
		analysisPanel.add(getAnalysisTablePanel(), BorderLayout.CENTER);
		return analysisPanel;
	}

	/**
	 * Gets the cost-effectiveness plane
	 *
	 * @return cost-effectiveness plane
	 */
	public JPanel getCEPlanePanel() {
		cePlanePanel = new JPanel();
		cePlanePanel.setLayout(new BorderLayout());
		cePlanePanel.add(getIntervalsPanel(AnalysisTab.CEPLANE), BorderLayout.WEST);
		cePlanePanel.add(getAbsRelShowHidePanel(AnalysisTab.CEPLANE), BorderLayout.EAST);
		cePlanePanel.add(getCEPlaneChartPanel(), BorderLayout.CENTER);

		return cePlanePanel;
	}

	/**
	 * Gets the frontier interventions panel
	 *
	 * @return frontier interventions panel
	 */
	public JPanel getFrontierInterventionsPanel() {
		frontierInterventionsPanel = new JPanel();
		frontierInterventionsPanel.setLayout(new BorderLayout());
		frontierInterventionsPanel.add(getIntervalsPanel(AnalysisTab.FRONTIER_INTERVENTIONS), BorderLayout.WEST);
		frontierInterventionsPanel.add(getShowHidePanel(AnalysisTab.FRONTIER_INTERVENTIONS), BorderLayout.EAST);
		frontierInterventionsPanel.add(getFrontierInterventionsTablePanel(), BorderLayout.CENTER);

		return frontierInterventionsPanel;
	}

	/**
	 * Get the intervals panel with all the compact intervals
	 *
	 * @return intervals panel with all the compact intervals
	 */
	public JScrollPane getIntervalsPanel(final AnalysisTab analysisTab) {

		JPanel intervalsPanel = new JPanel();

		cepsForDecision = new CEP[gtablePotentialResult.elementTable.size()];

		boolean moreThanOneInterval = false;

		LinkedHashSet<Double> thresholds = new LinkedHashSet<>();
		for (int i = 0; i < gtablePotentialResult.elementTable.size(); i++) {
			CEP cep = (CEP) gtablePotentialResult.elementTable.get(i);
			cepsForDecision[i] = cep;
			if (cep.getNumIntervals() != 1) {
				moreThanOneInterval = true;
				for (double threshold : cep.getThresholds()) {
					thresholds.add(threshold);
				}
			}
		}

		// If there are more than one interval, is necessary get the compact intervals and paint it into the panel
		if (moreThanOneInterval) {
			intervalsPanel
					.setBorder(new TitledBorder(stringDatabase.getString("CostEffectivenessResults.Intervals.Label")));
			intervalsPanel.setLayout(new BoxLayout(intervalsPanel, BoxLayout.PAGE_AXIS));

			thresholdList = new ArrayList<>(thresholds);
			Collections.sort(thresholdList);
			List<JRadioButton> thresholdsRadioButtons = new ArrayList<>();
			ButtonGroup buttonGroup = new ButtonGroup();
			double lowerBound = 0;

			for (double threshold : thresholdList) {
				JRadioButton intervalRadioButton = new JRadioButton(
						Util.roundWithSignificantFigures(lowerBound, DEFAULT_NUM_SIGNIFICANT_NUMBERS) + " - " + Util
								.roundWithSignificantFigures(threshold, DEFAULT_NUM_SIGNIFICANT_NUMBERS));
				buttonGroup.add(intervalRadioButton);
				thresholdsRadioButtons.add(intervalRadioButton);
				intervalsPanel.add(intervalRadioButton);
				intervalRadioButton.addActionListener(new ActionListener() {
					@Override public void actionPerformed(ActionEvent e) {
						thresholdChanged(analysisTab);
					}
				});
				lowerBound = threshold;
			}
			JRadioButton intervalRadioButton = new JRadioButton(
					Util.roundWithSignificantFigures(lowerBound, DEFAULT_NUM_SIGNIFICANT_NUMBERS) + " - "
							+ Double.POSITIVE_INFINITY);

			buttonGroup.add(intervalRadioButton);
			intervalRadioButton.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					thresholdChanged(analysisTab);
				}
			});
			thresholdsRadioButtons.add(intervalRadioButton);
			intervalsPanel.add(intervalRadioButton);

			thresholdsRadioButtons.get(0).setSelected(true);

			// If the selected tab is "Analysis" are the radio buttons for analysis
			if (analysisTab == AnalysisTab.ANALYSIS) {
				analysisThresholdsRadioButtons = thresholdsRadioButtons;
			} else if (analysisTab == AnalysisTab.CEPLANE) {
				cePlanethresholdsRadioButtons = thresholdsRadioButtons;
			} else if (analysisTab == AnalysisTab.FRONTIER_INTERVENTIONS) {
				frontierInterventionsthresholdsRadioButtons = thresholdsRadioButtons;
			}

		} else {
			selectedMinThreshold = 0;
			selectedMaxThreshold = Double.POSITIVE_INFINITY;
		}

		JScrollPane scrollPane = new JScrollPane(intervalsPanel);
		scrollPane.setBorder(new EmptyBorder(2, 2, 2, 2));
		return scrollPane;
	}

	/**
	 * Get the Panel with the JTable
	 *
	 * @return Panel with the JTable
	 */
	public JPanel getAnalysisTablePanel() {
		analysisTablePanel = new JPanel();
		analysisTablePanel.add(new JScrollPane(getAnalysisTable()));
		return analysisTablePanel;
	}

	/**
	 * Get the Panel with the JTable
	 *
	 * @return Panel with the JTable
	 */
	public JPanel getFrontierInterventionsTablePanel() {
		frontierInterventionsTablePanel = new JPanel();
		frontierInterventionsTablePanel.add(new JScrollPane(getFrontierInterventionsTable()));
		return frontierInterventionsTablePanel;
	}

	/**
	 * Get the table with the CEPs
	 *
	 * @return table with the CEPs
	 */
	public JTable getAnalysisTable() {
		// Add one for the header
		int numRows = decisionVariable.getNumStates();
		int numColumns = getColumns(AnalysisTab.ANALYSIS).length;
		// Set data in jTable
		Object[][] values = new Object[numRows][numColumns];

		for (int row = 0; row < numRows; row++) {
			// Set decision variable state name
			values[row][COLUMN_STATE_NAME] = decisionVariable.getStateName(row);

			// Set costs and effectiveness for that decision state
			values[row][COLUMN_COST] = cepsForDecision[row].getCost(meanThreshold);
			values[row][COLUMN_EFFECTIVENESS] = cepsForDecision[row].getEffectiveness(meanThreshold);
			if (hasInterventions) {
				values[row][COLUMN_INTERVENTION] = cepsForDecision[row].getIntervention(meanThreshold);
			}
		}

		final JTable jtable = new JTable(values, getColumns(AnalysisTab.ANALYSIS)) {
			@Override public void doLayout() {
				if (tableHeader != null) {
					TableColumn resizingColumn = tableHeader.getResizingColumn();
					//  Viewport size changed. Increase last columns width

					if (resizingColumn == null) {
						TableColumnModel tcm = getColumnModel();
						int lastColumn = tcm.getColumnCount() - 1;
						tableHeader.setResizingColumn(tcm.getColumn(lastColumn));
					}
				}

				super.doLayout();
			}

			@Override public boolean getScrollableTracksViewportWidth() {
				return getPreferredSize().width < getParent().getWidth();
			}
		};

		jtable.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent event) {
				int row = jtable.rowAtPoint(event.getPoint());
				int column = jtable.columnAtPoint(event.getPoint());
				if (column == COLUMN_INTERVENTION) {
					StrategyTree strategyTree = cepsForDecision[row].getIntervention(meanThreshold);

					if (strategyTree != null) {
						InterventionDialog interventionDialog = null;
						try {
							interventionDialog = new InterventionDialog(getOwner(), probNet, strategyTree);
						} catch (IncompatibleEvidenceException e) {
							e.printStackTrace();
						} catch (UnexpectedInferenceException e) {
							e.printStackTrace();
						}
						interventionDialog.setVisible(true);
					}
				}
			}
		});

		DefaultCellEditor notEditableCellEditor = new DefaultCellEditor(new JTextField()) {
			@Override public boolean isCellEditable(EventObject anEvent) {
				return false;
			}
		};

		for (int columnIndex = 0; columnIndex < jtable.getColumnModel().getColumnCount(); columnIndex++) {
			jtable.getColumnModel().getColumn(columnIndex).setCellEditor(notEditableCellEditor);
			jtable.getColumnModel().getColumn(columnIndex).setCellRenderer(getDoubleCellRenderer());
		}

		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
		headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		jtable.getTableHeader().setDefaultRenderer(headerRenderer);
		// Set colors in jTable
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setBackground(Color.decode(CLICKABLE_COLUMN_COLOR));

		if (hasInterventions) {
			jtable.getColumnModel().getColumn(COLUMN_INTERVENTION).setCellRenderer(renderer);
		}

		return jtable;
	}

	private DefaultTableCellRenderer getDoubleCellRenderer() {
		DefaultTableCellRenderer doubleCellRenderer = new DefaultTableCellRenderer() {
			@Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {

				if (value instanceof Double) {
					value = Util.roundWithSignificantFigures((Double) value, DEFAULT_NUM_SIGNIFICANT_NUMBERS);
				}
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
		};
		return doubleCellRenderer;
	}

	/**
	 * Get the table with the CEPs
	 *
	 * @return table with the CEPs
	 */
	public JTable getFrontierInterventionsTable() {

		List<CEP> frontierInterventions = calculateFrontierInterventions(AnalysisTab.FRONTIER_INTERVENTIONS);
		String[] interventionsNames = new String[frontierInterventions.size()];
		List<CEP> cepsForDecisionList = Arrays.asList(cepsForDecision);
		for (int i = 0; i < frontierInterventions.size(); i++) {
			int stateIndex = cepsForDecisionList.indexOf(frontierInterventions.get(i));
			interventionsNames[i] = decisionVariable.getStateName(stateIndex);
		}

		int numRows = frontierInterventions.size();
		int numColumns = getColumns(AnalysisTab.FRONTIER_INTERVENTIONS).length;
		// Set data in jTable
		Object[][] values = new Object[numRows][numColumns];

		for (int row = 0; row < numRows; row++) {
			// Set decision variable state name
			values[row][COLUMN_STATE_NAME] = interventionsNames[row];

			// Set costs and effectiveness for that decision state
			values[row][COLUMN_COST] = frontierInterventions.get(row).getCost(meanThreshold);
			values[row][COLUMN_EFFECTIVENESS] = frontierInterventions.get(row).getEffectiveness(meanThreshold);

			// Set the ICER between the first (cheaper) intervention and the current intervention
			double icer = 0;
			if (row != 0) {
				CEP cheapestIntervention = frontierInterventions.get(0);
				double costDif = (
						BigDecimal.valueOf(frontierInterventions.get(row).getCost(meanThreshold)).
								subtract(BigDecimal.valueOf(cheapestIntervention.getCost(meanThreshold)))
				).doubleValue();
				double effDif = (
						BigDecimal.valueOf(frontierInterventions.get(row).getEffectiveness(meanThreshold)).
								subtract(BigDecimal.valueOf(cheapestIntervention.getEffectiveness(meanThreshold)))
				).doubleValue();
				icer = costDif / effDif;
			}
			values[row][COLUMN_ICER] = new Double(icer);
		}

		JTable jtable = new JTable(values, getColumns(AnalysisTab.FRONTIER_INTERVENTIONS));

		// Create a new default editor with non-editable cells
		DefaultCellEditor notEditableCellEditor = new DefaultCellEditor(new JTextField()) {
			@Override public boolean isCellEditable(EventObject anEvent) {
				return false;
			}
		};

		// Sets the editor into all columns
		for (int columnIndex = 0; columnIndex < jtable.getColumnModel().getColumnCount(); columnIndex++) {
			jtable.getColumnModel().getColumn(columnIndex).setCellEditor(notEditableCellEditor);
			jtable.getColumnModel().getColumn(columnIndex).setCellRenderer(getDoubleCellRenderer());

		}

		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
		headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		jtable.getTableHeader().setDefaultRenderer(headerRenderer);

		return jtable;
	}

	/**
	 * Calculate the frontier interventions
	 *
	 * @return List of frontier interventions
	 */
	public List<CEP> calculateFrontierInterventions(AnalysisTab analysisTab) {
		ArrayList<CEP> remainingInterventions = new ArrayList<>();
		ArrayList<CEP> frontierInterventions = new ArrayList<>();

		// Get the selected interventions
		for (int cepIndex = 0; cepIndex < cepsForDecision.length; cepIndex++) {
			if (analysisTab == AnalysisTab.CEPLANE) {
				// If the intervention is selected add to remaining interventions
				if (cePlaneShowHideCheckBoxes.get(cepIndex).isSelected()) {
					remainingInterventions.add(cepsForDecision[cepIndex]);
				}
			} else if (analysisTab == AnalysisTab.FRONTIER_INTERVENTIONS) {
				// If the intervention is selected add to remaining interventions
				if (frontierInterventionsShowHideCheckBoxes.get(cepIndex).isSelected()) {
					remainingInterventions.add(cepsForDecision[cepIndex]);
				}
			}
		}

		if (remainingInterventions.size() == 0) {
			return frontierInterventions;
		}

		// Get the cheapest intervention
		CEP cheapestIntervention = remainingInterventions.get(0);
		for (int i = 1; i < remainingInterventions.size(); i++) {
			CEP intervention = remainingInterventions.get(i);
			if ((intervention.getCost(meanThreshold) < cheapestIntervention.getCost(meanThreshold)) || (
					intervention.getCost(meanThreshold) == cheapestIntervention.getCost(meanThreshold)
							&& intervention.getEffectiveness(meanThreshold) > cheapestIntervention
							.getEffectiveness(meanThreshold)
			)) {
				cheapestIntervention = intervention;
			}
		}
		// Add the cheapest intervention to the frontier interventions list and remove from auxiliar list
		frontierInterventions.add(cheapestIntervention);
		remainingInterventions.remove(cheapestIntervention);

		while (!remainingInterventions.isEmpty()) {
			// Remove interventions with minor effectiveness
			List<CEP> toRemove = new ArrayList<>();
			for (CEP intervention : remainingInterventions) {
				if (intervention.getEffectiveness(meanThreshold) <= cheapestIntervention
						.getEffectiveness(meanThreshold)) {
					toRemove.add(intervention);
				}
			}
			for (CEP intervention : toRemove) {
				remainingInterventions.remove(intervention);
			}

			// Get smallest ICER from minor intervention
			double smallestICER = Double.POSITIVE_INFINITY;
			CEP candidateintervention = null;
			for (CEP intervention : remainingInterventions) {
				double costDiff = (
						BigDecimal.valueOf(intervention.getCost(meanThreshold)).
								subtract(BigDecimal.valueOf(cheapestIntervention.getCost(meanThreshold)))
				).doubleValue();
				double effDiff = (
						BigDecimal.valueOf(intervention.getEffectiveness(meanThreshold)).
								subtract(BigDecimal.valueOf(cheapestIntervention.getEffectiveness(meanThreshold)))
				).doubleValue();
				//                double ICER = (BigDecimal.valueOf(costDiff).divide(BigDecimal.valueOf(effDiff))).doubleValue();
				double ICER = costDiff / effDiff;

				if (ICER < smallestICER) {
					candidateintervention = intervention;
					smallestICER = ICER;
				} else if (ICER == smallestICER && (
						intervention.getEffectiveness(meanThreshold) < candidateintervention
								.getEffectiveness(meanThreshold)
				)) {
					candidateintervention = intervention;
				}
			}
			if (!remainingInterventions.isEmpty()) {
				frontierInterventions.add(candidateintervention);
				remainingInterventions.remove(candidateintervention);
				cheapestIntervention = candidateintervention;
			}
		}
		return frontierInterventions;
	}

	/**
	 * Get column names
	 *
	 * @return column names
	 */
	public String[] getColumns(AnalysisTab analysisTab) {
		String[] columnNames;

		if (analysisTab == AnalysisTab.ANALYSIS) {
			if (hasInterventions) {
				columnNames = new String[4];
				columnNames[3] = stringDatabase.getString("CostEffectivenessResults.Intervention");
			} else {
				columnNames = new String[3];
			}
		} else if (analysisTab == AnalysisTab.FRONTIER_INTERVENTIONS) {
			columnNames = new String[4];
			columnNames[3] = stringDatabase.getString("CostEffectivenessResults.ICER");
		} else {
			return null;
		}

		columnNames[0] = decisionVariable.getBaseName();
		columnNames[1] = stringDatabase.getString("CostEffectivenessResults.Cost");
		columnNames[2] = stringDatabase.getString("CostEffectivenessResults.Effectiveness");

		return columnNames;
	}

	/**
	 * Build the right column with both panels
	 *
	 * @return right column with both panels
	 */
	public JPanel getAbsRelShowHidePanel(AnalysisTab analysisTab) {
		JPanel absRelShowHidePanel = new JPanel();
		absRelShowHidePanel.setLayout(new BorderLayout());
		absRelShowHidePanel.add(getAbsoluteRelativePanel(), BorderLayout.NORTH);
		absRelShowHidePanel.add(getShowHidePanel(analysisTab));

		return absRelShowHidePanel;
	}

	/**
	 * Returns the scroll pane with the absolute/relative functionality
	 *
	 * @return the scroll pane with the absolute/relative functionality
	 */
	public JPanel getAbsoluteRelativePanel() {
		JPanel absoluteRelativePanel = new JPanel();
		absoluteRelativePanel
				.setBorder(new TitledBorder(stringDatabase.getString("CostEffectivenessResults.Controls.Display")));
		absoluteRelativePanel.setLayout(new BoxLayout(absoluteRelativePanel, BoxLayout.PAGE_AXIS));

		ButtonGroup buttonGroup = new ButtonGroup();
		absoluteRadioButton = new JRadioButton(stringDatabase.getString("CostEffectivenessResults.Controls.Absolute"));
		relativeRadioButton = new JRadioButton(stringDatabase.getString("CostEffectivenessResults.Controls.Relative"));
		absoluteRadioButton.setSelected(true);
		absoluteRadioButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				relativeDecisionSelector.setEnabled(false);
				refreshChartPanel();
			}
		});

		relativeRadioButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				relativeDecisionSelector.setEnabled(true);
				refreshChartPanel();
			}
		});

		JPanel absoluteRadioButtonPanel = new JPanel();
		absoluteRadioButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		absoluteRadioButtonPanel.add(absoluteRadioButton);
		absoluteRelativePanel.add(absoluteRadioButtonPanel);

		JPanel relativeRadioButtonPanel = new JPanel();
		relativeRadioButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		relativeRadioButtonPanel.add(relativeRadioButton);
		absoluteRelativePanel.add(relativeRadioButtonPanel);

		buttonGroup.add(absoluteRadioButton);
		buttonGroup.add(relativeRadioButton);

		relativeDecisionSelector = new JComboBox<>();
		for (State state : decisionVariable.getStates()) {
			relativeDecisionSelector.addItem(state);
		}
		relativeDecisionSelector.setEnabled(false);
		relativeDecisionSelector.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				refreshChartPanel();
			}
		});

		absoluteRelativePanel.add(relativeDecisionSelector);
		absoluteRelativePanel.add(new JPanel());

		return absoluteRelativePanel;
	}

	/**
	 * Returns the scroll pane with the show/hide functionality
	 *
	 * @return scroll pane with the show/hide functionality
	 */
	public JScrollPane getShowHidePanel(final AnalysisTab analysisTab) {
		JPanel showHidePanel = new JPanel();
		showHidePanel
				.setBorder(new TitledBorder(stringDatabase.getString("CostEffectivenessResults.Controls.ShowHide")));
		showHidePanel.setLayout(new BoxLayout(showHidePanel, BoxLayout.PAGE_AXIS));

		ArrayList<JCheckBox> checkBoxesList = new ArrayList();

		for (State state : decisionVariable.getStates()) {
			JCheckBox stateCheckbox = new JCheckBox(state.getName());
			stateCheckbox.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					if (analysisTab == AnalysisTab.CEPLANE) {
						refreshChartPanel();
					} else if (analysisTab == AnalysisTab.FRONTIER_INTERVENTIONS) {
						refreshFrontierInterventionsTablePanel();
					}
				}
			});
			stateCheckbox.setSelected(true);
			checkBoxesList.add(stateCheckbox);
			showHidePanel.add(stateCheckbox);
		}

		if (analysisTab == AnalysisTab.CEPLANE) {
			cePlaneShowHideCheckBoxes = checkBoxesList;
		} else if (analysisTab == AnalysisTab.FRONTIER_INTERVENTIONS) {
			frontierInterventionsShowHideCheckBoxes = checkBoxesList;
		}

		JScrollPane scrollPane = new JScrollPane(showHidePanel);
		scrollPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		scrollPane.setPreferredSize(new Dimension(150, 0));
		return scrollPane;
	}

	/**
	 * Action performed when a threshold has changed
	 */
	private void thresholdChanged(AnalysisTab analysisTab) {
		List<JRadioButton> currentTabRadioButtons = null;

		// If the selected tab is "Analysis", the selected radio button is from analysis
		if (analysisTab == AnalysisTab.ANALYSIS) {
			currentTabRadioButtons = analysisThresholdsRadioButtons;
		} else if (analysisTab == AnalysisTab.CEPLANE) {
			currentTabRadioButtons = cePlanethresholdsRadioButtons;
		} else if (analysisTab == AnalysisTab.FRONTIER_INTERVENTIONS) {
			currentTabRadioButtons = frontierInterventionsthresholdsRadioButtons;
		}

		for (int i = 0; i < currentTabRadioButtons.size(); i++) {
			if (currentTabRadioButtons.get(i).isSelected()) {
				// Update the selected threshold
				if (i == 0) {
					this.selectedMinThreshold = 0;
					this.selectedMaxThreshold = thresholdList.get(0);
				} else if (i == currentTabRadioButtons.size() - 1) {
					this.selectedMinThreshold = thresholdList.get(i - 1);
					this.selectedMaxThreshold = Double.POSITIVE_INFINITY;
				} else {
					this.selectedMinThreshold = thresholdList.get(i - 1);
					this.selectedMaxThreshold = thresholdList.get(i);
				}

				// Mark as selected the same thresholds of the other tab
				analysisThresholdsRadioButtons.get(i).setSelected(true);
				cePlanethresholdsRadioButtons.get(i).setSelected(true);
				frontierInterventionsthresholdsRadioButtons.get(i).setSelected(true);

				this.meanThreshold = selectedMinThreshold + (selectedMaxThreshold - selectedMinThreshold) / 2;

				break;
			}
		}

		refreshChartPanel();
		refreshAnalysisTablePanel();
		refreshFrontierInterventionsTablePanel();
	}

	/**
	 * Repaint and refresh the chart panel and its components
	 */
	private void refreshChartPanel() {
		this.setVisible(false);
		cePlanePanel.remove(ceChartPanel);
		cePlanePanel.add(getCEPlaneChartPanel(), BorderLayout.CENTER);
		this.setVisible(true);
	}

	/**
	 * Repaint and refresh the analysisTablePanel and its components
	 */
	private void refreshAnalysisTablePanel() {
		this.setVisible(false);
		analysisPanel.remove(analysisTablePanel);
		analysisPanel.add(getAnalysisTablePanel(), BorderLayout.CENTER);
		this.setVisible(true);
	}

	/**
	 * Repaint and refresh the frontierInterventionsPanel and its components
	 */
	private void refreshFrontierInterventionsTablePanel() {
		this.setVisible(false);
		frontierInterventionsPanel.remove(frontierInterventionsTablePanel);
		frontierInterventionsPanel.add(getFrontierInterventionsTablePanel(), BorderLayout.CENTER);
		this.setVisible(true);
	}

	/**
	 * Get cost-effectiveness plane chart
	 *
	 * @return cost-effectiveness plane chart
	 */
	public ChartPanel getCEPlaneChartPanel() {
		// JFreeChart attributes definition
		XYSeriesCollection scatterPlotDataset = getScatterPlotData();

		XYPlot plot = new XYPlot();
		XYItemRenderer renderer1 = new XYLineAndShapeRenderer(false, true);
		ValueAxis domain = new NumberAxis(stringDatabase.getString("CostEffectivenessResults.Effectiveness"));
		ValueAxis range = new NumberAxis(stringDatabase.getString("CostEffectivenessResults.Cost"));

		plot.setDataset(0, scatterPlotDataset);
		plot.setRenderer(0, renderer1);
		plot.setDomainAxis(0, domain);
		plot.setRangeAxis(0, range);

		// Create the line data, renderer, and axis
		XYDataset collection2 = getLineFrontierInterventionsData();
		XYItemRenderer renderer2 = new XYLineAndShapeRenderer(true, false);   // Lines only
		renderer2.setSeriesPaint(0, Color.RED);
		BasicStroke stroke = new BasicStroke();
		renderer2.setSeriesStroke(0,
				new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] { 6.0f, 6.0f },
						0.0f));

		// Set the line data, renderer, and axis into plot
		plot.setDataset(1, collection2);
		plot.setRenderer(1, renderer2);
		JFreeChart chart = new JFreeChart(stringDatabase.getString("CostEffectivenessResults.Plane.Label"), plot);

		// Set general aspects
		chart.getLegend().setPosition(RectangleEdge.RIGHT);

		ceChartPanel = new ChartPanel(chart);
		ceChartPanel.setAutoscrolls(true);
		ceChartPanel.setDisplayToolTips(true);
		ceChartPanel.setMouseZoomable(true);
		//        XYPlot plot = (XYPlot) chart.getPlot ();
		XYItemRenderer renderer = plot.getRenderer();
		NumberFormat format = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
		XYToolTipGenerator generator = new StandardXYToolTipGenerator("{0}: ({1}, {2})", format, format);
		renderer.setBaseToolTipGenerator(generator);

		return ceChartPanel;
	}

	/**
	 * Get the scatter plot data from selected interventions
	 *
	 * @return scatter plot data from selected interventions
	 */
	public XYSeriesCollection getScatterPlotData() {
		XYSeriesCollection dataset = new XYSeriesCollection();

		double baseCost;
		double baseEffectiveness;

		// Relative
		if (relativeRadioButton.isSelected()) {
			int indexSelected = relativeDecisionSelector.getSelectedIndex();
			baseCost = cepsForDecision[indexSelected].getCost(meanThreshold);
			baseEffectiveness = cepsForDecision[indexSelected].getEffectiveness(meanThreshold);

			// Absolute
		} else {
			baseCost = 0;
			baseEffectiveness = 0;
		}

		for (int cepIndex = 0; cepIndex < cepsForDecision.length; cepIndex++) {

			// If the serie is hidden, skip it from JFreeChart
			if (!cePlaneShowHideCheckBoxes.get(cepIndex).isSelected()) {
				continue;
			}
			XYSeries series = new XYSeries(decisionVariable.getStateName(cepIndex));

			double cost = cepsForDecision[cepIndex].getCost(meanThreshold);
			cost -= baseCost;

			double effectiveness = cepsForDecision[cepIndex].getEffectiveness(meanThreshold);
			effectiveness -= baseEffectiveness;

			series.add(effectiveness, cost);

			dataset.addSeries(series);
		}

		return dataset;
	}

	/**
	 * Get frontier interventions lines
	 *
	 * @return frontier interventions lines
	 */
	public XYSeriesCollection getLineFrontierInterventionsData() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries frontierInterventionsSerie = new XYSeries(
				stringDatabase.getString("CostEffectivenessResults.FrontierInterventions.Tab"));

		double baseCost;
		double baseEffectiveness;

		if (relativeRadioButton.isSelected()) {
			int indexSelected = relativeDecisionSelector.getSelectedIndex();
			baseCost = cepsForDecision[indexSelected].getCost(meanThreshold);
			baseEffectiveness = cepsForDecision[indexSelected].getEffectiveness(meanThreshold);

			// Absolute
		} else {
			baseCost = 0;
			baseEffectiveness = 0;
		}

		for (CEP cep : calculateFrontierInterventions(AnalysisTab.CEPLANE)) {
			double cost = cep.getCost(meanThreshold);
			cost -= baseCost;

			double effectiveness = cep.getEffectiveness(meanThreshold);
			effectiveness -= baseEffectiveness;

			frontierInterventionsSerie.add(effectiveness, cost);
		}

		dataset.addSeries(frontierInterventionsSerie);

		return dataset;
	}

	private enum AnalysisTab {
		ANALYSIS, CEPLANE, FRONTIER_INTERVENTIONS
	}

}
