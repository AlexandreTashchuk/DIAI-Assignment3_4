package pt.unl.fct.iadi.novaevents.service

import org.springframework.stereotype.Service
import pt.unl.fct.iadi.novaevents.model.Club

@Service
class NovaeventsService {

    val clubs = listOf<Club>(
        Club(
            1,
            "Chess Club",
            "The Chess Club description",
            Club.ClubCategory.SPORTS
        ),
        Club(
            2,
            "Robotics Club",
            "The Robotics Club is the place to turn ideas into machines",
            Club.ClubCategory.TECHNOLOGY
        ),
        Club(
            3,
            "Photography Club",
            "The Photography Club description",
            Club.ClubCategory.SOCIAL
        ),
        Club(
            4,
            "Hiking & Outdoors Club",
            "The Hiking Club description",
            Club.ClubCategory.SOCIAL
        ),
        Club(
            5,
            "Film Society",
            "The Film Society description",
            Club.ClubCategory.CULTURAL
        )
    )

    val clubMap = clubs.associateBy { it.id }

    fun listAllClubs(): List<Club> {
        return clubs
    }
}