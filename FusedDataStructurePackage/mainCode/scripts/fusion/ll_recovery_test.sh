#Script for running tests...

#first kill the previous java processes which are still alive
numFaults=3
numOperations=500
for ((  numPrimaries = 1 ;  numPrimaries <= 10;  numPrimaries++  ))
do
   pkill -9 java
   echo "---------------------"
   echo "Number of Primaries = $numPrimaries"

   for ((  i = 0 ;  i < $numFaults;  i++  ))
   do
      java FusedList $i $numPrimaries $numFaults &
      sleep 10
   done
   
   for ((  i = 0 ;  i < $numPrimaries;  i++  ))
   do
     java PrimaryList $i $numFaults &
     sleep 10
   done
 
   java ListClient  $numPrimaries $numFaults $numOperations
done


