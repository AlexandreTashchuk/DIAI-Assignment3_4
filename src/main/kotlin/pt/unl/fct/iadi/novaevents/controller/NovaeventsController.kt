package pt.unl.fct.iadi.novaevents.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import pt.unl.fct.iadi.novaevents.controller.dto.EventForm
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.repository.EventRepository
import pt.unl.fct.iadi.novaevents.service.EventAlreadyExistsException
import pt.unl.fct.iadi.novaevents.service.NovaeventsService
import java.time.LocalDate

@Controller
class NovaeventsController(val service: NovaeventsService, val eventRepository: EventRepository) : NovaeventsAPI {

//    override fun listAllClubs(model: Model): String {
//        model.addAttribute("clubs", service.listAllClubs())
//        return "clubs/list"
//    }
    override fun listAllClubs(model: Model): String {
        val clubs = service.listAllClubs()

        // Fetch event counts for all clubs in one query
        val counts = eventRepository.countEventsByClub() // returns List<ClubEventCount>
        val countMap = counts.associate { it.clubId to it.eventCount }

        model.addAttribute("clubs", clubs)
        model.addAttribute("eventCounts", countMap)

        return "clubs/list"
    }

    override fun getClubDetail(id: Long, model: Model): String {

        val club = service.getClubById(id)
        val events = service.getEventsForClub(id)

        model.addAttribute("club", club)
        model.addAttribute("events", events)

        return "clubs/detail"
    }

    override fun listEvents(
        type: Event.EventType?,
        clubId: Long?,
        from: LocalDate?,
        to: LocalDate?,
        model: Model
    ): String {

        val events = service.filterEvents(type, clubId, from, to)

        val clubs = service.listAllClubs()
        val clubMap = clubs.associateBy { it.id }

        model.addAttribute("events", events)
        model.addAttribute("clubs", clubs)
        model.addAttribute("clubMap", clubMap)
        model.addAttribute("types", Event.EventType.values())
        //model.addAttribute("types", Event.EventType.entries.toTypedArray())

        return "events/list"
    }

    override fun getEventDetail(
        clubId: Long,
        eventId: Long,
        model: Model
    ): String {

        val event = service.getEventById(clubId, eventId)
        val club = service.getClubById(clubId)

        model.addAttribute("event", event)
        model.addAttribute("club", club)

        return "events/detail"
    }

    // ----------------------------
    // US5 — Show Create Form
    // ----------------------------
    override fun showCreateEventForm(clubId: Long, model: Model): String {

        // Ensures 404 if club doesn't exist
        service.getClubById(clubId)

        model.addAttribute("eventForm", EventForm())
        model.addAttribute("types", Event.EventType.values())
        model.addAttribute("clubId", clubId)

        return "events/create"
    }

    // ----------------------------
    // US5 — Create Event
    // ----------------------------
    override fun createEvent(
        clubId: Long,
        eventForm: EventForm,
        bindingResult: BindingResult,
        model: Model
    ): String {

        // Validation errors (Bean Validation)
        if (bindingResult.hasErrors()) {
            model.addAttribute("types", Event.EventType.values())
            model.addAttribute("clubId", clubId)
            return "events/create"
        }

        return try {
            val event = service.createEvent(clubId, eventForm)
            "redirect:/clubs/${clubId}/events/${event.id}"
        } catch (ex: EventAlreadyExistsException) {

            // bindingResult.rejectValue("name", "error.name", ex.message ?: "Duplicate event")
            bindingResult.rejectValue("name", "error.name", "An event with this name already exists")

            model.addAttribute("types", Event.EventType.values())
            model.addAttribute("clubId", clubId)

            "events/create"
        }
    }

    override fun showEditEventForm(
        clubId: Long,
        eventId: Long,
        model: Model
    ): String {
        val event = service.getEventForEdit(clubId, eventId)

        val form = EventForm(
            name = event.name,
            date = event.date,
            type = event.type,
            location = event.location,
            description = event.description
        )

        model.addAttribute("eventForm", form)
        model.addAttribute("clubId", clubId)
        model.addAttribute("eventId", eventId)

        return "events/edit"
    }

    override fun updateEvent(
        clubId: Long,
        eventId: Long,
        eventForm: EventForm,
        bindingResult: BindingResult,
        model: Model
    ): String {

        if (bindingResult.hasErrors()) {
            model.addAttribute("clubId", clubId)
            model.addAttribute("eventId", eventId)
            return "events/edit"
        }

        return try {
            val event = service.updateEventById(eventId, clubId, eventForm)
            "redirect:/clubs/${clubId}/events/${event.id}"
        } catch (ex: EventAlreadyExistsException) {

            // bindingResult.rejectValue("name", "error.name", ex.message ?: "Duplicate event")
            bindingResult.rejectValue("name", "error.name", "An event with this name already exists")

            model.addAttribute("types", Event.EventType.values())
            model.addAttribute("clubId", clubId)

            "events/edit"
        }
    }

    override fun showDeleteConfirmation(
        clubId: Long,
        eventId: Long,
        model: Model
    ): String {
        val event = service.getEventForDelete(clubId, eventId)

        model.addAttribute("event", event)
        model.addAttribute("clubId", clubId)

        return "events/delete"
    }

    override fun deleteEvent(clubId: Long, eventId: Long): String {
        service.deleteEventById(clubId, eventId)

        return "redirect:/clubs/$clubId"
    }
}