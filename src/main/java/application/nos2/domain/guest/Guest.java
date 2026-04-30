package application.nos2.domain.guest;

public record Guest(
        String id,
        String name,
        String phone,
        GuestStatus status,
        String inviteToken
) {
}
