#Script for running tests...
numFaults=1
numOperations=500
numPrimaries=3
for(( retries =1; retries <= 3; retries++))
do
	echo "Trial = $retries"
	echo "--LL_NEW_FUSION_UPDATE"
	for (( opFactor =1 ; opFactor<=10;  ++opFactor  ))

	do
		pkill -9 java
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
		java ListClient  $numPrimaries $numFaults $[numOperations*opFactor]
	done

	echo "--MAP_FUSION_UPDATE"
	for (( opFactor =1 ; opFactor<=10;  ++opFactor  ))

	do
		pkill -9 java
		for ((  i = 0 ;  i < $numFaults;  i++  ))
		do
		java FusedMap $i $numPrimaries $numFaults &
		sleep 10
		done

		for ((  i = 0 ;  i < $numPrimaries;  i++  ))
		do
		java PrimaryMap $i $numFaults &
		sleep 10
		done
		java MapClient  $numPrimaries $numFaults $[numOperations*opFactor]
	done
done
