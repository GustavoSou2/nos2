package application.nos2.api.dto;

import java.math.BigDecimal;

public record ReservationResponse(
        String giftId,
        boolean completed,
        BigDecimal reservedAmount,
        BigDecimal remainingAmount
) {
}
