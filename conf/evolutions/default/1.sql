# --- !Ups

CREATE TABLE IF NOT EXISTS account (
  id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name varchar(20) NOT NULL UNIQUE,
  password varchar(20) NOT NULL
) ENGINE=InnoDB;

# --- !Downs

-- DROP TABLE IF EXISTS image;