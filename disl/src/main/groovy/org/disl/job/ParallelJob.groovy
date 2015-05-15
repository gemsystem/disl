package org.disl.job

import org.disl.pattern.Executable
import org.junit.Test

class ParallelJob extends Job {

	int threadCount=4

	@Override
	public void execute() {
		JobThreadPool.instance.execute(this)
	}

	@Test
	public void testExecute() {
			
		addAll([new TestingJob(),
				new TestingJob(),
				new TestingJob(),
				new TestingJob(),
				new TestingJob(),
				new TestingExecutable(),
				new TestingExecutable()
			])
		execute()
	}

	static class TestingJob extends ParallelJob {
		TestingJob() {
			addAll([new TestingExecutable()])
		}
	}

	static class TestingExecutable implements Executable {
		void execute() {
			Thread.sleep(500)
			println "executable done"
		}
		void simulate(){}
	}
}