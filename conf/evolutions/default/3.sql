# --- !Ups

CREATE TABLE IF NOT EXISTS image (
  id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  entryid int NOT NULL,
  url varchar(100) NOT NULL
) ENGINE=InnoDB;

# --- !Downs

-- DROP TABLE IF EXISTS image;
