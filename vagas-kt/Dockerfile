FROM alpine:3.13
RUN apk add openjdk11 postgresql &&\
    mkdir /run/postgresql &&\
    chown postgres:postgres /run/postgresql &&\
    adduser vagas -D &&\
    su postgres -c "mkdir /var/log/postgresql/data"

COPY build/libs/vagas-kt-1.0.0.jar /
COPY --chown=vagas:vagas scripts/creation.sql /home/vagas
COPY --chown=vagas:vagas scripts/tables.sql /home/vagas
COPY --chown=vagas:vagas scripts/populate.sql /home/vagas

CMD su postgres -c "initdb /var/lib/postgresql/data" &&\
    su postgres -c "pg_ctl start -D /var/lib/postgresql/data -l /var/lib/postgresql/data/log.log" &&\
    su postgres -c "echo \"host all  all    0.0.0.0/0  md5\" >> /var/lib/postgresql/data/pg_hba.conf" &&\
    su postgres -c "echo \"listen_addresses='*'\" >> /var/lib/postgresql/data/postgresql.conf" &&\
    su postgres -c "pg_ctl status -D /var/lib/postgresql/data" &&\
    su postgres -c "psql -f /home/vagas/creation.sql" &&\
    su vagas -c "psql -f /home/vagas/tables.sql" &&\
    su vagas -c "psql -f /home/vagas/populate.sql" &&\
    java -jar /vagas-kt-1.0.0.jar

EXPOSE 9000