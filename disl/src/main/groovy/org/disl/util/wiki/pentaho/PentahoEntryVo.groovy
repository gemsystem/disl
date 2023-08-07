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

/**
 * VO with data about Pentaho Entry element.
 *
 * @author Lukáš Vlk
 */
class PentahoEntryVo {

    /**
     * Name of entry
     */
    String name

    /**
     * Type of entry
     */
    String type

    /**
     * Description
     */
    String description

    /**
     * External name (filename)
     */
    String extName

    PentahoEntryVo(String name, String type, String description, String extName) {
        this.name = name
        this.type = type
        this.description = description
        this.extName = extName
    }
}
