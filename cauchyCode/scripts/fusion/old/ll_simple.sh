#Script for running tests...

#first kill the previous java processes which are still alive
numStructs=$1
numFaults=$2  
  pkill -9 -P 1 java 
   echo "---------------------"
   echo "Faults = $numFaults"

   for ((  i = 0 ;  i < $numFaults;  i++  ))
   do
     java FusedLinkedList $i $numStructs $numFaults &
   done

   for ((  i = 0 ;  i < $numStructs;  i++  ))
   do
     java OriginalLinkedList $i &
   done

   sleep 10

   java FusionLinkedListOracle $numStructs $numFaults 1 &
