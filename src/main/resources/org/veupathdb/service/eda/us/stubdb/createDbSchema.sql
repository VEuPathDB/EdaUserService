-- Use oracle syntax throughout execution
SET DATABASE SQL SYNTAX ORA TRUE;

-- Contains all users who own analysis instances or preferences in this DB
CREATE TABLE users (
  user_id integer not null,
  is_guest integer not null,
  preferences clob,
  PRIMARY KEY (user_id)
);

--GRANT SELECT, INSERT, UPDATE, DELETE ON edauser.users TO COMM_WDK_W;

-- Contains analysis instance data
CREATE TABLE analysis (
  analysis_id varchar(50) not null,
  user_id integer not null,
  study_id varchar(50) not null,
  study_version varchar(50),
  api_version varchar(50),
  display_name varchar(50) not null,
  description varchar(4000),
  creation_time timestamp not null,
  modification_time timestamp not null,
  is_public integer not null,
  num_filters integer not null,
  num_computations integer not null,
  num_visualizations integer not null,
  analysis_descriptor clob,
  notes clob,
  provenance clob,
  PRIMARY KEY (analysis_id)
);
ALTER TABLE analysis ADD FOREIGN KEY (user_id) REFERENCES users (user_id);
CREATE INDEX analysis_user_id_idx ON analysis (user_id);

--GRANT SELECT, INSERT, UPDATE, DELETE ON edauser.analysis TO COMM_WDK_W;

CREATE TABLE derived_variables (
  variable_id   VARCHAR(36) NOT NULL,
  user_id       INTEGER NOT NULL,
  dataset_id    VARCHAR(50) NOT NULL,
  entity_id     VARCHAR(50) NOT NULL,
  display_name  VARCHAR(256) NOT NULL,
  description   VARCHAR(4000),
  provenance    CLOB,
  function_name VARCHAR(256) NOT NULL,
  config        CLOB NOT NULL,
  PRIMARY KEY (variable_id, user_id),
);

ALTER TABLE derived_variables ADD FOREIGN KEY (user_id) REFERENCES users(user_id);

--GRANT SELECT, INSERT, UPDATE, DELETE ON edauser.derived_variables TO COMM_WDK_W;
