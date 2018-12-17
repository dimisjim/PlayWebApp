# --- First database schema

# --- !Ups

CREATE TABLE title_basics (
  tconst               varchar(255),
  titleType            varchar(255),
  primaryTitle         varchar(500),
  originalTitle        varchar(500),
  isAdult              varchar(255),
  startYear            varchar(255),
  endYear              varchar(255),
  runtimeMinutes       varchar(255),
  genres               varchar(255),
  constraint pk_title primary key (tconst))
;

create table title_ratings (
  tconst               varchar(255),
  averagerating        varchar(255),
  numVotes             varchar(255),
  constraint pk_ratings primary key (tconst))
;

create table title_principals (
  tconst               varchar(255),
  ordering             varchar(255),
  nconst               varchar(255),
  category             varchar(255),
  job                  varchar(255),
  characters           varchar(255),
  constraint pk_principals primary key (tconst))
;


# --- !Downs

DROP TABLE if EXISTS title_basics;
DROP TABLE if EXISTS title_ratings;
DROP TABLE if EXISTS title_pricipals;
