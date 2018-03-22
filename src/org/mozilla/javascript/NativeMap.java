package org.mozilla.javascript;

public class NativeMap
    extends IdScriptableObject
{
    private static final Object MAP_TAG = "Map";
    static final String ITERATOR_TAG = "Map Iterator";

    private final Hashtable entries = new Hashtable();

    static void init(Context cx, Scriptable scope, boolean sealed)
    {
        NativeMap obj = new NativeMap();
        obj.exportAsJSClass(MAX_PROTOTYPE_ID, scope, false);

        ScriptableObject desc = (ScriptableObject)cx.newObject(scope);
        desc.put("enumerable", desc, false);
        desc.put("configurable", desc, true);
        desc.put("get", desc, obj.get(NativeSet.GETSIZE, obj));
        obj.defineOwnProperty(cx, "size", desc);

        if (sealed) {
            obj.sealObject();
        }
    }

    @Override
    public String getClassName() {
        return "Map";
    }

    @Override
    public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope,
                             Scriptable thisObj, Object[] args)
    {
        if (!f.hasTag(MAP_TAG)) {
            return super.execIdCall(f, cx, scope, thisObj, args);
        }
        int id = f.methodId();
        switch (id) {
            case Id_constructor:
                // TODO read from iterator argument
                if (thisObj == null) {
                    return new NativeMap();
                } else {
                    throw ScriptRuntime.typeError1("msg.no.new", "Map");
                }
            case Id_set:
                return realThis(thisObj, f).js_set(
                    args.length > 0 ? args[0] : Undefined.instance,
                    args.length > 1 ? args[1] : Undefined.instance);
            case Id_delete:
                return realThis(thisObj, f).js_delete(args.length > 0 ? args[0] : Undefined.instance);
            case Id_get:
                return realThis(thisObj, f).js_get(args.length > 0 ? args[0] : Undefined.instance);
            case Id_has:
                return realThis(thisObj, f).js_has(args.length > 0 ? args[0] : Undefined.instance);
            case Id_clear:
                return realThis(thisObj, f).js_clear();
            case Id_keys:
                return realThis(thisObj, f).js_iterator(scope, NativeCollectionIterator.Type.KEYS);
            case Id_values:
            case SymbolId_iterator:
                return realThis(thisObj, f).js_iterator(scope, NativeCollectionIterator.Type.VALUES);
            case Id_entries:
                return realThis(thisObj, f).js_iterator(scope, NativeCollectionIterator.Type.BOTH);
            case SymbolId_getSize:
                return realThis(thisObj, f).js_getSize();
        }
        throw new IllegalArgumentException("Map.prototype has no method: " + f.getFunctionName());
    }

    private Object js_set(Object arg1, Object arg2)
    {
        entries.put(arg1, arg2);
        return this;
    }

    private Object js_delete(Object arg)
    {
        final Object e = entries.delete(arg);
        return (e != null);
    }

    private Object js_get(Object arg)
    {
        final Object val = entries.get(arg);
        return val == null ? Undefined.instance : val;
    }

    private Object js_has(Object arg)
    {
        return entries.has(arg);
    }

    private Object js_getSize()
    {
        return entries.size();
    }

    private Object js_iterator(Scriptable scope, NativeCollectionIterator.Type type)
    {
        return new NativeCollectionIterator(scope, ITERATOR_TAG, type, entries.iterator());
    }

    private Object js_clear()
    {
        entries.clear();
        return Undefined.instance;
    }

    private NativeMap realThis(Scriptable thisObj, IdFunctionObject f)
    {
        try {
            return (NativeMap)thisObj;
        } catch (ClassCastException cce) {
            throw incompatibleCallError(f);
        }
    }

    @Override
    protected void initPrototypeId(int id)
    {
        switch (id) {
            case SymbolId_getSize:
                initPrototypeMethod(MAP_TAG, id, NativeSet.GETSIZE, "get size", 0);
                return;
            case SymbolId_iterator:
                initPrototypeMethod(MAP_TAG, id, SymbolKey.ITERATOR, "[Symbol.iterator]", 0);
                return;
            // fallthrough
        }

        String s, fnName = null;
        int arity;
        switch (id) {
            case Id_constructor:       arity=0; s="constructor";       break;
            case Id_set:               arity=2; s="set";               break;
            case Id_get:               arity=1; s="get";               break;
            case Id_delete:            arity=1; s="delete";            break;
            case Id_has:               arity=1; s="has";               break;
            case Id_clear:             arity=0; s="clear";             break;
            case Id_keys:              arity=0; s="keys";              break;
            case Id_values:            arity=0; s="values";            break;
            case Id_entries:           arity=0; s="entries";           break;
            default: throw new IllegalArgumentException(String.valueOf(id));
        }
        initPrototypeMethod(MAP_TAG, id, s, fnName, arity);
    }

    @Override
    protected int findPrototypeId(Symbol k)
    {
        if (NativeSet.GETSIZE.equals(k)) {
            return SymbolId_getSize;
        }
        if (SymbolKey.ITERATOR.equals(k)) {
            return SymbolId_iterator;
        }
        return 0;
    }

// #string_id_map#

    @Override
    protected int findPrototypeId(String s)
    {
        int id;
// #generated# Last update: 2018-03-22 00:25:16 MDT
        L0: { id = 0; String X = null; int c;
            L: switch (s.length()) {
            case 3: c=s.charAt(0);
                if (c=='g') { if (s.charAt(2)=='t' && s.charAt(1)=='e') {id=Id_get; break L0;} }
                else if (c=='h') { if (s.charAt(2)=='s' && s.charAt(1)=='a') {id=Id_has; break L0;} }
                else if (c=='s') { if (s.charAt(2)=='t' && s.charAt(1)=='e') {id=Id_set; break L0;} }
                break L;
            case 4: X="keys";id=Id_keys; break L;
            case 5: X="clear";id=Id_clear; break L;
            case 6: c=s.charAt(0);
                if (c=='d') { X="delete";id=Id_delete; }
                else if (c=='v') { X="values";id=Id_values; }
                break L;
            case 7: X="entries";id=Id_entries; break L;
            case 11: X="constructor";id=Id_constructor; break L;
            }
            if (X!=null && X!=s && !X.equals(s)) id = 0;
            break L0;
        }
// #/generated#
        return id;
    }

    private static final int
        Id_constructor = 1,
        Id_set = 2,
        Id_get = 3,
        Id_delete = 4,
        Id_has = 5,
        Id_clear = 6,
        Id_keys = 7,
        Id_values = 8,
        Id_entries = 9,
        SymbolId_getSize = 10,
        SymbolId_iterator = 11,
        MAX_PROTOTYPE_ID = SymbolId_iterator;

// #/string_id_map#
}
