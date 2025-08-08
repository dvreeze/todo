
# PostgreSQL database initialization

## Creating and initializing the PostgreSQL database

Here the PostgreSQL database server is assumed to be a Docker container, although it does not have to be.

The database is not initialized by the application, but must be initialized manually by the user
before running the application. That is, the user must run `create-db.sql` followed by
`load-init-data.sql` (one time), after starting the database server for the first time.

See [PostgreSQL Docker setup](https://www.baeldung.com/ops/postgresql-docker-setup) for a good article
on setting up PostgreSQL Docker containers. Also see
[how to use PostgreSQL Docker official image](https://www.docker.com/blog/how-to-use-the-postgres-docker-official-image/).

Steps:

```shell
# First "cd" into the root directory of this project

docker pull postgres

mkdir ~/postgresdata

# Note we do not create any volume, in order to avoid "role does not exist" issues.

docker run -d \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=postgres \
  -p 5432:5432 \
  -v ~/postgresdata:/var/lib/postgresql/data \
  --name postgresql \
  postgres

docker cp ./src/main/resources/db/create-db.sql postgresql:/tmp
docker cp ./src/main/resources/db/load-init-data.sql postgresql:/tmp

docker exec -it postgresql psql -U postgres

# We are now inside the running postgresql container, inside psql

CREATE DATABASE tododb;
# Check database "tododb" exists
\list

# Help
\?

\c tododb

\i /tmp/create-db.sql
\i /tmp/load-init-data.sql

# Displaying the tables
\dt

\d task

# Querying, but remember to add a semicolon at the end or else it does not work
select * from task;

\q

# We have left the running postgres container again
```

Now we can run the application against this database.

## Running the app, after one-time database initialization

Starting and stopping the application, after one-time database initialization, and after
starting the PostgreSQL Docker container:

```bash
mvn spring-boot:start

# Querying for tasks (we can do that in the browser too, of course)
curl -v \
  -H 'Accept: application/json' \
  http://localhost:8080/tasks.json

# Adding a task (as JSON)
curl -v \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{ "name": "mail regelen", "description": "overgaan op nieuwe mail provider", "targetEndOption": "2025-09-01T00:00:00Z", "extraInformationOption": null, "closed": false }' \
  http://localhost:8080/tasks.json

# Adding an address (as JSON)
curl -v \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{ "addressName": "tandarts", "addressLines": [ "kerkstraat 12" ], "zipCode": "6789ZZ", "city": "Havenstad", "countryCode": "NL" }' \
  http://localhost:8080/addresses.json

# Adding an appointment (as JSON)
curl -v \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{ "name": "tandarts-202508", "start": "2025-08-08T16:00:00Z", "end": "2025-08-08T17:00:00Z", "addressOption": { "idOption": 2, "addressName": "tandarts", "addressLines": [ "kerkstraat 12" ], "zipCode": "6789ZZ", "city": "Havenstad", "countryCode": "NL" }, "extraInformationOption": null }' \
  http://localhost:8080/appointments.json

# When we are ready to stop the application..
mvn spring-boot:stop
```

If after stopping the application port 8080 is still occupied, find the corresponding process and bring
that process down. In Linux:

```bash
# Find the culprit
lsof -i :8080
```
