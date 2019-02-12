package com.example.userdetails;

public class Utils {

    public static String caps(String a) {
        return a.substring(0,1).toUpperCase() + a.substring(1);
    }

    public static String capsMulti(String str) {
        StringBuilder result = new StringBuilder(str.length());
        String words[] = str.split("\\ ");
        for (int i = 0; i < words.length; i++)
        {
            result.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].substring(1)).append(" ");

        }
        return String.valueOf(result);
    }
}
