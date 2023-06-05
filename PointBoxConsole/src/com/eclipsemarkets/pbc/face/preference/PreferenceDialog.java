/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.face.IPbcFace;
import com.eclipsemarkets.web.pbc.PbcPricingModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * PreferenceDialog
 * <P>
 * {Insert class description here}
 * <P>
 * @author Zhijun Zhang
 * Created on Mar 24, 2011 at 12:00:23 PM
 */
public abstract class PreferenceDialog extends JDialog{
    private static final long serialVersionUID = 1L;
    
    private IPreferencePanel preferencePanel;
    
    private static final Logger logger = Logger.getLogger(PreferenceDialog.class.getName());

    private IPbcFace face;

    public PreferenceDialog(IPbcFace face, IPreferencePanel preferencePanel) {
        super(face.getPointBoxMainFrame(), true);
        this.face = face;
        this.preferencePanel = preferencePanel;
        
        setTitle("Settings");
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(preferencePanel.getBasePanel(), BorderLayout.CENTER);
        getContentPane().add(contentPanel);
        
        pack();
        
        Dimension size = contentPanel.getLayout().preferredLayoutSize(contentPanel);
        setSize(size.width, size.height);

        setLocation(SwingGlobal.getCenterPointOfParentWindow(face.getPointBoxMainFrame(), this));
        
        Window win = SwingUtilities.getWindowAncestor(contentPanel);
        if (win != null) {
            win.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    PreferenceDialog.this.preferencePanel.updateSettings();
                }
            });  
        }
        
    }

    IPbcFace getFace() {
        return face;
    }
    
//    abstract IPreferencePanel getPreferencePanel(IPbcFace face);
    
    /**
     * download all the curve files of every possible code from the server-side
     */
    public void downloadPricingRuntimeCurveFiles(boolean displayCompleteMessage){
        if (preferencePanel instanceof ICurvePreferencePanel){
            ((ICurvePreferencePanel)preferencePanel).downloadPricingRuntimeCurveFiles(displayCompleteMessage);
        }
    }
    public void downloadPricingRuntimeCurveFiles(PbcPricingModel aPbcPricingModel, boolean displayCompleteMessage){
        if (preferencePanel instanceof ICurvePreferencePanel){
            ((ICurvePreferencePanel)preferencePanel).downloadPricingRuntimeCurveFiles(aPbcPricingModel, displayCompleteMessage);
        }
    }

    /**
     * upload all the curve files of every possible code from the server-side
     */
    public void uploadPricingRuntimeCurveFiles(){
        if (preferencePanel instanceof ICurvePreferencePanel){
            ((ICurvePreferencePanel)preferencePanel).uploadPricingRuntimeCurveFiles(); 
        }
    }
        
    public void displayPreferenceDialog() {
        if (SwingUtilities.isEventDispatchThread()){
            displayPreferenceDialogHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displayPreferenceDialogHelper();
                }
            });
        }
    }
    private void displayPreferenceDialogHelper() {
        preferencePanel.populateSettings();
        //preferencePanel.expandPreferencePanel();
        super.setVisible(true);
    }
    
    @Override
    public void setVisible(boolean value){
        if (value){
            displayPreferenceDialogHelper();
        }else{
            super.setVisible(false);
        }
    }

    /**
     * @return the preferencePanel
     */
    public IPreferencePanel getPreferencePanel() {
        return preferencePanel;
    }
}//PreferenceDialog

