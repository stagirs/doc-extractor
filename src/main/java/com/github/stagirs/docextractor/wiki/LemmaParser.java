/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.stagirs.docextractor.wiki;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Dmitriy Malakhov
 */
public class LemmaParser {
    public static List<String> getLems(String text){
        List<Integer> pos = new ArrayList<Integer>();
        text = text.replace("{{|", "{{ |").replace("|}}", "| }}").replace("}{", "}}");
        fillSplit(text, "[[", pos);
        fillSplit(text, "]]", pos);
        fillSplit(text, "{{", pos);
        fillSplit(text, "}}", pos);
        fillSplit(text, "{|", pos);
        fillSplit(text, "|}", pos);
        fillSplit(text, "\t\t", pos);
        Collections.sort(pos);
        if(pos.isEmpty()){
            return Arrays.asList(text);
        }
        List<String> result = new ArrayList<String>();
        if(!pos.get(0).equals(0)){
            result.add(text.substring(0, pos.get(0)));
        }
        for (int i = 0; i < pos.size() - 1; i++) {
            result.add(text.substring(pos.get(i), pos.get(i + 1)));
        }
        if(!pos.get(pos.size() - 1).equals(text.length())){
            result.add(text.substring(pos.get(pos.size() - 1)));
        }
        return result;
    }
    
    public static void fillSplit(String text, String separator, List<Integer> pos){
        int index = text.indexOf(separator);
        while(index != -1){
            pos.add(index);
            pos.add(index + separator.length());
            index = text.indexOf(separator, index + separator.length());
        }
    }
}
