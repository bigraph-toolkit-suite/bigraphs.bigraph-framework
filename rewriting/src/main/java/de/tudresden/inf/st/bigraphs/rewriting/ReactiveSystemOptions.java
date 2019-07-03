package de.tudresden.inf.st.bigraphs.rewriting;

//TODO: add tactics/order/priorities for RR execution (here?)

import java.io.File;

/**
 * @author Dominik Grzelak
 */
public class ReactiveSystemOptions {
    private int maximumTransitions;
    private File folderOutputStates;
    //TODO output reaction graph? static inner class with folderOutputStates: see bigrapher...
    private ReactiveSystemOptions() {
    }

    private ReactiveSystemOptions(int maximumTransitions) {
        this.maximumTransitions = maximumTransitions;
    }

    public static ReactiveSystemOptions create(int maximumTransitions) {
        return new ReactiveSystemOptions(maximumTransitions);
    }

    public int getMaximumTransitions() {
        return maximumTransitions;
    }

    public ReactiveSystemOptions setMaximumTransitions(int maximumTransitions) {
        this.maximumTransitions = maximumTransitions;
        return this;
    }
}