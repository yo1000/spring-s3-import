CREATE TABLE "user" (
    id                      varchar(40)     PRIMARY KEY,
    username                varchar(250),
    email                   varchar(250),
    given_name              varchar(80),
    family_name             varchar(80),
    gender                  varchar(10),
    birth_date              date,
    address                 varchar(250),
    creation_epoch_millis   bigint
);

CREATE UNIQUE INDEX uq__user__username_creation_epoch_millis ON "user" (username, creation_epoch_millis);

CREATE INDEX idx__user__email           ON "user" (email, creation_epoch_millis);
CREATE INDEX idx__user__creation_epoch  ON "user" (creation_epoch_millis);

CREATE TABLE user_node (
    id                      varchar(40)     PRIMARY KEY,
    rank                    integer,
    lastmod_epoch_millis    bigint
);

CREATE INDEX idx__user__lastmod_epoch   ON user_node (lastmod_epoch_millis);
