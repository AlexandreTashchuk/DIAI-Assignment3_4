package pt.unl.fct.iadi.novaevents.service

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import pt.unl.fct.iadi.novaevents.controller.dto.EventForm
import pt.unl.fct.iadi.novaevents.model.Club
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.repository.AppUserRepository
import pt.unl.fct.iadi.novaevents.repository.ClubRepository
import pt.unl.fct.iadi.novaevents.repository.EventRepository
import java.time.LocalDate

@Service
class NovaeventsService(
    private val appUserRepository: AppUserRepository,
    private val clubRepository: ClubRepository,
    private val eventRepository: EventRepository
) {

    fun listAllClubs(): List<Club> =
        clubRepository.findAll()

    fun getClubById(id: Long): Club =
        clubRepository.findById(id)
            .orElseThrow { ClubNotFoundException(id) }

    fun getEventsForClub(clubId: Long): List<Event> {
        getClubById(clubId) // preserves 404 behavior
        return eventRepository.findByClubId(clubId)
    }

    fun filterEvents(
        type: Event.EventType?,
        clubId: Long?,
        from: LocalDate?,
        to: LocalDate?
    ): List<Event> {
        return eventRepository.filterEvents(type, clubId, from, to)
    }

    fun getEventById(clubId: Long, eventId: Long): Event {

        getClubById(clubId)

        val event = eventRepository.findById(eventId)
            .orElseThrow { EventNotFoundException("Event with id:$eventId not found") }

        if (event.clubId != clubId) {
            throw ClubDoesNotHaveEventException("Club $clubId does not own event $eventId")
        }

        return event
    }

    @PreAuthorize("hasAnyRole('EDITOR','ADMIN') and @eventSecurity.isOwner(#eventId, authentication)")
    fun getEventForEdit(clubId: Long, eventId: Long): Event = getEventById(clubId, eventId)

    @PreAuthorize("hasAnyRole('EDITOR','ADMIN')")
    fun createEvent(clubId: Long, form: EventForm): Event {

        getClubById(clubId)

        if (eventRepository.existsByNameIgnoreCase(form.name!!)) {
            throw EventAlreadyExistsException("Event '${form.name}' already exists")
        }

        val event = Event(
            clubId = clubId,
            owner = currentUser(),
            name = form.name,
            date = form.date!!,
            location = form.location ?: "",
            type = form.type!!,
            description = form.description ?: ""
        )

        return eventRepository.save(event)
    }

    @PreAuthorize("hasAnyRole('EDITOR','ADMIN') and @eventSecurity.isOwner(#eventId, authentication)")
    fun updateEventById(eventId: Long, clubId: Long, form: EventForm): Event {

        getClubById(clubId)

        val existing = eventRepository.findById(eventId)
            .orElseThrow { EventNotFoundException("Event with id:$eventId not found") }

        if (existing.clubId != clubId) {
            throw ClubDoesNotHaveEventException("Club $clubId does not own event $eventId")
        }

        if (eventRepository.existsByNameIgnoreCaseAndIdNot(form.name!!, eventId)) {
            throw EventAlreadyExistsException("Event '${form.name}' already exists")
        }

        val updated = Event(
            id = existing.id,
            clubId = clubId,
            owner = existing.owner,
            name = form.name,
            date = form.date!!,
            location = form.location ?: existing.location,
            type = form.type!!,
            description = form.description ?: existing.description
        )

        return eventRepository.save(updated)
    }

    @PreAuthorize("hasAnyRole('EDITOR','ADMIN') and @eventSecurity.isOwnerOrAdmin(#eventId, authentication)")
    fun getEventForDelete(clubId: Long, eventId: Long): Event = getEventById(clubId, eventId)

    @PreAuthorize("hasAnyRole('EDITOR','ADMIN') and @eventSecurity.isOwnerOrAdmin(#eventId, authentication)")
    fun deleteEventById(clubId: Long, eventId: Long) {

        getClubById(clubId)

        val event = eventRepository.findById(eventId)
            .orElseThrow { EventNotFoundException("Event $eventId not found") }

        if (event.clubId != clubId) {
            throw ClubDoesNotHaveEventException("Club $clubId does not own event $eventId")
        }

        eventRepository.delete(event)
    }

    fun listClubsWithEventCounts(): List<Pair<Club, Long>> {
        val clubs = clubRepository.findAll()
        val counts = eventRepository.countEventsByClub().associateBy { it.clubId }
        return clubs.map { club -> club to (counts[club.id]?.eventCount ?: 0) }
    }

    private fun currentUser() =
        appUserRepository.findByUsername(SecurityContextHolder.getContext().authentication.name)
            ?: throw IllegalStateException("Authenticated user does not exist in database")
}