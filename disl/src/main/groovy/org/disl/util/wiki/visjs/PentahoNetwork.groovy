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

import org.disl.util.wiki.pentaho.PentahoEntryVo
import org.disl.util.wiki.pentaho.PentahoFileVo
import org.disl.util.wiki.pentaho.PentahoJobVo
import org.disl.util.wiki.pentaho.PentahoTransformVo

/**
 * Helper for building Pentaho model for vis.js network visualisation. See http://visjs.org/docs/network/.
 *
 * @author Lukáš Vlk
 */
class PentahoNetwork {

    private int maxDepth = 50

    Set<PentahoNode> nodes = new HashSet<>()
    List<Edge> edges = []

    PentahoNetwork(PentahoFileVo vo) {
        walk(vo, null, 1)
    }

    boolean walk(PentahoTransformVo vo, PentahoNode node, int depth) {
        if (depth > maxDepth) {
            return false
        }

        def retval = true

        if (depth == 1) {
            node = new PentahoNode(vo, depth).tap {
                customization(vo, it)
            }
            retval = nodes.add(node)

            nodes.addAll vo.entryVos.collect {entry ->
                new PentahoNode(entry, -1).tap {
                    customization(entry, it)
                }
            }

            def toHops = vo.hopVos.collect {hop ->
                hop.to
            }
            vo.entryVos.findAll {entry ->
                !toHops.contains(entry.name)
            }.each {entry ->
                edges.add(PentahoEdge.edge(vo, entry))
                innerWalking(vo, entry, depth + 1)
            }
        } else {
            processNextLevel(vo, node, depth)
        }

        return retval
    }

    boolean walk(PentahoJobVo vo, PentahoNode node,  int depth) {
        if (depth > maxDepth) {
            return false
        }

        def retval = true

        if (depth == 1) {
            node = new PentahoNode(vo, depth).tap {
                customization(vo, it)
            }
            retval = nodes.add(node)

            nodes.addAll vo.entryVos.collect {entry ->
                new PentahoNode(entry, -1).tap {
                    customization(entry, it)
                }
            }

            def start = vo.entryVos.find {
                it.type == "SPECIAL" && it.name.toLowerCase() == "start"
            }

            edges.add(PentahoEdge.edge(vo, start))
            innerWalking(vo, start, depth + 1)
        } else {
            processNextLevel(vo, node, depth)
        }

        return retval
    }

    protected void processNextLevel(PentahoFileVo vo, node, int depth) {
        vo.jobVos.each { j ->
            def n2 = new PentahoNode(j, depth).tap {
                customization(j, it)
            }
            nodes.add n2
            edges.add(PentahoEdge.edge(node.id, j.graphId))
            walk(j, n2, depth + 1)
        }

        vo.transformVos.each { t ->
            def n2 = new PentahoNode(t, depth).tap {
                customization(t, it)
            }
            nodes.add n2
            edges.add(PentahoEdge.edge(node.id, t.graphId))
            walk(t, n2, depth + 1)
        }
    }

    boolean innerWalking(PentahoFileVo vo, PentahoEntryVo start, int depth) {
        def node = nodes.find {
            it.id == start.name
        }
        if (node.level > 0) {
            // Již jsme zpracovali
            return true
        }
        node.level = depth

        def usedHops = vo.hopVos.findAll { hop ->
            hop.from == node.id
        }.each { hop ->
            def n2 = nodes.find { n ->
                n.id == hop.to
            }
            edges.add(PentahoEdge.edge(hop))
            def entry2 = vo.entryVos.find { entry ->
                entry.name == n2.id
            }
            PentahoFileVo vo2 = null
            if (entry2.type == "JOB" || entry2.type == "JobExecutor") {
                if (vo instanceof PentahoJobVo) {
                    vo2 = vo.jobVos.find {
                        entry2.extName == it.graphId
                    }
                } else if (vo instanceof PentahoTransformVo) {
                    vo2 = vo.jobVos.find {
                        entry2.extName == it.graphId
                    }
                } else {
                    // do nothing
                }
            } else if (entry2.type == "TRANS" || entry2.type == "TransExecutor") {
                if (vo instanceof PentahoJobVo) {
                    vo2 = vo.transformVos.find {
                        entry2.extName == it.graphId
                    }
                } else if (vo instanceof PentahoTransformVo) {
                    vo2 = vo.transformVos.find {
                        entry2.extName == it.graphId
                    }
                } else {
                    // do nothing
                }
            }
            if (vo2 != null) {
                n2.targetUrl = vo2.mdRelativeUrl
                walk(vo2, n2, depth + 2)
            }
            innerWalking(vo, entry2, depth + 1)
        }
        return true
    }

    void customization(PentahoJobVo vo, PentahoNode node) {
        node.shape = "box"
        if (node.level == 1) {
            node.color = "red"
        } else {
            node.color = "orange"
        }

    }

    void customization(PentahoTransformVo vo, PentahoNode node) {
        node.shape = "ellipse"
        if (node.level == 1) {
            node.color = "red"
        } else {
            node.color = "lime"
        }

    }

    void customization(PentahoEntryVo vo, PentahoNode node) {
        switch (vo.type) {
            case "SPECIAL":
                node.shape = "triangle"
                break
            case "SUCCESS":
                node.shape = "square"
                break
            case "JOB":
            case "JobExecutor":
                node.shape = "box"
                node.color = "orange"
                break
            case "TRANS":
            case "TransExecutor":
                node.shape = "ellipse"
                node.color = "lime"
                break
            default:
                node.shape = "dot"
                break
        }
    }
}
