#Script for running tests...

numStructs=$1

echo "-------for n=$numStructs; operations=500-----------"
pkill -9 java
for ((  numFaults = 1 ;  numFaults <= $numStructs;  numFaults++  ))
do
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
 
   sleep 10
   java FusionQueueOracle  $numStructs $numFaults 
done


