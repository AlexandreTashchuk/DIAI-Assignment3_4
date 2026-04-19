package pt.unl.fct.iadi.novaevents.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import pt.unl.fct.iadi.novaevents.model.Event
import java.time.LocalDate

interface EventRepository : JpaRepository<Event, Long> {

    // --- Duplicate checks ---

    fun existsByNameIgnoreCase(name: String): Boolean

    fun existsByNameIgnoreCaseAndIdNot(name: String, id: Long): Boolean

    // --- Filtering ---

    fun findByClubId(clubId: Long): List<Event>

    fun findByType(type: Event.EventType): List<Event>

    fun findByClubIdAndType(clubId: Long, type: Event.EventType): List<Event>

    @Query("SELECT (COUNT(e) > 0) FROM Event e WHERE e.id = :eventId AND e.owner.username = :username")
    fun isOwner(eventId: Long, username: String): Boolean

    // filtros no html, para o futuro, e melhor
    @Query("""
    SELECT e FROM Event e
    WHERE (:type IS NULL OR e.type = :type)
      AND (:clubId IS NULL OR e.clubId = :clubId)
      AND (:from IS NULL OR e.date >= :from)
      AND (:to IS NULL OR e.date <= :to)
""")
    fun filterEvents(
        type: Event.EventType?,
        clubId: Long?,
        from: LocalDate?,
        to: LocalDate?
    ): List<Event>

    @Query("SELECT e.clubId AS clubId, COUNT(e) AS eventCount FROM Event e GROUP BY e.clubId")
    fun countEventsByClub(): List<ClubEventCount>
}