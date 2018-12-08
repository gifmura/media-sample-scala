# --- !Ups

CREATE TABLE IF NOT EXISTS image (
  id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  entryid int NOT NULL,
  url varchar(100) NOT NULL,
  FOREIGN KEY(entryid) REFERENCES entry(id) ON UPDATE CASCADE
) ENGINE=InnoDB;

# --- !Downs

-- DROP TABLE IF EXISTS account;