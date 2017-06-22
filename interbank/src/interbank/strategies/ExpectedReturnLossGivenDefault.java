package interbank.strategies;

import interbank.agents.Bank;

import java.nio.ByteBuffer;

import jmab.agents.CreditDemander;
import jmab.agents.MacroAgent;
import jmab.population.MacroPopulation;
import jmab.stockmatrix.Item;
import jmab.strategies.DefaultProbilityComputer;
import jmab.strategies.SpecificCreditSupplyStrategy;
import net.sourceforge.jabm.strategy.AbstractStrategy;

@SuppressWarnings("serial")
public class ExpectedReturnLossGivenDefault extends AbstractStrategy implements SpecificCreditSupplyStrategy {

	private DefaultProbilityComputer defaultComputer;
	private int loansId;	
	private int capitalId;
	private double haircut;
	private boolean binaryDecision;


	/* (non-Javadoc)
	 * @see jmab.strategies.SpecificCreditSupplyStrategy#computeSpecificSupply(jmab.agents.MacroAgent, double)
	 */
	@Override
	public double computeSpecificSupply(MacroAgent creditDemander,
			double required) {
		Bank creditSupplier= (Bank) this.getAgent();
		int duration=((CreditDemander)creditDemander).decideLoanLength(loansId);
		double riskFreeRate =  creditSupplier.getReserveInterestRate();
		double interest=creditSupplier.getInterestRate(loansId, creditDemander, required, duration);
		double periodicRePayment=required*(double) 1/ (double) duration;
		// Calculate NPV of cash flows
		double PVCashFlows = 0;
		double amountOwed = required;
		for (int t=1; t<(duration+1); t++) {
			double discountedCashFlow = (periodicRePayment + amountOwed*interest) / (Math.pow((1+riskFreeRate), t));
			amountOwed -= periodicRePayment;
			PVCashFlows += discountedCashFlow;
		}
		double expectedNPV = PVCashFlows - required;
		// Calculate expected loss
		double collateralValue=0;
		for (Item capital:creditDemander.getItemsStockMatrix(true, capitalId) ){
			collateralValue+=capital.getValue()*haircut;
		}
		// For the company that applies for a loan calculate its total debt
		double totCurrentDebt = 0;
		for (Item loan:creditDemander.getItemsStockMatrix(false, loansId)){
			totCurrentDebt+=loan.getValue();
		}
		double extraCollateralObtainedThroughLoan = 0;
		if (collateralValue > 0){
			extraCollateralObtainedThroughLoan = required;
		}
		collateralValue = collateralValue + extraCollateralObtainedThroughLoan - totCurrentDebt;
		double lossGivenDefault = 1;
		if (collateralValue > required) {
			lossGivenDefault = 0;
		}
		if ((collateralValue < required) && (collateralValue > 0)) {
			lossGivenDefault = (required- collateralValue) / required;
			}
		double probabilityOfDefault = defaultComputer.getDefaultProbability(creditDemander, creditSupplier, required);
		double expectedLoss = lossGivenDefault * probabilityOfDefault * required;
		
		if (binaryDecision==true){
			if (expectedNPV>=expectedLoss){
				return required;
			}
			else{
				return 0;
			}
		}
		else{
			if (expectedNPV>=expectedLoss){
				return required;
			}
			else{
				return 0;
			}
		}
		

	}
	/**
	 * @return the defaultComputer
	 */
	public DefaultProbilityComputer getDefaultComputer() {
		return defaultComputer;
	}
	/**
	 * @param defaultComputer the defaultComputer to set
	 */
	public void setDefaultComputer(DefaultProbilityComputer defaultComputer) {
		this.defaultComputer = defaultComputer;
	}
	/**
	 * @return the loansId
	 */
	public int getLoansId() {
		return loansId;
	}
	/**
	 * @param loansId the loansId to set
	 */
	public void setLoansId(int loansId) {
		this.loansId = loansId;
	}
	/**
	 * @return the capitalId
	 */
	public int getCapitalId() {
		return capitalId;
	}
	/**
	 * @param capitalId the capitalId to set
	 */
	public void setCapitalId(int capitalId) {
		this.capitalId = capitalId;
	}
	/**
	 * @return the haircut
	 */
	public double getHaircut() {
		return haircut;
	}
	/**
	 * @param haircut the haircut to set
	 */
	public void setHaircut(double haircut) {
		this.haircut = haircut;
	}
	/**
	 * @return the binaryDecision
	 */
	public boolean isBinaryDecision() {
		return binaryDecision;
	}
	/**
	 * @param binaryDecision the binaryDecision to set
	 */
	public void setBinaryDecision(boolean binaryDecision) {
		this.binaryDecision = binaryDecision;
	}
	
	/**
	 * Generate the byte array structure of the strategy. The structure is as follow:
	 * [haircut][loansId][capitalId][binaryDecision][computerSize][computerStructure]
	 * @return the byte array content
	 */
	@Override
	public byte[] getBytes() {
		byte[] computerStructure = this.defaultComputer.getBytes();
		ByteBuffer buf = ByteBuffer.allocate(21+computerStructure.length);
		buf.putDouble(haircut);
		buf.putInt(loansId);
		buf.putInt(capitalId);
		if(binaryDecision)
			buf.put((byte)1);
		else
			buf.put((byte)0);
		buf.putInt(computerStructure.length);
		buf.put(computerStructure);
		return buf.array();
	}


	/**
	 * Populates the strategy from the byte array content. The structure should be as follows:
	 * [haircut][loansId][capitalId][binaryDecision][computerSize][computerStructure]
	 * @param content the byte array containing the structure of the strategy
	 * @param pop the Macro Population of agents
	 */
	@Override
	public void populateFromBytes(byte[] content, MacroPopulation pop) {
		ByteBuffer buf = ByteBuffer.wrap(content);
		this.haircut = buf.getDouble();
		this.loansId = buf.getInt();
		this.capitalId = buf.getInt();
		this.binaryDecision=buf.get()==(byte)1;
		int sizeComputer = buf.getInt();
		byte[] computerStructure = new byte[sizeComputer];
		buf.get(computerStructure);
		this.defaultComputer.populateFromBytes(computerStructure, pop);
	}

}
