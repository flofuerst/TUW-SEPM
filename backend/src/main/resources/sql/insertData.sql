-- insert initial test data
-- the IDs are hardcoded to enable references between further test data
-- negative IDs are used to not interfere with user-entered data and allow clean deletion of test data

DELETE
FROM owner
WHERE id < 0;

INSERT INTO owner (id, first_name, last_name, email)
VALUES (-1, 'Hansi', 'Hinterseer', 'hansi@hinterseer.at')
;

INSERT INTO owner (id, first_name, last_name, email)
VALUES (-2, 'Stefan', 'Müller', '')
;

INSERT INTO owner (id, first_name, last_name, email)
VALUES (-3, 'Tom', 'Kegler', 'tom.kegler@gmail,com')
;

DELETE
FROM horse
WHERE id < 0;

INSERT INTO horse (id, name, description, date_of_birth, sex)
VALUES (-1, 'Wendy', 'The famous one!', '2012-12-12', 'FEMALE')
;

INSERT INTO horse (id, name, description, date_of_birth, sex, mother_id, owner_id)
VALUES (-2, 'Bobo', 'The cool sock', '2016-02-01', 'MALE', -1, -2)
;

INSERT INTO horse (id, name, description, date_of_birth, sex, mother_id)
VALUES (-3, 'Stern', 'Fast as hell', '2013-12-12', 'FEMALE', -1)
;

INSERT INTO horse (id, name, description, date_of_birth, sex, mother_id, father_id, owner_id)
VALUES (-4, 'Tobisch', 'Strange name and strange behaviour', '2017-02-01', 'MALE', -3, -2, -3)
;

INSERT INTO horse (id, name, description, date_of_birth, sex, father_id, owner_id)
VALUES (-5, 'Sissi', 'Very lazy', '2023-02-01', 'FEMALE', -4, -1)
;

