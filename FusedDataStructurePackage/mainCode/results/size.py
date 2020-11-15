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
			print tmp
			list = map(float, tmp)
			data[trialNumber][section].append(list)

			#data[trialNumber][section].append(map(float,line.split(',')[1:3]))
	handleFile2Parse.close()

	print trialNumber

	#Linked List Size 
	numPrimaries = map(lambda x : float(x[0]), data[0]['LL_NEW_FUSION_SIZE_REC'])
	llOldFusionNodes = map(lambda x : float(x[2]), data[0]['LL_OLD_FUSION_SIZE_REC'])
	llNewFusionNodes = map(lambda x : float(x[2]), data[0]['LL_NEW_FUSION_SIZE_REC'])
	
	plt.xlabel('Number of Primaries')
	plt.ylabel('Number of Backup Nodes')
	plt.plot(numPrimaries,llOldFusionNodes,'k:',numPrimaries,llNewFusionNodes,'k-')
	plt.legend(('Old Fusion','New Fusion'),loc='upper right');
	plt.savefig('.'.join(["LL_SIZE","ps"]))
	plt.close();

	#Linked List Recovery 
	numPrimaries = map(lambda x : float(x[0]), data[0]['LL_NEW_FUSION_SIZE_REC'])
	llOldFusionRecTime = map(lambda x : float(x[4]), data[0]['LL_OLD_FUSION_SIZE_REC'])
	llNewFusionRecTime = map(lambda x : float(x[4]), data[0]['LL_NEW_FUSION_SIZE_REC'])
	
	plt.xlabel('Number of Primaries')
	plt.ylabel('Recovery Time in Microseconds')
	plt.plot(numPrimaries,	llOldFusionRecTime,'k:',numPrimaries,llNewFusionRecTime,'k-')
	plt.legend(('Old Fusion','New Fusion'),loc='upper right');
	plt.savefig('.'.join(["LL_REC","ps"]))
	plt.close();

	#Linked List Update Time 
	numOperations = map(lambda x : float(x[1]), data[0]['LL_NEW_FUSION_UPDATE'])
	llOldUpdateTime = map(lambda x : float(x[3]), data[0]['LL_OLD_FUSION_UPDATE'])
	llNewUpdateTime = map(lambda x : float(x[3]), data[0]['LL_NEW_FUSION_UPDATE'])
	
	plt.xlabel('Number of Operations')
	plt.ylabel('Update Time in Microseconds')
	plt.plot(numPrimaries,llOldUpdateTime,'k:',numPrimaries,llNewUpdateTime,'k-')
	plt.legend(('Old Fusion','New Fusion'),loc='upper right');
	plt.savefig('.'.join(["LL_UPDATE","ps"]))
	plt.close();


