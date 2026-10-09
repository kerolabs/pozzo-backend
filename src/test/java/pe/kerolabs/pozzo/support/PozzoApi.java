package pe.kerolabs.pozzo.support;

import com.jayway.jsonpath.JsonPath;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Client of the REST API for the integration and acceptance tests. It calls the endpoints through
 * MockMvc, so every request goes through security, validation, the controllers and the database,
 * like a request from the mobile application.
 */
public class PozzoApi {

    /** Cutoff of the first turn of the groups the tests create: always in the future, whenever they run. */
    public static final LocalDate FIRST_CUTOFF = LocalDate.now().plusMonths(1);

    /** Nine-digit numbers that start with 9, different for every member a test creates. */
    private static final AtomicInteger NEXT_PHONE = new AtomicInteger(10_000_000);

    private final MockMvc mockMvc;
    private final RecordingSmsSender sms;
    private final JsonMapper json = JsonMapper.builder().build();

    public PozzoApi(MockMvc mockMvc, RecordingSmsSender sms) {
        this.mockMvc = mockMvc;
        this.sms = sms;
    }

    /** A member signed in from the application: the number, the session token and the account. */
    public record Member(String name, String phoneNumber, String token, String accountId) {
    }

    /** The status and the body of a response, with JSONPath to read the body. */
    public record Response(int status, String body) {

        public <T> T read(String path) {
            return JsonPath.read(body, path);
        }

        public String text(String path) {
            return String.valueOf(JsonPath.<Object>read(body, path));
        }
    }

    public String newPhoneNumber() {
        return "9" + NEXT_PHONE.getAndIncrement();
    }

    public String lastCodeSentTo(String phoneNumber) {
        return sms.lastCodeFor(phoneNumber);
    }

    /**
     * Creates an account the way the application does: requests the code, verifies it with the code that
     * arrived by SMS and completes the registration.
     */
    public Member signUp(String name) {
        var phone = newPhoneNumber();
        send(HttpMethod.POST, "/api/v1/auth/codes", null, Map.of("phoneNumber", phone));
        var verification = send(HttpMethod.POST, "/api/v1/auth/codes/verify", null,
                Map.of("phoneNumber", phone, "code", lastCodeSentTo(phone), "deviceLabel", "Pixel 8"));
        var registration = send(HttpMethod.POST, "/api/v1/auth/register", null, Map.of(
                "registrationToken", verification.text("$.registrationToken"),
                "displayName", name,
                "termsAccepted", true,
                "deviceLabel", "Pixel 8"));
        if (registration.status() != 201) {
            throw new IllegalStateException("Could not register " + name + ": " + registration.body());
        }
        return new Member(name, phone, registration.text("$.token"), registration.text("$.profile.accountId"));
    }

    /** A savings group that already started: its invitation, its cycle and the period of the first turn. */
    public record StartedGroup(String groupId, String invitationCode, String cycleId, String periodId) {
    }

    /**
     * Creates a monthly group of S/ 200 that sends the contributions to the organizer's Yape, fills it with
     * the other members through an invitation, draws the turns and starts it.
     */
    public String createGroup(Member organizer, int seats) {
        var created = post("/api/v1/groups", organizer, Map.of(
                "name", "Junta de la familia Weber",
                "contributionAmount", 200.0,
                "periodicity", "MONTHLY",
                "seats", seats,
                "firstContributionDate", FIRST_CUTOFF.toString(),
                "destination", Map.of("method", "YAPE", "phoneNumber", organizer.phoneNumber())));
        if (created.status() != 201) {
            throw new IllegalStateException("Could not create the group: " + created.body());
        }
        return created.text("$.id");
    }

    public String invitationCode(Member organizer, String groupId) {
        return post("/api/v1/groups/" + groupId + "/invitations", organizer, null).text("$.code");
    }

    public StartedGroup startedGroup(Member organizer, Member... members) {
        var groupId = createGroup(organizer, members.length + 1);
        var code = invitationCode(organizer, groupId);
        for (var member : members) {
            post("/api/v1/invitations/" + code + "/join", member, null);
        }
        post("/api/v1/groups/" + groupId + "/turns/draw", organizer, null);
        var started = post("/api/v1/groups/" + groupId + "/start", organizer, null);
        if (started.status() != 200) {
            throw new IllegalStateException("Could not start the group: " + started.body());
        }
        var cycleId = get("/api/v1/groups/" + groupId + "/cycle", organizer).text("$.id");
        var periodId = get("/api/v1/cycles/" + cycleId + "/periods/current", organizer).text("$.periodId");
        return new StartedGroup(groupId, code, cycleId, periodId);
    }

    /** The receipt of a contribution as the application sends it after reading it on the phone. */
    public static Map<String, Object> receipt(String operationNumber, double amount, String payee) {
        return Map.of("operationNumber", operationNumber, "payeeName", payee, "amount", amount,
                "paidAt", LocalDate.now().toString(), "source", "YAPE");
    }

    public Response get(String path, @Nullable Member member) {
        return call(HttpMethod.GET, path, member, null);
    }

    public Response post(String path, @Nullable Member member, @Nullable Object body) {
        return call(HttpMethod.POST, path, member, body);
    }

    public Response put(String path, @Nullable Member member, @Nullable Object body) {
        return call(HttpMethod.PUT, path, member, body);
    }

    public Response patch(String path, @Nullable Member member, @Nullable Object body) {
        return call(HttpMethod.PATCH, path, member, body);
    }

    public Response delete(String path, @Nullable Member member) {
        return call(HttpMethod.DELETE, path, member, null);
    }

    private Response call(HttpMethod method, String path, @Nullable Member member, @Nullable Object body) {
        return send(method, path, member == null ? null : member.token(), body);
    }

    private Response send(HttpMethod method, String path, @Nullable String token, @Nullable Object body) {
        try {
            var request = MockMvcRequestBuilders.request(method, path).accept(MediaType.APPLICATION_JSON);
            if (token != null) {
                request.header("Authorization", "Bearer " + token);
            }
            if (body != null) {
                request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
            }
            var response = mockMvc.perform(request).andReturn().getResponse();
            return new Response(response.getStatus(), response.getContentAsString(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("The request " + method + " " + path + " failed", e);
        }
    }
}
