import java.io.IOException;
import java.text.ParseException;

public class Main {

	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException, ParseException {

		if (args.length != 2){
			System.err.println("Usage: java GraduateProject arg0 arg1" + args[0]);
			System.exit(0);
		}

		if (args[0].equals("0")){
			System.out.println("Cplex Solver - Shift Design" + " FileName: " + args[1]);
			
			// read instance 
			SDInstance sdInstance = new SDInstance(args[1], true);
			
			// solve instance with Cplex
			SDCplex sdCplex = new SDCplex();
			sdCplex.model(sdInstance);
			SDValidation sdCValidation = new SDValidation(args[1], true);
		}
		
		if (args[0].equals("1")){			
			System.out.println("Gurobi Solver - Shift Design" + " FileName: " + args[1]);

			// read instance
			SDInstance sdInstance = new SDInstance(args[1], false);
			
			// solve instance with Gurobi
			SDGurobi sdGurobi = new SDGurobi();
			sdGurobi.model(sdInstance);
			SDValidation sdGValidation = new SDValidation(args[1], false);
		}
		
		if (args[0].equals("2")){
			System.out.println("Cplex Solver - Break Scheduling" + " FileName: " + args[1]);
			BSInstance bsInstance = new BSInstance(args[1]);
			
			BSCplex bsCplex = new BSCplex();
			bsCplex.model(bsInstance);
		}
		
		if (args[0].equals("3")){
			System.out.println("Validation - Shift Design" + " FileName: " + args[1]);

			SDValidation sdCValidation = new SDValidation(args[1], true);
			SDValidation sdGValidation = new SDValidation(args[1], false);

		}



		

		
	}
}

