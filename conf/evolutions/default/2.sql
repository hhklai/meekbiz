# --- 2nd database schema

# --- !Ups

create table S3File (
  id                        char(36) not null,
  publicId                  char(36) not null,
  ownerId                   char(36) not null,
  constraint pk_s3file primary key (id),
  constraint uk_s3file unique key (publicId),
  constraint fk1_s3file foreign key (ownerId) references User(id) on delete restrict
);

alter table User
add profilePic char(36);

alter table User
add foreign key (profilePic) references S3File(publicId) on delete set null;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

alter table User
drop foreign key user_ibfk_1;

alter table User
drop column profilePic;

drop table if exists S3File;

SET REFERENTIAL_INTEGRITY TRUE;

