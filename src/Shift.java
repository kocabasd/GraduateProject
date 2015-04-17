
public class Shift {
	private Integer shiftNr;
	private Integer start;
	private Integer length;
	private Integer[] workers;
	private Integer breakTime;
	private Integer nrOfLunchBreak;
	private Integer lunchBreakTime;

	
	public Shift(Integer shiftNr, Integer start, Integer length){
		this.shiftNr = shiftNr;
		this.start = start;
		this.length = length;
		this.setBreakTime((int)0);
	}
	

	public Integer[] getWorkers() {
		return workers;
	}


	public void setWorkers(Integer[] workers) {
		this.workers = workers;
	}
	
	public Integer getLunchBreakTime() {
		return lunchBreakTime;
	}

	public void setLunchBreakTime(Integer lunchBreakTime) {
		this.lunchBreakTime = lunchBreakTime;
	}

	public Integer getShiftNr() {
		return shiftNr;
	}

	public Integer getShiftNre() {
		return shiftNr;
	}

	public void setShiftNr(Integer shiftNr) {
		this.shiftNr = shiftNr;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Integer getNrOfLunchBreak() {
		return nrOfLunchBreak;
	}

	public void setNrOfLunchBreak(Integer nrOfLunchBreak) {
		this.nrOfLunchBreak = nrOfLunchBreak;
	}

	public Integer getBreakTime() {
		return breakTime;
	}

	public void setBreakTime(int breakTime) {
		this.breakTime = breakTime;
	}

    public String toString() {
        return 	"Shift " + (shiftNr + 1) + ": " + String.format("%02d",(start / 12)) + ":"  + String.format("%02d",(start %12)*5) + " "+ String.format("%02d",length / 12) + ":"  + String.format("%02d",(length %12)*5) 
        		+ " " + workers[0] + " " + workers[1] + " " + workers[2] + " " + workers[3] + " " + workers[4] + " " + workers[5] + " " + workers[6];
    }
}
