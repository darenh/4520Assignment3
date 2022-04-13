# 4520Assignment3

## How to compile
javac Assignment3.java

java Assignment3


# Problem 1: The Birthday Presents Party

## Approach
We use the lock-free list for our approach. This supports a concurrent lock free linked list that can add, remove and contains operations.
We then spawn 4 threads that represent our servants that will perform an add, remove, and contains on every loop. We will loop until we are done with our 
bag. That means we have looked through all the items and have added them all to our linked list chain.
There is an optimization in the code to remove the first element in our linked list, so our removal is always O(1)

## Proof of Correctness
The linked list approach comes from the book and is correct in that it will mark nodes and retry an operation if a node was in the middle of changing.
Thus, with this retry procedure it will always be up to date when we get it.

# Experimental Evaluation
It looks like we get all the nodes out of the bag and add it to the linked list, while also removing them.
There could be a scenario where not all the nodes are removed, and that's on the rare chance a remove operation fails. Which would make the amount of 
thank you cards sent always less than the amount of nodes added.
From testing the 500000, the algorithm is very fast and always gets the correct results.

# Problem 2: Atmospheric Temperature Reading Module

## Approach
For our approach we use a while loop for each minute passed, and at each minute we can do up to 3 operations. 
Every minute we will loop through all the sensors and get a temperature data for each one. We will store the sensor data in a shared 2d array.
Every 10 minutes we will get the max difference in the sensors and store what the max is.
Every 60 minutes we will fill out a report with the top 5 highest and lowest temps. And also the biggest 10 minute difference.

## Proof of Correctness
The sensors would have to be correct in that the temperatures would always be in the valid range, and the threads are a simple run and join. Meaning we will start the threads and wait for them to finish each time. Our data is stored in a shared 2d array so we will always have everything available and based off the current minutes are mod operation will make sure we are still in the array.

# Experimental Evaluation
From the results there's a good chance there will be multiple of the highest and lowest temperatures. Thus, the max difference is usually around 170. 
This makes sense because in a 60 minute interval with 8 threads, we will get 480 results. With 170 data points each temperature should be measured 3 times. So, our data matches the expected results. Also, the runtime is very fast.

