---
id: visualization
title: Visualizing Bigraphs
---

The framework provides simple means to graphically display bigraphs and
export them as `*.svg` or `*.png`.

Internally, the [graphviz-java](https://github.com/nidi3/graphviz-java) library is used. Bigraphs are constructed as
graphviz models and afterwards converted as graphic file. This allows to export the corresponding DOT file at any time.

## Basics

### As PNG

```java
PureBigraph bigraph = ...;
String convert = BigraphGraphvizExporter.toPNG(bigraph,
        true,
        new File("bigraph-exported.png")
);
```

The return value outputs the bigraph in the DOT format.

The second parameter allows to export the bigraph as a tree-format (flag must be set to `true`) or in the containment-format (flag must be set to `false`).
The difference is shown below.

|As Tree | As Containment |
|---|---|
| ![bigraph-tree](assets/visualization/ex_simple_tree.png) | ![bigraph-containment](assets/visualization/ex_simple_nesting.png)  |

### As DOT

Same as above but without exporting the bigraph to the filesystem:

```java
PureBigraph bigraph = ...;
String converted = BigraphGraphvizExporter.toDOT(bigraph, true);
```

The value of `converted` is:

```
graph "Bigraph" {
 graph ["rankdir"="BT"]
 "Job_v4" ["color"="black","shape"="rectangle"]
 "Computer_v2" ["color"="black","shape"="rectangle"]
 "Room_v0" ["color"="black","shape"="rectangle"]
 "r_0" ["fontcolor"="black","style"="setlinewidth(1)","shape"="ellipse"]
 "User_v1" ["color"="black","shape"="rectangle"]
 "Job_v3" ["color"="black","shape"="rectangle"]
 {
 graph ["rank"="same"]
 "r_0"
 }
 {
 graph ["rank"="same"]
 "Room_v0"
 }
 {
 graph ["rank"="same"]
 "User_v1"
 "Computer_v2"
 }
 {
 graph ["rank"="same"]
 "Job_v4"
 "Job_v3"
 }
 {
 graph ["rank"="source"]
 }
 {
 graph ["rank"="sink"]
 }
 "Job_v4" -- "Computer_v2" ["label"=""]
 "Computer_v2" -- "Room_v0" ["label"=""]
 "Room_v0" -- "r_0" ["label"=""]
 "User_v1" -- "Room_v0" ["label"=""]
 "Job_v3" -- "Computer_v2" ["label"=""]
 }
 ```

## Changing Styles

> Not yet enabled for user-defined customization

The visual representation of the shapes and the colors can be adjusted.
Therefore, special suppliers must be provided by the user.
To do so, all suppliers must extend the generic class `GraphicalFeatureSupplier<V>`.

### `DefaultLabelSupplier`

Allows to change the identifiers of the nodes and links.

### `DefaultShapeSupplier`

Allows to change the shape of all bigraph entities such as nodes, outer names or inner names.

### `DefaultColorSupplier`

Allows to change the color of the shapes with regards to type of a bigraphical element.
