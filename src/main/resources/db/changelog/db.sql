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