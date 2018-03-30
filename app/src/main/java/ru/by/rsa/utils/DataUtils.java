package ru.by.rsa.utils;



public class DataUtils {

    public static class Float{
        public static String format(String pattern, float value){
            return String.format(pattern, value).replace(',', '.');
        }

        public static String format(String pattern, String value){
            return String.format(pattern, java.lang.Float.parseFloat(value)).replace(',', '.');
        }

        public static float parse(String value){
            if(value == null || value.isEmpty()){
                return 0;
            }
            return java.lang.Float.parseFloat(value.replace(',', '.'));
        }
    }
}
