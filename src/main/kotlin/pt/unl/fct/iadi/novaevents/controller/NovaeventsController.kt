package pt.unl.fct.iadi.novaevents.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import pt.unl.fct.iadi.novaevents.service.NovaeventsService

@Controller
class NovaeventsController (val service: NovaeventsService) : NovaeventsAPI {

    override fun listAllClubs(model: Model): String {
        model.addAttribute("clubs", service.listAllClubs())
        return "clubs"
    }

}