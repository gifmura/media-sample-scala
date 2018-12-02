# --- !Ups

CREATE TABLE IF NOT EXISTS entry (
  id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  accountid int NOT NULL,
  title varchar(100),
  body text
-- FOREIGN KEY(accountid) REFERENCES account(id)
) ENGINE=InnoDB;

# --- !Downs

-- DROP TABLE IF EXISTS entry;
