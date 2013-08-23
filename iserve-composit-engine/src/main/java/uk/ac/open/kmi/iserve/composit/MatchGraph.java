package uk.ac.open.kmi.iserve.composit;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import uk.ac.open.kmi.iserve.commons.model.Operation;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URI;

public class MatchGraph {
    private Multimap<URI, URI> graphMatch;
    private Multimap<Operation, Operation> operationGraph;

    public MatchGraph(Multimap<URI, URI> graphMatch, Multimap<Operation, Operation> operationGraph) {
        this.graphMatch = graphMatch;
        this.operationGraph = operationGraph;
    }

    public Multimap<URI, URI> getGraphMatch() {
        return graphMatch;
    }

    public Multimap<Operation, Operation> getOperationGraph() {
        return operationGraph;
    }

    public JFrame display(){
        Graph<String,String> graph = buildGraph();

        DAGLayout<String, String> dagLayout = new DAGLayout<String, String>(graph);
        dagLayout.setSize(new Dimension(600,600));

        BasicVisualizationServer<String, String> visualizationServer = new BasicVisualizationServer<String, String>(dagLayout);
        visualizationServer.setPreferredSize(new Dimension(610,610));
        visualizationServer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
        JFrame frame = new JFrame("Graph");
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(visualizationServer);
        frame.pack();
        frame.setVisible(true);
        return frame;
    }

    public Graph<String, String> buildGraph(){
        Graph<String,String> graph = new SparseGraph<String, String>();
        // Create the operation graph
        for(Operation op1 : operationGraph.keySet()){
            String source = op1.getLabel();
            if (!graph.containsVertex(source)){
                graph.addVertex(source);
            }
            for(Operation op2 : operationGraph.get(op1)){
                String dest = op2.getLabel();
                if (!graph.containsVertex(dest)){
                    graph.addVertex(dest);
                }
                graph.addEdge(source + "->" + dest, source, dest, EdgeType.DIRECTED);
            }
        }
        return graph;
    }

    public void exportGraph(File destination){
        //TODO; Implement export graph
    }
}
