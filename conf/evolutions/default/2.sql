# --- !Ups

CREATE TABLE IF NOT EXISTS entry (
  id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  user_id int NOT NULL,
  title varchar(100) NOT NULL,
  content text NOT NULL,
  create_time TIMESTAMP NOT NULL DEFAULT NOW(),
  update_time TIMESTAMP NOT NULL DEFAULT NOW(),
  status varchar(20) NOT NULL DEFAULT 'ACTIVE',
  FOREIGN KEY(user_id) REFERENCES user(id) ON UPDATE CASCADE
) ENGINE=InnoDB;

# --- !Downs

DROP TABLE IF EXISTS entry;