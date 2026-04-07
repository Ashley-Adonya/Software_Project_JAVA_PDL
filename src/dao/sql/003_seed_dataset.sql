-- Seed dataset (Oracle)

-- 2 admins + 20 etudiants (dont kolawole et adonya)
MERGE INTO users u
USING (
	SELECT 'bouzbouz' AS login, 'bouzbouz' AS password, 'Bouzbouz Admin' AS full_name, 'ADMIN' AS role, 'ING3' AS promo FROM dual
	UNION ALL
	SELECT 'AJAM', 'AJAM', 'Ajam Admin', 'ADMIN', 'ING3' FROM dual
	UNION ALL
	SELECT 'kolawole', 'kolawole', 'Kolawole Etudiant', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'adonya', 'adonya', 'Adonya Etudiant', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'etu03', 'etu03', 'Etudiant 03', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'etu04', 'etu04', 'Etudiant 04', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'etu05', 'etu05', 'Etudiant 05', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'etu06', 'etu06', 'Etudiant 06', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'etu07', 'etu07', 'Etudiant 07', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'etu08', 'etu08', 'Etudiant 08', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'etu09', 'etu09', 'Etudiant 09', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'etu10', 'etu10', 'Etudiant 10', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'etu11', 'etu11', 'Etudiant 11', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'etu12', 'etu12', 'Etudiant 12', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'etu13', 'etu13', 'Etudiant 13', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'etu14', 'etu14', 'Etudiant 14', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'etu15', 'etu15', 'Etudiant 15', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'etu16', 'etu16', 'Etudiant 16', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'etu17', 'etu17', 'Etudiant 17', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'etu18', 'etu18', 'Etudiant 18', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'etu19', 'etu19', 'Etudiant 19', 'STUDENT', 'ING3' FROM dual
	UNION ALL
	SELECT 'etu20', 'etu20', 'Etudiant 20', 'STUDENT', 'ING3' FROM dual
) s
ON (u.login = s.login)
WHEN NOT MATCHED THEN
	INSERT (login, password, full_name, role, promo)
	VALUES (s.login, s.password, s.full_name, s.role, s.promo);

-- 18 dominantes
MERGE INTO dominantes d
USING (
	SELECT 'DLTQ' AS code, 'Developpement logiciel Test et Qualite' AS name, 'Resp DLTQ' AS responsible_name, 'Dominante numerique' AS description, '#ef4444' AS color FROM dual
	UNION ALL
	SELECT 'IABD', 'Intelligence Artificielle et Big Data', 'Resp IABD', 'Dominante numerique', '#10b981' FROM dual
	UNION ALL
	SELECT 'CYRI', 'Cybersecurite des reseaux et de l IOT', 'Resp CYRI', 'Dominante numerique', '#f59e0b' FROM dual
	UNION ALL
	SELECT 'BDTN', 'Big Data pour la transformation numerique', 'Resp BDTN', 'Dominante numerique', '#3b82f6' FROM dual
	UNION ALL
	SELECT 'IAIT', 'Ingenieur d affaires informatique et telecoms', 'Resp IAIT', 'Dominante management technique', '#8b5cf6' FROM dual
	UNION ALL
	SELECT 'ISN', 'Ingenierie des services du numerique', 'Resp ISN', 'Dominante numerique', '#ec4899' FROM dual
	UNION ALL
	SELECT 'IFIN', 'Ingenieur finance', 'Resp IFIN', 'Dominante finance', '#14b8a6' FROM dual
	UNION ALL
	SELECT 'DARI', 'Digitalisation automatisation robotique et IA', 'Resp DARI', 'Dominante industrie du futur', '#f43f5e' FROM dual
	UNION ALL
	SELECT 'EDD', 'Energie et developpement durable', 'Resp EDD', 'Dominante energie', '#84cc16' FROM dual
	UNION ALL
	SELECT 'GET', 'Genie electrique et transport', 'Resp GET', 'Dominante energie et transport', '#06b6d4' FROM dual
	UNION ALL
	SELECT 'IADES', 'Ingenieur d affaires distribution energie et signaux', 'Resp IADES', 'Dominante management technique', '#6366f1' FROM dual
	UNION ALL
	SELECT 'MSE', 'Mecatronique et systemes embarques', 'Resp MSE', 'Dominante systemes embarques', '#d946ef' FROM dual
	UNION ALL
	SELECT 'ISEMAC', 'Ingenierie systemes embarques mobiles autonomes connectes', 'Resp ISEMAC', 'Dominante systemes embarques', '#f97316' FROM dual
	UNION ALL
	SELECT 'ESAA', 'Electronique systemes automobile et aeronautique', 'Resp ESAA', 'Dominante electronique', '#0ea5e9' FROM dual
	UNION ALL
	SELECT 'RSI', 'Reseaux et systemes d information', 'Resp RSI', 'Dominante numerique', '#10b981' FROM dual
	UNION ALL
	SELECT 'IOTA', 'Internet des objets et architectures', 'Resp IOTA', 'Dominante numerique', '#8b5cf6' FROM dual
	UNION ALL
	SELECT 'TELC', 'Telecommunications et cloud', 'Resp TELC', 'Dominante telecom', '#3b82f6' FROM dual
	UNION ALL
	SELECT 'SAF', 'Surete de fonctionnement', 'Resp SAF', 'Dominante surete et fiabilite', '#ef4444' FROM dual
) s
ON (d.code = s.code)
WHEN NOT MATCHED THEN
	INSERT (code, name, responsible_name, description, color)
	VALUES (s.code, s.name, s.responsible_name, s.description, s.color);

COMMIT;

-- Campaign
MERGE INTO campaigns c
USING (
    SELECT 'Campagne ING3 2026' AS name, 'ING3' AS promo, TO_DATE('2026-05-01', 'YYYY-MM-DD') AS r
egistration_day, TO_DATE('2026-04-01', 'YYYY-MM-DD') AS start_date, TO_DATE('2026-04-30', 'YYYY-M
M-DD') AS end_date, 3 AS max_choices, 'OPEN' AS status, 1 AS created_by FROM dual
) s
ON (c.name = s.name)
WHEN NOT MATCHED THEN
    INSERT (name, promo, registration_day, start_date, end_date, max_choices, status, created_by)
    VALUES (s.name, s.promo, s.registration_day, s.start_date, s.end_date, s.max_choices, s.statu
s, s.created_by);

COMMIT;
