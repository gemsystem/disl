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
package org.disl.util.wiki.visjs

import org.disl.util.wiki.jenkins.JenkinsVo

/**
 * Representation of vis.js network Jenkins Edge.
 *
 * @author Lukáš Vlk
 */
class JenkinsEdge {

    static Edge edge(JenkinsVo src, JenkinsVo target) {
        edge(src.name, target.name)
    }

    static Edge edge(String src, String target) {
        new Edge(
            from: src,
            to: target,
            label: null,
            title: null
        )
    }

}
