### IMDB copycat

A Scala-Play-Postgres Web App, based on this sample: https://developer.lightbend.com/start/?group=play&project=play-scala-anorm-example

Get IMDB datasets from here: https://www.imdb.com/interfaces/


### Prerequisites

Install & Start postgresql:
```
apt-get install postgresql postgresql-contrib
update-rc.d postgresql enable
service postgresql start
```

Set password for *postgres* user (not set by default):
```
sudo -u postgres psql
ALTER USER postgres PASSWORD 'postgres';
```

Create database & grant privileges:
```
create database imdbcopycat;
grant all privileges on database imdbcopycat to postgres;
```

Run app in dev mode:
```
sbt run
```


