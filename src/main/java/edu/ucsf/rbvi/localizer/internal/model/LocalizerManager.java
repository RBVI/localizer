package edu.ucsf.rbvi.localizer.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

public class LocalizerManager {
	public static final String CLONE_ID = "Is Clone";
	// static string-network validation variables
	public static final String CONFIDENCE = "confidence score";
	public static final String CANONICAL = "canonical name";
	public static final String ID = "@id";
	public static final String SEQUENCE = "sequence";
	public static final String SPECIES = "species";
	public static final String SCORE = "score";

	private final CyServiceRegistrar registrar;

	public LocalizerManager(CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}

	/**
	 * network is a string network only if the network table has a confidence score, the node table has 
	 * "@id", "species", "canonical name", and "sequence" columns, and the edge table has a "score" column 
	 * @param network to validate whether or not it is a string network
	 * @return true if network is a string network
	 */
	public static boolean isStringNetwork(CyNetwork network) {
		// This is a string network only if we have a confidence score in the network table,
		// "@id", "species", "canonical name", and "sequence" columns in the node table, and
		// a "score" column in the edge table
		if (network == null || network.getRow(network).get(CONFIDENCE, Double.class) == null)
			return false;
		CyTable nodeTable = network.getDefaultNodeTable();
		if (nodeTable.getColumn(ID) == null)
			return false;
		if (nodeTable.getColumn(SPECIES) == null)
			return false;
		if (nodeTable.getColumn(CANONICAL) == null)
			return false;
		if (nodeTable.getColumn(SEQUENCE) == null)
			return false;
		CyTable edgeTable = network.getDefaultEdgeTable();
		if (edgeTable.getColumn(SCORE) == null)
			return false;
		return true;
	}

	/**
	 * a network has cloned nodes if the user has previously decided to clone the network's nodes based
	 * on a certain localization
	 * @param network
	 * @return
	 */
	public static boolean hasClonedNodes(CyNetwork network) {
		if(network == null || network.getDefaultNodeTable() == null)
			return false;
		return network.getDefaultNodeTable().getColumn(CLONE_ID) != null;
	}

	public void flush(CyNetworkView view) {
		registrar.getService(CyEventHelper.class).flushPayloadEvents();
		view.updateView();
	}

	public CyNetwork getCurrentNetwork() {
		return registrar.getService(CyApplicationManager.class).getCurrentNetwork();
	}

	public CyNetworkView getNetworkView(CyNetwork network) {
		Set<CyNetworkView> views = registrar.getService(CyNetworkViewManager.class).getNetworkViewSet();
		for(CyNetworkView view : views) 
			if(view.getModel().getSUID() == network.getSUID())
				return view;
		return null;
	}

	private List<String> getColumnNamesByID(CyNetwork network, String id) {
		List<String> columnsID = new ArrayList<>();
		for(CyColumn col : network.getDefaultNodeTable().getColumns()) {
			String name = col.getName();
			if(name.contains(id) && !name.equals(id))
				columnsID.add(name);
		}
		return columnsID;
	}

	public Map<CyNode, LocalizeNode> getLocalizeNodes(CyNetwork network, String id) {
		Map<CyNode, LocalizeNode> localizeNodes = new HashMap<>();
		List<String> localizations = getColumnNamesByID(network, id);
		CyTable nodeTable = network.getDefaultNodeTable();

		for(CyNode node : network.getNodeList()) {
			Map<String, Integer> confidences = new HashMap<>(); 
			CyRow nodeRow = nodeTable.getRow(node.getSUID());
			for(String localization : localizations) {
				if(nodeRow.getRaw(localization) != null) {
					long value = (Long) nodeRow.get(localization, Long.class);
					int confidence = (int) value;
					confidences.put(localization, confidence);
				}  else {
					confidences.put(localization, 0);
				}
			}

			LocalizeNode localizeNode = new LocalizeNode(confidences, nodeRow);
			localizeNodes.put(node, localizeNode);
		}
		return localizeNodes;
	}

	public CyNode cloneNode(CyNetwork network, CyNetworkView view, CyNode node, String id, String localization) {
		CyNode clone = network.addNode();
		CyTable nodeTable = network.getDefaultNodeTable();
		CyRow nodeRow = nodeTable.getRow(clone.getSUID());
		for(CyColumn column : nodeTable.getColumns()) {
			String name = column.getName();
			if(!name.equals(CyNetwork.SUID)) {
				nodeRow.set(name, nodeTable.getRow(node.getSUID()).getRaw(name));
			}
		}

		if(view != null) {
			VisualStyle currentStyle = registrar.getService(VisualMappingManager.class).getVisualStyle(view);
			currentStyle.apply(nodeRow, view);
		}

		if(nodeTable.getColumn(id) != null) {
			nodeRow.set(id, localization.substring(id.length() + 1));
			nodeRow.set(CLONE_ID, true);
		}
		return clone;
	}

	public void removeClonedNodes(CyNetwork network) {
		if(!hasClonedNodes(network))
			return;
		CyTable nodeTable = network.getDefaultNodeTable();
		Collection<CyNode> remove = new ArrayList<>();
		for(CyNode node : network.getNodeList()) {
			CyRow nodeRow = nodeTable.getRow(node.getSUID());
			if(nodeRow.getRaw(CLONE_ID) != null) {
				boolean isClone = nodeRow.get(CLONE_ID, Boolean.class);
				if(isClone) {
					List<CyEdge> clonedEdges = network.getAdjacentEdgeList(node, CyEdge.Type.ANY);
					network.removeEdges(clonedEdges);
					remove.add(node);
				}
			}
		}
		network.removeNodes(remove);
		nodeTable.deleteColumn(CLONE_ID);
		CyNetworkView view = getNetworkView(network);
		if(view != null)
			flush(view);
	}
}
