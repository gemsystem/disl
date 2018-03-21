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
import org.disl.meta.TestMappingMaterialized
import org.disl.pattern.generic.TruncateInsertPattern
import org.disl.test.DislTestCase
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TestJob extends DislTestCase {

	@Before
	void init() {
		Context.setContextName("disl-test")
		Context.getContext().setExecutionMode('testing')
	}

	@Test
	public void testTestingJobSimpleMappingNoDependency() {
		def job = new TestingJobSimpleMappingNoDependency()
		Assert.assertArrayEquals(job.jobEntries.collect {
			it.executable.class.simpleName
		}.toArray(), ["MappingMaterialized"].toArray())
	}

	@Test
	public void testTestingJobSimpleMappingDependency() {
		def job = new TestingJobSimpleMappingDependency()
		Assert.assertArrayEquals(job.jobEntries.collect {
			it.executable.class.simpleName
		}.toArray(), ["MappingMaterialized"].toArray())
	}


	@Test
	public void testTestingJobWithStgMappingNoDependency() {
		def job = new TestingJobWithStgMappingNoDependency()
		Assert.assertArrayEquals(job.jobEntries.collect {
			it.executable.class.simpleName
		}.toArray(), ["MappingIntoTable"].toArray())
	}

	@Test
	public void testTestingJobWithStgMappingDependency() {
		def job = new TestingJobWithStgMappingDependency()
		Assert.assertArrayEquals(job.jobEntries.collect {
			it.executable.class.simpleName
		}.toArray(), ["MappingMaterialized", "MappingIntoTable"].toArray())
	}

	@Test
	public void testTestingJobWithStgMappingDependencyPrune() {
		def job = new TestingJobWithStgMappingDependencyPrune()
		Assert.assertArrayEquals(job.jobEntries.collect {
			it.executable.class.simpleName
		}.toArray(), ["MappingMaterialized", "MappingIntoTable"].toArray())
		job.executionInfo.start()
		job.executionInfo.finish()
		job.executionInfo.endTime = job.executionInfo.startTime + 100000
		println job.getExecutionSummaryMessage()
	}

	@Test
	void testPrintingExecutionSummaryMessage() {
		def job = new TestingJobWithStgMappingDependencyPrune()
		job.executionInfo.start()
		job.executionInfo.finish()
		job.executionInfo.endTime = job.executionInfo.startTime + (1000L*60*60*26+23)
		println job.getExecutionSummaryMessage()
	}

	static class TestingJobSimpleMappingNoDependency extends Job {
		TestingJobSimpleMappingNoDependency() {
			def map = MetaFactory.create(TestMappingMaterialized.MappingMaterialized)
			autoAddDependencies = false
			addAll([map])
		}
	}


	static class TestingJobSimpleMappingDependency extends Job {
		TestingJobSimpleMappingDependency() {
			def map = MetaFactory.create(TestMappingMaterialized.MappingMaterialized)
			autoAddDependencies = true
			addAll([map])
		}
	}

	static class TestingJobWithStgMappingNoDependency extends Job {
		TestingJobWithStgMappingNoDependency() {
			def map = MetaFactory.create(TestMappingMaterialized.MappingIntoTable)
			autoAddDependencies = false
			addAll([map])
		}
	}

	static class TestingJobWithStgMappingDependency extends Job {
		TestingJobWithStgMappingDependency() {
			def map = MetaFactory.create(TestMappingMaterialized.MappingIntoTable)
			autoAddDependencies = true
			addAll([map])
		}
	}

	static class TestingJobWithStgMappingDependencyPrune extends Job {
		def tab1 = MetaFactory.create(TestMappingMaterialized.MappingIntoTable)
		def tab2 = MetaFactory.create(TestDependency.Tab2)
		Dependency dependency = new Dependency([tab1, tab2]).prune([TestMappingMaterialized.MappingIntoTable])

		TestingJobWithStgMappingDependencyPrune() {
			autoAddDependencies = true
			addAll(dependency.getMembers())
		}
	}

}