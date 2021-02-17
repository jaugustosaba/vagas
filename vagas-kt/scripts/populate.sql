INSERT INTO "level" (id, name) VALUES (1, 'estagiário');
INSERT INTO "level" (id, name) VALUES (2, 'júnior');
INSERT INTO "level" (id, name) VALUES (3, 'pleno');
INSERT INTO "level" (id, name) VALUES (4, 'sênior');
INSERT INTO "level" (id, name) VALUES (5, 'especialista');

INSERT INTO location (id, name) VALUES (1, 'A');
INSERT INTO location (id, name) VALUES (2, 'B');
INSERT INTO location (id, name) VALUES (3, 'C');
INSERT INTO location (id, name) VALUES (4, 'D');
INSERT INTO location (id, name) VALUES (5, 'E');
INSERT INTO location (id, name) VALUES (6, 'F');

INSERT INTO distance (from_location_id, to_location_id, "value") VALUES (1, 2, 5);
INSERT INTO distance (from_location_id, to_location_id, "value") VALUES (2, 3, 7);
INSERT INTO distance (from_location_id, to_location_id, "value") VALUES (2, 4, 3);
INSERT INTO distance (from_location_id, to_location_id, "value") VALUES (3, 5, 4);
INSERT INTO distance (from_location_id, to_location_id, "value") VALUES (4, 5, 10);
INSERT INTO distance (from_location_id, to_location_id, "value") VALUES (4, 6, 8);

