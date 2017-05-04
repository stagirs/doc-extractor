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

import com.github.stagirs.common.document.Document;
import com.github.stagirs.common.document.Point;
import com.github.stagirs.docextractor.Processor;
import com.github.stagirs.latex.MacroBlockProcessor;
import com.github.stagirs.latex.MacroProcessor;
import com.github.stagirs.latex.lexical.Chain;
import com.github.stagirs.latex.lexical.LatexLexicalAnalyzer;
import com.github.stagirs.latex.lexical.item.Command;
import com.github.stagirs.latex.lexical.item.CommandParam;
import com.github.stagirs.latex.lexical.item.Group;
import com.github.stagirs.latex.lexical.item.Item;
import com.github.stagirs.latex.lexical.item.PlainText;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Dmitriy Malakhov
 */
public class LatexDocProcessor implements Processor{
    HashSet<String> latexBlockNames = new HashSet(Arrays.asList("equation", "gather*", "multline*", "gather", "multline"));
    public class Block{
        List<Item> items = new ArrayList<>();
        public void add(Item item){
            items.add(item);
        }
        public boolean isEmpty(){
            return items.isEmpty();
        }
    }
    
    @Override
    public Document processDocument(String id, String doc){
        Chain chain = LatexLexicalAnalyzer.parse(doc);
        MacroProcessor.process(chain);
        MacroBlockProcessor.process(chain);
        Document document = new Document();
        document.setId(id);
        List<Block> blocks = getBlocks(chain);
        for (Item item : blocks.get(0).items) {
            if(item instanceof Command){
                Command command = (Command) item;
                switch(command.getName()){
                    case "\\thanks": document.setNotes(command.getFirstParamText()); break;
                    case "\\title":  document.setTitle(command.getFirstParamText()); break;
                    case "\\author":  document.setAuthor(command.getFirstParamText()); break;
                    case "\\udk":  document.getClassifiers().put("UDK", command.getFirstParamText()); break;
                    case "\\nomer":  document.setOutput(document.getOutput() + " nomer: " + command.getFirstParamText()); break;
                    case "\\god":  document.setOutput(document.getOutput() + " year: " + command.getFirstParamText()); break;
                }
            }
        }
        for (int i = 1; i < blocks.size(); i++) {
            Point point = new Point();
            StringBuilder sb = new StringBuilder();
            Block block = blocks.get(i);
            String formulaBlock = null;
            for (int j = 0; j < block.items.size(); j++) {
                Item item = block.items.get(j);
                if(item instanceof Command){
                    Command command = (Command) item;
                    switch(command.getName()){
                        case "$": 
                            if(formulaBlock != null){
                                if(!formulaBlock.equals(command.getName())){
                                    throw new RuntimeException();
                                }
                                sb.append("</formula> ");
                                formulaBlock = null;
                            }else{
                                sb.append(" <formula>");
                                formulaBlock = command.getName();
                            }
                            break;
                        case "\\[": 
                            if(formulaBlock != null){
                                throw new RuntimeException();
                            }
                            sb.append(" <formula>");
                            formulaBlock = command.getName();
                            break;
                        case "\\]": 
                            if(formulaBlock == null || !formulaBlock.equals("\\[")){
                                throw new RuntimeException();
                            }
                            sb.append("</formula> ");
                            formulaBlock = null;
                            break;
                        case "\\begin": 
                            break; 
                        case "\\end": case "\\end=":
                            break;      
                        case "\\section": case "\\subsection": 
                            point.setTitle(command.getFirstParamText());
                            break;  
                        case "\\text":  sb.append(" ").append(command.getFirstParamText()); break;
                        case "\\intertext":  sb.append(" ").append(command.getFirstParamText()); break;
                        default:
                            sb.append(item.toString());
                    }
                }else{
                    sb.append(item.toString());
                }
            }
            point.setText(sb.toString());
            document.getPoints().add(point);
        }
        return document;
    }
    
    private List<Block> getBlocks(Chain chain){
        LinkedList<Block> list = new LinkedList<>();
        list.add(new Block());
        Iterator<Item> items = chain.getList().iterator();
        while (items.hasNext()) {
            Item item = items.next();
            if(item instanceof Command && ((Command)item).getName().equals("\\begin")){
                processBlock(list, (Command) item, items);
                continue;
            }
            list.getLast().add(item);
        }
        return list;
    }
    
    private void processBlock(LinkedList<Block> list, Command begin, Iterator<Item> items){
        if(!list.getLast().isEmpty()){
            list.add(new Block());
        }
        list.getLast().add(begin);
        String cur = begin.getFirstParamText();
        while (items.hasNext()) {
            Item item = items.next();
            if(item instanceof Command){
                Command command = (Command) item;
                switch(command.getName()){
                    case "\\begin":
                        if(command.getFirstParamText().equals(cur)){
                            command.toString();
                        }
                        processBlock(list, command, items);
                        break; 
                    case "\\end": case "\\end=":
                        if(cur.isEmpty() || command.getFirstParamText().equals(cur)){
                            list.getLast().add(item);
                            list.add(new Block());
                            return; 
                        }else{
                            throw new RuntimeException();
                        }   
                    case "\n\n": case "\\par": case "\\newline": case "\\vskip": 
                        if(latexBlockNames.contains(cur)){
                            list.getLast().add(item);
                        }else{
                            if(!list.getLast().isEmpty()){
                                list.add(new Block());
                            }
                        }
                        break;  
                    case "\\section": case "\\subsection": 
                        if(!cur.equals("document")){
                            throw new RuntimeException();
                        }
                        if(!list.getLast().isEmpty()){
                            list.add(new Block());
                            list.getLast().add(item);
                        }
                        break; 
                    default: list.getLast().add(item);
                }
            }else{
                list.getLast().add(item);
            }
        }
    }
}
