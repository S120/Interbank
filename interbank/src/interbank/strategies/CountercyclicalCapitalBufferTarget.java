/**
 * 
 */
package interbank.strategies;

import jmab.population.MacroPopulation;
import net.sourceforge.jabm.EventScheduler;
import net.sourceforge.jabm.agent.Agent;
import net.sourceforge.jabm.strategy.AbstractStrategy;

/**
 * @author joeri
 *
 */
public class CountercyclicalCapitalBufferTarget extends AbstractStrategy
		implements MacroPrudentialStrategy {

	/* (non-Javadoc)
	 * @see interbank.strategies.MacroPrudentialStrategy#computePolicyTarget()
	 */
	@Override
	public double computePolicyTarget() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * 
	 */
	public CountercyclicalCapitalBufferTarget() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param agent
	 */
	public CountercyclicalCapitalBufferTarget(Agent agent) {
		super(agent);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param scheduler
	 * @param agent
	 */
	public CountercyclicalCapitalBufferTarget(EventScheduler scheduler,
			Agent agent) {
		super(scheduler, agent);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see jmab.strategies.SingleStrategy#getBytes()
	 */
	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jmab.strategies.SingleStrategy#populateFromBytes(byte[], jmab.population.MacroPopulation)
	 */
	@Override
	public void populateFromBytes(byte[] content, MacroPopulation pop) {
		// TODO Auto-generated method stub

	}


}
