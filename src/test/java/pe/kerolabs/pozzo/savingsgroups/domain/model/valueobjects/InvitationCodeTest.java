package pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("InvitationCode: the code a member types to join a savings group")
class InvitationCodeTest {

    @RepeatedTest(20)
    void startsWithTheInitialsOfTheGroupAndHasNoConfusingCharacters() {
        var code = InvitationCode.random("Junta de la familia Weber").value();

        assertThat(code).matches("JF-[23456789ABCDEFGHJKLMNPQRSTUVWXYZ]{4}");
    }

    @Test
    void usesTheFirstLettersWhenTheNameHasOneWord() {
        assertThat(InvitationCode.random("Ahorro").value()).startsWith("AH-");
        assertThat(InvitationCode.random("").value()).startsWith("PZ-");
    }

    @Test
    void readsACodeTypedWithoutTheDashOrInLowerCase() {
        assertThat(InvitationCode.parse("jf 7k4m").value()).isEqualTo("JF-7K4M");
        assertThat(InvitationCode.parse("JF7K4M").value()).isEqualTo("JF-7K4M");
    }

    @Test
    void rejectsACodeWithTheWrongLength() {
        assertThatThrownBy(() -> InvitationCode.parse("JF-7K4")).isInstanceOf(IllegalArgumentException.class);
    }
}
