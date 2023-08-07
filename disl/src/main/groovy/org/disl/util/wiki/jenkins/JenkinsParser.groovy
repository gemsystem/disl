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

import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult

import java.nio.file.Path

/**
 * Parser for jenkins config file.
 *
 * @author Lukáš Vlk
 */
class JenkinsParser {

    JenkinsVo parse(Path path) {
        def project = new XmlSlurper().parse(path)
        new JenkinsVo(
            jenkinsFilePath: path,
            name: parseJobName(path),
            description: project.description,
            disabled: parseDisabled(project),
            triggers: parseTriggers(project),
            upstreamProjects: parseUpstreamProjects(project),
            commands: parseCommands(project)
        )
    }

    private String parseJobName(Path path) {
        path?.parent?.getFileName()?.toString()
    }

    private boolean parseDisabled(GPathResult project) {
        project.disabled != "false"
    }

    private List<String> parseUpstreamProjects(GPathResult project) {
        project.triggers."jenkins.triggers.ReverseBuildTrigger".upstreamProjects?.toString()?.split(",")?.collect {
            it.trim()
        }?.findAll {
            it != null && !it.isEmpty()
        }
    }

    private List<String> parseTriggers(GPathResult project) {
        project.triggers."hudson.triggers.TimerTrigger".collect {
            it.spec.toString()
        }
    }

    private List<JenkinsCommandVo> parseCommands(GPathResult project) {
        return project.builders."hudson.tasks.BatchFile".collect {
            parseCommandBatch(it)
        } +
        project.builders."hudson.plugins.gradle.Gradle".collect {
            parseCommandGradle(it)
        } +
        project.builders."hudson.tasks.Ant".collect {
            parseCommandAnt(it)
        }
    }

    private JenkinsCommandVo parseCommandBatch(GPathResult batch) {
        new JenkinsCommandVo(
            type: "batchfile",
            command: batch.command.toString()
        )
    }

    private JenkinsCommandVo parseCommandGradle(GPathResult gradle) {
        StringBuilder sb = new StringBuilder()
        if (!gradle.buildFile.isEmpty()) {
            sb.append "--build-file ${gradle.buildFile} "
        }
        sb.append "${gradle.switches} "
        sb.append "${gradle.tasks} "

        new JenkinsCommandVo(
            type: "gradle",
            command: sb.toString().trim()
        )

    }

    private JenkinsCommandVo parseCommandAnt(GPathResult ant) {
        new JenkinsCommandVo(
            type: "ant",
            command: ant.targets.toString()
        )
    }
}
