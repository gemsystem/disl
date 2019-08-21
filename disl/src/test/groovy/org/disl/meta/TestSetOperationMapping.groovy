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

import static org.junit.Assert.*

import org.disl.meta.TestMapping.TestingMapping
import org.disl.meta.TestMapping.FewColsMapping
import org.disl.test.DislTestCase
import org.junit.Before
import org.junit.Test

class TestSetOperationMapping extends DislTestCase {

	static class TestingSetOperationMapping extends Mapping {
		String schema="L2"

		TestingMapping subquery1
		FewColsMapping subquery2

		ColumnMapping A=e "$subquery1.A"
		ColumnMapping c_renamed=e "C"
		ColumnMapping B=e "$subquery1.B"
		ColumnMapping EXTRA= e"EXTRA"

		@Override
		public void initMapping() {
			from subquery1
			union subquery2
		}
	}

	TestingSetOperationMapping mapping=MetaFactory.create(TestingSetOperationMapping)
	
	@Test
	void testGetSQLQuery() {
		assertEquals("""\
	/*Mapping TestingSetOperationMapping*/
		SELECT
			subquery1.A as A,
			C as c_renamed,
			subquery1.B as B,
			EXTRA as EXTRA
		FROM
		(
		SELECT A,C,B,null X
		FROM
			(
	/*Mapping TestingMapping*/
		SELECT
			s1.A as A,
			C as c,
			REPEAT(s2.B,3) as B
		FROM
			PUBLIC.TESTING_TABLE s1
			INNER JOIN PUBLIC.TESTING_TABLE s2  ON (s1.A=s2.A)
			LEFT OUTER JOIN PUBLIC.TESTING_TABLE s3  ON (s2.A=s3.A)
			RIGHT OUTER JOIN PUBLIC.TESTING_TABLE s4  ON (s2.A=s4.A)
			FULL OUTER JOIN PUBLIC.TESTING_TABLE s5  ON (s2.A=s5.A)
			LEFT HASH JOIN PUBLIC.TESTING_TABLE s7  ON (s2.A=s7.A)
			INNER HASH JOIN PUBLIC.TESTING_TABLE s8  ON (s2.A=s8.A)
			CROSS JOIN PUBLIC.TESTING_TABLE s6
			CROSS APPLY Any XML Join Can Be Here
		WHERE
			s1.A=s1.A
		GROUP BY
			s1.A,C,REPEAT(s2.B,3)
		HAVING
			min(s1.A)='xxx'
	/*End of mapping TestingMapping*/) subquery1
		UNION select A,null C,null B,X from (
	/*Mapping FewColsMapping*/
		SELECT
			s1.A as a,
			2 as X
		FROM
			PUBLIC.TESTING_TABLE s1
		WHERE
			1=1
		
	/*End of mapping FewColsMapping*/) subquery2
		) subquery1
		WHERE
			1=1
		
	/*End of mapping TestingSetOperationMapping*/""", mapping.getSQLQuery())
	}

	@Test
	void testGetSetOperationClause() {
		assertEquals("""\
UNION select A,null C,null B,X from (
	/*Mapping FewColsMapping*/
		SELECT
			s1.A as a,
			2 as X
		FROM
			PUBLIC.TESTING_TABLE s1
		WHERE
			1=1
		
	/*End of mapping FewColsMapping*/) subquery2""",mapping.getSetOperationClause())
	}
}
