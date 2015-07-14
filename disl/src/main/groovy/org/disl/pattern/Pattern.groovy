/*
 * Copyright 2015 Karel H�bl <karel.huebl@gmail.com>.
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
package org.disl.pattern;

import org.disl.meta.Context



public abstract class Pattern extends AbstractExecutable {
	
	private Collection<Step> steps
	
	public Collection<Step> getSteps() {
		if (steps==null) {
			steps=createSteps().findAll {it.executionMode.equals(Context.getContext().getExecutionMode())}
		}
		return steps
	}
				
	abstract Collection<Step> createSteps()
	
	@Override
	public int executeInternal() {
		long timestamp=System.currentTimeMillis();
		println "Executing pattern $this:"
		int processedRows=0		
		getSteps().each {it.execute();processedRows+=it.executionInfo.processedRows}		
		println "${this} executed in ${System.currentTimeMillis()-timestamp} ms"
		return processedRows		
	}
	
	@Override
	public void simulate() {
		println "Simulating pattern $this:"
		getSteps().each {it.simulate()}		
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
