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
 * Helper for building model for vis.js network visualisation Jenkins job dependencies. See http://visjs.org/docs/network/.
 *
 * @author Lukáš Vlk
 */
class JenkinsNetwork {

    private int maxDepth = 50

    Set<JenkinsNode> nodes = new HashSet<>()
    List<Edge> edges = []

    JenkinsNetwork(List<JenkinsVo> vos) {
        vos.each {vo ->
            vo.triggers.withIndex().each {trigger, idx ->
                def triggerId = "${vo.name}-TR$idx"
                nodes.add new JenkinsNode(triggerId, trigger)
                edges.add(JenkinsEdge.edge(triggerId, vo.name))
            }
            walk(vo, 1)
        }
    }

    boolean walk(JenkinsVo vo, int depth) {
        if (depth > maxDepth) {
            return false
        }

        def retval = true

        def node = new JenkinsNode(vo, depth).tap {
            customization(vo, it)
        }
        retval = nodes.add(node)

        vo.downstreamProjectsVos.each {
            edges.add(JenkinsEdge.edge(vo, it))
            walk(it, depth + 1)
        }

        return retval
    }

    void customization(JenkinsVo vo, JenkinsNode node) {
        node.shape = "box"
        if (node.level == 1) {
            node.color = "red"
        } else {
            node.color = "orange"
        }

    }

}
