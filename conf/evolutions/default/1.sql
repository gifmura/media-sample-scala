# --- !Ups
CREATE TABLE IF NOT EXISTS account (
  id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name varchar(20) NOT NULL UNIQUE,
  password varchar(20) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS entry (
  id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  accountid int NOT NULL,
  imageurl varchar(100),
  title varchar(100),
  body text
) ENGINE=InnoDB;
--   FOREIGN KEY(accountid) REFERENCES account(id)

# --- !Downs

-- drop table 'account' if exists;
