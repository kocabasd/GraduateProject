import java.util.Hashtable;

public class BSConstant {
	BSInstance instance;
	Integer n, sdd, slotLength, maxNrOfLunchBreak, maxLunchBreakTime, minWorkingPeriod, maxWorkingPeriod, 
			workLimit, minBreakExceedsWorkLimit, minBreakLength, maxBreakLength, lunchLatestEnd, 
			lunchEarliestStart, earliestStart, latestEnd;
	Hashtable<Integer, Shift> shiftSDD    = new Hashtable<Integer, Shift>();
	Hashtable<Integer, Integer> daySDD    = new Hashtable<Integer, Integer>();
	Integer[] shiftMinusRequirement, weight = new Integer[7];
	long start;

	public void modelConstraints(BSInstance instance){		
		start = System.currentTimeMillis();
		this.instance = instance;
		n = instance.getN();
		sdd = instance.getSdd();
		shiftMinusRequirement = new Integer[n];
		shiftMinusRequirement = instance.getShiftMinusRequirement();
		slotLength = instance.getSlotLength();
		weight = instance.getWeight();
		shiftSDD = instance.getShiftSDD();
		daySDD = instance.getDaySDD();
		maxNrOfLunchBreak = instance.getMaxNrOfLunchBreak();
		maxLunchBreakTime = instance.getMaxLunchBreakTime();
		minWorkingPeriod = instance.getMinWorkingPeriod();
		maxWorkingPeriod = instance.getMaxWorkingPeriod();
		workLimit = instance.getWorkLimit();
		minBreakExceedsWorkLimit = instance.getMinBreakExceedsWorkLimit();
		minBreakLength = instance.getMinBreakLength();
		maxBreakLength = instance.getMaxBreakLength();
		lunchLatestEnd = instance.getLunchLatestEnd();
		lunchEarliestStart = instance.getLunchEarliestStart();
		earliestStart = instance.getEarliestStart();
		latestEnd = instance.getLatestEnd();
	}
}

