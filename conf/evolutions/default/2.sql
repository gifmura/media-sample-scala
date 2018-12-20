# --- !Ups

CREATE TABLE IF NOT EXISTS entry (
  id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  title varchar(100),
  body text,
  user_id int NOT NULL,
  FOREIGN KEY(user_id) REFERENCES user(id) ON UPDATE CASCADE
) ENGINE=InnoDB;

# --- !Downs

-- DROP TABLE IF EXISTS entry;