package org.json.zip;

import org.json.JSONException;

public class Huff implements None, PostMortem {
    private final int domain;
    private final Symbol[] symbols;
    private Symbol table;
    private boolean upToDate = false;
    private int width;

    private static class Symbol implements PostMortem {
        public Symbol back = null;
        public final int integer;
        public Symbol next = null;
        public Symbol one = null;
        public long weight = 0;
        public Symbol zero = null;

        public Symbol(int integer) {
            this.integer = integer;
        }

        public boolean postMortem(PostMortem pm) {
            boolean z = true;
            boolean result = true;
            Symbol that = (Symbol) pm;
            if (this.integer != that.integer || this.weight != that.weight) {
                return false;
            }
            boolean z2;
            if (this.back != null) {
                z2 = true;
            } else {
                z2 = false;
            }
            if (that.back == null) {
                z = false;
            }
            if (z2 != z) {
                return false;
            }
            Symbol zero = this.zero;
            Symbol one = this.one;
            if (zero != null) {
                result = zero.postMortem(that.zero);
            } else if (that.zero != null) {
                return false;
            }
            if (one != null) {
                result = one.postMortem(that.one);
            } else if (that.one != null) {
                return false;
            }
            return result;
        }
    }

    public Huff(int domain) {
        int i;
        this.domain = domain;
        int length = (domain * 2) - 1;
        this.symbols = new Symbol[length];
        for (i = 0; i < domain; i++) {
            this.symbols[i] = new Symbol(i);
        }
        for (i = domain; i < length; i++) {
            this.symbols[i] = new Symbol(-1);
        }
    }

    public void generate() {
        if (!this.upToDate) {
            Symbol symbol;
            Symbol next;
            Symbol head = this.symbols[0];
            Symbol previous = head;
            this.table = null;
            head.next = null;
            for (int i = 1; i < this.domain; i++) {
                symbol = this.symbols[i];
                if (symbol.weight < head.weight) {
                    symbol.next = head;
                    head = symbol;
                } else {
                    if (symbol.weight < previous.weight) {
                        previous = head;
                    }
                    while (true) {
                        next = previous.next;
                        if (next == null || symbol.weight < next.weight) {
                            symbol.next = next;
                            previous.next = symbol;
                            previous = symbol;
                        } else {
                            previous = next;
                        }
                    }
                    symbol.next = next;
                    previous.next = symbol;
                    previous = symbol;
                }
            }
            int avail = this.domain;
            previous = head;
            while (true) {
                Symbol first = head;
                Symbol second = first.next;
                head = second.next;
                symbol = this.symbols[avail];
                avail++;
                symbol.weight = first.weight + second.weight;
                symbol.zero = first;
                symbol.one = second;
                symbol.back = null;
                first.back = symbol;
                second.back = symbol;
                if (head == null) {
                    this.table = symbol;
                    this.upToDate = true;
                    return;
                } else if (symbol.weight < head.weight) {
                    symbol.next = head;
                    head = symbol;
                    previous = head;
                } else {
                    while (true) {
                        next = previous.next;
                        if (next == null || symbol.weight < next.weight) {
                            symbol.next = next;
                            previous.next = symbol;
                            previous = symbol;
                        } else {
                            previous = next;
                        }
                    }
                    symbol.next = next;
                    previous.next = symbol;
                    previous = symbol;
                }
            }
        }
    }

    private boolean postMortem(int integer) {
        boolean z = true;
        int[] bits = new int[this.domain];
        Symbol symbol = this.symbols[integer];
        if (symbol.integer != integer) {
            return false;
        }
        int i = 0;
        while (true) {
            Symbol back = symbol.back;
            if (back == null) {
                break;
            }
            if (back.zero == symbol) {
                bits[i] = 0;
            } else if (back.one != symbol) {
                return false;
            } else {
                bits[i] = 1;
            }
            i++;
            symbol = back;
        }
        if (symbol != this.table) {
            return false;
        }
        this.width = 0;
        symbol = this.table;
        while (symbol.integer == -1) {
            i--;
            if (bits[i] != 0) {
                symbol = symbol.one;
            } else {
                symbol = symbol.zero;
            }
        }
        if (!(symbol.integer == integer && i == 0)) {
            z = false;
        }
        return z;
    }

    public boolean postMortem(PostMortem pm) {
        int integer = 0;
        while (integer < this.domain) {
            if (postMortem(integer)) {
                integer++;
            } else {
                JSONzip.log("\nBad huff ");
                JSONzip.logchar(integer, integer);
                return false;
            }
        }
        return this.table.postMortem(((Huff) pm).table);
    }

    public int read(BitReader bitreader) throws JSONException {
        try {
            this.width = 0;
            Symbol symbol = this.table;
            while (symbol.integer == -1) {
                this.width++;
                symbol = bitreader.bit() ? symbol.one : symbol.zero;
            }
            tick(symbol.integer);
            return symbol.integer;
        } catch (Throwable e) {
            JSONException jSONException = new JSONException(e);
        }
    }

    public void tick(int value) {
        Symbol symbol = this.symbols[value];
        symbol.weight++;
        this.upToDate = false;
    }

    public void tick(int from, int to) {
        for (int value = from; value <= to; value++) {
            tick(value);
        }
    }

    private void write(Symbol symbol, BitWriter bitwriter) throws JSONException {
        try {
            Symbol back = symbol.back;
            if (back != null) {
                this.width++;
                write(back, bitwriter);
                if (back.zero == symbol) {
                    bitwriter.zero();
                } else {
                    bitwriter.one();
                }
            }
        } catch (Throwable e) {
            JSONException jSONException = new JSONException(e);
        }
    }

    public void write(int value, BitWriter bitwriter) throws JSONException {
        this.width = 0;
        write(this.symbols[value], bitwriter);
        tick(value);
    }
}
