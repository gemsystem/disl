/*
 * Copyright 2015 - 2017 Antonin Krotky <antoninkrotky@gmail.com>.
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
package org.disl.meta

import org.disl.pattern.MappingPattern
import org.disl.pattern.TablePattern

/**
 * Enables to simply implement SubqueryMaterialized
 */
abstract class TableMappingMaterialized extends TableMapping {
    /**
     * Don't forget to implement:
     *   getPattern() e.g. DropCreateAsSelectStg pattern
     *   String getFullName() e.g. ${this.class.simpleName_PKG_${this.class.package.name.replaceAll("\\.","_")}
     */
    @Override
    public abstract MappingPattern getPattern()
    public abstract String getFullName()

    List<String> primaryKeyColumns =[]
    List<String> notNullColumns =[]

    @Override
    protected void initDescription() {
        super.initDescription()
        initPrimaryKey()
        initNotNull()
    }

    protected void initPrimaryKey() {
        getFieldsByType(ColumnMapping).each {
            PrimaryKey primaryKey = it.getAnnotation(PrimaryKey)
            if (primaryKey) {
                this.primaryKeyColumns.add(it.name)
            }
        }
    }

    protected void initNotNull() {
        getFieldsByType(ColumnMapping).each {
            NotNull notNull = it.getAnnotation(NotNull)
            if (notNull) {
                this.notNullColumns.add(it.name)
            }
        }
    }

    @Override
    Table getTarget() {
        new Table() {
            @Override
            TablePattern getPattern() {
                return null
            }
            @Override
            String getName() {
                return this.fullName
            }
        }
    }

    @Override
    public List<String> getPropertyNamesByType(Class type) {
        super.getPropertyNamesByType(type).findAll { !it.equals("target") }
    }

    @Override
    public String getRefference() {
        if (sourceAlias != null) {
            return "$fullName $sourceAlias"
        }
        return "(${getSQLQuery()})"
    }
}