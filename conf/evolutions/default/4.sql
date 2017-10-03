# --- 4th database schema

# --- !Ups

create table MizToS3File (
  mizId                     char(36) not null,
  s3Id                      char(36) not null,
  flag                      smallint,
  constraint pk_miz2s3file primary key (mizId, s3Id),
  constraint fk1_miz2s3file foreign key (mizId) references Miz(id) on delete restrict,
  constraint fk2_miz2s3file foreign key (s3Id) references S3File(publicId) on delete restrict
);

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists MizToS3File;

SET REFERENTIAL_INTEGRITY TRUE;

