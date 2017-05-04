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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 *
 * @author Dmitriy Malakhov
 */
public class LatexDocProcessorTest {
    class Text{
        String id;
        String text;
    }
    class DocIterator implements Iterator<Text>{
        Iterator<File> yearIterator;
        Iterator<File> monthIterator;
        Iterator<File> fileIterator;
        File currentYear;
        File currentMonth;
        File currentFile;

        public DocIterator(File dir) {
            yearIterator = Arrays.asList(dir.listFiles()).iterator();
        }

        @Override
        public boolean hasNext() {
            
            while(fileIterator != null && fileIterator.hasNext()){
                currentFile = fileIterator.next();
                if(currentFile.getName().endsWith(".tex") || currentFile.getName().endsWith(".TEX")){
                    return true;
                }
            }
            if(monthIterator != null && monthIterator.hasNext()){
                currentMonth = monthIterator.next();
                fileIterator = Arrays.asList(currentMonth.listFiles()).iterator();
                return hasNext();
            }
            if(yearIterator != null && yearIterator.hasNext()){
                currentYear = yearIterator.next();
                monthIterator = Arrays.asList(currentYear.listFiles()).iterator();
                return hasNext();
            }
            return false;
        }

        @Override
        public Text next() {
            Text text = new Text();
            text.id = currentYear.getName() + " " + currentMonth.getName() + " " + currentFile.getName().substring(0, currentFile.getName().length() - 4) + ".html";
            try {
                text.text = FileUtils.readFileToString(currentFile, "cp866");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return text;
        }
        
    }
    
    
    @Test
    public void test() throws IOException{
        DocIterator docs = new DocIterator(new File("W:\\apache-tomcat-8.0.37\\work\\stagirs\\docs\\collection"));
        while(docs.hasNext()){
            Text text = docs.next();
            try{
                FileUtils.writeStringToFile(new File("W:\\apache-tomcat-8.0.37\\work\\stagirs\\docs\\processed\\" + text.id), new ObjectMapper().writeValueAsString(new LatexDocProcessor().processDocument(text.id, text.text.replace("\\endС", "\\end"))), "utf-8");
            }catch(Throwable e){
                System.err.println(text.id);
                e.printStackTrace();
            }
        }
    }
}
