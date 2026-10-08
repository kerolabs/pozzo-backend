package pe.kerolabs.pozzo.compliancehistory.domain.services;

import pe.kerolabs.pozzo.compliancehistory.domain.model.entities.ComplianceEntry;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ComplianceSummary;

import java.util.List;

/**
 * Computes the summary and the level of a member from the compliance entries.
 */
public interface ComplianceScoringService {

    /**
     * Builds the summary of a member from all of their entries.
     * An empty list yields a summary with the NEW level.
     */
    ComplianceSummary summarize(List<ComplianceEntry> entries);
}
