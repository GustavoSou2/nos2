package application.nos2.api;

import application.nos2.api.dto.GiftResponse;
import application.nos2.api.dto.ReservationResponse;
import application.nos2.api.dto.ReserveGiftRequest;
import application.nos2.service.GiftService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/gifts")
public class GiftController {

    private final GiftService giftService;

    public GiftController(GiftService giftService) {
        this.giftService = giftService;
    }

    @GetMapping
    public CompletableFuture<List<GiftResponse>> listGifts() {
        return giftService.listGifts();
    }

    @PostMapping("/{giftId}/reservations")
    public CompletableFuture<ReservationResponse> reserveGift(
            @PathVariable String giftId,
            @Valid @RequestBody ReserveGiftRequest request
    ) {
        return giftService.reserveGift(giftId, request);
    }
}
