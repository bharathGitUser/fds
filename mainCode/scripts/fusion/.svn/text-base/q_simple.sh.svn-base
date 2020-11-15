#Script for running tests...

#first kill the previous java processes which are still alive
numStructs=$1
numFaults=$2  
  pkill -9 java
   echo "---------------------"
   echo "Faults = $numFaults"

   for ((  i = 0 ;  i < $numFaults;  i++  ))
   do
     java FusedQueue $i $numStructs $numFaults &
   done

   for ((  i = 0 ;  i < $numStructs;  i++  ))
   do
     java OriginalQueue $i &
   done

#   sleep 10

#   java FusionQueueOracle $numStructs $numFaults &
