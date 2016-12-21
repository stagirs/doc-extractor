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
import static com.github.stagirs.docextractor.latex.Utils.getFromBraces;
import java.util.Arrays;
import java.util.Iterator;

/**
 *
 * @author Dmitriy Malakhov
 */
public class LatexDocProcessor implements Processor{

    private Model model;
    private MetaProcessor metaProcessor;
    private TextProcessor textProcessor;
    
    @Override
    public Document processDocument(String id, String doc){
        model = new Model(id);
        metaProcessor = new MetaProcessor(model);
        textProcessor = new TextProcessor(model);
        doc = doc.replaceAll("\\$\\$", "\n\\$\\$\n")
                .replaceAll("\\\\begin\\s*?\\{(.*?)\\}", "\n\\\\begin{$1}\n")
                .replaceAll("\\\\end\\s*?\\{(.*?)\\}", "\n\\\\end{$1}\n")
                .replaceAll("\\\\section\\s*?\\{(.*?)\\}", "\n\\\\section{$1}\n")
                .replaceAll("\\\\subsection\\s*?\\{(.*?)\\}", "\n\\\\subsection{$1}\n")
                /*.replace("}{", "}\n{")*/.replace("\\,", ",").replace("\\;", ";").replace("\r", "");
        Iterator<String> lines = Arrays.asList(doc.split("\n")).iterator();
        while (lines.hasNext()) {
            String line = lines.next();
            
            if(model.isMeta()){
                metaProcessor.processMetaLine(line, lines);
                if(line.startsWith("\\begin{document}")){
                    model.closeMeta();
                }
                continue;
            }
            if(line.isEmpty() || line.startsWith("\\vskip")){
                model.newline();
                continue;
            }
            if(line.startsWith("\\par") || line.startsWith("\\newline")){
                model.closePoint(null);
                continue;
            }
            if(line.startsWith("\\begin{equation}")){
                processLatexBlock(lines, "\\begin{equation}", "\\end{equation}");
                continue;
            }
            if(line.startsWith("\\begin{gather*}")){
                processLatexBlock(lines, "\\begin{gather*}", "\\end{gather*}");
                continue;
            }
            if(line.startsWith("\\begin{multline*}")){
                processLatexBlock(lines, "\\begin{multline*}", "\\end{multline*}");
                continue;
            }
            if(line.startsWith("\\begin{gather}")){
                processLatexBlock(lines, "\\begin{gather}", "\\end{gather}");
                continue;
            }
            if(line.startsWith("\\begin{multline}")){
                processLatexBlock(lines, "\\begin{multline}", "\\end{multline}");
                continue;
            }
            if(line.startsWith("$$")){
                processLatexBlock(lines, "$$", "$$");
                continue;
            }
            if(line.startsWith("\\section{")){
                model.closeSection();
                model.openSection(getFromBraces(line, lines));
                continue;
            }
            if(line.startsWith("\\subsection{")){
                model.closeSubsection();
                model.openSubsection(getFromBraces(line, lines));
                continue;
            }
            textProcessor.processTextLine(line, lines);
            model.newline();
        }
        model.closeSection();
        return model.getDocument();
    }
    
    private void processLatexBlock(Iterator<String> lines, String start, String end){
        if(!lines.hasNext()){
            return;
        }
        if(!model.isSentence()){
            model.openSentence();
        }
        model.openPart("latex");
        int level = 1;
        model.appendLine(start).append('\n');
        do{
            String line = lines.next();
            model.appendLine(line).append('\n');
            if(line.startsWith(end)){
                level--;
            }else if(line.startsWith(start)){
                level++;
            }
        }while(level > 0 && lines.hasNext());
        model.closePart();
    }
}
