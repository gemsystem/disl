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

import static org.disl.meta.TestLibrary.*
import static org.junit.Assert.*

import org.disl.meta.TestTable.TESTING_TABLE
import org.disl.meta.TestTable.TESTING_TABLE_XML
import org.disl.test.DislTestCase
import org.junit.Test


class TestMapping extends DislTestCase {
	@Description('Testing mapping.')
	static class TestingMapping extends Mapping {

		TESTING_TABLE s1
		TESTING_TABLE s2
		TESTING_TABLE s3
		TESTING_TABLE s4
		TESTING_TABLE s5
		TESTING_TABLE s6
		TESTING_TABLE s7
		TESTING_TABLE s8
		TESTING_TABLE_XML s9
		TESTING_TABLE s10

		ColumnMapping A=e s1.A
		ColumnMapping c=e "C"
		ColumnMapping B=e repeat(s2.B,3)
		
		SqlExpression CONSTANT=constant 1

		void initMapping() {
			from s1
			innerJoin s2 on (s1.A,s2.A)
			leftOuterJoin s3 on "$s2.A=$s3.A"
			rightOuterJoin s4 on "$s2.A=$s4.A"
			fullOuterJoin s5 on "$s2.A=$s5.A"
			leftHashJoin s7 on "$s2.A=$s7.A"
			innerHashJoin s8 on "$s2.A=$s8.A"
			fullHashJoin s10 on "$s2.A=$s10.A"
			cartesianJoin s6
			crossApply s9
			where "$s1.A=$s1.A"
			groupBy()
			having "min($s1.A)='xxx'"
		}
		
	}

	static class EmptyMapping extends Mapping {
		TESTING_TABLE s1

		@Override
		void initMapping() {
			from s1
		}
	}

	static class FewColsMapping extends Mapping {
		TESTING_TABLE s1

		ColumnMapping a=e s1.A
		ColumnMapping X=e "2"

		@Override
		void initMapping() {
			from s1
		}
	}

	@Test
	void testGetImplicitMapping() {
		EmptyMapping m=MetaFactory.create(EmptyMapping)
		m.traceInitialColumnMapping()
	}
	

	@Test
	void testGetSQLQuery() {
		TestingMapping mapping=MetaFactory.create(TestingMapping)
		assertEquals ("""	/*Mapping TestingMapping*/
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
			FULL HASH JOIN PUBLIC.TESTING_TABLE s10 ON (s2.A=s10.A)
			CROSS JOIN PUBLIC.TESTING_TABLE s6
			CROSS APPLY Any XML Join Can Be Here
		WHERE
			s1.A=s1.A
		GROUP BY
			s1.A,C,REPEAT(s2.B,3)
		HAVING
			min(s1.A)='xxx'
	/*End of mapping TestingMapping*/""".toString(),mapping.getSQLQuery())
	}
	
	@Test
	void testConstant() {
		TestingMapping mapping=MetaFactory.create(TestingMapping)
		assertEquals("1",(String)mapping.CONSTANT)
		assertEquals("1+1",(String)"${mapping.CONSTANT}+1")
	}

	@Test
	void testDescrption() {
		TestingMapping mapping=MetaFactory.create(TestingMapping)
		assertEquals('Testing mapping.',mapping.description)

	}
	
	@Test
	void testGroupBy() {
		TestingMapping mapping=MetaFactory.create(TestingMapping)
		String value="value"
		mapping.groupBy value
		assertEquals(value,mapping.getGroupBy())
		mapping.groupBy value,value
		assertEquals("value,value",mapping.getGroupBy())
		mapping.groupBy()
		assertEquals("s1.A,C,REPEAT(s2.B,3)",mapping.getGroupBy())
		
	}
}
