package pt.unl.fct.iadi.novaevents.controller

import org.springframework.http.HttpStatus
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import pt.unl.fct.iadi.novaevents.service.ClubNotFoundException
import pt.unl.fct.iadi.novaevents.service.EventAlreadyExistsException

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ClubNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleClubNotFound(
        ex: ClubNotFoundException,
        model: Model
    ): String {

        model.addAttribute("message", ex.message)

        return "error/404"
    }

    //TODO: Response Status + 400.html page
    @ExceptionHandler(EventAlreadyExistsException::class)
    fun handleDuplicateEvent(ex: EventAlreadyExistsException, model: Model): String {
        model.addAttribute("message", ex.message)
        return "error/400"
    }
}