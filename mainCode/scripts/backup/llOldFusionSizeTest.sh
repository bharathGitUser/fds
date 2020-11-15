#Script for running tests...

#first kill the previous java processes which are still alive
echo"--LL_OLD_FUSION_SIZE_REC"
numFaults=3
numOperations=500
for ((  numPrimaries = 1 ;  numPrimaries <= 10;  numPrimaries++  ))
do
   pkill -9 java

   for ((  i = 0 ;  i < $numFaults;  i++  ))
   do
      java FusedLinkedList $i $numPrimaries $numFaults &
      sleep 10
   done
   
   for ((  i = 0 ;  i < $numPrimaries;  i++  ))
   do
     java OriginalLinkedList $i &
     sleep 10
   done
 
   java FusionLinkedListOracle $numPrimaries $numFaults $numOperations
done


