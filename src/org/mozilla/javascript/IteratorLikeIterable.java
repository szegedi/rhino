package org.mozilla.javascript;

import java.util.Iterator;

/**
 * This is a class that makes it easier to iterate over "iterator-like" objects as defined
 * in the ECMAScript spec. The caller is responsible for retrieving an object that implements
 * the "iterator" pattern. This class will follow that pattern and throw appropriate
 * JavaScript exceptions.
 *
 * The pattern that the target class should follow is:
 * * It should have a function property called "next"
 * * The property should return an object with a boolean value called "done".
 * * If "done" is true, then the returned object should also contain a "value" property.
 */
public class IteratorLikeIterable
    implements Iterable<Object>
{
    private final Context cx;
    private final Scriptable scope;
    private final Callable next;
    private final Scriptable iterator;

    public IteratorLikeIterable(Context cx, Scriptable scope, Object target) {
        this.cx = cx;
        this.scope = scope;
        next = ScriptRuntime.getPropFunctionAndThis(target, "next", cx, scope);
        iterator = ScriptRuntime.lastStoredScriptable(cx);
    }


    @Override
    public Iterator<Object> iterator() {
        return new Itr();
    }

    private final class Itr
        implements Iterator<Object>
    {
        private Object nextVal;

        @Override
        public boolean hasNext() {
            final Object val = next.call(cx, scope, iterator, ScriptRuntime.emptyArgs);
            final Object doneval = ScriptRuntime.getObjectProp(val, "done", cx, scope);
            if (Undefined.instance.equals(doneval)) {
                throw ScriptRuntime.undefReadError(val, "done");
            }
            if (Boolean.TRUE.equals(doneval)) {
                return false;
            }
            nextVal = ScriptRuntime.getObjectProp(val, "value", cx, scope);
            return true;
        }

        @Override
        public Object next() {
            return nextVal;
        }
    }
}
