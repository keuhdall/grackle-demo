CREATE SEQUENCE public.country_id_seq AS integer START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE public.countries (
   id integer NOT NULL PRIMARY KEY DEFAULT nextval('public.country_id_seq'::regclass),
   name text NOT NULL,
   continent text NOT NULL,
   best_food text,
   has_eiffel_tower boolean NOT NULL
);