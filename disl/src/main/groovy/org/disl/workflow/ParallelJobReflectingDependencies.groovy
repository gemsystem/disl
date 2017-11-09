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
/**
 * ParallelJob executes executables in parallel.
 * */
abstract class ParallelJobReflectingDependencies extends Job {

    List<String> ignoreDependencyTypes = []
    List<String> onlyDependencyTypes = []
    int intervalToEvaluateConditions = 5000
    int jobSimulateSleepTime = 0
    String fileName = null
    boolean openBrowser = false
    int parallelExecutorThreads = 0 //use parallelExecutorThreads parameter from property file when 0

    @Override
    public int executeInternal() {
        def executor = ParallelJobExecutorReflectingDependencies.instance
                .setIgnoreDependencyTypes(ignoreDependencyTypes)
                .setOnlyDependencyTypes(onlyDependencyTypes)
                .setIntervalToEvaluateConditions(intervalToEvaluateConditions)
                .setJobSimulateSleepTime(jobSimulateSleepTime)
                .setFileName(fileName)
                .setOpenBrowser(openBrowser)
                .setParallelExecutorThreads(parallelExecutorThreads)
                .execute(this)
        int processedRows = 0
        jobEntries.each { processedRows += it.executionInfo.processedRows }
        return processedRows
    }

}
