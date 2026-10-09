package pe.kerolabs.pozzo.acceptance;

import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.support.PozzoApi.Member;
import pe.kerolabs.pozzo.support.PozzoApi.Response;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * What the steps of one scenario share: the members by name, the group they work with and the last
 * response of the service. Spring hands the steps a proxy of it, so everything goes through methods.
 */
public class ScenarioState {

    private final Map<String, Member> members = new HashMap<>();
    private final Map<String, String> contributions = new HashMap<>();
    private @Nullable Response response;
    private @Nullable String phoneNumber;
    private @Nullable Instant codeRequestedAt;
    private @Nullable String registrationToken;
    private @Nullable String organizer;
    private @Nullable String groupId;
    private @Nullable String invitationCode;
    private @Nullable String cycleId;
    private @Nullable String shareToken;

    public void add(Member member) {
        members.put(member.name(), member);
    }

    public Member member(String name) {
        var member = members.get(name);
        if (member == null) {
            throw new IllegalStateException(name + " has no account in this scenario");
        }
        return member;
    }

    public Iterable<Member> members() {
        return members.values();
    }

    public void keepResponse(Response response) {
        this.response = response;
    }

    public Response response() {
        return required(response, "No request was made in this scenario");
    }

    public void keepContribution(String memberName, String contributionId) {
        contributions.put(memberName, contributionId);
    }

    public String contributionOf(String memberName) {
        return required(contributions.get(memberName), memberName + " registered no contribution");
    }

    /** The group the scenario works with and the member who organizes it. */
    public void keepGroup(String organizerName, String groupId, @Nullable String invitationCode) {
        this.organizer = organizerName;
        this.groupId = groupId;
        this.invitationCode = invitationCode;
    }

    public Member organizer() {
        return member(required(organizer, "The scenario has no group"));
    }

    public String groupId() {
        return required(groupId, "The scenario has no group");
    }

    public String invitationCode() {
        return required(invitationCode, "The group has no invitation");
    }

    public void keepCycle(String cycleId) {
        this.cycleId = cycleId;
    }

    public String cycleId() {
        return required(cycleId, "The group has not started");
    }

    public void keepPhoneNumber(String phoneNumber, @Nullable Instant codeRequestedAt) {
        this.phoneNumber = phoneNumber;
        this.codeRequestedAt = codeRequestedAt;
    }

    public String phoneNumber() {
        return required(phoneNumber, "The scenario has no phone number");
    }

    public Instant codeRequestedAt() {
        return required(codeRequestedAt, "No code was requested");
    }

    public void keepRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public String registrationToken() {
        return required(registrationToken, "The number was not verified");
    }

    public void keepShareToken(String shareToken) {
        this.shareToken = shareToken;
    }

    public String shareToken() {
        return required(shareToken, "The history was not shared");
    }

    private static <T> T required(@Nullable T value, String message) {
        if (value == null) {
            throw new IllegalStateException(message);
        }
        return value;
    }
}
