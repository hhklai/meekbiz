# --- 3nd database schema

# --- !Ups

/* Miz contains non editable meta data for a service */
create table Miz (
  id                        char(36) not null,
  ownerId                   char(36) not null,
  editable                  tinyint(1) not null,  /*not editable once published*/
  isPublic                  tinyint(1) not null,
  urlTitle                  varchar(255) not null,
  initialModeratedBy        char(36),
  initialModerationDate     timestamp,
  highQuality               tinyint(1),           /*flag added by moderator to boost service*/
  bidStartDate              timestamp,
  bidEndDate                timestamp,
  serviceValidStartDate     timestamp,
  serviceExpiryDate         timestamp,
  locationEnum              tinyint not null,  /*at specified location, at client's location*/
  locationRadius            float, /*at client's locations only if less than radius in km*/
  locationLat               double,  /*center of location radius*/
  locationLng               double,  /*center of location radius*/
  locationRegion            varchar(36) not null,  /*region to have ad displayed, ie. Calgary*/
  minCustomers              int,
  maxCustomers              int,
  minPricePerCustomer       float,
  valuePropositionEnum      tinyint not null,  /*value discount or high quality with great credentials*/
  searchSynched           boolean,
  constraint pk_miz primary key (id),
  constraint uk_miz unique key (ownerId, urlTitle),
  constraint fk1_miz foreign key (ownerId) references User(id) on delete restrict,
  constraint fk2_miz foreign key (initialModeratedBy) references User(id) on delete restrict
);

CREATE INDEX miz_created_index ON Miz(isPublic, searchSynched);

/* Miz Content contains editable content,  a history is kept by creating a new one each time the service is edited */
create table MizContent (
  id                        char(36) not null,
  mizId                     char(36) not null,
  moderatedBy               char(36),
  createdDate               timestamp not null,  /*used for versioning when there are changes and updates*/
  title                     varchar(255) not null,
  summary                   text,
  approxServiceDurationEnum tinyint not null,
  bannerPic                 char(36),
  thumbnail                 char(36),
  contentBody               longtext,
  constraint pk_mizcontent primary key (id),
  constraint fk1_mizcontent foreign key (mizId) references Miz(id) on delete restrict,
  constraint fk2_mizcontent foreign key (bannerPic) references S3File(publicId) on delete set null,
  constraint fk3_mizcontent foreign key (thumbnail) references S3File(publicId) on delete set null,
  constraint fk4_mizcontent foreign key (moderatedBy) references User(id) on delete restrict
);

CREATE INDEX mizcontent_created_index ON MizContent(createdDate);


# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

DROP INDEX miz_created_index ON Miz;
DROP INDEX mizcontent_created_index ON MizContent;
drop table if exists MizContent;
drop table if exists Miz;

SET REFERENTIAL_INTEGRITY TRUE;

