-- =============================================================================
-- Seed / starter data (standalone SQL)
-- =============================================================================
-- Use this file so someone else can get the same starter data.
-- The app normally seeds via Java DemoDataLoader; this file is for:
--   - File-based H2 (e.g. jdbc:h2:file:./data/customerdb) instead of in-memory
--   - Or any DB you point the services to (run the relevant section per service)
--
-- All user passwords in this file are:  password
-- (BCrypt hash below = "password" with strength 10)
-- =============================================================================

-- =============================================================================
-- PART 1 – CUSTOMER SERVICE (tables: users, customer)
-- Run this against the customer-service database (e.g. customerdb).
-- =============================================================================

-- USERS (id 1 = admin, 2–51 = customers; password = 'password')
INSERT INTO users (id, username, email, password, first_name, last_name, role, account_non_expired, account_non_locked, credentials_non_expired, enabled, created_date, created_by) VALUES
(1, 'admin', 'admin@banque.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'Système', 'ADMIN', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(2, 'marie.dupont', 'marie.dupont@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Marie', 'Dupont', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(3, 'jean.martin', 'jean.martin@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jean', 'Martin', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(4, 'sophie.bernard', 'sophie.bernard@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Sophie', 'Bernard', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(5, 'nadia.chakir', 'nadia.chakir@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Nadia', 'Chakir', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(6, 'pierre.dupuis', 'pierre.dupuis@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Pierre', 'Dupuis', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(7, 'amelie.leroy', 'amelie.leroy@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Amélie', 'Leroy', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(8, 'lucas.moreau', 'lucas.moreau@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Lucas', 'Moreau', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(9, 'claire.roux', 'claire.roux@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Claire', 'Roux', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(10, 'thomas.brun', 'thomas.brun@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Thomas', 'Brun', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(11, 'emma.robert', 'emma.robert@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Emma', 'Robert', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(12, 'nicolas.petit', 'nicolas.petit@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Nicolas', 'Petit', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(13, 'julie.mercier', 'julie.mercier@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Julie', 'Mercier', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(14, 'antoine.renard', 'antoine.renard@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Antoine', 'Renard', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(15, 'camille.noel', 'camille.noel@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Camille', 'Noël', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(16, 'hugo.durand', 'hugo.durand@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Hugo', 'Durand', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(17, 'lea.colin', 'lea.colin@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Léa', 'Colin', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(18, 'paul.fournier', 'paul.fournier@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Paul', 'Fournier', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(19, 'ines.garnier', 'ines.garnier@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Inès', 'Garnier', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(20, 'maxime.benoit', 'maxime.benoit@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Maxime', 'Benoît', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(21, 'manon.dupuy', 'manon.dupuy@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Manon', 'Dupuy', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(22, 'alexandre.gerard', 'alexandre.gerard@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alexandre', 'Gérard', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(23, 'chloe.morin', 'chloe.morin@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Chloé', 'Morin', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(24, 'quentin.lucas', 'quentin.lucas@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Quentin', 'Lucas', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(25, 'salome.charles', 'salome.charles@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Salomé', 'Charles', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(26, 'youssef.benali', 'youssef.benali@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Youssef', 'Benali', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(27, 'fatima.elhassan', 'fatima.elhassan@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Fatima', 'El Hassan', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(28, 'rachid.boumediene', 'rachid.boumediene@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Rachid', 'Boumediene', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(29, 'amina.belaid', 'amina.belaid@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Amina', 'Belaid', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(30, 'karim.lamrani', 'karim.lamrani@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Karim', 'Lamrani', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(31, 'sara.ait', 'sara.ait@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Sara', 'Aït', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(32, 'mehdi.bouras', 'mehdi.bouras@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Mehdi', 'Bouras', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(33, 'julien.perrin', 'julien.perrin@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Julien', 'Perrin', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(34, 'anais.guillot', 'anais.guillot@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Anaïs', 'Guillot', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(35, 'renaud.dupont', 'renaud.dupont@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Renaud', 'Dupont', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(36, 'celine.martinez', 'celine.martinez@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Céline', 'Martinez', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(37, 'gael.roche', 'gael.roche@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Gaël', 'Roche', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(38, 'laura.pires', 'laura.pires@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Laura', 'Pires', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(39, 'bruno.schmitt', 'bruno.schmitt@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bruno', 'Schmitt', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(40, 'isabelle.morel', 'isabelle.morel@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Isabelle', 'Morel', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(41, 'kevin.marchand', 'kevin.marchand@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Kévin', 'Marchand', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(42, 'amel.benammar', 'amel.benammar@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Amel', 'Ben Ammar', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(43, 'samir.ouali', 'samir.ouali@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Samir', 'Ouali', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(44, 'nadine.valois', 'nadine.valois@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Nadine', 'Valois', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(45, 'tarek.bellamine', 'tarek.bellamine@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Tarek', 'Bellamine', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(46, 'aicha.benali', 'aicha.benali@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Aïcha', 'Benali', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(47, 'zakaria.haddad', 'zakaria.haddad@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Zakaria', 'Haddad', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(48, 'ines.belkacem', 'ines.belkacem@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Inès', 'Belkacem', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(49, 'nora.kerrouche', 'nora.kerrouche@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Nora', 'Kerrouche', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(50, 'yassin.mansour', 'yassin.mansour@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Yassin', 'Mansour', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system'),
(51, 'salma.boukhalfa', 'salma.boukhalfa@email.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Salma', 'Boukhalfa', 'CUSTOMER', true, true, true, true, CURRENT_TIMESTAMP, 'system');

-- CUSTOMERS (one per customer user; user_id 2..51)
INSERT INTO customer (id, name, email, phone, address, created_date, created_by, user_id) VALUES
(1, 'Marie Dupont', 'marie.dupont@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 2),
(2, 'Jean Martin', 'jean.martin@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 3),
(3, 'Sophie Bernard', 'sophie.bernard@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 4),
(4, 'Nadia Chakir', 'nadia.chakir@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 5),
(5, 'Pierre Dupuis', 'pierre.dupuis@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 6),
(6, 'Amélie Leroy', 'amelie.leroy@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 7),
(7, 'Lucas Moreau', 'lucas.moreau@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 8),
(8, 'Claire Roux', 'claire.roux@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 9),
(9, 'Thomas Brun', 'thomas.brun@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 10),
(10, 'Emma Robert', 'emma.robert@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 11),
(11, 'Nicolas Petit', 'nicolas.petit@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 12),
(12, 'Julie Mercier', 'julie.mercier@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 13),
(13, 'Antoine Renard', 'antoine.renard@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 14),
(14, 'Camille Noël', 'camille.noel@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 15),
(15, 'Hugo Durand', 'hugo.durand@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 16),
(16, 'Léa Colin', 'lea.colin@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 17),
(17, 'Paul Fournier', 'paul.fournier@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 18),
(18, 'Inès Garnier', 'ines.garnier@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 19),
(19, 'Maxime Benoît', 'maxime.benoit@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 20),
(20, 'Manon Dupuy', 'manon.dupuy@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 21),
(21, 'Alexandre Gérard', 'alexandre.gerard@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 22),
(22, 'Chloé Morin', 'chloe.morin@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 23),
(23, 'Quentin Lucas', 'quentin.lucas@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 24),
(24, 'Salomé Charles', 'salome.charles@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 25),
(25, 'Youssef Benali', 'youssef.benali@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 26),
(26, 'Fatima El Hassan', 'fatima.elhassan@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 27),
(27, 'Rachid Boumediene', 'rachid.boumediene@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 28),
(28, 'Amina Belaid', 'amina.belaid@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 29),
(29, 'Karim Lamrani', 'karim.lamrani@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 30),
(30, 'Sara Aït', 'sara.ait@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 31),
(31, 'Mehdi Bouras', 'mehdi.bouras@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 32),
(32, 'Julien Perrin', 'julien.perrin@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 33),
(33, 'Anaïs Guillot', 'anais.guillot@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 34),
(34, 'Renaud Dupont', 'renaud.dupont@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 35),
(35, 'Céline Martinez', 'celine.martinez@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 36),
(36, 'Gaël Roche', 'gael.roche@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 37),
(37, 'Laura Pires', 'laura.pires@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 38),
(38, 'Bruno Schmitt', 'bruno.schmitt@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 39),
(39, 'Isabelle Morel', 'isabelle.morel@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 40),
(40, 'Kévin Marchand', 'kevin.marchand@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 41),
(41, 'Amel Ben Ammar', 'amel.benammar@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 42),
(42, 'Samir Ouali', 'samir.ouali@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 43),
(43, 'Nadine Valois', 'nadine.valois@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 44),
(44, 'Tarek Bellamine', 'tarek.bellamine@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 45),
(45, 'Aïcha Benali', 'aicha.benali@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 46),
(46, 'Zakaria Haddad', 'zakaria.haddad@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 47),
(47, 'Inès Belkacem', 'ines.belkacem@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 48),
(48, 'Nora Kerrouche', 'nora.kerrouche@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 49),
(49, 'Yassin Mansour', 'yassin.mansour@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 50),
(50, 'Salma Boukhalfa', 'salma.boukhalfa@email.fr', '+33 6 12 34 56 78', '123 Rue Example, Paris', CURRENT_TIMESTAMP, 'system', 51);


-- =============================================================================
-- PART 2 – ACCOUNT SERVICE (table: bank_account, discriminator TYPE: CA=current, SA=saving)
-- Run this against the account-service database (e.g. accountdb).
-- customer_id 2..51 refer to customer-service customer IDs (same as user 2..51).
-- =============================================================================

INSERT INTO bank_account (id, balance, create_date, status, customer_id, created_by, type, over_draft, interest_rate) VALUES
('ACC-CA-001', 12500.00, CURRENT_TIMESTAMP, 'ACTIVATED', 2, 'system', 'CA', 2000.00, NULL),
('ACC-CA-002', 8200.50, CURRENT_TIMESTAMP, 'ACTIVATED', 3, 'system', 'CA', 1500.00, NULL),
('ACC-CA-003', 18500.00, CURRENT_TIMESTAMP, 'ACTIVATED', 4, 'system', 'CA', 3000.00, NULL),
('ACC-CA-004', 6100.75, CURRENT_TIMESTAMP, 'ACTIVATED', 5, 'system', 'CA', 1000.00, NULL),
('ACC-CA-005', 22000.00, CURRENT_TIMESTAMP, 'ACTIVATED', 6, 'system', 'CA', 2500.00, NULL),
('ACC-CA-006', 9500.00, CURRENT_TIMESTAMP, 'ACTIVATED', 7, 'system', 'CA', 1800.00, NULL),
('ACC-CA-007', 14200.00, CURRENT_TIMESTAMP, 'ACTIVATED', 8, 'system', 'CA', 2200.00, NULL),
('ACC-CA-008', 7800.25, CURRENT_TIMESTAMP, 'ACTIVATED', 9, 'system', 'CA', 1200.00, NULL),
('ACC-CA-009', 16800.00, CURRENT_TIMESTAMP, 'ACTIVATED', 10, 'system', 'CA', 2800.00, NULL),
('ACC-CA-010', 11000.00, CURRENT_TIMESTAMP, 'ACTIVATED', 11, 'system', 'CA', 2000.00, NULL),
('ACC-CA-011', 13500.00, CURRENT_TIMESTAMP, 'ACTIVATED', 12, 'system', 'CA', 1900.00, NULL),
('ACC-CA-012', 9200.00, CURRENT_TIMESTAMP, 'ACTIVATED', 13, 'system', 'CA', 1600.00, NULL),
('ACC-CA-013', 19800.00, CURRENT_TIMESTAMP, 'ACTIVATED', 14, 'system', 'CA', 3200.00, NULL),
('ACC-CA-014', 7200.50, CURRENT_TIMESTAMP, 'ACTIVATED', 15, 'system', 'CA', 1400.00, NULL),
('ACC-CA-015', 15600.00, CURRENT_TIMESTAMP, 'ACTIVATED', 16, 'system', 'CA', 2600.00, NULL),
('ACC-CA-016', 10500.00, CURRENT_TIMESTAMP, 'ACTIVATED', 17, 'system', 'CA', 2100.00, NULL),
('ACC-CA-017', 12800.00, CURRENT_TIMESTAMP, 'ACTIVATED', 18, 'system', 'CA', 2300.00, NULL),
('ACC-CA-018', 8800.00, CURRENT_TIMESTAMP, 'ACTIVATED', 19, 'system', 'CA', 1700.00, NULL),
('ACC-CA-019', 17200.00, CURRENT_TIMESTAMP, 'ACTIVATED', 20, 'system', 'CA', 2700.00, NULL),
('ACC-CA-020', 6400.25, CURRENT_TIMESTAMP, 'ACTIVATED', 21, 'system', 'CA', 1100.00, NULL),
('ACC-CA-021', 20500.00, CURRENT_TIMESTAMP, 'ACTIVATED', 22, 'system', 'CA', 3500.00, NULL),
('ACC-CA-022', 11800.00, CURRENT_TIMESTAMP, 'ACTIVATED', 23, 'system', 'CA', 2400.00, NULL),
('ACC-CA-023', 9900.00, CURRENT_TIMESTAMP, 'ACTIVATED', 24, 'system', 'CA', 1500.00, NULL),
('ACC-CA-024', 14400.00, CURRENT_TIMESTAMP, 'ACTIVATED', 25, 'system', 'CA', 2200.00, NULL),
('ACC-CA-025', 7600.00, CURRENT_TIMESTAMP, 'ACTIVATED', 26, 'system', 'CA', 1300.00, NULL),
('SA_001', 15000.00, CURRENT_TIMESTAMP, 'ACTIVATED', 2, 'system', 'SA', NULL, 1.0),
('SA_002', 22000.00, CURRENT_TIMESTAMP, 'ACTIVATED', 3, 'system', 'SA', NULL, 2.0),
('SA_003', 18500.00, CURRENT_TIMESTAMP, 'ACTIVATED', 4, 'system', 'SA', NULL, 3.0),
('SA_004', 25000.00, CURRENT_TIMESTAMP, 'ACTIVATED', 5, 'system', 'SA', NULL, 4.0),
('SA_005', 12000.00, CURRENT_TIMESTAMP, 'ACTIVATED', 6, 'system', 'SA', NULL, 5.0),
('SA_006', 19500.00, CURRENT_TIMESTAMP, 'ACTIVATED', 7, 'system', 'SA', NULL, 1.0),
('SA_007', 16800.00, CURRENT_TIMESTAMP, 'ACTIVATED', 8, 'system', 'SA', NULL, 2.0),
('SA_008', 23000.00, CURRENT_TIMESTAMP, 'ACTIVATED', 9, 'system', 'SA', NULL, 3.0),
('SA_009', 14200.00, CURRENT_TIMESTAMP, 'ACTIVATED', 10, 'system', 'SA', NULL, 4.0),
('SA_010', 27800.00, CURRENT_TIMESTAMP, 'ACTIVATED', 11, 'system', 'SA', NULL, 5.0),
('SA_011', 11000.00, CURRENT_TIMESTAMP, 'ACTIVATED', 12, 'system', 'SA', NULL, 1.0),
('SA_012', 20500.00, CURRENT_TIMESTAMP, 'ACTIVATED', 13, 'system', 'SA', NULL, 2.0),
('SA_013', 19200.00, CURRENT_TIMESTAMP, 'ACTIVATED', 14, 'system', 'SA', NULL, 3.0),
('SA_014', 15800.00, CURRENT_TIMESTAMP, 'ACTIVATED', 15, 'system', 'SA', NULL, 4.0),
('SA_015', 24500.00, CURRENT_TIMESTAMP, 'ACTIVATED', 16, 'system', 'SA', NULL, 5.0),
('SA_016', 13200.00, CURRENT_TIMESTAMP, 'ACTIVATED', 17, 'system', 'SA', NULL, 1.0),
('SA_017', 21000.00, CURRENT_TIMESTAMP, 'ACTIVATED', 18, 'system', 'SA', NULL, 2.0),
('SA_018', 17600.00, CURRENT_TIMESTAMP, 'ACTIVATED', 19, 'system', 'SA', NULL, 3.0),
('SA_019', 26800.00, CURRENT_TIMESTAMP, 'ACTIVATED', 20, 'system', 'SA', NULL, 4.0),
('SA_020', 14500.00, CURRENT_TIMESTAMP, 'ACTIVATED', 21, 'system', 'SA', NULL, 5.0),
('SA_021', 18800.00, CURRENT_TIMESTAMP, 'ACTIVATED', 22, 'system', 'SA', NULL, 1.0),
('SA_022', 22400.00, CURRENT_TIMESTAMP, 'ACTIVATED', 23, 'system', 'SA', NULL, 2.0),
('SA_023', 16200.00, CURRENT_TIMESTAMP, 'ACTIVATED', 24, 'system', 'SA', NULL, 3.0),
('SA_024', 25800.00, CURRENT_TIMESTAMP, 'ACTIVATED', 25, 'system', 'SA', NULL, 4.0),
('SA_025', 13900.00, CURRENT_TIMESTAMP, 'ACTIVATED', 26, 'system', 'SA', NULL, 5.0);


-- =============================================================================
-- PART 3 – TRANSACTION SERVICE (table: account_operations)
-- Run this against the transaction-service database (e.g. transactiondb).
-- Account IDs must exist in account-service (ACC-CA-001, SA_001, etc.).
-- =============================================================================

INSERT INTO account_operations (operation_date, amount, description, type, bank_account_id, performed_by) VALUES
(DATEADD('DAY', -5, CURRENT_TIMESTAMP), 2100.00, 'Salaire - Société Tech Paris', 'CREDIT', 'ACC-CA-001', 'system-demo'),
(DATEADD('DAY', -4, CURRENT_TIMESTAMP), 85.40, 'Courses Carrefour Lyon', 'DEBIT', 'ACC-CA-001', 'system-demo'),
(DATEADD('DAY', -3, CURRENT_TIMESTAMP), 49.99, 'Abonnement Netflix', 'DEBIT', 'ACC-CA-001', 'system-demo'),
(DATEADD('DAY', -8, CURRENT_TIMESTAMP), 1950.50, 'Virement salaire - Banque Nationale', 'CREDIT', 'ACC-CA-002', 'system-demo'),
(DATEADD('DAY', -6, CURRENT_TIMESTAMP), 120.30, 'Facture électricité EDF', 'DEBIT', 'ACC-CA-002', 'system-demo'),
(DATEADD('DAY', -7, CURRENT_TIMESTAMP), 320.00, 'Remboursement frais professionnels', 'CREDIT', 'ACC-CA-003', 'system-demo'),
(DATEADD('DAY', -2, CURRENT_TIMESTAMP), 45.90, 'Abonnement SNCF', 'DEBIT', 'ACC-CA-003', 'system-demo'),
(DATEADD('DAY', -9, CURRENT_TIMESTAMP), 29.99, 'Abonnement Spotify', 'DEBIT', 'ACC-CA-004', 'system-demo'),
(DATEADD('DAY', -1, CURRENT_TIMESTAMP), 64.50, 'Restaurant Marseille', 'DEBIT', 'ACC-CA-004', 'system-demo'),
(DATEADD('DAY', -10, CURRENT_TIMESTAMP), 500.00, 'Virement épargne mensuel', 'CREDIT', 'SA_001', 'system-demo'),
(DATEADD('DAY', -20, CURRENT_TIMESTAMP), 150.00, 'Prime exceptionnelle', 'CREDIT', 'SA_001', 'system-demo'),
(DATEADD('DAY', -15, CURRENT_TIMESTAMP), 300.00, 'Épargne automatique', 'CREDIT', 'SA_002', 'system-demo'),
(DATEADD('DAY', -30, CURRENT_TIMESTAMP), 250.00, 'Intérêts trimestriels', 'CREDIT', 'SA_003', 'system-demo');


-- =============================================================================
-- LOGINS TO SHARE
--   Admin:     admin / password
--   Customer:  marie.dupont / password  (or any username from the list above)
-- =============================================================================
