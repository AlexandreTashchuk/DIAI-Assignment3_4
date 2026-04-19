package pt.unl.fct.iadi.novaevents.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
class Event(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    var clubId: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", insertable = false, updatable = false)
    var club: Club? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = true)
    var owner: AppUser? = null,

    var name: String = "",

    var date: LocalDate = LocalDate.now(),

    var location: String = "",

    @Enumerated(EnumType.STRING)
    var type: EventType = EventType.OTHER,

    @Column(length = 2000)
    var description: String = ""
) {
    enum class EventType {
        WORKSHOP,
        TALK,
        COMPETITION,
        SOCIAL,
        MEETING,
        OTHER
    }
}