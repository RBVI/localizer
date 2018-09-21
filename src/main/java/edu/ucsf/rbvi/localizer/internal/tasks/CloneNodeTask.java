package edu.ucsf.rbvi.localizer.internal.tasks;

import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.localizer.internal.model.LocalizerManager;

public class CloneNodeTask extends AbstractTask {
	private LocalizerManager manager;
	private CyNetwork network;
	private Map<CyNode, List<String>> cloneLocals; 
	private final String id;

	public CloneNodeTask(LocalizerManager manager, CyNetwork network, Map<CyNode, List<String>> cloneLocals, String id) {
		this.manager = manager;
		this.network = network;
		this.cloneLocals = cloneLocals;
		this.id = id;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {
		if(cloneLocals.isEmpty()) 
			monitor.showMessage(TaskMonitor.Level.INFO, "No nodes were cloned");
		CyNetworkView view = manager.getNetworkView(network);
		for(CyNode node : cloneLocals.keySet()) {
			for(String localization : cloneLocals.get(node)) {
				CyNode clone = manager.cloneNode(network, view, node, id, localization);
				List<CyEdge> edges = network.getAdjacentEdgeList(node, CyEdge.Type.ANY);
				for(CyEdge edge : edges) {
					if(edge.getSource().getSUID() == node.getSUID())
						network.addEdge(clone, edge.getTarget(), edge.isDirected());
					else if(edge.getTarget().getSUID() == node.getSUID())
						network.addEdge(edge.getSource(), clone, edge.isDirected());
					else 
						System.out.println("Something went wrong copying edges!"); //safe to remove?
				}
			}
		}
	}
}
