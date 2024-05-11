package airline.api

import kotlin.time.Duration
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class Flight(
    val flightId: String,
    val departureTime: Instant,
    val isCancelled: Boolean = false,
    val actualDepartureTime: Instant = departureTime,
    val checkInNumber: String? = null,
    val gateNumber: String? = null,
    val plane: Plane,
    val tickets: Map<String, Ticket> = mutableMapOf(),
)

fun Flight.toFlightInfo(): FlightInfo {
    return FlightInfo(
        flightId = flightId,
        departureTime = departureTime,
        isCancelled = isCancelled,
        actualDepartureTime = actualDepartureTime,
        checkInNumber = checkInNumber,
        gateNumber = gateNumber,
        plane = plane,
    )
}

fun Flight.isDeparted() = Clock.System.now() > actualDepartureTime

fun Flight.isSeatBusy(seatNo: String) = tickets.containsKey(seatNo)

fun Flight.isSeatExists(seatNo: String) = plane.seats.contains(seatNo)

fun Flight.hasFreeSeats() = plane.seats.size > tickets.size

fun Flight.isTicketSaleOver(ticketSaleEndTime: Duration) =
    actualDepartureTime - ticketSaleEndTime < Clock.System.now()
