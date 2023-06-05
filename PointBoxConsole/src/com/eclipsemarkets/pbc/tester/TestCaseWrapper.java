/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.tester;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;

/**
 *
 * @author Fang.Bao
 */
public  class TestCaseWrapper{
        private IGatewayConnectorBuddy sender;
        private IGatewayConnectorBuddy receiver;
        private String status; 
        private PointBoxTesterCaseDialog testCase;
       

        public TestCaseWrapper(IGatewayConnectorBuddy sender,IGatewayConnectorBuddy receiver,String status,PointBoxTesterCaseDialog testCase) {
            this.sender = sender;
            this.receiver = receiver;
            this.status = status;
            this.testCase=testCase;
        }


        @Override
        public String toString() {
            return  sender.getIMServerTypeString()+":"+sender.getIMScreenName()+" -> "+receiver.getIMScreenName()+"   "+status;
        }

    /**
     * @return the testCase
     */
    public PointBoxTesterCaseDialog getTestCase() {
        return testCase;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the receiver
     */
    public IGatewayConnectorBuddy getReceiver() {
        return receiver;
    }

    /**
     * @param receiver the receiver to set
     */
    public void setReceiver(IGatewayConnectorBuddy receiver) {
        this.receiver = receiver;
    }
        
 }
