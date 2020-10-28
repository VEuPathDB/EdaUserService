-- the EDA service doesn't need to know much about the Study, because the WDK will serve that data
-- the abbrev would be used in the name of the tall and ancestors tables
-- the study_id is a stable ID
create table Study (
  study_id varchar(30) not null,
  abbrev varchar(20) not null,
  PRIMARY KEY (study_id)
);
alter table study add unique (abbrev);

-- a controlled vocab of entity names, eg "Household", "Participant"
-- (the abbrev would be used in the name of the tall and ancestors tables, 
-- IF the entity ID is not recognizable)
CREATE TABLE EntityName (
  entity_name_id integer not null,
  display_name varchar(30) not null,
  display_name_plural varchar (30) not null,
  abbrev varchar(25),
  PRIMARY KEY (entity_name_id),
);
alter table entityname add unique (display_name);
alter table entityname add unique (display_name_plural);
alter table entityname add unique (abbrev);

-- the entity_id is a stable id
CREATE TABLE Entity (
  entity_id varchar(30) not null,
  entity_name_id integer not null,
  study_id varchar(30) not null,
  parent_entity_id varchar(30),
  description varchar(100),
  PRIMARY KEY (entity_id),
);
alter table entity add unique (entity_name_id, study_id);
ALTER TABLE Entity 
   ADD FOREIGN KEY (entity_name_id) REFERENCES EntityName (entity_name_id);
ALTER TABLE Entity 
   ADD FOREIGN KEY (study_id) REFERENCES Study (study_id); 
   
   
-- a controlled vocabulary of variable types, eg "string", "date", "number"
create table VariableType (
  variable_type_id integer not null,
  variable_type varchar(20) not null,
  PRIMARY KEY (variable_type_id),
);
alter table VariableType add unique (variable_type);

-- this is the brief version of the ontology tree that is needed by EDA
-- a "variable" might have values or not.  if not, it is just a category.
-- the "variable_id" would hold the ontology term (ie, is a stable ID)
-- we might want to add an variable_id (integer) for performance reasons?
create table Variable (
  variable_id varchar(30),
  variable_type_id integer not null,
  entity_id varchar(30) not null,
  parent_variable_id varchar(30),
  provider_label varchar(30) not null,
  display_name varchar(30) not null,
  has_values integer,
  is_continuous integer,
  units varchar (30),
  precision integer,
  PRIMARY KEY (variable_id)
);
ALTER TABLE Variable 
   ADD FOREIGN KEY (entity_id) REFERENCES Entity (entity_id);
ALTER TABLE Variable 
   ADD FOREIGN KEY (variable_type_id) REFERENCES VariableType (variable_type_id); 
ALTER TABLE Variable 
   ADD FOREIGN KEY (parent_variable_id) REFERENCES Variable (variable_id); 

-------------------------------------------------------------------------------------   
-- THE FOLLOWING TABLES ARE AN EXAMPLE OF THE TABLES FOR A PARTICULAR FAKE STUDY (GEMS)
-------------------------------------------------------------------------------------   
-- might want to use an integer internal_variable_id for performance
create table GEMS_House_tall (
  GEMS_House_id integer,
  variable_id varchar(30),
  number_value integer, 
  string_value varchar(100),
  date_value varchar(30),
  PRIMARY KEY (GEMS_House_id)
);
ALTER TABLE GEMS_House_tall 
   ADD FOREIGN KEY (variable_id) REFERENCES Variable (variable_id); 
CREATE UNIQUE INDEX GEMS_House_tall_i1
ON GEMS_House_tall (variable_id, number_value, GEMS_House_id);
CREATE UNIQUE INDEX GEMS_House_tall_i2
ON GEMS_House_tall (variable_id, string_value, GEMS_House_id);
CREATE UNIQUE INDEX GEMS_House_tall_i3
ON GEMS_House_tall (variable_id, date_value, GEMS_House_id);

create table GEMS_House_ancestors (
  GEMS_House_id integer,
  PRIMARY KEY (GEMS_House_id)
);
CREATE UNIQUE INDEX GEMS_House_ancestors_i1
ON GEMS_House_ancestors (GEMS_House_id);

create table GEMS_HouseObs_tall (
  GEMS_HouseObs_id integer,
  variable_id varchar(30),
  number_value integer, 
  string_value varchar(100),
  date_value varchar(30),
  PRIMARY KEY (GEMS_HouseObs_id)
);
ALTER TABLE GEMS_HouseObs_tall 
   ADD FOREIGN KEY (variable_id) REFERENCES Variable (variable_id); 
CREATE UNIQUE INDEX GEMS_HouseObs_tall_i1
ON GEMS_HouseObs_tall (variable_id, number_value, GEMS_HouseObs_id);
CREATE UNIQUE INDEX GEMS_HouseObs_tall_i2
ON GEMS_HouseObs_tall (variable_id, string_value, GEMS_HouseObs_id);
CREATE UNIQUE INDEX GEMS_HouseObs_tall_i3
ON GEMS_HouseObs_tall (variable_id, date_value, GEMS_HouseObs_id);

create table GEMS_HouseObs_ancestors (
  GEMS_HouseObs_id integer,
  GEMS_House_id integer,
);
CREATE UNIQUE INDEX GEMS_HouseObs_ancestors_i1
ON GEMS_HouseObs_ancestors (GEMS_HouseObs_id);

create table GEMS_Part_tall (
  GEMS_Part_id integer,
  variable_id varchar(30),
  number_value integer, 
  string_value varchar(100),
  date_value varchar(30),
);
ALTER TABLE GEMS_Part_tall 
   ADD FOREIGN KEY (variable_id) REFERENCES Variable (variable_id); 
CREATE UNIQUE INDEX GEMS_Part_tall_i1
ON GEMS_Part_tall (variable_id, number_value, GEMS_Part_id);
CREATE UNIQUE INDEX GEMS_Part_tall_i2
ON GEMS_Part_tall (variable_id, string_value, GEMS_Part_id);
CREATE UNIQUE INDEX GEMS_Part_tall_i3
ON GEMS_Part_tall (variable_id, date_value, GEMS_Part_id);

create table GEMS_Part_ancestors (
  GEMS_Part_id integer,
  GEMS_House_id integer, 
  PRIMARY KEY (GEMS_Part_id)
);
CREATE UNIQUE INDEX GEMS_Part_ancestors_i1
ON GEMS_Part_ancestors (GEMS_Part_id);

create table GEMS_PartObs_tall (
  GEMS_PartObs_id integer,
  variable_id varchar(30),
  number_value integer, 
  string_value varchar(100),
  date_value varchar(30),
);
ALTER TABLE GEMS_PartObs_tall 
   ADD FOREIGN KEY (variable_id) REFERENCES Variable (variable_id); 
CREATE UNIQUE INDEX GEMS_PartObs_tall_i1
ON GEMS_PartObs_tall (variable_id, number_value, GEMS_PartObs_id);
CREATE UNIQUE INDEX GEMS_PartObs_tall_i2
ON GEMS_PartObs_tall (variable_id, string_value, GEMS_PartObs_id);
CREATE UNIQUE INDEX GEMS_PartObs_tall_i3
ON GEMS_PartObs_tall (variable_id, date_value, GEMS_PartObs_id);

create table GEMS_PartObs_ancestors (
  GEMS_PartObs_id integer,
  GEMS_Part_id integer,
  GEMS_House_id integer, 
  PRIMARY KEY (GEMS_PartObs_id)
);
CREATE UNIQUE INDEX GEMS_PartObs_ancestors_i1
ON GEMS_PartObs_ancestors (GEMS_PartObs_id);



