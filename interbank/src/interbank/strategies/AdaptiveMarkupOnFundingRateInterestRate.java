/*
 * JMAB - Java Macroeconomic Agent Based Modeling Toolkit
 * Copyright (C) 2013 Alessandro Caiani and Antoine Godin
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package interbank.strategies;

import java.nio.ByteBuffer;
import java.util.List;

import interbank.StaticValues;
import interbank.agents.Bank;
import jmab.agents.MacroAgent;
import jmab.population.MacroPopulation;
import jmab.stockmatrix.InterestBearingItem;
import jmab.stockmatrix.Item;
import jmab.strategies.InterestRateStrategy;
import net.sourceforge.jabm.Population;
import net.sourceforge.jabm.SimulationController;
import net.sourceforge.jabm.agent.Agent;
import net.sourceforge.jabm.distribution.AbstractDelegatedDistribution;
import net.sourceforge.jabm.strategy.AbstractStrategy;

/**
 * @author Alessandro Caiani and Antoine Godin
 *
 */
@SuppressWarnings("serial")
public class AdaptiveMarkupOnFundingRateInterestRate extends AbstractStrategy implements
InterestRateStrategy {

	private double adaptiveParameter;
	private AbstractDelegatedDistribution distribution; 
	private int[] liabilitiesId;
	private double markup;
	private int mktId;

	/* (non-Javadoc)
	 * @see jmab.strategies.InterestRateStrategy#computeInterestRate(jmab.agents.MacroAgent, double, int)
	 */
	@Override
	public double computeInterestRate(MacroAgent creditDemander, double amount,
			int length) {
		double threshold=0;
		double avInterest=0;
		SimulationController controller = (SimulationController)this.getScheduler();
		MacroPopulation macroPop = (MacroPopulation) controller.getPopulation();
		Population banks = macroPop.getPopulation(StaticValues.BANKS_ID);
		double tot=0;
		double inter=0;
		double n=(double) banks.getSize();
		for (Agent b:banks.getAgents()){
			Bank bank = (Bank) b;
			if (bank.getNumericBalanceSheet()[0][StaticValues.SM_LOAN]!=0&&bank.getNetWealth()>0){
				tot+=bank.getCapitalRatio();
				inter+=bank.getPassedValue(StaticValues.LAG_LOANINTEREST, 1);
			}
			else{
				n-=1;
			}
		}
		threshold=tot/n;
		avInterest=inter/n;

		Bank lender=(Bank) this.getAgent();
		double interestPay=0;
		double totValue=0;
		for(int liabilityId:liabilitiesId){
			List<Item> liabilities = lender.getItemsStockMatrix(false, liabilityId);
			for(Item item:liabilities){
				InterestBearingItem liability = (InterestBearingItem) item;
				interestPay += liability.getInterestRate()*liability.getValue();
				totValue +=liability.getValue();
			}
		}
		double fundingRate = interestPay/totValue;
		double referenceVariable=lender.getCapitalRatio();
		//double iR = lender.getInterestRate(mktId);
		if(referenceVariable>threshold){
			markup-=markup*adaptiveParameter*distribution.nextDouble();
		}else{
			markup+=markup*adaptiveParameter*distribution.nextDouble();
		}
		if(fundingRate+markup>avInterest){
			markup-=markup*adaptiveParameter*distribution.nextDouble();
		}else{
			markup+=markup*adaptiveParameter*distribution.nextDouble();
		}
		double iR=fundingRate+markup;
		
		return Math.min(Math.max(iR, lender.getInterestRateLowerBound(mktId)),lender.getInterestRateUpperBound(mktId));
	}

	/**
	 * @return the adaptiveParameter
	 */
	public double getAdaptiveParameter() {
		return adaptiveParameter;
	}

	/**
	 * @param adaptiveParameter the adaptiveParameter to set
	 */
	public void setAdaptiveParameter(double adaptiveParameter) {
		this.adaptiveParameter = adaptiveParameter;
	}

	/**
	 * @return the distribution
	 */
	public AbstractDelegatedDistribution getDistribution() {
		return distribution;
	}

	/**
	 * @param distribution the distribution to set
	 */
	public void setDistribution(AbstractDelegatedDistribution distribution) {
		this.distribution = distribution;
	}

	/**
	 * @return the mkId
	 */
	public int getMktId() {
		return mktId;
	}


	/**
	 * @param mkId the mkId to set
	 */
	public void setMktId(int mktId) {
		this.mktId = mktId;
	}

	public int[] getLiabilitiesId() {
		return liabilitiesId;
	}

	public void setLiabilitiesId(int[] liabilitiesId) {
		this.liabilitiesId = liabilitiesId;
	}

	public double getMarkup() {
		return markup;
	}

	public void setMarkup(double markup) {
		this.markup = markup;
	}

	/**
	 * Generate the byte array structure of the strategy. The structure is as follow:
	 * [threshold][adaptiveParameter][avInterest][mktId][increase]
	 * @return the byte array content
	 */
	@Override
	public byte[] getBytes() {
		ByteBuffer buf = ByteBuffer.allocate(21);
		buf.putDouble(adaptiveParameter);
		buf.putInt(mktId);
		return buf.array();
	}


	/**
	 * Populates the strategy from the byte array content. The structure should be as follows:
	 * [threshold][adaptiveParameter][avInterest][mktId][increase]
	 * @param content the byte array containing the structure of the strategy
	 * @param pop the Macro Population of agents
	 */
	@Override
	public void populateFromBytes(byte[] content, MacroPopulation pop) {
		ByteBuffer buf = ByteBuffer.wrap(content);
		this.adaptiveParameter = buf.getDouble();
		this.mktId = buf.getInt();
	}

}
