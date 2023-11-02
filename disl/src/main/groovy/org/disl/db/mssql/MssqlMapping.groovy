/*
 * Copyright 2023 - 2023 GEM System a.s. <disl@gemsystem.cz>.
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

import org.disl.meta.ColumnMapping
import org.disl.meta.Mapping
import org.disl.meta.SetOperation

/**
 *
 * @author Lukáš Vlk
 */
abstract class MssqlMapping extends Mapping {

    @Override
    String getSQLQuery() {
        if (setOperations.size()>0) {
            return """\
	/*Mapping ${name}*/
		SELECT
			${getQueryColumnList()}
		FROM
		(
		SELECT ${SetOperation.getExpandedOrderedColumnList(getAllRefferencedColumns(),getSources().get(0).getColumnsStr())}
		FROM
			${getSources().collect({it.fromClause}).join("\n			")}
		${getSetOperationClause()}
		) ${getSources().get(0).sourceAlias}
		WHERE
			${filter}
		${getGroupByClause()}${getHavingClause()}${getOrderByClause()}
	/*End of mapping $name*/"""
        } else {
            """\
	/*Mapping ${name}*/
		SELECT
			${getQueryColumnList()}
		FROM
			${getSources().collect({it.fromClause}).join("\n			")}
		${getPivotClause()}${getUnpivotClause()}WHERE
			${filter}
		${getGroupByClause()}${getHavingClause()}${getOrderByClause()}${getSetOperationClause()}
	/*End of mapping $name*/"""
        }
    }

    @Override
    void pivot(String aggregatePivotColumnMapping, String pivotFor) {
        pivot = """PIVOT
		(
			${aggregatePivotColumnMapping} 
			for ${pivotFor} in ( 
				${pivotColumns.collect {it.alias}.join(",\n\t\t\t\t")})
		) pvt
		"""
    }

    @Override
    void unpivot(ColumnMapping valueColumn, ColumnMapping pivotColumn, Collection<ColumnMapping> pivotColumns) {
        unpivot = """UNPIVOT 
		(
			${valueColumn.alias} for ${pivotColumn.alias} in ( 
				${pivotColumns.collect {it.alias}.join(",\n\t\t\t\t")})
		) unpvt
		"""
    }

    String getPivotClause() {
        pivot ?: ""
    }

    String getUnpivotClause() {
        unpivot ?: ""
    }

}
