package de.tudresden.inf.st.bigraphs.core.utils.auxiliary;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Source from <a href="https://dzone.com/articles/nested-builder">https://dzone.com/articles/nested-builder</a>
 *
 * @param <T> type of the parent's builder class
 * @param <V> type of the child's class
 * @see <a href="https://dzone.com/articles/nested-builder">https://dzone.com/articles/nested-builder</a>
 */
public abstract class NestedBuilder<T, V> {

    /**
     * To get the parent builder
     *
     * @return T the instance of the parent builder
     */

    public T done() {

        Class<?> parentClass = parent.getClass();

        try {

            V build = this.build();

            String methodname = "with" + build.getClass().getSimpleName();

            Method method = parentClass.getDeclaredMethod(methodname, build.getClass());

            method.invoke(parent, build);

        } catch (NoSuchMethodException

                | IllegalAccessException

                | InvocationTargetException e) {

            e.printStackTrace();

        }

        return parent;

    }

    public abstract V build();

    protected T parent;

    /**
     * @param parent
     * @return
     */

    public <P extends NestedBuilder<T, V>> P withParentBuilder(T parent) {

        this.parent = parent;

        return (P) this;

    }

}