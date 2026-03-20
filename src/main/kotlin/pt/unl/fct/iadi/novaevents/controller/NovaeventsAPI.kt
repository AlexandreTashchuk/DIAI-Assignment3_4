package pt.unl.fct.iadi.novaevents.controller

import jakarta.validation.Valid
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import pt.unl.fct.iadi.novaevents.controller.dto.EventForm
import pt.unl.fct.iadi.novaevents.model.Event
import java.time.LocalDate

interface NovaeventsAPI {

    @RequestMapping(
        value = ["/clubs"],
        method = [RequestMethod.GET]
    )
    fun listAllClubs(model: Model): String //US1

    @RequestMapping(
        value = ["/clubs/{id}"],
        method = [RequestMethod.GET]
    )
    fun getClubDetail(@PathVariable id: Long, model: Model): String //US2

    @RequestMapping(
        value = ["/events"],
        method = [RequestMethod.GET]
    )
    fun listEvents(
        @RequestParam(required = false) type: Event.EventType?,
        @RequestParam(required = false) clubId: Long?,
        @RequestParam(required = false) from: LocalDate?,
        @RequestParam(required = false) to: LocalDate?,
        model: Model
    ): String //US3

    @RequestMapping(
        value = ["/clubs/{clubId}/events/{eventId}"],
        method = [RequestMethod.GET]
    )
    fun getEventDetail(
        @PathVariable clubId: Long,
        @PathVariable eventId: Long,
        model: Model
    ): String //US4

    @RequestMapping(
        value = ["/clubs/{clubId}/events/new"],
        method = [RequestMethod.GET]
    )
    fun showCreateEventForm(
        @PathVariable clubId: Long,
        model: Model
    ): String //US5

    @RequestMapping(
        value = ["/clubs/{clubId}/events"],
        method = [RequestMethod.POST]
    )
    fun createEvent(
        @PathVariable clubId: Long,
        @Valid @ModelAttribute eventForm: EventForm,
        bindingResult: BindingResult,
        model: Model
    ): String //US5

    @RequestMapping(
        value = ["/clubs/{clubId}/events/{eventId}/edit"],
        method = [RequestMethod.GET]
    )
    fun showEditEventForm(
        @PathVariable clubId: Long,
        @PathVariable eventId: Long,
        model: Model
    ): String //US6

    @RequestMapping(
        value = ["/clubs/{clubId}/events/{eventId}"],
        method = [RequestMethod.PUT]
    )
    fun updateEvent(
        @PathVariable clubId: Long,
        @PathVariable eventId: Long,
        @Valid @ModelAttribute eventForm: EventForm,
        bindingResult: BindingResult,
        model: Model
    ): String //US6

    @RequestMapping(
        value = ["/clubs/{clubId}/events/{eventId}/delete"],
        method = [RequestMethod.GET]
    )
    fun showDeleteConfirmation(
        @PathVariable clubId: Long,
        @PathVariable eventId: Long,
        model: Model
    ): String

    @RequestMapping(
        value = ["/clubs/{clubId}/events/{eventId}/delete"],
        method = [RequestMethod.DELETE]
    )
    fun deleteEvent(
        @PathVariable clubId: Long,
        @PathVariable eventId: Long
    ): String
}