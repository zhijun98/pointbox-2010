/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.runtime.settings.record;

/**
 *
 * @author Rueyfarn Wang
 *
 */
class BuddyProfileRecord implements IBuddyProfileRecord {

   private String userOwner;
   private String profileUuid;
   private String gatewayServerType;
   private String screenName;
   private String country;
   private String middleName;

   private String firstName;
   private String lastName;
   private String nickName;
   private String notes;
   private String workStreet;

   private String workCity;
   private String workState;
   private String workZip;
   private String workPhone;
   private String cellPhone;

   private String workFax;
   private String pager;
   private String workEmail;

   public synchronized String getUserOwner()
   {
      return userOwner;
   }

   public synchronized void setUserOwner(String userOwner)
   {
      this.userOwner = userOwner;
   }


   public synchronized String getProfileUuid()
   {
      return profileUuid;
   }

   public synchronized void setProfileUuid(String profileUuid)
   {
      this.profileUuid = profileUuid;
   }



   public synchronized String getFirstName()
   {
      return firstName;
   }

   public synchronized void setFirstName(String firstName)
   {
      this.firstName = firstName;
   }

   public synchronized String getLastName()
   {
      return lastName;
   }

   public synchronized void setLastName(String lastName)
   {
      this.lastName = lastName;
   }

   public synchronized String getNickName()
   {
      return nickName;
   }

   public synchronized void setNickName(String nickName)
   {
      this.nickName = nickName;
   }

   public synchronized String getNotes()
   {
      return notes;
   }

   public synchronized void setNotes(String notes)
   {
      this.notes = notes;
   }

   public synchronized String getWorkStreet()
   {
      return workStreet;
   }

   public synchronized void setWorkStreet(String workStreet)
   {
      this.workStreet = workStreet;
   }


   public synchronized String getWorkState()
   {
      return workState;
   }

   public synchronized void setWorkState(String workState)
   {
      this.workState = workState;
   }

   public synchronized String getWorkCity()
   {
      return workCity;
   }

   public synchronized void setWorkCity(String workCity)
   {
      this.workCity = workCity;
   }

   public synchronized String getWorkZip()
   {
      return workZip;
   }

   public synchronized void setWorkZip(String workZip)
   {
      this.workZip = workZip;
   }


   public synchronized String getWorkPhone()
   {
      return workPhone;
   }

   public synchronized void setWorkPhone(String workPhone)
   {
      this.workPhone = workPhone;
   }


   public synchronized String getCellPhone()
   {
      return cellPhone;
   }

   public synchronized void setCellPhone(String cellPhone)
   {
      this.cellPhone = cellPhone;
   }

  
   public synchronized String getWorkFax()
   {
      return workFax;
   }

   public synchronized void setWorkFax(String workFax)
   {
      this.workFax = workFax;
   }




   public synchronized String getPager()
   {
      return pager;
   }

   public synchronized void setPager(String pager)
   {
      this.pager = pager;
   }



   public synchronized String getWorkEmail()
   {
      return workEmail;
   }


   public synchronized void setWorkEmail(String workEmail)
   {
      this.workEmail = workEmail;
   }

   public synchronized void cleanUp() {
      userOwner = null;
      profileUuid = null;

      firstName = null;
      lastName = null;
      nickName = null;
      notes = null;
      workStreet = null;

      workCity = null;
      workState = null;
      workZip = null;
      workPhone = null;
      cellPhone = null;

      workFax = null;
      pager = null;
      workEmail = null;
      
      gatewayServerType=null;
      screenName=null;
      country=null;
      middleName=null;
      
   }

    /**
     * @return the gatewayServerType
     */
    public String getGatewayServerType() {
        return gatewayServerType;
    }

    /**
     * @param gatewayServerType the gatewayServerType to set
     */
    public void setGatewayServerType(String gatewayServerType) {
        this.gatewayServerType = gatewayServerType;
    }

    /**
     * @return the screenName
     */
    public String getScreenName() {
        return screenName;
    }

    /**
     * @param screenName the screenName to set
     */
    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * @return the middleName
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * @param middleName the middleName to set
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

}
