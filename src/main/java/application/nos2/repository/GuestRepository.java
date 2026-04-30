package application.nos2.repository;

import application.nos2.domain.guest.Guest;

import java.util.Optional;

public interface GuestRepository {

    Optional<Guest> findById(String guestId);
}
