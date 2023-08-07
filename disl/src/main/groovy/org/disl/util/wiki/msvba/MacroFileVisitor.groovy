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

import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

/**
 * File visitor for Excel file.
 *
 * @author Lukáš Vlk
 */
class MacroFileVisitor extends SimpleFileVisitor<Path> {

    MacroFileHandler handler

    MacroFileVisitor(MacroFileHandler handler) {
        this.handler = handler
    }

    @Override
    FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes basicFileAttributes) throws IOException {
        handler.handleDir(dir)
        return FileVisitResult.CONTINUE
    }

    @Override
    FileVisitResult visitFile(Path file, BasicFileAttributes basicFileAttributes) throws IOException {
        def filename = file.getFileName().toString()
        if (filename.endsWithAny(".xls", ".xlsx", ".xlsm", ".xlsb")) {
            handler.handleFile(file)
        }
        return FileVisitResult.CONTINUE
    }
}
