/**
 * Eclipse Market Solutions LLC
 */
/*
 * PbcReloadingDialog.java
 *
 * @author Fang Bao
 * Created on July 17, 2010, 12:06:07 PM
 */

package com.eclipsemarkets.pbc.face;

import com.eclipsemarkets.global.CalendarGlobal;
import com.eclipsemarkets.global.NIOGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.face.talker.IPbcTalker;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;


/**
 *
 * @author Fang Bao
 */
public class PbcReloadingDialog extends javax.swing.JDialog 
{
    private static final long serialVersionUID = 1L;

    /**
     * Owner of this dialog
     */
    private final IPbcTalker talker;
    private static PbcReloadingDialog self;
     private final File loggingMessageFolder;

    static {
        self = null;
    }
    
    /**
     * Creates new form Reloader Dialog
     */
    private PbcReloadingDialog(final IPbcTalker talker)
    {   
        super(talker.getKernel().getPointBoxMainFrame(),false); //modalless
        initComponents();

        this.talker = talker;
        
        loggingMessageFolder = new File("records");
        if (!loggingMessageFolder.isDirectory()){
            loggingMessageFolder.mkdir();
        }
        jQuotes.setModel(new DefaultListModel());
        jQuotes.clearSelection();
        jQuotes.setSelectionModel(new DefaultListSelectionModel() {
			@Override
			public void setSelectionInterval(int index0, int index1) {
				if (super.isSelectedIndex(index0)) {
					super.removeSelectionInterval(index0, index1);
				} else {
					super.addSelectionInterval(index0, index1);
				}
			}
	});
        jQuotes.setCellRenderer(new QuoteListRenderer());

        jProgressBar.setIndeterminate(false);
        
        
        
        PointBoxConsoleProperties prop=PointBoxConsoleProperties.getSingleton();
        if(prop.retrieveReloaderOpenedOption(talker.getPointBoxLoginUser().getIMUniqueName())){
            jReloadCheckBox.setSelected(true);
        }else{
            jReloadCheckBox.setSelected(false);
        }
        
        String settings=prop.retrieveReloaderDefaultSettings(talker.getPointBoxLoginUser().getIMUniqueName());
        if(settings.equals(jLoadAllRadioBtn.getText())){
            jLoadAllRadioBtn.setSelected(true);
        }else if(settings.equals(jLoadLastestRadioBtn.getText())){
            jLoadLastestRadioBtn.setSelected(true);
        }else{
            jMannualRadioBtn.setSelected(true);
        }
        
        populateQuoteItems();
    }
    
  

    public static PbcReloadingDialog getSingletonInstance(IPbcTalker talker)
    {
        if(self==null){
            self= new PbcReloadingDialog(talker);

            self.initializeReloadingDialog();

            return self;
        }
        return self;
    }
    
    /**
     * initialize default values (before login)
     */
    public void initializeReloadingDialog(){
        if (SwingUtilities.isEventDispatchThread()){
            initializeReloadingDialogHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    initializeReloadingDialogHelper();
                }
            });
        }
    }

    private String getDateFromFilename(String filename) {
        String[] strArr=filename.split("_");
        String filedate="";
        if(strArr.length>=5){
            String month=strArr[1];
            String day=strArr[2];
            String year=strArr[3];
            filedate=month+"_"+day+"_"+year;
        }
        return filedate;
    }
    private void initializeReloadingDialogHelper(){
        setTitle("PBC Reloader");

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                hideFaceComponent();
            }
        });

        pack();
        setResizable(false);
        setModal(false);
        super.setVisible(false);
    }


    @Override
    public void setVisible(boolean value) {
        if (value){
            displayFaceComponent();
        }else{
            hideFaceComponent();
        }
    }

    public void close() {
        hideFaceComponent();
    }

    public void display() {
        displayFaceComponent();
    }

    

    public void displayFaceComponent() {
        if (SwingUtilities.isEventDispatchThread()){
            displayFaceComponentHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displayFaceComponentHelper();
                }
            });
        }
    }

    private void displayFaceComponentHelper(){
        jQuotes.setModel(new DefaultListModel());
        populateQuoteItems();
        if(jReloadCheckBox.isSelected()){
            jSettingPanel.setVisible(true);
        }else{
            jSettingPanel.setVisible(false);
        }
        pack();
        
        setLocation(SwingGlobal.getCenterPointOfParentWindow(talker.getPointBoxFrame(), this));

        super.setVisible(true);
    }
    
    

    public void hideFaceComponent() {
        if (SwingUtilities.isEventDispatchThread()){
            hideFaceComponentHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    hideFaceComponentHelper();
                }
            });
        }
    }

    private void hideFaceComponentHelper(){ 
        storeSettings();
        super.setVisible(false);
      
    }

    private void storeSettings(){
        PointBoxConsoleProperties prop = PointBoxConsoleProperties.getSingleton();
        String settings;
        if(jLoadAllRadioBtn.isSelected()){
            settings=jLoadAllRadioBtn.getText();
        }else if(jLoadLastestRadioBtn.isSelected()){
            settings=jLoadLastestRadioBtn.getText();
        }else{
            settings=jMannualRadioBtn.getText();
        }
        prop.storeReloaderDefaultSettings(settings, talker.getPointBoxLoginUser().getIMUniqueName());
        
        prop.storeReloaderOpenedOption(jReloadCheckBox.isSelected(), talker.getPointBoxLoginUser().getIMUniqueName());
    }
    

    public void releaseFaceComponent() {
        if (SwingUtilities.isEventDispatchThread()){
            dispose();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    dispose();
                }
            });
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jBasePanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jQuotes = new javax.swing.JList();
        jPanel5 = new javax.swing.JPanel();
        jLoad = new javax.swing.JButton();
        jDelete = new javax.swing.JButton();
        jProgressBar = new javax.swing.JProgressBar();
        jDeleteAll = new javax.swing.JButton();
        jReloadCheckBox = new javax.swing.JCheckBox();
        jSettingPanel = new javax.swing.JPanel();
        jLoadAllRadioBtn = new javax.swing.JRadioButton();
        jLoadLastestRadioBtn = new javax.swing.JRadioButton();
        jMannualRadioBtn = new javax.swing.JRadioButton();
        jKeepAllCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jBasePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Historical Messages", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 18))); // NOI18N
        jBasePanel.setName("jBasePanel"); // NOI18N

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Historical Logs:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, java.awt.Color.blue));
        jPanel4.setForeground(new java.awt.Color(0, 51, 204));
        jPanel4.setName("jPanel4"); // NOI18N

        jScrollPane3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jQuotes.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jQuotes.setForeground(new java.awt.Color(0, 153, 51));
        jQuotes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jQuotes.setName("jQuotes"); // NOI18N
        jScrollPane3.setViewportView(jQuotes);

        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setLayout(new java.awt.GridLayout(1, 2, 1, 0));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(314, 314, 314)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLoad.setText("Load");
        jLoad.setName("jLoad"); // NOI18N
        jLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLoadActionPerformed(evt);
            }
        });

        jDelete.setText("Delete");
        jDelete.setName("jDelete"); // NOI18N
        jDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDeleteActionPerformed(evt);
            }
        });

        jProgressBar.setName("jProgressBar"); // NOI18N

        jDeleteAll.setText("Delete All");
        jDeleteAll.setName("jDeleteAll"); // NOI18N
        jDeleteAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDeleteAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jBasePanelLayout = new javax.swing.GroupLayout(jBasePanel);
        jBasePanel.setLayout(jBasePanelLayout);
        jBasePanelLayout.setHorizontalGroup(
            jBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jBasePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jBasePanelLayout.createSequentialGroup()
                        .addComponent(jDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jDeleteAll)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                        .addComponent(jLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jBasePanelLayout.setVerticalGroup(
            jBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jBasePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addGroup(jBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jDelete)
                    .addComponent(jLoad)
                    .addComponent(jDeleteAll))
                .addContainerGap())
        );

        jPanel4.getAccessibleContext().setAccessibleName("Current AOL Connected Accounts:");

        jReloadCheckBox.setText("Load old messages at login");
        jReloadCheckBox.setName("jReloadCheckBox"); // NOI18N
        jReloadCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jReloadCheckBoxActionPerformed(evt);
            }
        });

        jSettingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Default Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, java.awt.Color.blue));
        jSettingPanel.setName("jSettingPanel"); // NOI18N

        buttonGroup1.add(jLoadAllRadioBtn);
        jLoadAllRadioBtn.setText("Load all messages");
        jLoadAllRadioBtn.setName("jLoadAllRadioBtn"); // NOI18N
        jLoadAllRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLoadAllRadioBtnActionPerformed(evt);
            }
        });

        buttonGroup1.add(jLoadLastestRadioBtn);
        jLoadLastestRadioBtn.setText("Load last hour");
        jLoadLastestRadioBtn.setName("jLoadLastestRadioBtn"); // NOI18N
        jLoadLastestRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLoadLastestRadioBtnActionPerformed(evt);
            }
        });

        buttonGroup1.add(jMannualRadioBtn);
        jMannualRadioBtn.setText("Choose messages to load");
        jMannualRadioBtn.setName("jMannualRadioBtn"); // NOI18N
        jMannualRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMannualRadioBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jSettingPanelLayout = new javax.swing.GroupLayout(jSettingPanel);
        jSettingPanel.setLayout(jSettingPanelLayout);
        jSettingPanelLayout.setHorizontalGroup(
            jSettingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jSettingPanelLayout.createSequentialGroup()
                .addGroup(jSettingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLoadAllRadioBtn)
                    .addComponent(jLoadLastestRadioBtn)
                    .addComponent(jMannualRadioBtn))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jSettingPanelLayout.setVerticalGroup(
            jSettingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jSettingPanelLayout.createSequentialGroup()
                .addComponent(jLoadAllRadioBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLoadLastestRadioBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jMannualRadioBtn))
        );

        jKeepAllCheckBox.setSelected(true);
        jKeepAllCheckBox.setText("Keeps all the logs even if it is older than 1 month");
        jKeepAllCheckBox.setToolTipText("If this is unchecked, PBC only keeps historical message logs in the past 4 weeks. Other too old logs will be erased.");
        jKeepAllCheckBox.setName("jKeepAllCheckBox"); // NOI18N
        jKeepAllCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jKeepAllCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jReloadCheckBox)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jKeepAllCheckBox)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jBasePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jSettingPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jBasePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jKeepAllCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jReloadCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addComponent(jSettingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLoadActionPerformed
        Object[] objs = jQuotes.getSelectedValues(); 
        if (objs.length <= 0) {
            JOptionPane.showMessageDialog(this, "Please select the quote data which you want to load!");
            return;
        }
        loaderInEDT();
        setVisible(false);
    }//GEN-LAST:event_jLoadActionPerformed

    private void jReloadCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jReloadCheckBoxActionPerformed
        if(jReloadCheckBox.isSelected())
            jSettingPanel.setVisible(true);
        else
            jSettingPanel.setVisible(false);
        pack();
    }//GEN-LAST:event_jReloadCheckBoxActionPerformed

    private void jDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDeleteActionPerformed
        Object[] objs = jQuotes.getSelectedValues();
        if (objs.length <= 0) {
            JOptionPane.showMessageDialog(this, "Please select the quote data which you want to delete!");
            return;
        }
        int[] indices = jQuotes.getSelectedIndices();
        for (int n : indices) {
            String filename=((QuoteDataWrapper)jQuotes.getModel().getElementAt(n)).filename;
            String filedate = getDateFromFilename(filename);
            if(CalendarGlobal.getCurrentMMddyyyy("_").equalsIgnoreCase(filedate)){
                JOptionPane.showMessageDialog(this, "Today's logs can not be deleted. Please try again. ");
                selectDefault();
                return;
            }
        }
        if (JOptionPane.showConfirmDialog(this,
                "Are you sure to delete these data?",
                "Confirm", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION) {
            deleteQuoteData(objs);
        }
        selectDefault();
    }//GEN-LAST:event_jDeleteActionPerformed

    private void jDeleteAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDeleteAllActionPerformed
         if (JOptionPane.showConfirmDialog(this,
                "Are you sure to delete all logs except today's logs?",
                "Confirm", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION) {
               
            List<Object> collection=new ArrayList<Object>();
            for (int n=0;n<jQuotes.getModel().getSize();n++) {
                String filename=((QuoteDataWrapper)jQuotes.getModel().getElementAt(n)).filename;
                String filedate=getDateFromFilename(filename);
                if(!CalendarGlobal.getCurrentMMddyyyy("_").equalsIgnoreCase(filedate)){
                     collection.add(jQuotes.getModel().getElementAt(n));
                }
            }
             
            Object[] objs=collection.toArray(); 
            deleteQuoteData(objs);
         }
         selectDefault();
    }//GEN-LAST:event_jDeleteAllActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        this.setVisible(false);
    }//GEN-LAST:event_formWindowClosing

    public void selectAll(){
        jQuotes.clearSelection();
        for(int i=0;i<jQuotes.getModel().getSize();i++){
             jQuotes.setSelectedIndex(i);
        }
    }
    
    public void selectDefault(){
        jQuotes.clearSelection();
        jQuotes.setSelectedIndex(jQuotes.getModel().getSize()-1);
    }
    
    private void jLoadAllRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLoadAllRadioBtnActionPerformed
        selectAll();
    }//GEN-LAST:event_jLoadAllRadioBtnActionPerformed

    private void jLoadLastestRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLoadLastestRadioBtnActionPerformed
        selectDefault();
    }//GEN-LAST:event_jLoadLastestRadioBtnActionPerformed

    private void jMannualRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMannualRadioBtnActionPerformed
        selectDefault();
    }//GEN-LAST:event_jMannualRadioBtnActionPerformed

    private void jKeepAllCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jKeepAllCheckBoxActionPerformed
        boolean refreshModel = false;
        if (!jKeepAllCheckBox.isSelected()){
            if (JOptionPane.showConfirmDialog(this, 
                                              "Are you sure to erase all the historical logs which are older than 1 month? "
                                            + "This operation cannot be rollback.",
                                              "Warning",
                                              JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            {
                refreshModel = true;
            }else{
                jKeepAllCheckBox.setSelected(true);
            }
        }
        if (refreshModel){
            jQuotes.setModel(new DefaultListModel());
            populateQuoteItems();
        }
    }//GEN-LAST:event_jKeepAllCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel jBasePanel;
    private javax.swing.JButton jDelete;
    private javax.swing.JButton jDeleteAll;
    private javax.swing.JCheckBox jKeepAllCheckBox;
    private javax.swing.JButton jLoad;
    private javax.swing.JRadioButton jLoadAllRadioBtn;
    private javax.swing.JRadioButton jLoadLastestRadioBtn;
    private javax.swing.JRadioButton jMannualRadioBtn;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JList jQuotes;
    private javax.swing.JCheckBox jReloadCheckBox;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPanel jSettingPanel;
    // End of variables declaration//GEN-END:variables

    private void populateQuoteItems() {
        (new SwingWorker<ListModel, Void>(){
            @Override
            protected ListModel doInBackground() throws Exception {
                return processHistoricalLogFiles();
            }

            @Override
            protected void done() {
                try {
                    jQuotes.setModel(get());
                } catch (InterruptedException ex) {
                    jQuotes.setModel(new DefaultListModel());
                } catch (ExecutionException ex) {
                    jQuotes.setModel(new DefaultListModel());
                }
                jQuotes.clearSelection();
                jQuotes.setSelectedIndex(jQuotes.getModel().getSize()-1);
                jQuotes.ensureIndexIsVisible(jQuotes.getSelectedIndex());
            }
        }).execute();
    }
    
    private synchronized DefaultListModel processHistoricalLogFiles(){
        DefaultListModel model = new DefaultListModel();
        if ((loggingMessageFolder != null) && (loggingMessageFolder.isDirectory())){
             File[] files = loggingMessageFolder.listFiles();
             Arrays.sort(files, new Comparator<File>(){
                 @Override
                 public int compare(File o1, File o2) {
                     if ((o1 != null) && (o2 != null)){
                         return (new Long(o1.lastModified())).compareTo((new Long(o2.lastModified())));
                     }else{
                         return 0;
                     }
                 }
             });
             ArrayList<File> oldFiles = new ArrayList<File>();
             long cutoffTimepoint = 0;
             if (jKeepAllCheckBox.isSelected()){
                //only keep logs in the previous month
                GregorianCalendar cutoff = new GregorianCalendar();
                cutoff.add(Calendar.MONTH, -1);
                cutoffTimepoint = cutoff.getTimeInMillis();
             }
             for (int i=0; i<files.length; i++) {
                 String filename=files[i].getName();
                 String[] strArr=filename.split("_");
                 if(talker.getKernel().getPointBoxLoginUser().getIMScreenName().equals(strArr[0])){
                     if (files[i].lastModified() > cutoffTimepoint){
                       QuoteDataWrapper wrapper=new QuoteDataWrapper(files[i]);
                       model.addElement(wrapper);
                     }else{
                         oldFiles.add(files[i]);
                     }
                 }
             }
             for (File oldFile : oldFiles){
               try {
                   NIOGlobal.deleteFile(oldFile);
               } catch (IOException ex) {
                   Logger.getLogger(PbcReloadingDialog.class.getName()).log(Level.SEVERE, null, ex);
               }
             }
        }
        return model;
    }
    
    private void loaderHelper() {
        jProgressBar.setIndeterminate(true);
        jDelete.setEnabled(false);
        jDeleteAll.setEnabled(false);
        jLoad.setEnabled(false);
        jReloadCheckBox.setEnabled(false);
        jLoadAllRadioBtn.setEnabled(false);
        jLoadLastestRadioBtn.setEnabled(false);
        jMannualRadioBtn.setEnabled(false);
        
        Object[] objs=jQuotes.getSelectedValues();
        List<String> filepathes=new ArrayList<String>();
        
        for(Object obj:objs){
            QuoteDataWrapper wrapper=(QuoteDataWrapper) obj;
            filepathes.add(wrapper.file.getAbsolutePath());
        }
        talker.getKernel().loadQuoteMessages(filepathes);
        
    }

    public void loaderInEDT() {
        if(SwingUtilities.isEventDispatchThread()){
            loaderHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                   loaderHelper();
                }
            });
        }
    }

   public void loadDone(){
        loaderDoneInEDT();
   }


     private void loaderDoneInEDT() {
        if(SwingUtilities.isEventDispatchThread()){
            loaderDoneHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                   loaderDoneHelper();
                }
            });
        }
    }
    
    private void loaderDoneHelper() {
        selectDefault();
        jProgressBar.setIndeterminate(false);
        jDelete.setEnabled(true);
        jDeleteAll.setEnabled(true);
        jLoad.setEnabled(true);
        jReloadCheckBox.setEnabled(true);
        jLoadAllRadioBtn.setEnabled(true);
        jLoadLastestRadioBtn.setEnabled(true);
        jMannualRadioBtn.setEnabled(true);
         this.setVisible(false);
        JOptionPane.showMessageDialog(this, "Loading completed!","info",JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteQuoteData(final Object[] objs) {
       if(objs.length>0){
           Thread deleter=new Thread(new Runnable() {
                @Override
                public void run() {
                     for(Object obj:objs){
                        QuoteDataWrapper wrapper=(QuoteDataWrapper)obj;
                        File file=wrapper.file;
                        file.delete();
                     }
                     populateQuoteItems();
                }
            });
            deleter.start();
       }
    }

    public boolean isEmptyForLoginUser(){
        boolean empty = true;
        if ((loggingMessageFolder != null) && (loggingMessageFolder.isDirectory())){
             File[] files = loggingMessageFolder.listFiles();
             if ((files != null) && (files.length > 0)){
                for (int i=0; i<files.length; i++) {
                    String filename=files[i].getName();
                    String[] strArr=filename.split("_");
                    if(talker.getKernel().getPointBoxLoginUser().getIMScreenName().equals(strArr[0])){
                        empty = false;
                        break;
                    }
                }//for
             }
        }
        return empty;
    }
    
    class QuoteListRenderer extends DefaultListCellRenderer {

        Color originalLabelForeground;
        QuoteListRenderer() {
            originalLabelForeground = this.getBackground();
        }
        
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            QuoteDataWrapper  wrapper = (QuoteDataWrapper) value;
            String filename=wrapper.filename;
            String[] strArr=filename.split("_");
            if(strArr.length>=5){
                String month=strArr[1];
                String day=strArr[2];
                String year=strArr[3];
                String hour=strArr[4];
                filename="  "+month+"/"+day+"/"+year+" - "+hour+":00 logged";
            }
            setIcon(talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings().getArchiveIcon());
            setText(filename);
            setFont(SwingGlobal.getLabelFont());
            
            if(isSelected){
                setBackground(Color.YELLOW);
            }else{
                setBackground(originalLabelForeground);
            }
            return this;
        }
    }

   class QuoteDataWrapper{
       private String filename;
       private File file;
       
       public QuoteDataWrapper(File file){
           this.file=file;
           this.filename=file.getName();
       }
       
        @Override
        public String toString(){
            String[] strArr=filename.split("_");
            if(strArr.length>=5){
                String month=strArr[1];
                String day=strArr[2];
                String year=strArr[3];
                String hour=strArr[4];
                return month+"/"+day+"/"+year+" - "+hour+":00 logged";
            }
            return filename;
        }
   }  
}
