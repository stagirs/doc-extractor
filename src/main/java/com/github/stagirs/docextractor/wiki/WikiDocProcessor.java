/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.stagirs.docextractor.wiki;

import com.github.stagirs.common.document.Document;
import com.github.stagirs.common.document.Point;
import com.github.stagirs.common.document.Tag;
import com.github.stagirs.common.text.TextUtils;
import com.github.stagirs.docextractor.Processor;
import com.github.stagirs.docextractor.wiki.model.Link;
import com.github.stagirs.lingvo.morpho.MorphoAnalyst;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

/**
 *
 * @author Dmitriy Malakhov
 */
public class WikiDocProcessor implements Processor{

    
    
    @Override
    public Document processDocument(String id, String str) {
        Document doc = new Document();
        doc.setId(id);
        List<Point> points = new ArrayList<Point>();
        doc.setPoints(points);
        points.add(new Point(0, true, str.substring(str.indexOf("<title>") + "<title>".length(), str.indexOf("</title>"))));
        if(!str.contains("<text xml:space=\"preserve\">")){
            return doc;
        }
        String text = str.substring(str.indexOf("<text xml:space=\"preserve\">") + "<text xml:space=\"preserve\">".length(), str.indexOf("</text>"));
        text = text.replace("&lt;", "<").replace("&gt;", ">")/*.replaceAll("<!--.*?->", "").replaceAll("<.*?>.*?</*?>", "")*/;
        try { 
            Iterator elems = WikiParser.getElems(LemmaParser.getLems(text).iterator(), null).iterator();
            List forPoint = new ArrayList();
            int level = 0;
            while(elems.hasNext()){
                Object elem = elems.next();
                if(elem.equals("\t\t")){
                    if(!forPoint.isEmpty()){
                        level = fillPoints(points, level, forPoint);
                        forPoint.clear();
                    }
                    continue;
                }
                forPoint.add(elem);
            }
            if(!forPoint.isEmpty()){
                fillPoints(points, level, forPoint);
            }
            return doc;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    private int fillPoints(List<Point> points, int level, List elems){
        List<String> tags = new ArrayList<String>();
        for (Object elem : elems) {
            if(elem instanceof Link){
                tags.add(((Link)elem).getLink());
            }
        }
        String text = StringUtils.join(elems, " ");
        if(text.startsWith("=")){
            level = 0;
            for (int i = 0; i < text.length(); i++) {
                if(text.charAt(i) == '='){
                    level++;
                }else{
                    break;
                }
            }
            if(text.contains("\t")){
                points.add(new Point(level, true, text.substring(0, text.indexOf("\t")).replace("=", "").trim()));
                text = text.substring(text.indexOf("\t") + 1);
            }else{
                points.add(new Point(level, true, text.replace("=", "").trim()));
                return level;
            }
        }
        if(text.trim().isEmpty()){
            return level;
        }
        Point point = new Point(level, false, text);
        point.setTags(new Tag[tags.size()]);
        for (int i = 0; i < tags.size(); i++) {
            if(tags.get(i).startsWith("Файл:")){
                continue;
            }    
            List<String> terms = MorphoAnalyst.normalize(TextUtils.splitWords(tags.get(i), true));
            point.getTags()[i] = new Tag(terms.toArray(new String[terms.size()]), 1);
        }
        points.add(point);
        return level;
    }
    
    
}
