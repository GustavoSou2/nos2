package application.nos2.repository.firestore;

import application.nos2.domain.guest.Guest;
import application.nos2.repository.GuestRepository;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class FirestoreGuestRepository implements GuestRepository {

    private static final String COLLECTION = "guests";

    private final Firestore firestore;

    public FirestoreGuestRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public Optional<Guest> findById(String guestId) {
        try {
            var snapshot = firestore.collection(COLLECTION).document(guestId).get().get();
            if (!snapshot.exists()) {
                return Optional.empty();
            }
            return Optional.of(FirestoreMapper.toGuest(snapshot.getId(), snapshot.getData()));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Guest lookup interrupted.", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Failed to load guest from Firestore.", exception);
        }
    }
}
