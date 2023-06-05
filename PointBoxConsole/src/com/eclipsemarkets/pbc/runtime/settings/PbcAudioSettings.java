/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.runtime.settings;

import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.pbc.runtime.PbcAudioFileName;
import com.eclipsemarkets.pbc.runtime.properties.PointBoxSoundProperties;
import com.eclipsemarkets.pbc.runtime.properties.PointBoxTabFlashProperties;
import java.net.URL;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * PbcAudioSettings
 * <P>
 * {Insert class description here}
 * <P>
 * @author Zhijun Zhang
 * Created on Jan 29, 2011 at 5:56:27 PM
 */
public class PbcAudioSettings extends PbconsoleSettings implements IPbconsoleAudioSettings{
    private static final Logger logger;
    static{
        logger = Logger.getLogger(PbcAudioSettings.class.getName());
    }

    private EnumMap<PbcAudioFileName, URL> sounds;
    private HashMap soundSetting;
    private HashMap messageTabColorSetting;

    private final String audioFolder;

    public PbcAudioSettings(IPbcRuntime runtime) {
        super(runtime);
        audioFolder = "resources/audio/";
        
        soundSetting = PointBoxSoundProperties.getSingleton().getSoundSetting();
        messageTabColorSetting = PointBoxTabFlashProperties.getSingleton().gettabFlashetting();
        
        sounds = new EnumMap<PbcAudioFileName, URL>(PbcAudioFileName.class);
        sounds.put(PbcAudioFileName.BuddyIn, getAudioURL(PbcAudioFileName.BuddyIn));
        sounds.put(PbcAudioFileName.BuddyOut, getAudioURL(PbcAudioFileName.BuddyOut));
        sounds.put(PbcAudioFileName.CashRegister, getAudioURL(PbcAudioFileName.CashRegister));
        sounds.put(PbcAudioFileName.DoorOpen, getAudioURL(PbcAudioFileName.DoorOpen));
        sounds.put(PbcAudioFileName.DoorSlam, getAudioURL(PbcAudioFileName.DoorSlam));
        sounds.put(PbcAudioFileName.ImRcv, getAudioURL(PbcAudioFileName.ImRcv));
        sounds.put(PbcAudioFileName.ImSend, getAudioURL(PbcAudioFileName.ImSend));
        //sounds.put(PbcAudioFileName.IncomingCall, getAudioURL(PbcAudioFileName.IncomingCall));
        sounds.put(PbcAudioFileName.Moo, getAudioURL(PbcAudioFileName.Moo));
        sounds.put(PbcAudioFileName.NewAlert, getAudioURL(PbcAudioFileName.NewAlert));
        sounds.put(PbcAudioFileName.NewMail, getAudioURL(PbcAudioFileName.NewMail));
        sounds.put(PbcAudioFileName.PanelChange1, getAudioURL(PbcAudioFileName.PanelChange1));
        sounds.put(PbcAudioFileName.Phone, getAudioURL(PbcAudioFileName.Phone));
        //sounds.put(PbcAudioFileName.PhoneRingInternal, getAudioURL(PbcAudioFileName.PhoneRingInternal));
        sounds.put(PbcAudioFileName.Ring, getAudioURL(PbcAudioFileName.Ring));
        sounds.put(PbcAudioFileName.TalkBeg, getAudioURL(PbcAudioFileName.TalkBeg));
        sounds.put(PbcAudioFileName.TalkEnd, getAudioURL(PbcAudioFileName.TalkEnd));
        sounds.put(PbcAudioFileName.TalkStop, getAudioURL(PbcAudioFileName.TalkStop));
    }

    @Override
    public void loadPersonalSettings() {
        //todo: zzj - if it need persistent feature, do it here
    }

    @Override
    public void storePersonalSettings() {
        PointBoxTabFlashProperties.getSingleton().saveSettings(messageTabColorSetting);
        PointBoxSoundProperties.getSingleton().saveSettings(soundSetting);
    }

    public PbcSettingsType getPbcSettingsType() {
        return PbcSettingsType.PbcAudioSettings;
    }

    private URL getAudioURL(PbcAudioFileName audioFileName) {
        return getClass().getResource(audioFolder + audioFileName);
    }

    @Override
    public URL getBuddyIn() {
        return sounds.get(PbcAudioFileName.BuddyIn);
    }

    @Override
    public URL getBuddyOut() {
        return sounds.get(PbcAudioFileName.BuddyOut);
    }

    @Override
    public URL getCashRegister() {
        return sounds.get(PbcAudioFileName.CashRegister);
    }

    @Override
    public URL getDoorOpen() {
        return sounds.get(PbcAudioFileName.DoorOpen);
    }

    @Override
    public URL getDoorSlam() {
        return sounds.get(PbcAudioFileName.DoorSlam);
    }

    @Override
    public URL getImRcv() {
        return sounds.get(PbcAudioFileName.ImRcv);
    }

    @Override
    public URL getImSend() {
        return sounds.get(PbcAudioFileName.ImSend);
    }

//    public URL getIncomingCall() {
//        return sounds.get(PbcAudioFileName.IncomingCall);
//    }

    @Override
    public URL getMoo() {
        return sounds.get(PbcAudioFileName.Moo);
    }

    @Override
    public URL getNewAlert() {
        return sounds.get(PbcAudioFileName.NewAlert);
    }

    @Override
    public URL getNewMail() {
        return sounds.get(PbcAudioFileName.NewMail);
    }

    @Override
    public URL getPanelChange1() {
        return sounds.get(PbcAudioFileName.PanelChange1);
    }

    @Override
    public URL getPhone() {
        return sounds.get(PbcAudioFileName.Phone);
    }

//    public URL getPhoneRingInternal() {
//        return sounds.get(PbcAudioFileName.PhoneRingInternal);
//    }

    @Override
    public URL getRing() {
        return sounds.get(PbcAudioFileName.Ring);
    }

    @Override
    public URL getTalkBeg() {
        return sounds.get(PbcAudioFileName.TalkBeg);
    }

    @Override
    public URL getTalkEnd() {
        return sounds.get(PbcAudioFileName.TalkEnd);
    }

    @Override
    public URL getTalkStop() {
        return sounds.get(PbcAudioFileName.TalkStop);
    }

    @Override
    public EnumMap<PbcAudioFileName, URL> getSoundList() {
        return sounds;
    }

    @Override
    public HashMap getSoundSetting() {
        return soundSetting;
    }
    
    @Override
    public void setSoundSetting(HashMap setting){
        soundSetting = setting;
    }
    
    @Override
    public HashMap getTabFlashingSetting(){
        return messageTabColorSetting;
    }
    
    @Override
    public void setTabFlashingSetting(HashMap setting){
        messageTabColorSetting = setting;
    }

}//PbcAudioSettings

