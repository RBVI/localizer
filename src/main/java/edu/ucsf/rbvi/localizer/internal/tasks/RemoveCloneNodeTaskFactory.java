package edu.ucsf.rbvi.localizer.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.localizer.internal.model.LocalizerManager;

public class RemoveCloneNodeTaskFactory extends AbstractNetworkTaskFactory implements TaskFactory {

	public final static String REMOVE_CLONED_NODES = "Remove cloned nodes";

	private LocalizerManager manager;

	public RemoveCloneNodeTaskFactory(LocalizerManager manager) {
		this.manager = manager;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new RemoveCloneNodeTask(manager, network));
	}

	@Override
	public boolean isReady(CyNetwork network) {
		return LocalizerManager.hasClonedNodes(network);
	}

	@Override
	public TaskIterator createTaskIterator() {
		return createTaskIterator(manager.getCurrentNetwork());
	}

	@Override
	public boolean isReady() {
		return isReady(manager.getCurrentNetwork());
	}

	public String getTitle() {
		return REMOVE_CLONED_NODES;
	}
}
