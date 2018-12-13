/*
 * Copyright 2015 - 2018 GEM System a.s. <sales@gemsystem.cz>.
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

import org.disl.pattern.generic.CreateOrReplaceTablePattern
import org.disl.test.DislTestCase
import org.junit.Before
import org.junit.Test

/**
 *
 * @author Lukáš Vlk
 */
class TestUIMapping extends DislTestCase {

	static class TestingTable extends Table {

		CreateOrReplaceTablePattern pattern

		@Description("Column A.")
		@DataType("VARCHAR(255)")
		@DefaultValue("'A'")
		@UIMapping(["Form1.A", "Form3.A"])
		Column A

		@PrimaryKey
		@DataType("VARCHAR(255)")
		@UIMapping("Form1.B")
		Column B

		@DataType("VARCHAR(255)")
		Column C

	}

	TestingTable table

	@Before
	void init() {
		Context.setContextName("disl-test")
		table = MetaFactory.create(TestingTable)
	}

	@Test
	void testGetColumns() {
		assert table.A.uiFields == ["Form1.A", "Form3.A"]
		assert table.B.uiFields == ["Form1.B"]
		assert table.C.uiFields == []
	}

}
