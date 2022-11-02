package org.openmarkov.gui.util;

import org.openmarkov.core.dt.DecisionTreeBranch;
import org.openmarkov.core.dt.DecisionTreeElement;
import org.openmarkov.core.dt.DecisionTreeNode;
import org.openmarkov.core.model.network.NodeType;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TreeNodeToDot {

    private final String C_decisionColor = "#cfe3fd";
    private final String C_chanceColor = "#fbf999";
    private final String C_utilityColor = "#d0e6b2";

    private class DotNode {
        private String nodeName;
        private int number;
        private NodeType type;
        private double computedUtility;

        public DotNode(int number, String nodeName, double computedUtility, NodeType type) {
            this.nodeName = nodeName;
            this.number = number;
            this.type = type;
            this.computedUtility = computedUtility;
        }

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public NodeType getType() {
            return type;
        }

        public void setType(NodeType type) {
            this.type = type;
        }

        private String buildStyle() {
            if (this.type.equals(NodeType.CHANCE)) {
                return "shape = \"oval\", fillcolor=\"" + C_chanceColor + "\"";
            } else if (this.type.equals(NodeType.DECISION)) {
                return "shape = \"box\", fillcolor=\"" + C_decisionColor + "\"";
            } else {
                return "shape = \"hexagon\", fillcolor=\"" + C_utilityColor + "\"";
            }
        }

        private String buildLabel() {
            return "<b>" + this.nodeName + "</b><br/><font color=\"red\"> U=" + df.format(this.computedUtility) + "</font>";
        }

        @Override
        public String toString() {
            return this.number + " [label=<" + buildLabel() + ">, " + buildStyle() + "];";
        }
    }

    private class DotLink {
        private DotNode sourceNode;
        private DotNode destinationNode;
        private String branchState;
        private double probability;

        public DotLink(DotNode sourceNode, DotNode destinationNode, String branchState, double probability) {
            this.sourceNode = sourceNode;
            this.destinationNode = destinationNode;
            this.probability = probability;
            this.branchState = branchState;

        }

        public DotNode getSourceNode() {
            return sourceNode;
        }

        public void setSourceNode(DotNode sourceNode) {
            this.sourceNode = sourceNode;
        }

        public DotNode getDestinationNode() {
            return destinationNode;
        }

        public void setDestinationNode(DotNode destinationNode) {
            this.destinationNode = destinationNode;
        }

        private String buildLabel() {
            if (this.sourceNode.getType().equals(NodeType.CHANCE)) {
                return "<b>" + this.branchState + "</b><br/>P=" + df.format(this.probability);
            } else {
                return "<b>" + this.branchState + "</b>";
            }
        }

        @Override
        public String toString() {
            return this.sourceNode.getNumber() + " -> " + this.destinationNode.getNumber() + " [label=<" + buildLabel() + ">];";
        }
    }

    List<DotNode> dotNodes = new ArrayList<>();
    List<DotLink> dotLinks = new ArrayList<>();

    private int numNode = 0;
    private DecimalFormat df = new DecimalFormat();
    private int graphDPI;

    public TreeNodeToDot() {
        graphDPI = 300;
        setNumDecimals(4);
    }

    public void paintDTNode(DecisionTreeNode treeNode) {
        List<DecisionTreeNode> children = new ArrayList<>();
        children.add(treeNode);

        DotNode sourceNode = new DotNode(numNode, treeNode.getVariable().getName(), (double) treeNode.getUtility(), treeNode.getNodeType());
        numNode += 1;
        dotNodes.add(sourceNode);


        parseTreeNode(sourceNode, treeNode);

        StringBuilder graph = new StringBuilder();
        graph.append("digraph G {" + "\n");
        graph.append("\tgraph [dpi = " + graphDPI + "];\n");
        graph.append("\trankdir=LR;\n");
        graph.append("\tnode [style=\"filled\"]; \n");
        for (DotNode node : dotNodes) {
            graph.append("\t" + node.toString() + "\n");
        }

        for (DotLink dotLink : dotLinks) {
            graph.append("\t" + dotLink + "\n");
        }
        graph.append("}");

        System.out.println(graph.toString());

        JFileChooser chooser = new JFileChooser();
        int retrival = chooser.showSaveDialog(null);
        if (retrival == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(chooser.getSelectedFile() + ".gv")) {
                fw.write(graph.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseTreeNode(DotNode sourceNode, DecisionTreeNode treeNode) {

        // Analyze the branches of that node
        for (Object elements : treeNode.getChildren()) {
            DecisionTreeBranch branch = (DecisionTreeBranch) elements;
            String branchState = null;
            branchState = branch.getBranchState().getName();

            DecisionTreeNode childNode = branch.getChild();
            DotNode destinationNode = new DotNode(numNode, childNode.getVariable().getName(), (double) childNode.getUtility(), childNode.getNodeType());
            numNode += 1;
            dotNodes.add(destinationNode);
            dotLinks.add(new DotLink(sourceNode, destinationNode, branchState, branch.getBranchProbability()));

            parseTreeNode(destinationNode, childNode);
        }

    }

    public void setNumDecimals(int numDecimals) {
        df.setMaximumFractionDigits(numDecimals);
    }

    public void setGraphDPI(int graphDPI) {
        this.graphDPI = graphDPI;
    }
}
