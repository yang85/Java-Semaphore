import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.*;

public class Project2 {
	// all the semaohores
	public static Semaphore TellerSem = new Semaphore(2); // # of tellers
	public static Semaphore LoanSem = new Semaphore(1); // # of loan officers
	public static Semaphore LoanInLine = new Semaphore(0); // indication of presents in loan queue line
	public static Semaphore[] LeavesLoan = new Semaphore[] { new Semaphore(0), new Semaphore(0), new Semaphore(0),
			new Semaphore(0), new Semaphore(0), new Semaphore(0) }; // indication of leaving loan window
	public static Semaphore TellerInLine = new Semaphore(0); // indication of presents in teller queue line
	public static Semaphore[] LeavesTeller = new Semaphore[] { new Semaphore(0), new Semaphore(0), new Semaphore(0),
			new Semaphore(0), new Semaphore(0), new Semaphore(0) }; // indication of leaving loan window
	public static Semaphore[] trans = new Semaphore[] { new Semaphore(0), new Semaphore(0), new Semaphore(0),
			new Semaphore(0), new Semaphore(0), new Semaphore(0) }; // indication of transaction completion
	static LinkedList<String> LoanLine = new LinkedList<String>(); // queue for loan
	static LinkedList<String> TellerLine = new LinkedList<String>();// queue for teller
	static LinkedList<String> TellerDone = new LinkedList<String>();// Log from teller
	static int MaxVisits = 0; // # of visits, breaks at 15

	// all the thread initialtion
	static customer c1 = new customer(1);
	static customer c2 = new customer(2);
	static customer c3 = new customer(3);
	static customer c4 = new customer(4);
	static customer c5 = new customer(5);
	static teller t1 = new teller(1);
	static teller t2 = new teller(2);
	static loanoff lo1 = new loanoff(1);

	public static void main(String args[]) {
		// starting the threads
		try {
			c1.start();
			c2.start();
			c3.start();
			c4.start();
			c5.start();
			t1.start();
			t2.start();
			lo1.start();
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	// print the summary and end the program
	public static void EndProgram() {

		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		}
		// print summary
		System.out.println("\n\n\t\tBank Simulation Summary");
		System.out.println("\t\tEndBalance\tLoan Amount");
		System.out.printf("Customer " + c1.num + "\t" + c1.account + "\t\t" + c1.loan + "\n");
		System.out.printf("Customer " + c2.num + "\t" + c2.account + "\t\t" + c2.loan + "\n");
		System.out.printf("Customer " + c3.num + "\t" + c3.account + "\t\t" + c3.loan + "\n");
		System.out.printf("Customer " + c4.num + "\t" + c4.account + "\t\t" + c4.loan + "\n");
		System.out.printf("Customer " + c5.num + "\t" + c5.account + "\t\t" + c5.loan + "\n\n");
		int acc = c1.account + c2.account + c3.account + c4.account + c5.account;
		int loa = c1.loan + c2.loan + c3.loan + c4.loan + c5.loan;
		System.out.printf("Total\t\t" + acc + "\t\t" + loa + "\n");
		System.exit(0);
	}
}

// thread to create teller
class teller extends Thread {
	int num = 0;

	// labeling the teller via index
	teller(int num) {
		this.num = num;
	}

	public void run() {
		System.out.printf("Teller " + num + " is created\n");
		while (true) {
			try {
				// wait till customer appears
				Project2.TellerInLine.acquire();

				// get the 1st customer and process the request
				String temp;
				temp = Project2.TellerLine.getFirst();
				Project2.TellerLine.removeFirst();
				String CNum = temp.substring(0, temp.indexOf('<'));
				int ICNum = Integer.parseInt(CNum);
				String Cam = temp.substring((temp.indexOf('<')) + 1, temp.indexOf('>'));
				int amount = Integer.parseInt(Cam);
				String todo = temp.substring(temp.indexOf('>') + 1);
				if (todo.contains("2")) {
					todo = "Deposit";
				}
				if (todo.contains("3")) {
					todo = "Withdrawal";
				}
				System.out.printf("Teller " + num + " is now serving Customor " + CNum + " with amount :" + amount
						+ " Requested to " + todo + "\n");
				System.out.printf("Request from Customor " + CNum + " approved by Teller " + num + "\n");
				String ntemp = Integer.toString(num);
				Project2.TellerDone.addLast(ntemp);
				// signal customer that request is done
				Project2.trans[ICNum].release();
				// window is open for business again
				Project2.TellerSem.release();
				// waits customer leaves.
				Project2.LeavesTeller[ICNum].acquire();
				System.out.printf("Teller " + num + " is now free\n");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

}

// thread for loan officer
class loanoff extends Thread {
	int num = 0;

	// index the loan officer '0"
	loanoff(int num) {
		this.num = num;
	}

	public void run() {
		System.out.printf("Loan Officer " + num + " is created\n");
		while (true) {
			try {
				// wait customer in Loan line
				Project2.LoanInLine.acquire();

				// process the request
				String temp;
				temp = Project2.LoanLine.getFirst();
				Project2.LoanLine.removeFirst();
				String CNum = temp.substring(0, temp.indexOf('<'));
				String Cam = temp.substring((temp.indexOf('<')) + 1, temp.indexOf('>'));
				int ICNum = Integer.parseInt(CNum);
				int amount = Integer.parseInt(Cam);
				String todo = temp.substring(temp.indexOf('>'));
				System.out.printf(
						"Loan Officer " + num + " is now serving Customor " + CNum + " with amount " + amount + "\n");
				// signal customer that request is done
				Project2.trans[ICNum].release();
				System.out.printf("Request from Customor " + CNum + " approved by Loan Offier\n");
				// window is open for business again
				Project2.LoanSem.release();
				// waits customer leaves.
				Project2.LeavesLoan[ICNum].acquire();
				System.out.printf("Loan Officer " + num + " is now free\n");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
}

class customer extends Thread {
	int num = 0;
	int account = 1000;
	int loan = 0;
	Random r = new Random();

	// index the customer
	customer(int num) {
		this.num = num;
	}

	public void run() {
		System.out.printf("Customer " + num + " is created\n");
		for (int i = 0; i < 3; i++) {
			try {
				// generates a request and an amount
				String command;
				int todo = r.nextInt(3);
				todo++;
				int amount = r.nextInt(4);
				amount++;
				amount = amount * 100;
				command = num + "<" + amount + ">" + todo;
				// loan
				if (todo == 1) {
					System.out.printf("Customer " + num + " wants a loan of " + amount + "\n");
					// queue in line
					Project2.LoanLine.addLast(command);
					// signnal imhere
					Project2.LoanInLine.release();
					// wait for open window
					Project2.LoanSem.acquire();
					// wait till transaction is finished
					Project2.trans[num].acquire();
					System.out.printf("Customer " + num + " has acquired loan\n");
					loan = loan + amount;
					System.out.printf("Customer " + num + " has left\n");
					// signal loan that customer has left
					Project2.LeavesLoan[num].release();
					Project2.MaxVisits++;
				}
				// deposit
				if (todo == 2) {
					System.out.printf("Customer " + num + " want to deposit " + amount + "\n");
					// queue in line
					Project2.TellerLine.addLast(command);
					// signnal imhere
					Project2.TellerInLine.release();
					// wait for open window
					Project2.TellerSem.acquire();
					// wait till transaction is finished
					Project2.trans[num].acquire();
					int tnum;
					tnum = Integer.parseInt(Project2.TellerDone.getFirst());
					Project2.TellerDone.removeFirst();
					System.out.printf("Customer " + num + " gets receipt from teller " + tnum + "\n");
					account = account + amount;
					System.out.printf("Customer " + num + " has left\n");
					// signal loan that customer has left
					Project2.LeavesTeller[num].release();
					Project2.MaxVisits++;
				}
				// withdrawal
				if (todo == 3) {
					System.out.printf("Customer " + num + " want to Withdrawal " + amount + "\n");
					// queue in line
					Project2.TellerLine.addLast(command);
					// signnal imhere
					Project2.TellerInLine.release();
					// wait for open window
					Project2.TellerSem.acquire();
					// wait till transaction is finished
					Project2.trans[num].acquire();
					String temp = Project2.TellerDone.getFirst();
					int tnum = Integer.parseInt(temp);
					Project2.TellerDone.removeFirst();
					System.out.printf("Customer " + num + " gets cash and receipt from Teller " + tnum + "\n");
					account = account - amount;
					System.out.printf("Customer " + num + " has left\n");
					// signal loan that customer has left
					Project2.LeavesTeller[num].release();
					Project2.MaxVisits++;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// End all running program via function
			if (Project2.MaxVisits == 15) {
				Project2.EndProgram();
			}
		}
	}
}