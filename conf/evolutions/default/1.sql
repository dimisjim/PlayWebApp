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
  genres               varchar(255))
;

--CREATE TABLE title_crew (
--  tconst               varchar(255),
--  titleType            varchar(255),
--  primaryTitle         varchar(255),
--  originalTitle        varchar(255),
--  isAdult              boolean,
--  startYear            integer,
--  endYear              integer,
--  runtimeMinutes       integer,
--  genres               varchar(255))
--;


# --- !Downs

DROP TABLE if EXISTS title_basics;


--CREATE TABLE title_basicsa (tconst varchar(255), titleType varchar(255), primaryTitle varchar(255), originalTitle varchar(255), isAdult boolean, startYear integer, endYear integer, runtimeMinutes integer,genres varchar(255));