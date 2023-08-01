import java.math.BigDecimal;
import java.util.Map;

public class TicketService {
    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketService(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    public void makePayment(Map<String, Integer> ticketTypeRequests) {
        if (!
        (ticketTypeRequests)) {
            throw new IllegalArgumentException("Invalid ticket purchase requests.");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        int adultTickets = 0;
        int childTickets = 0;

        for (Map.Entry<String, Integer> entry : ticketTypeRequests.entrySet()) {
            String ticketType = entry.getKey();
            int quantity = entry.getValue();

            if ("INFANT".equals(ticketType)) {
                // Infants do not have a ticket price, no payment needed.
                continue;
            } else if ("CHILD".equals(ticketType)) {
                childTickets += quantity;
            } else if ("ADULT".equals(ticketType)) {
                adultTickets += quantity;
            }

            BigDecimal ticketPrice = getTicketPrice(ticketType);
            totalAmount = totalAmount.add(ticketPrice.multiply(BigDecimal.valueOf(quantity)));
        }

        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Invalid ticket purchase requests: No Adult tickets purchased.");
        }

        if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            // Make payment request to the TicketPaymentService
            ticketPaymentService.makePayment(totalAmount);
        }

        // Reserve seats for Adult and Child tickets
        if (adultTickets > 0 || childTickets > 0) {
            seatReservationService.reserveSeats(adultTickets + childTickets);
        }
    }

    private boolean validateTicketTypeRequests(Map<String, Integer> ticketTypeRequests) {
        // Check if total number of tickets does not exceed 20
        int totalTickets = ticketTypeRequests.values().stream().mapToInt(Integer::intValue).sum();
        if (totalTickets > 20) {
            return false;
        }

        // Check if Adult tickets are purchased when Child or Infant tickets are purchased
        if ((ticketTypeRequests.containsKey("CHILD") || ticketTypeRequests.containsKey("INFANT"))
                && !ticketTypeRequests.containsKey("ADULT")) {
            return false;
        }

        return true;
    }

    private BigDecimal getTicketPrice(String ticketType) {
        Map<String, BigDecimal> ticketPrices = Map.of(
                "INFANT", BigDecimal.ZERO,
                "CHILD", BigDecimal.TEN,
                "ADULT", BigDecimal.valueOf(20)
        );

        return ticketPrices.get(ticketType);
    }
}
