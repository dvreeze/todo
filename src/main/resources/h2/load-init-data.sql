insert into Task (name, description, target_end, extra_information)
values ('hogedrukreiniger', 'hogedrukreiniger thuis gebruiken', '2026-05-01', NULL);

insert into Address (name, address_line1, address_line2, address_line3, address_line4, zip_code, city)
values ('thuis', NULL, NULL, NULL, NULL, '0000ZZ', 'thuis');

insert into Appointment (name, start, end_date_time, address_id, extra_information)
select 'stofzuigen', '2020-02-02', '2020-02-02', addr.id, NULL
  from Address addr
 where name = 'thuis';
