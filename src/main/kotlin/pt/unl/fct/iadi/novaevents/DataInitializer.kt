package pt.unl.fct.iadi.novaevents

import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
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

    override fun run(args: org.springframework.boot.ApplicationArguments?) {
        seedUserIfMissing("alice", "password123", AppRoleName.ROLE_EDITOR)
        seedUserIfMissing("bob", "password123", AppRoleName.ROLE_EDITOR)
        seedUserIfMissing("charlie", "password123", AppRoleName.ROLE_ADMIN)

        val clubs = if (clubRepository.count() == 0L) {
            clubRepository.saveAll(
                listOf(
                    Club(name = "Chess Club", description = "A community for players ...", category = Club.ClubCategory.SPORTS),
                    Club(name = "Robotics Club", description = "The Robotics Club is the place ...", category = Club.ClubCategory.TECHNOLOGY),
                    Club(name = "Photography Club", description = "A space for photography enthusiasts ...", category = Club.ClubCategory.SOCIAL),
                    Club(name = "Hiking & Outdoors Club", description = "Focused on outdoor activities ...", category = Club.ClubCategory.SOCIAL),
                    Club(name = "Film Society", description = "A club for cinema enthusiasts ...", category = Club.ClubCategory.CULTURAL)
                )
            )
        } else {
            clubRepository.findAll()
        }

        if (eventRepository.count() == 0L && clubs.size >= 5) {
            val alice = appUserRepository.findByUsername("alice")
                ?: throw IllegalStateException("Seed user alice is missing")

            val events = listOf(
                Event(clubId = clubs[0].id, owner = alice, name = "Beginner's Chess Workshop", date = LocalDate.now().plusDays(7), location = "Room A101", type = Event.EventType.WORKSHOP, description = "Introduction to chess basics"),
                Event(clubId = clubs[0].id, owner = alice, name = "Spring Chess Tournament", date = LocalDate.now().plusDays(13), location = "Main Hall", type = Event.EventType.COMPETITION, description = "University open spring chess tournament"),
                Event(clubId = clubs[1].id, owner = alice, name = "Robotics Workshop", date = LocalDate.now().plusDays(10), location = "Lab 1", type = Event.EventType.WORKSHOP, description = "Build a robot"),
                Event(clubId = clubs[2].id, owner = alice, name = "Photo Walk", date = LocalDate.now().plusDays(3), location = "City Center", type = Event.EventType.SOCIAL, description = "Outdoor photography"),
                Event(clubId = clubs[3].id, owner = alice, name = "Mountain Hike", date = LocalDate.now().plusDays(7), location = "Sintra", type = Event.EventType.SOCIAL, description = "Day hike"),
                Event(clubId = clubs[4].id, owner = alice, name = "Film Screening", date = LocalDate.now().plusDays(2), location = "Auditorium", type = Event.EventType.MEETING, description = "Classic movie night")
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