/*
 * Copyright 2015 - 2018 Antonín Krotký <antoninkrotky@gmail.com>.
 *
 * This file is part of disl.
 *
 * Disl is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Disl is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Disl.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.disl.db.mysql

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.transform.CompileStatic
import org.disl.meta.Mapping
import org.disl.meta.PhysicalSchema
import org.junit.Assert

/**
 * Implementation of My SQL Server PhysicalSchema based on JDBC driver https://mvnrepository.com/artifact/mysql/mysql-connector-java
 * */
@CompileStatic
class MysqlSchema extends PhysicalSchema {
	String host
	int port
	String instance

	String jdbcDriver="com.mysql.cj.jdbc.Driver"
	
	@Override
	public void init() {
		super.init();
		host=getSchemaProperty('host')
		port=Integer.parseInt(getSchemaProperty('port','3306'))
		databaseName=getSchemaProperty('databaseName')
		instance=getSchemaProperty('instance')
	}

	public String getJdbcUrl() {
		if (getInstance()==null) {
			return "jdbc:mysql://${getHost()}:${getPort()}/${getDatabaseName()}?useSSL=FALSE&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC"
		}
		"jdbc:mysql://${getHost()}:${getPort()}/${getDatabaseName()}?useSSL=FALSE&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC"
	}

	@Override
	public String evaluateExpressionQuery(String expression) {
		"SELECT ${expression} FROM DUAL"
	}
	
	@Override
	public String evaluateConditionQuery(String expression) {
		"select 1 from dual where ${expression}"
	}
	
	@Override
	public String getRecordQuery(int index,String expressions) {
		"select ${index} as DUMMY_KEY,${expressions} from dual\n"
	}
	
}
