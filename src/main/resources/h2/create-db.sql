create table Address (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  address_line1 VARCHAR(500),
  address_line2 VARCHAR(500),
  address_line3 VARCHAR(500),
  address_line4 VARCHAR(500),
  zip_code VARCHAR(20) NOT NULL,
  city VARCHAR(75) NOT NULL,
  country_code VARCHAR(3) NOT NULL DEFAULT 'NL'
);

create table Appointment (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  start TIMESTAMP NOT NULL,
  end_date_time TIMESTAMP NOT NULL,
  address_id BIGINT,
  extra_information VARCHAR(1000),
  FOREIGN KEY (address_id) REFERENCES Address(id)
);

create table Task (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(1000) NOT NULL,
  target_end TIMESTAMP,
  extra_information VARCHAR(1000),
  closed BOOLEAN NOT NULL DEFAULT FALSE
);
