DROP TABLE IF EXISTS genatt_verify_by;
DROP TABLE IF EXISTS genatt_field;
DROP TABLE IF EXISTS genatt_response;
DROP TABLE IF EXISTS genatt_entry;
DROP TABLE IF EXISTS genatt_entry_type;

--
-- Table structure for table genatt_entry_type
--
CREATE TABLE genatt_entry_type (
	id_type int default 0 NOT NULL,
	title varchar(255),
	is_group smallint default NULL,
	is_comment int default NULL,
	is_mylutece_user smallint default NULL,
	class_name varchar(255),
	icon_name varchar(255),
	plugin varchar(255) NOT NULL,
	PRIMARY KEY (id_type)
);

CREATE INDEX index_genatt_entry_type_plugin ON genatt_entry_type (plugin);


--
-- Table structure for table genatt_entry
--
CREATE TABLE genatt_entry (
	id_entry int default 0 NOT NULL,
	id_resource int default 0 NOT NULL,
	resource_type VARCHAR(255) NOT NULL,
	id_type int default 0 NOT NULL,
	id_parent int default NULL,
	title long varchar,
	code varchar(100) default NULL,	
	help_message long varchar,
	comment long varchar,
	mandatory smallint default NULL,
	fields_in_line smallint default NULL,
	pos int default NULL,
	id_field_depend int default NULL,
	confirm_field smallint default NULL,
	confirm_field_title long varchar,
	field_unique smallint default NULL,
	map_provider varchar(45) default NULL,
	css_class varchar(255) default NULL,
	pos_conditional int default 0,
	error_message long varchar default NULL,
	num_row smallint default 0,
	num_column smallint default 0,
    is_role_associated smallint DEFAULT '0',
    is_only_display_back smallint DEFAULT '0',
	is_editable_back smallint DEFAULT '0',
	is_indexed smallint DEFAULT '0',
	is_shown_in_completeness smallint DEFAULT '0',
    PRIMARY KEY (id_entry)
);

CREATE INDEX index_genatt_entry_resource ON genatt_entry (id_resource);
CREATE INDEX index_genatt_entry_parent ON genatt_entry (id_parent);
CREATE INDEX index_genatt_code ON genatt_entry ( code);

ALTER TABLE genatt_entry ADD CONSTRAINT fk_genatt_entry_type FOREIGN KEY (id_type)
	REFERENCES genatt_entry_type (id_type);

--
-- Table structure for table genatt_field
--
CREATE TABLE genatt_field (
	id_field int default 0 NOT NULL,
	id_entry int default 0 NOT NULL,
	title varchar(255),
	code varchar(100) default NULL,
	value long varchar,
	height int default NULL,
	width int default NULL,
	default_value smallint default NULL,
	max_size_enter int default NULL,
	pos int default NULL,
	value_type_date date NULL,
	no_display_title smallint default NULL,
	comment long varchar default null,
    role_key varchar(50) DEFAULT NULL,
    image_type varchar(50) DEFAULT NULL,
    
	PRIMARY KEY (id_field)
);

CREATE INDEX index_genatt_field_entry ON genatt_field (id_entry);

ALTER TABLE genatt_field ADD CONSTRAINT fk_genatt_field_entry FOREIGN KEY (id_entry)
	REFERENCES genatt_entry (id_entry);
	
--
-- Table structure for table genatt_response
--
CREATE TABLE genatt_response (
	id_response int default 0 NOT NULL,
	response_value long VARCHAR DEFAULT NULL,
	id_entry int default NULL,
	iteration_number int default -1,
	id_field int default NULL,
	id_file int default NULL,
	status smallint default 1,
	PRIMARY KEY (id_response)
);

ALTER TABLE genatt_response ADD CONSTRAINT fk_genatt_response_entry FOREIGN KEY (id_entry)
	REFERENCES genatt_entry (id_entry);
CREATE INDEX index_genatt_response_entry ON genatt_response (id_entry);
	
--
-- Table structure for table genatt_verify_by
--
CREATE TABLE genatt_verify_by (
	id_field int default 0 NOT NULL,
	id_expression int default 0 NOT NULL,
	PRIMARY KEY (id_field,id_expression)
);

CREATE INDEX index_genatt_verify_by_field ON genatt_verify_by (id_field);
	
ALTER TABLE genatt_verify_by ADD CONSTRAINT fk_genatt_verify_by_field FOREIGN KEY (id_field)
	REFERENCES genatt_field (id_field);
	
