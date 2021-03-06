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
package org.disl.workflow

import org.disl.meta.Context
import org.disl.meta.MetaFactory
import org.disl.meta.TableMapping
import org.disl.pattern.generic.TruncateInsertPattern
import org.disl.test.DislTestCase
import org.junit.Before
import org.junit.Test

class TestParallelJobReflectingDependencies extends DislTestCase {
	
	@Before
    void init() {
		Context.setContextName("disl-test")
		Context.getContext().setExecutionMode('testing')
	}
	
	@Test
	public void testExecute() {
		int jobSimulateSleepTime = 3000
		def job = new TestingJob(
				ignoreDependencyTypes: [],
				onlyDependencyTypes: [],
				intervalToEvaluateConditions: 1000,
				jobSimulateSleepTime: jobSimulateSleepTime,
				fileName: null,
				openBrowser: false,
				parallelExecutorThreads: 0 //use default value from property file
		)
		int startTime = System.currentTimeMillis()
		job.execute()
		int finishTime = System.currentTimeMillis()
		assert (finishTime-startTime) > (jobSimulateSleepTime*2)
	}

	static class TestingJob extends ParallelJobReflectingDependencies {
		def mapA = MetaFactory.create(TestingMappingA)
		def mapB = MetaFactory.create(TestingMappingB)
		TestingJob() {
			addAll([mapA,mapB])
		}
	}

	static class TestingMappingA extends TableMapping {
		TruncateInsertPattern pattern

		TestDependency.Tab1 target

		void initMapping() {
		}
	}

	static class TestingMappingB extends TableMapping {
		TruncateInsertPattern pattern

		TestDependency.Tab1 target

		TestingMappingA mA

		void initMapping() {
			from mA
		}
	}

}
