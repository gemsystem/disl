/*
 * Copyright 2015 - 2017 Karel H�bl <karel.huebl@gmail.com>.
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
package org.disl.util.wiki

import org.disl.meta.Report
import org.disl.meta.Table
import org.disl.pattern.FileOutputStep

/**
 * Generate markdown wiki page file with Table documentation.
 */
class ReportPageStep extends FileOutputStep {

    Report report

    @Override
    File getFile() {
        return WikiHelper.getWikiPageFile(report)
    }

    @Override
    String getCharset() {
        'utf-8'
    }

    @Override
    public String getCode() {
        """\
+++
    title= "${report.name}"
    packages=["${report.getClass().getPackage().getName().replace('.','/')}"]
    schemas=["${report.getSchema()}"]
    tags=["report"]
    group="${report.wikiGroup}"
+++

## ${report.getSchema()}

${WikiHelper.renderElementDescription(report)}


${WikiHelper.renderDataLineage(report)}
"""	}

}
