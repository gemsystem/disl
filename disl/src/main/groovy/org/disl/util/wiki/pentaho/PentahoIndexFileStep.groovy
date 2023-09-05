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
package org.disl.util.wiki.pentaho

import org.disl.pattern.FileOutputStep

import java.nio.file.Path

/**
 * Create markdown file for Pentaho index.
 *
 * @author Lukáš Vlk
 */
class PentahoIndexFileStep extends FileOutputStep {

    Path indexFilePath
    TreeSet<PentahoVo> directoryTree
    Path pentahoDirPath

    @Override
    File getFile() {
        return indexFilePath.toFile()
    }

    @Override
    String getCharset() {
        'utf-8'
    }

    @Override
    String getCode() {
        return """+++
title = "List of Pentaho Jobs"
tags = ["Pentaho"]
+++

$body
"""
    }

    String getBody() {
        StringBuilder sb = new StringBuilder()
        int prefixCount = pentahoDirPath.getNameCount()
        directoryTree.each {vo ->
            def level = vo.pentahoFilePath.getNameCount() - prefixCount - 2
            int padCount = level * 4
            String pad = "".padLeft(padCount, " ")
            if (vo.isDirectory()) {
                if (level < 0) {
                    sb.append("## ${vo.pentahoFilename}\n")
                } else {
                    sb.append("$pad* **${vo.pentahoFilename}**\n")
                }
            } else if (vo instanceof PentahoJobVo) {
                def relativeDir = vo.mdRelativeUrl
                sb.append("$pad* [${vo.name}](${relativeDir})\n")
            }
        }
        return sb.toString()
    }
}
