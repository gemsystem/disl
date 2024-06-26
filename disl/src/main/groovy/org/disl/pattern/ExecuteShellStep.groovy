/*
 * Copyright 2015 - 2017 Karel Hübl <karel.huebl@gmail.com>.
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
package org.disl.pattern

/**
 * Execute shell command.
 */
abstract class ExecuteShellStep extends Step {

    File workingDirectory=new File(System.properties.'user.dir')
    String processOutput



    @Override
    protected int executeInternal() {
        Process process = createProcess(getCode())
        processOutput=process.text
        process.waitFor()
        if (!isIgnoreErrors() && process.exitValue() != 0) {
            throw new RuntimeException("""\
Shell command exited with retun code ${process.exitValue()}.

${processOutput}""")
        }
        return process.exitValue()
    }

    protected Process createProcess(String command) {
        new ProcessBuilder(addShellPrefix(command))
                .directory(workingDirectory)
                .start()

    }

    protected String[] addShellPrefix(String command) {
        if (isWindows()) {
            return ['cmd','/c',command]
        } else {
            return ['sh','-c',command]
        }
    }


    boolean isWindows() {
        System.getProperty('os.name').toLowerCase().contains('windows')
    }
}
