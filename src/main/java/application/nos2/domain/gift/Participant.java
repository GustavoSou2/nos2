package application.nos2.domain.gift;

import java.math.BigDecimal;

public record Participant(
        String guestId,
        String name,
        BigDecimal amount,
        ParticipantStatus status
) {
}
