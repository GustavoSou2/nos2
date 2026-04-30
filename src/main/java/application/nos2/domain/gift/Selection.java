package application.nos2.domain.gift;

import java.util.List;

public record Selection(
        SelectionType type,
        SelectionMethod method,
        List<Participant> participants
) {
}
