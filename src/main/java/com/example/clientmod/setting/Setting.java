package com.example.clientmod.setting;

import java.util.List;

/** One configurable value on a module. Bool, number (slider) or mode (cycle list). */
public class Setting<T> {
    public final String name;
    private T value;
    public double min, max, step;   // numbers only
    public List<String> modes;      // modes only

    private Setting(String name, T value) { this.name = name; this.value = value; }

    public static Setting<Boolean> bool(String name, boolean def) { return new Setting<>(name, def); }

    public static Setting<Double> number(String name, double def, double min, double max, double step) {
        Setting<Double> s = new Setting<>(name, def);
        s.min = min; s.max = max; s.step = step;
        return s;
    }

    public static Setting<String> mode(String name, String def, String... all) {
        Setting<String> s = new Setting<>(name, def);
        s.modes = List.of(all);
        return s;
    }

    public T get() { return value; }
    public void set(T v) { value = v; }

    /** Left click: toggle / cycle / step up. Right click: step down. */
    @SuppressWarnings("unchecked")
    public void click(boolean right) {
        if (value instanceof Boolean b) {
            value = (T) Boolean.valueOf(!b);
        } else if (value instanceof Double d) {
            double n = d + (right ? -step : step);
            if (n > max) n = min;
            if (n < min) n = max;
            value = (T) Double.valueOf(Math.round(n / step) * step);
        } else if (value instanceof String s) {
            int i = modes.indexOf(s);
            i = (i + (right ? -1 : 1) + modes.size()) % modes.size();
            value = (T) modes.get(i);
        }
    }

    public String display() {
        if (value instanceof Double d) return String.format("%.2f", d);
        return String.valueOf(value);
    }
}
