# --- !Ups

CREATE TABLE IF NOT EXISTS account (
  id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name varchar(20) NOT NULL PRIMARY KEY,
  password varchar(20) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS diary (
  id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  accountid int NOT NULL,
  imageid int,
  title varchar(100),
  body text
--   FOREIGN KEY(accountid) REFERENCES account(id)
) ENGINE=InnoDB;

# --- !Downs

-- drop table 'account' if exists;
