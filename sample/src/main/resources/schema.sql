create user test password 'test';
create database users;
grant all privileges on database users to test;

create sequence users_id_seq
create table users(id bigint  primary key, name varchar(256), age int, sex varchar(16)) ;
create table user_info(user_id bigint, faculty varchar(128), address varchar(128), phone varchar(16), foreign key (user_id) references users(id)) ;

insert into users(id, name, age, sex) values(nextval('users_id_seq'), 'Властелин Темных Сил', 30, 'М');

