/*
 * Copyright 2015 - 2017 Karel Hübl <karel.huebl@gmail.com>.
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

import groovy.json.JsonBuilder
import org.disl.meta.Table
import org.disl.pattern.FileOutputStep
import org.disl.util.wiki.visjs.DataModelNetwork

/**
 * Create file containg table data model in JSON format for vis.js network visualisation.
 */
class TableDataModelData  extends FileOutputStep {

    Table table

    @Override
    File getFile() {
        return WikiHelper.getDataModelDataFile(table.class.name)
    }

    @Override
    String getCode() {
        new JsonBuilder(new DataModelNetwork(table)).toPrettyString()
    }

    @Override
    int executeInternal() {
        //generate data model file only when foreign keys exist.
        if (table.foreignKeys.size()>0) {
            return super.executeInternal()
        }
        return 0
    }
}
