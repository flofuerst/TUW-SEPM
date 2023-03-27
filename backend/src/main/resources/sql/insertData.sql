-- insert initial test data
-- the IDs are hardcoded to enable references between further test data
-- negative IDs are used to not interfere with user-entered data and allow clean deletion of test data

DELETE
FROM owner
WHERE id < 0;

INSERT INTO owner (id, first_name, last_name, email)
VALUES (-1, 'Hansi', 'Hinterseer', 'hansi@hinterseer.at'),
       (-2, 'Stefan', 'Müller', ''),
       (-3, 'Tom', 'Kegler', 'tom.kegler@gmail,com'),
       (-4, 'Walter', 'White', 'walter.white@gmail.com'),
       (-5, 'Jesse', 'Pinkman', 'jesse.pinkman@gmx.de'),
       (-6, 'Michael', 'Scott', 'michael.scott@outlook.com'),
       (-7, 'Dwight', 'Schrute', 'dwight.schrute@gmail.at'),
       (-8, 'Malcolm', 'Wilkerson', 'malcolm.wilkerson@outlook.com'),
       (-9, 'Hal', 'Wilkerson', 'hal.wilkerson@gmx.de'),
       (-10, 'Jojo', 'Joestar', 'jojo.joestar@gmail.at'),
       (-11, 'Jotaro', 'Kujo', 'jotaro.kujo@gmx.de'),
       (-12, 'Giorno', 'Giovanna', 'giorno.giovanna@outlook.com'),
       (-13, 'Bruno', 'Bucciarati', 'bruno.bucciarati@gmail.com');

DELETE
FROM horse
WHERE id < 0;

INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id, mother_id, father_id)
VALUES (-1, 'Wendy', 'The famous one!', '1998-12-12', 'FEMALE', NULL, NULL, NULL),
       (-2, 'Bobo', 'The cool sock', '2016-02-01', 'MALE', -1, -1, NULL),
       (-3, 'Stern', 'Fast as hell', '2013-12-12', 'FEMALE', -1, -1, NULL),
       (-4, 'Flitzer', 'Strange name and strange behaviour', '2017-05-25', 'MALE', -3, -3, -2),
       (-5, 'Sissi', 'Very lazy', '2020-02-01', 'FEMALE', -4, -1, NULL),
       (-6, 'Scooby', 'A loyal and playful horse', '2018-07-15', 'MALE', -2, -3, -4),
       (-7, 'Garfield', 'A lazy but affectionate horse', '2017-02-28', 'MALE', -5, NULL, -2),
       (-8, 'Nemo', 'A curious and adventurous horse', '2014-11-03', 'MALE', -8, NULL, NULL),
       (-9, 'Dory', 'A forgetful but friendly horse', '2015-06-15', 'FEMALE', -6, -1, -8),
       (-10, 'Simba', 'A brave and wise horse', '2013-09-22', 'MALE', -10, NULL, NULL),
       (-11, 'Stitch', 'A mischievous but lovable horse', '2023-01-26', 'MALE', -9, -9, -10),
       (-12, 'Snoopy', 'A cheerful and loyal horse', '2009-12-12', 'MALE', -2, -1, NULL),
       (-13, 'Tom', 'A mischievous and clever horse', '2022-10-31', 'MALE', NULL, -5, -10);