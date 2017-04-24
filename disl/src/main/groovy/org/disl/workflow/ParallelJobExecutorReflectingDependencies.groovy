/*
 * Copyright 2015 - 2017 GEM System a.s. <sales@gemsystem.cz>.
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

import groovy.util.logging.Slf4j

import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Created by akrotky on 5.4.2017.
 */
@Slf4j
@Singleton(lazy=true,strict=false)
class ParallelJobExecutorReflectingDependencies {
    ParallelJobExecutorReflectingDependenciesWorker parallelJobExecutorWorker = new ParallelJobExecutorReflectingDependenciesWorker()

    /**
     * Execute Job's jobEntries in parallel.
     * */
    void execute(Job job) {
        parallelJobExecutorWorker.execute(job)
    }

    void setIgnoreDependencyTypes(List<String> ignoreDependencyTypes){
        parallelJobExecutorWorker.ignoreDependencyTypes=ignoreDependencyTypes
    }

    void setOnlyDependencyTypes(List<String> onlyDependencyTypes){
        parallelJobExecutorWorker.onlyDependencyTypes=onlyDependencyTypes
    }

    void setIntervalToEvaluateConditions(int intervalToEvaluateConditions) {
        parallelJobExecutorWorker.intervalToEvaluateConditions=intervalToEvaluateConditions
    }

    Dependency getDependency() {
        return parallelJobExecutorWorker.dependency
    }

    void setJobSleepTime(int jobSleepTime) {
        parallelJobExecutorWorker.jobSleepTime=jobSleepTime
    }

    @Slf4j
    static class ParallelJobExecutorReflectingDependenciesWorker extends ParallelJobExecutor.ParallelJobExecutorWorker {
        public int intervalToEvaluateConditions = 5000
        Dependency dependency
        List<String> ignoreDependencyTypes = ["FK"]
        List<String> onlyDependencyTypes = []
        int jobSleepTime = 0 //used during testing to make jobs last longer

        HashMap<JobEntry, Future> nonParallelTasks = new HashMap<>()

        Future getFuture(JobEntry jobEntry) {
            return nonParallelTasks[jobEntry]
        }

        boolean isFinished(JobEntry jobEntry) {
            if (getFuture(jobEntry) == null) {
                return false
            }
            try {
                getFuture(jobEntry).get(1, TimeUnit.MILLISECONDS)
                return true
            } catch (TimeoutException e) {
                return false
            }
        }

        protected HashMap<JobEntry, Future> getNotStartedTasks() {
            return this.nonParallelTasks.findAll { jobEntry, future -> future == null }
        }

        protected HashMap<JobEntry, Future> getStartedTasks() {
            return this.nonParallelTasks.findAll { jobEntry, future -> future != null }
        }

        protected HashMap<JobEntry, Future> getRunningTasks() {
            getStartedTasks().findAll { jobEntry, future -> !isFinished(jobEntry) }
        }

        protected HashMap<JobEntry, Future> getFinishedTasks() {
            getStartedTasks().findAll { jobEntry, future -> isFinished(jobEntry) }
        }

        protected HashMap<JobEntry, Future> getNotFinishedTasks() {
            this.nonParallelTasks.findAll { jobEntry, future -> future == null || !isFinished(jobEntry) }
        }

        protected setDependencyNodeColors() {
            getNotStartedTasks().each{jobEntry,future ->
                this.dependency.objects.get(jobEntry.executable).backgroundColor='grey'
                this.dependency.objects.get(jobEntry.executable).title='Not started yet'
            }
            getStartedTasks().each{jobEntry,future ->
                this.dependency.objects[jobEntry.executable].backgroundColor='gold'
                this.dependency.objects[jobEntry.executable].title='Started, waiting for avaiable thread'
            }
            getRunningTasks().each{jobEntry,future ->
                this.dependency.objects.get(jobEntry.executable).backgroundColor='red'
                this.dependency.objects.get(jobEntry.executable).title='Running'
            }
            getFinishedTasks().each{jobEntry,future ->
                this.dependency.objects.get(jobEntry.executable).backgroundColor='green'
                this.dependency.objects.get(jobEntry.executable).title='Finished'
            }
        }

        protected createChartTmpFile() {
            new DependencyDrawChart(this.dependency).createFile()
        }

        protected void addTasks(Job job) {
            job.jobEntries.findAll({ !isParallelJobEntry(it) }).each { nonParallelTasks[it] = null }
            dependency = new Dependency(nonParallelTasks.collect{key,value->key.executable})
            new DependencySetProperties(dependency).setGraphicalStyle()
        }

        protected submitTasks() {
            getNotStartedTasks().each { jobEntry, future -> submitTaskIfYouCan((JobEntry) jobEntry) }
        }

        protected submitTaskIfYouCan(JobEntry jobEntryToRun) {
            boolean canRun = true
            log.debug("*I want to run ${jobEntryToRun.executable.class.simpleName}")
            getNotFinishedTasks().findAll { jobEntry, future -> !jobEntry.executable.class.canonicalName.equals(jobEntryToRun.executable.class.canonicalName) }.each { jobEntry, future ->
                boolean isDependant=isDependant(jobEntryToRun.executable, jobEntry.executable)
                log.debug("..testing against ${jobEntry.executable} with result isDependant: $isDependant")
                if (isDependant) {
                    canRun = false
                }
            }
            log.debug("*Result ${jobEntryToRun.executable.class.simpleName} canRun: $canRun")
            if (canRun) {
                this.nonParallelTasks[jobEntryToRun] = submitTask(jobEntryToRun)
            }
        }

        protected boolean isDependant(Object theObj, Object onObj) {
            return dependency.isDependant(theObj,onObj,ignoreDependencyTypes,onlyDependencyTypes)
        }

        protected Future submitTask(JobEntry jobEntry) {
            def service = getExecutorService()
            return service.submit(createCallable(jobEntry,jobSleepTime))
        }

        @Override
        void execute(Job job) {
            List<Future> parallelFutures = submitParallelJobTasks(job.getJobEntries())
            addTasks(job)

            while (getNotStartedTasks().size() > 0 || getRunningTasks().size() > 0) {
                submitTasks()
                setDependencyNodeColors()
                createChartTmpFile()
                sleep(intervalToEvaluateConditions)
            }
            try {
                parallelFutures.each { it.get() }
                nonParallelTasks.each { jobEntry, future -> future.get() }
            } catch (Exception e) {
                throw new RuntimeException("Exception in asynchronous execution.", e)
            } finally {
                releaseParallelJob()
            }
        }


    }

}