import java.io.*;
import java.text.ParseException;
import java.util.*;

public class SDValidation {
    private Scanner s1;
    private Scanner s2;
	String dataSet, fileName;

    private Integer nrOfShift, nrOfUnderCover, nrOfOverCover, slotLength, n, daysPerCycle;
	Integer[] x, y;

    
	public SDValidation(String fileLocation, Boolean CplexorGurobi) throws IOException, ParseException{	
		OpenFile(fileLocation, CplexorGurobi);
		ReadFile();
		CloseFile();
	}

	protected void OpenFile(String fileLocation, Boolean CplexorGurobi){
		try {
			dataSet = fileLocation.split("-")[0];
			fileName = fileLocation.split("-")[1];
			s1 = new Scanner(new File("./SDdata/DataSet" + dataSet + "/" + fileName  + ".txt")); 
			if(CplexorGurobi)
				s2 = new Scanner(new File("./Result/SDCplex" + fileLocation));
			else
				s2 = new Scanner(new File("./Result/SDGurobi" + fileLocation));

		}
		catch (Exception e){
			System.out.println("File could not find");
		}
	}


	protected void ReadFile() throws IOException, ParseException{
		String strLine;
		String[] temp, tempStart;		
		
		while (s1.hasNextLine()) {			  
			strLine = s1.nextLine();
			strLine.trim();
			if (strLine.contains("#MinuteInterval:")){
				strLine = s1.nextLine();
				strLine.trim();	
				slotLength = Integer.parseInt(strLine);
				//System.out.print("SlotLength: " + slotLength + "  ");
			}
			
			if (strLine.contains("#DaysPerCycle:")){
				strLine = s1.nextLine();
				strLine.trim();	
				daysPerCycle = Integer.parseInt(strLine);
				n = 24 * daysPerCycle * 60 / slotLength;
				x = new Integer[n];
				y = new Integer[n];

				//System.out.print("DaysPerCycle: " + daysPerCycle + "  " );
			}
			
		
			if (strLine.contains("#Requirements:")){
				strLine = s1.nextLine();
				strLine.trim();	
				temp = strLine.split(" ");
				for (int i=0; i < temp.length; i++){
					x[i] = Integer.parseInt(temp[i]);
					y[i] = 0;
					//System.out.println("x["+ i + "]: " + x[i] );
				}
			}

		}
		while (s2.hasNextLine()) {
			int shift =0;
			strLine = s2.nextLine();
			if (strLine.contains("#Number of Undercover:")){
				strLine = s2.nextLine();
				strLine.trim();	
				nrOfUnderCover = Integer.parseInt(strLine);
				System.out.println(nrOfUnderCover);

			}

			if (strLine.contains("#Number of Overcover:")){
				strLine = s2.nextLine();
				strLine.trim();	
				nrOfOverCover = Integer.parseInt(strLine);
				System.out.println(nrOfOverCover);

			}
			
			if (strLine.contains("#Number of Shifts:")){
				strLine = s2.nextLine();
				strLine.trim();	
				nrOfShift = Integer.parseInt(strLine);
				System.out.println(nrOfShift);
			}
			
			if (strLine.contains("#Shift Start Length Duties:")){
				while (s2.hasNextLine()) {
					strLine = s2.nextLine();
					strLine.trim();	
					temp = strLine.split(" ");
					shift++;
					temp[2].trim();	
					tempStart = temp[3].split(":");
	
					for (int j = 0; j < daysPerCycle; j++){
						for (int i = 0; i< Integer.parseInt(temp[7]) / slotLength; i++){
							y[(j * 24 * 60 / slotLength + ((Integer.parseInt(tempStart[0]) * 60 + Integer.parseInt(tempStart[1])) / slotLength + i))%n] += Integer.parseInt(temp[14 + 2 * j]);
						}
					}
				}
				int overCover = 0, underCover = 0;
				for (int i = 0; i < n; i++ ){	
					// System.out.println("x [" + i + "] = " +  (y[i]) );
					// System.out.println("y [" + i + "] = " +  (y[i]) );

					if(y[i] > x[i]){
						//System.out.println("Overcover [" + i + "] = " +  (y[i] - x[i]) );
						overCover += y[i]- x[i];
					}
					
					if(y[i] < x[i]){
						//System.out.println("Undercover [" + i + "] = " +  (- y[i] + x[i]) );
						underCover += x[i]- y[i];
					}
				}
				System.out.println();
				if(nrOfOverCover == overCover * slotLength && nrOfUnderCover == underCover * slotLength && nrOfShift == shift){
					System.out.println("The validation succeeded") ;
				}
				else{
					System.out.println("The validation failed") ;
					if(nrOfOverCover != overCover * slotLength)
						System.out.println("Overcover =" + overCover* slotLength);
					if(nrOfUnderCover != underCover * slotLength)
						System.out.println("Undercover =" + underCover* slotLength);
					if(nrOfShift != shift)
						System.out.println("Shifts =" + shift) ;
				}
			}
		}
	}
	
	public void CloseFile() throws IOException{
		s1.close();	
		s2.close();		
	}

}
