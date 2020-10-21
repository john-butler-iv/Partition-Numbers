/**
 * Author: John Butler
 * 
 * This Program was inspired by Mathologer's video titled 'The hardest "What comes next?" (Euler's pentagonal 
 * formula)', youtube.com/watch?v=iJ8pnCO0nTY, in which he encourages viewers to find the partition number of 666. 
 * According to my program, that number is 11956824258286445517629485
 */

import java.util.*;
import java.math.BigInteger;

// the numbers grow exponentially, so I use BigInteger to 
public class PartitionNumber implements Iterator<BigInteger>{
	/**
	 * Keeps track of both the index needed to go back and whether to add or subtract.
	 * It would be possible to refactor to add or subtract by any scalar by changing sign to an int, but that's
	 * unnecessary for our use case
	 */
	private class FibInfo {
		static final boolean ADD = true;
		static final boolean SUB = false;
		int offset;
		boolean sign;
		public FibInfo(int offset, boolean sign){
			this.offset = offset;
			this.sign = sign;
		}
	}

	// we want random access to each of the partition numbers, so we put them in an array
	private ArrayList<BigInteger> prevParts;
	// we only ever iterate through this one, so we use a linked list for faster insertion 
	private LinkedList<FibInfo> fibAdds;
	private Iterator<FibInfo> addIt;

	private boolean firstNext;

	public PartitionNumber(){
		prevParts = new ArrayList<BigInteger>();
		fibAdds = new LinkedList<FibInfo>();
		addIt = findFibAdds();
	}


	/**
	 * Iterates through the different indices which you need to add/sub together to find the next partition number.
	 * For example for the real Fiboanacci sequence, it would be {1, 2}, since you only add the last two terms.
	 * I should probably refactor this into a separate file, but I don't want to deal with two files. Also it
	 * only adds ~50 lines to keep here.
	 */
	private Iterator<FibInfo> findFibAdds(){
		return new Iterator<FibInfo>(){
			int currSign = 0;		
			Iterator<Integer> gapIt = findFibGaps();
			int currOff = 0;

			public boolean hasNext(){
				// there is actaully a restriction from overflow, but to figure that out, I would keep track of the
				// next return value and I don't want to do that.
				// Also I'm using this value to index in an array, and if it overflows, I'll have run out of memory
				return true; 
			}

			public FibInfo next(){
				currOff += gapIt.next();
				
				// we don't need to reset currSign. Even if there's an overflow, currSign & 2 will still 
				// do it
				boolean sign = (currSign++ & 2) == 2 ? FibInfo.SUB : FibInfo.ADD;
				
				return new FibInfo(currOff, sign);
			}
		};
	}

	/**
	 * Iterates through the gaps between the different "Fibonacci indices". Used by findFibAdds()
	 */
	private Iterator<Integer> findFibGaps(){
		return new Iterator<Integer>(){
			// start with index 0
			boolean odd = false;
			int evenVal = 1;
			int oddVal = 1;
			
			public boolean hasNext(){
				return odd || evenVal > 0;
			}

			public Integer next(){
				int retVal;
				if(odd) {
					retVal = oddVal;
					oddVal++;
				} else {
					retVal = evenVal;
					evenVal += 2;
				}
				odd = !odd;
				
				return retVal;
			}
		};
	}
	
	public boolean hasNext(){
		// technically there is a restriction from the memory required for storing previous partition numbers
		return true;
	}
	
	
	/**
	 * Returns the next partition number. The first call returns the partition number for n=0.
	 * This runs with memory complexity O(n) assuming the increased memory required for large BigIntegers is 
	 * negligible, and I think time complexity is O(sqrt(n))
	 */
	public BigInteger next(){
		// our algorithm breaks down when either list is empty, so I handle that special case here.
		if(prevParts.isEmpty()){
			// if we haven't found any partitions, that clearly means that n=0
			// insert items to make lists not empty
			prevParts.add(BigInteger.ONE);
			fibAdds.addLast(addIt.next());

			return BigInteger.ONE;
		}

		// since we don't have a way of predicting the next terms, we need to stay ahead to ensure we have all
		// terms we need
		if(fibAdds.getLast().offset <= prevParts.size())
			fibAdds.addLast(addIt.next());
		
		BigInteger nextPart = BigInteger.ZERO;

		Iterator<FibInfo> it = fibAdds.iterator();
		FibInfo currFib = it.next();
		// we are storing more parts than we need, so we don't want to iterate through all of them
		while(currFib.offset <= prevParts.size()){

			// calculate the next term to add/subtract
			BigInteger val = prevParts.get(prevParts.size() - (currFib.offset));
			if(currFib.sign == currFib.SUB)
				val = val.multiply(BigInteger.valueOf(-1));
				

			// add next part to 
			nextPart = nextPart.add(val);
			
			currFib = it.next();
		}

		// store to calculate future terms
		prevParts.add(nextPart);
		return nextPart;
	}

	public static void main(String[] args){
		PartitionNumber it = new PartitionNumber();
		// the args[0] can cause an error, but I won't worry about that
		for(int i = 0; i <= Integer.valueOf(args[0]); i++)
			System.out.println(i + ": " + it.next());
		
	}
}
