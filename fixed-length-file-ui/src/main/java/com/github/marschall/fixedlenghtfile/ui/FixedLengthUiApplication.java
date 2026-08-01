package com.github.marschall.fixedlenghtfile.ui;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

public class FixedLengthUiApplication {

  private static JPanel createPanel() {
    JPanel panel = new JPanel(new GridLayout(1,0));

    JTable table = new JTable();
    table.setPreferredScrollableViewportSize(new Dimension(500, 70));
    table.setFillsViewportHeight(true);

    JScrollPane scrollPane = new JScrollPane(table);
    panel.add(scrollPane);

    return panel;
  }

  private static void createAndShowGUI() {
    JFrame frame = new JFrame("FixedLength File");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel newContentPane = createPanel();
    newContentPane.setOpaque(true);
    frame.setContentPane(newContentPane);

    frame.pack();
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(FixedLengthUiApplication::createAndShowGUI);
  }

}
