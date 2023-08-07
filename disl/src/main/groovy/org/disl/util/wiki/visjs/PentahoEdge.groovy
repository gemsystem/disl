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
import org.disl.util.wiki.pentaho.PentahoHopVo

/**
 * Representation of vis.js network Pentaho Edge.
 *
 * @author Lukáš Vlk
 */
class PentahoEdge {

    static Edge edge(PentahoFileVo src, PentahoFileVo target) {
        edge(src.graphId, target.graphId)
    }

    static Edge edge(PentahoHopVo vo) {
        edge(vo.from, vo.to)
    }

    static Edge edge(PentahoFileVo src, PentahoEntryVo target) {
        edge(src.graphId, target.name)
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
