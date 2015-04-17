import java.io.IOException;
import java.text.ParseException;

import ilog.cplex.*;
import ilog.concert.*;

public class SDCplex extends SDConstant{
	IloCplex cplex;
	IloNumVar[] b, l, ex, sh, F;
	IloNumVar[][] w;
	
	public void model(SDInstance instance) throws IOException, ParseException{
		// Initialization of n, m, w, x and a variables
		modelConstraints(instance);
		// Initialization of Cplex solver
		initCplex();
		// Initialization of Cplex variables
		modelCplexVariables();
		// Initialization of Cplex constraints
		modelCplexConstraints();
		// Solve Cplex model
		solve();
	}
	
	public void initCplex(){
		try {
			cplex = new IloCplex();
			//cplex.setParam(IloCplex.DoubleParam.TiLim, 3600);
			cplex.setParam(IloCplex.DoubleParam.EpGap, 0.02 );
			//cplex.setParam(IloCplex.DoubleParam.EpAGap, 30);
		} 
		catch(IloException e){
			e.printStackTrace();
		}
	}
	
	public void modelCplexVariables() {
		try {
			// Initialization of variable b		
			b = cplex.numVarArray(m, 0, 1, IloNumVarType.Bool);
			
			// Initialization of variable w
			w = new IloNumVar[m][];
			for (int s = 0; s < m; s++){
				w[s] = cplex.numVarArray(daysPerCycle, 0, Integer.MAX_VALUE, IloNumVarType.Int);				
			}			
			
			// Initialization of variable l
			l = cplex.numVarArray(n, 0, Integer.MAX_VALUE, IloNumVarType.Int);
			
			// Initialization of variable ex
			ex = cplex.numVarArray(n, 0, Integer.MAX_VALUE, IloNumVarType.Int);
			
			// Initialization of variable sh
			sh = cplex.numVarArray(n, 0, Integer.MAX_VALUE, IloNumVarType.Int);
			
			// Initialization of variable F
			F = cplex.numVarArray(3, 0, Integer.MAX_VALUE, IloNumVarType.Int);
		} 
		catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	public void modelCplexConstraints() {
		try {
			IloLinearNumExpr obj = cplex.linearNumExpr();
			for ( int i = 0; i < 3; i++){
				obj.addTerm(weight[i], F[i]);
			}
			
			// Objective Function			
			cplex.addMinimize(obj);
						
			// Constraint 1 (6)
			IloLinearNumExpr[] expr1 = new IloLinearNumExpr[m];
			for ( int s = 0; s < m; s++){
				expr1[s] = cplex.linearNumExpr();
				expr1[s].addTerm(b[s], instance.getMaxEmployeeNeeded()); 
				for ( int d = 0; d < daysPerCycle; d++){
					cplex.addLe(w[s][d], expr1[s]);
				}
			}
			
			// Constraint 2 (7)
			Integer tempDay;
			IloLinearNumExpr[] expr2 = new IloLinearNumExpr[n];
			for ( int t = 0; t < n; t++){
				expr2[t] = cplex.linearNumExpr();
				for ( int s = 0; s < m; s++){
					if(nextDay[s] == true && t % (24 * 60 / slotLength) < 12 * 60 / slotLength ){
						tempDay = ((daysPerCycle *t/n) + daysPerCycle -1) %daysPerCycle;
					}
					else 
						tempDay = (daysPerCycle *t/n);

					expr2[t].addTerm(a[t][s], w[s][tempDay]);
				}
				cplex.addEq (l[t], expr2[t]);
			}
			
			// Constraint 3 (8) 4 (9)
			IloLinearNumExpr[] expr3 = new IloLinearNumExpr[n];
			for ( int t = 0; t < n; t++){
				expr3[t] = cplex.linearNumExpr();
				expr3[t].addTerm(slotLength, l[t]);
				expr3[t].addTerm(-1.0, ex[t]);
				expr3[t].addTerm(1.0, sh[t]);
				cplex.addEq (expr3[t], slotLength*x[t]);
			}
			cplex.addEq (F[0], cplex.sum(ex));
			cplex.addEq (F[1], cplex.sum(sh));	
			
			// Constraint 5 (10)
			cplex.addEq (F[2], cplex.sum(b));
			
		} 
		catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	public void solve() throws IOException, ParseException{
		try{	

			if (cplex.solve()){	
				System.out.println("CPLEX Status: " + cplex.getStatus());
				System.out.println("Branch & Bound Nodes: " + cplex.getNnodes());
				System.out.println("CPU Time: " + ((System.currentTimeMillis() - startTime)/1000) );
				
				instance.WriteFile("#CPLEX Status: \n" + cplex.getStatus() + "\n \n");
				instance.WriteFile("#Branch & Bound Nodes: \n" + cplex.getNnodes() + "\n \n");
				instance.WriteFile("#CPU Time: \n" + ((System.currentTimeMillis() - startTime)/1000)  + "\n \n");
				
				// Show excesses and shortages of TimeSlot t 
				double[] Excesses = cplex.getValues(ex); 
				double[] Shortages = cplex.getValues(sh); 
				double[] Load = cplex.getValues(l); 
				for (int j = 0; j < n; ++j) {
					if (Math.round(Excesses[j]) != 0 || Math.round(Shortages[j]) != 0)
					System.out.println("Excesses/Shortages of TimeSlot " + j + " = " + Excesses[j]/slotLength+ " / " + Shortages[j]/slotLength+ "         Load = " + Load[j] +"        nrOfNeededEmployees: " + x[j]); 
				}
				
				double[] Obj = cplex.getValues(F); 
				// The value of F and weight
				for (int j = 0; j < 3; ++j) {
					System.out.println("F_" + j + " = " + Math.round(Obj[j]) + "  W_" + j + " = " + weight[j]);
				}
				instance.WriteFile("#Number of Undercover: \n" 	+ Math.round(Obj[1]) + "\n \n");
				instance.WriteFile("#Number of Overcover: \n"	+ Math.round(Obj[0])  +"\n \n");
				instance.WriteFile("#Number of Shifts: \n" 	+ Math.round(Obj[2])  + "\n \n");
				
				System.out.println();

				// The value of objective function
				System.out.println("#Fitness of assumed optimal Solution:");
				instance.WriteFile("#Fitness of assumed optimal Solution: \n");
				System.out.println("# " + Math.round(cplex.getObjValue()));
				instance.WriteFile("# " + Math.round(cplex.getObjValue()) + "\n \n");
				System.out.println();
				System.out.println("#Optimal Solution:");
				System.out.println("#Shift Start Length Duties:");
				instance.WriteFile("#Shift Start Length Duties: \n");

				double[] IsShiftActive = cplex.getValues(b);
				
				// The value of
				int shift = 1;

				for (int s =0; s< tempShift.size(); s++){		
					double[] nrOfAssignedEmployees = cplex.getValues(w[s]);

					if (Math.round(IsShiftActive[s])==1){
						instance.WriteFile("# " + shift + ":  " + tempShift.get(s).getStart() * slotLength/60 + ":" + (tempShift.get(s).getStart() % (60/slotLength)) * slotLength +  "    " + tempShift.get(s).getLength() * slotLength  + "       ");
						System.out.print("# " + shift + ":  " + tempShift.get(s).getStart() * slotLength/60 + ":" + (tempShift.get(s).getStart() % (60/slotLength)) * slotLength +  "    " + tempShift.get(s).getLength() * slotLength  + "       ");
						for (int i = 0; i < daysPerCycle; ++i) {
							instance.WriteFile(Math.round(nrOfAssignedEmployees[i]) + "  ");
							System.out.print(Math.round(nrOfAssignedEmployees[i]) + "  ");
						}
						instance.WriteFile("\n");
						System.out.println();
						shift ++;
					}
				}

				instance.CloseWriteFile();
			}
			else
				System.out.println("cplex.solve() is " + cplex.solve());
			cplex.end();
		}
		catch(IloException e){
			e.printStackTrace();
		}
	}
}
