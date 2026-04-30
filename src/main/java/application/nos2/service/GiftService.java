package application.nos2.service;

import application.nos2.api.dto.GiftResponse;
import application.nos2.api.dto.ReservationResponse;
import application.nos2.api.dto.ReserveGiftRequest;
import application.nos2.domain.gift.Gift;
import application.nos2.domain.gift.Participant;
import application.nos2.domain.gift.Selection;
import application.nos2.domain.gift.SelectionMethod;
import application.nos2.domain.gift.SelectionType;
import application.nos2.domain.guest.Guest;
import application.nos2.repository.GiftRepository;
import application.nos2.repository.GuestRepository;
import application.nos2.repository.firestore.FirestoreMapper;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;

@Service
public class GiftService {

    private final Firestore firestore;
    private final GiftRepository giftRepository;
    private final GuestRepository guestRepository;
    private final ExecutorService virtualThreadExecutor;

    public GiftService(
            Firestore firestore,
            GiftRepository giftRepository,
            GuestRepository guestRepository,
            ExecutorService virtualThreadExecutor
    ) {
        this.firestore = firestore;
        this.giftRepository = giftRepository;
        this.guestRepository = guestRepository;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    public CompletableFuture<List<GiftResponse>> listGifts() {
        return CompletableFuture.supplyAsync(() -> giftRepository.findAll().stream()
                .map(this::toResponse)
                .toList(), virtualThreadExecutor);
    }

    public CompletableFuture<ReservationResponse> reserveGift(String giftId, ReserveGiftRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            var guest = guestRepository.findById(request.guestId())
                    .orElseThrow(() -> new NoSuchElementException("Guest not found: " + request.guestId()));

            try {
                return firestore.runTransaction(transaction -> reserveInsideTransaction(transaction, giftId, request, guest))
                        .get();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Gift reservation interrupted.", exception);
            } catch (ExecutionException exception) {
                throw new IllegalStateException("Failed to reserve gift.", exception);
            }
        }, virtualThreadExecutor);
    }

    private ReservationResponse reserveInsideTransaction(
            Transaction transaction,
            String giftId,
            ReserveGiftRequest request,
            Guest guest
    ) throws Exception {
        var documentReference = giftRepository.document(giftId);
        var snapshot = transaction.get(documentReference).get();
        if (!snapshot.exists()) {
            throw new NoSuchElementException("Gift not found: " + giftId);
        }

        var gift = FirestoreMapper.toGift(snapshot.getId(), snapshot.getData());
        validateRequest(gift, request);

        var updatedSelections = mergeSelections(gift, request, guest);
        var completed = resolveCompletion(gift, request, updatedSelections);
        var reservedAmount = calculateReservedAmount(updatedSelections);

        if (request.method() == SelectionMethod.MONEY && reservedAmount.compareTo(gift.price()) > 0) {
            throw new IllegalArgumentException("Contributions exceed gift total price.");
        }

        transaction.update(documentReference, "selections", FirestoreMapper.toSelectionMaps(updatedSelections));
        transaction.update(documentReference, "is_completed", completed);

        return new ReservationResponse(
                giftId,
                completed,
                reservedAmount,
                gift.price().subtract(reservedAmount).max(BigDecimal.ZERO)
        );
    }

    private void validateRequest(Gift gift, ReserveGiftRequest request) {
        if (gift.isCompleted()) {
            throw new IllegalStateException("Gift is already completed.");
        }

        if (request.type() == SelectionType.INDIVIDUAL && request.method() == SelectionMethod.PHYSICAL
                && request.amount().compareTo(gift.price()) != 0) {
            throw new IllegalArgumentException("Physical individual reservation must match the full gift price.");
        }
    }

    private List<Selection> mergeSelections(Gift gift, ReserveGiftRequest request, Guest guest) {
        var selections = new ArrayList<>(gift.selections());
        var participant = new Participant(guest.id(), guest.name(), request.amount(), request.status());

        var existingSelection = selections.stream()
                .filter(selection -> selection.type() == request.type() && selection.method() == request.method())
                .findFirst();

        if (existingSelection.isPresent()) {
            var current = existingSelection.get();
            var participants = new ArrayList<>(current.participants());
            participants.removeIf(item -> item.guestId().equals(guest.id()));
            participants.add(participant);

            if (request.type() == SelectionType.GROUP && participants.size() > 4) {
                throw new IllegalArgumentException("Group gifts allow at most 4 participants.");
            }

            selections.remove(current);
            selections.add(new Selection(current.type(), current.method(), List.copyOf(participants)));
            return List.copyOf(selections);
        }

        if (request.type() == SelectionType.GROUP) {
            return appendSelection(selections, request, List.of(participant));
        }

        if (!selections.isEmpty()) {
            throw new IllegalStateException("Individual reservation does not allow concurrent selections.");
        }

        return appendSelection(selections, request, List.of(participant));
    }

    private List<Selection> appendSelection(
            List<Selection> selections,
            ReserveGiftRequest request,
            List<Participant> participants
    ) {
        if (request.type() == SelectionType.GROUP && participants.size() > 4) {
            throw new IllegalArgumentException("Group gifts allow at most 4 participants.");
        }

        selections.add(new Selection(request.type(), request.method(), List.copyOf(participants)));
        return List.copyOf(selections);
    }

    private boolean resolveCompletion(Gift gift, ReserveGiftRequest request, List<Selection> selections) {
        if (request.method() == SelectionMethod.PHYSICAL && request.type() == SelectionType.INDIVIDUAL) {
            return true;
        }

        if (request.method() == SelectionMethod.MONEY) {
            return calculateReservedAmount(selections).compareTo(gift.price()) >= 0;
        }

        return gift.isCompleted();
    }

    private BigDecimal calculateReservedAmount(List<Selection> selections) {
        return selections.stream()
                .filter(selection -> selection.method() == SelectionMethod.MONEY)
                .flatMap(selection -> selection.participants().stream())
                .map(Participant::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private GiftResponse toResponse(Gift gift) {
        var reservedAmount = calculateReservedAmount(gift.selections());
        return new GiftResponse(
                gift.id(),
                gift.title(),
                gift.price(),
                gift.isCompleted(),
                reservedAmount,
                gift.price().subtract(reservedAmount).max(BigDecimal.ZERO),
                gift.selections().stream()
                        .map(selection -> new GiftResponse.SelectionResponse(
                                selection.type(),
                                selection.method(),
                                selection.participants().stream()
                                        .map(participant -> new GiftResponse.ParticipantResponse(
                                                participant.guestId(),
                                                participant.name(),
                                                participant.amount(),
                                                participant.status()
                                        ))
                                        .toList()
                        ))
                        .toList()
        );
    }
}
