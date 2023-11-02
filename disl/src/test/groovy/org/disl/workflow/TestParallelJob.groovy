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
package org.disl.workflow

import groovy.sql.Sql;

import org.disl.meta.Context
import org.disl.pattern.AbstractExecutable
import org.disl.pattern.ExecuteSQLQueryStep;
import org.disl.pattern.ExecuteSQLScriptStep;
import org.disl.test.DislTestCase
import org.junit.Before
import org.junit.Test

class TestParallelJob extends DislTestCase {
	
	@Before
    void init() {
		Context.setContextName("disl-test")
		Context.getContext().setExecutionMode('testing')
	}
	
	@Test
	public void testExecute() {
		new ParallelJob(){}.addAll([new TestingJob(),
				new TestingJob(),
				new TestingJob(),
				new TestingJob(),
				new TestingJob(),
				new TestingExecutable(),
				new TestingExecutable()
			]).execute()
	}
	
	@Test
	public void testExecuteTestingExecutable() {
		new TestingExecutable().execute()
	}
	
	static class TestingJob extends ParallelJob {
		TestingJob() {
			addAll([new TestingExecutable()])
		}
	}

	static class TestingExecutable extends  ExecuteSQLQueryStep {
		
		@Override
		public Sql getSql() {
			return Context.getContext().getSql('default');
		}
		
		@Override
		public String getCode() {
			'select * from DUAL'
		}
		
		int executeInternal() {			
			assert Context.getContext().getExecutionMode().equals('testing')
			super.executeInternal()
			Thread.sleep(500)
			return 0
		}
		
		void simulate(){}
	}

}
