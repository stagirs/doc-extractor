/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.stagirs.docextractor.wiki;

import com.github.stagirs.docextractor.wiki.model.Command;
import com.github.stagirs.docextractor.wiki.model.Link;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Dmitriy Malakhov
 */
public class WikiParser {
    
    public static List getElems(Iterator<String> lems, String endLem){
        List result = new ArrayList();
        while(lems.hasNext()){
            String lem = lems.next();
            if(lem.equals("{{")){
                try{
                    result.add(getCommand(lems, "}}"));
                }catch(ElemWasNotFoundException e){
                    result.add("\t\t");
                }    
                continue;
            }
            if(lem.equals("{|")){
                try{
                    result.add(getCommand(lems, "|}"));
                }catch(ElemWasNotFoundException e){
                    result.add("\t\t");
                }  
                continue;
            }
            if(lem.equals("[[")){
                try{
                    result.add(getLink(lems, "]]"));
                }catch(ElemWasNotFoundException e){
                    result.add("\t\t");
                }
                continue;
            }
            if(endLem != null && lem.equals(endLem)){
                return result;
            }
            if(endLem != null && lem.equals("\t\t")){
                throw new ElemWasNotFoundException(endLem);
            }
            result.add(lem);
        }
        return result;
    }
    
    private static Command getCommand(Iterator<String> lems, String end){
        return new Command(getElems(lems, end));
    }
    
    private static Link getLink(Iterator<String> lems, String end){
        List result = getElems(lems, end);
        String str = StringUtils.join(result, " ");
        if(!str.contains("|")){
            return new Link(str, str);
        }else{
            return new Link(str.substring(0, str.lastIndexOf("|")), str.substring(str.lastIndexOf("|") + 1));
        }
    }
}
