package org.mozilla.javascript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This generic hash table class is used by Set and Map. It uses
 * a standard HashMap for storing keys and values so that we can handle
 * lots of hash collisions if necessary, and a list to support the iterator
 * capability.
 */

public class Hashtable
{
    private final HashMap<Object, Entry> map = new HashMap<>();
    protected final ArrayList<Entry> entries = new ArrayList<>();

    static final class Entry {
        protected Object key;
        protected Object value;
        private final int hashCode;

        Entry(Object k, Object value) {
            if ((k instanceof Number) && ( ! ( k instanceof Double))) {
                // Hash comparison won't work if we don't do this
                this.key = ((Number)k).doubleValue();
            } else {
                this.key = k;
            }

            if (key == null) {
                hashCode = 0;
            } else if (k.equals(ScriptRuntime.negativeZero)) {
                hashCode = 0;
            } else {
                hashCode = key.hashCode();
            }

            this.value = value;
        }

        /**
         * Zero out key and value and return old value.
         */
        Object clear() {
            final Object ret = value;
            key = null;
            value = null;
            return ret;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object o) {
            try {
                return ScriptRuntime.sameZero(key, ((Entry)o).key);
            } catch (ClassCastException cce) {
                throw new AssertionError(cce);
            }
        }
    }

    public int size() {
        return map.size();
    }

    public void put(Object key, Object value) {
        final Entry nv = new Entry(key, value);
        // We need to make sure that we replace the old value so the iterator works
        final Entry ev = map.putIfAbsent(nv, nv);
        if (ev == null) {
            // New value
            entries.add(nv);
        } else {
            // Have to update
            ev.value = value;
        }
    }

    public Object get(Object key) {
        final Entry e = new Entry(key, null);
        final Entry v = map.get(e);
        if (v == null) {
            return null;
        }
        return v.value;
    }

    public boolean has(Object key) {
        final Entry e = new Entry(key, null);
        return map.containsKey(e);
    }

    public Object delete(Object key) {
        final Entry e = new Entry(key, null);
        final Entry v = map.remove(e);
        if (v == null) {
            return null;
        }
        // Something was removed from the map. Leave it in the entries array
        // so that iterators work but null it out.
       return v.clear();
    }

    public void clear() {
        // Clear the hash map, but not entries, so that we can
        // have existing iterators continue to work.
        map.clear();
        entries.forEach(Entry::clear);
    }

    public Iterator<Entry> iterator() {
        return new Iter();
    }

    // The iterator for this class works manually on the "entries" behavior
    // because the Java "fail fast" behavior would cause errors otherwise
    private final class Iter
        implements Iterator<Entry>
    {
        private int position = 0;

        @Override
        public boolean hasNext() {
            while (position < entries.size()) {
                if (entries.get(position).key != null) {
                    return true;
                }
                position++;
            }
            return false;
        }

        @Override
        public Entry next() {
            assert(position < entries.size());
            final Entry e = entries.get(position);
            position++;
            return e;
        }
    }
}
