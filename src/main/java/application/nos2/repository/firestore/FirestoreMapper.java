package application.nos2.repository.firestore;

import application.nos2.domain.gift.Gift;
import application.nos2.domain.gift.Participant;
import application.nos2.domain.gift.ParticipantStatus;
import application.nos2.domain.gift.Selection;
import application.nos2.domain.gift.SelectionMethod;
import application.nos2.domain.gift.SelectionType;
import application.nos2.domain.guest.Guest;
import application.nos2.domain.guest.GuestStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class FirestoreMapper {

    private FirestoreMapper() {
    }

    public static Guest toGuest(String id, Map<String, Object> source) {
        return new Guest(
                id,
                stringValue(source.get("name")),
                stringValue(source.get("phone")),
                enumValue(source.get("status"), GuestStatus.class),
                stringValue(source.get("invite_token"))
        );
    }

    public static Gift toGift(String id, Map<String, Object> source) {
        return new Gift(
                id,
                stringValue(source.get("title")),
                decimalValue(source.get("price")),
                booleanValue(source.get("is_completed")),
                selections(source.get("selections"))
        );
    }

    public static List<Map<String, Object>> toSelectionMaps(List<Selection> selections) {
        return selections.stream()
                .map(selection -> Map.<String, Object>of(
                        "type", selection.type().name().toLowerCase(),
                        "method", selection.method().name().toLowerCase(),
                        "participants", selection.participants().stream()
                                .map(participant -> Map.<String, Object>of(
                                        "guestId", participant.guestId(),
                                        "name", participant.name(),
                                        "amount", participant.amount().toPlainString(),
                                        "status", participant.status().name().toLowerCase()
                                ))
                                .toList()
                ))
                .toList();
    }

    private static List<Selection> selections(Object rawSelections) {
        if (!(rawSelections instanceof List<?> values)) {
            return List.of();
        }

        var selections = new ArrayList<Selection>();
        for (Object value : values) {
            if (value instanceof Map<?, ?> rawSelection) {
                selections.add(new Selection(
                        enumValue(rawSelection.get("type"), SelectionType.class),
                        enumValue(rawSelection.get("method"), SelectionMethod.class),
                        participants(rawSelection.get("participants"))
                ));
            }
        }
        return List.copyOf(selections);
    }

    private static List<Participant> participants(Object rawParticipants) {
        if (!(rawParticipants instanceof List<?> values)) {
            return List.of();
        }

        var participants = new ArrayList<Participant>();
        for (Object value : values) {
            if (value instanceof Map<?, ?> rawParticipant) {
                participants.add(new Participant(
                        stringValue(rawParticipant.get("guestId")),
                        stringValue(rawParticipant.get("name")),
                        decimalValue(rawParticipant.get("amount")),
                        enumValue(rawParticipant.get("status"), ParticipantStatus.class)
                ));
            }
        }
        return List.copyOf(participants);
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private static boolean booleanValue(Object value) {
        return value instanceof Boolean current && current;
    }

    private static BigDecimal decimalValue(Object value) {
        return switch (value) {
            case null -> BigDecimal.ZERO;
            case BigDecimal decimal -> decimal;
            case Number number -> BigDecimal.valueOf(number.doubleValue());
            default -> new BigDecimal(value.toString());
        };
    }

    private static <E extends Enum<E>> E enumValue(Object value, Class<E> enumType) {
        var normalized = stringValue(value);
        if (normalized == null || normalized.isBlank()) {
            return enumType.getEnumConstants()[0];
        }
        return Enum.valueOf(enumType, normalized.trim().replace('-', '_').toUpperCase());
    }
}
