package com.ideatec.definition_generator.resources;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JDBCDriver {

    MYSQL("com.mysql.cj.jdbc.Driver" , "mysql"),
    MARIADB("org.mariadb.jdbc.Driver" , "mariadb"),
    POSTGRESQL("org.postgresql.Driver" , "postgresql"),
    ORACLE("oracle.jdbc.OracleDriver" , "oracle");

    private final String driver;
    private final String type;

}
