package com.blog.mylog.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class XssFilter {

    private static final Safelist SAFELIST = Safelist.relaxed()
            .addTags("code", "pre", "span", "div")
            .addAttributes("code", "class")
            .addAttributes("span", "class")
            .addAttributes("div", "class");

    public static String clean(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Jsoup.clean(input, SAFELIST);
    }
}