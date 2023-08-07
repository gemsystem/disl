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

import groovy.json.JsonBuilder
import org.disl.pattern.FileOutputStep
import org.disl.util.wiki.visjs.JenkinsNetwork

import java.nio.file.Path

/**
 * Create JSON file with Jenkins network.
 *
 * @author Lukáš Vlk
 */
class JenkinsGraphStep extends FileOutputStep {

    Path graphFilePath
    List<JenkinsVo> roots

    @Override
    File getFile() {
        graphFilePath.toFile()
    }

    @Override
    String getCode() {
        new JsonBuilder(new JenkinsNetwork(roots)).toPrettyString()
    }
}
