package org.apache.commons.logging.impl;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class WeakHashtable extends Hashtable {
    private static final int MAX_CHANGES_BEFORE_PURGE = 100;
    private static final int PARTIAL_PURGE_COUNT = 10;
    private static final long serialVersionUID = -1546036869799732453L;
    private int changeCount = 0;
    private final ReferenceQueue queue = new ReferenceQueue();

    class AnonymousClass1 implements Enumeration {
        private final WeakHashtable this$0;
        private final Enumeration val$enumer;

        AnonymousClass1(WeakHashtable weakHashtable, Enumeration enumeration) {
            this.this$0 = weakHashtable;
            this.val$enumer = enumeration;
        }

        public boolean hasMoreElements() {
            return this.val$enumer.hasMoreElements();
        }

        public Object nextElement() {
            return Referenced.access$100((Referenced) this.val$enumer.nextElement());
        }
    }

    private static final class Entry implements java.util.Map.Entry {
        private final Object key;
        private final Object value;

        Entry(Object x0, Object x1, AnonymousClass1 x2) {
            this(x0, x1);
        }

        private Entry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean equals(java.lang.Object r5) {
            /*
            r4 = this;
            r1 = 0;
            if (r5 == 0) goto L_0x0023;
        L_0x0003:
            r2 = r5 instanceof java.util.Map.Entry;
            if (r2 == 0) goto L_0x0023;
        L_0x0007:
            r0 = r5;
            r0 = (java.util.Map.Entry) r0;
            r2 = r4.getKey();
            if (r2 != 0) goto L_0x0024;
        L_0x0010:
            r2 = r0.getKey();
            if (r2 != 0) goto L_0x0032;
        L_0x0016:
            r2 = r4.getValue();
            if (r2 != 0) goto L_0x0034;
        L_0x001c:
            r2 = r0.getValue();
            if (r2 != 0) goto L_0x0032;
        L_0x0022:
            r1 = 1;
        L_0x0023:
            return r1;
        L_0x0024:
            r2 = r4.getKey();
            r3 = r0.getKey();
            r2 = r2.equals(r3);
            if (r2 != 0) goto L_0x0016;
        L_0x0032:
            r1 = 0;
            goto L_0x0023;
        L_0x0034:
            r2 = r4.getValue();
            r3 = r0.getValue();
            r2 = r2.equals(r3);
            if (r2 == 0) goto L_0x0032;
        L_0x0042:
            goto L_0x0022;
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.impl.WeakHashtable.Entry.equals(java.lang.Object):boolean");
        }

        public int hashCode() {
            int i = 0;
            int hashCode = getKey() == null ? 0 : getKey().hashCode();
            if (getValue() != null) {
                i = getValue().hashCode();
            }
            return hashCode ^ i;
        }

        public Object setValue(Object value) {
            throw new UnsupportedOperationException("Entry.setValue is not supported.");
        }

        public Object getValue() {
            return this.value;
        }

        public Object getKey() {
            return this.key;
        }
    }

    private static final class Referenced {
        private final int hashCode;
        private final WeakReference reference;

        Referenced(Object x0, ReferenceQueue x1, AnonymousClass1 x2) {
            this(x0, x1);
        }

        Referenced(Object x0, AnonymousClass1 x1) {
            this(x0);
        }

        static Object access$100(Referenced x0) {
            return x0.getValue();
        }

        private Referenced(Object referant) {
            this.reference = new WeakReference(referant);
            this.hashCode = referant.hashCode();
        }

        private Referenced(Object key, ReferenceQueue queue) {
            this.reference = new WeakKey(key, queue, this, null);
            this.hashCode = key.hashCode();
        }

        public int hashCode() {
            return this.hashCode;
        }

        private Object getValue() {
            return this.reference.get();
        }

        public boolean equals(Object o) {
            if (!(o instanceof Referenced)) {
                return false;
            }
            Referenced otherKey = (Referenced) o;
            Object thisKeyValue = getValue();
            Object otherKeyValue = otherKey.getValue();
            if (thisKeyValue != null) {
                return thisKeyValue.equals(otherKeyValue);
            }
            boolean result;
            if (otherKeyValue == null) {
                result = true;
            } else {
                result = false;
            }
            if (result && hashCode() == otherKey.hashCode()) {
                return true;
            }
            return false;
        }
    }

    private static final class WeakKey extends WeakReference {
        private final Referenced referenced;

        WeakKey(Object x0, ReferenceQueue x1, Referenced x2, AnonymousClass1 x3) {
            this(x0, x1, x2);
        }

        static Referenced access$400(WeakKey x0) {
            return x0.getReferenced();
        }

        private WeakKey(Object key, ReferenceQueue queue, Referenced referenced) {
            super(key, queue);
            this.referenced = referenced;
        }

        private Referenced getReferenced() {
            return this.referenced;
        }
    }

    public boolean containsKey(Object key) {
        return super.containsKey(new Referenced(key, null));
    }

    public Enumeration elements() {
        purge();
        return super.elements();
    }

    public Set entrySet() {
        purge();
        Set<java.util.Map.Entry> referencedEntries = super.entrySet();
        Set unreferencedEntries = new HashSet();
        for (java.util.Map.Entry entry : referencedEntries) {
            Object key = Referenced.access$100((Referenced) entry.getKey());
            Object value = entry.getValue();
            if (key != null) {
                unreferencedEntries.add(new Entry(key, value, null));
            }
        }
        return unreferencedEntries;
    }

    public Object get(Object key) {
        return super.get(new Referenced(key, null));
    }

    public Enumeration keys() {
        purge();
        return new AnonymousClass1(this, super.keys());
    }

    public Set keySet() {
        purge();
        Set<Referenced> referencedKeys = super.keySet();
        Set unreferencedKeys = new HashSet();
        for (Referenced referenceKey : referencedKeys) {
            Object keyValue = Referenced.access$100(referenceKey);
            if (keyValue != null) {
                unreferencedKeys.add(keyValue);
            }
        }
        return unreferencedKeys;
    }

    public synchronized Object put(Object key, Object value) {
        if (key == null) {
            throw new NullPointerException("Null keys are not allowed");
        } else if (value == null) {
            throw new NullPointerException("Null values are not allowed");
        } else {
            int i = this.changeCount;
            this.changeCount = i + 1;
            if (i > 100) {
                purge();
                this.changeCount = 0;
            } else if (this.changeCount % 10 == 0) {
                purgeOne();
            }
        }
        return super.put(new Referenced(key, this.queue, null), value);
    }

    public void putAll(Map t) {
        if (t != null) {
            for (java.util.Map.Entry entry : t.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    public Collection values() {
        purge();
        return super.values();
    }

    public synchronized Object remove(Object key) {
        int i = this.changeCount;
        this.changeCount = i + 1;
        if (i > 100) {
            purge();
            this.changeCount = 0;
        } else if (this.changeCount % 10 == 0) {
            purgeOne();
        }
        return super.remove(new Referenced(key, null));
    }

    public boolean isEmpty() {
        purge();
        return super.isEmpty();
    }

    public int size() {
        purge();
        return super.size();
    }

    public String toString() {
        purge();
        return super.toString();
    }

    protected void rehash() {
        purge();
        super.rehash();
    }

    private void purge() {
        List toRemove = new ArrayList();
        synchronized (this.queue) {
            while (true) {
                WeakKey key = (WeakKey) this.queue.poll();
                if (key == null) {
                    break;
                }
                toRemove.add(WeakKey.access$400(key));
            }
        }
        int size = toRemove.size();
        for (int i = 0; i < size; i++) {
            super.remove(toRemove.get(i));
        }
    }

    private void purgeOne() {
        synchronized (this.queue) {
            WeakKey key = (WeakKey) this.queue.poll();
            if (key != null) {
                super.remove(WeakKey.access$400(key));
            }
        }
    }
}
