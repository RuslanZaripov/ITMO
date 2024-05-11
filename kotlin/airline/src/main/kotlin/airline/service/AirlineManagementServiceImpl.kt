package airline.service

import airline.AirlineMessage
import airline.api.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.datetime.Instant

class AirlineManagementServiceImpl(private val messageFlow: MutableSharedFlow<AirlineMessage>) :
    AirlineManagementService {
        override suspend fun scheduleFlight(flightId: String, departureTime: Instant, plane: Plane) {
            messageFlow.emit(Schedule(flightId, departureTime, plane))
        }

        override suspend fun delayFlight(flightId: String, departureTime: Instant, actualDepartureTime: Instant) {
            messageFlow.emit(Delay(flightId, departureTime, actualDepartureTime))
        }

        override suspend fun cancelFlight(flightId: String, departureTime: Instant) {
            messageFlow.emit(Cancel(flightId, departureTime))
        }

        override suspend fun setCheckInNumber(flightId: String, departureTime: Instant, checkInNumber: String) {
            messageFlow.emit(CheckInNumber(flightId, departureTime, checkInNumber))
        }

        override suspend fun setGateNumber(flightId: String, departureTime: Instant, gateNumber: String) {
            messageFlow.emit(GateNumber(flightId, departureTime, gateNumber))
        }
    }
