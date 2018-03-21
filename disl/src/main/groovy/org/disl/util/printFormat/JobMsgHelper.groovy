/*
 * Copyright 2017 - 2018 Lukas Vlk.
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
package org.disl.util.printFormat

import org.disl.pattern.ExecutionInfo
import org.disl.workflow.JobEntry

/**
 * Job Message Creator.
 * @author Lukas Vlk
 */
class JobMsgHelper {

	String getDurationHour(ExecutionInfo executionInfo) {
		def dur = executionInfo.duration ?: 0
		def sec = (dur / 1000).longValue()
		def h = ((sec) / 3600).intValue()
		def m = ((sec % 3600) / 60).intValue()
		def s = ((sec % 60)).intValue()
		def ms = (dur % 1000)
		String.format("%02d:%02d:%02d.%03d", h, m, s, ms)
	}

	String getExecutionSummaryMessage(String jobName, ExecutionInfo executionInfo, List<JobEntry> jobEntries) {
		String name = jobName.padRight(50).toString().substring(0, 50)
		String dur = getDuration(executionInfo)
		String durH = getDurationHour(executionInfo)
		String stat = getStatus(executionInfo)
		String processedRows = getProcessedRows(executionInfo)
		return """ Execution results for ${name}:
************************************************************************************************************
* Name                                               *   Status   *  Time (ms) *     Time     *       Rows *
************************************************************************************************************
* ${name} * ${stat} * ${dur} * ${durH} * ${processedRows} *
************************************************************************************************************
${jobEntries.join('\n')}
************************************************************************************************************
"""
	}

	String getExecutionSummaryMessageEntry(String entryName, ExecutionInfo executionInfo) {
		String name = entryName.padRight(50).toString().substring(0, 50)
		String dur = getDuration(executionInfo)
		String durH = getDurationHour(executionInfo)
		String stat = getStatus(executionInfo)
		String processedRows = getProcessedRows(executionInfo)
		return "* ${name} * ${stat} * ${dur} * ${durH} * ${processedRows} *"
	}

	protected String getDuration(ExecutionInfo executionInfo) {
		executionInfo.duration.toString().padLeft(10).toString().substring(0, 10)
	}

	protected String getStatus(ExecutionInfo executionInfo) {
		executionInfo.status.toString().padLeft(10).toString().substring(0, 10)
	}

	protected String getProcessedRows(ExecutionInfo executionInfo) {
		executionInfo.processedRows.toString().padLeft(10).toString().substring(0, 10)
	}

}
