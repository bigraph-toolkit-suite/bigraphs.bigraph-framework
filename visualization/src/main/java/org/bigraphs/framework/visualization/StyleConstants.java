package org.bigraphs.framework.visualization;

import com.mxgraph.util.mxConstants;

import java.util.Hashtable;

/**
 * Some styling constants for the reaction graph export.
 *
 * @author Dominik Grzelak
 */
public class StyleConstants {

    public static Hashtable<String, Object> predicateMatchedNodeStylesheet() {
        Hashtable<String, Object> style = new Hashtable<>();
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_DOUBLE_RECTANGLE);
        style.put(mxConstants.STYLE_OPACITY, 50);
        style.put(mxConstants.STYLE_STROKECOLOR, "#00FF7F");
        style.put(mxConstants.STYLE_STROKEWIDTH, 2);
        style.put(mxConstants.STYLE_FONTCOLOR, "#006400");
        style.put(mxConstants.STYLE_FONTSIZE, "8");
        return style;
    }

    public static Hashtable<String, Object> defaultNodeStylesheet() {
        Hashtable<String, Object> style = new Hashtable<>();
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        style.put(mxConstants.STYLE_OPACITY, 100);
        style.put(mxConstants.STYLE_STROKECOLOR, "#232323");
        style.put(mxConstants.STYLE_FILLCOLOR, "#ffffff");
        style.put(mxConstants.STYLE_FONTCOLOR, "#232323");
        style.put(mxConstants.STYLE_FONTSIZE, "8");
        return style;
    }

    public static Hashtable<String, Object> defaultEdgeStylesheet() {
        Hashtable<String, Object> style = new Hashtable<>();
        style.put(mxConstants.STYLE_STROKECOLOR, "#232323");
        style.put(mxConstants.STYLE_FONTCOLOR, "#232323");
        style.put(mxConstants.STYLE_FONTSIZE, "6");
        return style;
    }
}
