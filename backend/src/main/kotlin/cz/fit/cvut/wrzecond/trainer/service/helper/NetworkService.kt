package cz.fit.cvut.wrzecond.trainer.service.helper

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

/**
 * Service class responsible for handling network operations related to FIT OAuth authentication.
 */
@Service
class NetworkService {

    /** HttpClient used for network requests to Apple servers */
    private val client = HttpClient(Java) {
        install(JsonFeature) {
            serializer = GsonSerializer {}
        }
    }

    /**
     * Function to sign in using FIT OAuth
     * @param code token used for authentication
     * @param clientId client id for FIT oauth
     * @param secret client secret for FIT oauth
     * @param redirectUri client redirection URI
     * @return JSON String containing FIT OAuth login information
     * @throws Exception on failure
     */
    fun fitAuthRequest (code: String, clientId: String, secret: String, redirectUri: String) : String = runBlocking {
        client.submitForm<HttpStatement>(
            url = AuthService.FIT_ENDPOINT + AuthService.TOKEN_PATH,
            formParameters = Parameters.build {
                append("client_id", clientId)
                append("client_secret", secret)
                append("grant_type", "authorization_code")
                append("redirect_uri", redirectUri)
                append("code", code)
            }
        ).execute().receive()
    }

    /**
     * Function used to get username with OAuth access token
     * @param accessToken
     * @return JSON string containing FIT OAuth token information
     * @throws Exception on failure
     */
    fun fitTokenInfoRequest(accessToken: String) : String = runBlocking {
        client.post<HttpStatement>(AuthService.FIT_ENDPOINT + AuthService.TOKEN_INFO_PATH) {
            parameter("token", accessToken)
        }.execute().receive()
    }
}