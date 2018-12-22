# --- !Ups

CREATE TABLE IF NOT EXISTS user (
  id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  email varchar(50) NOT NULL UNIQUE,
  password varchar(20) NOT NULL,
  name varchar(20) NOT NULL UNIQUE,
  user_type varchar(20) NOT NULL DEFAULT 'NORMAL',
  registration_time TIMESTAMP NOT NULL DEFAULT NOW(),
  status varchar(20) NOT NULL DEFAULT 'ACTIVE'
) ENGINE=InnoDB;

# --- !Downs

-- DROP TABLE IF EXISTS image;