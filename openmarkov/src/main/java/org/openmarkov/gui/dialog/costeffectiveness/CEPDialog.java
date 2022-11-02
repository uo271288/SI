/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.costeffectiveness;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Util;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.gui.localize.StringDatabase;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * @author Manuel Arias
 */
@SuppressWarnings("serial") public class CEPDialog extends JDialog {

	// Constants
	private final int DEFAULT_NUM_DECIMALS = 6;

	private final String INTERVENTION_RANGE_COLOR = "#C9EFFB";

	private final String CLICKABLE_COLUMN_COLOR = "#DDF5D8";

	private CEP cep;

	private ProbNet probNet;

	private StringDatabase stringDatabase = StringDatabase.getUniqueInstance();

	private JTable jtableCEP;

	// Constructor

	/**
	 * @param owner
	 * @param cep     <code>CEP</code>
	 * @param probNet
	 */
	public CEPDialog(Window owner, CEP cep, ProbNet probNet) {
		super(owner);

		this.cep = cep;
		this.probNet = probNet.copy();

		initialize();

		//        // Center dialog
		this.setLocationRelativeTo(owner);
		//        Toolkit toolkit = Toolkit.getDefaultToolkit();
		//        Dimension screenSize = toolkit.getScreenSize();
		//        int x = (screenSize.width - this.getWidth()) / 2;
		//        int y = (screenSize.height - this.getHeight()) / 2;
		//        this.setLocation(x, y);

		this.setVisible(true);
	}

	private void initialize() {
		setTitle(stringDatabase.getString("CostEffectivenessResults.Intervals.Title"));
		getContentPane().add(getJContentPane(), BorderLayout.CENTER);
		pack();
		jtableCEP.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
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

	private JPanel getBottomPanel() {
		JPanel buttonsPanel = new JPanel();
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

	private JScrollPane getComponentsPanel() {
		JScrollPane scrollPane = new JScrollPane(getJTableFromCEP(cep));
		scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		return scrollPane;
	}

	// Public method

	/**
	 * @param cep <code>CEP</code>
	 * @return <code>JTable</code>
	 */
	public JTable getJTableFromCEP(final CEP cep) {
		// Set data in jTable
		jtableCEP = new JTable(getDataFromCEP(cep), getColumnsStrings()) {
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

			public boolean getScrollableTracksViewportWidth() {
				return getPreferredSize().width < getParent().getWidth();
			}
		};
		CellEditorNotEditable notEditableCellEditor = new CellEditorNotEditable(new JTextField());

		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
		for (CEPColumns cepColumn : CEPColumns.values()) {
			jtableCEP.getColumnModel().getColumn(cepColumn.ordinal()).setCellEditor(notEditableCellEditor);
		}
		headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		jtableCEP.getTableHeader().setDefaultRenderer(headerRenderer);
		// Set colors in jTable
		setColumnCellRenderer(jtableCEP, CEPColumns.LAMBDA_INF.ordinal(), Color.decode(INTERVENTION_RANGE_COLOR));
		setColumnCellRenderer(jtableCEP, CEPColumns.LAMBDA_SUP.ordinal(), Color.decode(INTERVENTION_RANGE_COLOR));
		setColumnCellRenderer(jtableCEP, CEPColumns.COST.ordinal());
		setColumnCellRenderer(jtableCEP, CEPColumns.EFFECTIVENESS.ordinal());
		setColumnCellRenderer(jtableCEP, CEPColumns.INTERVENTION.ordinal(), Color.decode(CLICKABLE_COLUMN_COLOR),
				stringDatabase.getString("CostEffectivenessResults.Intervals.InterventionTooltip"));

		jtableCEP.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent event) {
				int row = jtableCEP.rowAtPoint(event.getPoint());
				int column = jtableCEP.columnAtPoint(event.getPoint());
				if (column == CEPColumns.INTERVENTION.ordinal()) {
					InterventionDialog interventionDialog = null;
					try {
						interventionDialog = new InterventionDialog(getOwner(), probNet, cep.getStrategyTrees()[row]);
					} catch (IncompatibleEvidenceException e) {
						e.printStackTrace();
					} catch (UnexpectedInferenceException e) {
						e.printStackTrace();
					}
					interventionDialog.setVisible(true);
				}
			}
		});

		return jtableCEP;
	}

	/**
	 * @return Array of <code>String</code>s with the columns headings
	 */
	private String[] getColumnsStrings() {
		int numColumns = CEPColumns.values().length;
		String[] columnsNames = new String[numColumns];
		for (int i = 0; i < numColumns; i++) {
			columnsNames[i] = CEPColumns.values()[i].getText();
		}
		return columnsNames;
	}

	/**
	 * @param cep <code>CEP</code>
	 * @return Rectangular matrix for a <code>JTable</code>.
	 */
	private Object[][] getDataFromCEP(CEP cep) {
		double[] costs = cep.getCosts();
		double[] effectiveness = cep.getEffectivities();
		int numRows = costs.length;
		final StrategyTree[] strategyTrees = cep.getStrategyTrees();
		Object[][] data = new Object[numRows][CEPColumns.values().length];
		for (int i = 0; i < numRows; i++) {
			data[i][CEPColumns.LAMBDA_INF.getIndex()] = getLambdaLeftEndPoint(cep, i);
			data[i][CEPColumns.LAMBDA_SUP.getIndex()] = getLambdaRightEndPoint(cep, i, numRows);
			data[i][CEPColumns.COST.getIndex()] = costs[i];
			data[i][CEPColumns.EFFECTIVENESS.getIndex()] = effectiveness[i];
			data[i][CEPColumns.INTERVENTION.getIndex()] = strategyTrees[i] == null ?
					"---" :
					getFirstLine(strategyTrees[i].toString());
		}

		return data;
	}

	private String getFirstLine(String string) {
		int indexEOL = string.indexOf("\n");
		return indexEOL == -1 ? string : string.substring(0, indexEOL);
	}

	private void setColumnCellRenderer(JTable table, int columnIndex) {
		setColumnCellRenderer(table, columnIndex, null, null);
	}

	private void setColumnCellRenderer(JTable table, int columnIndex, Color color) {
		setColumnCellRenderer(table, columnIndex, color, null);
	}

	private void setColumnCellRenderer(JTable table, int columnIndex, Color color, String text) {
		DefaultTableCellRenderer renderer = getDoubleCellRenderer();
		if (text != null) {
			renderer.setToolTipText(text);
		}

		if (color != null) {
			renderer.setBackground(color);
		}

		if (columnIndex == CEPColumns.LAMBDA_INF.ordinal()) {
			renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		} else if (columnIndex == CEPColumns.LAMBDA_SUP.ordinal()) {
			renderer.setHorizontalAlignment(SwingConstants.LEFT);
		} else if (columnIndex == CEPColumns.COST.ordinal()) {
			renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		} else if (columnIndex == CEPColumns.EFFECTIVENESS.ordinal()) {
			renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		} else if (columnIndex == CEPColumns.INTERVENTION.ordinal()) {
			renderer.setHorizontalAlignment(SwingConstants.LEFT);
		}

		table.getColumnModel().getColumn(columnIndex).setCellRenderer(renderer);
	}

	/**
	 * @param cep
	 * @param intervalIndex
	 * @return Left end point. <code>String</code>
	 */
	private String getLambdaLeftEndPoint(CEP cep, int intervalIndex) {
		Double threshold;
		if (intervalIndex == 0) {
			threshold = cep.getMinThreshold();
		} else {
			threshold = cep.getThreshold(intervalIndex - 1);
		}
		return new Double(Util.roundWithSignificantFigures(threshold, DEFAULT_NUM_DECIMALS)).toString();
	}

	/**
	 * @param cep
	 * @param intervalIndex
	 * @return Right end point. <code>String</code>
	 */
	private String getLambdaRightEndPoint(CEP cep, int intervalIndex, int numIntervals) {
		Double threshold;
		if (intervalIndex == numIntervals - 1) {
			threshold = cep.getMaxThreshold();
		} else {
			threshold = cep.getThreshold(intervalIndex);
		}
		String lambdaRight;
		if (threshold == Double.POSITIVE_INFINITY) {
			lambdaRight = "+\u221E"; // +Inifinite
		} else {
			lambdaRight = new Double(Util.roundWithSignificantFigures(threshold, DEFAULT_NUM_DECIMALS)).toString();
		}
		return lambdaRight;
	}

	private DefaultTableCellRenderer getDoubleCellRenderer() {
		DefaultTableCellRenderer doubleCellRenderer = new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {

				if (value instanceof Double) {
					value = Util.roundWithSignificantFigures((Double) value, DEFAULT_NUM_DECIMALS);
				}
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
		};
		return doubleCellRenderer;
	}

	/**
	 * Enumerate to use in JTable columns
	 */
	private enum CEPColumns {
		LAMBDA_INF(0, "\u03BB inf."), LAMBDA_SUP(1, "\u03BB sup."), COST(2,
				StringDatabase.getUniqueInstance().getString("CostEffectivenessResults.Cost")), EFFECTIVENESS(3,
				StringDatabase.getUniqueInstance().getString("CostEffectivenessResults.Effectiveness")), INTERVENTION(4,
				StringDatabase.getUniqueInstance().getString("CostEffectivenessResults.Intervention"));

		private String text;
		private int index;

		CEPColumns(int index, String text) {
			this.index = index;
			this.text = text;
		}

		private int getIndex() {
			return index;
		}

		public String getText() {
			return text;
		}
	}

	public static class CellEditorNotEditable extends DefaultCellEditor {
		public CellEditorNotEditable(JTextField textField) {
			super(textField);
		}

		@Override public boolean isCellEditable(EventObject anEvent) {
			return false;
		}
	}
}
