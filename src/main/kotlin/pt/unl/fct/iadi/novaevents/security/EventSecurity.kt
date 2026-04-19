package pt.unl.fct.iadi.novaevents.security

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import pt.unl.fct.iadi.novaevents.repository.EventRepository

@Component("eventSecurity")
class EventSecurity(
    private val eventRepository: EventRepository
) {

    fun isOwner(eventId: Long, authentication: Authentication?): Boolean {
        if (authentication == null || authentication.name.isNullOrBlank()) return false
        return eventRepository.isOwner(eventId, authentication.name)
    }

    fun isOwnerOrAdmin(eventId: Long, authentication: Authentication?): Boolean {
        if (authentication == null || authentication.name.isNullOrBlank()) return false

        val isAdmin = authentication.authorities.any { it.authority == "ROLE_ADMIN" }
        return isAdmin || eventRepository.isOwner(eventId, authentication.name)
    }
}

