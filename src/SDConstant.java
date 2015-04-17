import java.util.List;

public class SDConstant {
	SDInstance instance;
	Integer n, m, daysPerCycle, slotLength;
	Integer[] x;
	Integer[] weight = new Integer[3];
	List<Shift> tempShift;
	Boolean[] nextDay;
	Integer[][] a;
	long startTime;

	public void modelConstraints(SDInstance instance){
		startTime = System.currentTimeMillis();
		this.instance = instance;
		n = instance.getN();
		x = new Integer[n];
		x = instance.getX();
		weight = instance.getWeight();
		m = instance.getM();
		daysPerCycle = instance.getDaysPerCycle();
		slotLength = instance.getSlotLength();
		// Initialization of variable a
		System.out.println("nrOfPossibleShift (m) : " + m + "  " + "nrOfTimeSlot (n) : " + n);

		a = new Integer[n][m];
		nextDay = new Boolean[m];
		
		for (int t =0; t< n; t++){
			for (int s =0; s< m; s++){
				a[t][s] = 0;
			}
		}		
		
		for (int i =0; i< instance.getNrOfShiftTemplates(); i++){
			tempShift = instance.getShifts();
			int j, l;
			for (int s =0; s< tempShift.size(); s++){		
				j = tempShift.get(s).getStart();
				l = tempShift.get(s).getLength();
				nextDay[s] = false;
				for (int k =0; k< l; k++){								
					for (int d =0; d< daysPerCycle; d++){		
						a[(j+k + 24*d*60/instance.getSlotLength())%n][s] = 1;
						//System.out.println("a[" + (j+k + 24*d*60/instance.getSlotLength())%n + "," + s+ "] = " + 1);
						if((j+k + 24*d*60/instance.getSlotLength())/n == 1){
							nextDay[s] = true;
							//System.out.println("a[" + (j+k + 24*d*60/instance.getSlotLength())%n + "," + s+ "] = " + 1);
						}
					}
				}
			}
		}
	}
}
