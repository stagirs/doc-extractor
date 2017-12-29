/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.stagirs.docextractor.wiki.model;

import java.util.List;

/**
 *
 * @author Dmitriy Malakhov
 */
public class Command {
    List items;

    public Command(List items) {
        this.items = items;
    }

    public Command() {
    }
    
    

    public List getItems() {
        return items;
    }

    public void setItems(List items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "";
    }
    
}
