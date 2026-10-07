package pe.kerolabs.pozzo.savingsgroups.infrastructure.turns;

import org.springframework.stereotype.Service;
import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.SavingsGroup;
import pe.kerolabs.pozzo.savingsgroups.domain.model.entities.Membership;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.TurnMethod;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.TurnSlot;
import pe.kerolabs.pozzo.savingsgroups.domain.services.TurnAssignmentService;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Draws the collection order with a published seed.
 *
 * <p>The members are sorted by the moment they joined, and then shuffled with a random generator
 * initialized from the SHA-256 of the seed. Anyone with the seed and the member list gets the same
 * order, so the group can check that the organizer did not choose it.</p>
 */
@Service
public class SeededTurnAssignmentService implements TurnAssignmentService {

    @Override
    public List<TurnSlot> drawTurns(SavingsGroup group, String seed) {
        var members = new ArrayList<>(group.activeMemberships());
        members.sort(Comparator.comparing(Membership::getJoinedAt).thenComparing(Membership::getId));
        Collections.shuffle(members, new Random(seedToLong(seed)));
        return IntStream.range(0, members.size())
                .mapToObj(index -> new TurnSlot(index + 1, members.get(index).getId(), TurnMethod.DRAW))
                .toList();
    }

    @Override
    public List<TurnSlot> agreedTurns(SavingsGroup group, List<UUID> order) {
        return IntStream.range(0, order.size())
                .mapToObj(index -> new TurnSlot(index + 1, order.get(index), TurnMethod.AGREED))
                .toList();
    }

    private static long seedToLong(String seed) {
        try {
            var digest = MessageDigest.getInstance("SHA-256").digest(seed.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(digest).getLong();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
