import java.io.*;
import java.text.ParseException;
import java.util.*;

public class BSInstance {
    private Scanner s;
	private BufferedWriter w;

    private Integer slotLength, n, sdd = 0, maxNrOfLunchBreak = 0, maxLunchBreakTime = 0,
    			minWorkingPeriod, maxWorkingPeriod, workLimit, minBreakExceedsWorkLimit, 
    			minBreakLength, maxBreakLength, lunchLatestEnd, lunchEarliestStart, earliestStart,
    			latestEnd;

	Integer[] shiftMinusRequirement, weight = new Integer[7], workers;
	private List<Shift> shifts = new ArrayList<Shift>();	
    private Shift 	tempShift;
    // To find the shift and the day of the duty 
	private Hashtable<Integer, Shift> shiftSDD    = new Hashtable<Integer, Shift>();
	private Hashtable<Integer, Integer> daySDD    = new Hashtable<Integer, Integer>();


	public BSInstance(String fileLocation) throws IOException, ParseException{	
		OpenFile(fileLocation);
		ReadFile();
		CloseFile();
	}
	
	protected void OpenFile(String fileName){
		try {
			s = new Scanner(new File("./BSdata/" + fileName + ".txt")); 
			FileWriter fstream = new FileWriter("./Result/BSSolution" + fileName);
			w = new BufferedWriter(fstream);
		}
		catch (Exception e){
			System.err.println("File could not find" + e);
		}
	}

	protected void ReadFile() throws IOException, ParseException{
		String strLine;
		String[] temp, tempTime, tempStartTime, tempLengthTime;

		while (s.hasNextLine()) {
			strLine = s.nextLine();
			strLine.trim();
			if (strLine.contains("#MinuteInterval")){
				strLine = s.nextLine();
				strLine.trim();	
				slotLength = Integer.parseInt(strLine);
				n = 24 * 7 * 60 / slotLength;
				shiftMinusRequirement = new Integer[n];
				//System.out.println("SlotLength: " + slotLength + "  n:" + n);
			}
			
			if (strLine.contains("#Staffing Requirements")){
				for (int k =0; k< n/7; k++){
					strLine = s.nextLine();
					strLine.trim();	
					temp = strLine.split(" ");
					for (int i=2; i < temp.length; i++){
						shiftMinusRequirement[k + n * (i-2)/7] = -1 * Integer.parseInt(temp[i]);
						// System.out.println("requirement[" + (k + n * (i-2)/7)+ "]: " + Integer.parseInt(temp[i]));
					}
				}
			}
			
			if (strLine.contains("requirements:")){
				slotLength = 5;
				n = 2016;
				shiftMinusRequirement = new Integer[n];
				for (int k =0; k< n/14; k++){
					strLine = s.nextLine();
					strLine.trim();	
					temp = strLine.split(" ");
					for (int i=2; i < temp.length; i++){
						shiftMinusRequirement[2 * k + n * (i-2)/7] = -1 * Integer.parseInt(temp[i]);
						//System.out.println("requirement[" + (2 * k  + n * (i-2)/7)+ "]: " + Integer.parseInt(temp[i]));

						shiftMinusRequirement[2 * k + 1 + n * (i-2)/7] = -1 * Integer.parseInt(temp[i]);

						//System.out.println("requirement[" + (2 * k + 1 + n * (i-2)/7)+ "]: " + Integer.parseInt(temp[i]));
					}
				}
			}
			
			if (strLine.contains("#Shift Plan")){
				int start, length;
				strLine = s.nextLine();
				strLine.trim();
				int m =0;
				while (strLine.contains("S")){	
					temp = strLine.split(" ");
					// System.out.println(strLine);
					tempStartTime = temp[1].split(":");
					tempLengthTime = temp[2].split(":");
					 
					start = (Integer.parseInt(tempStartTime[0]) * 60 + Integer.parseInt(tempStartTime[1])) / slotLength;
					length = (Integer.parseInt(tempLengthTime[0]) * 60 + Integer.parseInt(tempLengthTime[1])) / slotLength;
					
					workers = new Integer[7];
					for (int j=3; j < temp.length; j++){
						workers[j-3] = Integer.parseInt(temp[j]);
						//System.out.println(workers[j-3]);

					}
					tempShift = new Shift(m,  start, length);
					tempShift.setWorkers(workers);
					
					shifts.add(tempShift);
					m++;
					strLine = s.nextLine();
					strLine.trim();	
				}
				
				
				for ( int i=0; i<shifts.size(); i++){
					for (int j=0; j < 7; j++){
						for (int l=0; l < shifts.get(i).getLength(); l++){						
							shiftMinusRequirement[(shifts.get(i).getStart() + l + n * j/7)%n] += shifts.get(i).getWorkers()[j];
						}
						for (int k=0; k<shifts.get(i).getWorkers()[j]; k++){							
							shiftSDD.put(sdd, shifts.get(i));
							daySDD.put(sdd, j);
							sdd++;
						}
					}
				}
				//System.out.println(shiftDayDutyNr);

				//for(int i =0; i<shiftMinusRequirement.length; i++)
				//	System.out.println("shiftMinusRequirement[" +i +"] =" + shiftMinusRequirement[i]);
			}
			
			if (strLine.contains("start solution:")){
				int start, length;
				strLine = s.nextLine();
				strLine.trim();
				int m =0;
				while (!strLine.isEmpty()){	
					temp = strLine.split(" ");
					// System.out.println(strLine);
					tempStartTime = temp[1].split(":");
					tempLengthTime = temp[2].split(":");
					 
					start = (Integer.parseInt(tempStartTime[0]) * 60 + Integer.parseInt(tempStartTime[1])) / slotLength;
					length = (Integer.parseInt(tempLengthTime[0]) * 60 + Integer.parseInt(tempLengthTime[1])) / slotLength;
					
					
					workers = new Integer[7];
					for (int j=3; j < temp.length; j++){
						workers[j-3] = Integer.parseInt(temp[j]);
						//System.out.println(workers[j-3]);

					}
					tempShift = new Shift(m,  start, length);
					tempShift.setWorkers(workers);
					if (maxNrOfLunchBreak < (int)((Math.floor((length * slotLength - 20) / 50 )* 10)/slotLength))
						maxNrOfLunchBreak = (int)((Math.floor((length * slotLength - 20) / 50 )* 10)/slotLength);
					
					if(length <= 120){
						tempShift.setBreakTime((int)((Math.floor((length * slotLength - 20) / 50 )* 10)/slotLength));
					}
					else {
						tempShift.setBreakTime((int)(Math.ceil(length / 4)));
					}
					
					if(length <= 72){
						tempShift.setNrOfLunchBreak(0);
					}
					else {
						tempShift.setNrOfLunchBreak(1);
						maxLunchBreakTime = 1;
					}
					tempShift.setLunchBreakTime( maxLunchBreakTime * 6);

					shifts.add(tempShift);
					m++;
					strLine = s.nextLine();
					strLine.trim();	
				}	
				
				for ( int i=0; i<shifts.size(); i++){
					for (int j=0; j < 7; j++){
						for (int l=0; l < shifts.get(i).getLength(); l++){	
							shiftMinusRequirement[(shifts.get(i).getStart() + l + n * j/7)%n] += shifts.get(i).getWorkers()[j];
						}
						for (int k=0; k<shifts.get(i).getWorkers()[j]; k++){							
							shiftSDD.put(sdd, shifts.get(i));
							daySDD.put(sdd, j);
							sdd++;
						}
					}
				}
				//System.out.println(shiftDayDutyNr);

				//for(int i =0; i<shiftMinusRequirement.length; i++)
				//	System.out.println("shiftMinusRequirement[" +i +"] =" + shiftMinusRequirement[i]);
		    	minWorkingPeriod = 6;		    	
		    	maxWorkingPeriod = 20;
		    	workLimit =10;
		    	minBreakExceedsWorkLimit =4;
		    	minBreakLength=2;
		    	maxBreakLength=12;
		    	lunchLatestEnd=72;
		    	lunchEarliestStart=42;
		    	earliestStart = 6;
		    	latestEnd = 6;
			}

// set breakTime
			if (strLine.contains("#Breaktime per shift in minutes")){
				int start, end;

				strLine = s.nextLine();
				strLine.trim();	

				while (strLine.contains("SHIFT-LENGTH")){
					temp = strLine.substring(14, 19).split(":");
					start = (Integer.parseInt(temp[0]) * 60  + Integer.parseInt(temp[1])) / slotLength; 
					if (strLine.substring(22, 27).contains("    *")){
						end = 24 * 60 / slotLength;
					}
					else {
						temp = strLine.substring(22, 27).split(":");
						end = (Integer.parseInt(temp[0]) * 60  + Integer.parseInt(temp[1])) / slotLength; 
					}
					
					if(strLine.substring(40).contains("floor( ( minutes(SHIFT-LENGTH) - 20) / 50 ) * 10")){
						//System.out.println(strLine.substring(40));
						for ( int i=0; i<shifts.size(); i++){
							if( shifts.get(i).getLength() >= start && shifts.get(i).getLength() <= end){
								shifts.get(i).setBreakTime((int)((Math.floor((shifts.get(i).getLength() * slotLength - 20) / 50 )* 10)/slotLength));
								//System.out.println(Math.floor(((shifts.get(i).getLength() * slotLength - 20) / 50 )* 10)/slotLength);
								if (maxNrOfLunchBreak < (int)((Math.floor((shifts.get(i).getLength() * slotLength - 20) / 50 )* 10)/slotLength))
									maxNrOfLunchBreak = (int)((Math.floor((shifts.get(i).getLength() * slotLength - 20) / 50 )* 10)/slotLength);
							}							
						}
					}
					else if(strLine.substring(40).contains("ceil (   minutes(SHIFT-LENGTH) / 4 )")){
						//System.out.println(strLine.substring(40));
						for ( int i=0; i<shifts.size(); i++){
							if( shifts.get(i).getLength() > start && shifts.get(i).getLength() <= end){
								shifts.get(i).setBreakTime((int)(Math.ceil(shifts.get(i).getLength() / 4)));
								//System.out.println(Math.ceil(shifts.get(i).getLength() / 4));
								if (maxNrOfLunchBreak < (int)((Math.floor((shifts.get(i).getLength() * slotLength - 20) / 50 )* 10)/slotLength))
									maxNrOfLunchBreak = (int)((Math.floor((shifts.get(i).getLength() * slotLength - 20) / 50 )* 10)/slotLength);
							}							
						}
					}
					else
						System.out.println("Different Total Break Time Calculation");
					strLine = s.nextLine();
					strLine.trim();	
				}
			}

// set earliestStart, latestEnd			
			if (strLine.contains("#Break Positions")){
				strLine = s.nextLine();
				strLine.trim();
				while(!strLine.isEmpty()){	
					temp = strLine.split("	");
					tempTime = temp[2].split(":");
					if(temp[0].contains("EARLIEST-START")){
						earliestStart = (Integer.parseInt(tempTime[0]) * 60 + Integer.parseInt(tempTime[1]))/ slotLength;
						//System.out.println((Integer.parseInt(tempTime[0]) * 60 + Integer.parseInt(tempTime[1]))/ slotLength);
					}
					else if(temp[0].contains("LATEST-END")){
						latestEnd = (Integer.parseInt(tempTime[0]) * 60 + Integer.parseInt(tempTime[1]))/ slotLength;
						//System.out.println((Integer.parseInt(tempTime[0]) * 60 + Integer.parseInt(tempTime[1]))/ slotLength);
					}
					else
						System.out.println("Different Break Positions");				
					strLine = s.nextLine();
					strLine.trim();	
				}
			}	
			
// set nrOfLunchBreak, lunchBreakTime, lunchEarliestStart, lunchLatestEnd
			if (strLine.contains("#Lunch Breaks")){
				int start, end;

				strLine = s.nextLine();
				strLine.trim();	

				while (strLine.contains("SHIFT-LENGTH")){
					temp = strLine.substring(15, 20).split(":");
					start = (Integer.parseInt(temp[0]) * 60  + Integer.parseInt(temp[1])) / slotLength; 
					if (strLine.substring(23, 28).contains("    *")){
						end = 24 * 60 / slotLength;
					}
					else {
						temp = strLine.substring(23, 28).split(":");
						end = (Integer.parseInt(temp[0]) * 60  + Integer.parseInt(temp[1])) / slotLength; 
					}					

					if(strLine.substring(30).contains("LUNCHBREAKS")){
						for ( int i=0; i<shifts.size(); i++){
							if( shifts.get(i).getLength() > start && shifts.get(i).getLength() <= end){
								shifts.get(i).setNrOfLunchBreak( Integer.parseInt(strLine.substring(42)));
								if( maxLunchBreakTime < Integer.parseInt(strLine.substring(42)))
										maxLunchBreakTime = Integer.parseInt(strLine.substring(42));
								//System.out.println(Integer.parseInt(strLine.substring(42)));
							}							
						}
					}
					else
						System.out.println("Different Lunch Break Calculation");
					strLine = s.nextLine();
					strLine.trim();	
				}
				while (!strLine.contains("LUNCH-BREAK")){
					strLine = s.nextLine();
					strLine.trim();
				}
				
				strLine = s.nextLine();
				strLine.trim();
				while(!strLine.isEmpty()){
					temp = strLine.split("	");
					if (temp[1].contains("LENGTH")){
						for ( int i=0; i<shifts.size(); i++){
							tempTime = temp[3].split(":");
							shifts.get(i).setLunchBreakTime((Integer.parseInt(tempTime[0]) * 60 + Integer.parseInt(tempTime[1]))/ slotLength);
							//System.out.println((Integer.parseInt(tempTime[0]) * 60 + Integer.parseInt(tempTime[1]))/ slotLength);
						}
					}
					else if(temp[1].contains("EARLIEST-START")){
						tempTime = temp[2].split(":");
						lunchEarliestStart = (Integer.parseInt(tempTime[0]) * 60 + Integer.parseInt(tempTime[1]))/ slotLength;
						//System.out.println((Integer.parseInt(tempTime[0]) * 60 + Integer.parseInt(tempTime[1]))/ slotLength);
					}
					else if(temp[1].contains("LATEST-END")){
						tempTime = temp[2].split(":");
						lunchLatestEnd = (Integer.parseInt(tempTime[0]) * 60 + Integer.parseInt(tempTime[1]))/ slotLength;
						//System.out.println((Integer.parseInt(tempTime[0]) * 60 + Integer.parseInt(tempTime[1]))/ slotLength);
					}
					else
						System.out.println("Different Break Positions");					
					strLine = s.nextLine();
					strLine.trim();
				}				
			}
// set minWorkingPeriod, maxWorkingPeriod
			if (strLine.contains("#Length of Working Periods")){
				strLine = s.nextLine();
				strLine.trim();
				while(!strLine.isEmpty()){
					temp = strLine.split("	");
					if(temp[0].contains("MINIMUM-LENGTH")){
						tempTime = temp[2].split(":");
						minWorkingPeriod = (Integer.parseInt(tempTime[0]) * 60 + Integer.parseInt(tempTime[1]))/ slotLength;
						//System.out.println("minWorkingPeriod = " + minWorkingPeriod);
					}
					else if(temp[0].contains("MAXIMUM-LENGTH")){
						tempTime = temp[2].split(":");
						maxWorkingPeriod = (Integer.parseInt(tempTime[0]) * 60 + Integer.parseInt(tempTime[1]))/ slotLength;
						//System.out.println("maxWorkingPeriod = " + maxWorkingPeriod);
					}
					else
						System.out.println("Different Length of Working Periods : " + temp[0]);	
					strLine = s.nextLine();
					strLine.trim();					
				}
			}
			
// set workLimit, minBreakExceedsWorkLimit			
			if (strLine.contains("#Minimum Break Times ")){
				strLine = s.nextLine();
				strLine.trim();
				while(!strLine.isEmpty()){
					temp = strLine.split("	");

					if(temp[0].contains("MINIMUM-LENGTH")){
						tempTime = temp[2].split(":");
						minBreakExceedsWorkLimit = (Integer.parseInt(tempTime[0]) * 60 + Integer.parseInt(tempTime[1]))/ slotLength;
						tempTime = temp[3].substring(6, 11).split(":");
						workLimit = (Integer.parseInt(tempTime[0]) * 60 + Integer.parseInt(tempTime[1]))/ slotLength;
						//System.out.println("workLimit =" + workLimit + " , minBreakExceedsWorkLimit = " + minBreakExceedsWorkLimit);
					}					
					else
						System.out.println("Different Minimum Break Times : " + temp[0]);	
					strLine = s.nextLine();
					strLine.trim();					
				}
			}
			
// set minBreakLength, maxBreakLength
			if (strLine.contains("#Break Lengths")){
				strLine = s.nextLine();
				strLine.trim();
				while(!strLine.isEmpty()){
					temp = strLine.split("	");
					if(temp[0].contains("MINIMUM-LENGTH")){
						tempTime = temp[2].split(":");
						minBreakLength = (Integer.parseInt(tempTime[0]) * 60 + Integer.parseInt(tempTime[1]))/ slotLength;
						//System.out.println("minBreakLength = " + minBreakLength);
					}
					else if(temp[0].contains("MAXIMUM-LENGTH")){
						tempTime = temp[2].split(":");
						maxBreakLength = (Integer.parseInt(tempTime[0]) * 60 + Integer.parseInt(tempTime[1]))/ slotLength;
						//System.out.println("maxBreakLength = " + maxBreakLength);
					}
					else
						System.out.println("Different Break Lengths : " + temp[0]);	
					strLine = s.nextLine();
					strLine.trim();					
				}
			}

// set weight[i]			
			if (strLine.contains("#Constraint Weights")){
				while (!strLine.contains("-")){
				strLine = s.nextLine();
				}
				int i = 0;
				strLine = s.nextLine();
				strLine.trim();
				while(!strLine.isEmpty()){

					//System.out.println(strLine);
					temp = strLine.split("	");
					weight[i] = Integer.parseInt(temp[temp.length -1].trim());
					//System.out.println("weight[" + i + "] = " + weight[i]);
					i++;
					strLine = s.nextLine();
					strLine.trim();
				}
			}			
		}		
	}
	
	public void WriteFile(String line) throws IOException, ParseException{	
		w.write(line);
	}
	
	public Integer getMaxLunchBreakTime() {
		return maxLunchBreakTime;
	}

	public void setMaxLunchBreakTime(Integer maxLunchBreakTime) {
		this.maxLunchBreakTime = maxLunchBreakTime;
	}

	public Integer getMinWorkingPeriod() {
		return minWorkingPeriod;
	}

	public void setMinWorkingPeriod(Integer minWorkingPeriod) {
		this.minWorkingPeriod = minWorkingPeriod;
	}

	public Integer getMaxWorkingPeriod() {
		return maxWorkingPeriod;
	}

	public void setMaxWorkingPeriod(Integer maxWorkingPeriod) {
		this.maxWorkingPeriod = maxWorkingPeriod;
	}

	public Integer getWorkLimit() {
		return workLimit;
	}

	public void setWorkLimit(Integer workLimit) {
		this.workLimit = workLimit;
	}

	public Integer getMinBreakExceedsWorkLimit() {
		return minBreakExceedsWorkLimit;
	}

	public void setMinBreakExceedsWorkLimit(Integer minBreakExceedsWorkLimit) {
		this.minBreakExceedsWorkLimit = minBreakExceedsWorkLimit;
	}

	public Integer getMinBreakLength() {
		return minBreakLength;
	}

	public void setMinBreakLength(Integer minBreakLength) {
		this.minBreakLength = minBreakLength;
	}

	public Integer getMaxBreakLength() {
		return maxBreakLength;
	}

	public void setMaxBreakLength(Integer maxBreakLength) {
		this.maxBreakLength = maxBreakLength;
	}

	public void CloseFile() throws IOException{
		s.close();
	}
	
	public void CloseWriteFile() throws IOException{
		w.flush();
		w.close();
	}
	
	public Integer getSlotLength() {
		return slotLength;
	}

	public void setSlotLength(Integer slotLength) {
		this.slotLength = slotLength;
	}

	public Integer getN() {
		return n;
	}

	public void setN(Integer n) {
		this.n = n;
	}

	public Integer[] getShiftMinusRequirement() {
		return shiftMinusRequirement;
	}

	public void setShiftMinusRequirement(Integer[] shiftMinusRequirement) {
		this.shiftMinusRequirement = shiftMinusRequirement;
	}

	public Integer[] getWeight() {
		return weight;
	}

	public void setWeight(Integer[] weight) {
		this.weight = weight;
	}
    
	public Integer getSdd() {
		return sdd;
	}

	public void setSdd(Integer sdd) {
		this.sdd = sdd;
	}


	public Hashtable<Integer, Shift> getShiftSDD() {
		return shiftSDD;
	}

	public void setShiftSDD(Hashtable<Integer, Shift> shiftSDD) {
		this.shiftSDD = shiftSDD;
	}

	public Hashtable<Integer, Integer> getDaySDD() {
		return daySDD;
	}

	public void setDaySDD(Hashtable<Integer, Integer> daySDD) {
		this.daySDD = daySDD;
	}	

	public Integer getMaxNrOfLunchBreak() {
		return maxNrOfLunchBreak;
	}

	public void setMaxNrOfLunchBreak(Integer maxNrOfLunchBreak) {
		this.maxNrOfLunchBreak = maxNrOfLunchBreak;
	}

	public Integer getLunchLatestEnd() {
		return lunchLatestEnd;
	}

	public void setLunchLatestEnd(Integer lunchLatestEnd) {
		this.lunchLatestEnd = lunchLatestEnd;
	}

	public Integer getLunchEarliestStart() {
		return lunchEarliestStart;
	}

	public void setLunchEarliestStart(Integer lunchEarliestStart) {
		this.lunchEarliestStart = lunchEarliestStart;
	}

	public Integer getEarliestStart() {
		return earliestStart;
	}

	public void setEarliestStart(Integer earliestStart) {
		this.earliestStart = earliestStart;
	}

	public Integer getLatestEnd() {
		return latestEnd;
	}

	public void setLatestEnd(Integer latestEnd) {
		this.latestEnd = latestEnd;
	}
}

