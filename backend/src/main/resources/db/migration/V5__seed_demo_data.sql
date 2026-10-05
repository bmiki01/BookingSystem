-- Seed Demo Users (Passwords hashed with BCrypt strength 10)
-- Admin: admin@example.com / AdminPassword123!
-- User: user@example.com / UserPassword123!
INSERT INTO users (email, password_hash, full_name, role, created_at, updated_at)
VALUES 
(
    'admin@example.com',
    '$2a$10$If32IPEvy1uq7dZ6a3pBIuPRDWoQQEIAMBbP9XvrePEzfi/VU/k9O',
    'System Admin',
    'ADMIN',
    NOW(),
    NOW()
),
(
    'user@example.com',
    '$2a$10$lYprUgvEOgjeJbjT0eLKDess1.M70yo/R0Bafc2ZQ4JlLvrQni9UK',
    'Demo User',
    'USER',
    NOW(),
    NOW()
)
ON CONFLICT (email) DO NOTHING;

-- Seed Sample Resources
INSERT INTO resources (id, name, description, capacity, type, active, slot_granularity_minutes, min_booking_duration_minutes, max_booking_duration_minutes, version, created_at, updated_at)
VALUES 
(
    1,
    'Executive Boardroom',
    'High-end meeting room with 4K display, video conferencing, and seating for 12.',
    12,
    'MEETING_ROOM',
    true,
    30,
    30,
    240,
    0,
    NOW(),
    NOW()
),
(
    2,
    'Podcast & Recording Studio',
    'Acoustically treated studio equipped with Shure SM7B mics and Rodecaster Pro console.',
    4,
    'EQUIPMENT',
    true,
    30,
    60,
    180,
    0,
    NOW(),
    NOW()
),
(
    3,
    'Tech Consultation Desk A',
    'One-on-one architecture review desk for senior developer consulting.',
    2,
    'CONSULTATION',
    true,
    15,
    15,
    60,
    0,
    NOW(),
    NOW()
)
ON CONFLICT (id) DO NOTHING;

SELECT setval('resources_id_seq', (SELECT MAX(id) FROM resources));

-- Seed Default Opening Hours for Resource 1, 2, 3 (Monday - Friday 08:00 - 18:00)
INSERT INTO resource_opening_hours (resource_id, day_of_week, open_time, close_time, is_closed)
VALUES
-- Resource 1
(1, 'MONDAY', '08:00:00', '18:00:00', false),
(1, 'TUESDAY', '08:00:00', '18:00:00', false),
(1, 'WEDNESDAY', '08:00:00', '18:00:00', false),
(1, 'THURSDAY', '08:00:00', '18:00:00', false),
(1, 'FRIDAY', '08:00:00', '18:00:00', false),
(1, 'SATURDAY', '08:00:00', '18:00:00', true),
(1, 'SUNDAY', '08:00:00', '18:00:00', true),

-- Resource 2
(2, 'MONDAY', '09:00:00', '17:00:00', false),
(2, 'TUESDAY', '09:00:00', '17:00:00', false),
(2, 'WEDNESDAY', '09:00:00', '17:00:00', false),
(2, 'THURSDAY', '09:00:00', '17:00:00', false),
(2, 'FRIDAY', '09:00:00', '17:00:00', false),
(2, 'SATURDAY', '10:00:00', '14:00:00', false),
(2, 'SUNDAY', '00:00:00', '00:00:00', true),

-- Resource 3
(3, 'MONDAY', '08:00:00', '16:00:00', false),
(3, 'TUESDAY', '08:00:00', '16:00:00', false),
(3, 'WEDNESDAY', '08:00:00', '16:00:00', false),
(3, 'THURSDAY', '08:00:00', '16:00:00', false),
(3, 'FRIDAY', '08:00:00', '16:00:00', false),
(3, 'SATURDAY', '00:00:00', '00:00:00', true),
(3, 'SUNDAY', '00:00:00', '00:00:00', true)
ON CONFLICT (resource_id, day_of_week) DO NOTHING;
