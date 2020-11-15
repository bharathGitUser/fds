#!/usr/bin/python

import os
import sys

import stats
import sys

import numpy as np
import matplotlib.pyplot as plt
from operator import add 
def foo(section, dict):
	dict[section] = {}
	dict[section]['xval'] = []
	dict[section]['yval'] = []

def boo(d, dict):
	for (key,val) in d.items():
		dict[key]['yval'].append(map(lambda x : x[1], val))

if __name__ == "__main__":
	assert len(sys.argv)>1

	file2Parse = str(sys.argv[1])

	handleFile2Parse = open(file2Parse,'r')

	data = {}
	trialNumber = -1
	opNumber = 10
	for line in handleFile2Parse:
		if line.startswith('Trial'):
			trialNumber = trialNumber+1
			data[trialNumber] = {}
		elif line.startswith('-'):
			section = line[:-1].strip('-')
			data[trialNumber][section] = []
		else:
			tmp = line.split(',')
			list = []
			list.append(float(tmp[1]))
			list.append(float(tmp[2]))
			list.append(float(tmp[3]))
			list.append(float(tmp[0]))
			list.append(float(tmp[4]))
			list.append(float(tmp[5]))
			list.append(float(tmp[6]))
			list.append(float(tmp[7]))
			list.append(float(tmp[8]))
			data[trialNumber][section].append(list)

			#data[trialNumber][section].append(map(float,line.split(',')[1:3]))
	handleFile2Parse.close()

	#Linked List Fusion Updates
	operations = map(lambda x : float(x[0]), data[0]['Q_FUSION_UPDATE'])
	# print operations
	nodes = map(lambda x : float(x[1]), data[0]['Q_FUSION_UPDATE'])
	# print nodes
	q_fusion_update_time = map(lambda x : float(x[2]), data[0]['Q_FUSION_UPDATE'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[2]), data[i]['Q_FUSION_UPDATE'])
		q_fusion_update_time = map(add,q_fusion_update_time,tmp) 		
	
	for j in range (0,opNumber):
		q_fusion_update_time[j] = q_fusion_update_time[j]/(trialNumber+1)
	#Linked List Rep Updates
	q_rep_update_time = map(lambda x : float(x[2]), data[0]['Q_REP_UPDATE'])
	# print q_rep_update_time
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[2]), data[i]['Q_REP_UPDATE'])
		# print tmp
		q_rep_update_time = map(add,q_rep_update_time,tmp) 		
	
	for j in range (0,opNumber):
		q_rep_update_time[j] = q_rep_update_time[j]/(trialNumber+1)
		
	print "****Updates*******"
	print q_fusion_update_time
	print q_rep_update_time
	plt.xlabel('Number of Updates')
	plt.ylabel('Time Taken per update in micros')
	plt.plot(operations,q_fusion_update_time,'k:',operations,q_rep_update_time,'k--')
	plt.legend(('Fusion','Replication'),2);
	plt.savefig('.'.join(["Q_UPDATE","ps"]))
	plt.close();
	
	# print "---------Recovery-------------"
	#Linked List Rep Recovery 
	q_rep_rec = map(lambda x : float(x[4]), data[0]['Q_REP_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[4]), data[i]['Q_REP_REST'])
		q_rep_rec= map(add,q_rep_rec,tmp) 		
	
	for j in range (0,opNumber):
		q_rep_rec[j] = q_rep_rec[j]/(trialNumber+1)
	q_rep_rrcrashtime = map(lambda x : float(x[8]), data[0]['Q_REP_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[8]), data[i]['Q_REP_REST'])
		q_rep_rrcrashtime= map(add,q_rep_rrcrashtime,tmp) 		
	for j in range (0,opNumber):
		q_rep_rrcrashtime[j] = q_rep_rrcrashtime[j]/(trialNumber+1)
		q_rep_rrcrashtime[j] = q_rep_rrcrashtime[j]/10
	
	time_for_origs = q_rep_rrcrashtime[0]*10 
	q_rep_rec= map(add,q_rep_rec,q_rep_rrcrashtime) 		

	for j in range (0,opNumber):
		q_rep_rec[j] = q_rep_rec[j]/1000000


	#Linked List Fusion Recovery 
	faults = map(lambda x : float(x[3]), data[0]['Q_FUSION_REST'])
	# print faults 
	q_fusion_rec = map(lambda x : float(x[4]), data[0]['Q_FUSION_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[4]), data[i]['Q_FUSION_REST'])
		q_fusion_rec= map(add,q_fusion_rec,tmp) 		
	
	for j in range (0,opNumber):
		q_fusion_rec[j] = q_fusion_rec[j]/(trialNumber+1)

	q_fusion_rrcrashtime = map(lambda x : float(x[8]), data[0]['Q_FUSION_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[8]), data[i]['Q_FUSION_REST'])
		q_fusion_rrcrashtime= map(add,q_fusion_rrcrashtime,tmp) 		
	
	for j in range (0,opNumber):
		q_fusion_rrcrashtime[j] = q_fusion_rrcrashtime[j]/(trialNumber+1)
		q_fusion_rrcrashtime[j] = q_fusion_rrcrashtime[j] + time_for_origs

	q_fusion_rec= map(add,q_fusion_rec,q_fusion_rrcrashtime) 		

	for j in range (0,opNumber):
		q_fusion_rec[j] = q_fusion_rec[j]/1000000


	print "****Recovery****"
	print q_fusion_rec
	print q_rep_rec
	plt.xlabel('Number of Faults')
	plt.ylabel('Time Taken for recovery in seconds')
	plt.plot(faults,q_fusion_rec,'k:',faults,q_rep_rec,'k--')
	plt.legend(('Fusion','Replication'),2);
	plt.savefig('.'.join(["Q_REC","ps"]))
	plt.close()

	# Byz
	q_fusion_byz = map(lambda x : float(x[5]), data[0]['Q_FUSION_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[5]), data[i]['Q_FUSION_REST'])
		q_fusion_byz= map(add,q_fusion_byz,tmp) 		
	
	for j in range (0,opNumber):
		q_fusion_byz[j] = q_fusion_byz[j]/(trialNumber+1)
	
	q_fusion_rrbyztime = map(lambda x : float(x[8]), data[0]['Q_FUSION_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[8]), data[i]['Q_FUSION_REST'])
		q_fusion_rrbyztime= map(add,q_fusion_rrbyztime,tmp) 		
	
	for j in range (0,opNumber):
		q_fusion_rrbyztime[j] = q_fusion_rrbyztime[j]/(trialNumber+1)
		q_fusion_rrbyztime[j] = q_fusion_rrbyztime[j]+ time_for_origs

	q_fusion_byz= map(add,q_fusion_byz,q_fusion_rrbyztime) 		

	for j in range (0,opNumber):
		q_fusion_byz[j] = q_fusion_byz[j]/1000000


	#Linked List Rep Byz
	q_rep_byz = map(lambda x : float(x[5]), data[0]['Q_REP_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[5]), data[i]['Q_REP_REST'])
		q_rep_byz= map(add,q_rep_byz,tmp) 		
	
	for j in range (0,opNumber):
		q_rep_byz[j] = q_rep_byz[j]/(trialNumber+1)
	
	q_rep_rrbyztime = map(lambda x : float(x[8]), data[0]['Q_REP_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[8]), data[i]['Q_REP_REST'])
		q_rep_rrbyztime= map(add,q_rep_rrbyztime,tmp) 		
	
	for j in range (0,opNumber):
		q_rep_rrbyztime[j] = q_rep_rrbyztime[j]/(trialNumber+1)
		q_rep_rrbyztime[j] = q_rep_rrbyztime[j]+time_for_origs
	
	q_rep_byz= map(add,q_rep_byz,q_rep_rrbyztime) 		

	for j in range (0,opNumber):
		q_rep_byz[j] = q_rep_byz[j]/1000000

	print "***Byz****"
	print q_fusion_byz
	print q_rep_byz	
	plt.xlabel('Number of Faults')
	plt.ylabel('Time Taken for Byzantine Sanity Check in seconds')
	plt.plot(faults,q_fusion_byz,'k:',faults,q_rep_byz,'k--')
	plt.legend(('Fusion','Replication'),2);
	plt.savefig('.'.join(["Q_BYZ","ps"]))
	plt.close()

	# print "size"
	q_fusion_size = map(lambda x : float(x[6]), data[0]['Q_FUSION_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[6]), data[i]['Q_FUSION_REST'])
		q_fusion_size= map(add,q_fusion_size,tmp) 		
	
	for j in range (0,opNumber):
		q_fusion_size[j] = q_fusion_size[j]/(trialNumber+1)
	
	
	#Linked List Rep Byz
	q_rep_size = map(lambda x : float(x[6]), data[0]['Q_REP_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[6]), data[i]['Q_REP_REST'])
		q_rep_size= map(add,q_rep_size,tmp) 		
	
	for j in range (0,opNumber):
		q_rep_size[j] = q_rep_size[j]/(trialNumber+1)
		
	print "****size****"
	print q_fusion_size
	print q_rep_size
	plt.xlabel('Number of Faults')
	plt.ylabel('Total number of nodes in the backup structures')
	plt.plot(faults,q_fusion_size,'k:',faults,q_rep_size,'k--')
	plt.legend(('Fusion','Replication'),2);
	plt.savefig('.'.join(["Q_SIZE","ps"]))
	plt.close()

	# Random Byz
	q_fusion_randbyz = map(lambda x : float(x[7]), data[0]['Q_FUSION_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[7]), data[i]['Q_FUSION_REST'])
		q_fusion_randbyz= map(add,q_fusion_randbyz,tmp) 		
	
	for j in range (0,opNumber):
		q_fusion_randbyz[j] = q_fusion_randbyz[j]/(trialNumber+1)
	
	q_fusion_rrbyztime = map(lambda x : float(x[8]), data[0]['Q_FUSION_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[8]), data[i]['Q_FUSION_REST'])
		q_fusion_rrbyztime= map(add,q_fusion_rrbyztime,tmp) 		
	
	for j in range (0,opNumber):
		q_fusion_rrbyztime[j] = q_fusion_rrbyztime[j]/(trialNumber+1)
		q_fusion_rrbyztime[j] = q_fusion_rrbyztime[j] + time_for_origs

	q_fusion_randbyz= map(add,q_fusion_randbyz,q_fusion_rrbyztime) 		

	for j in range (0,opNumber):
		q_fusion_randbyz[j] = q_fusion_randbyz[j]/1000000


	#Linked List Rep Byz
	q_rep_randbyz = map(lambda x : float(x[7]), data[0]['Q_REP_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[7]), data[i]['Q_REP_REST'])
		q_rep_randbyz= map(add,q_rep_randbyz,tmp) 		
	
	for j in range (0,opNumber):
		q_rep_randbyz[j] = q_rep_randbyz[j]/(trialNumber+1)
	
	q_rep_rrbyztime = map(lambda x : float(x[8]), data[0]['Q_REP_REST'])
	for i in range (1,trialNumber+1):
		tmp =  map(lambda x : float(x[8]), data[i]['Q_REP_REST'])
		q_rep_rrbyztime= map(add,q_rep_rrbyztime,tmp) 		
	
	for j in range (0,opNumber):
		q_rep_rrbyztime[j] = q_rep_rrbyztime[j]/(trialNumber+1)
		q_rep_rrbyztime[j] = q_rep_rrbyztime[j]+ time_for_origs
	
	q_rep_randbyz= map(add,q_rep_randbyz,q_rep_rrbyztime) 		

	for j in range (0,opNumber):
		q_rep_randbyz[j] = q_rep_randbyz[j]/1000000


	plt.xlabel('Number of Faults')
	plt.ylabel('Time Taken for Byzantine Sanity Check in seconds')
	plt.plot(faults,q_fusion_randbyz,'k:',faults,q_rep_randbyz,'k--')
	plt.legend(('Fusion','Replication'),2);
	plt.savefig('.'.join(["Q_RAND_BYZ","ps"]))
	plt.close()

