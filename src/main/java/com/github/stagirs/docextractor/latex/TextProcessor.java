/*
 * Copyright 2016 Dmitriy Malakhov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.stagirs.docextractor.latex;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Iterator;

/**
 *
 * @author Dmitriy Malakhov
 */
public class TextProcessor {
    private final Model model;

    public TextProcessor(Model model) {
        this.model = model;
    }
    
    public void processTextLine(String line, Iterator<String> lines){
        line = line.trim();
        
        while(lines.hasNext() && line.split("\\$", -1).length % 2 == 0){
            line += " " + lines.next();
        }
        CharacterIterator it = new StringCharacterIterator(line); 
        
        if(Character.isLetter(it.first()) && Character.isUpperCase(it.first())){
            model.closeSentence();
            model.openSentence();
        }
        boolean p = false;
        for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
            if(p){
                if(Character.isWhitespace(c)){
                    model.append(c);
                    continue;
                }
                if(!Character.isLetter(c) && c !=',' && c !='$' || Character.isLetter(c) && !Character.isLowerCase(c)){
                    model.closeSentence();
                    model.openSentence();
                }
                p = false;
            }
            if(c == '$'){
                processSimpleFormule(it);
                continue;
            }
            if(c == '{'){
                if(model.isSentence()){
                    model.append(' ');
                }
                continue;
            }
            if(c == '}'){
                if(model.isSentence()){
                    model.append(' ');
                }
                continue;
            }
            if(c == '\\'){
                processComplexBlock(it);
                continue;
            }
            if(c == '%'){
                break;
            }
            if(!model.isSentence()){
                model.openSentence();
            }
            model.append(c);
            if(c == '.'){
                p = true;
            }
        }
    }
    
    private void processSimpleFormule(CharacterIterator it){
        if(!model.isSentence()){
            model.openSentence();
        }
        model.openPart("latex");
        model.append('$');
        for (char c = it.next(); c != '$' && c != CharacterIterator.DONE; c = it.next()) {
            model.append(c);
        }
        model.append('$');
        model.closePart();
    }
    
    private void processComplexBlock(CharacterIterator it){
        String type = "";
        for (char c = it.next(); c != ' ' && c != CharacterIterator.DONE; c = it.next()) {
            type += c;
        }
        if(type.startsWith("begin{")){
            model.closePoint(null);
            model.openPoint(type.substring(type.indexOf("{") + 1, type.indexOf("}")));
        }
        if(type.startsWith("end{")){
            model.closePoint(type.substring(type.indexOf("{") + 1, type.indexOf("}")));
        }
    }
}
