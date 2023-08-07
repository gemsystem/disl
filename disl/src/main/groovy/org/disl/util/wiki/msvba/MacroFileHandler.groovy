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
package org.disl.util.wiki.msvba

import java.nio.file.Path

/**
 * Handler for Excel file.
 *
 * @author Lukáš Vlk
 */
class MacroFileHandler {

    private TreeSet<MacroVo> tree

    MacroFileHandler() {
        this.tree = new TreeSet<>()
    }

    TreeSet<MacroVo> getTree() {
        return tree
    }

    void handleFile(Path file) {
        tree.add(new MacroVo(file))
    }

    void handleDir(Path dir) {
        tree.add(new MacroVo(dir))
    }

}
