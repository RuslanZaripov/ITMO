package airline.api

import airline.AirlineMessage
import kotlinx.datetime.Instant

sealed class AirlineManagementMessages(
    open val flightId: String,
    open val departureTime: Instant,
) : AirlineMessage {
    abstract fun change(flight: Flight): Flight

    abstract fun message(): String
}

class Schedule(
    override val flightId: String,
    override val departureTime: Instant,
    val plane: Plane,
) : AirlineManagementMessages(flightId, departureTime) {
    override fun change(flight: Flight): Flight {
        return flight.copy(plane = plane)
    }

    override fun message() = "Flight $flightId at $departureTime is scheduled"
}

class Delay(
    override val flightId: String,
    override val departureTime: Instant,
    private val actualDepartureTime: Instant,
) : AirlineManagementMessages(flightId, departureTime) {
    override fun change(flight: Flight): Flight {
        return flight.copy(
            actualDepartureTime = actualDepartureTime,
            isCancelled = false,
        )
    }

    override fun message() = "Flight $flightId at $departureTime is delayed"
}

class Cancel(
    override val flightId: String,
    override val departureTime: Instant,
) : AirlineManagementMessages(flightId, departureTime) {
    override fun change(flight: Flight): Flight {
        return flight.copy(isCancelled = true)
    }

    override fun message() = "Flight $flightId at $departureTime is cancelled"
}

class CheckInNumber(
    override val flightId: String,
    override val departureTime: Instant,
    private val checkInNumber: String,
) : AirlineManagementMessages(flightId, departureTime) {
    override fun change(flight: Flight): Flight {
        return flight.copy(checkInNumber = checkInNumber)
    }

    override fun message() = "Flight $flightId at $departureTime has check-in number $checkInNumber"
}

data class GateNumber(
    override val flightId: String,
    override val departureTime: Instant,
    val gateNumber: String,
) : AirlineManagementMessages(flightId, departureTime) {
    override fun change(flight: Flight): Flight {
        return flight.copy(gateNumber = gateNumber)
    }

    override fun message() = "Flight $flightId at $departureTime has gate number $gateNumber"
}

data class BuyTicket(
    override val flightId: String,
    override val departureTime: Instant,
    val ticket: Ticket,
) : AirlineManagementMessages(flightId, departureTime) {
    override fun change(flight: Flight): Flight {
        val newTickets = flight.tickets.toMutableMap()
        newTickets[ticket.seatNo] = ticket
        return flight.copy(tickets = newTickets)
    }

    override fun message() = "Seat ${ticket.seatNo} for flight $flightId at $departureTime is bought"
}
