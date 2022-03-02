package com.github.videohandler4j.gui.imp;

import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.github.videohandler4j.gui.ICutManager;

import net.miginfocom.swing.MigLayout;

public class CutManagerPanel extends JPanel implements ICutManager {
  
  public CutManagerPanel() {
    setLayout(new MigLayout());
    add(new CutPanel(), "wrap");
    add(new JSeparator(), "wrap, pushx, growx");
    add(new CutPanel(), "wrap");
    add(new JSeparator(), "wrap, pushx, growx");
    add(new CutPanel(), "wrap");
    add(new JSeparator(), "wrap, pushx, growx");
    add(new CutPanel(), "wrap");
    add(new JSeparator(), "wrap, pushx, growx");
    add(new CutPanel(), "wrap");
    add(new JSeparator(), "wrap, pushx, growx");
    add(new CutPanel(), "wrap");
    add(new JSeparator(), "wrap, pushx, growx");
    add(new CutPanel(), "wrap");
    add(new JSeparator(), "wrap, pushx, growx");
    add(new CutPanel(), "wrap");
    add(new JSeparator(), "wrap, pushx, growx");
    add(new CutPanel(), "wrap");
    add(new JSeparator(), "wrap, pushx, growx");
    add(new CutPanel(), "wrap");
    add(new JSeparator(), "wrap, pushx, growx");
    add(new CutPanel(), "wrap");
    add(new JSeparator(), "wrap, pushx, growx");
    add(new CutPanel(), "wrap");
    add(new JSeparator(), "wrap, pushx, growx");
    add(new CutPanel(), "wrap");
    add(new JSeparator(), "wrap, pushx, growx");
    add(new CutPanel(), "wrap");
    add(new JSeparator(), "wrap, pushx, growx");
    add(new CutPanel(), "wrap");
    add(new JSeparator(), "wrap, pushx, growx");
    add(new CutPanel(), "wrap");
    add(new JSeparator(), "wrap, pushx, growx");
  }
}
