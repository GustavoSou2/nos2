package application.nos2.domain.gift;

import java.math.BigDecimal;
import java.util.List;

public record Gift(
        String id,
        String title,
        BigDecimal price,
        boolean isCompleted,
        List<Selection> selections
) {
}
