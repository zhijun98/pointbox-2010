/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.pricer;

import com.eclipsemarkets.pricer.AbstractPricingEnvironmentData;
import com.eclipsemarkets.pricer.data.LiborCurveData;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.Vector;

/**
 *
 * @author Zhijun Zhang
 */
class PbcPricingEnvironmentData extends AbstractPricingEnvironmentData{

    public LinkedHashMap<String, Double> getInterestRates() {
        return interestRates;
    }

    public LinkedHashMap<String, Double> getUnderliers() {
        return underliers;
    }

    public LinkedHashMap<String, Date> getExpirations() {
        return expirations;
    }

    public TreeSet<Date> getHolidays() {
        return holidays;
    }

    public LinkedHashMap<String, Double> getAtmVolSurface() {
        return atmVolSurface;
    }

    public LinkedHashMap<String, LinkedHashMap<Double, Double>> getSkewVolSurface() {
        return skewVolSurface;
    }

    public Vector<Double> getSkewVolStrikes() {
        return skewVolStrikes;
    }

    public LinkedHashMap<String, Vector<Double>> getVolSmile() {
        return volSmile;
    }

    public void setInterestRates(LinkedHashMap<String, Double> interestRates) {
        this.interestRates = interestRates;
    }
    
    public void setLiborCurveData(LiborCurveData liborCurveData) {
        this.liborCurveData = liborCurveData;
    }
    
    public void setUnderliers(LinkedHashMap<String, Double> underliers) {
        this.underliers = underliers;
    }

    public void setExpirations(LinkedHashMap<String, Date> expirations) {
        this.expirations = expirations;
    }

    public void setHolidays(TreeSet<Date> holidays) {
        this.holidays = holidays;
    }

    public void setAtmVolSurface(LinkedHashMap<String, Double> atmVolSurface) {
        this.atmVolSurface = atmVolSurface;
    }

    public void setSkewVolSurface(LinkedHashMap<String, LinkedHashMap<Double, Double>> skewVolSurface) {
        this.skewVolSurface = skewVolSurface;
    }

    public void setSkewVolStrikes(Vector<Double> skewVolStrikes) {
        this.skewVolStrikes = skewVolStrikes;
    }

    public void setVolSmile(LinkedHashMap<String, Vector<Double>> volSmile) {
        this.volSmile = volSmile;
    }

    public void setAllDescriptiveExpData(LinkedHashMap<GregorianCalendar, String> allDescriptiveExpData) {
        this.allDescriptiveExpData = allDescriptiveExpData;
    }
    
    public LinkedHashMap<GregorianCalendar, String> getAllDescriptiveExpData(){
        return allDescriptiveExpData;
    }
}
