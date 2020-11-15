#Script for running tests...

#first kill the previous java processes which are still alive
numStructs=$1
for ((  numFaults = 1 ;  numFaults <= $numStructs;  numFaults++  ))
do
   pkill -9 java
   echo "---------------------"
   echo "Faults = $numFaults"

   for ((  i = 0 ;  i <= ($numFaults \* $numStructs);  i++  ))
   do
     java ReplicatedQueue $i &
   done

   for ((  i = 0 ;  i < $numStructs;  i++  ))
   do
     java OriginalQueue $i &
   done
 
   sleep 60
   java ReplicationQueueOracle  $numStructs $numFaults 
done


