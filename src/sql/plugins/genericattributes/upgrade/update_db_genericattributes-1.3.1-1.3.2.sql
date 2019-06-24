ALTER TABLE genatt_entry MODIFY COLUMN code varchar(100) default NULL; 
ALTER TABLE genatt_field MODIFY COLUMN code varchar(100) default NULL; 

CREATE INDEX index_genatt_code ON genatt_entry ( code);

ALTER TABLE genatt_entry ADD is_shown_in_completeness smallint DEFAULT '0';