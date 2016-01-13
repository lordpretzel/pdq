package uk.ac.ox.cs.pdq.ui.prefuse.utils;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.ui.prefuse.types.EdgeTypes;
import uk.ac.ox.cs.pdq.ui.prefuse.types.PathTypes;

public class Utils {
	
	public static Node addNode(Graph graph, SearchNode searchNode) {
		Node n = graph.addNode();
		n.set("id", searchNode.getId());
		n.set("type", searchNode.getStatus());
		n.set("pathToSuccess", PathTypes.NOSUCCESSFULPATH);
		n.set("data", searchNode);
		n.set("isCollapsed", false);
		n.set("hidePointerEdges", false);
		return n;
	}
	
	public static Edge addEdge(Graph graph, SearchNode source, SearchNode target, EdgeTypes type) {
		Node s = toNode(graph, source); 
		Node t = toNode(graph, target); 
		
		if(s == null || t == null) {
			throw new java.lang.IllegalArgumentException();
		}
		Edge edge = graph.addEdge(s, t);
		edge.set("type", type);
		return edge;
	}
	
	
	public static Node toNode(Graph graph, SearchNode searchNode) {
		for(int n = 0; n < graph.getNodeCount(); ++n) {
			Node node = graph.getNode(n);
			if((int)node.get("id") == searchNode.getId()) {
				return node;
			}
		}
		return null;
	}
	
	
	public static Node toNode(Graph graph, Integer id) {
		for(int n = 0; n < graph.getNodeCount(); ++n) {
			Node node = graph.getNode(n);
			if((int)node.get("id") == id) {
				return node;
			}
		}
		return null;
	}
	
	public static void modifyNodeProperty(Graph graph, Integer nodeId, String attribute, Object value) {
		for(int n = 0; n < graph.getNodeCount(); ++n) {
			Node node = graph.getNode(n);
			if ((int) node.get("id") == nodeId) {
				node.set(attribute, value);
			}
		}
	}
	
	public static void modifyNodeProperty(Graph graph, Collection<Integer> nodeIds, String attribute, Object value) {
		for(int n = 0; n < graph.getNodeCount(); ++n) {
			Node node = graph.getNode(n);
			if(nodeIds.contains((int)node.get("id"))) {
				node.set(attribute, value);
			}
		}
	}
	
	
	public static List<Node> toNode(Graph graph, List<Integer> nodeIds) {
		if(nodeIds == null) {
			return null;
		}
		List<Node> n = new ArrayList<>();
		for(Integer id:nodeIds) {
			n.add(toNode(graph, id));
		}
		return n;
	}
	
	public static List<NodeItem> toNodeItem(Visualization visualization, String nodeGroup, Graph graph, List<Integer> nodeIds) {
		if(nodeIds == null) {
			return null;
		}
		List<NodeItem> n = new ArrayList<>();
		for(Integer id:nodeIds) {
			Node node = toNode(graph, id);
			VisualItem item = visualization.getVisualItem(nodeGroup, node);
			n.add((NodeItem) item);
		}
		return n;
	}
	
	
	public static AggregateTable updateAggregateTable(Visualization visualization, String nodeGroup, Graph graph, AggregateTable at, List<List<Integer>> nodes, Boolean isVisible) {
		at.clear();
		int i = 0;
		for (List<Integer> list:nodes) {
			AggregateItem aitem = (AggregateItem)at.addItem();
			aitem.setVisible(isVisible);
			aitem.setInt("id", i++);
			for(Integer node:list) {
				VisualItem item = visualization.getVisualItem(nodeGroup, toNode(graph, node));
				aitem.addItem(item);
			}	
		}
		return at;
	}

}
