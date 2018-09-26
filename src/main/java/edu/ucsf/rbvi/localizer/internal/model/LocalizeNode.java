package edu.ucsf.rbvi.localizer.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;

public class LocalizeNode {
	private static final int DEFAULT_CONFIDENCE = 0;
	private static final int MIN_CONFIDENCE = 1;

	private CyRow rowInfo;
	private Map<String, Integer> confidences;
	private List<String> highestConfidences;
	private int maxConfidence;

	public LocalizeNode(Map<String, Integer> confidences, CyRow rowInfo, List<String> highestConfidences) {
		this.confidences = confidences;
		this.rowInfo = rowInfo;
		this.highestConfidences = highestConfidences;
		if (this.confidences == null)
			this.confidences = new HashMap<>();
		if (this.highestConfidences == null)
			this.highestConfidences = new ArrayList<>();
		if (!confidences.isEmpty() && !highestConfidences.isEmpty())
			maxConfidence = confidences.get(highestConfidences.get(0));
		else
			maxConfidence = DEFAULT_CONFIDENCE;
	}

	public LocalizeNode(Map<String, Integer> confidences, CyRow rowInfo) {
		this.confidences = confidences;
		this.rowInfo = rowInfo;
		if (confidences == null || confidences.isEmpty())
			confidences = new HashMap<>();
		highestConfidences = new ArrayList<>();

		maxConfidence = MIN_CONFIDENCE;
		if (!confidences.isEmpty()) {
			for (String key : confidences.keySet()) {
				if (confidences.get(key) > maxConfidence) {
					maxConfidence = confidences.get(key);
					highestConfidences.clear();
				}
				if (confidences.get(key) >= maxConfidence)
					highestConfidences.add(key);
			}
		}
	}

	public int getHighestConfidenceSize() {
		if (highestConfidences == null)
			return 0;
		return highestConfidences.size();
	}

	public int getMaxConfidence() {
		return maxConfidence;
	}

	public List<String> getLocalizations() {
		return highestConfidences;
	}

	public Integer getConfidence(String localization) {
		if (confidences == null || confidences.isEmpty() || !confidences.containsKey(localization))
			return DEFAULT_CONFIDENCE;
		return confidences.get(localization);
	}

	public boolean isMaxConfidence(String localization) {
		if (highestConfidences != null && !highestConfidences.isEmpty())
			return highestConfidences.contains(localization);
		return false;
	}

	public boolean hasExclusiveConfidence() {
		if (highestConfidences != null && !highestConfidences.isEmpty())
			return highestConfidences.size() == 1;
		return false;
	}

	public void setLocalization(String id, String localization) {
		if (localization == null || localization.length() == 0)
			return;
		rowInfo.set(id, localization.substring(id.length() + 1));
	}

	public long getNodeSUID() {
		return (Long) rowInfo.getRaw(CyIdentifiable.SUID);
	}
}
