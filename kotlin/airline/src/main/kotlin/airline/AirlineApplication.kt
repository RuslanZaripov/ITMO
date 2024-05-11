package airline

import airline.api.*
import airline.service.*
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

interface AirlineMessage

class AirlineApplication(private val config: AirlineConfig, emailService: EmailService) {
    private var flights = MutableStateFlow(emptyList<Flight>())
    private val airlineManagementMessagesFlow = MutableSharedFlow<AirlineMessage>()

    val airportAudioAlerts: Flow<AudioAlerts>
        get() = flow {
            airlineManagementMessagesFlow.collect { msg ->
                if (msg is AudioAlerts) emit(msg)
            }
        }

    private val bufferedEmailService = BufferedEmailService(emailService, 100)

    val bookingService: BookingService =
        BookingServiceImpl(config, flights, airlineManagementMessagesFlow)

    val managementService: AirlineManagementService =
        AirlineManagementServiceImpl(airlineManagementMessagesFlow)

    private val passengerNotificationService =
        PassengerNotificationService(flights, bufferedEmailService)

    @FlowPreview
    fun airportInformationDisplay(coroutineScope: CoroutineScope): StateFlow<InformationDisplay> {
        return flights
            .sample(config.displayUpdateInterval.inWholeMilliseconds)
            .map { flights ->
                val now = Clock.System.now()
                flights
                    .filter { flight -> flight.departureTime in now..now + 1.days }
                    .map(Flight::toFlightInfo)
                    .sortedBy(FlightInfo::flightId)
            }
            .map { InformationDisplay(it) }
            .stateIn(
                coroutineScope,
                SharingStarted.Eagerly,
                InformationDisplay(emptyList()),
            )
    }

    private suspend fun tryChangeFlight(msg: AirlineManagementMessages): Boolean {
        val fs = flights.value.toMutableList()
        val flightId = fs.indexOfFirstOrNull(msg.flightId, msg.departureTime) ?: return false
        val flight = fs[flightId]
        fs[flightId] = msg.change(flight)
        flights.emit(fs)
        return true
    }

    private suspend fun tryCreateFlight(msg: Schedule): Boolean {
        val fs = flights.value.toMutableList()
        val flightId = fs.indexOfFirstOrNull(msg.flightId, msg.departureTime)
        if (flightId != null) return false
        val newFlight = Flight(
            flightId = msg.flightId,
            departureTime = msg.departureTime,
            plane = msg.plane,
        )
        fs.add(newFlight)
        flights.emit(fs)
        return true
    }

    private suspend fun audioAlerts() {
        flights.value.forEach { flight ->
            val registrationOpeningTime = flight.actualDepartureTime - config.registrationOpeningTime
            val registrationClosingTime = flight.actualDepartureTime - config.registrationClosingTime
            val boardingOpeningTime = flight.actualDepartureTime - config.boardingOpeningTime
            val boardingClosingTime = flight.actualDepartureTime - config.boardingClosingTime

            val currentTime = Clock.System.now()
            val boardingClosingDiff = (currentTime - boardingClosingTime).inWholeMinutes
            val boardingOpeningDiff = (boardingOpeningTime - currentTime).inWholeMinutes
            val registrationClosingDiff = (currentTime - registrationClosingTime).inWholeMinutes
            val registrationOpeningDiff = (registrationOpeningTime - currentTime).inWholeMinutes

            if (registrationOpeningDiff in 0..3) {
                if (flight.checkInNumber != null) {
                    airlineManagementMessagesFlow.emit(
                        AudioAlerts.RegistrationOpen(
                            flight.flightId,
                            flight.checkInNumber,
                        ),
                    )
                }
            }
            if (registrationClosingDiff in 0..3) {
                if (flight.checkInNumber != null) {
                    airlineManagementMessagesFlow.emit(
                        AudioAlerts.RegistrationClosing(
                            flight.flightId,
                            flight.checkInNumber,
                        ),
                    )
                }
            }
            if (boardingOpeningDiff in 0..3) {
                if (flight.gateNumber != null) {
                    airlineManagementMessagesFlow.emit(AudioAlerts.BoardingOpened(flight.flightId, flight.gateNumber))
                }
            }
            if (boardingClosingDiff in 0..3) {
                if (flight.gateNumber != null) {
                    airlineManagementMessagesFlow.emit(AudioAlerts.BoardingClosing(flight.flightId, flight.gateNumber))
                }
            }
        }
    }

    private fun successMessage(
        ticket: Ticket,
    ): String {
        return """
            Dear ${ticket.passengerName},
            
            You have successfully bought a ticket for flight ${ticket.flightId} at ${ticket.departureTime}.
            Your seat number is ${ticket.seatNo}.
            
            Have a nice day!
            """.trimIndent()
    }

    private fun failureMessage(
        problem: String,
        ticket: Ticket,
    ): String {
        return """
            Dear ${ticket.passengerName},
            
            Failed to buy a ticket for flight ${ticket.flightId} at ${ticket.departureTime}.
            Reason: $problem
            
            Have a nice day!
            """.trimIndent()
    }

    private suspend fun tryBuyTicket(msg: BuyTicket) {
        val flight = flights.value.find(msg.flightId, msg.departureTime)

        val ticket = msg.ticket
        val seatNo = msg.ticket.seatNo
        val responseMessage = when {
            flight == null ->
                failureMessage("Flight does not exist.", ticket)

            flight.isTicketSaleOver(config.ticketSaleEndTime) ->
                failureMessage("Ticket sale is over.", ticket)

            flight.isCancelled ->
                failureMessage("Flight is cancelled.", ticket)

            flight.isDeparted() ->
                failureMessage("Flight has already departed.", ticket)

            !flight.isSeatExists(seatNo) ->
                failureMessage("Seat $seatNo does not exist.", ticket)

            flight.isSeatBusy(seatNo) ->
                failureMessage("Seat $seatNo is already taken.", ticket)

            else -> {
                tryChangeFlight(msg)
                successMessage(ticket)
            }
        }

        bufferedEmailService.send(to = ticket.passengerEmail, text = responseMessage)
    }

    suspend fun run() {
        with(CoroutineScope(coroutineContext)) {
            launch {
                while (isActive) {
                    audioAlerts()
                    delay(config.audioAlertsInterval)
                }
            }
            launch { passengerNotificationService.run() }
            launch { bufferedEmailService.run() }
        }
        airlineManagementMessagesFlow.collect { msg ->
            when (msg) {
                is Schedule -> tryCreateFlight(msg)
                is BuyTicket -> tryBuyTicket(msg)
                is AirlineManagementMessages -> {
                    if (tryChangeFlight(msg)) {
                        passengerNotificationService.notifyPassengers(msg.flightId, msg.message())
                    }
                }
            }
        }
    }
}

fun List<Flight>.indexOfFirstOrNull(flightId: String, departureTime: Instant): Int? {
    return indexOfFirst { flight -> flight.flightId == flightId && flight.departureTime == departureTime }
        .takeIf { index -> index != -1 }
}

fun List<Flight>.find(flightId: String, departureTime: Instant): Flight? {
    return find { flight -> flight.flightId == flightId && flight.departureTime == departureTime }
}
