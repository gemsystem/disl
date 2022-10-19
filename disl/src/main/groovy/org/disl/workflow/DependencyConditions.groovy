/*
 * Copyright 2015 - 2017 GEM System a.s. <disl@gemsystem.cz>.
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
package org.disl.workflow

import org.disl.meta.Base
import org.disl.meta.Mapping
import org.disl.meta.MetaFactory
import org.disl.meta.Table
import org.disl.meta.TableMapping

/**
 * Created by akrotky on 30.3.2017.
 */
class DependencyConditions {
    protected HashSet<String> getDependencyTypes(Table theTable, Mapping onMapping) {
        HashSet<String> ret = []
        boolean dependsOnTarget = false
        Table targetTable
        if (onMapping instanceof TableMapping) {
            targetTable = onMapping.target
        }
        if (theTable.class.canonicalName.equals(targetTable?.class?.canonicalName)) {
            dependsOnTarget = true
        }
        if (dependsOnTarget)
            ret.add("target")
        ret.addAll(evaluateDependsOnAnnotation(theTable,onMapping))
        return ret
    }

    protected HashSet<String> getDependencyTypes(Table theTable, Table onTable) {
        HashSet<String> ret = []
        if (theTable.class.canonicalName.equals(onTable.class.canonicalName)) {
            ret.add("source")
        }
        boolean dependsOnFK = false
        theTable.getForeignKeys().each{
            FK ->
                if(onTable.class.canonicalName.equals(FK.targetTable.class.canonicalName))
                    dependsOnFK=true
        }
        if (dependsOnFK)
            ret.add("FK")
        ret.addAll(evaluateDependsOnAnnotation(theTable,onTable))
        return ret
    }

    protected HashSet<String> getDependencyTypes(Mapping theMapping, Mapping onMapping) {
        HashSet<String> ret = []
        if (theMapping.class.canonicalName.equals(onMapping.class.canonicalName)) {
            ret.add("mapping")
        }
        theMapping.sources.each {
            ret.addAll(getDependencyTypes(it, onMapping))
        }
        theMapping.setOperations.each {
            ret.addAll(getDependencyTypes(it.source, onMapping))
        }
        ret.addAll(evaluateDependsOnAnnotation(theMapping,onMapping))
        return ret
    }

    protected HashSet<String> getDependencyTypes(Mapping theMapping, Table onTable) {
        HashSet<String> ret = []
        theMapping.sources.each {
            ret.addAll(getDependencyTypes(it, onTable))
        }
        theMapping.setOperations.each {
            ret.addAll(getDependencyTypes(it.source, onTable))
        }
        ret.addAll(evaluateDependsOnAnnotation(theMapping,onTable))
        return ret
    }

    protected HashSet<String> getDependencyTypes(Object theObject, Object onObject) {
        return [] //no dependency
    }

    protected evaluateDependsOnAnnotation(Base theBase, Base onBase) {
        HashSet<String> ret = []
        theBase.dependsOn.each {
            Base theBaseCreated = MetaFactory.create(it)
            ret.addAll(getDependencyTypes(theBaseCreated,onBase))
            /*
            alternative implementation without recursion:
              if (it.name.equals(onBase.getClass().getName())) ret.add("annotation")
            */
        }
        if (ret) return ["annotation"] else []
    }
}
