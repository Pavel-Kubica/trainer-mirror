package cz.fit.cvut.wrzecond.trainer.controller.helper

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse

/**
 * Sets a "loginSecret" cookie in the HTTP response with specified properties.
 *
 * @param loginSecret The value to be stored in the "loginSecret" cookie.
 * @param response The HttpServletResponse object to which the cookie will be added.
 */
fun setCookie(loginSecret: String, response: HttpServletResponse) {
    val cookie = Cookie("loginSecret", loginSecret)
    cookie.maxAge = 60 * 60 * 2
    cookie.secure = true
    cookie.isHttpOnly = true
    cookie.path = "/"
    cookie.setAttribute("SameSite", "Strict")
    response.addCookie(cookie)
}
