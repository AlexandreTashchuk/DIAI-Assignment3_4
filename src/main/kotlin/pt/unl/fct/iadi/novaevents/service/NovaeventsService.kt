package pt.unl.fct.iadi.novaevents.service

import org.springframework.stereotype.Service
import pt.unl.fct.iadi.novaevents.controller.dto.EventForm
import pt.unl.fct.iadi.novaevents.model.Club
import pt.unl.fct.iadi.novaevents.model.Event
import java.time.LocalDate

@Service
class NovaeventsService {

    private val clubs = listOf<Club>(
        Club(
            1,
            "Chess Club",
            "A community for players of all skill levels to learn, practice, and compete in chess. " +
                    "We host weekly matches, strategy sessions, and tournaments, and participate in inter-university competitions.",
            Club.ClubCategory.SPORTS
        ),
        Club(
            2,
            "Robotics Club",
            "The Robotics Club is the place to turn ideas into machines. Members work on hands-on projects involving " +
                    "electronics, programming, and mechanical design, and regularly participate in robotics competitions.",
            Club.ClubCategory.TECHNOLOGY
        ),
        Club(
            3,
            "Photography Club",
            "A space for photography enthusiasts to improve their skills and showcase their work. Activities include " +
                    "photo walks, editing workshops, and exhibitions across multiple photography styles.",
            Club.ClubCategory.SOCIAL
        ),
        Club(
            4,
            "Hiking & Outdoors Club",
            "Focused on outdoor activities such as hiking and camping, this club organizes regular trips to natural " +
                    "locations, promoting physical activity, exploration, and environmental awareness.",
            Club.ClubCategory.SOCIAL
        ),
        Club(
            5,
            "Film Society",
            "A club for cinema enthusiasts to watch and discuss films from different genres and cultures. " +
                    "Includes screenings, thematic series, and discussions on film techniques and storytelling.",
            Club.ClubCategory.CULTURAL
        )
    )

    private val clubMap = clubs.associateBy { it.id }
    private val events: MutableList<Event> = mutableListOf()

    private var nextEventId: Long = 1

    init {
        seedEvents()
    }

    private fun seedEvents() {

        events.addAll(
            listOf(
                Event(
                    nextEventId++,
                    1,
                    "Chess Tournament",
                    LocalDate.now().plusDays(5),
                    "Room A",
                    Event.EventType.COMPETITION,
                    "Annual tournament"
                ),
                Event(
                    nextEventId++,
                    2,
                    "Robotics Workshop",
                    LocalDate.now().plusDays(10),
                    "Lab 1",
                    Event.EventType.WORKSHOP,
                    "Build a robot"
                ),
                Event(
                    nextEventId++,
                    3,
                    "Photo Walk",
                    LocalDate.now().plusDays(3),
                    "City Center",
                    Event.EventType.SOCIAL,
                    "Outdoor photography"
                ),
                Event(
                    nextEventId++,
                    4,
                    "Mountain Hike",
                    LocalDate.now().plusDays(7),
                    "Sintra",
                    Event.EventType.SOCIAL,
                    "Day hike"
                ),
                Event(
                    nextEventId++,
                    5,
                    "Film Screening",
                    LocalDate.now().plusDays(2),
                    "Auditorium",
                    Event.EventType.MEETING,
                    "Classic movie night"
                )
            )
        )
    }

    fun listAllClubs(): List<Club> {
        return clubs
    }

    fun getClubById(id: Long): Club {
        return clubMap[id] ?: throw ClubNotFoundException(id)
    }

    fun getEventsForClub(clubId: Long): List<Event> {
        return events.filter { it.clubId == clubId }
    }

    fun filterEvents(
        type: Event.EventType?,
        clubId: Long?,
        from: LocalDate?,
        to: LocalDate?
    ): List<Event> {

        return events.filter { event ->

            (type == null || event.type == type) &&
                    (clubId == null || event.clubId == clubId) &&
                    (from == null || !event.date.isBefore(from)) &&
                    (to == null || !event.date.isAfter(to))

        }
    }

    fun getEventById(clubId: Long, eventId: Long): Event {
        clubs.find { it.id == clubId }
            ?: throw ClubNotFoundException(clubId)

        return events.find { it.id == eventId && it.clubId == clubId }
            ?: throw EventNotFoundException("Event with id:$eventId not found")
    }

    fun createEvent(clubId: Long, form: EventForm): Event {

        val club = clubs.find { it.id == clubId }
            ?: throw ClubNotFoundException(clubId)

        //TODO: Event names should be different across all clubs
        // ✅ across ALL clubs
        if (events.any { it.name.equals(form.name, ignoreCase = true) }) {
            throw EventAlreadyExistsException("Event '${form.name}' already exists")
        }

        val event = Event(
            id = nextEventId++,
            clubId = clubId,
            name = form.name!!,
            date = form.date!!,
            location = form.location ?: "",
            type = form.type!!,
            description = form.description ?: ""
        )

        events.add(event)
        return event
    }
}