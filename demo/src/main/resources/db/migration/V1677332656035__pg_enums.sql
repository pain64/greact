create type gender as enum ('MALE', 'FEMALE');
create type term as enum ('SPRING', 'AUTUMN');

alter table teachers drop column gender;
alter table teachers drop column term;

alter table teachers add column gender gender;
alter table teachers add column term term;
