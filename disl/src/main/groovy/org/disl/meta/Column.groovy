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
package org.disl.meta

import groovy.transform.CompileStatic;

/**
 * Column of database table or view.
 * */
@CompileStatic
class Column extends AbstractSqlExpression {
	String propertyName
	String name
	MappingSource parent
	String description
	String dataType
	String defaultValue
	String check
	boolean notNull=false
	boolean primaryKey = false
	List<String> uiFields = []

	Column(){}
	
	Column(String propertyName, String name,Table parent) {
		this.propertyName=propertyName
		this.name=name
		this.parent=parent
	}
	
	String getColumnDefinition() {
		getPhysicalSchema().getColumnDefinition(this)		
	}
	
	PhysicalSchema getPhysicalSchema() {
		Context.getContext().getPhysicalSchema(getParent().getSchema())
	}

	String getNameWithoutParenthesis() {
		this.@name
	}

	String getName() {
		String leftParenthesisColumnName=Context.getContext().getProperty('leftParenthesisColumnName')
		String rightParenthesisColumnName=Context.getContext().getProperty('rightParenthesisColumnName')
		"${leftParenthesisColumnName?:''}${nameWithoutParenthesis}${rightParenthesisColumnName?:''}"
	}
	
	String toString(){
		if (parent==null || parent.getSourceAlias()==null) {
			return getName()
		}
		"${parent.getSourceAlias()}.${getName()}"
	}
} 
