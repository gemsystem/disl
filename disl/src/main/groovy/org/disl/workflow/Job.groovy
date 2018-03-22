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

import org.disl.meta.Mapping
import org.disl.meta.MetaFactory
import org.disl.meta.TableMapping
import org.disl.pattern.AbstractExecutable
import org.disl.pattern.Executable

import groovy.transform.CompileStatic;
import groovy.util.logging.Slf4j
import org.disl.util.jenkins.JobMsgHelper

/**
 * Job executes list of job entries in serial order.
 * */
@Slf4j
@CompileStatic
abstract class Job extends AbstractExecutable {

	boolean autoAddDependencies = false
	List<JobEntry> jobEntries=[]

	/**
	 * Add executable instance to job entry list.
	 * */
	public void add(Executable executable) {
		if (autoAddDependencies && executable instanceof Mapping) {
			addMaterializedSubmappings(executable,true)
		}
		addExecutable(executable)
	}

	/**
	 * Add an executable to jobEntries list
	 */
	void addExecutable(Executable executable) {
		this.jobEntries.add(new JobEntry(executable: executable))
	}

	/**
	 * Check all dependencies and add missing TableMapping
	 */
	void addMaterializedSubmappings(Mapping executable,boolean firstCall=false) {
		executable.sources.each { recursivelyAdd(it) }
		executable.setOperations.each { recursivelyAdd(it.source) }
		if (!firstCall && executable instanceof TableMapping) {
			if (hasntBeenAdded(executable)) {
				addExecutable(executable)
			}
		}
	}

	/**
	 * Condition for recursion.
	 */
	void recursivelyAdd(Object mapping) {
		if (mapping instanceof Mapping) {
			addMaterializedSubmappings(mapping)
		}
	}

	/**
	 * Don't allow duplicates when auto adding.
	 */
	boolean hasntBeenAdded(Executable executable) {
		JobEntry jobEntry = jobEntries.find { it.executable.class.canonicalName.equals(executable.class.canonicalName) }
		return !jobEntry
	}

	/**
	 * Create new instance of Executable and add it to job entry list.
	 * */
	public Job addType(Class<? extends Executable> type) {
		add((Executable)MetaFactory.create(type))
		return this
	}

	/**
	 * Create list of Executable instances and add it to job entry list.
	 * */
	public Job addTypes(List<Class<? extends Executable>> types) {
		types.each({ addType(it) })
		return this
	}

	/**
	 * Add list of executables to job entry list.
	 * */

	public Job addAll(List<? extends Executable> executables) {
		executables.each { add(it) }
		return this
	}

	/**
	 * Find, create and add executables in job package and all subpackages to job entry list. 
	 * Compiled executables must be located in the same classpath element (directory or jar).
	 * @param assignableType Only classes assignable from assignableType will be added to job entry list.
	 * */
	public Job addAll(Class assignableType) {
		addAll(MetaFactory.createAll(this.getClass().getPackage().getName(),assignableType));
	}

	/**
	 * Find, create and add executables to job entry list. 
	 * Compiled executables must be located in the same classpath element (directory or jar). 
	 * @param rootPackage Root package to look for executables classes in.
	 * @param assignableType Only classes assignable from assignableType will be added to job entry list.
	 * */
	public Job addAll(String rootPackage,Class assignableType) {
		addAll(MetaFactory.createAll(rootPackage,assignableType));
	}

	protected int executeInternal() {
		int processedRows=0
		jobEntries.each {
			it.execute(); processedRows+=it.executionInfo.processedRows
		}
		return processedRows
	}

	@Override
	public void postExecute() {
		super.postExecute();
		traceStatus()
	}

	public void simulate() {
		jobEntries.each { it.simulate() }
	}

	@Override
	public String toString() {
		return getClass().getSimpleName()
	}

	
	public void traceStatus() {
		if (log.infoEnabled) {
				log.info(getExecutionSummaryMessage())
		}
	}

	public String getExecutionSummaryMessage() {
		JobMsgHelper jobMsgFormat = new JobMsgHelper()
		return jobMsgFormat.getExecutionSummaryMessage(name.toString(), executionInfo, jobEntries)
	}
}
