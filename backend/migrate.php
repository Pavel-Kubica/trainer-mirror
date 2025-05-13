<?php

const DB_HOST = "localhost:3306";
const DB_NAME = "progtrain";
const DB_USER = "root";
const DB_PASS = "root";

$pdo = new \PDO("mysql:host=".DB_HOST.";dbname=".DB_NAME, DB_USER, DB_PASS);

$st = $pdo->prepare("SELECT * FROM `module` WHERE 1");
$st->execute();

const TYPE_QUIZ = 0;
const TYPE_CODE = 1;
const TYPE_TEXT = 2;
const TYPE_ASSIGNMENT = 3;

$updateAssignment = $pdo->prepare("UPDATE `module` SET `assignment` = :assignment WHERE `id` = :id");
$createCode = $pdo->prepare("INSERT INTO `code_module` ".
    "(id, code_hidden, code_limit, file_limit, code_shown, code_type, param_right, param_wrong, module_id) VALUES ".
    "(:id, :codeHidden, :codeLimit, :fileLimit, :codeShown, :codeType, :paramRight, :paramWrong, :id)");
$codeMappingTable = [
    "SHOWCASE" => 0, "TEST_ASSERT" => 1, "TEST_IO" => 2, "WRITE_ASSERT" => 3, "WRITE_IO" => 4
];


foreach ($st->fetchAll(PDO::FETCH_OBJ) as $module) {
    if ($module->type === TYPE_QUIZ)
        continue;

    // Migrate assignment
    if ($module->type === TYPE_TEXT || $module->type === TYPE_ASSIGNMENT || $module->type === TYPE_CODE) {
        $data = json_decode($module->data);
        $updateAssignment->execute(['id' => $module->id, 'assignment' => $data->assignment ?? '']);
        echo "Updating assignment for " . $module->name . PHP_EOL;

        // Code module
        if ($module->type === TYPE_CODE) {
            // Migrate the rest
            $createCode->execute([
                'id' => $module->id, 'codeHidden' => $data->codeHidden ?? '', 'codeLimit' => $data->codeLimit ?? 1024,
                'codeShown' => $data->codeShown ?? '', 'codeType' => $codeMappingTable[$data->codeType],
                'paramRight' => $data->paramRight ?? null, 'paramWrong' => $data->paramWrong ?? null,
                'fileLimit' => $data->fileLimit ?? 10240
            ]);
            echo "Updating code data for " . $module->name . PHP_EOL;
            continue;
        }
        continue;
    }

    echo "Unrecognized module type: " . $module->type . PHP_EOL;
}

$stm = $pdo->prepare("SELECT * FROM `student_module` WHERE 1");
$stm->execute();

const REQUEST_TYPE_HELP = 0;
const REQUEST_TYPE_COMMENT = 1;
const REQUEST_TYPE_EVALUATE = 2;

$startingId = 1; // <-- CHANGE this
$teacherId = 3; // <-- CHANGE this (3 = Honza)

$createSrCompleted = $pdo->prepare("INSERT INTO `student_module_request` " .
    "(id, request_text, request_type, teacher_response, sm_id, teacher_id, satisfied, requested_on, satisfied_on) VALUES " .
    "(:id, '', :requestType, :teacherResponse, :smId, :teacherId, :satisfied, NOW(), :satisfiedOn)");

// Migrate "needs help" requests
foreach ($stm->fetchAll(PDO::FETCH_OBJ) as $studentModule) {
    if (empty($studentModule->data))
        continue; // nothing to parse

    $smData = json_decode($studentModule->data);
    // Already solved request
    if ($smData->teacherComment ?? false) {
        $createSrCompleted->execute([
            'id' => $startingId++, 'requestType' => REQUEST_TYPE_EVALUATE,
            'teacherResponse' => $smData->teacherComment, 'smId' => $studentModule->id,
            'teacherId' => $teacherId, 'satisfied' => intval(!($studentModule->needs_help ?? false)),
            'satisfiedOn' => date('Y-m-d H:i:s')
        ]);
        echo "Creating SUBMIT request for " . $studentModule->id . PHP_EOL;
        continue;
    }

    // Unsolved request
    if ($studentModule->needs_help ?? false) {
        $createSrCompleted->execute([
            'id' => $startingId++, 'requestType' => REQUEST_TYPE_HELP,
            'teacherResponse' => null, 'smId' => $studentModule->id,
            'teacherId' => null, 'satisfied' => intval(false), 'satisfiedOn' => null,
        ]);
        echo "Creating HELP request for " . $studentModule->id . PHP_EOL;
        continue;
    }

    echo "Nothing to update on " . $studentModule->id . PHP_EOL;
}
