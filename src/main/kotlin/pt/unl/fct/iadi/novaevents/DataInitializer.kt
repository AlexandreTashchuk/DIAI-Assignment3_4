package pt.unl.fct.iadi.novaevents

import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.iadi.novaevents.model.AppRole
import pt.unl.fct.iadi.novaevents.model.AppRoleName
import pt.unl.fct.iadi.novaevents.model.AppUser
import pt.unl.fct.iadi.novaevents.model.Club
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.repository.AppUserRepository
import pt.unl.fct.iadi.novaevents.repository.ClubRepository
import pt.unl.fct.iadi.novaevents.repository.EventRepository
import java.time.LocalDate

@Component
class DataInitializer(
    private val appUserRepository: AppUserRepository,
    private val clubRepository: ClubRepository,
    private val eventRepository: EventRepository,
    private val passwordEncoder: PasswordEncoder
) : ApplicationRunner {

    @Transactional
    override fun run(args: org.springframework.boot.ApplicationArguments?) {
        seedUserIfMissing("alice", "password123", AppRoleName.ROLE_EDITOR)
        seedUserIfMissing("bob", "password123", AppRoleName.ROLE_EDITOR)
        seedUserIfMissing("charlie", "password123", AppRoleName.ROLE_ADMIN)

        val alice = appUserRepository.findByUsername("alice")
            ?: throw IllegalStateException("Seed user alice is missing")
        val bob = appUserRepository.findByUsername("bob")
            ?: throw IllegalStateException("Seed user bob is missing")
        val charlie = appUserRepository.findByUsername("charlie")
            ?: throw IllegalStateException("Seed user charlie is missing")

        // One-time repair for databases created before owner FK migration.
        eventRepository.backfillMissingOwners(alice.id)

        val clubs = if (clubRepository.count() == 0L) {
            clubRepository.saveAll(
                listOf(
                    Club(
                        name = "Chess Club",
                        description = "A community for players of all skill levels to learn, practice, and compete in chess. " +
                            "We host weekly matches, strategy sessions, and tournaments, and participate in inter-university competitions.",
                        category = Club.ClubCategory.SPORTS
                    ),
                    Club(
                        name = "Robotics Club",
                        description = "The Robotics Club is the place to turn ideas into machines. Members work on hands-on projects involving " +
                            "electronics, programming, and mechanical design, and regularly participate in robotics competitions.",
                        category = Club.ClubCategory.TECHNOLOGY
                    ),
                    Club(
                        name = "Photography Club",
                        description = "A space for photography enthusiasts to improve their skills and showcase their work. Activities include " +
                            "photo walks, editing workshops, and exhibitions across multiple photography styles.",
                        category = Club.ClubCategory.SOCIAL
                    ),
                    Club(
                        name = "Hiking & Outdoors Club",
                        description = "Focused on outdoor activities such as hiking and camping, this club organizes regular trips to natural " +
                            "locations, promoting physical activity, exploration, and environmental awareness.",
                        category = Club.ClubCategory.SOCIAL
                    ),
                    Club(
                        name = "Film Society",
                        description = "A club for cinema enthusiasts to watch and discuss films from different genres and cultures. " +
                            "Includes screenings, thematic series, and discussions on film techniques and storytelling.",
                        category = Club.ClubCategory.CULTURAL
                    )
                )
            )
        } else {
            clubRepository.findAll()
        }

        if (eventRepository.count() == 0L && clubs.size >= 5) {

            val events = listOf(
                Event(
                    clubId = clubs[0].id,
                    owner = alice,
                    name = "Beginner's Chess Workshop",
                    date = LocalDate.now().plusDays(7),
                    location = "Room A101",
                    type = Event.EventType.WORKSHOP,
                    description = "Introduction to chess basics"
                ),
                Event(
                    clubId = clubs[0].id,
                    owner = bob,
                    name = "Spring Chess Tournament",
                    date = LocalDate.now().plusDays(13),
                    location = "Main Hall",
                    type = Event.EventType.COMPETITION,
                    description = "University open spring chess tournament"
                ),
                Event(
                    clubId = clubs[1].id,
                    owner = charlie,
                    name = "Robotics Workshop",
                    date = LocalDate.now().plusDays(10),
                    location = "Lab 1",
                    type = Event.EventType.WORKSHOP,
                    description = "Build a robot"
                ),
                Event(
                    clubId = clubs[2].id,
                    owner = alice,
                    name = "Photo Walk",
                    date = LocalDate.now().plusDays(3),
                    location = "City Center",
                    type = Event.EventType.SOCIAL,
                    description = "Outdoor photography"
                ),
                Event(
                    clubId = clubs[3].id,
                    owner = bob,
                    name = "Mountain Hike",
                    date = LocalDate.now().plusDays(7),
                    location = "Sintra",
                    type = Event.EventType.SOCIAL,
                    description = "Day hike"
                ),
                Event(
                    clubId = clubs[4].id,
                    owner = charlie,
                    name = "Film Screening",
                    date = LocalDate.now().plusDays(2),
                    location = "Auditorium",
                    type = Event.EventType.MEETING,
                    description = "Classic movie night"
                )
            )
            eventRepository.saveAll(events)
        }
    }

    private fun seedUserIfMissing(username: String, rawPassword: String, roleName: AppRoleName) {
        if (appUserRepository.existsByUsername(username)) return

        val user = AppUser(
            username = username,
            password = passwordEncoder.encode(rawPassword)
        )

        val role = AppRole(role = roleName, user = user)
        user.roles.add(role)

        appUserRepository.save(user)
    }
}