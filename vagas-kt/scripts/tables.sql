CREATE TABLE location (
    id      SERIAL PRIMARY KEY,
    name    VARCHAR(255),
    UNIQUE (name)
);

CREATE TABLE distance (
    from_location_id    INTEGER NOT NULL,
    to_location_id      INTEGER NOT NULL,
    value               INTEGER,
    PRIMARY KEY (from_location_id, to_location_id)
);

CREATE TABLE "level" (
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(255),
    UNIQUE (name)
);

CREATE TABLE applicant (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    profession  VARCHAR(255) NOT NULL,
    location_id INTEGER NOT NULL,
    level_id    INTEGER NOT NULL
);

CREATE TABLE job (
    id              SERIAL PRIMARY KEY,
    company         VARCHAR(255) NOT NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT NOT NULL,
    location_id     INTEGER NOT NULL,
    level_id        INTEGER NOT NULL
);

CREATE TABLE application (
    applicant_id    INTEGER NOT NULL,
    job_id          INTEGER NOT NULL,
    PRIMARY KEY (applicant_id, job_id)
);

ALTER TABLE applicant
ADD FOREIGN KEY (level_id) REFERENCES "level"(id);

ALTER TABLE distance
ADD FOREIGN KEY (from_location_id) REFERENCES location(id);

ALTER TABLE distance
ADD FOREIGN KEY (to_location_id) REFERENCES location(id);

ALTER TABLE job
ADD FOREIGN KEY (level_id) REFERENCES "level"(id);

ALTER TABLE application
ADD FOREIGN KEY (applicant_id) REFERENCES applicant(id);

ALTER TABLE application
ADD FOREIGN KEY (job_id) REFERENCES job(id);