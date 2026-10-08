package pe.kerolabs.pozzo.compliancehistory.domain.services;

import pe.kerolabs.pozzo.compliancehistory.domain.model.entities.ComplianceEntry;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ComplianceSummary;

import java.util.List;

/**
 * Domain service contract for evaluating member compliance history and determining
 * their reputation score and reliability level based on historical contributions.
 */
public interface ComplianceScoringService {

    /**
     * Builds the aggregated compliance summary of a member from their historical records.
     * Computes punctual, late, covered, and defaulted entries into an overall score.
     * An empty list yields a baseline summary with {@link pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ComplianceLevel#NEW}.
     *
     * @param entries list of all recorded compliance entries for the member
     * @return the computed compliance summary containing counts and score level
     */
    ComplianceSummary summarize(List<ComplianceEntry> entries);
}
