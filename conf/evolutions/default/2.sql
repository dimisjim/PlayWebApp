# --- Sample dataset

# --- !Ups

COPY title_basics FROM '/home/jim/dimisjim/PlayWebApp/datasets/title.basics.tsv' DELIMITER E'\t';

COPY title_principals FROM '/home/jim/dimisjim/PlayWebApp/datasets/title.principals.tsv' DELIMITER E'\t';

COPY title_ratings FROM '/home/jim/dimisjim/PlayWebApp/datasets/title.ratings.tsv' DELIMITER E'\t';

# --- !Downs

DELETE FROM title_basics;

DELETE FROM title_principals;

DELETE FROM title_ratings;