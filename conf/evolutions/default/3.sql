# --- !Ups

CREATE TABLE IF NOT EXISTS image (
  id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  entry_id int NOT NULL,
  uri varchar(200) NOT NULL,
  size int NOT NULL,
  FOREIGN KEY(entry_id) REFERENCES entry(id) ON UPDATE CASCADE
) ENGINE=InnoDB;

# --- !Downs

-- DROP TABLE IF EXISTS account;