
# H2 database initialization

## Creating and initializing the H2 database

The H2 database is a file-based database in the user's home directory.

It is not initialized by the application, but must be initialized manually by the user
before running the application. That is, the user must run `create-db.sql` followed by
`load-init-data.sql`.

Suppose we have downloaded H2 as file `h2-2.3.232.jar` into our `~/Downloads` folder,
then we can open a H2 terminal session as follows:

```bash
java -cp ~/Downloads/h2-2.3.232.jar org.h2.tools.Shell
```

Inside that session, we can execute SQL queries/statements, or commands such as `show tables;`.
Yet first the H2 shell will ask for JDBC URL (`jdbc:h2:~/tododb`), driver (leave it at the default,
namely `org.h2.Driver`), user (`sa`) and password (`password`).

## Running the app, after one-time database initialization

Starting and stopping the application, after one-time database initialization:

```bash
mvn spring-boot:start

mvn spring-boot:stop
```

After starting the application, point the browser at `http://localhost:8080/tasks.json`, for example.

If after stopping the application port 8080 is still occupied, find the corresponding process and bring
that process down. In Linux:

```bash
# Find the culprit
lsof -i :8080
```
