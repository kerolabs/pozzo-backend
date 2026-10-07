package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.transform;

import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.GroupRules;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.DestinationResource;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.RulesResource;

/**
 * Converts {@link GroupRules} into {@link RulesResource}.
 */
public class RulesResourceFromEntityAssembler {

    private static final String PERU_PREFIX = "+51";

    /**
     * @param includeDestination false to hide the destination number, e.g. before joining a group
     */
    public static RulesResource toResourceFromEntity(GroupRules rules, boolean includeDestination) {
        var destination = includeDestination && rules.hasDestination()
                ? new DestinationResource(rules.destination().method(),
                        rules.destination().phoneNumber().replace(PERU_PREFIX, ""))
                : null;
        return new RulesResource(
                rules.contribution().amount(),
                rules.contribution().currency(),
                rules.periodicity(),
                rules.seats(),
                rules.firstContributionDate(),
                rules.cutoffDay(),
                rules.pot().amount(),
                destination);
    }
}
