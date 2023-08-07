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

import org.disl.util.wiki.CollatorHandler

import java.nio.file.Path

/**
 * VO with data about Excel macros.
 *
 * @author Lukáš Vlk
 */
class MacroVo implements Comparable<MacroVo> {

    Path macroFilePath

    boolean directory
    String macroFilename

    Path mdFilePath
    String mdFilename
    String mdRelativeUrl

    List<Tuple2<String, String>> codeTuple

    MacroVo(Path filePath) {
        this.macroFilePath = filePath
        this.directory = macroFilePath.toFile().isDirectory()
    }

    String getMdRelativeUrl(Path contentDirPath, Path mdFilePath) {
        def contentFile = contentDirPath.toFile()
        def mdFile = mdFilePath.toFile()

        def pathStr = contentFile.relativePath(mdFile)
        return "/" + pathStr.substring(0, pathStr.length() - 3)
    }

    @Override
    int compareTo(MacroVo other) {
        return Objects.compare(this.macroFilePath.toString(), other.macroFilePath.toString(), CollatorHandler.collator)
    }
}
