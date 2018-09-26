package edu.ucsf.rbvi.localizer.internal.tasks;

import org.cytoscape.model.CyNode;

public class EdgeClone {
	protected final CyNode source;
	protected final CyNode target;
	protected final boolean isDirected;
	
	public EdgeClone(CyNode source, CyNode target, boolean isDirected) {
		this.source = source;
		this.target = target;
		this.isDirected = isDirected;
	}
}
