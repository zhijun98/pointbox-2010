/**
 * Eclipse Market Solutions LLC
 *
 * GeneralSettingsPanel.java
 *
 * @author Zhijun Zhang
 * Created on May 23, 2010, 11:40:24 PM
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.data.PointBoxQuoteCodeWrapper;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.face.IPbcFace;
import com.eclipsemarkets.pbc.face.PbcFaceComponentType;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.pbc.runtime.PbcAudioFileName;
import com.eclipsemarkets.pbc.runtime.settings.IPbconsoleAudioSettings;
import com.eclipsemarkets.pbc.runtime.settings.IPbconsoleImageSettings;
import com.eclipsemarkets.web.pbc.PbcPricingModel;
import com.eclipsemarkets.web.pbc.PbcSystemFrameLayout;
import com.eclipsemarkets.web.pbc.PbcSystemFrameStyle;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.io.File;
import java.net.URL;
import javax.swing.filechooser.FileFilter;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

/**
 *
 * @author Zhijun Zhang
 */
class GeneralSettingsPanel extends javax.swing.JPanel implements IPreferenceComponentPanel {
    private static final Logger logger;
    static{
        logger = Logger.getLogger(GeneralSettingsPanel.class.getName());
    }
    private static final long serialVersionUID = 1L;

    private final IPbcFace face;

    private final HashMap soundSettings;
    private final EnumMap<PbcAudioFileName, URL> sounds;
    private final HashMap messageTabColorSetting;

    private IPbconsoleImageSettings imageSettings;
    private IPbconsoleAudioSettings audioSettings;
    
    private JFileChooser fileChooser;

    /**
     * 
     * @param face
     * @param pointBoxFrame
     */
    GeneralSettingsPanel(final IPbcFace face) {
        initComponents();

        this.face = face;

        imageSettings = face.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings();
        audioSettings = face.getKernel().getPointBoxConsoleRuntime().getPbcAudioSettings();
        
        fileChooser = new JFileChooser();

        soundSettings = audioSettings.getSoundSetting();
        sounds = audioSettings.getSoundList();
        messageTabColorSetting = audioSettings.getTabFlashingSetting();
        //initial the sound settings and update the sound check box and button on the setting panel 
        initSoundComponents(jCB_ReceivedSoundOptions,jReceivedIMSound,jButChangeReceivedSound,"jReceivedIMSound");
        initSoundComponents(jCB_SentSoundOptions,jSentIMSound,jButChangeSentSound,"jSentIMSound");
        initTabsComponents();
        
        jTxtFld_Foreground.setEditable(false);
        jTxtFld_Foreground.setBackground(Color.decode((String)messageTabColorSetting.get("flashingTabForeground")));
        jTxtFld_Background.setEditable(false);
        jTxtFld_Background.setBackground(Color.decode((String)messageTabColorSetting.get("flashingTabBackground")));
        jTxtFld_FlashFrequency.setText((String)messageTabColorSetting.get("flashingFrequency"));
       
        //String defaultCode = face.getKernel().getDefaultSimCodeFromProperties().toString();
        PointBoxQuoteCodeWrapper defaultCodeWrapper = new PointBoxQuoteCodeWrapper(face.getKernel().getDefaultSimCodeFromProperties());
        HashMap<String, PbcPricingModel> codeMap = face.getKernel().getPointBoxConsoleRuntime().getPbcPricingModelMap();
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        Set<String> codeValues = codeMap.keySet();
        for (String codeValue : codeValues){
            model.addElement(new PointBoxQuoteCodeWrapper(PointBoxQuoteCode.convertEnumNameToType(codeValue)));
        }
        jSimCodeComboBox.setModel(model);
        jSimCodeComboBox.setSelectedItem(defaultCodeWrapper);
    }
    
    //initial tabs settings and update tabs resume checkbox
    private void initTabsComponents(){
        PointBoxConsoleProperties prop=PointBoxConsoleProperties.getSingleton();
        if(prop.retrieveTabPersistedOption(face.getPointBoxLoginUser().getIMUniqueName())){
            jCB_TabResume.setSelected(true);
        }else{
            jCB_TabResume.setSelected(false);
        }      
        jCB_TabResume.setVisible(false);
        jPanel4.setVisible(false);
    }

    private void initSoundComponents(javax.swing.JComboBox jCB_SoundOptions,javax.swing.JLabel jIMSound,javax.swing.JButton jButChangeSound,String soundType){
        for(Object sound : sounds.keySet().toArray()){
            jCB_SoundOptions.addItem(sound.toString());
        }
        if (soundSettings != null){
            if (soundSettings.get("enableSound") != null){
                if(((String)soundSettings.get("enableSound")).equals("true")){
                    jCB_SoundEnable.setSelected(true);
                    if(soundSettings.containsKey(soundType)){
                        //jCB_SoundOptions.setText((String)soundSettings.get("jReceivedIMSound"));
                        if(sounds.containsKey(PbcAudioFileName.converToType((String)soundSettings.get(soundType))))
                        {
                            jCB_SoundOptions.setSelectedItem((String)soundSettings.get(soundType));
                            jIMSound.setText((String)soundSettings.get(soundType));
                            jCB_SoundOptions.setEnabled(true);
                        }
                        else{
                             File aFile = new File((String)soundSettings.get(soundType));
                             if(((String)soundSettings.get(soundType)).equals("No Sounds")){
                                //jCB_SoundEnable.setSelected(false);   //useless
                                jIMSound.setText(aFile.getName());
                             }
                             else{
                                jCB_SoundOptions.setSelectedItem("Other Selected");
                                jIMSound.setText(aFile.getName());
                                jCB_SoundOptions.setEnabled(true);
                             }
                        }
                    }
                    else{
                        /*jReceivedIMSound.setText((String)soundSettings.get("jReceivedIMSound"));
                        jReceivedIMSound.setEnabled(true);
                        jReceivedIMSound.setEditable(false);*/
                    }
                }
                else{
                    jCB_SoundEnable.setSelected(false);
                    if(soundSettings.containsKey(soundType)){
                        jIMSound.setText((String)soundSettings.get(soundType));
                        jCB_SoundOptions.setSelectedItem((String)soundSettings.get(soundType));
                    }
                    jCB_SoundOptions.setEnabled(false);
                    jButChangeSound.setEnabled(false);
                }
            }
        }
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
        applySizeSettingsHelper();
        applyLayoutSettingsHelper();
        applyStyleSettingsHelper();
        applySoundEnableSettingHelper();        //update the sound settings components on the panel based on the settings.
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
    
    private IPbcRuntime getPbcRuntime(){
        return face.getKernel().getPointBoxConsoleRuntime();
    }

    private void applySizeSettingsHelper(){
        Dimension size = getPbcRuntime().getPbcWindowsSize(PbcFaceComponentType.PointBoxFrame);
        jFrameWidth.setText(Double.toString(size.getWidth()));
        jFrameHeight.setText(Double.toString(size.getHeight()));
    }
    
    private boolean ishLayout(){
        return getPbcRuntime().getPbcSystemFrameLayout().equals(PbcSystemFrameLayout.Horizontal);
    }
    
    private boolean isFloatingStyle() {
        return getPbcRuntime().getPbcSystemFrameStyle().equals(PbcSystemFrameStyle.Floating);
    }

    private void applyStyleSettingsHelper(){
        if (isFloatingStyle()){
            if (jDocking.isSelected()){
                jDocking.setSelected(false);
                jFloating.setSelected(true);
            }
        }else{
            if (jFloating.isSelected()){
                jDocking.setSelected(true);
                jFloating.setSelected(false);
            }
        }
    }

    private void applyLayoutSettingsHelper(){
        if (ishLayout()){
            if (jVLayout.isSelected()){
                jHLayout.setSelected(true);
                jVLayout.setSelected(false);
            }
        }else{
            if (jHLayout.isSelected()){
                jHLayout.setSelected(false);
                jVLayout.setSelected(true);
            }
        }
    }
    
    
    //update the checkBox based on the settings in case of singleton issue to make the update useless.
    private void applySoundEnableSettingHelper(){
        initSoundComponents(jCB_ReceivedSoundOptions,jReceivedIMSound,jButChangeReceivedSound,"jReceivedIMSound");
        initSoundComponents(jCB_SentSoundOptions,jSentIMSound,jButChangeSentSound,"jSentIMSound");
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        layoutGroup = new javax.swing.ButtonGroup();
        styleGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jFrameWidth = new javax.swing.JTextField();
        jFrameHeight = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jVLayout = new javax.swing.JRadioButton();
        jHLayout = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        jDocking = new javax.swing.JRadioButton();
        jFloating = new javax.swing.JRadioButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jCB_SoundEnable = new javax.swing.JCheckBox();
        jButChangeReceivedSound = new javax.swing.JButton();
        jCB_ReceivedSoundOptions = new javax.swing.JComboBox();
        jReceivedIMSound = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jTxtFld_Foreground = new javax.swing.JTextField();
        jBt_Foreground = new javax.swing.JButton();
        jBt_Background = new javax.swing.JButton();
        jTxtFld_Background = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jTxtFld_FlashFrequency = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jSentIMSound = new javax.swing.JLabel();
        jCB_SentSoundOptions = new javax.swing.JComboBox();
        jButChangeSentSound = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jSimCodeComboBox = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        jCB_TabResume = new javax.swing.JCheckBox();

        setFocusable(false);
        setVerifyInputWhenFocusTarget(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Main Frame Size: "));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(210, 100));

        jLabel1.setText("Width:");
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText("Height:");
        jLabel2.setName("jLabel2"); // NOI18N

        jFrameWidth.setName("jFrameWidth"); // NOI18N
        jFrameWidth.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jFrameWidthFocusLost(evt);
            }
        });

        jFrameHeight.setName("jFrameHeight"); // NOI18N
        jFrameHeight.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jFrameHeightFocusLost(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jFrameHeight)
                    .addComponent(jFrameWidth))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel1)
                    .addComponent(jFrameWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jFrameHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Main Frame Layout: "));
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setPreferredSize(new java.awt.Dimension(210, 100));

        layoutGroup.add(jVLayout);
        jVLayout.setSelected(true);
        jVLayout.setText("Horizontal Layout");
        jVLayout.setName("jVLayout"); // NOI18N
        jVLayout.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jVLayoutItemStateChanged(evt);
            }
        });

        layoutGroup.add(jHLayout);
        jHLayout.setText("Vertical Layout");
        jHLayout.setName("jHLayout"); // NOI18N
        jHLayout.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jHLayoutItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jHLayout)
                    .addComponent(jVLayout))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jVLayout)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jHLayout)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Main Frame Style: "));
        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setPreferredSize(new java.awt.Dimension(210, 100));

        styleGroup.add(jDocking);
        jDocking.setSelected(true);
        jDocking.setText("Docking Windows");
        jDocking.setName("jDocking"); // NOI18N
        jDocking.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jDockingItemStateChanged(evt);
            }
        });

        styleGroup.add(jFloating);
        jFloating.setText("Floating Windows");
        jFloating.setName("jFloating"); // NOI18N
        jFloating.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jFloatingItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jFloating)
                    .addComponent(jDocking))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jDocking)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jFloating)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("IM Notification Sound Alert:"));
        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setPreferredSize(new java.awt.Dimension(210, 100));

        jLabel7.setText("Received message sound:");
        jLabel7.setName("jLabel7"); // NOI18N

        jCB_SoundEnable.setText("Enable Sound Alert");
        jCB_SoundEnable.setName("jCB_SoundEnable"); // NOI18N
        jCB_SoundEnable.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCB_SoundEnableItemStateChanged(evt);
            }
        });

        jButChangeReceivedSound.setText("Other");
        jButChangeReceivedSound.setName("jButChangeReceivedSound"); // NOI18N
        jButChangeReceivedSound.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButChangeReceivedSoundActionPerformed(evt);
            }
        });

        jCB_ReceivedSoundOptions.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Sounds", "Other Selected" }));
        jCB_ReceivedSoundOptions.setName("jCB_ReceivedSoundOptions"); // NOI18N
        jCB_ReceivedSoundOptions.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCB_ReceivedSoundOptionsItemStateChanged(evt);
            }
        });

        jReceivedIMSound.setText("jLabel8");
        jReceivedIMSound.setName("jReceivedIMSound"); // NOI18N

        jLabel8.setText("Foreground Tab Flash Color");
        jLabel8.setName("jLabel8"); // NOI18N

        jTxtFld_Foreground.setName("jTxtFld_Foreground"); // NOI18N

        jBt_Foreground.setText("Change Foreground Color");
        jBt_Foreground.setName("jBt_Foreground"); // NOI18N
        jBt_Foreground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBt_ForegroundActionPerformed(evt);
            }
        });

        jBt_Background.setText("Change Background Color");
        jBt_Background.setName("jBt_Background"); // NOI18N
        jBt_Background.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBt_BackgroundActionPerformed(evt);
            }
        });

        jTxtFld_Background.setName("jTxtFld_Background"); // NOI18N

        jLabel9.setText("Background Tab Flash Color");
        jLabel9.setName("jLabel9"); // NOI18N

        jLabel10.setText("Tab Flash Frequency");
        jLabel10.setName("jLabel10"); // NOI18N

        jTxtFld_FlashFrequency.setName("jTxtFld_FlashFrequency"); // NOI18N
        jTxtFld_FlashFrequency.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTxtFld_FlashFrequencyKeyTyped(evt);
            }
        });

        jLabel11.setText("Sent message sound:");
        jLabel11.setName("jLabel11"); // NOI18N

        jSentIMSound.setText("jLabel8");
        jSentIMSound.setName("jSentIMSound"); // NOI18N

        jCB_SentSoundOptions.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Sounds", "Other Selected" }));
        jCB_SentSoundOptions.setName("jCB_SentSoundOptions"); // NOI18N
        jCB_SentSoundOptions.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCB_SentSoundOptionsItemStateChanged(evt);
            }
        });

        jButChangeSentSound.setText("Other");
        jButChangeSentSound.setName("jButChangeSentSound"); // NOI18N
        jButChangeSentSound.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButChangeSentSoundActionPerformed(evt);
            }
        });

        jLabel3.setText("Default SIM Code: ");
        jLabel3.setName("jLabel3"); // NOI18N

        jSimCodeComboBox.setName("jSimCodeComboBox"); // NOI18N
        jSimCodeComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jSimCodeComboBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jCB_SoundEnable)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(jSentIMSound, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCB_SentSoundOptions, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButChangeSentSound)
                        .addGap(8, 8, 8))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addGap(28, 28, 28)
                                .addComponent(jReceivedIMSound, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                                        .addComponent(jLabel8)
                                        .addGap(18, 18, 18)
                                        .addComponent(jTxtFld_Foreground, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel9)
                                            .addComponent(jLabel10))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jTxtFld_FlashFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jTxtFld_Background, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                                .addGap(0, 4, Short.MAX_VALUE)
                                .addComponent(jCB_ReceivedSoundOptions, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButChangeReceivedSound)
                                .addGap(8, 8, 8))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jBt_Background)
                                    .addComponent(jBt_Foreground)
                                    .addGroup(jPanel6Layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jSimCodeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE))))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jCB_SoundEnable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jButChangeSentSound)
                    .addComponent(jSentIMSound)
                    .addComponent(jCB_SentSoundOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jButChangeReceivedSound)
                    .addComponent(jReceivedIMSound)
                    .addComponent(jCB_ReceivedSoundOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jBt_Foreground)
                    .addComponent(jTxtFld_Foreground, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jBt_Background)
                    .addComponent(jTxtFld_Background, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTxtFld_FlashFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel3)
                    .addComponent(jSimCodeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Tabs:"));
        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setPreferredSize(new java.awt.Dimension(210, 100));

        jCB_TabResume.setText("Tabs Resume After Relogin");
        jCB_TabResume.setName("jCB_TabResume"); // NOI18N
        jCB_TabResume.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCB_TabResumeItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCB_TabResume)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jCB_TabResume)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)))
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jVLayoutItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jVLayoutItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            if (ishLayout()){
                getPbcRuntime().setPbcSystemFrameLayout(PbcSystemFrameLayout.Vertical);
                face.updateSystemFrameLayoutAndStyle();
            }
        }
    }//GEN-LAST:event_jVLayoutItemStateChanged

    private void jHLayoutItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jHLayoutItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            if (!ishLayout()){
                getPbcRuntime().setPbcSystemFrameLayout(PbcSystemFrameLayout.Horizontal);
                face.updateSystemFrameLayoutAndStyle();
            }
        }
    }//GEN-LAST:event_jHLayoutItemStateChanged

    private void jDockingItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jDockingItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            if (isFloatingStyle()){
                getPbcRuntime().setPbcSystemFrameStyle(PbcSystemFrameStyle.Docked);
                face.updateSystemFrameLayoutAndStyle();
            }
        }
    }//GEN-LAST:event_jDockingItemStateChanged

    private void jFloatingItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jFloatingItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            if (!isFloatingStyle()){
                getPbcRuntime().setPbcSystemFrameStyle(PbcSystemFrameStyle.Floating);
                face.updateSystemFrameLayoutAndStyle();
            }
        }
    }//GEN-LAST:event_jFloatingItemStateChanged

    private void jButChangeReceivedSoundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButChangeReceivedSoundActionPerformed
        if (soundSettings == null){
            return;
        }
        //Handle open button action.
        //Create a file chooser
        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new SoundFilter());
        fc.setAcceptAllFileFilterUsed(false);
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try{
                File file = fc.getSelectedFile();
                URL path = file.toURI().toURL();
                String fileName = fc.getName(file);
                AudioInputStream ais = AudioSystem.getAudioInputStream(path);

                jReceivedIMSound.setText(fileName);
                soundSettings.put("jReceivedIMSound", path.toExternalForm());
                audioSettings.setSoundSetting(soundSettings);
            }
            catch(UnsupportedAudioFileException e){

            }
            catch(Exception e){
                logger.log(Level.SEVERE, e.getMessage(), e);
            }

        } else {

        }
    }//GEN-LAST:event_jButChangeReceivedSoundActionPerformed

    private void jCB_SoundEnableItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCB_SoundEnableItemStateChanged
         if(evt.getStateChange() == ItemEvent.SELECTED){
            jCB_ReceivedSoundOptions.setEnabled(true);
            jCB_SentSoundOptions.setEnabled(true);
            jButChangeReceivedSound.setEnabled(true);
            jButChangeSentSound.setEnabled(true);
            soundSettings.put("enableSound", "true");
            audioSettings.setSoundSetting(soundSettings);
        }
        if(evt.getStateChange() == ItemEvent.DESELECTED){
            jButChangeReceivedSound.setEnabled(false);
            jCB_ReceivedSoundOptions.setEnabled(false);
            jButChangeSentSound.setEnabled(false);
            jCB_SentSoundOptions.setEnabled(false);
            soundSettings.put("enableSound", "false");
            audioSettings.setSoundSetting(soundSettings);
        }
        face.updateSoundSettings();
    }//GEN-LAST:event_jCB_SoundEnableItemStateChanged

    private void jCB_ReceivedSoundOptionsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCB_ReceivedSoundOptionsItemStateChanged
        String soundFileName = jCB_ReceivedSoundOptions.getSelectedItem().toString();
        if ((evt.getStateChange() == ItemEvent.SELECTED) && (!soundFileName.equalsIgnoreCase("Other Selected"))){
            jReceivedIMSound.setText(soundFileName);
            soundSettings.put("jReceivedIMSound", soundFileName);
            audioSettings.setSoundSetting(soundSettings);
        }
    }//GEN-LAST:event_jCB_ReceivedSoundOptionsItemStateChanged

    private void jBt_ForegroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBt_ForegroundActionPerformed
        Color color = JColorChooser.showDialog(this,
                        "Color chooser", new Color(23, 45, 200));
        jTxtFld_Foreground.setBackground(color);
        messageTabColorSetting.put("flashingTabForeground", String.valueOf(color.getRGB()));
    }//GEN-LAST:event_jBt_ForegroundActionPerformed

    private void jBt_BackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBt_BackgroundActionPerformed
        Color color = JColorChooser.showDialog(this,
                        "Color chooser", new Color(23, 45, 200));
        jTxtFld_Background.setBackground(color);
        messageTabColorSetting.put("flashingTabBackground", String.valueOf(color.getRGB()));
    }//GEN-LAST:event_jBt_BackgroundActionPerformed

    private void jTxtFld_FlashFrequencyKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtFld_FlashFrequencyKeyTyped
        if(Integer.getInteger(jTxtFld_FlashFrequency.getText()) != null){
            messageTabColorSetting.put("flashingFrequency", String.valueOf(jTxtFld_FlashFrequency.getText()));
        }
    }//GEN-LAST:event_jTxtFld_FlashFrequencyKeyTyped

    
    private void jFrameWidthFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jFrameWidthFocusLost
        Dimension size = getPbcRuntime().getPbcWindowsSize(PbcFaceComponentType.PointBoxFrame);
        int newWidth = DataGlobal.convertToInteger(jFrameWidth.getText());
        int height = (int)size.getHeight();
        if (newWidth != (int)size.getWidth()){
            Dimension newSize = new Dimension(newWidth, height);
            getPbcRuntime().setPbcWindowsSize(newSize, PbcFaceComponentType.PointBoxFrame);
            face.updateSystemFrameSize(newSize);
        }
    }//GEN-LAST:event_jFrameWidthFocusLost

    private void jFrameHeightFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jFrameHeightFocusLost
        Dimension size = getPbcRuntime().getPbcWindowsSize(PbcFaceComponentType.PointBoxFrame);
        int width = (int)size.getWidth();
        int newHeight = DataGlobal.convertToInteger(jFrameHeight.getText());
        if (newHeight != (int)size.getHeight()){
            Dimension newSize = new Dimension(width, newHeight);
            getPbcRuntime().setPbcWindowsSize(newSize, PbcFaceComponentType.PointBoxFrame);
            face.updateSystemFrameSize(newSize);
        }
    }//GEN-LAST:event_jFrameHeightFocusLost

    private void jCB_SentSoundOptionsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCB_SentSoundOptionsItemStateChanged
        String soundFileName = jCB_SentSoundOptions.getSelectedItem().toString();
        if ((evt.getStateChange() == ItemEvent.SELECTED) && (!soundFileName.equalsIgnoreCase("Other Selected"))){
            jSentIMSound.setText(soundFileName);
            soundSettings.put("jSentIMSound", soundFileName);
            audioSettings.setSoundSetting(soundSettings);
        }
    }//GEN-LAST:event_jCB_SentSoundOptionsItemStateChanged

    private void jButChangeSentSoundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButChangeSentSoundActionPerformed
        if (soundSettings == null){
            return;
        }
        //Handle open button action.
        //Create a file chooser
        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new SoundFilter());
        fc.setAcceptAllFileFilterUsed(false);
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try{
                File file = fc.getSelectedFile();
                URL path = file.toURI().toURL();
                String fileName = fc.getName(file);
                AudioInputStream ais = AudioSystem.getAudioInputStream(path);

                jSentIMSound.setText(fileName);
                soundSettings.put("jSentIMSound", path.toExternalForm());
                audioSettings.setSoundSetting(soundSettings);
            }
            catch(UnsupportedAudioFileException e){

            }
            catch(Exception e){
                logger.log(Level.SEVERE, e.getMessage(), e);
            }

        } else {

        }
    }//GEN-LAST:event_jButChangeSentSoundActionPerformed

    private void jCB_TabResumeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCB_TabResumeItemStateChanged
        PointBoxConsoleProperties prop = PointBoxConsoleProperties.getSingleton();
        prop.storeTabPersistedOption(jCB_TabResume.isSelected(), face.getPointBoxLoginUser().getIMUniqueName());
    }//GEN-LAST:event_jCB_TabResumeItemStateChanged

    private void jSimCodeComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jSimCodeComboBoxItemStateChanged
        if (jSimCodeComboBox.getSelectedItem() instanceof PointBoxQuoteCodeWrapper){
            PointBoxConsoleProperties prop = PointBoxConsoleProperties.getSingleton();
            prop.storeDefaultPointBoxQuoteCode(face.getPointBoxLoginUser().getIMUniqueName(), ((PointBoxQuoteCodeWrapper)jSimCodeComboBox.getSelectedItem()).getCode());
        }
    }//GEN-LAST:event_jSimCodeComboBoxItemStateChanged
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBt_Background;
    private javax.swing.JButton jBt_Foreground;
    private javax.swing.JButton jButChangeReceivedSound;
    private javax.swing.JButton jButChangeSentSound;
    private javax.swing.JComboBox jCB_ReceivedSoundOptions;
    private javax.swing.JComboBox jCB_SentSoundOptions;
    private javax.swing.JCheckBox jCB_SoundEnable;
    private javax.swing.JCheckBox jCB_TabResume;
    private javax.swing.JRadioButton jDocking;
    private javax.swing.JRadioButton jFloating;
    private javax.swing.JTextField jFrameHeight;
    private javax.swing.JTextField jFrameWidth;
    private javax.swing.JRadioButton jHLayout;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JLabel jReceivedIMSound;
    private javax.swing.JLabel jSentIMSound;
    private javax.swing.JComboBox jSimCodeComboBox;
    private javax.swing.JTextField jTxtFld_Background;
    private javax.swing.JTextField jTxtFld_FlashFrequency;
    private javax.swing.JTextField jTxtFld_Foreground;
    private javax.swing.JRadioButton jVLayout;
    private javax.swing.ButtonGroup layoutGroup;
    private javax.swing.ButtonGroup styleGroup;
    // End of variables declaration//GEN-END:variables

    private class SoundFilter extends FileFilter {

        //Accept all directories and all gif, jpg, tiff, or png files.
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null) {
                if (extension.equals("wav") ||
                    //extension.equals("mp3") ||
                    extension.equals("au") ||
                    extension.equals("aiff")){
                        return true;
                } else {
                    return false;
                }
            }
            return false;
        }

        /*
        * Get the extension of a file.
        */
        public String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 &&  i < s.length() - 1) {
                ext = s.substring(i+1).toLowerCase();
            }
            return ext;
        }
        //The description of this filter
        @Override
        public String getDescription() {
            return "Sounds(wav, au, aiff)";
        }
    }
}
