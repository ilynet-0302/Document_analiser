package com.example.Document_analiser.util;

public final class RequestTextHolder {
    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();
    private RequestTextHolder() {}
    public static void set(String text) { HOLDER.set(text); }
    public static String get() { return HOLDER.get(); }
    public static void clear() { HOLDER.remove(); }
}

