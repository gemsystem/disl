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

import org.disl.meta.Mapping
import org.disl.meta.Table

/**
 * Created by akrotky on 30.3.2017.
 */
class DependencySetProperties {
    Dependency dependency

    DependencySetProperties(Dependency dependency) {
        this.dependency=dependency
    }

    public void setGraphicalStyle(){
        setEdgesProperties(dependency)
        setNodesProperties(dependency)
    }

    protected void setNodesProperties(Dependency dependency) {
        dependency.objects.each {
            theObj,prop ->
                setNodeColor(theObj,prop)
                setNodeBackgroundColor(theObj,prop)
        }
    }

    protected void setEdgesProperties(Dependency dependency) {
        dependency.dependencies.each {
            theObj, map ->
                map.each{
                    onObj, prop ->
                        setEdgesDashes(theObj,onObj,prop)
                        setEdgesColor(theObj,onObj,prop)
                        setEdgesBackgroundColor(theObj,onObj,prop)
                }
        }
    }

    protected void setNodeColor(Object theObj, DependencyPropertiesVO prop) {
    }

    protected void setNodeBackgroundColor(Object theObj, DependencyPropertiesVO prop) {
        if (theObj instanceof Mapping)
            prop.backgroundColor='LightGreen'
        else if (theObj instanceof Table)
            prop.backgroundColor='LightBlue'
        else
            prop.backgroundColor='LightPink'
    }

    protected void setEdgesColor(Object theObj, Object onObj, DependencyPropertiesVO prop) {
        if(prop.types.contains("source"))
            prop.color="blue"
        else if(prop.types.contains("target"))
            prop.color="red"
        else
            prop.color="black"
    }

    protected void setEdgesBackgroundColor(Object theObj, Object onObj, DependencyPropertiesVO prop) {
    }

    protected void setEdgesDashes(Object theObj, Object onObj, DependencyPropertiesVO prop) {
        if(prop.types.contains("FK") && prop.types.size()==1)
            prop.dashes=true
        else
            prop.dashes=false
    }


}
