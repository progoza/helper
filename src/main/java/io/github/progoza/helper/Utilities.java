package io.github.progoza.helper;

import java.math.BigDecimal;
import java.util.Scanner;

public class Utilities {

    private final boolean silentDefaults;

    public Utilities(boolean silentDefaults) {
        this.silentDefaults = silentDefaults;
    }

    public BigDecimal readBigDecimal(Scanner s, String prompt, BigDecimal def, boolean silentDefault) {
        if (silentDefault && silentDefaults) {
            return def;
        }
        System.out.print( String.format(prompt, def != null ? " (domyśln.: " + def + ")" : ""));
        String strVal = s.nextLine();
        if (strVal.isBlank()) {
            return def;
        }
        return new BigDecimal(strVal);
    }

    public BigDecimal readBigDecimal(Scanner s, String prompt, BigDecimal def) {
        return readBigDecimal(s, prompt, def, false);
    }

    public BigDecimal readBigDecimalSilent(Scanner s, String prompt, BigDecimal def) {
        return readBigDecimal(s, prompt, def, true);
    }

    public long readLong(Scanner s, String prompt, Long def, boolean silentDefault) {
        if (silentDefault && silentDefaults) {
            return def;
        }
        System.out.print( String.format(prompt, def != null ? " (domyśln.: " + def + ")" : ""));
        String strVal = s.nextLine();
        if (strVal.isBlank()) {
            return def == null ? 0L : def;
        }
        return Long.parseLong(strVal);
    }

    public long readLong(Scanner s, String prompt, Long def) {
        return readLong(s, prompt, def, false);
    }

    public long readLongSilent(Scanner s, String prompt, Long def) {
        return readLong(s, prompt, def, true);
    }

    public int readInt(Scanner s, String prompt, Integer def, boolean silentDefault) {
        if (silentDefault && silentDefaults) {
            return def == null ? 0 : def;
        }
        System.out.print( String.format(prompt, def != null ? " (domyśln.: " + def + ")" : ""));
        String strVal = s.nextLine();
        if (strVal.isBlank()) {
            return def == null ? 0 : def;
        }
        return Integer.parseInt(strVal);
    }

    public int readInt(Scanner s, String prompt, Integer def) {
        return readInt(s, prompt, def, false);
    }

    public int readIntSilent(Scanner s, String prompt, Integer def) {
        return readInt(s, prompt, def, true);
    }

    public String readString(Scanner s, String prompt, String def, boolean silentDefault) {
        if (silentDefault && silentDefaults) {
            return def;
        }
        System.out.print( String.format(prompt, def != null ? " (domyśln.: " + def + ")" : ""));
        String strVal = s.nextLine();
        if (strVal.isBlank()) {
            return def;
        }
        return strVal;
    }

    public String readString(Scanner s, String prompt, String def) {
        return readString(s, prompt, def, false);
    }

    public String readStringSilent(Scanner s, String prompt, String def) {
        return readString(s, prompt, def, true);
    }    
}
