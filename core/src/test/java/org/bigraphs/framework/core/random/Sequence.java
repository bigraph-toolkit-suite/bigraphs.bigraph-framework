package org.bigraphs.framework.core.random;

import com.google.common.collect.AbstractIterator;

/**
 * Source: https://stackoverflow.com/a/8714470
 *
 * @see <a href="https://stackoverflow.com/a/8714470">https://stackoverflow.com/a/8714470</a>
 */
class Sequence extends AbstractIterator<String> {
    private int now;
    private static char[] vs;

    static {
        vs = new char['Z' - 'A' + 1];
        for (char i = 'A'; i <= 'Z'; i++) vs[i - 'A'] = i;
    }

    private StringBuilder alpha(int i) {
        assert i > 0;
        char r = vs[--i % vs.length];
        int n = i / vs.length;
        return n == 0 ? new StringBuilder().append(r) : alpha(n).append(r);
    }

    @Override
    protected String computeNext() {
        return alpha(++now).toString();
    }
}
