import java.io.IOException;
import java.text.ParseException;

import ilog.cplex.*;
import ilog.concert.*;

public class BSCplex extends BSConstant{
	IloCplex cplex;
	IloNumVar[] C, ex, sh;
	IloNumVar[][] workPeriod, startTime, l;
	IloNumVar[][][] bl, al;
	
	public void model(BSInstance instance) throws IOException, ParseException{
		// Initialization of variables
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
			//cplex.setParam(IloCplex.DoubleParam.EpGap, 0.1);
			//cplex.setParam(IloCplex.DoubleParam.EpAGap, 15);


		}
		catch(IloException e){
			e.printStackTrace();
		}
	}
	
	public void modelCplexVariables() {
		try {
			// Initialization of variable breaks
			bl = new IloNumVar[sdd][3][];
			for (int s = 0; s < sdd; s++){
				for (int i = 0; i < 3; i++){
					bl [s][i] = cplex.numVarArray(lunchLatestEnd - earliestStart - shiftSDD.get(s).getLunchBreakTime() - minWorkingPeriod * 3 -6, 0, 1, IloNumVarType.Bool);
				}
			}
			
			l = new IloNumVar[sdd][];
			for (int s = 0; s < sdd; s++){
				if(shiftSDD.get(s).getNrOfLunchBreak() > 0)
					l [s] = cplex.numVarArray(lunchLatestEnd - lunchEarliestStart - shiftSDD.get(s).getLunchBreakTime(), 0, 1, IloNumVarType.Bool);
				else
					System.out.println("No lunch break");
			}	
			
			al = new IloNumVar[sdd][maxNrOfLunchBreak / 2 - 3 - maxLunchBreakTime * 3][];
			for (int s = 0; s < sdd; s++){
				for (int i = 0; i < (shiftSDD.get(s).getBreakTime() - shiftSDD.get(s).getLunchBreakTime()) / 2 -3; i++){
					al [s][i] = cplex.numVarArray(shiftSDD.get(s).getLength() - lunchEarliestStart - shiftSDD.get(s).getLunchBreakTime() - latestEnd - minWorkingPeriod - 2 - (minWorkingPeriod + 2) * ((shiftSDD.get(s).getBreakTime() - shiftSDD.get(s).getLunchBreakTime()) / 2 - 4), 0, 1, IloNumVarType.Bool);
				}
			}
			
			workPeriod = new IloNumVar[sdd][];			
			for (int s = 0; s < sdd; s++){
				workPeriod [s] = cplex.numVarArray((shiftSDD.get(s).getBreakTime() - shiftSDD.get(s).getLunchBreakTime()) / 2 + shiftSDD.get(s).getNrOfLunchBreak() + 1, minWorkingPeriod, maxWorkingPeriod, IloNumVarType.Int);
			}
			
			startTime = new IloNumVar[sdd][];			
			for (int s = 0; s < sdd; s++){
				startTime [s] = cplex.numVarArray((shiftSDD.get(s).getBreakTime() - shiftSDD.get(s).getLunchBreakTime()) / 2 + shiftSDD.get(s).getNrOfLunchBreak(), 0, shiftSDD.get(s).getLength() - earliestStart - latestEnd, IloNumVarType.Int);
			}
			
			ex = cplex.numVarArray(n, 0, Integer.MAX_VALUE, IloNumVarType.Int);

			sh = cplex.numVarArray(n, 0, Integer.MAX_VALUE, IloNumVarType.Int);

			// Initialization of variable C
			C = cplex.numVarArray(2, 0, Integer.MAX_VALUE, IloNumVarType.Int);
		} 
		catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	public void modelCplexConstraints() {
		try {
			IloLinearNumExpr obj = cplex.linearNumExpr();
			for ( int i = 0; i < 2; i++){
				obj.addTerm(weight[i + 5], C[i]);
			}
			// Objective Function			
			cplex.addMinimize(obj);

			// Hard Constraints
			IloLinearNumExpr[][] expr1 = new IloLinearNumExpr[sdd][maxNrOfLunchBreak/2];
			IloLinearNumExpr[][] expr2 = new IloLinearNumExpr[sdd][maxNrOfLunchBreak/2];

			for ( int s = 0; s < sdd; s++){
				for ( int i = 0; i < 3; i++){
					cplex.addEq(1.0, cplex.sum(bl[s][i]));
					expr1 [s][i] = cplex.linearNumExpr();
					expr1 [s][i].setConstant(earliestStart + (minWorkingPeriod + 2) * i);	
					for ( int t = 0; t < lunchLatestEnd - earliestStart - shiftSDD.get(s).getLunchBreakTime() - minWorkingPeriod * 3 -6; t++){
						expr1 [s][i].addTerm(bl[s][i][t], t);
					}
					cplex.addEq(startTime[s][i], expr1 [s][i]);
				}
				
				cplex.addEq(1.0, cplex.sum(l[s]));
				expr1 [s][3] = cplex.linearNumExpr();
				expr1 [s][3].setConstant(lunchEarliestStart);	
				for ( int t = 0; t < lunchLatestEnd - lunchEarliestStart - shiftSDD.get(s).getLunchBreakTime(); t++){
					expr1 [s][3].addTerm(l[s][t], t); 
				}
				cplex.addEq(startTime[s][3], expr1 [s][3]);					
				
				for (int i = 0; i < (shiftSDD.get(s).getBreakTime() - shiftSDD.get(s).getLunchBreakTime()) / 2 -3; i++){
					cplex.addEq(1.0, cplex.sum(al[s][i]));
					expr1 [s][i+4] = cplex.linearNumExpr();
					expr1 [s][i+4].setConstant(lunchEarliestStart + shiftSDD.get(s).getLunchBreakTime() + minWorkingPeriod + (minWorkingPeriod + 2) * i);	
					for ( int t = 0; t < shiftSDD.get(s).getLength() - lunchEarliestStart - shiftSDD.get(s).getLunchBreakTime() -latestEnd - minWorkingPeriod - 2 - (minWorkingPeriod + 2) *((shiftSDD.get(s).getBreakTime() - shiftSDD.get(s).getLunchBreakTime()) / 2 - 4); t++){
						expr1 [s][i+4].addTerm(al[s][i][t], t);
					}
					cplex.addEq(startTime[s][i+4], expr1 [s][i+4]);
				}
				
				cplex.addLe(workPeriod[s][0], workLimit);
				cplex.addEq(workPeriod[s][0], startTime[s][0]);
				
				for ( int i = 0; i < ((shiftSDD.get(s).getBreakTime() - shiftSDD.get(s).getLunchBreakTime()) / 2) + shiftSDD.get(s).getNrOfLunchBreak() -1; i++){
					expr2 [s][i] = cplex.linearNumExpr();
					if( i == 3){
						expr2 [s][i].setConstant(-6);
					}
					else {
						expr2 [s][i].setConstant(-2);
					}
					expr2 [s][i].addTerm(startTime[s][i], -1); 
					expr2 [s][i].addTerm(startTime[s][i+1], 1);
					if(i != 2)
						cplex.addLe(workPeriod[s][i+1], workLimit);
					cplex.addEq(workPeriod[s][i+1], expr2 [s][i]);
				}

				expr2 [s][((shiftSDD.get(s).getBreakTime() - shiftSDD.get(s).getLunchBreakTime()) / 2) + shiftSDD.get(s).getNrOfLunchBreak() -1] = cplex.linearNumExpr();
				expr2 [s][((shiftSDD.get(s).getBreakTime() - shiftSDD.get(s).getLunchBreakTime()) / 2) + shiftSDD.get(s).getNrOfLunchBreak() -1].setConstant(shiftSDD.get(s).getLength() - 2);
				expr2 [s][((shiftSDD.get(s).getBreakTime() - shiftSDD.get(s).getLunchBreakTime()) / 2) + shiftSDD.get(s).getNrOfLunchBreak() -1].addTerm(startTime[s][((shiftSDD.get(s).getBreakTime() - shiftSDD.get(s).getLunchBreakTime()) / 2) + shiftSDD.get(s).getNrOfLunchBreak()-1], -1); 
				cplex.addEq(workPeriod[s][((shiftSDD.get(s).getBreakTime() - shiftSDD.get(s).getLunchBreakTime()) / 2) + shiftSDD.get(s).getNrOfLunchBreak()], expr2 [s][((shiftSDD.get(s).getBreakTime() - shiftSDD.get(s).getLunchBreakTime()) / 2) + shiftSDD.get(s).getNrOfLunchBreak()-1]);
			}


			// Soft Constraints
			IloLinearNumExpr[] expr3 = new IloLinearNumExpr[n];			
			for ( int i = 0; i < n; i++){
				expr3[i] = cplex.linearNumExpr();
				for ( int s = 0; s < sdd; s++){
					for ( int t = 0; t < lunchLatestEnd - earliestStart - shiftSDD.get(s).getLunchBreakTime() - minWorkingPeriod * 3 -6; t++){
						for ( int b = 0; b < 3; b++){
							if (i == (t + shiftSDD.get(s).getStart() + earliestStart + (minWorkingPeriod + 2) * b +(daySDD.get(s) * n / 7))%n){
								if(t < lunchLatestEnd - earliestStart - shiftSDD.get(s).getLunchBreakTime() - minWorkingPeriod * 3 - 8){
									expr3[i].addTerm(bl[s][b][t], 1);
								}
								if(t >= 1 && t < lunchLatestEnd - earliestStart - shiftSDD.get(s).getLunchBreakTime() - minWorkingPeriod * 3 - 7){
									expr3[i].addTerm(bl[s][b][t-1], 1);
								}
								if(t >= 2){
									expr3[i].addTerm(bl[s][b][t-2], 1);
								}
							}
						}
					}
					for ( int t = 0; t < lunchLatestEnd - lunchEarliestStart - shiftSDD.get(s).getLunchBreakTime(); t++){
						if (i == (t + shiftSDD.get(s).getStart() + lunchEarliestStart + (daySDD.get(s) * n / 7))%n){
							for(int j =0; j< shiftSDD.get(s).getLunchBreakTime() + 1; j++){
								if((t-j)>=0){
									expr3[i].addTerm(l[s][t-j], 1);
								}
							}
						}
					}
					for ( int t = 0; t < shiftSDD.get(s).getLength() - lunchEarliestStart - shiftSDD.get(s).getLunchBreakTime() -latestEnd - minWorkingPeriod - 2 - (minWorkingPeriod + 2) *((shiftSDD.get(s).getBreakTime() - shiftSDD.get(s).getLunchBreakTime()) / 2 - 4); t++){
						for (int b = 0; b < (shiftSDD.get(s).getBreakTime() - shiftSDD.get(s).getLunchBreakTime()) / 2 -3; b++){
							if (i == (t + shiftSDD.get(s).getStart() + lunchEarliestStart + shiftSDD.get(s).getLunchBreakTime() + minWorkingPeriod + (minWorkingPeriod + 2) * b + (daySDD.get(s) * n / 7))%n){
								if(t < shiftSDD.get(s).getLength() - lunchEarliestStart - shiftSDD.get(s).getLunchBreakTime() -latestEnd - minWorkingPeriod - 4 - (minWorkingPeriod + 2) *((shiftSDD.get(s).getBreakTime() - shiftSDD.get(s).getLunchBreakTime()) / 2 - 4)){
									expr3[i].addTerm(al[s][b][t], 1);
								}
								if(t >= 1 && t < shiftSDD.get(s).getLength() - lunchEarliestStart - shiftSDD.get(s).getLunchBreakTime() -latestEnd - minWorkingPeriod - 3 - (minWorkingPeriod + 2) *((shiftSDD.get(s).getBreakTime() - shiftSDD.get(s).getLunchBreakTime()) / 2 - 4)){
									expr3[i].addTerm(al[s][b][t-1], 1);
								}
								if(t >= 2){
									expr3[i].addTerm(al[s][b][t-2], 1);
								}
							}
						}
					}
				}
				expr3[i].addTerm(sh[i], -1);
				expr3[i].addTerm(ex[i], +1);
				cplex.addEq (expr3[i], shiftMinusRequirement[i]);

			}
			cplex.addEq (C[0], cplex.sum(sh));
			cplex.addEq (C[1], cplex.sum(ex));

			
		} 
		catch (IloException e) {
			e.printStackTrace();
		}
	}

	public void solve() throws IOException, ParseException{
		try{					
			if (cplex.solve()){		
				double[] Cx = cplex.getValues(C); 
				instance.WriteFile("#CPLEX Status: \n" + cplex.getStatus() + "\n \n");
				instance.WriteFile("#Branch & Bound Nodes: \n" + cplex.getNnodes() + "\n \n");
				instance.WriteFile("#CPU Time: \n" + ((System.currentTimeMillis() - start)/1000)  + "\n \n");
				for (int i =0; i < 2; i++)
					System.out.println(Cx[i]);
				int duty = 1;
				int nrOfShift = 0;
				for ( int s = 0; s < sdd; s++){
					if(s == 0){
						System.out.println(shiftSDD.get(s));
						instance.WriteFile(shiftSDD.get(s) + "\n");
					}
					else if(shiftSDD.get(s) != shiftSDD.get(s - 1)){
						System.out.println(shiftSDD.get(s));
						instance.WriteFile("\n");
						instance.WriteFile(shiftSDD.get(s) + "\n");
						nrOfShift = shiftSDD.get(s).getShiftNr();
					}
					if(s == 0){
						System.out.println("DAY" + (daySDD.get(s) + 1));
						instance.WriteFile("DAY " + (daySDD.get(s) + 1) + ":\n");
					}
					else if(daySDD.get(s) != daySDD.get(s - 1) || shiftSDD.get(s) != shiftSDD.get(s - 1)){
						System.out.println("DAY" + (daySDD.get(s) + 1));
						instance.WriteFile("\n");
						instance.WriteFile("DAY " + (daySDD.get(s) + 1) + ":\n");
						duty = 1;
					}
					System.out.print("DUTY" + duty + ": ");
					instance.WriteFile("DUTY " + duty + ": ");
					duty ++;	
					for ( int i = 0; i < 3; i++){
						double[] beforeLunch = cplex.getValues(bl[s][i]); 
						for ( int t = 0; t < lunchLatestEnd - earliestStart - shiftSDD.get(s).getLunchBreakTime() - minWorkingPeriod * 3 -6; t++){
							if(beforeLunch[t] >0){
								instance.WriteFile(String.format("%02d",(t + shiftSDD.get(s).getStart() + earliestStart + (minWorkingPeriod + 2) * i)%(n/7) / 12) + ":"  + String.format("%02d",((t + shiftSDD.get(s).getStart() + earliestStart + (minWorkingPeriod + 2) * i)%(n/7) %12) *5) + "    00:10   " ); 
								System.out.print(String.format("%02d",(t + shiftSDD.get(s).getStart() + earliestStart + (minWorkingPeriod + 2) * i)%(n/7) / 12) + ":"  + String.format("%02d",((t + shiftSDD.get(s).getStart() + earliestStart + (minWorkingPeriod + 2) * i)%(n/7) %12) *5) + " 15 [10 MONITOR + 5 EXTRA]  " ); 
								//System.out.print("  " + t + "    " + shiftSDD.get(s).getStart() + earliestStart + "  " ); 
							}
						}
					}
					double[] lunch = cplex.getValues(l[s]); 
					for ( int t = 0; t < lunchLatestEnd - lunchEarliestStart - shiftSDD.get(s).getLunchBreakTime(); t++){
						if(lunch[t] >0){
							instance.WriteFile(String.format("%02d",(t + shiftSDD.get(s).getStart() + lunchEarliestStart)%(n/7) / 12) + ":"  + String.format("%02d",((t + shiftSDD.get(s).getStart() + lunchEarliestStart)%(n/7) %12) *5) + "    00:30   " ); 
							System.out.print(String.format("%02d",(t + shiftSDD.get(s).getStart() + lunchEarliestStart)%(n/7) / 12) + ":"  + String.format("%02d",((t + shiftSDD.get(s).getStart() + lunchEarliestStart)%(n/7) %12) *5) + " 35 [30 LUNCH + 5 EXTRA]  " ); 
							//System.out.print("  " + t + "    " + shiftSDD.get(s).getStart() + lunchEarliestStart + "  " ); 
						}
					}
					for (int i = 0; i < (shiftSDD.get(s).getBreakTime() - shiftSDD.get(s).getLunchBreakTime()) / 2 -3; i++){
						double[] afterLunch = cplex.getValues(al[s][i]); 						
						for ( int t = 0; t < shiftSDD.get(s).getLength() - lunchEarliestStart - shiftSDD.get(s).getLunchBreakTime() -latestEnd - minWorkingPeriod -2 - (minWorkingPeriod + 2) *((shiftSDD.get(s).getBreakTime() - shiftSDD.get(s).getLunchBreakTime()) / 2 - 4); t++){
							if(afterLunch[t] >0){
								instance.WriteFile(String.format("%02d",(t + shiftSDD.get(s).getStart() + lunchEarliestStart + shiftSDD.get(s).getLunchBreakTime() + minWorkingPeriod + (minWorkingPeriod + 2) * i)%(n/7) / 12) + ":"  + String.format("%02d",((t + shiftSDD.get(s).getStart() + lunchEarliestStart + shiftSDD.get(s).getLunchBreakTime() + minWorkingPeriod+ (minWorkingPeriod + 2) * i)%(n/7) %12) *5) + "    00:10   " ); 
								System.out.print(String.format("%02d",(t + shiftSDD.get(s).getStart() + lunchEarliestStart + shiftSDD.get(s).getLunchBreakTime() + minWorkingPeriod + (minWorkingPeriod + 2) * i)%(n/7) / 12) + ":"  + String.format("%02d",((t + shiftSDD.get(s).getStart() + lunchEarliestStart + shiftSDD.get(s).getLunchBreakTime() + minWorkingPeriod + (minWorkingPeriod + 2) * i)%(n/7) %12) *5) + " 15 [10 MONITOR + 5 EXTRA]  " ); 
								//System.out.print("  " + t + "    " + shiftSDD.get(s).getStart() + lunchEarliestStart + "  " ); 
							}
						}	
					}
					System.out.println();
				}									
		
				instance.WriteFile("#Solution Features \n"); 
				instance.WriteFile("CONSTRAINT            Violations \n"); 
				instance.WriteFile("------------------------------------------ \n"); 
				instance.WriteFile("BREAK POSITIONS             0 \n"); 
				instance.WriteFile("LUNCH BREAKS                0 \n"); 
				instance.WriteFile("LENGTH OF WORKING PERIODS   0 \n"); 
				instance.WriteFile("MINIMUM BREAK TIMES         0 \n"); 
				instance.WriteFile("BREAK LENGTHS               0 \n"); 
				instance.WriteFile("SHORTAGE         			" + (int) Cx[0] +"\n"); 
				instance.WriteFile("EXCESS               		" + (int) Cx[1] +"\n");
				instance.WriteFile("NUMBER OF SHIFTS            " + (nrOfShift +1) +"\n"); 
				instance.WriteFile("TOTAL                       " + ((nrOfShift +1) * 60 +(int) (weight[5] * Cx[0]) + (int)(weight[6] * Cx[1]))+"\n"); 
				instance.CloseWriteFile();
			}
			else
				System.out.println("cplex.solve() is " + cplex.solve());
			//cplex.exportModel("model.lp");
			cplex.end();
		}
		catch(IloException e){
			e.printStackTrace();
		}
	}

}
