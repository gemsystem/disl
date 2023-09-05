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
package org.disl.util.wiki.jenkins

import org.disl.pattern.FileOutputStep

/**
 * Create markdown file for Jenkins Job.
 *
 * @author Lukáš Vlk
 */
class JenkinsMdFileStep extends FileOutputStep {

    JenkinsVo vo

    @Override
    File getFile() {
        vo.mdFilePath.toFile()
    }

    @Override
    String getCharset() {
        'utf-8'
    }

    @Override
    String getCode() {
        """+++
title = "${vo.name}"
tags = ["Jenkins"]
+++

**Description**: ${vo.description}

**[Open Jenkins Job](${vo.url})**

**Triggers**:
${triggers}

**Upstream projects**:
${upstreamProjects}

**Downstream projects**:
${downstreamProjects}

**Commands**:
${commands}

"""
    }

    protected String getUpstreamProjects() {
        vo.upstreamProjectsVos.toSorted {a, b ->
            a.name <=> b.name
        }.collect {
            "* [${it.name}](${it.mdRelativeUrl})"
        }.join("\n")
    }

    protected String getDownstreamProjects() {
        vo.downstreamProjectsVos.toSorted {a, b ->
            a.name <=> b.name
        }.collect {
            "* [${it.name}](${it.mdRelativeUrl})"
        }.join("\n")
    }

    protected String getCommands() {
        vo.commands.collect {
            "* ${it.type == "batchfile" ? "" : it.type} ${it.command}"
        }.join("\n")
    }

    protected String getTriggers() {
        vo.triggers.collect {
            """* [${it}](. "Min Hour DayOfTheMonth Month DayOfTheWeek")"""
        }.join("\n")
    }
}
