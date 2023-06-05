/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.preference;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author Zhijun Zhang, date & time: May 13, 2013 - 12:19:39 PM
 */
public class JavaFxColorChooser {

    private Color selectedColor = null;
    private Color tempSelectedColor = null;
    private final JFXPanel fxPanel;
    public JavaFxColorChooser() {
        fxPanel = createJFXPanel();
    }
    
    /**
     * 
     * @param caller - who called this method to select a color for some purpose; 
     * this cannot be NULL. If it is NULL, defaultColor will be returned
     * @param title
     * @param defaultColor. If it is NULL, it (NULL) will be returned
     * @return 
     */
    public Color selectColor(final JDialog caller, final String title, final Color defaultColor) {
        if ((caller == null) || (defaultColor == null)){
            return defaultColor;
        }
        selectedColor = null;
        tempSelectedColor = defaultColor;
        if (SwingUtilities.isEventDispatchThread()){
            selectColorHelper(caller, title, defaultColor);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    selectColorHelper(caller, title, defaultColor);
                }
            });
            
        }
        while (selectedColor == null){
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                break;
            }
        }
        return selectedColor;
    }
    
    private void selectColorHelper(final JDialog caller, final String title, final Color defaultColor) {
        final JDialog colorPickerDialog = new JDialog(caller, title, true);
        
        colorPickerDialog.add(fxPanel);
        colorPickerDialog.setSize(300, 80);
        colorPickerDialog.setLocation(caller.getLocation());
        colorPickerDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        colorPickerDialog.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                if (selectedColor == null){
                    selectedColor = tempSelectedColor;
                }
                colorPickerDialog.dispose();
            }
        });
        colorPickerDialog.setVisible(true);
    }

    private JFXPanel createJFXPanel() {
        final JFXPanel aFxPanel = new JFXPanel();
        final HBox box = new HBox(20);
        box.setPadding(new Insets(5, 5, 5, 5));
        final ColorPicker colorPicker = new ColorPicker();
        final Text text = new Text("Select color and close it to go back.");
        text.setFill(colorPicker.getValue());
        colorPicker.setOnAction(new EventHandler() {
            @Override
            public void handle(Event t) {
                javafx.scene.paint.Color fxColor = colorPicker.getValue();
                if (fxColor != null){
                    int r = (int)(fxColor.getRed()*255);
                    int g = (int)(fxColor.getGreen()*255);
                    int b = (int)(fxColor.getBlue()*255);
                    tempSelectedColor = new Color(r, g, b);
                }
                text.setFill(fxColor);
            }
        });
        box.getChildren().addAll(colorPicker, text);
        
        Platform.setImplicitExit(false);
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Scene scene = new Scene(box);
                aFxPanel.setScene(scene);
            }
        });
        return aFxPanel;
    }
}
