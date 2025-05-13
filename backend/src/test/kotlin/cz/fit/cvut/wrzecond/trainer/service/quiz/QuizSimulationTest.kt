import cz.fit.cvut.wrzecond.trainer.TrainerApplication
import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.dto.quiz.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuestionType
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.service.*
import cz.fit.cvut.wrzecond.trainer.service.quiz.*
import org.junit.Ignore
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.openqa.selenium.*
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.html5.WebStorage
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.sql.Driver
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.random.Random


@Disabled
@SpringBootTest(classes = [TrainerApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConcurrentQuizTest(
    @Autowired val userRepository: UserRepository,
    @Autowired val courseUserService: CourseUserService,
    @Autowired val roleRepository: RoleRepository,
    @Autowired val courseService: CourseService,
    @Autowired val weekService: WeekService,
    @Autowired val lessonService: LessonService,
    @Autowired val moduleService: ModuleService,
    @Autowired val lessonModuleService: LessonModuleService,
    @Autowired val quizService: QuizService,
    @Autowired val questionService: QuestionService,
    @Autowired val quizQuestionService: QuizQuestionService,
    @Autowired val quizroomService: QuizroomService,
    @Autowired val quizroomStudentService: QuizroomStudentService
) {
    private val numStudents = 1
    private val baseUrl = "http://localhost:8080"
    private val numQuestions = 3
    private val questionTimeLimit = 10
    private val headless = false
    private lateinit var quizroomUrl: String
    private lateinit var quizPassword: String
    private val users = mutableListOf<User>()
    private lateinit var module: ModuleGetDTO
    private lateinit var questions: MutableList<QuestionGetDTO>
    private lateinit var teacherAuth: UserAuthenticateDto
    private lateinit var room: QuizroomGetDTO
    private lateinit var teacher: User
    private lateinit var lesson: LessonGetDTO

    @BeforeEach
    fun setup() {
        if (roleRepository.findAll().isEmpty()) {
            roleRepository.saveAndFlush(Role(RoleLevel.TEACHER, 0))
            roleRepository.saveAndFlush(Role(RoleLevel.STUDENT, 1))
        }
        val randomNumber = Random.nextInt(10000000)

        teacher = userRepository.saveAndFlush(
            User(
                UserService.generateSecret(),
                "teacher$randomNumber",
                "teacher$randomNumber",
                Timestamp.from(Instant.now()),
                false,
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                randomNumber.toInt(),
                true
            )
        )
        teacherAuth = UserAuthenticateDto(teacher.id, teacher.username, "test", "")

        val courseDto = courseService.create(
            CourseCreateDTO(
                "Test Course", "Test Course Description", false, null, null
            ), teacherAuth
        )

        courseUserService.addCourseUsers(
            courseDto.id,
            listOf(CourseUserEditDTO(teacher.username, teacher.name, RoleLevel.TEACHER)),
            teacherAuth
        )


        val week = weekService.create(
            WeekCreateDTO(
                "Test Week", Timestamp.from(Instant.now()), Timestamp.from(Instant.now()), courseDto.id
            ), teacherAuth
        )
        lesson = lessonService.create(
            LessonCreateDTO(
                week.id, "Test Lesson Description", false, 1, LessonType.TUTORIAL, null,
                null, null, "description"
            ), teacherAuth
        )
        module = moduleService.create(
            ModuleCreateDTO(
                "Test Module", ModuleType.QUIZ, listOf(), 0, null, "a", false, false, false, false
            ), teacherAuth
        )
        lessonModuleService.putLessonModule(
            lesson.id,
            module.id,
            LessonModuleEditDTO(order = 1, depends = null),
            teacherAuth
        )
        quizroomUrl = "$baseUrl/lessons/${lesson.id}/modules/${module.id}"

        // Create test users
        for (i in 1..numStudents) {
            val username = "autotest${randomNumber}_$i"
            val user = userRepository.saveAndFlush(
                User(
                    UserService.generateSecret(), username, username, Timestamp.from(Instant.now()), false,
                    emptyList(), emptyList(), emptyList(), emptyList(), emptyList()
                )
            )
            users.add(user)
        }

        // join the course
        courseUserService.addCourseUsers(
            courseDto.id,
            users.map { CourseUserEditDTO(it.username, it.name, RoleLevel.STUDENT) },
            teacherAuth
        )

        // Create a quiz
        val quizDto = QuizCreateDTO("Test Quiz", numQuestions, 1, listOf(), module.id)
        val quizGetDTO = quizService.create(quizDto, teacherAuth)

        questions = mutableListOf()
        // Create questions
        for (i in 1..numQuestions) {
            val questionDto = QuestionCreateDTO(
                "Question $i", "[\"Answer1 $i\", \"Answer2 $i\", \"Answer3 $i\"]", "Answer1 $i", questionTimeLimit,
                "explanation", users[0].username, true, QuestionType.MULTICHOICE, null
            )
            questions.add(questionService.create(questionDto, teacherAuth))
        }

        // Add questions to the quiz
        for (i in 0..<numQuestions) {
            quizQuestionService.putQuizQuestion(quizGetDTO.id, questions[i].id, QuizQuestionEditDTO(i + 1), teacherAuth)
        }

        val roomDto = QuizroomCreateDTO(teacher.username, quizGetDTO.id, lesson.id)
        room = quizroomService.create(roomDto, teacherAuth)
        quizPassword = room.roomPassword

        // join the teacher to the quiz room
        quizroomStudentService.createQuizroomStudent(room.id, QuizroomStudentCreateDTO(teacher.id, room.id), teacherAuth)
    }

    @AfterEach
    fun cleanup() {

        // Delete the questions
        questions.forEach {
            questionService.delete(it.id, teacherAuth)
        }

        // Delete the module
        moduleService.delete(module.id, teacherAuth)

        // Delete test users
        users.forEach {
            userRepository.delete(it)
        }
        userRepository.delete(teacher)
    }

    @Test
    fun `test concurrent students taking quiz`() {
        println("Starting $numStudents student clients, password $quizPassword")
        val executor = Executors.newFixedThreadPool(numStudents)
        val latch = CountDownLatch(numStudents)

        val driverOptions = FirefoxOptions()
        if (headless) {
            driverOptions.addArguments("--headless")
        }
        driverOptions.addArguments("--disable-gpu")
        driverOptions.addArguments("--window-size=1920,1080")

        for (i in 0 until numStudents) {
            executor.submit {
                val driver = FirefoxDriver(driverOptions)
                val wait = WebDriverWait(driver, Duration.ofSeconds(120))

                try {
                    val user = users[i]
                    val username = user.username

                    println("Starting student: $username")
                    signIn(driver, user, baseUrl)
                    driver.get(quizroomUrl)
                    driver.manage().window().maximize()

                    wait.until(ExpectedConditions.and(
                        ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='text']")),
                        ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@class, 'bg')]"))
                    ))

                    Thread.sleep(1000)

                    val passwordField = driver.findElement(By.xpath("//input[@type='text']"))
                    passwordField.sendKeys(quizPassword)
                    val enterButton = driver.findElement(By.xpath("//button[contains(@class, 'bg')]"))
                    scrollIntoView(driver, enterButton)
                    enterButton.click()

                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class, 'v-card-subtitle')]")))
                    latch.countDown()

                    for (j in 0 until numQuestions) {
                        try {
                            wait.until(
                                ExpectedConditions.or(
                                    ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@class, 'bg-blue')]")),
                                    ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@class, 'bg-red')]"))
                                )
                            )

                            if (driver.findElements(By.xpath("//button[contains(@class, 'bg-red')]")).isNotEmpty()) {
                                break
                            }

                            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//input[@type='radio'])[1]")))
                            val firstAnswer = driver.findElement(By.xpath("(//input[@type='radio'])[1]"))
                            scrollIntoView(driver, firstAnswer)
                            firstAnswer.click()

                            val submitButton = driver.findElement(By.xpath("//button[contains(@class, 'bg-blue')]"))
                            scrollIntoView(driver, submitButton)
                            submitButton.click()
                            println("User $username answered question $j")
                        } catch (e: Exception) {
                            println("User $username failed with error ${e.message}")
                            break
                        }
                    }

                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@class, 'bg-red')]")))
                    val endButton = driver.findElement(By.xpath("//button[contains(@class, 'bg-red')]"))
                    scrollIntoView(driver, endButton)
                    endButton.click()

                    println("User $username finished the quiz.")
                } catch (e: Exception) {
                    println("User $i failed with error ${e.message}")
                    throw e
                }
                driver.quit()
            }
        }

        // main thread
        println("Started all clients, starting the teacher")
        val teacherDriver = FirefoxDriver(driverOptions)
        val teacherWait = WebDriverWait(teacherDriver, Duration.ofSeconds(120))
        signIn(teacherDriver, teacher, baseUrl)
        teacherDriver.get(quizroomUrl)
        teacherDriver.manage().window().maximize()

        println("Waiting for students to click the enter button")
        latch.await()
        println("All students have clicked the enter button")
        Thread.sleep(4000)

        teacherWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@class, 'bg-green')]")))
        val startButton = teacherDriver.findElement(By.xpath("//button[contains(@class, 'bg-green')]"))
        scrollIntoView(teacherDriver, startButton)
        startButton.click()
        println("Teacher clicked start quiz")

        for (i in 0 until numQuestions) {
            try {
                teacherWait.until(
                    ExpectedConditions.or(
                        ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@class, 'bg-green')]")),
                        ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@class, 'bg-red')]"))
                    )
                )
                Thread.sleep(2000)

                if (teacherDriver.findElements(By.xpath("//button[contains(@class, 'bg-green')]")).isEmpty()) {
                    break
                }

                val nextButton = teacherDriver.findElement(By.xpath("//button[contains(@class, 'bg-green')]"))
                scrollIntoView(teacherDriver, nextButton)
                nextButton.click()
                println("Teacher clicked next question")
            } catch (e: Exception) {
                break
            }
        }

        teacherWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@class, 'bg-red')]")))
        val endButton = teacherDriver.findElement(By.xpath("//button[contains(@class, 'bg-red')]"))
        scrollIntoView(teacherDriver, endButton)
        endButton.click()

        teacherWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@class, 'bg-red')]")))
        val leaveButton = teacherDriver.findElement(By.xpath("//button[contains(@class, 'bg-red')]"))
        scrollIntoView(teacherDriver, leaveButton)
        leaveButton.click()

        teacherDriver.quit()

        executor.awaitTermination(1, TimeUnit.MINUTES)
    }

    private fun signIn(
        driver: FirefoxDriver,
        user: User,
        baseUrl: String
    ) {
        driver.get(baseUrl)
        driver.manage().addCookie(Cookie("loginSecret", user.loginSecret))
        val local = (driver as WebStorage).localStorage
        val studentId = user.id
        val username = user.username
        local.setItem(
            "user", """{"user":{"id":$studentId,"username":"$username","name":"$username","isTeacher":[],"isGuarantor":[],"isAdmin":false},
                        "realUser":{"id":$studentId,"username":"$username","name":"$username","isTeacher":[],"isGuarantor":[],"isAdmin":false},
                        "anonymous":false,"darkMode":false,"semester":0,"originalMode":"","notifications":0,
                        "hideSatisfiedNotifications":false,"requestedHelpFilter":false,"allowedShowFilter":false,
                        "solvedFilter":false,"hiddenWeeks":{},"hiddenSubjects":{},"lessonEditItemIds":{},"moduleEditItemIds":{},"locale":"customEn"}
                        """.trimIndent()
        )
        driver.get(baseUrl)
    }

    private fun scrollIntoView(driver: WebDriver, element: WebElement) {
        (driver as JavascriptExecutor).executeScript("arguments[0].scrollIntoView(true);", element)
        Thread.sleep(500)
    }
}