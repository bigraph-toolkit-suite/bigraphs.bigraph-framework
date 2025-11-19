/*
 * Copyright (c) 2019-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.core;

/**
 * Enum of control <i>status</i> values.
 *
 * @author Dominik Grzelak
 */
public enum ControlStatus {

    ATOMIC, ACTIVE, PASSIVE;

    /**
     * Returns {@code true} if the control is atomic, meaning, that nothing may be nested within a node incorporating
     * this control.
     *
     * @param control the control to check
     * @return {@true} if the control is atomic, otherwise {@code false}
     */
    public static boolean isAtomic(Control control) {
        return control.getControlKind() == ControlStatus.ATOMIC;
    }

    public static boolean isActive(Control control) {
        return control.getControlKind() == ControlStatus.ACTIVE;
    }

    public static ControlStatus fromString(String value) {
        if (value.equalsIgnoreCase("atomic")) return ATOMIC;
        if (value.equalsIgnoreCase("active")) return ACTIVE;
        if (value.equalsIgnoreCase("passive")) return PASSIVE;
        return ACTIVE;
    }
}
