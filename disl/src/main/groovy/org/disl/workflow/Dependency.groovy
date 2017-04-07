/*
 * Copyright 2015 - 2017 GEM System a.s. <sales@gemsystem.cz>.
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
/**
 * Created by akrotky on 30.3.2017.
 */
class Dependency {
    Map<Object, DependencyPropertiesVO> objects = new HashMap<>() //object->color
    Map<Object, Map<Object,DependencyPropertiesVO>> dependencies = new HashMap<>()
    DependencyConditions dependencyConditions
    DependencyDrawChart dependencyDrawChart

    public Dependency(List objects, DependencyConditions dependencyConditions = null) {
        objects.each {this.objects[it]=new DependencyPropertiesVO()}
        //
        if (dependencyConditions == null)
            this.dependencyConditions = new DependencyConditions();
        else
            this.dependencyConditions = dependencyConditions
        //
        buildDependencyMap(objects)
    }

    protected void buildDependencyMap(List objects) {
        objects.each {
            theObject ->
                objects.findAll {
                    !it.class.canonicalName.contains(theObject.class.canonicalName)
                }.each {
                    onObject ->
                        HashSet<String> dependencyTypes = calculateDependencyTypes(theObject, onObject)
                        addDependencies(theObject, onObject, dependencyTypes)
                }
        }
    }

    protected void addDependencies(Object theObject, Object onObject, HashSet<String> dependencyTypes) {
        if (dependencyTypes?.size()>0) {
            if (dependencies.get(theObject) == null) {
                dependencies[theObject] = new HashMap<>()
                dependencies[theObject][onObject] = new DependencyPropertiesVO(types: dependencyTypes)
            } else {
                if (dependencies.get(theObject)?.get(onObject) == null) {
                    dependencies[theObject][onObject] = new DependencyPropertiesVO(types: dependencyTypes)
                } else {
                    dependencies[theObject][onObject].types.addAll(dependencyTypes)
                }
            }
        }
    }

    protected HashSet<String> calculateDependencyTypes(Object theObject, Object onObject) {
        return this.dependencyConditions.getDependencyTypes(theObject, onObject)
    }

    public HashSet<String> getDependencyTypes(Object theObject, Object onObject, List<String> ignoreTypes = [], List<String> onlyTypes = []) {
        HashSet<String> ret = dependencies.get(theObject)?.get(onObject)?.types
                .findAll { !ignoreTypes.contains(it) || ignoreTypes.size() == 0 }
                .findAll { onlyTypes.contains(it) || onlyTypes.size() == 0 }
        return ret==null?[]:ret

    }

    public boolean isDependant(Object theObject, Object onObject, List<String> ignoreTypes = [], List<String> onlyTypes = []) {
        HashSet<String> dependencyTypes = getDependencyTypes(theObject, onObject, ignoreTypes, onlyTypes)
        if (dependencyTypes != null && dependencyTypes.size() > 0)
            return true
        else
            return false
    }
}
