package application.nos2.repository;

import application.nos2.domain.gift.Gift;
import com.google.cloud.firestore.DocumentReference;

import java.util.List;
import java.util.Optional;

public interface GiftRepository {

    List<Gift> findAll();

    Optional<Gift> findById(String giftId);

    DocumentReference document(String giftId);
}
