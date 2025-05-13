package cz.fit.cvut.wrzecond.trainer.controller

import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

/**
 * CustomHandshakeInterceptor checks for a specific cookie named 'loginSecret' in the incoming
 * handshake request and, if present, adds its value to the WebSocket session attributes.
 */
@Component
class CustomHandshakeInterceptor : HandshakeInterceptor {

    /**
     * Intercepts the WebSocket handshake request before it is processed.
     *
     * If the incoming request is a ServletServerHttpRequest and contains a cookie named
     * "loginSecret", the cookie's value is added to the WebSocket session attributes.
     *
     * @param request the incoming HTTP request
     * @param response the outgoing HTTP response
     * @param wsHandler the WebSocket handler
     * @param attributes a mutable map to store attributes for the WebSocket session
     * @return true to proceed with the handshake; false to abort the handshake
     */
    override fun beforeHandshake(
        request: ServerHttpRequest, response: ServerHttpResponse, wsHandler: WebSocketHandler, attributes: MutableMap<String, Any>
    ): Boolean {
        if (request is ServletServerHttpRequest) {
            val servletRequest = request.servletRequest
            val cookies = servletRequest.cookies
            cookies?.forEach {
                if (it.name == "loginSecret") {
                    attributes["auth"] = it.value
                }
            }
        }
        return true
    }

    /**
     * Completes the WebSocket handshake process. This method is called after the handshake has been
     * processed.
     *
     * @param request the incoming HTTP request
     * @param response the outgoing HTTP response
     * @param wsHandler the WebSocket handler
     * @param exception an exception that might have occurred during the handshake, or null if none
     */
    override fun afterHandshake(
        request: ServerHttpRequest, response: ServerHttpResponse, wsHandler: WebSocketHandler, exception: Exception?
    ) {
    }
}
