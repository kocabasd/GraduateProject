import java.io.IOException;
import java.text.ParseException;

import gurobi.*;


public class SDGurobi extends SDConstant {	
	GRBEnv    env;
	GRBModel  model;
	GRBVar [] b, l, ex, sh, F;
	GRBVar [][] w;
	
	public void model(SDInstance instance) throws IOException, ParseException{
		
			// Initialization of n, m, w, x and a variables
			modelConstraints(instance);
			// Initialization of Gurobi solver
			initGurobi();
			// Initialization of Gurobi variables
			modelGurobiVariables();
			// Initialization of Gurobi constraints
			modelGurobiConstraints();
			// Solve Gurobi model
			solve();
	}		
	
	public void initGurobi(){
		try {
			env   = new GRBEnv();
			model = new GRBModel(env);
		} 
		catch (GRBException e) {
			e.printStackTrace();
		}
	}
	
	public void		modelGurobiVariables(){
		try{
			// Initialization of variable b
			b = new GRBVar[m];
			// Initialization of variable w
			w = new GRBVar[m][daysPerCycle];
			for (int s = 0; s < m; s++){
				b[s] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "b");
				for (int d = 0; d < daysPerCycle; d++){
					w[s][d] = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.INTEGER, "w");
				}
			}
			
			// Initialization of variable l
			l = new GRBVar[n];			
			
			// Initialization of variable ex
			ex = new GRBVar[n];			
			
			// Initialization of variable sh
			sh = new GRBVar[n];
			for (int t = 0; t < n; t++){
				l[t] = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.INTEGER, "load");
				ex[t] = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.INTEGER, "excesses");
				sh[t] = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.INTEGER, "shortages");
			}
			
			// Initialization of variable F
			F = new GRBVar[3];
			for ( int i = 0; i < 3; i++)
				F[i] = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.INTEGER, "F");

			model.update();
		
		}
		catch(GRBException e){
			e.printStackTrace();
		}
	}
	public void modelGurobiConstraints()	{
		try{
			GRBLinExpr obj = new GRBLinExpr();
			for ( int i = 0; i < 3; i++){
				obj.addTerm(weight[i], F[i]);
			}
			
			// Objective Function			
			model.setObjective(obj, GRB.MINIMIZE);
		     

			// Constraint 1 (6)
			for ( int s = 0; s < m; s++){
				GRBLinExpr expr1 = new GRBLinExpr();
				double M = 1.00 / (instance.getMaxEmployeeNeeded() * 7);
				for (int d = 0; d < daysPerCycle; d++){
					expr1.addTerm(M, w[s][d]); 
				}
				 model.addConstr(expr1, GRB.LESS_EQUAL, b[s], "c1");
			}
			
			// Constraint 2 (7)
			Integer tempDay;
			for ( int t = 0; t < n; t++){
				GRBLinExpr expr2 = new GRBLinExpr();
				for ( int s = 0; s < m; s++){
					if(nextDay[s] == true && t % (24 * 60 / slotLength) < 12 * 60 / slotLength){
						tempDay = ((daysPerCycle *t/n) + daysPerCycle -1) % daysPerCycle;
					}
					else 
						tempDay = (daysPerCycle *t/n);

					expr2.addTerm(a[t][s], w[s][tempDay]);
					
				}
				model.addConstr(expr2, GRB.EQUAL, l[t], "c2");
			}
			
			// Constraint 3 (8) Constraint 4 (9)
			for ( int t = 0; t < n; t++){
				GRBLinExpr expr3 = new GRBLinExpr();
				GRBLinExpr expr4 = new GRBLinExpr();
				expr3.addConstant(-slotLength * x[t]);
				expr3.addTerm(slotLength, l[t]);
				model.addConstr(expr3, GRB.LESS_EQUAL, ex[t], "c3");
				expr4.addConstant(slotLength*x[t]);
				expr4.addTerm(-slotLength, l[t]);
				model.addConstr(expr4, GRB.LESS_EQUAL, sh[t], "c4");
			}
			
			// Constraint 5 (9) Constraint 6 (10)
			GRBLinExpr expr5 = new GRBLinExpr();
			GRBLinExpr expr6 = new GRBLinExpr();
			for (int t = 0; t < n; t++){
				expr5.addTerm(1.0, ex[t]);
				expr6.addTerm(1.0, sh[t]);				
			}
			model.addConstr(expr5, GRB.EQUAL, F[0], "c5");
			model.addConstr(expr6, GRB.EQUAL, F[1], "c6");

			// Constraint 7 (11)
			GRBLinExpr expr7 = new GRBLinExpr();
			for (int s = 0; s < m; s++){
				expr7.addTerm(1.0, b[s]);				
			}
			model.addConstr(expr7, GRB.EQUAL, F[2], "c7");
		}
	
		catch(GRBException e){
			e.printStackTrace();
		}
	}
	public void solve() throws IOException, ParseException{
		try{	
		    model.optimize();	
		    int status = model.get(GRB.IntAttr.Status);
		    if (status == GRB.Status.OPTIMAL) {
		    	// Show excesses and shortages of TimeSlot t 
				double[] Excesses = model.get(GRB.DoubleAttr.X, ex); 
				double[] Shortages = model.get(GRB.DoubleAttr.X, sh); 
				double[] Load = model.get(GRB.DoubleAttr.X, l); 
				for (int j = 0; j < n; ++j) {
					if (Math.round(Excesses[j]) != 0 || Math.round(Shortages[j]) != 0)
					System.out.println("Excesses/Shortages of TimeSlot " + j + " = " + Excesses[j]/slotLength+ " / " + Shortages[j]/slotLength+ "         Load = " + Load[j] +"        nrOfNeededEmployees: " + x[j]); 
				}
				
				System.out.println();
				instance.WriteFile("#CPU Time: \n" + ((System.currentTimeMillis() - startTime)/1000)  + "\n \n");double[] Obj = model.get(GRB.DoubleAttr.X, F); 
				// The value of F and weight
				for (int j = 0; j < 3; ++j) {
					System.out.println("F_" + j + " = " + Obj[j] + "  W_" + j + " = " + weight[j]);
				}
				instance.WriteFile("#Number of Undercover: \n" 	+ Math.round(Obj[1]) + "\n \n");
				instance.WriteFile("#Number of Overcover: \n"	+ Math.round(Obj[0])  +"\n \n");
				instance.WriteFile("#Number of Shifts: \n" 	+ Math.round(Obj[2])  + "\n \n");

				System.out.println();
				// The value of objective function
				System.out.println("#Fitness of assumed optimal Solution:");				
				instance.WriteFile("#Fitness of assumed optimal Solution: \n");
				System.out.println("# " + model.get(GRB.DoubleAttr.ObjVal));
				instance.WriteFile("# " + Math.round(model.get(GRB.DoubleAttr.ObjVal)) + "\n \n");
				System.out.println();
				System.out.println("#Optimal Solution:");
				System.out.println("#Shift Start Length Duties:");
				instance.WriteFile("#Shift Start Length Duties: \n");

				double[] IsShiftActive = model.get(GRB.DoubleAttr.X, b);

				int shift = 1;
				for (int s =0; s< tempShift.size(); s++){		
					double[] nrOfAssignedEmployees = model.get(GRB.DoubleAttr.X, w[s]);
					if (IsShiftActive[s]==1){									
						instance.WriteFile("# " + shift + ":  " + tempShift.get(s).getStart() * slotLength/60 + ":" + (tempShift.get(s).getStart() % (60/slotLength)) * slotLength +  "    " + tempShift.get(s).getLength() * slotLength  + "       ");
						System.out.print("# " + shift + ":  " + tempShift.get(s).getStart() * slotLength/60 + ":" + (tempShift.get(s).getStart() % (60/slotLength)) * slotLength +  "    " + tempShift.get(s).getLength() * slotLength  + "       ");
						for (int i = 0; i < 7; ++i) {
							instance.WriteFile((int)nrOfAssignedEmployees[i] + "  ");
							System.out.print((int)nrOfAssignedEmployees[i] + "  ");
						}
						instance.WriteFile("\n");
						System.out.println();
						shift ++;
					}
				}
				instance.CloseWriteFile();
			}
		    
		    // Dispose of model and environment
		    model.dispose();
		    env.dispose();
		}
		catch(GRBException e){
			e.printStackTrace();
		}
	}
}
