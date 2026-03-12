package pt.unl.fct.iadi.novaevents.controller

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

interface NovaeventsAPI {

    @RequestMapping(
        value = ["/clubs"],
        method = [RequestMethod.GET]
    )
    fun listAllClubs(model: Model): String
}