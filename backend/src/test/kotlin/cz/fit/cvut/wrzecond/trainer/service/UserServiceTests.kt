package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.User
import cz.fit.cvut.wrzecond.trainer.repository.SandboxUserRepository
import cz.fit.cvut.wrzecond.trainer.repository.SubjectGuarantorRepository
import cz.fit.cvut.wrzecond.trainer.service.helper.ConverterService
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import cz.fit.cvut.wrzecond.trainer.service.helper.FileService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.mockito.Answers
import org.mockito.Mockito.reset
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant

@SpringBootTest(classes = [UserService::class])
class UserServiceTests(
        @MockBean val userRepository: UserRepository,
        @MockBean val sandboxUserRepository: SandboxUserRepository,
        @MockBean val subjectGuarantorRepository: SubjectGuarantorRepository,
        @MockBean(answer = Answers.CALLS_REAL_METHODS) val converterService: ConverterService,
        @MockBean val fileService: FileService,
        service: UserService
): StringSpec({

    val user1 = User("user1", "user1", "user1", Timestamp.from(Instant.now()), false,
            emptyList(), emptyList(), emptyList(), emptyList(),emptyList(),2, false)
    val admin = user1.copy(username = "admin",isAdmin = true, id = 3)
    val userDTO2 = UserFindDTO(1, user1.username, user1.name)
    val userAuthDTO2 = UserAuthenticateDto(userDTO2.id, userDTO2.username, "", "")
    val userGetDTO = UserGetDto(1, user1.username, user1.name, false)
    //val adminGetDTO = UserGetDto(3, admin.username, admin.name, true)
    val adminDTO = UserFindDTO(3, admin.username, admin.name)
    val adminAuthDTO = UserAuthenticateDto(adminDTO.id, adminDTO.username, "", "")

    beforeTest {
        reset(userRepository)
    }

    "findAll_403" {
        given(userRepository.getByUsername(userDTO2.username)).willReturn(user1)
        val e = shouldThrow<ResponseStatusException> {
            service.findAll("name", 0, 1, userAuthDTO2)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN
    }

    "getOne" {
        given(userRepository.getByUsername(adminDTO.username)).willReturn(admin)
        given(userRepository.getReferenceById(user1.id)).willReturn(user1)

        service.getByID(user1.id, adminAuthDTO) shouldBe userGetDTO.copy(id = 2)

        verify(userRepository).getByUsername(adminDTO.username)
        verify(userRepository).getReferenceById(user1.id)
    }

    "getOne_403" {
        given(userRepository.getByUsername(adminDTO.username)).willReturn(admin.copy(isAdmin = false))
        given(userRepository.getByUsername(userDTO2.username)).willReturn(user1)
        val e = shouldThrow<ResponseStatusException> {
            service.getByID(user1.id, adminAuthDTO)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN
    }
})
