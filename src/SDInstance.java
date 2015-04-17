import java.io.*;
import java.text.ParseException;
import java.util.*;

public class SDInstance {
    private Scanner s;
	private BufferedWriter w;
	String dataSet, fileName;

    private Integer slotLength, m, n, daysPerCycle, nrOfShiftTemplates, maxEmployeeNeeded, minStart, maxStart, minLength, maxLength;
	double dutiesPerWeekLowerLimit, dutiesPerWeekUpperLimit;
    private Boolean flagCyclical;    
	Integer[] x;
	private List<Shift> shifts = new ArrayList<Shift>();	
    private Shift 	tempShift;
    private Double avgNrOfHoursPerWeek;
	Integer[] weight = new Integer[3];
    
	public SDInstance(String fileLocation, Boolean CplexorGurobi) throws IOException, ParseException{	
		OpenFile(fileLocation, CplexorGurobi);
		ReadFile();
		CloseFile();
	}

	protected void OpenFile(String fileLocation, Boolean CplexorGurobi){
		try {
			FileWriter fstream;
			dataSet = fileLocation.split("-")[0];
			fileName = fileLocation.split("-")[1];

			s = new Scanner(new File("./SDdata/DataSet" + dataSet + "/" + fileName  + ".txt")); 
			if(CplexorGurobi)
				fstream = new FileWriter("./Result/SDCplex" + fileLocation);
			else
				fstream = new FileWriter("./Result/SDGurobi" + fileLocation);
			w = new BufferedWriter(fstream);
		}
		catch (Exception e){
			System.err.println("File could not find" + e);
		}
	}

	protected void ReadFile() throws IOException, ParseException{
		String strLine;
		String[] temp, tempStart;
		
		while (s.hasNextLine()) {			  
			strLine = s.nextLine();
			strLine.trim();
			if (strLine.contains("#MinuteInterval:")){
				strLine = s.nextLine();
				strLine.trim();	
				slotLength = Integer.parseInt(strLine);
				//System.out.print("SlotLength: " + slotLength + "  ");
			}
			
			if (strLine.contains("#DaysPerCycle:")){
				strLine = s.nextLine();
				strLine.trim();	
				daysPerCycle = Integer.parseInt(strLine);
				n = 24 * getDaysPerCycle() * 60 / slotLength;
				x = new Integer[n];
				//System.out.print("DaysPerCycle: " + daysPerCycle + "  " );
			}
			
			if (strLine.contains("#FlagCyclical:")){
				strLine = s.nextLine();
				strLine.trim();	
				if(strLine.contains("yes")){
						flagCyclical = true;
				}
				else 
					flagCyclical = false;
				//System.out.print("FlagCyclical: " + flagCyclical + "  " );
			}
			
			if (strLine.contains("#Requirements:")){
				maxEmployeeNeeded = 0;
				strLine = s.nextLine();
				strLine.trim();	
				temp = strLine.split(" ");
				for (int i=0; i < temp.length; i++){
					x[i] = Integer.parseInt(temp[i]);
					if(x[i] >= maxEmployeeNeeded )
						maxEmployeeNeeded = x[i];
					//System.out.println("x["+ i + "]: " + x[i] );
				}
			}
			
			if (strLine.contains("#Number of shift templates")){
				strLine = s.nextLine();
				strLine.trim();	
				nrOfShiftTemplates = Integer.parseInt(strLine);
				//System.out.println("nrOfShiftTemplates: " + nrOfShiftTemplates + "  " );
			}
			
			if (strLine.contains("#Shift templates")){
				strLine = s.nextLine();
				m = 0;
				for (int i=0; i<nrOfShiftTemplates; i++){
					strLine = s.nextLine();
					strLine.trim();
					temp  = strLine.split(" ");					
					tempStart = temp[2].split(":");
					
					minStart = (Integer.parseInt(tempStart[0]) * 60 + Integer.parseInt(tempStart[1]) - Integer.parseInt(temp[4])) / slotLength;
					maxStart = (Integer.parseInt(tempStart[0]) * 60 + Integer.parseInt(tempStart[1]) + Integer.parseInt(temp[3])) / slotLength;
					minLength = (Integer.parseInt(temp[5]) - Integer.parseInt(temp[7])) / slotLength;
					maxLength = (Integer.parseInt(temp[5]) + Integer.parseInt(temp[6])) / slotLength;
					for (int j =minStart; j<= maxStart; j++){
						for (int l =minLength; l<= maxLength; l++){
							tempShift = new Shift(m,  j,	l);
							shifts.add(tempShift);
							m++;
						}
					}					
					
				}
			}
			
			if (strLine.contains("#Duties per week: Lower limit, Upper limit")){
				strLine = s.nextLine();
				strLine.trim();
				temp  = strLine.split(" ");					
			    dutiesPerWeekLowerLimit = Double.parseDouble(temp[0]);
			    dutiesPerWeekUpperLimit =  Double.parseDouble(temp[1]);
				//System.out.println("DutiesPerWeekLowerLimit: " + dutiesPerWeekLowerLimit + ", DutiesPerWeekUpperLimit: " + dutiesPerWeekUpperLimit );
			}
			
			if (strLine.contains("# Average number of hours per week")){
				strLine = s.nextLine();
				strLine.trim();
				avgNrOfHoursPerWeek = Double.parseDouble(strLine);
				//System.out.println("AvgNrOfHoursPerWeek: " + avgNrOfHoursPerWeek);
				
			}
			
			if (strLine.contains("# Weights about the criteria")){
				while (s.hasNextLine()) {
					strLine = s.nextLine();
					strLine.trim();
					if (strLine.contains("#UnderCover:")){
						strLine = s.nextLine();
						strLine.trim();
						weight[0] = Integer.parseInt(strLine);
						//System.out.println("WeightsUnderCover: " + weight[0]);
					}
					
					else if (strLine.contains("#Overcover:")){
						strLine = s.nextLine();
						strLine.trim();
						weight[1] = Integer.parseInt(strLine);
						//System.out.println("WeightsOvercover: " + weight[1]);						
					}
					
					else if (strLine.contains("#Shifts:")){
						strLine = s.nextLine();
						strLine.trim();
						weight[2] = Integer.parseInt(strLine);
						//System.out.println("WeightsShifts: " + weight[2]);
						break;
					}
				}
			}
		}
	}
	
	public void WriteFile(String line) throws IOException, ParseException{	
		w.write(line);
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

	public Integer getDaysPerCycle() {
		return daysPerCycle;
	}

	public void setDaysPerCycle(Integer daysPerCycle) {
		this.daysPerCycle = daysPerCycle;
	}

	public Boolean getFlagCyclical() {
		return flagCyclical;
	}

	public void setFlagCyclical(Boolean flagCyclical) {
		this.flagCyclical = flagCyclical;
	}

	public Integer[] getX() {
		return x;
	}
	
	public void setX(Integer[] x) {
		this.x = x;
	}

	public Integer getNrOfShiftTemplates() {
		return nrOfShiftTemplates;
	}

	public void setNrOfShiftTemplates(Integer nrOfShiftTemplates) {
		this.nrOfShiftTemplates = nrOfShiftTemplates;
	}

	public Integer getMinStart() {
		return minStart;
	}

	public void setMinStart(Integer minStart) {
		this.minStart = minStart;
	}

	public Integer getMaxStart() {
		return maxStart;
	}

	public void setMaxStart(Integer maxStart) {
		this.maxStart = maxStart;
	}

	public Integer getMinLength() {
		return minLength;
	}

	public void setMinLength(Integer minLength) {
		this.minLength = minLength;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

	public List<Shift> getShifts() {
		return shifts;
	}

	public void setShifts(List<Shift> shifts) {
		this.shifts = shifts;
	}

	public Double getAvgNrOfHoursPerWeek() {
		return avgNrOfHoursPerWeek;
	}

	public void setAvgNrOfHoursPerWeek(Double avgNrOfHoursPerWeek) {
		this.avgNrOfHoursPerWeek = avgNrOfHoursPerWeek;
	}

	public Integer[] getWeight() {
		return weight;
	}

	public void setWeight(Integer[] weight) {
		this.weight = weight;
	}

	public Integer getN() {
		return n;
	}

	public void setN(Integer n) {
		this.n = n;
	}

	public Integer getMaxEmployeeNeeded() {
		return maxEmployeeNeeded;
	}

	public void setMaxEmployeeNeeded(Integer maxEmployeeNeeded) {
		this.maxEmployeeNeeded = maxEmployeeNeeded;
	}

	public Integer getM() {
		return m;
	}

	public void setM(Integer m) {
		this.m = m;
	}
}
