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

import com.github.stagirs.common.model.Document;
import com.github.stagirs.common.model.Point;
import com.github.stagirs.common.model.Section;
import com.github.stagirs.common.model.Sentence;
import com.github.stagirs.common.model.Text;

/**
 *
 * @author Dmitriy Malakhov
 */
public class Model {
    private Document document = new Document();
    private boolean meta = true;
    private Text part = null;
    private Sentence sentence = null;
    private Point point = null;
    private Section section = null;
    private Section subsection = null;
    private int pointId = 0;
    private int sentenceId = 0;

    public Model(String id) {
        document.setId(id);
    }
    

    public boolean isMeta() {
        return meta;
    }

    public boolean isSentence() {
        return sentence != null;
    }
    
    public void closeMeta() {
        this.meta = false;
    }
    
    public void newline(){
        if(part != null){
            part.append('\n');
        }
    }
    
    
    public Model append(char str){
        if(part == null){
            if(sentence == null){
                throw new RuntimeException("sentence is closed");
            }
            part = new Text(null, "");
        }
        part.append(str);
        return this;
    }
    
    public Model appendLine(String str){
        if(part == null){
            if(sentence == null){
                throw new RuntimeException("sentence is closed");
            }
            part = new Text(null, "");
        }
        part.append(str);
        return this;
    }
        
    public void openSubsection(String title){
        if(subsection != null || point != null || sentence != null){
            throw new RuntimeException("can't open subsection");
        }
        subsection = new Section(document.getId(), title);
    }
    
    public void openSection(String title){
        if(section != null || subsection != null || point != null || sentence != null){
            throw new RuntimeException("can't open section");
        }
        section = new Section(document.getId(), title);
    }
    
    
    public void closeSubsection(){
        closePoint(null);
        if(subsection != null){
            section.getBlocks().add(subsection);
            subsection = null;
        }
    }
    
    public void closeSection(){
        closeSubsection();
        if(section != null){
            document.getBlocks().add(section);
            section = null;
        }
    }
    
    public void openPart(String className){
        if(sentence == null){
            throw new RuntimeException("sentence is closed");
        }
        if(part != null){
            sentence.getParts().add(part);
        }
        part = new Text(className, "");
    }
    
    public void closePart(){
        if(sentence == null){
            throw new RuntimeException("sentence is closed");
        }
        sentence.getParts().add(part);
        part = null;
    }
    
    public void openSentence(){
        if(point == null){
            openPoint(null);
        }
        if(sentence == null){
            sentence = new Sentence(document.getId(), point.getNumber(), sentenceId++, 0);
        }
    }
    
    public void closeSentence(){
        if(sentence != null){
            if(part != null){
                sentence.getParts().add(part);
                part = null;
            }
            point.getSentences().add(sentence);
            sentence = null;
        }
    }
    
    public void openPoint(String className){
        if(point == null){
            point = new Point(document.getId(), pointId++, className);
        }
    }
    
    public void closePoint(String className){
        closeSentence();
        if(point != null && (point.getClassName() == null || point.getClassName().equals(className))){
            if(subsection != null){
                subsection.getBlocks().add(point);
                point = null;
                return;
            }
            if(section != null){
                section.getBlocks().add(point);
                point = null;
                return;
            }
            document.getBlocks().add(point);
            point = null;
        }
    }

    public Document getDocument() {
        return document;
    }
}
