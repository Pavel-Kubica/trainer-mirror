package cz.fit.cvut.wrzecond.trainer.controller

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

/**
 * WebSocketConfig is a configuration class that sets up WebSocket message handling in the application.
 *
 * @property customHandshakeInterceptor an interceptor to handle custom logic during the WebSocket handshake process.
 */
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(private val customHandshakeInterceptor: CustomHandshakeInterceptor) : WebSocketMessageBrokerConfigurer {

    /**
     * Configures the message broker for handling WebSocket messages.
     *
     * @param registry an instance of MessageBrokerRegistry to configure message routing and broker settings.
     */
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/messages")
        registry.setApplicationDestinationPrefixes("/ws")
    }

    /**
     * Registers STOMP endpoints to enable WebSocket communication with SockJS fallback options.
     *
     * @param registry the registry for configuring STOMP endpoints.
     */
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/broker")
            .setAllowedOriginPatterns("*")
            .addInterceptors(customHandshakeInterceptor)
            .withSockJS()
    }

}