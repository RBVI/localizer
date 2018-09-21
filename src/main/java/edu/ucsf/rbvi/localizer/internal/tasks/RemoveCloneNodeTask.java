package edu.ucsf.rbvi.localizer.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.localizer.internal.model.LocalizerManager;

public class RemoveCloneNodeTask extends AbstractTask {

	@Tunable (description = "Are you sure you want to remove cloned nodes?")
	public boolean removeClones = true;
	
	private CyNetwork network;
	private LocalizerManager manager;
	
	public RemoveCloneNodeTask(LocalizerManager manager, CyNetwork network) {
		this.manager = manager;
		this.network = network;
	}
	
	@Override
	public void run(TaskMonitor monitor) throws Exception {
		if(removeClones) 
			manager.removeClonedNodes(network);
	}
}
