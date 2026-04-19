package pt.unl.fct.iadi.novaevents.repository

import pt.unl.fct.iadi.novaevents.model.Event
import java.time.LocalDate

interface EventListRow {
    val id: Long
    val clubId: Long
    val clubName: String?
    val name: String
    val date: LocalDate
    val type: Event.EventType
    val location: String
}

