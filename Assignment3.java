import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Assignment3
{
	final int SERVANTS = 4;
	final int PRESENTS = 500000;
	AtomicInteger addedPresents = new AtomicInteger(0);
	AtomicInteger removedPresents = new AtomicInteger(0);
	AtomicInteger bagIndex = new AtomicInteger(0);
  List<Integer> bag = new ArrayList<Integer>(PRESENTS);


	// Problem 2 constants
	public static final int SENSORS = 8;
	static int[][] sixtyMinutes = new int[SENSORS][60];
	static int[][] tenMinutes = new int[SENSORS][10];
	int tenMinDiff = -100;

	// Lock Free List based off page 217 & 218.
  public class LockFreeList
  {
  	private ItemNode head;
  	private ItemNode tail;

		// sets head and tail for the linked list
  	public LockFreeList()
  	{
  		tail = new ItemNode(Integer.MAX_VALUE);
			tail.next = new AtomicMarkableReference<ItemNode>(null, false);
  		head = new ItemNode(Integer.MIN_VALUE);
			head.next = new AtomicMarkableReference<ItemNode>(tail, false);
  	}

		// adds an item node passed to the linked list.
  	public boolean add(ItemNode node)
  	{
  		int key = node.key;

  		while (true)
  		{
  			Window window = Window.find(head, key);
  			ItemNode pred = window.pred, curr = window.curr;

  			if (curr.key == key)
  			{
  				return false;
  			}
  			else
  			{
  				ItemNode newNode = new ItemNode(key);
					newNode.next = new AtomicMarkableReference<ItemNode>(curr, false);

  				if (pred.next.compareAndSet(curr, newNode, false, false))
  				{
  					return true;
  				}
  			}
  		}
  	}

		// modifies removeNode on page 218, it is because we can remove any node for this algorithm.
		// So, we will just remove the first node we see so it's O(1) time.
  	public boolean removeFirstNode()
  	{
			// gets the first node so it's fastest to remove.
			ItemNode firstNode = head.next.getReference();
  		int key = firstNode.key;
  		boolean snip;

  		while (true)
  		{
  			Window window = Window.find(head, key);
  			ItemNode pred = window.pred, curr = window.curr;

  			if (curr.key != key)
  			{
  				return false;
  			}
  			else
  			{
  				ItemNode succ = curr.next.getReference();
  				snip = curr.next.compareAndSet(succ, succ, false, true);

  				if (!snip)
  					continue;

  				pred.next.compareAndSet(curr, succ, false, false);
  				return true;
  			}
  		}
  	}

		// checks if our linked list contains a node.
  	public boolean contains(ItemNode node)
  	{
			// book wasn't working with just setting marked to false so changed it to
			// be a boolean array of length 1, which is auto set to false.
  		boolean[] marked = new boolean[1];
  		int key = node.key;
  		ItemNode curr = head;

  		while (curr.key < key)
  		{
  			curr = curr.next.getReference();
  			ItemNode succ = curr.next.get(marked);
  		}

  		return (curr.key == key && !marked[0]);
  	}
  }

  class ServantThread implements Runnable {
      private int threadId;
			// creates a new list, but head is shared, so all the nodes will be the same.
    	private LockFreeList list = new LockFreeList();

    	public ServantThread(int id)
    	{
    		this.threadId = id;
    	}
      @Override
      public void run()
      {
				Random random = new Random();
				// loop until we've removed every present which means all the
				// thank you gifts are sent.
        while (removedPresents.get() < PRESENTS)
    		{
					// gets the bagNumber from our index
					int bagNumber = bagIndex.getAndIncrement();
					// if we get here we will have searched passed the amount of presents
					if (bagNumber >= PRESENTS) {
						return;
					}
					// present number we are currently on to get from our bag of presents.
    			ItemNode currentPresent = new ItemNode(bag.get(bagNumber));
					// random key to get a random present which we will attempt a contains on
					int randomKey = random.nextInt(PRESENTS);
					ItemNode randomPresent = new ItemNode(bag.get(randomKey));
					list.contains(randomPresent);

    			boolean added = list.add(currentPresent);
    			boolean removed = list.removeFirstNode();
					// if we add the node successfully, increment the counter for addedPresents.
					if (added == true) {
						addedPresents.getAndIncrement();
					}
					if (removed == true) {
						removedPresents.getAndIncrement();
					}
        }
      }
    }

	// Problem one handles the minotaur's servants adding to a linked list then removing it.
	public void startProblemOne()
	{
		Thread[] servants = new Thread[SERVANTS];
		for (int i = 0; i < PRESENTS; i++)
		{
			bag.add(i);
		}
    // randomize bag of presents order.
		Collections.shuffle(bag);
		// start the servants thread
		for (int i = 0; i < SERVANTS; i++)
		{
			servants[i] = new Thread(new ServantThread(i));
			servants[i].start();
		}
		try {
			// attempt to join when they all finish
			for (int i = 0; i < SERVANTS; i++)
			{
				servants[i].join();
			}
		} catch (InterruptedException e) {
        System.out.print(e.getMessage());
	}
}

	// Problem 2 down here
	// gets the sixty minute report
	public void run60Minute() {
		int[] flatTemps = new int[480];
		int count = 0;
		// flatten the sixty minute report to sort everything for the highest, lowest values.
		for (int i = 0; i < sixtyMinutes.length; i++) {
			for (int j = 0; j < sixtyMinutes[i].length; j++)
			{
				flatTemps[count++] = sixtyMinutes[i][j];
			}
		}
		Arrays.sort(flatTemps);

		System.out.println();
		System.out.println("REPORT");

		System.out.println("Top 5 Highest Temperatures: " + flatTemps[flatTemps.length - 1] + ", " +
		flatTemps[flatTemps.length - 2] + ", " + flatTemps[flatTemps.length - 3] + ", " + flatTemps[flatTemps.length - 4]
		+ ", " + flatTemps[flatTemps.length - 5]);

		System.out.println("Top 5 Lowest Temperatures: " + flatTemps[0] + ", " +
		flatTemps[1] + ", " + flatTemps[2] + ", " + flatTemps[3]
		+ ", " + flatTemps[4]);

		System.out.println("Max Diff: " + tenMinDiff);
		tenMinDiff = -100;
	}

	// every ten minutes we add the max difference to our shared array
	public void run10Minute() {
		int minTemp = 70;
		int maxTemp = -100;

		// gets the max and min in the 10 minute array
		for (int i = 0; i < tenMinutes.length; i++)
		{
			for (int j = 0; j < tenMinutes[i].length; j++)
			{
				int tenMinuteTemp = tenMinutes[i][j];
				if (tenMinuteTemp < minTemp)
				{
					minTemp = tenMinuteTemp;
				}
				if (tenMinuteTemp > maxTemp)
				{
					maxTemp = tenMinuteTemp;
				}
			}
		}
		// set our current max difference to the tenMinDiff if it's bigger.
		int maxTempDiff = maxTemp - minTemp;
		if (maxTempDiff > tenMinDiff) {
			tenMinDiff = maxTempDiff;
		}
	}

	// every minute thread runs and gets new data.
	public void runMinute(Thread[] threads) {
		try {
			for (int i = 0; i < SENSORS; ++i)
			{
				threads[i].run();
			}
			for (int i = 0; i < SENSORS; ++i)
			{
				threads[i].join();
			}
		} catch (InterruptedException e) {
		System.out.print(e.getMessage());
	}
	}

	class SensorThread implements Runnable
	{
		int threadId;
		int count = 0;

		public SensorThread(int id)
		{
			this.threadId = id;
		}

		@Override
		public void run()
		{
			// Get random temperature from -100 to 70.
			int randomTemp = ThreadLocalRandom.current().nextInt(-100, 71);

			sixtyMinutes[this.threadId][count % 60] = randomTemp;
			tenMinutes[this.threadId][count % 10] = randomTemp;
			count++;
		}
	}

	public void startProblemTwo()
	{

		// Initialize user input variables.
		int numMinutes = 60;
		int currMinute = 1;
		Thread threads[] = new Thread[SENSORS];
		for (int i = 0; i < SENSORS; i++)
		{
			Thread thread = new Thread(new SensorThread(i));
			threads[i] = thread;
		}

		// Ask for input from the user for the number of minutes to read temperature
		// data from the sensors.
		while (currMinute <= numMinutes)
		{
			// runs each thread to get temps
			runMinute(threads);
			// every 10 minutes gets the differences report
			if (currMinute % 10 == 0) {
				run10Minute();
			}
			// every 60 minutes gets the differences and max, min report
			if (currMinute % 60 == 0) {
				run60Minute();
			}
			currMinute++;
		}
	}


	public static void main(String[] args) throws InterruptedException
	{
		Assignment3 assignment3 = new Assignment3();
		assignment3.startProblemOne();
		assignment3.startProblemTwo();
	}
}
