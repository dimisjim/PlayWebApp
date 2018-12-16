# --- Sample dataset

# --- !Ups

COPY title_basics FROM '/home/jim/dimisjim/PlayWebApp/datasets/title.basics.tsv' DELIMITER E'\t';

--COPY title_crew FROM '/home/jim/dimisjim/datasets/title.crew.tsv' DELIMITER '\t';

# --- !Downs

DELETE FROM title_basics;

--DELETE FROM title_crew;