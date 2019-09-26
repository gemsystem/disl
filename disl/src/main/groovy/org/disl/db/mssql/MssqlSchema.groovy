/*
 * Copyright 2015 - 2016 Karel H�bl <karel.huebl@gmail.com>.
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
package org.disl.db.mssql

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.transform.CompileStatic

import org.disl.meta.Mapping
import org.disl.meta.PhysicalSchema
import org.junit.Assert
/**
 * Implementation of MS SQL Server PhysicalSchema based on JTDS JDBC driver (groupId: net.sourceforge.jtds, artifactId: jtds). 
 * */
@CompileStatic
class MssqlSchema extends PhysicalSchema {
	String host
	int port
	String instance
	String domain
	String useUnicode
	String characterEncoding
	String multiSubnetFailover
	String integratedSecurity

	String jdbcDriver="net.sourceforge.jtds.jdbc.Driver"
	
	@Override
	public void init() {
		super.init();
		host=getSchemaProperty('host')
		port=Integer.parseInt(getSchemaProperty('port','1433'))
		databaseName=getSchemaProperty('databaseName')
		instance=getSchemaProperty('instance')
		domain=getSchemaProperty('domain')
		useUnicode=getSchemaProperty('useUnicode')
		characterEncoding=getSchemaProperty('characterEncoding')
		multiSubnetFailover=getSchemaProperty('multiSubnetFailover')
		integratedSecurity=getSchemaProperty('integratedSecurity')
	}

	public String getJdbcUrl() {
        String userElement = ""
        if (!(getIntegratedSecurity()!=null && getIntegratedSecurity().equalsIgnoreCase("true"))) {
            userElement=";user=${getUser()}"
        }
        String passwordElement=""
        if (!(getIntegratedSecurity()!=null && getIntegratedSecurity().equalsIgnoreCase("true"))) {
            passwordElement=";password=${getPassword()}"
        }
		String instanceElement=""
		if (getInstance()!=null) {
			instanceElement=";instance=${getInstance()}"
		}
		String domainElement=""
		if (getDomain()!=null) {
			domainElement=";domain=${getDomain()}"
		}
		String useUnicodeElement=""
		if (getUseUnicode()!=null) {
			useUnicodeElement=";useUnicode=${getUseUnicode()}"
		}
		String characterEncodingElement=""
		if (getCharacterEncoding()!=null) {
			characterEncodingElement=";characterEncoding=${getCharacterEncoding()}"
		}
		String multiSubnetFailoverElement=""
		if (getMultiSubnetFailover()!=null) {
			multiSubnetFailoverElement=";multiSubnetFailover=${getMultiSubnetFailover()}"
		}
		String integratedSecurityElement=""
		if (getIntegratedSecurity()!=null) {
            integratedSecurityElement=";integratedSecurity=${getIntegratedSecurity()}"
		}
		String myUrl="jdbc:jtds:sqlserver://${getHost()}:${getPort()}/${getDatabaseName()}${domainElement}${instanceElement}${userElement}${passwordElement}${useUnicodeElement}${characterEncodingElement}${multiSubnetFailoverElement}${integratedSecurityElement}"
		//println myUrl
		return myUrl
	}

	@Override
	protected Sql createSql() {
		//Pass username & password to jdbcUrl to prevent SSO error when ntlmauth.dll is not on path.
		def sql=Sql.newInstance(getJdbcUrl(),getJdbcDriver());
		sql.getConnection().setAutoCommit(false)
		sql
	}
	
	@Override
	public String evaluateExpressionQuery(String expression) {
		"SELECT ${expression}"
	}
	
	@Override
	public String evaluateConditionQuery(String expression) {
		"select 1 where ${expression}"
	}
	
	@Override
	public String getRecordQuery(int index,String expressions) {
		"select ${index} as DUMMY_KEY,${expressions}\n"
	}
	
	@Override
	public void validateViewDeployment(Mapping mapping, Sql sql=getSql()) {
		super.validateViewDeployment(mapping, sql);
		
		String dataDictionaryQuery="select VIEW_DEFINITION from ${getDatabaseName()}.INFORMATION_SCHEMA.VIEWS where TABLE_NAME='${mapping.name}' and TABLE_SCHEMA='${getSchema()}' and TABLE_CATALOG='${databaseName}'"
		GroovyRowResult result=sql.firstRow(dataDictionaryQuery)
		if (!result) {
			throw new AssertionError("View definition not found in data dictionary. Check ${dataDictionaryQuery}.")
		}
		String expected="""CREATE VIEW ${schema}.${mapping.name} AS
${mapping.getSQLQuery()}
"""
		String actual=result[0]
		
		Assert.assertEquals("View definition of deployed ${mapping.refference} does not match to model.",expected,actual)
	}
}
