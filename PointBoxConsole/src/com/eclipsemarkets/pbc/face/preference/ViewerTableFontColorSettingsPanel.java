/**
 * Eclipse Market Solutions LLC
 *
 * ViewerTableFontColorSettingsPanel.java
 *
 * @author Zhijun Zhang
 * Created on May 23, 2010, 3:13:15 PM
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.face.ViewerColumnSettingsChangedEvent;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.PbcText;
import com.eclipsemarkets.pbc.PbcTextKey;
import com.eclipsemarkets.pbc.face.IPbcFace;
import com.eclipsemarkets.pbc.face.viewer.model.ViewerColumnIdentifier;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.l2fprod.common.swing.JFontChooser;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author Zhijun Zhang
 */
class ViewerTableFontColorSettingsPanel extends javax.swing.JPanel implements IPreferenceComponentPanel
{
    private static final long serialVersionUID = 1L;

    private final IPbcFace face;
    private final String viewerUniqueTabName;
    
    //private JavaFxColorChooser aJavaFxColorChooser = null;;

    ViewerTableFontColorSettingsPanel(IPbcFace face, String viewerUniqueTabName) {
        initComponents();

        this.face = face;
        this.viewerUniqueTabName = viewerUniqueTabName;
        tabNameLabel.setVisible(false);
        jViewerTabName.setVisible(false);
    }
    
    private IPbcRuntime getPointBoxConsoleRuntime(){
        return face.getKernel().getPointBoxConsoleRuntime();
    }

    @Override
    public final void populateSettings() {
        if (SwingUtilities.isEventDispatchThread()){
            populateSettingsHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    populateSettingsHelper();
                }
            });
        }
    }
    
    private void populateSettingsHelper(){
        applyViewerTableSettingsHelper();
    }

    @Override
    public void updateSettings() {
        if (SwingUtilities.isEventDispatchThread()){
            updateSettingsHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    updateSettingsHelper();
                }
            });
        }
    }
    private void updateSettingsHelper(){
    }

    private void applyViewerTableSettingsHelper() {
        jViewerTabName.setText(this.viewerUniqueTabName);
        applyViewerTableColoringFontSettings();
    }

    private void applyViewerTableColoringFontSettings(){
        if (getPointBoxConsoleRuntime() == null){
            return;
        }
        applyGeneralFont(getPointBoxConsoleRuntime().getViewerGeneralFont(viewerUniqueTabName));
        jGeneralSample.setForeground(getPointBoxConsoleRuntime().getViewerGeneralColor(viewerUniqueTabName));
        jRqc.setForeground(getPointBoxConsoleRuntime().getQtFgColor(viewerUniqueTabName));
        jRqc.setBackground(getPointBoxConsoleRuntime().getQtBgColor(viewerUniqueTabName));
        jPb.setForeground(getPointBoxConsoleRuntime().getPb_FgColor(viewerUniqueTabName));
        jPb.setBackground(getPointBoxConsoleRuntime().getPb_BgColor(viewerUniqueTabName));
        jPbimQt.setForeground(getPointBoxConsoleRuntime().getPbimQtFgColor(viewerUniqueTabName));
        jPbimQt.setBackground(getPointBoxConsoleRuntime().getPbimQtBgColor(viewerUniqueTabName));
        jPa.setForeground(getPointBoxConsoleRuntime().getPa_FgColor(viewerUniqueTabName));
        jPa.setBackground(getPointBoxConsoleRuntime().getPa_BgColor(viewerUniqueTabName));
        jSkippedQt.setForeground(getPointBoxConsoleRuntime().getSkippedQtFgColor(viewerUniqueTabName));
        jSkippedQt.setBackground(getPointBoxConsoleRuntime().getSkippedQtBgColor(viewerUniqueTabName));
        jBpa.setForeground(getPointBoxConsoleRuntime().getBpa_FgColor(viewerUniqueTabName));
        jBpa.setBackground(getPointBoxConsoleRuntime().getBpa_BgColor(viewerUniqueTabName));
        jMsg.setForeground(getPointBoxConsoleRuntime().getMsgFgColor(viewerUniqueTabName));
        jMsg.setBackground(getPointBoxConsoleRuntime().getMsgBgColor(viewerUniqueTabName));
        jOutgoingQt.setForeground(getPointBoxConsoleRuntime().getOutgoingForeground(viewerUniqueTabName));
        jOutgoingQt.setBackground(getPointBoxConsoleRuntime().getOutgoingBackground(viewerUniqueTabName));
        jLatestRow.setForeground(getPointBoxConsoleRuntime().getLatestRowForeground(viewerUniqueTabName));
        jLatestRow.setBackground(getPointBoxConsoleRuntime().getLatestRowBackground(viewerUniqueTabName));
        jSelectedRow.setForeground(getPointBoxConsoleRuntime().getSelectedRowForeground(viewerUniqueTabName));
        jSelectedRow.setBackground(getPointBoxConsoleRuntime().getSelectedRowBackground(viewerUniqueTabName));
    }

    private void setCheckBox(JCheckBox checkBox, ViewerColumnIdentifier aViewerColumnIdentifier) {
        if (getPointBoxConsoleRuntime().isViewerColumnVisible(viewerUniqueTabName, aViewerColumnIdentifier)){
            checkBox.setSelected(true);
        }else{
            checkBox.setSelected(false);
        }
        if(checkBox.getItemListeners().length<=0)   //prevent to repeat adding same listener.
            checkBox.addItemListener(new ViewerColumnCheckBoxListener(viewerUniqueTabName, aViewerColumnIdentifier));
    }

    private class ViewerColumnCheckBoxListener implements ItemListener{
        private String wiewerUniqueTabName;
        private ViewerColumnIdentifier viewerColumnIdentifier;
        ViewerColumnCheckBoxListener(String wiewerUniqueTabName, ViewerColumnIdentifier colRecord) {
            this.viewerColumnIdentifier = colRecord;
            this.wiewerUniqueTabName = wiewerUniqueTabName;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            try{
                boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
                if (e.getStateChange() == ItemEvent.SELECTED){
                    //if (!getPointBoxConsoleRuntime().isViewerColumnVisible(viewerUniqueTabName, viewerColumnIdentifier)){  
                    //Notice:  If checkbox was selected, it must update to make column visible. So above judgement is unnecessary.
                    //What's important, above judgement will make reset to the function of default settings useless. Because after resetting, it will reset the visible value of every column.
                    if (isViewerSettingsForAllViewers){
                        /**
                         * every viewer has the same settings. thus, use the first tab's settings
                         */
                        for(String viewerUniqueTabName:face.getPointBoxViewer().getTabStorage().keySet()){
                            getPointBoxConsoleRuntime().setViewerColumnVisible(viewerUniqueTabName, viewerColumnIdentifier, true);
                        }
                    }else{
                        getPointBoxConsoleRuntime().setViewerColumnVisible(viewerUniqueTabName, viewerColumnIdentifier, true);
                    }
                    face.getKernel().raisePointBoxEvent(new ViewerColumnSettingsChangedEvent(PointBoxEventTarget.PbcFace,
                                                                 wiewerUniqueTabName));
                }else if (e.getStateChange() == ItemEvent.DESELECTED){
                    if (isViewerSettingsForAllViewers){
                        /**
                         * every viewer has the same settings. thus, use the first tab's settings
                         */
                        for(String viewerUniqueTabName:face.getPointBoxViewer().getTabStorage().keySet()){
                            getPointBoxConsoleRuntime().setViewerColumnVisible(viewerUniqueTabName, viewerColumnIdentifier, false);
                        }
                    }else{
                        getPointBoxConsoleRuntime().setViewerColumnVisible(viewerUniqueTabName, viewerColumnIdentifier, false);
                    }
                    face.getKernel().raisePointBoxEvent(new ViewerColumnSettingsChangedEvent(PointBoxEventTarget.PbcFace,
                                                                 wiewerUniqueTabName));
                }
            }catch (Exception ex){
            }
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

        jPanel2 = new javax.swing.JPanel();
        tabNameLabel = new javax.swing.JLabel();
        jViewerTabName = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jGeneralFont = new javax.swing.JButton();
        jGeneralColor = new javax.swing.JButton();
        jResetFontColors = new javax.swing.JButton();
        jGeneralSample = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jMsg = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jMsg_FG = new javax.swing.JButton();
        jMsg_BG = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jRqc = new javax.swing.JTextField();
        jPanel9 = new javax.swing.JPanel();
        jRqc_FG = new javax.swing.JButton();
        jRqc_BG = new javax.swing.JButton();
        jSkippedQt = new javax.swing.JTextField();
        jPanel14 = new javax.swing.JPanel();
        jPbim_FG = new javax.swing.JButton();
        jPbim_BG = new javax.swing.JButton();
        jPbimQt = new javax.swing.JTextField();
        jPanel15 = new javax.swing.JPanel();
        jSkippedQt_FG = new javax.swing.JButton();
        jSkippedQt_BG = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        jOutgoingQt = new javax.swing.JTextField();
        jPanel13 = new javax.swing.JPanel();
        jOutgoingQt_FG = new javax.swing.JButton();
        jOutgoingQt_BG = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLatestRow = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        jLatestRow_FG = new javax.swing.JButton();
        jLatestRow_BG = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jSelectedRow = new javax.swing.JTextField();
        jPanel11 = new javax.swing.JPanel();
        jSelectedRow_FG = new javax.swing.JButton();
        jSelectedRow_BG = new javax.swing.JButton();
        jPanel16 = new javax.swing.JPanel();
        jPb = new javax.swing.JTextField();
        jPanel17 = new javax.swing.JPanel();
        jPb_FG = new javax.swing.JButton();
        jPb_BG = new javax.swing.JButton();
        jBpa = new javax.swing.JTextField();
        jPanel18 = new javax.swing.JPanel();
        jPa_FG = new javax.swing.JButton();
        jPa_BG = new javax.swing.JButton();
        jPa = new javax.swing.JTextField();
        jPanel19 = new javax.swing.JPanel();
        jBpa_FG = new javax.swing.JButton();
        jBpa_BG = new javax.swing.JButton();

        setRequestFocusEnabled(false);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Aggregator General Settings: "));
        jPanel2.setName("jPanel2"); // NOI18N

        tabNameLabel.setText("Tab Name:");
        tabNameLabel.setName("tabNameLabel"); // NOI18N

        jViewerTabName.setName("jViewerTabName"); // NOI18N
        jViewerTabName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jViewerTabNameActionPerformed(evt);
            }
        });

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(new java.awt.GridLayout(1, 3, 3, 1));

        jGeneralFont.setText("Font");
        jGeneralFont.setName("jGeneralFont"); // NOI18N
        jGeneralFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jGeneralFontActionPerformed(evt);
            }
        });
        jPanel3.add(jGeneralFont);

        jGeneralColor.setText("Font's Color");
        jGeneralColor.setName("jGeneralColor"); // NOI18N
        jGeneralColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jGeneralColorActionPerformed(evt);
            }
        });
        jPanel3.add(jGeneralColor);

        jResetFontColors.setText("Default");
        jResetFontColors.setName("jResetFontColors"); // NOI18N
        jResetFontColors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jResetFontColorsActionPerformed(evt);
            }
        });
        jPanel3.add(jResetFontColors);

        jGeneralSample.setEditable(false);
        jGeneralSample.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jGeneralSample.setText("General Settings");
        jGeneralSample.setName("jGeneralSample"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(tabNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jViewerTabName, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE))
                    .addComponent(jGeneralSample, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(tabNameLabel)
                    .addComponent(jViewerTabName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jGeneralSample, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Incoming Message Settings: "));
        jPanel4.setName("jPanel4"); // NOI18N

        jMsg.setEditable(false);
        jMsg.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jMsg.setText("Message Column");
        jMsg.setName("jMsg"); // NOI18N

        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setLayout(new java.awt.GridLayout(1, 2, 3, 1));

        jMsg_FG.setText("FG Color");
        jMsg_FG.setName("jMsg_FG"); // NOI18N
        jMsg_FG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMsg_FGActionPerformed(evt);
            }
        });
        jPanel5.add(jMsg_FG);

        jMsg_BG.setText("BG Color");
        jMsg_BG.setName("jMsg_BG"); // NOI18N
        jMsg_BG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMsg_BGActionPerformed(evt);
            }
        });
        jPanel5.add(jMsg_BG);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jMsg, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jMsg, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Incoming Quote Settings: "));
        jPanel8.setName("jPanel8"); // NOI18N

        jRqc.setEditable(false);
        jRqc.setBackground(new java.awt.Color(0, 0, 153));
        jRqc.setForeground(new java.awt.Color(255, 255, 255));
        jRqc.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jRqc.setText("Regular Quote Column");
        jRqc.setName("jRqc"); // NOI18N

        jPanel9.setName("jPanel9"); // NOI18N
        jPanel9.setLayout(new java.awt.GridLayout(1, 2, 3, 1));

        jRqc_FG.setText("FG Color");
        jRqc_FG.setName("jRqc_FG"); // NOI18N
        jRqc_FG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRqc_FGActionPerformed(evt);
            }
        });
        jPanel9.add(jRqc_FG);

        jRqc_BG.setText("BG Color");
        jRqc_BG.setName("jRqc_BG"); // NOI18N
        jRqc_BG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRqc_BGActionPerformed(evt);
            }
        });
        jPanel9.add(jRqc_BG);

        jSkippedQt.setEditable(false);
        jSkippedQt.setBackground(new java.awt.Color(255, 255, 153));
        jSkippedQt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jSkippedQt.setText("Skipped Quote's Column");
        jSkippedQt.setName("jSkippedQt"); // NOI18N

        jPanel14.setName("jPanel14"); // NOI18N
        jPanel14.setLayout(new java.awt.GridLayout(1, 2, 3, 1));

        jPbim_FG.setText("FG Color");
        jPbim_FG.setName("jPbim_FG"); // NOI18N
        jPbim_FG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPbim_FGActionPerformed(evt);
            }
        });
        jPanel14.add(jPbim_FG);

        jPbim_BG.setText("BG Color");
        jPbim_BG.setName("jPbim_BG"); // NOI18N
        jPbim_BG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPbim_BGActionPerformed(evt);
            }
        });
        jPanel14.add(jPbim_BG);

        jPbimQt.setEditable(false);
        jPbimQt.setBackground(new java.awt.Color(0, 102, 0));
        jPbimQt.setForeground(new java.awt.Color(255, 255, 255));
        jPbimQt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPbimQt.setText("PBIM's Quote Column");
        jPbimQt.setName("jPbimQt"); // NOI18N

        jPanel15.setName("jPanel15"); // NOI18N
        jPanel15.setLayout(new java.awt.GridLayout(1, 2, 3, 1));

        jSkippedQt_FG.setText("FG Color");
        jSkippedQt_FG.setName("jSkippedQt_FG"); // NOI18N
        jSkippedQt_FG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSkippedQt_FGActionPerformed(evt);
            }
        });
        jPanel15.add(jSkippedQt_FG);

        jSkippedQt_BG.setText("BG Color");
        jSkippedQt_BG.setName("jSkippedQt_BG"); // NOI18N
        jSkippedQt_BG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSkippedQt_BGActionPerformed(evt);
            }
        });
        jPanel15.add(jSkippedQt_BG);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRqc, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(jPanel14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(jPbimQt, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(jSkippedQt, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(jPanel15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRqc, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPbimQt, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSkippedQt, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder("Outgoing Quote or Message Settings: "));
        jPanel12.setName("jPanel12"); // NOI18N

        jOutgoingQt.setEditable(false);
        jOutgoingQt.setBackground(new java.awt.Color(204, 204, 255));
        jOutgoingQt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jOutgoingQt.setText("Outgoing Quote or Message");
        jOutgoingQt.setName("jOutgoingQt"); // NOI18N

        jPanel13.setName("jPanel13"); // NOI18N
        jPanel13.setLayout(new java.awt.GridLayout(1, 2, 3, 1));

        jOutgoingQt_FG.setText("FG Color");
        jOutgoingQt_FG.setName("jOutgoingQt_FG"); // NOI18N
        jOutgoingQt_FG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jOutgoingQt_FGActionPerformed(evt);
            }
        });
        jPanel13.add(jOutgoingQt_FG);

        jOutgoingQt_BG.setText("BG Color");
        jOutgoingQt_BG.setName("jOutgoingQt_BG"); // NOI18N
        jOutgoingQt_BG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jOutgoingQt_BGActionPerformed(evt);
            }
        });
        jPanel13.add(jOutgoingQt_BG);

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jOutgoingQt, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                    .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jOutgoingQt, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Latest Row Settings: "));
        jPanel6.setName("jPanel6"); // NOI18N

        jLatestRow.setEditable(false);
        jLatestRow.setBackground(new java.awt.Color(255, 204, 204));
        jLatestRow.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jLatestRow.setText("Latest Row");
        jLatestRow.setName("jLatestRow"); // NOI18N

        jPanel7.setName("jPanel7"); // NOI18N
        jPanel7.setLayout(new java.awt.GridLayout(1, 2, 3, 1));

        jLatestRow_FG.setText("FG Color");
        jLatestRow_FG.setName("jLatestRow_FG"); // NOI18N
        jLatestRow_FG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLatestRow_FGActionPerformed(evt);
            }
        });
        jPanel7.add(jLatestRow_FG);

        jLatestRow_BG.setText("BG Color");
        jLatestRow_BG.setName("jLatestRow_BG"); // NOI18N
        jLatestRow_BG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLatestRow_BGActionPerformed(evt);
            }
        });
        jPanel7.add(jLatestRow_BG);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLatestRow, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLatestRow, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Selected Row Settings: "));
        jPanel10.setName("jPanel10"); // NOI18N

        jSelectedRow.setEditable(false);
        jSelectedRow.setBackground(new java.awt.Color(255, 255, 0));
        jSelectedRow.setForeground(new java.awt.Color(255, 0, 0));
        jSelectedRow.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jSelectedRow.setText("Selected Row");
        jSelectedRow.setName("jSelectedRow"); // NOI18N

        jPanel11.setName("jPanel11"); // NOI18N
        jPanel11.setLayout(new java.awt.GridLayout(1, 2, 3, 1));

        jSelectedRow_FG.setText("FG Color");
        jSelectedRow_FG.setName("jSelectedRow_FG"); // NOI18N
        jSelectedRow_FG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSelectedRow_FGActionPerformed(evt);
            }
        });
        jPanel11.add(jSelectedRow_FG);

        jSelectedRow_BG.setText("BG Color");
        jSelectedRow_BG.setName("jSelectedRow_BG"); // NOI18N
        jSelectedRow_BG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSelectedRow_BGActionPerformed(evt);
            }
        });
        jPanel11.add(jSelectedRow_BG);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSelectedRow, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSelectedRow, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder("Pricing Column Settings: "));
        jPanel16.setName("jPanel16"); // NOI18N

        jPb.setEditable(false);
        jPb.setBackground(new java.awt.Color(255, 0, 0));
        jPb.setForeground(new java.awt.Color(255, 255, 0));
        jPb.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPb.setText("Price < Bid");
        jPb.setName("jPb"); // NOI18N

        jPanel17.setName("jPanel17"); // NOI18N
        jPanel17.setLayout(new java.awt.GridLayout(1, 2, 3, 1));

        jPb_FG.setText("FG Color");
        jPb_FG.setName("jPb_FG"); // NOI18N
        jPb_FG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPb_FGActionPerformed(evt);
            }
        });
        jPanel17.add(jPb_FG);

        jPb_BG.setText("BG Color");
        jPb_BG.setName("jPb_BG"); // NOI18N
        jPb_BG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPb_BGActionPerformed(evt);
            }
        });
        jPanel17.add(jPb_BG);

        jBpa.setEditable(false);
        jBpa.setBackground(new java.awt.Color(255, 255, 153));
        jBpa.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jBpa.setText("Bid < Price < Ask");
        jBpa.setName("jBpa"); // NOI18N

        jPanel18.setName("jPanel18"); // NOI18N
        jPanel18.setLayout(new java.awt.GridLayout(1, 2, 3, 1));

        jPa_FG.setText("FG Color");
        jPa_FG.setName("jPa_FG"); // NOI18N
        jPa_FG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPa_FGActionPerformed(evt);
            }
        });
        jPanel18.add(jPa_FG);

        jPa_BG.setText("BG Color");
        jPa_BG.setName("jPa_BG"); // NOI18N
        jPa_BG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPa_BGActionPerformed(evt);
            }
        });
        jPanel18.add(jPa_BG);

        jPa.setEditable(false);
        jPa.setBackground(new java.awt.Color(0, 102, 0));
        jPa.setForeground(new java.awt.Color(255, 255, 0));
        jPa.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPa.setText("Price > Ask");
        jPa.setName("jPa"); // NOI18N

        jPanel19.setName("jPanel19"); // NOI18N
        jPanel19.setLayout(new java.awt.GridLayout(1, 2, 3, 1));

        jBpa_FG.setText("FG Color");
        jBpa_FG.setName("jBpa_FG"); // NOI18N
        jBpa_FG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBpa_FGActionPerformed(evt);
            }
        });
        jPanel19.add(jBpa_FG);

        jBpa_BG.setText("BG Color");
        jBpa_BG.setName("jBpa_BG"); // NOI18N
        jBpa_BG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBpa_BGActionPerformed(evt);
            }
        });
        jPanel19.add(jBpa_BG);

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPb, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                    .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                    .addComponent(jPanel18, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                    .addComponent(jPa, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                    .addComponent(jBpa, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                    .addComponent(jPanel19, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPb, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPa, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jBpa, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel12, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        jPanel2.getAccessibleContext().setAccessibleName("Viewer General Settings:");
    }// </editor-fold>//GEN-END:initComponents

    private void jGeneralFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jGeneralFontActionPerformed
        Font font = JFontChooser.showDialog(this,
                                            "Font Chooser",
                                            SwingGlobal.getLabelFont());
        if (!getPointBoxConsoleRuntime().getViewerGeneralFont(viewerUniqueTabName).equals(font)){
            getPointBoxConsoleRuntime().setViewerGeneralFont(viewerUniqueTabName, font);
            applyGeneralFont(font);
        }
    }//GEN-LAST:event_jGeneralFontActionPerformed

    private void applyGeneralFont(Font font){
        jGeneralSample.setFont(font);
        jRqc.setFont(font);
        jPb.setFont(font);
        jPa.setFont(font);
        jPbimQt.setFont(font);
        jSkippedQt.setFont(font);
        jBpa.setFont(font);
        jMsg.setFont(font);
        jOutgoingQt.setFont(font);
        jLatestRow.setFont(font);
        jSelectedRow.setFont(font);
    }

    private void jGeneralColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jGeneralColorActionPerformed
        Color color = getUserSelectedColor(jGeneralSample.getForeground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getViewerGeneralColor(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().setViewerGeneralColor(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setViewerGeneralColor(viewerUniqueTabName, color);
            }
            jGeneralSample.setForeground(color);
        }
    }//GEN-LAST:event_jGeneralColorActionPerformed

    private void jRqc_FGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRqc_FGActionPerformed
        Color color = getUserSelectedColor(jRqc.getForeground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getQtFgColor(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().setQtFgColor(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setQtFgColor(viewerUniqueTabName, color);
            }
            jRqc.setForeground(color);
        }
    }//GEN-LAST:event_jRqc_FGActionPerformed

    private Color getUserSelectedColor(Color originalColor){
        if (originalColor == null){
            originalColor = new Color(23, 45, 200);
        }
//        boolean useJavaFx = false;
//        String jv = DataGlobal.getJavaVersion();
//        if (DataGlobal.isNonEmptyNullString(jv)){
//            double jvDouble = DataGlobal.convertToDouble(RegexGlobal.retrieveFirstMatch(jv, "^([0-9]+[.][0-9]+)"));
//            if (jvDouble > 1.6){
//                useJavaFx = true;
//            }
//        }
//        if (useJavaFx){
//            return getJavaFxColorChooser().selectColor(face.getPointBoxPreferenceDialog(), "Color chooser", new Color(23, 45, 200));
//        }else{
        try{
            return JColorChooser.showDialog(this, "Color chooser", originalColor);
        }catch(Exception ex){
            JOptionPane.showMessageDialog(this, PbcText.getSingleton().getText(PbcTextKey.JvmNotSupportFeature));
            return null;
        }
            
//        }
    }
    
    private void jResetFontColorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jResetFontColorsActionPerformed
        int answer = JOptionPane.showConfirmDialog(this,
                "Reset font, color and column settings for this aggregator panel?",
                "Reset to default settings:",
                JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().resetViewerDefaultSettings(tabName);
                }            
            }else{
                getPointBoxConsoleRuntime().resetViewerDefaultSettings(viewerUniqueTabName);
            }
            populateSettings();
        }else{
            //do nothing
        }
    }//GEN-LAST:event_jResetFontColorsActionPerformed

    private void jRqc_BGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRqc_BGActionPerformed
        Color color = getUserSelectedColor(jRqc.getBackground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getQtBgColor(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().setQtBgColor(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setQtBgColor(viewerUniqueTabName, color);
            }
            jRqc.setBackground(color);
        }
    }//GEN-LAST:event_jRqc_BGActionPerformed

    private void jPb_FGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPb_FGActionPerformed
        Color color = getUserSelectedColor(jPb.getForeground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getPb_FgColor(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().setPb_FgColor(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setPb_FgColor(viewerUniqueTabName, color);
            }
            jPb.setForeground(color);
        }
    }//GEN-LAST:event_jPb_FGActionPerformed

    private void jPb_BGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPb_BGActionPerformed
        Color color = getUserSelectedColor(jPb.getBackground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getPb_BgColor(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().setPb_BgColor(tabName, color);
                }   
            }else{
                getPointBoxConsoleRuntime().setPb_BgColor(viewerUniqueTabName, color);
            }
            jPb.setBackground(color);
        }
    }//GEN-LAST:event_jPb_BGActionPerformed

    private void jPbim_FGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPbim_FGActionPerformed
        Color color = getUserSelectedColor(jPbimQt.getForeground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getPbimQtFgColor(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().setPbimQtFgColor(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setPbimQtFgColor(viewerUniqueTabName, color);
            }
            jPbimQt.setForeground(color);
        }
    }//GEN-LAST:event_jPbim_FGActionPerformed

    private void jPbim_BGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPbim_BGActionPerformed
        Color color = getUserSelectedColor(jPbimQt.getBackground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getPbimQtBgColor(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().setPbimQtBgColor(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setPbimQtBgColor(viewerUniqueTabName, color);
            }
            jPbimQt.setBackground(color);
        }
    }//GEN-LAST:event_jPbim_BGActionPerformed

    private void jPa_FGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPa_FGActionPerformed
        Color color = getUserSelectedColor(jPa.getForeground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getPa_FgColor(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().setPa_FgColor(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setPa_FgColor(viewerUniqueTabName, color);
            }
            jPa.setForeground(color);
        }
    }//GEN-LAST:event_jPa_FGActionPerformed

    private void jPa_BGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPa_BGActionPerformed
        Color color = getUserSelectedColor(jPa.getBackground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getPa_BgColor(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().setPa_BgColor(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setPa_BgColor(viewerUniqueTabName, color);
            }
            jPa.setBackground(color);
        }
    }//GEN-LAST:event_jPa_BGActionPerformed

    private void jSkippedQt_FGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSkippedQt_FGActionPerformed
        Color color = getUserSelectedColor(jSkippedQt.getForeground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getSkippedQtFgColor(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().setSkippedQtFgColor(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setSkippedQtFgColor(viewerUniqueTabName, color);
            }
            jSkippedQt.setForeground(color);
        }
    }//GEN-LAST:event_jSkippedQt_FGActionPerformed

    private void jSkippedQt_BGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSkippedQt_BGActionPerformed
        Color color = getUserSelectedColor(jSkippedQt.getBackground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getSkippedQtBgColor(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().setSkippedQtBgColor(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setSkippedQtBgColor(viewerUniqueTabName, color);
            }
            jSkippedQt.setBackground(color);
        }
    }//GEN-LAST:event_jSkippedQt_BGActionPerformed

    private void jBpa_FGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBpa_FGActionPerformed
        Color color = getUserSelectedColor(jBpa.getForeground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getBpa_FgColor(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().setBpa_FgColor(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setBpa_FgColor(viewerUniqueTabName, color);
            }
            jBpa.setForeground(color);
        }
    }//GEN-LAST:event_jBpa_FGActionPerformed

    private void jBpa_BGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBpa_BGActionPerformed
        Color color = getUserSelectedColor(jBpa.getBackground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getBpa_BgColor(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().setBpa_BgColor(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setBpa_BgColor(viewerUniqueTabName, color);
            }
            jBpa.setBackground(color);
        }
    }//GEN-LAST:event_jBpa_BGActionPerformed

    private void jMsg_FGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMsg_FGActionPerformed
        Color color = getUserSelectedColor(jMsg.getForeground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getMsgFgColor(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().setMsgFgColor(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setMsgFgColor(viewerUniqueTabName, color);
            }
            jMsg.setForeground(color);
        }
    }//GEN-LAST:event_jMsg_FGActionPerformed

    private void jMsg_BGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMsg_BGActionPerformed
        Color color = getUserSelectedColor(jMsg.getBackground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getMsgBgColor(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().setMsgBgColor(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setMsgBgColor(viewerUniqueTabName, color);
            }
            jMsg.setBackground(color);
        }
    }//GEN-LAST:event_jMsg_BGActionPerformed

    private void jOutgoingQt_FGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jOutgoingQt_FGActionPerformed
        Color color = getUserSelectedColor(jOutgoingQt.getForeground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getOutgoingForeground(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().setOutgoingForeground(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setOutgoingForeground(viewerUniqueTabName, color);
            }
            jOutgoingQt.setForeground(color);
        }
    }//GEN-LAST:event_jOutgoingQt_FGActionPerformed

    private void jOutgoingQt_BGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jOutgoingQt_BGActionPerformed
        Color color = getUserSelectedColor(jOutgoingQt.getBackground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getOutgoingBackground(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().setOutgoingBackground(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setOutgoingBackground(viewerUniqueTabName, color);
            }
            jOutgoingQt.setBackground(color);
        }
    }//GEN-LAST:event_jOutgoingQt_BGActionPerformed

    private void jLatestRow_FGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLatestRow_FGActionPerformed
        Color color = getUserSelectedColor(jLatestRow.getForeground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getLatestRowForeground(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().setLatestRowForeground(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setLatestRowForeground(viewerUniqueTabName, color);
            }
            jLatestRow.setForeground(color);
        }
    }//GEN-LAST:event_jLatestRow_FGActionPerformed

    private void jLatestRow_BGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLatestRow_BGActionPerformed
        Color color = getUserSelectedColor(jLatestRow.getBackground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getLatestRowBackground(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                    getPointBoxConsoleRuntime().setLatestRowBackground(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setLatestRowBackground(viewerUniqueTabName, color);
            }
            jLatestRow.setBackground(color);
        }
    }//GEN-LAST:event_jLatestRow_BGActionPerformed

    private void jSelectedRow_FGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSelectedRow_FGActionPerformed
        Color color = getUserSelectedColor(jSelectedRow.getForeground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getSelectedRowForeground(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                     getPointBoxConsoleRuntime().setSelectedRowForeground(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setSelectedRowForeground(viewerUniqueTabName, color);
            }
            jSelectedRow.setForeground(color);
        }
    }//GEN-LAST:event_jSelectedRow_FGActionPerformed

    private void jSelectedRow_BGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSelectedRow_BGActionPerformed
        Color color = getUserSelectedColor(jSelectedRow.getBackground());
        if (color == null){
            return;
        }
        if (!getPointBoxConsoleRuntime().getSelectedRowBackground(viewerUniqueTabName).equals(color)){
            boolean isViewerSettingsForAllViewers = PointBoxConsoleProperties.getSingleton().isViewerSettingsForAllViewers(face.getKernel().getPointBoxLoginUser().getIMUniqueName());
            if (isViewerSettingsForAllViewers){
                for(String tabName:face.getPointBoxViewer().getTabStorage().keySet()){
                     getPointBoxConsoleRuntime().setSelectedRowBackground(tabName, color);
                }
            }else{
                getPointBoxConsoleRuntime().setSelectedRowBackground(viewerUniqueTabName, color);
            }
            jSelectedRow.setBackground(color);
        }
    }//GEN-LAST:event_jSelectedRow_BGActionPerformed

    private void jViewerTabNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jViewerTabNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jViewerTabNameActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField jBpa;
    private javax.swing.JButton jBpa_BG;
    private javax.swing.JButton jBpa_FG;
    private javax.swing.JButton jGeneralColor;
    private javax.swing.JButton jGeneralFont;
    private javax.swing.JTextField jGeneralSample;
    private javax.swing.JTextField jLatestRow;
    private javax.swing.JButton jLatestRow_BG;
    private javax.swing.JButton jLatestRow_FG;
    private javax.swing.JTextField jMsg;
    private javax.swing.JButton jMsg_BG;
    private javax.swing.JButton jMsg_FG;
    private javax.swing.JTextField jOutgoingQt;
    private javax.swing.JButton jOutgoingQt_BG;
    private javax.swing.JButton jOutgoingQt_FG;
    private javax.swing.JTextField jPa;
    private javax.swing.JButton jPa_BG;
    private javax.swing.JButton jPa_FG;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JTextField jPb;
    private javax.swing.JButton jPb_BG;
    private javax.swing.JButton jPb_FG;
    private javax.swing.JTextField jPbimQt;
    private javax.swing.JButton jPbim_BG;
    private javax.swing.JButton jPbim_FG;
    private javax.swing.JButton jResetFontColors;
    private javax.swing.JTextField jRqc;
    private javax.swing.JButton jRqc_BG;
    private javax.swing.JButton jRqc_FG;
    private javax.swing.JTextField jSelectedRow;
    private javax.swing.JButton jSelectedRow_BG;
    private javax.swing.JButton jSelectedRow_FG;
    private javax.swing.JTextField jSkippedQt;
    private javax.swing.JButton jSkippedQt_BG;
    private javax.swing.JButton jSkippedQt_FG;
    private javax.swing.JTextField jViewerTabName;
    private javax.swing.JLabel tabNameLabel;
    // End of variables declaration//GEN-END:variables

}
