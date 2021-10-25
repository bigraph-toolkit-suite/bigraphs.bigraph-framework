---
id: visualization-styles
title: Changing Styles
---

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
