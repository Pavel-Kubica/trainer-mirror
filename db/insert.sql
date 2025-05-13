SET NAMES utf8;
SET time_zone = '+00:00';
SET foreign_key_checks = 0;
SET sql_mode = 'NO_AUTO_VALUE_ON_ZERO';

SET NAMES utf8mb4;

-- ids
INSERT INTO ids (table_name, id_value) VALUES ('code_module', 42);
INSERT INTO ids (table_name, id_value) VALUES ('code_module_file', 41);
INSERT INTO ids (table_name, id_value) VALUES ('code_module_test', 47);
INSERT INTO ids (table_name, id_value) VALUES ('course', 7);
INSERT INTO ids (table_name, id_value) VALUES ('course_user', 60);
INSERT INTO ids (table_name, id_value) VALUES ('lesson', 72);
INSERT INTO ids (table_name, id_value) VALUES ('lesson_module', 121);
INSERT INTO ids (table_name, id_value) VALUES ('module', 105);
INSERT INTO ids (table_name, id_value) VALUES ('module_editor', 211);
INSERT INTO ids (table_name, id_value) VALUES ('question', 42);
INSERT INTO ids (table_name, id_value) VALUES ('quiz', 31);
INSERT INTO ids (table_name, id_value) VALUES ('quiz_question', 138);
INSERT INTO ids (table_name, id_value) VALUES ('quizroom', 123);
INSERT INTO ids (table_name, id_value) VALUES ('quizroom_student', 147);
INSERT INTO ids (table_name, id_value) VALUES ('role', 2);
INSERT INTO ids (table_name, id_value) VALUES ('student_answer', 178);
INSERT INTO ids (table_name, id_value) VALUES ('student_module', 127);
INSERT INTO ids (table_name, id_value) VALUES ('student_module_request', 64);
INSERT INTO ids (table_name, id_value) VALUES ('user', 22);
INSERT INTO ids (table_name, id_value) VALUES ('week', 39);
insert into ids(table_name, id_value) values ('subject_guarantor',0);
insert into ids(table_name, id_value) values ('subject',0);
insert into ids(table_name, id_value) values ('topic',0);
insert into ids(table_name, id_value) values ('module_topic',0);
insert into ids(table_name, id_value) values ('student_rating',0);
insert into ids(table_name, id_value) values ('semester',0);
insert into ids(table_name, id_value) values ('log',0);
insert into ids(table_name, id_value) values ('code_comment',0);
insert into ids(table_name, id_value) values ('module_subject',0);
insert into ids(table_name, id_value) values ('teacher_note',0);

-- role
INSERT INTO role (id, level) VALUES (1, 0);
INSERT INTO role (id, level) VALUES (2, 1);

-- user
INSERT INTO user (id, login_secret, name, username, last_notification_time, is_admin) VALUES (1, 'Dc241X9fN3aUI18f', 'Testovací student', 'test01', '2024-03-02 10:09:06', 0);
INSERT INTO user (id, login_secret, name, username, last_notification_time, is_admin) VALUES (2, 'ARC2GAgTrMPquqNW', 'Ondřej Wrzecionko', 'wrzecond', '2024-04-04 09:10:17', 1);
INSERT INTO user (id, login_secret, name, username, last_notification_time, is_admin) VALUES (12, 'ASw45fvW0w1ZklEH', 'Testovací Uživatel', 'tester', '2024-04-04 09:12:11',0);
INSERT INTO user (id, login_secret, name, username, last_notification_time, is_admin) VALUES (13, 'GFlhy20blRWlVRJh', 'Testovací Učitel', 'teacher', '2024-02-20 12:30:38', 0);

-- semester
INSERT INTO semester (id, code, `from`, until) VALUES (1, 'A', '2024-01-22 09:50:16.000000', '2024-01-22 09:50:16.000000');

-- subject
INSERT INTO subject (id, code, name) VALUES (1, 'TEST', 'TEST');

-- course
INSERT INTO course (id, name, public, secret, semester_id, subject_id, short_name) VALUES (1, 'Pískoviště', false, 'DEMO', 1, 1, 'Pískoviště');
INSERT INTO course (id, name, public, secret, semester_id, subject_id, short_name) VALUES (5, 'Test', false, 'VFGRvP94UkjyEDy', 1, 1, 'Test');

-- course_user

INSERT INTO course_user (id, course_id, role_id, user_id) VALUES (22, 5, 1, 12);
INSERT INTO course_user (id, course_id, role_id, user_id) VALUES (27, 5, 2, 13);
INSERT INTO course_user (id, course_id, role_id, user_id) VALUES (44, 1, 2, 13);

-- week

INSERT INTO week (id, `from`, name, `until`, course_id) VALUES (8, '2024-01-31 23:00:00.000000', 'TestWeek', '2025-01-31 23:00:00.000000', 5);
INSERT INTO week (id, `from`, name, `until`, course_id) VALUES (9, '2027-02-03 10:02:00.000000', 'Odpadkový koš', '2027-02-24 10:02:00.000000', 1);

-- lesson

INSERT INTO lesson (id, hidden, name, `order`, lock_code, description, time_end, time_start, type, week_id) VALUES (10, false, 'TestLesson', 1, 'testcode', 'Introduction', '2025-01-31 23:00:00.000000', '2024-01-31 23:00:00.000000', 5, 8);
INSERT INTO lesson (id, hidden, name, `order`, lock_code, description, time_end, time_start, type, week_id) VALUES (11, true, 'HiddenLesson', 2, null, '', null, null, 0, 8);

-- module

INSERT INTO module (id, file, name, type, author_id, difficulty, assignment, lockable, manual_eval, min_percent, time_limit, last_modification_time, hidden) VALUES (38, null, 'Řetězce I – převod písmen', 1, 2, null, 'Tvým cílem bude napsat funkci `strtoupper`, která převede všechna malá písmena v zadaném řetězci na velká. Způsob převodu je patrný z dodaných assertů.
', 1, 0, 100, 0, '2024-02-20 10:36:41', 0);
INSERT INTO module (id, file, name, type, author_id, difficulty, assignment, lockable, manual_eval, min_percent, time_limit, last_modification_time, hidden) VALUES (49, null, 'Text module', 2, 2, 2, 'Assignment', 0, 0, 50, 0, '2024-03-18 10:15:40', 0);

-- module_editor
INSERT INTO module_editor (id, editor_id, module_id) VALUES (146, 13, 38);
INSERT INTO module_editor (id, editor_id, module_id) VALUES (201, 13, 49);

-- lesson_module
INSERT INTO lesson_module (id, `order`, lesson_id, module_id, depends_on) VALUES (53, 1, 10, 49, null);

-- code_module

INSERT INTO code_module (id, code_hidden, code_type, file_limit, module_id, interaction_type, custom_envelope, envelope_type, library_type, reference_public) VALUES (11, '#include <ctype.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include "lib/c/trainer.h"

namespace CRef {
    char * strtoupper (char * str);
}

namespace CStudent {
    char * strtoupper (char * str);
}

using namespace CTeacher;

void randStr ( char * str, int minLen, int maxLen ) {
    int len = minLen + (rand() % (maxLen - minLen - 1));
    for (int i = 0; i < len; ++i)
        str[i] = (rand() % 95) + 32;
    str[len] = \'\\0\';
}

#define NUM_TESTS_LONG 1000
void testEdge() {
    srand(time(NULL));
    const int MAX_STR_LEN = 1000000;
    static char strInput [MAX_STR_LEN], strOutput [MAX_STR_LEN], strRef [MAX_STR_LEN];
    for (int i = 0; i < NUM_TESTS_LONG; ++i) {
        randStr (strInput, 500000, MAX_STR_LEN);
        strcpy ( strOutput, strInput );
        strcpy ( strRef, strInput );
        CTester::assertEqual ( swrap(strInput), swrap(CStudent::strtoupper(strOutput)), swrap(CRef::strtoupper(strRef)) );
    }
}

#define NUM_TESTS 100000
void testRandom() {
    CTester::assertBool ( CStudent::strtoupper(NULL) == NULL, "Nezapomen osetrit NULL!" );
    srand(time(NULL));
    const int MAX_STR_LEN = 64;
    char str [MAX_STR_LEN] = {}, strOutput [MAX_STR_LEN], strRef [MAX_STR_LEN];
    randStr (str, 0, MAX_STR_LEN);
    CTester::assertBool ( CStudent::strtoupper(str) == str, "Nealokuj novy retezec!" );
    for (int i = 0; i < NUM_TESTS; ++i) {
        randStr (str, 0, MAX_STR_LEN);
        strcpy ( strOutput, str );
        strcpy ( strRef, str );
        CTester::assertEqual ( swrap(str), swrap(CStudent::strtoupper(strOutput)), swrap(CRef::strtoupper(strRef)) );
    }
}

void testBasic() {
    char str [64];
    strcpy(str, "ahoj");
    CTester::assertEqual ( "ahoj", swrap(CStudent::strtoupper(str)), "AHOJ" );
    strcpy(str, "VELKE");
    CTester::assertEqual ( "VELKE", swrap(CStudent::strtoupper(str)), "VELKE" );
    strcpy(str, "ToTo je string s 123 cisly a znaky.");
    CTester::assertEqual ( "ToTo je string s 123 cisly a znaky.", swrap(CStudent::strtoupper(str)), "TOTO JE STRING S 123 CISLY A ZNAKY." );
}

int main(int argc, char ** argv) {
    int param = argc > 1 ? atoi(argv[1]) : 1;
    void (* testFns [])() = {testBasic, testRandom, testEdge};
    testFns[param == 2 ? 1 : (param == 101 ? 2 : 0)]();
    if (param == 1) testBasic();
    return 0;
}
', 1, 10240, 38, 0, null, 0, 1, 0);
