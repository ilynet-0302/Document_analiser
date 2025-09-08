package com.example.Document_analiser.util;

/**
 * ThreadLocal държач за текущия текст на заявката.
 *
 * - За какво служи: позволява да запазим текста на текущия въпрос/заявка
 *   на ниво нишка (thread), за да е достъпен по-късно в помощни методи
 *   (напр. при fallback търсене по ключови думи), без да го подаваме
 *   експлицитно през много слоеве.
 * - Защо ThreadLocal: всяка HTTP заявка се обработва в отделна нишка,
 *   така гарантираме, че стойността е изолирана за конкретната заявка.
 */
public final class RequestTextHolder {
    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();
    private RequestTextHolder() {}

    /** Задава текста на текущата заявка за текущата нишка. */
    public static void set(String text) { HOLDER.set(text); }
    /** Връща текста, ако е зададен, иначе null. */
    public static String get() { return HOLDER.get(); }
    /** Изчиства стойността, за да избегнем изтичане на памет. */
    public static void clear() { HOLDER.remove(); }
}

