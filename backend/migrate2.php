<?php

const DB_HOST = "localhost:3336";
const DB_NAME = "devdb";
const DB_USER = "root";
const DB_PASS = "root";

$pdo = new \PDO("mysql:host=".DB_HOST.";dbname=".DB_NAME, DB_USER, DB_PASS);

// Migrate courses
$st = $pdo->prepare("SELECT * FROM `course` WHERE 1");
$st->execute();

$SUBJECT_START_ID = 1;
$SEMESTER_START_ID = 1;
$WEEK_START_ID = 1;

$subjectSt = $pdo->prepare("SELECT * FROM `subject` WHERE `code` = :code");
$createSubjectSt = $pdo->prepare("INSERT INTO `subject` (`id`, `code`, `name`) VALUES (:id, :code, :code)");
$updateSubjectSt = $pdo->prepare("UPDATE `course` SET `subject_id` = :subjectId WHERE `id` = :id");

$semesterSt = $pdo->prepare("SELECT * FROM `semester` WHERE `code` = :code");
$createSemesterSt = $pdo->prepare("INSERT INTO `semester` (`id`, `code`, `from`, `until`) VALUES (:id, :code, :from, :until)");
$updateSemesterSt = $pdo->prepare("UPDATE `course` SET `semester_id` = :semesterId WHERE `id` = :id");

$weekSt = $pdo->prepare("SELECT * FROM `week` WHERE `course_id` = :courseId LIMIT 1");
$createWeekSt = $pdo->prepare("INSERT INTO `week` (`id`, `name`, `from`, `until`, `course_id`) VALUES (:id, :name, :from, :until, :courseId)");
$updateLessonSt = $pdo->prepare("UPDATE `lesson` SET `week_id` = :weekId WHERE `course_id` = :courseId");

foreach ($st->fetchAll(PDO::FETCH_OBJ) as $course) {
    $subjectSt->execute(['code' => $course->code]);
    $subject = $subjectSt->fetch(PDO::FETCH_OBJ);
    if (!$subject) {
        $subjectId = $SUBJECT_START_ID;
        $createSubjectSt->execute(['id' => $SUBJECT_START_ID++, 'code' => $course->code]);
    }
    else
        $subjectId = $subject->id;

    $updateSubjectSt->execute(['subjectId' => $subjectId, 'id' => $course->id]);
    echo "Updated subject for course " . $course->id . "\n";

    $semesterSt->execute(['code' => $course->semester]);
    $semester = $semesterSt->fetch(PDO::FETCH_OBJ);
    if (!$semester) {
        $semesterId = $SEMESTER_START_ID;
        $createSemesterSt->execute(['id' => $SEMESTER_START_ID++, 'code' => $course->semester,
            'from' => date('Y-m-d H:i:s'), 'until' => date('Y-m-d H:i:s')]);
    }
    else
        $semesterId = $semester->id;

    $updateSemesterSt->execute(['semesterId' => $semesterId, 'id' => $course->id]);
    echo "Updated semester for course " . $course->id . "\n";

    $weekSt->execute(['courseId' => $course->id]);
    $week = $weekSt->fetch(PDO::FETCH_OBJ);
    if (!$week) {
        $weekId = $WEEK_START_ID;
        $createWeekSt->execute(['id' => $WEEK_START_ID++, 'name' => 'Týden', 'from' => date('Y-m-d H:i:s'),
            'until' => date('Y-m-d H:i:s'), 'courseId' => $course->id]);
        echo "Created week for course " . $course->id . "\n";
    }
    else
        $weekId = $week->id;

    $updateLessonSt->execute(['courseId' => $course->id, 'weekId' => $weekId]);
    echo "Set week instead for lessons in course " . $course->id . "\n";
}

// Migrate tests and files
$codeSt = $pdo->prepare("SELECT * FROM `code_module` WHERE 1");
$codeSt->execute();

$CODE_TEST_START_ID = 1;
$CODE_FILE_START_ID = 1;
$createCodeTest = $pdo->prepare("INSERT INTO `code_module_test` (`id`, `check_memory`, `description`," .
    " `name`, `parameter`, `should_fail`, `time_limit`, `code_module_id`) VALUES (:id, true, '', :name," .
    " :param, :shouldFail, NULL, :cmId)");
$createCodeFile = $pdo->prepare("INSERT INTO `code_module_file` (`id`, `name`, `code_limit`, `header_file`," .
    " `content`, `code_module_id`) VALUES (:id, 'student.cpp', :codeLimit, false, :content, :cmId)");

foreach ($codeSt->fetchAll(PDO::FETCH_OBJ) as $code) {
    $paramRightStr = $code->param_right ? explode(", ", $code->param_right) : [];
    foreach ($paramRightStr as $paramRight)
        $createCodeTest->execute(['id' => $CODE_TEST_START_ID++, 'name' => "Test $paramRight", 'param' => intval($paramRight),
            'shouldFail' => 0, 'cmId' => $code->id]);

    $paramWrongStr = $code->param_wrong ? explode(", ", $code->param_wrong) : [];
    foreach ($paramWrongStr as $paramWrong)
        $createCodeTest->execute(['id' => $CODE_TEST_START_ID++, 'name' => "Test $paramWrong", 'param' => intval($paramWrong),
            'shouldFail' => 1, 'cmId' => $code->id]);

    echo "Updated tests for code module " . $code->id . "\n";

    $createCodeFile->execute(['id' => $CODE_FILE_START_ID++, 'codeLimit' => $code->code_limit,
        'content' => $code->code_shown, 'cmId' => $code->id]);
    echo "Updated file for code module " . $code->id . "\n";
}

// Update library
$updateLibEnvType = $pdo->prepare("UPDATE `code_module` SET `library_type` = 1, `envelope_type` = 1, `custom_envelope` = NULL WHERE 1");
$updateLibEnvType->execute();
