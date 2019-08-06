ALTER TABLE genatt_entry MODIFY COLUMN code varchar(100) default NULL; 
ALTER TABLE genatt_field MODIFY COLUMN code varchar(100) default NULL; 

CREATE INDEX index_genatt_code ON genatt_entry ( code);

ALTER TABLE genatt_entry ADD is_shown_in_completeness smallint DEFAULT '0';

/*
    Note : AUTO_INCREMENT fields work with Mysql. For PostgreSQL, you have to 
    replace AUTO_INCREMENT, depending on your PostgreSQL version. You can
    find an example on buid.properties in target/lutece/sql directory after compilation.
*/
ALTER TABLE genatt_response MODIFY id_response INT AUTO_INCREMENT;