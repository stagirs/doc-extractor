/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.stagirs.docextractor.wiki.model;

/**
 *
 * @author Dmitriy Malakhov
 */
public class Link {
    String link;
    String raw;

    public Link(String link, String raw) {
        this.link = link;
        this.raw = raw;
    }

    public String getLink() {
        return link;
    }

    
    public String getRaw() {
        return raw;
    }

    @Override
    public String toString() {
        return raw;
    }
    
    
}
