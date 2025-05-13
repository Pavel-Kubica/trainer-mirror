package cz.fit.cvut.wrzecond.trainer.service.quiz

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.dto.quiz.*
import cz.fit.cvut.wrzecond.trainer.entity.ModuleType
import cz.fit.cvut.wrzecond.trainer.entity.quiz.Quiz
import cz.fit.cvut.wrzecond.trainer.entity.quiz.Quizroom
import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuizroomStudent
import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuestionInstance
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuestionInstanceRepo
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizRepo
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizroomRepo
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizroomStudentRepo
import cz.fit.cvut.wrzecond.trainer.service.IServiceImplOld
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthorizationService
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import cz.fit.cvut.wrzecond.trainer.service.LogService


/**
 * Service class for managing quiz rooms.
 *
 * @property repository The repository to manage `Quizroom` entities.
 * @property authorizationService Service for handling authorization.
 * @property quizRepo The repository to manage `Quiz` entities.
 * @property quizroomStudentRepo The repository to manage `QuizroomStudent` entities.
 * @property userRepo The repository to manage `User` entities.
 * @property moduleRepo The repository to manage `Module` entities.
 * @property lessonRepository The repository to manage `Lesson` entities.
 * @property weekRepository The repository to manage `Week` entities.
 * @property cuRepository The repository to manage `CourseUser` entities.
 * @property logRepository The repository to manage `Log` entities.
 * @property qiRepo The repository to manage `QuestionInstance` entities.
 * @property logService The service to manage `Log` entities.
 */
@Service
class QuizroomService(override val repository: QuizroomRepo,
                      private val authorizationService: AuthorizationService,
                      private val quizRepo: QuizRepo,
                      private val quizroomStudentRepo: QuizroomStudentRepo, private val userRepo: UserRepository,
                      private val moduleRepo : ModuleRepository, private val lessonRepository: LessonRepository,
                      private val weekRepository: WeekRepository, private val cuRepository: CourseUserRepository,
                      private val logRepository: LogRepository,private val qiRepo : QuestionInstanceRepo,
                      private val logService: LogService
                      )
    : IServiceImplOld<Quizroom, QuizroomFindDTO, QuizroomGetDTO, QuizroomCreateDTO, QuizroomUpdateDTO>(repository, userRepo) {

    /**
     * Retrieves quiz rooms based on the provided room password.
     * If the room with the specified password exists and is in an active state, it returns the rooms as a list of QuizroomFindDTO.
     * Otherwise, it throws a ResponseStatusException with a NOT_FOUND status.
     *
     * @param roomPassword The password of the room to be used for fetching quiz rooms.
     * @param user The authenticated user making the request (optional).
     * @return A list of QuizroomFindDTO if the room with the specified password exists and is active.
     * @throws ResponseStatusException with NOT_FOUND status if no room with the specified password exists.
     */
    fun getByPassword(roomPassword : String, user : UserAuthenticateDto?) = tryCatch{
        val userEntity = try { getUser(user) } catch (_: Exception) { null }
        if(repository.existsByRoomPassword(roomPassword)){
            repository.getAllByRoomPasswordAndRoomState(roomPassword,true).map { it.toFindDTO() }
        }
        else{
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }

    /**
     * Retrieves all quiz rooms associated with a specific user and quiz.
     *
     * @param studentId The ID of the student for whom the quiz rooms are being retrieved.
     * @param quizId The ID of the quiz for which the rooms are being retrieved.
     * @param user The authenticated user making the request (optional).
     * @return A list of QuizroomListDTO for the specified user and quiz.
     */
    fun getAllByUserAndQuiz(studentId: Int, quizId: Int, user : UserAuthenticateDto?) = tryCatch {
        val allRooms = getAllRoomsByUserId(studentId, user)

        val filteredRooms = allRooms.map{ repository.findById(it.quizroom.id).get() }.filter { it.quiz.id == quizId }
        filteredRooms.map { it.toListDTO(allRooms) }

    }

    /**
     * Retrieves the last quiz room for a specific user in a given module.
     *
     * @param studentId The ID of the student whose last quiz room is being retrieved.
     * @param moduleId The ID of the module for which the quiz room is being retrieved.
     * @param user The authenticated user making the request (optional).
     * @throws ResponseStatusException with NOT_FOUND status if the module does not exist.
     * @return The last quiz room associated with the specified user and module as QuizroomGetDTO.
     */
    fun getLastRoomByUser(studentId: Int, moduleId: Int, user : UserAuthenticateDto?) = tryCatch {

        var ret = QuizroomGetDTO(-1, "", "", -1, emptyList(), 0, false, 0, "")

        if (!moduleRepo.existsById(moduleId))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val module = moduleRepo.findById(moduleId).get()

        val quiz = quizRepo.getByModule(module)

        val allRooms = getAllByUserAndQuiz(studentId, quiz.id, user)
        if (allRooms.isNotEmpty()) {
            val maxItemDTO = allRooms.maxBy { it.id }
            val maxItem = repository.findById(maxItemDTO.id).get()

            ret = maxItem.toGetDTO()
        }

        ret
    }

    fun getActiveRoomsByLessonId(lessonId: Int, user: UserAuthenticateDto?) = tryCatch {
        if (!lessonRepository.existsById(lessonId))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val lesson = lessonRepository.findById(lessonId).get()
        val modules = lesson.modules.map { it.module }
        val quizModules = modules.filter { it.type == ModuleType.QUIZ }
        val quizModuleIds = quizModules.map { it.id }

        val activeRooms = repository.getAllActive()

        activeRooms.filter { quizModuleIds.contains(it.quiz.module.id) }
            .map { it.toFindDTO() }

    }

    /**
     * Creates a self-test based on the provided DTO and authenticated user details.
     *
     * @param dto The data transfer object containing details for creating the self-test. This includes the quiz ID,
     * lesson ID, self-test length, and time control level.
     * @param user The authenticated user performing the action. This is optional and can be null.
     * @throws ResponseStatusException If the specified quiz ID does not exist, or if the user details are invalid.
     * Additionally, various HTTP status exceptions can be thrown for different error scenarios.
     * @return SelftestGetDTO The created self-test object after being persisted.
     */
    fun createSelftest(dto: SelftestCreateDTO, user: UserAuthenticateDto?) = tryCatch {
        val userEntity = getUser(user)

        if(!quizRepo.existsById(dto.quiz))
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)

        val quiz = quizRepo.findById(dto.quiz).get()

        val toPick = dto.selftestLength
        val pickedIndexes = (0..quiz.numOfQuestions - 1).shuffled().take(toPick)


        val pickedQuestions = (0..pickedIndexes.size - 1).map {
            quiz.questions[pickedIndexes[it]]
        }


        val room = repository.saveAndFlush(dto.toSelftestEntity())

        quizroomStudentRepo.saveAndFlush(QuizroomStudent(userEntity, room,0))


        var order = 1
        pickedQuestions.forEach {

            qiRepo.saveAndFlush(QuestionInstance(room, it.question, order))

            order++
        }

        room.toSelftestDTO()
    }

    /**
     * Retrieves a self-test based on the provided ID.
     *
     * @param id The unique identifier of the self-test to be retrieved.
     * @param user The authenticated user making the request (optional).
     * @throws ResponseStatusException with NOT_FOUND status if no self-test with the specified ID exists.
     * @return The retrieved self-test as a SelftestGetDTO.
     */
    fun getSelftestByID(id : Int, user: UserAuthenticateDto?) = tryCatch {
        if(!repository.existsById(id))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val room = repository.findById(id).get()

        room.toSelftestDTO()
    }

    /**
     * Creates a new Quizroom based on the provided data transfer objects.
     *
     * @param dto The data transfer object containing the details required to create a Quizroom.
     * @param userDto The data transfer object containing user authentication information; can be null.
     * @return A data transfer object representing the created Quizroom.
     */
    @Transactional
    override fun create(dto: QuizroomCreateDTO, userDto: UserAuthenticateDto?): QuizroomGetDTO {

        canStartQuiz(dto.lesson,userDto)

        val room = repository.saveAndFlush(dto.toEntity())

        logService.log(userDto, room, "create")

        return room.toGetDTO()
    }

    /**
     * Updates an existing Quizroom based on the provided data transfer object and user authentication details.
     *
     * @param id the unique identifier of the Quizroom to be updated.
     * @param dto the data transfer object containing updated information for the Quizroom.
     * @param userDto the data transfer object containing user authentication details, can be null.
     * @return the updated Quizroom data transfer object.
     */
    override fun update(id: Int, dto: QuizroomUpdateDTO, userDto: UserAuthenticateDto?): QuizroomGetDTO {
        if(!repository.existsById(id))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val oldRoom = repository.findById(id)

        if(oldRoom.get().quiz.module.type != ModuleType.SELFTEST)
            canStartQuiz(dto.lesson,userDto)


        val newRoom = repository.saveAndFlush(oldRoom.get().merge(dto))

        logService.log(userDto, newRoom, "update")

        return newRoom.toGetDTO()
    }

    /**
     * Transforms a Quizroom entity into a QuizroomGetDTO.
     *
     * @return The transformed QuizroomGetDTO containing the details of the quiz room, such as
     *         ID, creator, room password, quiz ID, list of students' usernames, current question,
     *         room state, time left, and quiz state.
     */

    override fun Quizroom.toGetDTO() : QuizroomGetDTO {
        val studsInRoom = quizroomStudentRepo.findAllByQuizroom(this)
        return QuizroomGetDTO(id, createdBy,roomPassword, quiz.id, studsInRoom.map{ it.student.username}, currQuestion, roomState, timeLeft, quizState)
    }

    /**
     * Converts a Quizroom entity into a QuizroomFindDTO.
     *
     * @return The QuizroomFindDTO containing the details of the quiz room, including ID, creator, room password, quiz ID,
     *         list of students' IDs, current question, room state, time left, and quiz state.
     */
    override fun Quizroom.toFindDTO() : QuizroomFindDTO {
        val studsInRoom = quizroomStudentRepo.findAllByQuizroom(this)
        return QuizroomFindDTO(id, createdBy, roomPassword, quiz.id, studsInRoom.map { it.student.id }, currQuestion, roomState, timeLeft, quizState)
    }

    /**
     * Converts a QuizroomCreateDTO into a Quizroom entity.
     *
     * @return A Quizroom entity based on the details provided in QuizroomCreateDTO.
     * @throws ResponseStatusException if the quiz with the provided ID does not exist.
     */
    override fun QuizroomCreateDTO.toEntity(): Quizroom {
        val tmp : Quiz
        if(quizRepo.existsById(quiz)){
            tmp = quizRepo.findById(quiz).get()

            var passwd = generatePassword()
            while(repository.existsByRoomPassword(passwd)){
                passwd = generatePassword()
            }


            return Quizroom(createdBy,passwd,0,0,true, "waitingRoom", tmp, emptyList())
        }
        throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    /**
     * Merges the Quizroom entity with the updated fields provided in the QuizroomUpdateDTO.
     *
     * @param dto The data transfer object containing the updated fields for the Quizroom entity.
     * @return The updated Quizroom entity after merging the provided fields.
     * @throws ResponseStatusException if any of the student IDs in dto do not exist or if the quiz ID in dto does not exist.
     */
    override fun Quizroom.merge(dto: QuizroomUpdateDTO): Quizroom {

        dto.students?.forEach{
            if(!userRepo.existsById(it))
                throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }

        val tmp : Quiz = if (dto.quiz?.let { quizRepo.existsById(it) } == true) {
            quizRepo.findById(dto.quiz).get()
        } else {
            quiz
        }

        return Quizroom(
            dto.createdBy ?: createdBy,
            dto.roomPassword ?: roomPassword,
            dto.currQuestion ?: currQuestion,
            dto.timeLeft ?: timeLeft,
            dto.roomState ?: roomState,
            dto.quizState ?: quizState,
            tmp,
            answersInRoom,
            id
        )
    }

    /**
     * Generates a random 6-digit password using numerical characters.
     *
     * @return A randomly generated 6-digit password as a String.
     */
    private fun generatePassword() : String{
        val allowedChars = ('0'..'9')
        return (1..6)
            .map { allowedChars.random() }
            .joinToString("")

    }

    /**
     * Converts a Quizroom entity into a QuizroomListDTO.
     *
     * @param pointList The list of QuizroomStudent objects to filter by the current Quizroom instance.
     * @return The QuizroomListDTO containing the filtered details of the quiz room such as ID, creator,
     * room password, quiz ID, list of students' IDs, current question, room state, time left, quiz state, and points.
     */
    private fun Quizroom.toListDTO(pointList :List<QuizroomStudent>) : QuizroomListDTO {
        val tmp = pointList.filter { it.quizroom.id == id }
        val studsInRoom = quizroomStudentRepo.findAllByQuizroom(this)
        return QuizroomListDTO(id, createdBy, roomPassword, quiz.id, studsInRoom.map { it.student.id }, currQuestion, roomState, timeLeft, quizState, tmp[0].points)
    }

    /**
     * Converts the current Quizroom instance into a SelftestGetDTO.
     *
     * @return The corresponding SelftestGetDTO containing the details of
     *         the quiz room.
     */
    private fun Quizroom.toSelftestDTO() : SelftestGetDTO {

        val studsInRoom = quizroomStudentRepo.findAllByQuizroom(this)

        val questions = qiRepo.findAllByQuizroom(this)

        return SelftestGetDTO(id, createdBy,roomPassword, quiz.id, studsInRoom.map{ it.student.username},
            currQuestion, roomState, timeLeft, quizState, questions.map{ it.question.id})
    }

    /**
     * Converts a SelftestCreateDTO to a Quizroom entity.
     * This method checks the existence of the quiz associated with the self-test and determines the time
     * left for the self-test based on its difficulty level.
     *
     * @throws ResponseStatusException if the quiz does not exist or if the time control is invalid.
     * @return A Quizroom entity initialized with the self-test details.
     */
    private fun SelftestCreateDTO.toSelftestEntity(): Quizroom{
        if(!quizRepo.existsById(quiz))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)


        val quizEntity = quizRepo.findById(quiz).get()
        val timeLeft : Int
        if(timeControl == "EASY")
            timeLeft = selftestLength * 60
        else if(timeControl == "MEDIUM")
            timeLeft = selftestLength * 30
        else if(timeControl == "HARD")
            timeLeft = selftestLength * 20
        else if(timeControl == "HELL")
            timeLeft = selftestLength * 10
        else
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)

        return Quizroom(createdBy,"SELFTEST",0, timeLeft,true, "ANSWERING", quizEntity, emptyList())

    }

    /**
     * Converts a QuizroomStudent entity into a QuizroomStudentFindDTO.
     *
     * This method is an extension function of the QuizroomStudent class that maps its properties
     * to a QuizroomStudentFindDTO. This DTO contains the necessary details needed to represent
     * a student within a quiz room, such as the student's username, the corresponding quiz room's
     * ID, and the points the student has earned.
     *
     * @return A QuizroomStudentFindDTO containing the details of the QuizroomStudent.
     */
    private fun QuizroomStudent.toFindDTO() = QuizroomStudentFindDTO(id, student.username, quizroom.id, points)

    /**
     * Retrieves all quiz rooms associated with the specified student.
     *
     * @param studentId The ID of the student for whom the quiz rooms are being retrieved.
     * @param user The authenticated user making the request (optional).
     * @return A list of QuizroomStudent for the specified student.
     */
    private fun getAllRoomsByUserId(studentId: Int, user : UserAuthenticateDto?) : List<QuizroomStudent> {
        try { getUser(user) } catch (_: Exception) {  }
        if(!userRepo.existsById(studentId))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val stud = userRepo.findById(studentId).get()

        return quizroomStudentRepo.findAllByStudent(stud)
    }

    /**
     * Determines if a quiz can be started based on the lesson and user authentication status.
     *
     * @param lessonId The ID of the lesson for which the quiz is being started.
     * @param userDto The authenticated user initiating the action, might be null.
     * @throws ResponseStatusException If the lesson or week associated with the lesson does not exist,
     * or if the user is not authorized to start the quiz.
     */
    private fun canStartQuiz(lessonId: Int, userDto: UserAuthenticateDto?){
        val userEntity = getUser(userDto)

        if(!lessonRepository.existsById(lessonId))
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)

        val lesson = lessonRepository.findById(lessonId).get()

        if(!weekRepository.existsById(lesson.week.id))
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)

        val week = weekRepository.findById(lesson.week.id).get()

      //  val teachers = cuRepository.findByCourse(week.course)

        if(!authorizationService.isTrusted(userEntity, week.course))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }



}