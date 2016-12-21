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

import java.util.Iterator;

/**
 *
 * @author Dmitriy Malakhov
 */
public class Utils {
    
    public static String getFromBraces(String line, Iterator<String> lines){
        while(line.split("\\{", -1).length != line.split("\\}", -1).length && lines.hasNext()){
            line += " " + lines.next();
        }
        if(!line.contains("}")){
            throw new RuntimeException("can't find }");
        }
        return line.substring(line.indexOf("{") + 1, line.lastIndexOf("}")).replaceAll("\\\\.*? ", " ");
    }
}
