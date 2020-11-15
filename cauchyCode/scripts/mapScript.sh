#Script for running tests...
for(( retries =1; retries <= 5; retries++))
do

	echo "--MAP_FUSION_SIZE_REC"
	numFaults=3
	numOperations=500
	for ((  numPrimaries = 1 ;  numPrimaries <= 10;  numPrimaries++  ))
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

		java MapClient  $numPrimaries $numFaults $numOperations
	done
	
	echo "--MAP_FUSION_UPDATE"
	numFaults=1
	numOperations=50
	numPrimaries=1
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

	echo "Trial = $retries"
	echo "--MAP_REP_SIZE_REC"
	numFaults=3
	numOperations=500
	for ((  numPrimaries = 1 ;  numPrimaries <= 10;  numPrimaries++  ))
	do
		pkill -9 java

		for ((  i = 0 ;  i <= ($numFaults \* $numPrimaries);  i++  ))
		do
		java ReplicatedMap $i &
		sleep 10
		done

		for ((  i = 0 ;  i < $numPrimaries;  i++  ))
		do
		java OriginalMap $i & 
		sleep 10
		done

		java ReplicationMapOracle  $numPrimaries $numFaults $numOperations 
	done

	echo "--MAP_REP_UPDATE"
	numFaults=1
	numOperations=50
	numPrimaries=1
	for ((  opFactor = 1 ;  opFactor <= 10;  opFactor++  ))
	do

		pkill -9 java

		for ((  i = 0 ;  i <= ($numFaults \* $numPrimaries);  i++  ))
		do
		java ReplicatedMap $i &
		sleep 10
		done

		for ((  i = 0 ;  i < $numPrimaries;  i++  ))
		do
		java OriginalMap $i &
		sleep 10
		done

		java ReplicationMapOracle  $numPrimaries $numFaults $[numOperations*opFactor]
	done

done
