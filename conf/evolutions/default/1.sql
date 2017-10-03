# --- First database schema

# --- !Ups

create table User (
  id                        char(36) not null,
  name                      varchar(255) unique not null,
  email                     varchar(255) unique not null,
  password                  varchar(255),
  isAdmin                   bool,
  isModerator               bool,
  constraint pk_user primary key (id)
);

CREATE INDEX user_email_index ON User (email);
CREATE INDEX user_name_index ON User (name);
# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

DROP INDEX user_email_index ON User;
DROP INDEX user_name_index ON User;

drop table if exists User;

SET REFERENTIAL_INTEGRITY TRUE;

