# embedded-postgresql-postgis
Embedded Postgresq (with Postgis)  

A small demo project using **OpenTable Embedded PostgreSQL Component** (https://github.com/opentable/otj-pg-embedded/) with **Postgis**.

Sometimes, `H2` database is not enough for testing purposes :
 - GIS is incomplete and partially deprecated
 - Some SQL statements is not H2 compliant (could happen with some native queries under JPA) ex: https://www.postgresql.org/docs/10/sql-values.html

In this case, an embedded Postgres could be a better option.
