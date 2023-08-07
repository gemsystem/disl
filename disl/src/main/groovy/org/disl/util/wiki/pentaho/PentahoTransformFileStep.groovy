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

import org.disl.pattern.FileOutputStep

/**
 * Create markdown file for Pentaho transformation.
 *
 * @author Lukáš Vlk
 */
class PentahoTransformFileStep extends FileOutputStep {

    PentahoTransformVo vo

    @Override
    File getFile() {
        return vo.mdFilePath.toFile()
    }

    @Override
    String getCode() {
        return """+++
title = "Transformation: ${vo.name}"
tags = ["Pentaho"]
+++

**Description**: ${vo.description}

**Jobs**:
$jobs

**Transformations**:
$transformation

**Input tables**:
$inputTables

**Output tables**:
$outputTables


{{< pentaho \\"${vo.graphRelativeUrl}\\" >}}

"""
    }

    protected String getJobs() {
        vo.jobVos?.toSorted {a, b ->
            a.name <=> b.name
        }?.collect {
            "* [${it.name}](${it.mdRelativeUrl})"
        }?.join("\n")
    }

    protected String getTransformation() {
        vo.transformVos?.toSorted {a, b ->
            a.name <=> b.name
        }?.collect {
            "* [${it.name}](${it.mdRelativeUrl})"
        }?.join("\n")
    }

    protected String getInputTables() {
        vo.inputTables?.toSorted {a, b ->
            a <=> b
        }?.collect {
            """```
${it}
```"""
        }?.join("\n\n")

    }

    protected String getOutputTables() {
        vo.outputTables?.toSorted {a, b ->
            a <=> b
        }?.collect {
            "* $it"
        }?.join("\n")
    }
}
