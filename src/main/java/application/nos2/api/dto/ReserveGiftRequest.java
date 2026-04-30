package application.nos2.api.dto;

import application.nos2.domain.gift.ParticipantStatus;
import application.nos2.domain.gift.SelectionMethod;
import application.nos2.domain.gift.SelectionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ReserveGiftRequest(
        @NotBlank String guestId,
        @NotNull SelectionType type,
        @NotNull SelectionMethod method,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull ParticipantStatus status
) {
}
