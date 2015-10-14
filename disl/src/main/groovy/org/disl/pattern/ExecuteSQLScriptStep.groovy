/*
 * Copyright 2015 Karel H�bl <karel.huebl@gmail.com>.
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
package org.disl.pattern;

import org.disl.meta.Table;

import groovy.sql.Sql

abstract class ExecuteSQLScriptStep extends Step {

	public static final String BACKSLASH_NEW_LINE = "\\\\\n"
	
	boolean ignoreErrors=false;
	String commandSeparator=";";

	abstract Sql getSql();

	public int executeInternal() {
		try {
			
		int processedRows=0
		getCommands().each {
			processedRows=processedRows+executeSqlStatement(it)
		}
		getSql().commit()
		return processedRows
		} catch (Exception e) {
			getSql().rollback()
			throw e
		}
	}
	
	protected Collection<String> getCommands() {
		return code.split(getCommandSeparator())
	}

	protected int executeSqlStatement(String sqlCommand) {
		if (''.equals(sqlCommand.trim())) {
			return 0
		}
		try {
			return executeSqlStatementInternal(sqlCommand)
		} catch (Exception e) {
			if (!isIgnoreErrors()) {
				throw new RuntimeException("Error executing ${this}. SQL statement: $sqlCommand",e)
			}
			return 0
		}
	}

	protected int executeSqlStatementInternal(String sqlCommand) {
		return getSql().executeUpdate(sqlCommand)
	}
	
}
