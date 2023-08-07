/*
 * Copyright 2015 - 2023 GEM System a.s. <disl@gemsystem.cz>.
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
package org.disl.util.wiki.msvba

import groovy.json.JsonSlurper

import java.nio.file.Path

/**
 * Caller olevba process.
 *
 * @author Lukáš Vlk
 */
class MacroAnalyzer {

    String prg
    List<String> args
    long timeout = 30000L


    String exec(Path filePath) {
        def filename = filePath.toAbsolutePath().toString()
        def sout = new ByteArrayOutputStream()
        def serr = new ByteArrayOutputStream()

        def proc = new ProcessBuilder([prg, args, filename].flatten())
                .start()
        proc.consumeProcessOutput(sout, serr)
        proc.waitForOrKill(timeout)
        proc.destroy()
        return sout.toString()
    }

    List<Tuple2<String, String>> parseMacros(String json) {
        def vba = new JsonSlurper().parseText(json)
        return vba.findAll {
            it.macros != null
        }.collectMany {
            it.macros.collect {m ->
                new Tuple2(m.ole_stream, m.code)
            }
        }.findAll {pair ->
            !pair.v2.isEmpty()
        }
    }
}
