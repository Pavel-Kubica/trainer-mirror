package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.AuthDTO
import cz.fit.cvut.wrzecond.trainer.dto.UserLoginDTO
import cz.fit.cvut.wrzecond.trainer.entity.User
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthService
import cz.fit.cvut.wrzecond.trainer.service.helper.NetworkService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant

const val AUTH_TEST_CODE  = "unitTest"
const val AUTH_TEST_TOKEN = "unitToken"
const val AUTH_TEST_USERNAME = "unitUsername"

class AuthServiceTest: StringSpec({
    // Mocks
    val mockNetworkService = mockk<NetworkService>()
    val mockSubjectService = mockk<SubjectService>()
    val mockUserRepository = mockk<UserRepository>()
    val mockEnvironment = mockk<Environment>()

    every { mockEnvironment.getProperty("auth.clientId", "") } returns ""
    every { mockEnvironment.getProperty("auth.redirectUri", "") } returns ""
    every { mockEnvironment.getProperty("auth.clientSecret", "") } returns ""
    every { mockSubjectService.findByGuarantor(any()) } returns emptyList()
    every { mockEnvironment.getProperty("auth.testUsername", "test01") } returns "test01"
    every { mockEnvironment.getProperty("spring.profiles.active", "") } returns ""
    every { mockEnvironment.getProperty("auth.isDevVersion", "false") } returns "false"

    val service = AuthService(mockUserRepository, mockNetworkService, mockSubjectService, mockEnvironment)
    val authDTO = AuthDTO(AUTH_TEST_CODE)

    "authenticate" {
        val user = User("dummy", AUTH_TEST_USERNAME, "Dummy", Timestamp.from(Instant.now()), false,
            emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), 1)
        val userLoginDto = UserLoginDTO(user.id, user.username, user.name, emptyList(), emptyList(), false)

        every { mockNetworkService.fitAuthRequest(AUTH_TEST_CODE, "", "", "") } returns "{\"access_token\": \"${AUTH_TEST_TOKEN}\"}"
        every { mockNetworkService.fitTokenInfoRequest(AUTH_TEST_TOKEN) } returns "{\"user_name\": \"${AUTH_TEST_USERNAME}\"}"
        every { mockUserRepository.getByUsername(AUTH_TEST_USERNAME) } returns user
        every {mockUserRepository.saveAndFlush(user)} returns user

        service.authenticate(authDTO) shouldBe userLoginDto

        verify { mockNetworkService.fitAuthRequest(AUTH_TEST_CODE, "", "", "") }
        verify { mockNetworkService.fitTokenInfoRequest(AUTH_TEST_TOKEN) }
        verify { mockUserRepository.getByUsername(AUTH_TEST_USERNAME) }
    }

    "authenticate_bad" {
        every { mockNetworkService.fitAuthRequest(AUTH_TEST_CODE, "", "", "") } returns "{}"

        val e = shouldThrow<ResponseStatusException> {
            service.authenticate(authDTO)
        }
        e.statusCode shouldBe HttpStatus.BAD_REQUEST

        verify { mockNetworkService.fitAuthRequest(AUTH_TEST_CODE, "", "", "") }
    }

})