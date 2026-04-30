package application.nos2.api.dto;

import application.nos2.domain.gift.ParticipantStatus;
import application.nos2.domain.gift.SelectionMethod;
import application.nos2.domain.gift.SelectionType;

import java.math.BigDecimal;
import java.util.List;

public record GiftResponse(
        String id,
        String title,
        BigDecimal price,
        boolean isCompleted,
        BigDecimal reservedAmount,
        BigDecimal remainingAmount,
        List<SelectionResponse> selections
) {

    public record SelectionResponse(
            SelectionType type,
            SelectionMethod method,
            List<ParticipantResponse> participants
    ) {
    }

    public record ParticipantResponse(
            String guestId,
            String name,
            BigDecimal amount,
            ParticipantStatus status
    ) {
    }
}
