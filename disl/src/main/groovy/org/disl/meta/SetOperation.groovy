/*
 * Copyright 2015 - 2016 Karel Hübl <karel.huebl@gmail.com>.
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

import groovy.transform.CompileStatic

/**
 * Defines set operation to be performed on Mapping's sources.
 * */
@CompileStatic
public abstract class SetOperation {
	
	MappingSource source

	public abstract String getSetOperationClause(List<String> allReferencedColumns);

	static String getExpandedOrderedColumnList(List<String> allReferencedColumnsUpperCased, List<String> refferencedColumns) {
		List<String> refferencedColumnsUpperCased=refferencedColumns.collect{it.toUpperCase()}
		List<String> nullColsUpperCased=allReferencedColumnsUpperCased.minus(refferencedColumnsUpperCased)
		allReferencedColumnsUpperCased.collect{
			if (nullColsUpperCased.contains(it)) {
				return "null $it"
			}
			return it
		}.join(",")
	}

	protected String getSourceQuery(List<String> allReferencedColumns) {
		"select ${getExpandedOrderedColumnList(allReferencedColumns,source.getRefferenceColumnsStr())} from ${source.getRefference()}"
	}
	
	static class UNION extends SetOperation {
		@Override
		public String getSetOperationClause(List<String> allReferencedColumns) {
			"UNION ${getSourceQuery(allReferencedColumns)}"
		}
	}
	static class UNION_ALL extends SetOperation {
		@Override
		public String getSetOperationClause(List<String> allReferencedColumns) {
			"UNION ALL ${getSourceQuery(allReferencedColumns)}"
		}
	}
	static class INTERSECT extends SetOperation {
		@Override
		public String getSetOperationClause(List<String> allReferencedColumns) {
			"INTERSECT ${getSourceQuery(allReferencedColumns)}"
		}
	}
	static class MINUS extends SetOperation {
		@Override
		public String getSetOperationClause(List<String> allReferencedColumns) {
			"MINUS ${getSourceQuery(allReferencedColumns)}"
		}
	}
	static class EXCEPT extends SetOperation {
		@Override
		public String getSetOperationClause(List<String> allReferencedColumns) {
			"EXCEPT ${getSourceQuery(allReferencedColumns)}"
		}
	}


}
