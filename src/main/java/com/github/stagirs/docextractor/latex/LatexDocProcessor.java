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
import com.github.stagirs.docextractor.Processor;
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
import java.util.Iterator;

/**
 *
 * @author Dmitriy Malakhov
 */
//TODO сначала обрабатываем блоки, потом $$, потом $, потом выделяем предложения
public class LatexDocProcessor implements Processor{

    private Model model;
    private Command openLatexBlockCommand;
    
    @Override
    public Document processDocument(String id, String doc){
        Chain chain = LatexLexicalAnalyzer.parse(doc);
        MacroProcessor.process(chain);
        model = new Model(id);
        processChain(chain);
        return model.getDocument();
    }
    
    private void processChain(Chain chain){
        Iterator<Item> items = chain.getList().iterator();
        while (items.hasNext()) {
            Item item = items.next();
            if(item instanceof Command){
                processCommand((Command) item);
            }
            if(item instanceof Group){
                processChain(((Group) item).getText());
            }
            if(item instanceof PlainText){
                processTextLine(((PlainText) item).toString());
            }
        }
    }
    
    private void processCommand(Command command){
        switch(command.getName()){
            case "$": case "$$": 
                if(openLatexBlockCommand != null){
                    if(command.getName().equals(openLatexBlockCommand.getName())){
                        if(model.isSentence()){
                            model.closePart();
                        }
                        openLatexBlockCommand = null;
                    }else{
                        return;
                    }
                }else{
                    if(!model.isSentence()){
                        model.openSentence();
                    }
                    model.openPart("latex");
                    openLatexBlockCommand = command;
                }
                break;
            case "\\[": 
                if(openLatexBlockCommand != null){
                    throw new RuntimeException("begin command: " + openLatexBlockCommand + "; end command:" + command);
                }else{
                    if(!model.isSentence()){
                        model.openSentence();
                    }
                    model.openPart("latex");
                    openLatexBlockCommand = command;
                }
                break;
            case "\\]": 
                if(openLatexBlockCommand == null){
                    throw new RuntimeException("begin command: null; end command:" + command);
                }else{
                    if(command.getName().equals("\\]")){
                        model.closePart();
                        openLatexBlockCommand = null;
                    }else{
                        throw new RuntimeException("begin command: " + openLatexBlockCommand + "; end command:" + command);
                    }
                }
                break;
                
            case "\\begin": 
                switch(command.getFirstParamText()){
                    case "equation": case "gather*": case "multline*": case "gather": case "multline":  
                        if(openLatexBlockCommand != null){
                            throw new RuntimeException("begin command: " + openLatexBlockCommand + "; end command:" + command);
                        }else{
                            if(!model.isSentence()){
                                model.openSentence();
                            }
                            model.openPart("latex");
                            openLatexBlockCommand = command;
                        }
                    break;    
                    case "document": model.closeMeta(); break;   
                }
                break; 
            case "\\end": 
                switch(command.getFirstParamText()){
                    case "equation": case "gather*": case "multline*": case "gather": case "multline":  
                        if(openLatexBlockCommand == null){
                            throw new RuntimeException("begin command: null; end command:" + command);
                        }else{
                            if(openLatexBlockCommand.getName().equals("\\begin") && command.getFirstParamText().equals(openLatexBlockCommand.getFirstParamText())){
                                model.closePart();
                                openLatexBlockCommand = null;
                            }else{
                                throw new RuntimeException("begin command: " + openLatexBlockCommand + "; end command:" + command);
                            }
                        }
                    break;    
                }
                break;     
            case "\n\n": case "\\par": case "\\newline": case "\\vskip": model.closePoint(null); break;  
            case "\\section": 
                model.closeSection();
                model.openSection(command.getFirstParamText());
                break;
            case "\\subsection": 
                model.closeSubsection();
                model.openSubsection(command.getFirstParamText());
                break;    
            case "\\thanks": model.getDocument().setThanks(command.getFirstParamText()); break;
            case "\\title":  model.getDocument().setTitle(command.getFirstParamText()); break;
            case "\\author":  model.getDocument().setAuthor(command.getFirstParamText()); break;
            case "\\udk":  model.getDocument().setClassifier(command.getFirstParamText()); break;
            case "\\nomer":  model.getDocument().addOutput("nomer: " + command.getFirstParamText()); break;
            case "\\god":  model.getDocument().addOutput("year: " + command.getFirstParamText()); break;
            case "\\text":  model.appendLine(command.getFirstParamText()); break;
            case "\\intertext":  model.appendLine(command.getFirstParamText()); break;
            default:
                for (CommandParam param : command.geParams()) {
                    processChain(param.getText());
                }
        }
    }
    
    private void processTextLine(String line){
        if(line.contains("Рассмотрим процедуру, подобную")){
            line.toString();
        }
        line = " " + line.trim();
        if(line.isEmpty()){
            return;
        }
        CharacterIterator it = new StringCharacterIterator(line); 
        char c = it.first();
        while ( c != CharacterIterator.DONE) {
            if(!model.isSentence()){
                model.openSentence();
            }
            model.append(c);
            if(c == '.'){
                char first = it.next();
                while(first != CharacterIterator.DONE && Character.isWhitespace(first)){
                    first = it.next();
                }
                char second = it.next();
                if(Character.isLetter(first) && Character.isUpperCase(first) && Character.isLetter(second)){
                    model.closeSentence();
                }
                if(second != CharacterIterator.DONE){
                    it.previous();
                }
                if(first != CharacterIterator.DONE){
                    it.previous();
                }
            }
            c = it.next();
        }
    }
    
}
