package cz.fit.cvut.wrzecond.trainer.dto

/**
 * Class for authenticating with FIT OAuth code
 * @property code code received from FIT OAuth
 */
data class AuthDTO(val code: String)

/**
 * Class containing Settings for FIT OAuth
 * @property clientId client id to use
 * @property redirectUri redirect uri to use
 */
data class AuthSettingsDTO(val clientId: String, val redirectUri: String)