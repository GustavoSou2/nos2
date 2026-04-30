package application.nos2.repository.firestore;

import application.nos2.domain.gift.Gift;
import application.nos2.repository.GiftRepository;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class FirestoreGiftRepository implements GiftRepository {

    private static final String COLLECTION = "gifts";

    private final Firestore firestore;

    public FirestoreGiftRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public List<Gift> findAll() {
        try {
            return firestore.collection(COLLECTION).get().get().getDocuments().stream()
                    .map(snapshot -> FirestoreMapper.toGift(snapshot.getId(), snapshot.getData()))
                    .toList();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gift listing interrupted.", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Failed to list gifts from Firestore.", exception);
        }
    }

    @Override
    public Optional<Gift> findById(String giftId) {
        try {
            var snapshot = document(giftId).get().get();
            if (!snapshot.exists()) {
                return Optional.empty();
            }
            return Optional.of(FirestoreMapper.toGift(snapshot.getId(), snapshot.getData()));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gift lookup interrupted.", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Failed to load gift from Firestore.", exception);
        }
    }

    @Override
    public DocumentReference document(String giftId) {
        return firestore.collection(COLLECTION).document(giftId);
    }
}
