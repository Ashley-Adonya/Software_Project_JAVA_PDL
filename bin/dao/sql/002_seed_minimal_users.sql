-- Seed minimal users (Oracle)
MERGE INTO users u
USING (
	SELECT 'bouzbouz' AS login, 'bouzbouz' AS password, 'Bouzbouz Admin' AS full_name, 'ADMIN' AS role, 'ING3' AS promo FROM dual
	UNION ALL
	SELECT 'AJAM', 'AJAM', 'Ajam Admin', 'ADMIN', 'ING3' FROM dual
	UNION ALL
	SELECT 'kolawole', 'kolawole', 'Kolawole Etudiant', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'adonya', 'adonya', 'Adonya Etudiant', 'STUDENT', 'ING3' FROM dual
) s
ON (u.login = s.login)
WHEN NOT MATCHED THEN
	INSERT (login, password, full_name, role, promo)
	VALUES (s.login, s.password, s.full_name, s.role, s.promo);

COMMIT;
