package edu.ucsf.rbvi.localizer.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.localizer.internal.model.LocalizerManager;

public class LocalizeTaskFactory extends AbstractNetworkTaskFactory implements TaskFactory {
	private static final String TITLE = "Localize network";
	private final LocalizerManager manager;
	
	public LocalizeTaskFactory(LocalizerManager manager) {
		this.manager = manager;
	}

	public String getTitle() {
		return TITLE;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new LocalizeTask(manager, network));
	}

	@Override
	public boolean isReady(CyNetwork network) {
		return LocalizerManager.isStringNetwork(network);
	}

	@Override
	public TaskIterator createTaskIterator() {
		return createTaskIterator(manager.getCurrentNetwork());
	}

	@Override
	public boolean isReady() {
		return isReady(manager.getCurrentNetwork());
	}
}
