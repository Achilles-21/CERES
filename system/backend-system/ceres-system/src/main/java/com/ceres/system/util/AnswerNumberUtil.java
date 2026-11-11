package com.ceres.system.util;

public class AnswerNumberUtil {

    public static String answerToNumber(String answer){
        int length = answer.length();
        StringBuffer answerNumStr = new StringBuffer();
        for (int i = 0; i < length; i++) {
            String s = String.valueOf(answer.charAt(i));
            switch(s){
                case "A":
                    answerNumStr.append(1);
                    break;
                case "B":
                    answerNumStr.append(2);
                    break;
                case "C":
                    answerNumStr.append(3);
                    break;
                case "D":
                    answerNumStr.append(4);
                    break;
                case "E":
                    answerNumStr.append(5);
                    break;
                case "F":
                    answerNumStr.append(6);
                    break;
            }
        }
        return answerNumStr.toString();
    }

}
