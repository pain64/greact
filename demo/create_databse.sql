-- > sudo su postgres
-- > psql

create database jstack_demo;
create user jstack with password '1234';
grant all privileges on database jstack_demo to jstack;