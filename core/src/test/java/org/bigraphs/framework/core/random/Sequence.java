/*
 * Copyright (c) 2019-2024 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
