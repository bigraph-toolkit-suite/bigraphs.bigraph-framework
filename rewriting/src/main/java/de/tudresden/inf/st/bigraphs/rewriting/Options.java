package de.tudresden.inf.st.bigraphs.rewriting;

//TODO: add tactics/order/priorities for RR execution (here?)

/**
 * @author Dominik Grzelak
 */
public class Options {
    private int maximumTransitions;

    private Options() {
    }

    private Options(int maximumTransitions) {
        this.maximumTransitions = maximumTransitions;
    }

    public static Options create(int maximumTransitions) {
        return new Options(maximumTransitions);
    }

    public int getMaximumTransitions() {
        return maximumTransitions;
    }

    public Options setMaximumTransitions(int maximumTransitions) {
        this.maximumTransitions = maximumTransitions;
        return this;
    }
}