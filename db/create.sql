SET NAMES utf8;
SET time_zone = '+00:00';
SET foreign_key_checks = 0;

SET NAMES utf8mb4;

create table ids
(
    table_name varchar(255) not null primary key,
    id_value   bigint       null
);

create table role
(
    id    int     not null primary key,
    level tinyint not null,
    check (`level` between 0 and 1)
);

create table user
(
    id                     int                                  not null primary key,
    login_secret           varchar(255)                         not null,
    name                   varchar(255)                         not null,
    username               varchar(255)                         not null,
    last_notification_time timestamp  default CURRENT_TIMESTAMP not null,
    is_admin               tinyint(1) default 0                 not null,
    constraint UK_ipgey2i9peau3ka21mrse0oxm
        unique (login_secret),
    constraint UK_sb8bbouer5wak8vyiiy4pf2bx
        unique (username)
);

create table semester
(
    id      int          not null primary key,
    code    varchar(255) not null,
    `from`  datetime(6)  not null,
    `until` datetime(6)  not null
);

create table subject
(
    id   int          not null primary key,
    code varchar(255) not null,
    name varchar(255) not null
);

create table course
(
    id          int           not null primary key,
    name        varchar(255)  not null,
    public      bit default 0 not null,
    secret      varchar(255)  null,
    semester_id int           null,
    subject_id  int           null,
    short_name  varchar(255)  not null,
    constraint UK_k8ragf131a70ej0oi4qmfyjxf
        unique (secret),
    constraint FKlmyb73uymsfhqh374ndr3n4c0
        foreign key (semester_id) references semester (id)
            on delete set null,
    constraint FKm1expnaas0onmafqpktmjixnx
        foreign key (subject_id) references subject (id)
            on delete set null
);

create table course_user
(
    id        int not null primary key,
    course_id int not null,
    role_id   int not null,
    user_id   int not null,
    constraint UKswc8cdk498136cqr4bi8h2voi
        unique (user_id, course_id),
    constraint FK2e2vx1koh906052eej4244e0v
        foreign key (user_id) references user (id)
            on delete cascade,
    constraint FK77b3uh2xy0xstu5buynepeay8
        foreign key (role_id) references role (id)
            on delete cascade,
    constraint FKm2sw6qb4f42s0be8drns7d71r
        foreign key (course_id) references course (id)
            on delete cascade
);

create table week
(
    id        int          not null primary key,
    `from`    datetime(6)  not null,
    name      varchar(255) null,
    `until`   datetime(6)  not null,
    course_id int          not null,
    constraint FKiyakwowuwtqbffvkq25dws43i
        foreign key (course_id) references course (id)
            on delete restrict
);

create table lesson
(
    id          int           not null primary key,
    hidden      bit default 0 not null,
    name        varchar(255)  not null,
    `order`     int           not null,
    lock_code   varchar(255)  null,
    description longtext      not null,
    time_end    datetime(6)   null,
    time_start  datetime(6)   null,
    type        tinyint       not null check (type between 0 and 5),
    week_id     int           not null,
    constraint FKsfd0d1wsp4owcj741pumf18ok
        foreign key (week_id) references week (id)
            on delete cascade
);

create table module
(
    id                     int                                  not null primary key,
    file                   varchar(255)                         null,
    name                   varchar(255)                         not null,
    type                   tinyint                              not null check (type between 0 and 3),
    author_id              int                                  not null,
    difficulty             tinyint                              null check (`difficulty` between 0 and 4),
    assignment             longtext                             not null,
    lockable               tinyint(1) default 0                 not null,
    manual_eval            tinyint(1) default 0                 not null,
    min_percent            int                                  not null,
    time_limit             tinyint(1) default 0                 not null,
    last_modification_time timestamp  default CURRENT_TIMESTAMP not null,
    hidden                 tinyint(1) default 0                 not null,
    constraint FKkmks7x8a0xl5hxr3jyms52r6w
        foreign key (author_id) references user (id)
            on delete restrict
);

create table lesson_module
(
    id         int not null primary key,
    `order`    int not null,
    lesson_id  int not null,
    module_id  int not null,
    depends_on int null,
    constraint UK2fxb99kj9gcs125wertgb1nxq
        unique (lesson_id, module_id),
    constraint FK6ko39ymw6i4t9v28qg27i48tx
        foreign key (lesson_id) references lesson (id)
            on delete cascade,
    constraint FKbu0advuh6mb839t8m81or98nd
        foreign key (depends_on) references module (id)
            on delete set null,
    constraint FKlmk73u1wp5ukme8rvyyc6t1ms
        foreign key (module_id) references module (id)
            on delete cascade
);

create table student_module
(
    id              int                  not null primary key,
    allowed_show    tinyint(1) default 0 not null,
    file            varchar(255)         null,
    percent         int                  null,
    lesson_id       int                  not null,
    module_id       int                  not null,
    student_id      int                  not null,
    completed_early tinyint(1) default 0 not null,
    completed_on    datetime(6)          null,
    opened_on       datetime(6)          null,
    unlocked        tinyint(1) default 0 not null,
    constraint UKe224expxnmxyd9jvdl4cmosca
        unique (student_id, lesson_id, module_id),
    constraint FK31nmqfsj0m9s5v556ah3x614e
        foreign key (module_id) references module (id)
            on delete cascade,
    constraint FKnlbevp9vuri2oxtlynt0sspqq
        foreign key (student_id) references user (id)
            on delete cascade,
    constraint FKt1oxv00vqeq683d65641s1uuh
        foreign key (lesson_id) references lesson (id)
            on delete cascade
);

create table student_module_request
(
    id               int                  not null primary key,
    request_text     longtext             null,
    request_type     tinyint              not null check (`request_type` between 0 and 2),
    requested_on     datetime(6)          not null,
    satisfied        tinyint(1) default 0 not null,
    satisfied_on     datetime(6)          null,
    teacher_response longtext             null,
    sm_id            int                  not null,
    teacher_id       int                  null,
    constraint FK98c8y8gmaogohnusdslmar3jy
        foreign key (teacher_id) references user (id)
            on delete cascade,
    constraint FKgawxbj70d4thy8sd977gys1pi
        foreign key (sm_id) references student_module (id)
            on delete cascade
);

-- CodeModule

create table code_module
(
    id               int                  not null primary key,
    code_hidden      longtext             not null,
    code_type        tinyint              not null check (code_type between 0 and 4),
    file_limit       int                  not null,
    module_id        integer              not null,
    interaction_type tinyint              not null check (interaction_type between 0 and 1),
    custom_envelope  longtext             null,
    envelope_type    tinyint              not null check (envelope_type between 0 and 4),
    library_type     tinyint              not null check (library_type between 0 and 2),
    reference_public tinyint(1) default 0 not null,
    constraint UK_kpftdltp693h4xwkhghothjlk
        unique (module_id),
    constraint FKayfl9bbtf039f0msqdce3d8vv
        foreign key (module_id) references module (id)
            on delete cascade
);

create table code_module_file
(
    id             int                  not null primary key,
    code_limit     int                  not null,
    content        longtext             not null,
    header_file    tinyint(1) default 0 not null,
    name           varchar(255)         not null,
    code_module_id int                  not null,
    reference      longtext             not null,
    constraint FKm0te2x0o6e50yby49s9lt9k5i
        foreign key (code_module_id) references code_module (id)
            on delete cascade
);

create table code_module_test
(
    id             int                  not null primary key,
    check_memory   tinyint(1) default 0 not null,
    description    longtext             not null,
    name           varchar(255)         not null,
    parameter      int                  not null,
    should_fail    tinyint(1) default 0 not null,
    time_limit     int                  null,
    code_module_id int                  not null,
    hidden         tinyint(1) default 0 not null,
    constraint FKo04s77sf7xsvditaov8asg2ew
        foreign key (code_module_id) references code_module (id)
            on delete cascade
);

create table code_comment
(
    id           int          not null primary key,
    `row_number` int          not null,
    smr_id       int          not null,
    file_name    varchar(255) not null,
    comment      longtext     not null,
    constraint FK95waskwimc29qg9gug8esthqi
        foreign key (smr_id) references student_module_request (id)
            on delete cascade
);

create table module_editor
(
    id        int not null primary key,
    editor_id int not null,
    module_id int not null,
    constraint UKl8r2ji3dvkvgou477fptt5k4e
        unique (editor_id, module_id),
    constraint FKmi4ekcvo3evjk7xb4i9t58tw6
        foreign key (editor_id) references user (id)
            on delete cascade,
    constraint FK2q4j8tfnp3s7nmpy6p3evwqnp
        foreign key (module_id) references module (id)
            on delete cascade
);

create table module_subject
(
    id         int not null primary key,
    module_id  int not null,
    subject_id int not null,
    constraint UKbo26c4ce0v6ef2smk0wo2r8d
        unique (module_id, subject_id),
    constraint FK30kjtg4o58ingjk0nl80yyuca
        foreign key (module_id) references module (id)
            on delete cascade,
    constraint FKi4i8536ty3sv6ys4l3g6e11kw
        foreign key (subject_id) references subject (id)
            on delete cascade
);

create table student_rating
(
    id                int          not null primary key,
    points            int,
    student_module_id integer      not null unique,
    published         timestamp(6) not null,
    text              varchar(255),
    constraint FKt0mflfgeufssisfjx5wmemouf
        foreign key (student_module_id) references student_module (id)
            on delete cascade
);

create table teacher_note
(
    id        int                                 not null primary key,
    module_id int                                 not null,
    redacted  tinyint(1)                          not null,
    user_id   int                                 not null,
    created   timestamp default CURRENT_TIMESTAMP not null,
    content   text                                not null,
    constraint FKrmnrjhcoi9l7ra204u8j5wcn0
        foreign key (user_id) references user (id)
            on delete cascade,
    constraint FKacvfeqg3w0ceojapb5h1jmgd5
        foreign key (module_id) references module (id)
            on delete cascade
);

create table topic
(
    id   int          not null primary key,
    name varchar(255) not null
);

create table module_topic
(
    id        int not null primary key,
    module_id int not null,
    topic_id  int not null,
    constraint UKk16su3mxkon7nkgq6w42o8nch
        unique (module_id, topic_id),
    constraint FKo051j4s75dea0s0ao9yp1qs3b
        foreign key (module_id) references module (id)
            on delete cascade,
    constraint FK8riunvcf2tavmxquifrf9r641
        foreign key (topic_id) references topic (id)
            on delete cascade
);

create table subject_guarantor
(
    id           int not null primary key,
    subject_id   int not null,
    guarantor_id int not null,
    constraint UKt64mbdvpwb36bjhtxvogiif69
        unique (guarantor_id, subject_id),
    constraint FKc7beaa5qky3glsmijti1p6ipk
        foreign key (guarantor_id) references user (id)
            on delete cascade,
    constraint FKgssbvf1ircqmovp0daqt76tus
        foreign key (subject_id) references subject (id)
            on delete cascade
);

-- QuizModule

create table quiz
(
    id               int          not null primary key,
    name             varchar(255) not null,
    num_of_attempts  int          null,
    num_of_questions int          not null,
    module_id        int          null,
    constraint UK_dwt7mqcvaqam3nb991q281wkt
        unique (module_id),
    constraint FKd3pi74lbnexp6xjb3595p9088
        foreign key (module_id) references module (id)
            on delete cascade
);

create table quizroom
(
    id            int          not null primary key,
    created_by    varchar(255) not null,
    room_password varchar(255) not null,
    quiz_id       int          null,
    curr_question int          not null,
    room_state    bit          not null,
    time_left     int          not null,
    quiz_state    varchar(255) null,
    constraint FKria70l4biio2lefvvuijk42u2
        foreign key (quiz_id) references quiz (id)
            on delete cascade
);

create table quizroom_student
(
    id          int          not null
        primary key,
    points      int          not null,
    quizroom_id int          null,
    user_id     int          not null,
    quiz_state  varchar(255) null,
    constraint UK1q5filukiuf2gmcbmf5q6vw4w
        unique (user_id, quizroom_id),
    constraint FK6r64p5an3mxo2dbgbhjcxs020
        foreign key (quizroom_id) references quizroom (id)
            on delete cascade,
    constraint FKht15blsq56ay5xau54eqhq02v
        foreign key (user_id) references user (id)
            on delete cascade
);

create table question
(
    id                    int          not null
        primary key,
    correct_answer_data   longtext     not null,
    possible_answers_data longtext     not null,
    question_data         longtext     not null,
    time_limit            int          not null,
    explanation           longtext     null,
    author                varchar(255) null,
    single_answer         bit          not null,
    question_type         tinyint      null check (`question_type` between 0 and 6)
);

create table quiz_question
(
    id          int not null primary key,
    order_num   int not null,
    question_id int not null,
    quiz_id     int not null,
    constraint FK62empq7vfu15qv1kci624f1js
        foreign key (question_id) references question (id)
            on delete cascade,
    constraint FKdtynvfjgh6e7fd8l0wk37nrpc
        foreign key (quiz_id) references quiz (id)
            on delete cascade
);

create table student_answer
(
    id          int      not null primary key,
    answer_data longtext null,
    question_id int      null,
    quizroom_id int      null,
    user_id     int      not null,
    constraint FK20mrv6r77tm750sp7oha616e1
        foreign key (user_id) references user (id)
            on delete cascade,
    constraint FK2vouf6spj6e0d9kv99g8xkaof
        foreign key (quizroom_id) references quizroom (id)
            on delete cascade,
    constraint FKsvdyurb450wbvvwcd261efibw
        foreign key (question_id) references question (id)
            on delete cascade
);

create table log
(
    entity_id  int          not null primary key,
    id         int          not null,
    timestamp  timestamp(6) not null,
    client     varchar(255) not null,
    entity     varchar(255) not null,
    ip_address varchar(255) not null,
    operation  varchar(255) not null,
    username   varchar(255) not null
);