INSERT INTO countries (name, continent, best_food, has_eiffel_tower)
VALUES
('France', 'Europe', 'croissants', true),
('England', 'Europe', null, false),
('USA', 'North America', 'cheeseburger', false),
('Argentina', 'South America', 'empenadas', false);

INSERT INTO cities (name, country_id, is_capital)
VALUES
('Paris', 1, true),
('La Rochelle', 1, false),
('Bordeaux', 1, false),
('London', 2, true),
('Washington', 3, true),
('New York', 3, false),
('Seattle', 3, false),
('San Francisco', 3, false),
('Buenos Aires', 4, true);