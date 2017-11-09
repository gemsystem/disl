/*
 * Copyright 2015 - 2017 Antonin Krotky <antoninkrotky@gmail.com>.
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

import org.disl.meta.TestTable.TESTING_TABLE
import org.disl.pattern.generic.TruncateInsertPattern
import org.disl.test.DislTestCase
import org.junit.Test
import static org.junit.Assert.assertEquals


class TestMappingMaterialized extends DislTestCase {

	@Test
	void testMappingIntoTable() {
		MappingIntoTable mapping=MetaFactory.create(MappingIntoTable)
		assertEquals ("""\t/*Mapping MappingIntoTable*/
\t\tSELECT
\t\t\t1 as A,
\t\t\t'2' as B
\t\tFROM
\t\t\tPUBLIC.TESTING_TABLE src1
\t\t\tINNER JOIN db.schema.MappingMaterialized_PKG_org_disl_meta src2  ON (1=1)
\t\tWHERE
\t\t\t1=1
\t\t
\t/*End of mapping MappingIntoTable*/""".toString(),mapping.getSQLQuery())
	}

	@Test
	void testMappingMaterialized() {
		MappingMaterialized mapping=MetaFactory.create(MappingMaterialized)
		assertEquals ("""\t/*Mapping MappingMaterialized*/
\t\tSELECT
\t\t\t3 as A,
\t\t\t'4' as B
\t\tFROM
\t\t\tPUBLIC.TESTING_TABLE tab1
\t\tWHERE
\t\t\t1=1
\t\t
\t/*End of mapping MappingMaterialized*/""".toString(),mapping.getSQLQuery())
	}

	static class MappingIntoTable extends TableMapping {
		TruncateInsertPattern pattern

		TESTING_TABLE target

		TESTING_TABLE src1
		MappingMaterialized src2

		ColumnMapping A=e 1
		ColumnMapping B=e "'2'"

		@Override
		public void initMapping() {
			from src1
			innerJoin src2 on "1=1"
		}
	}

	static class MappingMaterialized extends TableMappingMaterialized {
		TruncateInsertPattern pattern
		@Override
		String getFullName() {
			"""db.schema.${this.class.simpleName}_PKG_${this.class.package.name.replaceAll("\\.","_")}"""
		}

		TESTING_TABLE tab1

		ColumnMapping A=e 3
		ColumnMapping B=e "'4'"

		@Override
		void initMapping() {
			from tab1
		}
	}
}