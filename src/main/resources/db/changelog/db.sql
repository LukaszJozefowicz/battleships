--liquibase formatted sql
--changeset lukasz:1
CREATE TABLE IF NOT EXISTS roles
(
    id bigint NOT NULL,
    name VARCHAR(255) NOT NULL,
    primary key(id),
    CONSTRAINT name_unique UNIQUE (name)
);

--changeset lukasz:2
CREATE TABLE IF NOT EXISTS users
(
    id bigint NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    primary key(id),
    CONSTRAINT username_unique UNIQUE (username),
    CONSTRAINT email_unique UNIQUE (email)
);

--changeset lukasz:3
CREATE TABLE IF NOT EXISTS users_roles
(
    user_id bigint NOT NULL,
    role_id bigint NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users,
    FOREIGN KEY (role_id) REFERENCES roles
);

--changeset lukasz:4
CREATE SEQUENCE IF NOT EXISTS users_sequence OWNED BY users.id;

--changeset lukasz:5
insert into roles (id, name) values(1, 'ROLE_USER');
insert into roles (id, name) values(2, 'ROLE_ADMIN');

--changeset lukasz:6
CREATE TABLE IF NOT EXISTS boards (
    id BIGINT NOT NULL,
    persisted_board text,
    CONSTRAINT board_pk PRIMARY KEY (id)
);

--changeset lukasz:7
CREATE TABLE IF NOT EXISTS games (
    id BIGINT NOT NULL,
    player1_id BIGINT NOT NULL,
    player2_id BIGINT,
    game_state VARCHAR(20) NOT NULL,
    player1_board BIGINT NOT NULL,
    player2_board BIGINT,
    player_turn VARCHAR(20) NOT NULL,
    CONSTRAINT games_pk PRIMARY KEY (id),
    CONSTRAINT games_fk_1 FOREIGN KEY (player1_id) REFERENCES users(id),
    CONSTRAINT games_fk_2 FOREIGN KEY (player2_id) REFERENCES users(id),
    CONSTRAINT games_fk_3 FOREIGN KEY (player1_board) REFERENCES boards(id),
    CONSTRAINT games_fk_4 FOREIGN KEY (player2_board) REFERENCES boards(id)
);

--changeset lukasz:8
CREATE SEQUENCE IF NOT EXISTS games_sequence OWNED BY games.id;
CREATE SEQUENCE IF NOT EXISTS boards_sequence OWNED BY boards.id;

--changeset lukasz:9
create table if not exists allowed_ships
(
    id bigint not null,
    type varchar(25),
    length int not null,
    num_allowed int not null,
    CONSTRAINT allowed_ships_pk primary key (id)
);
insert into allowed_ships (id, type, length, num_allowed) values(1, 'carrier', 4, 1);
insert into allowed_ships (id, type, length, num_allowed) values(2, 'battleship', 3, 2);
insert into allowed_ships (id, type, length, num_allowed) values(3, 'destroyer', 2, 3);
insert into allowed_ships (id, type, length, num_allowed) values(4, 'patrolBoat', 1, 4);

--changeset lukasz:10
create table if not exists ships
(
    id bigint not null,
    type varchar(25),
    length int not null,
    fields varchar(100),
    board_id bigint,
    is_destroyed boolean not null default false,
    CONSTRAINT ships_pk primary key (id),
    CONSTRAINT ships_fk FOREIGN KEY (board_id) REFERENCES boards(id)
);

--changeset lukasz:11
CREATE SEQUENCE IF NOT EXISTS ships_sequence OWNED BY ships.id;

--changeset lukasz:12
insert into roles (id, name) values(3, 'ROLE_BOT');

--changeset lukasz:13
CREATE TABLE IF NOT EXISTS settings
(
    id bigint NOT NULL,
    difficulty VARCHAR(50) NOT NULL,
    starting_player VARCHAR(10) NOT NULL,
    ship_shape VARCHAR(50) NOT NULL,
    primary key(id)
);

INSERT INTO settings (id, difficulty, starting_player, ship_shape) VALUES(1, 'NORMAL', 'RANDOM', 'CLASSIC');

ALTER TABLE users ADD COLUMN IF NOT EXISTS settings_id bigint;
ALTER TABLE users ADD CONSTRAINT settings_fk FOREIGN KEY(settings_id) REFERENCES settings;

--changeset lukasz:14
CREATE SEQUENCE IF NOT EXISTS settings_sequence OWNED BY settings.id
    increment 1
    start 2
    minvalue 1
    maxvalue 9223372036854775807
    cache 1;

--changeset lukasz:15
drop table allowed_ships cascade;
create table if not exists allowed_ships
(
    id bigint not null,
    type varchar(25),
    length int not null,
    num_allowed int not null,
    CONSTRAINT allowed_ships_pk primary key (id)
);
insert into allowed_ships (id, type, length, num_allowed) values(1, 'superCarrier', 5, 1);
insert into allowed_ships (id, type, length, num_allowed) values(2, 'carrier', 4, 1);
insert into allowed_ships (id, type, length, num_allowed) values(3, 'battleship', 3, 2);
insert into allowed_ships (id, type, length, num_allowed) values(4, 'destroyer', 2, 3);
insert into allowed_ships (id, type, length, num_allowed) values(5, 'patrolBoat', 1, 4);