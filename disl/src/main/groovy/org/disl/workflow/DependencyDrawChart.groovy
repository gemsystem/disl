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

import groovy.util.logging.Slf4j

import java.awt.Desktop
import java.text.SimpleDateFormat

/**
 * Created by akrotky on 30.3.2017.
 */
@Slf4j
class DependencyDrawChart {
    Dependency dependency

    DependencyDrawChart(Dependency dependency) {
        this.dependency = dependency
    }

    public void createFile(String fileName = null, boolean openBrowser = false) {
        BufferedWriter writer
        File file
        if (fileName == null) {
            file = File.createTempFile("dependencyChart", ".html")
            writer = new BufferedWriter(new FileWriter(file))
        } else {
            file = new File(fileName)
            writer = new BufferedWriter(new FileWriter(file));
        }
        writer.write(getChart());
        writer.close()
        log.info("Location of dependency chart: ${file.toURI()}")
        if (openBrowser) Desktop.getDesktop().browse(file.toURI());

    }

    public String getChart() {
        SimpleDateFormat time_formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
        String currentTime = time_formatter.format(System.currentTimeMillis());
        """
<!doctype html>
<html>
<head>
  <title>Dependency chart ${currentTime}</title>

  <script type="text/javascript" src="http://visjs.org/dist/vis.js"></script>
  <link href="http://visjs.org/dist/vis-network.min.css" rel="stylesheet" type="text/css" />

  <style type="text/css">
    #mynetwork {
      width: 90hh;
      height:90vh;
      border: 1px solid lightgray;
    }
  </style>
</head>
<body>


<div id="mynetwork"></div>

<script type="text/javascript">
  // create an array with nodes
  var nodes = new vis.DataSet([
    ${
            dependency.objects.collect { key, prop -> "{id: '${key.getClass().getCanonicalName()}', label: '${key}', title: '${prop.title} ${key.getClass().getCanonicalName()}', color: '${prop.backgroundColor}'}" }.join(",\n\t")
        }
  ]);

  // create an array with edges
  var edges = new vis.DataSet([
    ${
            dependency.dependencies.collect { fromObj, to ->
                to.collect { toObj, prop ->
                    "{from: '${fromObj.getClass().getCanonicalName()}', to: '${toObj.getClass().getCanonicalName()}', arrows:'to', dashes:${prop.dashes}, label:'${prop.types.join(",")}', color: '${prop.color}'}"
                }.join(",\n\t")
            }.join(",\n\t")
        }
  ]);

  // create a network
  var container = document.getElementById('mynetwork');
  var data = {
    nodes: nodes,
    edges: edges
  };
  var options = {
   layout:{
    randomSeed: 0,
    improvedLayout:true,
    hierarchical: {
      enabled:false,
      levelSeparation: 150,
      nodeSpacing: 100,
      treeSpacing: 200,
      blockShifting: false,
      edgeMinimization: false,
      parentCentralization: false,
      direction: 'UD',        // UD, DU, LR, RL
      sortMethod: 'hubsize'   // hubsize, directed
    },
   },
   edges:{
    smooth: true
   },
   physics: {
    enabled: true,
    minVelocity: 0.75
   },
  interaction:{
    multiselect: true
  }  
};
  var network = new vis.Network(container, data, options);
</script>


</body>
</html>
"""
    }

}
