-- =====================================================================
-- insert_data.sql
-- Demo seed data for Attendee University
--
-- Login password for ALL seeded accounts: Password123!
-- (BCrypt hash below was generated for that exact password)
--
-- Run this AFTER your Spring Boot app has started at least once with
-- ddl-auto=update (so Hibernate has created the tables), or after your
-- Flyway/schema migration has run.
--
-- Safe to re-run: wrapped in a DO block that skips insertion if the
-- admin demo user already exists.
-- =====================================================================

DO $$
DECLARE
v_password_hash   TEXT := '$2b$12$FxX6a4U095wdYaACpbwh7OOqTNtXu4NNTJ7Z3tmEGu6Nk.LsYFO0W'; -- Password123!

    -- Users
    v_admin_id         UUID := gen_random_uuid();
    v_instr1_id        UUID := gen_random_uuid();
    v_instr2_id        UUID := gen_random_uuid();
    v_student1_id      UUID := gen_random_uuid();
    v_student2_id      UUID := gen_random_uuid();
    v_student3_id      UUID := gen_random_uuid();
    v_student4_id      UUID := gen_random_uuid();
    v_student5_id      UUID := gen_random_uuid();

    -- Courses
    v_course_cs101_id  UUID := gen_random_uuid();
    v_course_cs201_id  UUID := gen_random_uuid();
    v_course_math101_id UUID := gen_random_uuid();

    -- Zones
    v_zone_main_id     UUID := gen_random_uuid();
    v_zone_lab_id      UUID := gen_random_uuid();

    -- Groups
    v_group_cs101_a_id UUID := gen_random_uuid();
    v_group_cs201_a_id UUID := gen_random_uuid();
    v_group_math_a_id  UUID := gen_random_uuid();

    -- Sessions
    v_session_active_id  UUID := gen_random_uuid();
    v_session_past1_id   UUID := gen_random_uuid();
    v_session_past2_id   UUID := gen_random_uuid();
    v_session_upcoming_id UUID := gen_random_uuid();

BEGIN

    -- Skip entirely if already seeded
    IF EXISTS (SELECT 1 FROM app_users WHERE email = 'admin@attendee.edu') THEN
        RAISE NOTICE 'Seed data already present — skipping.';
        RETURN;
END IF;

    -- =================================================================
    -- USERS
    -- =================================================================
INSERT INTO app_users (id, name, email, password, phone, student_id, generation, avatar, role, is_verified, is_first_login, is_active, face_registered, device_bound, created_at, updated_at)
VALUES
    (v_admin_id,    'System Admin',      'admin@attendee.edu',      v_password_hash, '+855 12 345 678', NULL,        NULL, NULL, 'ADMIN',      true, false, true, false, false, now(), now()),
    (v_instr1_id,   'Dr. Sarah Chen',     'sarah.chen@attendee.edu', v_password_hash, '+855 12 111 222', NULL,        NULL, NULL, 'INSTRUCTOR', true, false, true, false, false, now(), now()),
    (v_instr2_id,   'Prof. David Kim',    'david.kim@attendee.edu',  v_password_hash, '+855 12 333 444', NULL,        NULL, NULL, 'INSTRUCTOR', true, false, true, false, false, now(), now()),
    (v_student1_id, 'Endy Ong',           'endy.student@attendee.edu', v_password_hash, '+855 12 555 001', 'STU-2024-001', 2024, NULL, 'STUDENT', true, false, true, true,  true,  now(), now()),
    (v_student2_id, 'Lina Sok',           'lina.sok@attendee.edu',  v_password_hash, '+855 12 555 002', 'STU-2024-002', 2024, NULL, 'STUDENT', true, false, true, true,  true,  now(), now()),
    (v_student3_id, 'Pisey Chan',         'pisey.chan@attendee.edu',v_password_hash, '+855 12 555 003', 'STU-2024-003', 2024, NULL, 'STUDENT', true, false, true, false, true,  now(), now()),
    (v_student4_id, 'Ratha Heng',         'ratha.heng@attendee.edu',v_password_hash, '+855 12 555 004', 'STU-2024-004', 2024, NULL, 'STUDENT', true, true,  true, false, false, now(), now()),
    (v_student5_id, 'Sopheak Khun',       'sopheak.khun@attendee.edu', v_password_hash, '+855 12 555 005', 'STU-2023-099', 2023, NULL, 'STUDENT', true, false, false, false, false, now(), now());
-- ^ Sopheak is intentionally inactive (banned) to test that UI state

-- =================================================================
-- COURSES
-- =================================================================
INSERT INTO courses (id, code, name, created_at)
VALUES
    (v_course_cs101_id,  'CS101',  'Introduction to Programming', now()),
    (v_course_cs201_id,  'CS201',  'Data Structures & Algorithms', now()),
    (v_course_math101_id,'MATH101','Calculus I', now());

-- =================================================================
-- ZONES
-- =================================================================
-- Example coordinates: Phnom Penh, Cambodia
INSERT INTO zones (id, name, latitude, longitude, radius_meters, created_by, created_at)
VALUES
    (v_zone_main_id, 'Main Campus Hall',     11.556448, 104.928203, 100, v_admin_id, now()),
    (v_zone_lab_id,  'Computer Lab Building', 11.557200, 104.929000, 60,  v_admin_id, now());

-- =================================================================
-- GROUPS
-- =================================================================
INSERT INTO groups (id, course_id, name, instructor_id, capacity, semester, created_at)
VALUES
    (v_group_cs101_a_id, v_course_cs101_id,  'CS101 - Section A', v_instr1_id, 40, '2025-S2', now()),
    (v_group_cs201_a_id, v_course_cs201_id,  'CS201 - Section A', v_instr1_id, 35, '2025-S2', now()),
    (v_group_math_a_id,  v_course_math101_id,'MATH101 - Section A', v_instr2_id, 50, '2025-S2', now());

-- =================================================================
-- GROUP MEMBERS
-- =================================================================
INSERT INTO group_members (id, group_id, app_user_id, joined_at)
VALUES
    (gen_random_uuid(), v_group_cs101_a_id, v_student1_id, now()),
    (gen_random_uuid(), v_group_cs101_a_id, v_student2_id, now()),
    (gen_random_uuid(), v_group_cs101_a_id, v_student3_id, now()),
    (gen_random_uuid(), v_group_cs101_a_id, v_student4_id, now()),
    (gen_random_uuid(), v_group_cs201_a_id, v_student1_id, now()),
    (gen_random_uuid(), v_group_cs201_a_id, v_student2_id, now()),
    (gen_random_uuid(), v_group_math_a_id,  v_student3_id, now()),
    (gen_random_uuid(), v_group_math_a_id,  v_student5_id, now());

-- =================================================================
-- DEVICE FINGERPRINTS (only for students with device_bound = true)
-- =================================================================
INSERT INTO device_fingerprints (id, app_user_id, fingerprint_hash, device_info, created_at)
VALUES
    (gen_random_uuid(), v_student1_id, 'fp_demo_endy_iphone15',  'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0) Mobile Safari', now()),
    (gen_random_uuid(), v_student2_id, 'fp_demo_lina_pixel8',    'Mozilla/5.0 (Linux; Android 14) Chrome Mobile',          now());

-- =================================================================
-- SESSIONS
-- =================================================================
-- 1. Currently ACTIVE session (started 30 min ago, ends in 30 min)
INSERT INTO group_sessions (id, group_id, zone_id, start_time, end_time, created_by, created_at)
VALUES (v_session_active_id, v_group_cs101_a_id, v_zone_main_id,
        now() - interval '30 minutes', now() + interval '30 minutes',
        v_instr1_id, now() - interval '30 minutes');

-- 2. Past session (last week) — fully attended, used for history/reports
INSERT INTO group_sessions (id, group_id, zone_id, start_time, end_time, created_by, created_at)
VALUES (v_session_past1_id, v_group_cs101_a_id, v_zone_main_id,
        now() - interval '7 days' - interval '2 hours', now() - interval '7 days',
        v_instr1_id, now() - interval '7 days' - interval '2 hours');

-- 3. Past session for CS201
INSERT INTO group_sessions (id, group_id, zone_id, start_time, end_time, created_by, created_at)
VALUES (v_session_past2_id, v_group_cs201_a_id, v_zone_lab_id,
        now() - interval '3 days' - interval '90 minutes', now() - interval '3 days',
        v_instr1_id, now() - interval '3 days' - interval '90 minutes');

-- 4. Upcoming session (tomorrow) — not active yet, no attendance
INSERT INTO group_sessions (id, group_id, zone_id, start_time, end_time, created_by, created_at)
VALUES (v_session_upcoming_id, v_group_math_a_id, v_zone_main_id,
        now() + interval '1 day', now() + interval '1 day' + interval '2 hours',
        v_instr2_id, now());

-- =================================================================
-- ATTENDANCE RECORDS
-- =================================================================

-- Active session: 2 students already checked in (PRESENT + LATE), 2 still pending (no row = absent in UI)
INSERT INTO attendance_records (id, session_id, student_id, checked_in_at, latitude, longitude, distance_meters, status)
VALUES
    (gen_random_uuid(), v_session_active_id, v_student1_id, now() - interval '20 minutes', 11.556450, 104.928210, 4.2,  'PRESENT'),
    (gen_random_uuid(), v_session_active_id, v_student2_id, now() - interval '5 minutes',  11.556500, 104.928300, 38.7, 'LATE');
-- student3 and student4 are in this group but have NOT checked in -> they'll show in the "absent" endpoint

-- Past session 1 (CS101): everyone accounted for
INSERT INTO attendance_records (id, session_id, student_id, checked_in_at, latitude, longitude, distance_meters, status)
VALUES
    (gen_random_uuid(), v_session_past1_id, v_student1_id, (now() - interval '7 days' - interval '2 hours') + interval '5 minutes',  11.556449, 104.928205, 2.1,  'PRESENT'),
    (gen_random_uuid(), v_session_past1_id, v_student2_id, (now() - interval '7 days' - interval '2 hours') + interval '8 minutes',  11.556470, 104.928250, 15.3, 'PRESENT'),
    (gen_random_uuid(), v_session_past1_id, v_student3_id, (now() - interval '7 days' - interval '2 hours') + interval '25 minutes', 11.556600, 104.928400, 85.0, 'LATE');
-- student4 absent entirely for this one (no row)

-- Past session 2 (CS201)
INSERT INTO attendance_records (id, session_id, student_id, checked_in_at, latitude, longitude, distance_meters, status)
VALUES
    (gen_random_uuid(), v_session_past2_id, v_student1_id, (now() - interval '3 days' - interval '90 minutes') + interval '3 minutes', 11.557210, 104.929010, 11.4, 'PRESENT'),
    (gen_random_uuid(), v_session_past2_id, v_student2_id, (now() - interval '3 days' - interval '90 minutes') + interval '4 minutes', 11.557220, 104.929020, 13.0, 'PRESENT');

RAISE NOTICE 'Seed data inserted successfully.';
    RAISE NOTICE 'Login any account with password: Password123!';
    RAISE NOTICE 'Admin: admin@attendee.edu | Instructor: sarah.chen@attendee.edu | Student: endy.student@attendee.edu';

END $$;