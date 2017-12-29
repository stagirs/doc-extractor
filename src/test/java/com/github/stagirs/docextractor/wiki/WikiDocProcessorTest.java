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
package com.github.stagirs.docextractor.wiki;

import com.github.stagirs.common.document.Document;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Dmitriy Malakhov
 */
public class WikiDocProcessorTest {
    
    @Test
    public void test1() throws IOException{
        WikiDocProcessor processor = new WikiDocProcessor();
        Document doc = processor.processDocument("", FileUtils.readFileToString(new File("src/test/resources/Литва"), "utf-8"));
        assertEquals(doc.getPoints().size(), 119);
    }
    
    @Test
    public void test2() throws IOException{
        WikiDocProcessor processor = new WikiDocProcessor();
        Document doc = processor.processDocument("", FileUtils.readFileToString(new File("src/test/resources/Россия"), "utf-8"));
        assertEquals(doc.getPoints().size(), 120);
    }
    
    @Test
    public void test3() throws IOException{
        WikiDocProcessor processor = new WikiDocProcessor();
        Document doc = processor.processDocument("", FileUtils.readFileToString(new File("src/test/resources/Каприянский монастырь"), "utf-8"));
        assertEquals(doc.getPoints().size(), 120);
    }
    
    
}
