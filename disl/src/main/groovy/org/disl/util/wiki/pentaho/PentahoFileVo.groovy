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

import java.nio.file.Path

/**
 * VO with data about Pentaho file.
 *
 * @author Lukáš Vlk
 */
class PentahoFileVo extends PentahoVo {

    Path mdFilePath
    String mdFilename
    String mdRelativeUrl
    Path graphFilePath
    String graphRelativeUrl
    String graphId
    List<PentahoEntryVo> entryVos
    List<PentahoHopVo> hopVos

    @Override
    boolean isDirectory() {
        return false
    }

    @Override
    boolean isFile() {
        return true
    }

}
