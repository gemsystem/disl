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

import groovy.xml.XmlSlurper

/**
 * Parser for Pentaho file.
 *
 * @author Lukáš Vlk
 */
class PentahoParser {

    PentahoJobVo parse(PentahoJobVo vo) {
        def job = new XmlSlurper().parse(vo.pentahoFilePath)
        vo.name = job.name.toString()
        vo.description = job.description.toString()
        def entryList = job.entries.entry
        vo.jobs = entryList.findAll { entry ->
            entry.type == "JOB"
        }.collect { entry ->
            entry.jobname.toString()
        }.findAll {
            it != null && !it.isEmpty()
        }
        vo.transforms = entryList.findAll { entry ->
            entry.type == "TRANS"
        }.collect { entry ->
            entry.transname.toString()
        }.findAll {
            it != null && !it.isEmpty()
        }

        vo.entryVos = entryList.collect { entry ->
            new PentahoEntryVo(
                entry.name.toString(),
                entry.type.toString(),
                entry.description.toString(),
                getExtName(entry.type.toString(),entry.jobname.toString(), entry.transname.toString(), vo),
                entry.filename.toString(),
                entry.transname.toString(),
                entry.directory.toString()
            )
        }.findAll {
            it != null
        }

        vo.hopVos = job.hops.hop.collect { hop ->
            new PentahoHopVo(
                from: hop.from.toString(),
                to: hop.to.toString()
            )
        }.findAll {
            it != null
        }

        return vo
    }

    protected String getExtName(String type, String jobname, String transname, PentahoJobVo vo) {
        if (type == "TRANS") {
            vo.graphId.replace(vo.graphFilePath.fileName.toString(), transname.toLowerCase() + ".ktr.json")
        } else if (type == "JOB") {
            vo.graphId.replace(vo.graphFilePath.fileName.toString(), jobname.toLowerCase() + ".kjb.json")
        } else {
            null
        }
    }

    protected String getExtName(String type, String jobname, String transname, PentahoTransformVo vo) {
        if (type == "TransExecutor") {
            vo.graphId.replace(vo.graphFilePath.fileName.toString(), transname.toLowerCase() + ".ktr.json")
        } else if (type == "JobExecutor") {
            vo.graphId.replace(vo.graphFilePath.fileName.toString(), jobname.toLowerCase() + ".kjb.json")
        } else {
            null
        }
    }

    PentahoTransformVo parse(PentahoTransformVo vo) {
        def transformation = new XmlSlurper().parse(vo.pentahoFilePath)
        vo.name = transformation.info.name.toString()
        vo.description = transformation.info.description.toString()
        def stepList = transformation.step
        vo.jobs = stepList.findAll { step ->
            step.type == "JobExecutor"
        }.collect { step ->
            step.job_name.toString()
            // TODO <directory_path>${Internal.Entry.Current.Directory}</directory_path>
        }.findAll {
            it != null && !it.isEmpty()
        }
        vo.transforms = stepList.findAll { step ->
            step.type == "TransExecutor"
        }.collect { step ->
            step.trans_name.toString()
        }.findAll {
            it != null && !it.isEmpty()
        }

        vo.inputTables = stepList.findAll { step ->
            step.type == "TableInput"
        }.collect { step ->
            "${step.sql}"
        }.findAll {
            it != null && !it.isEmpty()
        }

        vo.outputTables = stepList.findAll { step ->
            step.type == "TableOutput"
        }.collect { step ->
            "${step.connection}/${step.schema}.${step.table}"
        }.findAll {
            it != null && !it.isEmpty()
        }

        vo.entryVos = stepList.collect { entry ->
            new PentahoEntryVo(
                entry.name.toString(),
                entry.type.toString(),
                entry.description.toString(),
                getExtName(entry.type.toString(), entry.job_name.toString(), entry.trans_name.toString(), vo)
            )
        }.findAll {
            it != null
        }

        vo.hopVos = transformation.order.hop.collect { hop ->
            new PentahoHopVo(
                from: hop.from,
                to: hop.to
            )
        }.findAll {
            it != null
        }

        return vo
    }
}
