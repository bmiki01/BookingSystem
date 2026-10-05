CREATE TABLE resources (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    capacity INT NOT NULL DEFAULT 1,
    type VARCHAR(100) NOT NULL DEFAULT 'ROOM',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    slot_granularity_minutes INT NOT NULL DEFAULT 30,
    min_booking_duration_minutes INT NOT NULL DEFAULT 30,
    max_booking_duration_minutes INT NOT NULL DEFAULT 240,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE resource_opening_hours (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL REFERENCES resources(id) ON DELETE CASCADE,
    day_of_week VARCHAR(20) NOT NULL,
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    is_closed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_resource_day UNIQUE (resource_id, day_of_week)
);

CREATE INDEX idx_resources_active ON resources(active);
CREATE INDEX idx_opening_hours_resource ON resource_opening_hours(resource_id);
