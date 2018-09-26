package edu.ucsf.rbvi.localizer.internal.tasks;

import java.util.ArrayList;
import java.util.HashMap;
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

	public CloneNodeTask(LocalizerManager manager, CyNetwork network, Map<CyNode, List<String>> cloneLocals,
			String id) {
		this.manager = manager;
		this.network = network;
		this.cloneLocals = cloneLocals;
		this.id = id;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {
		if (cloneLocals.isEmpty())
			monitor.showMessage(TaskMonitor.Level.INFO, "No nodes were cloned");
		Map<CyNode, List<CyNode>> nodeClonesMap = new HashMap<>();
		List<EdgeClone> addEdges = new ArrayList<>();
		CyNetworkView view = manager.getNetworkView(network);
		for (CyNode node : cloneLocals.keySet()) {
			List<CyNode> clones = new ArrayList<>();
			List<CyEdge> edges = network.getAdjacentEdgeList(node, CyEdge.Type.ANY);
			for (String localization : cloneLocals.get(node)) {
				CyNode clone = manager.cloneNode(network, view, node, id, localization);
				for(CyEdge edge : edges) {
					CyNode source = edge.getSource();
					CyNode target = edge.getTarget();
					boolean isDirected = edge.isDirected();
					if(source.getSUID() == node.getSUID()) {
						addEdges.add(new EdgeClone(clone, target, isDirected));
						if(nodeClonesMap.containsKey(target))
							for(CyNode targetClone : nodeClonesMap.get(target))
								addEdges.add(new EdgeClone(clone, targetClone, isDirected));
					} else {
						addEdges.add(new EdgeClone(source, clone, isDirected));
						if(nodeClonesMap.containsKey(source)) 
							for(CyNode sourceClone : nodeClonesMap.get(source)) 
								addEdges.add(new EdgeClone(sourceClone, clone, isDirected));
					}
				}
				addEdges.add(new EdgeClone(node, clone, false));
				clones.add(clone);
			}
			nodeClonesMap.put(node, clones);
		}
		
		for(EdgeClone addEdge : addEdges) 
			network.addEdge(addEdge.source, addEdge.target, addEdge.isDirected);
	}
}
