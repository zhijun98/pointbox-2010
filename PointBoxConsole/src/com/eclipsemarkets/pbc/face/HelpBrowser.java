/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face;

/**
 *
 * @author Andal
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

class HelpBrowser extends JFrame implements HyperlinkListener,
                                               ActionListener {
    private static final long serialVersionUID = 1L;
  private JButton homeButton;
  private JButton backButton;
  private JButton forwardButton;
  private JTextField urlField;
  private JEditorPane htmlPane;
  private String initialURL;
  private ArrayList visitedPages;
  private int currentVisitedPage;

  HelpBrowser(String aURL) {
    super("PointBox Help Browser");
    this.initialURL = aURL;//"file://" + aURL;

    JPanel topPanel = new JPanel();
    topPanel.setBackground(Color.lightGray);
    homeButton = new JButton("Home");
    homeButton.addActionListener(this);
    backButton = new JButton("Back");
    backButton.addActionListener(this);
    forwardButton = new JButton("Forward");
    forwardButton.addActionListener(this);
    
    JLabel urlLabel = new JLabel("URL:");
    urlField = new JTextField(30);
    urlField.setText(initialURL);
    urlField.addActionListener(this);
    topPanel.add(backButton);
    topPanel.add(forwardButton);
    topPanel.add(homeButton);
    topPanel.add(urlLabel);
    topPanel.add(urlField);

    //urlLabel.setVisible(false);
    //urlField.setVisible(false);

    getContentPane().add(topPanel, BorderLayout.NORTH);
    visitedPages = new ArrayList();
    visitedPages.add(initialURL);
    currentVisitedPage = 0;

    try {
        htmlPane = new JEditorPane(initialURL);
        htmlPane.setEditable(false);
        htmlPane.addHyperlinkListener(this);
        JScrollPane scrollPane = new JScrollPane(htmlPane);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    } catch(IOException ioe) {
       ioe.printStackTrace();
      }
    Dimension screenSize = getToolkit().getScreenSize();
    int width = screenSize.width * 8 / 10;
    int height = screenSize.height * 8 / 10;
    setBounds(width/8, height/8, width, height);
    
    setSize(new Dimension(600, 400));
    setVisible(true);
  }

  public void actionPerformed(ActionEvent event) {
    String url="";
    if (event.getSource() == urlField){
      url = urlField.getText();
    }
    else if(event.getSource() == backButton){
        if(currentVisitedPage != 0){
            url = (String)visitedPages.get(currentVisitedPage - 1);
            currentVisitedPage--;
        }
        else
            return;
    }
    else if(event.getSource() == forwardButton){
        if(currentVisitedPage != visitedPages.size()-1){
            url = (String)visitedPages.get(currentVisitedPage + 1);
            currentVisitedPage++;
        }
        else
            return;
    }
    else  // Clicked "home" button instead of entering URL
      url = initialURL;
    try {
      htmlPane.setPage(new URL(url));
      urlField.setText(url);
    } catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void hyperlinkUpdate(HyperlinkEvent event) {
    if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      try {
        htmlPane.setPage(event.getURL());
        urlField.setText(event.getURL().toExternalForm());
        
        if(currentVisitedPage < visitedPages.size()-1){
            for(int i = visitedPages.size()-1; i > currentVisitedPage; i--){
                visitedPages.remove(i);
            }
        }
        visitedPages.add(event.getURL().toExternalForm());
        currentVisitedPage++;
      } catch(IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }
}
