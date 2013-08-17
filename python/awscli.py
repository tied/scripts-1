#!/usr/bin/python3
import sys, os, subprocess
from optparse import OptionParser

usage = "usage: %prog [options] arg1 arg2"
argumentParser = OptionParser(usage=usage, version="1.0")

reservation=['Rev-identifier','Rev-ID','AWSAccountID','SG']
instance=['INS-identifier','INS-ID','AMIID','PublicDNS','PrivateDNS','state','INS-key','AMILaunchIndex','productCodes','INS-type','launchtime','AvailabilityZone','kernelID','RAMID','platform','monitoringState','publicIP','privateIP','VPCID','subnetID','rootdeviceType','instanceLifecycle','SpotInstanceRequestID','instanceLicense','clusterPlacementGroup','virtualizationType','hypervisorType','clientToken','SGID','tenancy','EBSoptimized','IAMARN']
ebs=['EBS-identifier','name','EBS-ID','attachTimestamp','deleteOnTermination','EBS-type','IOPS']
tag=['TAG-identifier','typeIdentifier','TAG-ID','TAG-key','TAG-value']

def main():
    argumentsParser = OptionParser()
    argumentsParser.add_option("-v", "--verbose",action="store_true", dest="verbose", help="make lots of noise [default]")
    argumentsParser.add_option("-i", "--instance_description", action="store_true", dest="instanceDescription", help="Describe the account\'s instances")
    argumentsParser.add_option("-p", "--profile", action="store", dest="profile", help="profile as in ~/awscliconfigfile file [default] is default ")
    argumentsParser.add_option("-r", "--filter_region", action="store", dest="region", help="specify a region such as us-east-1\nus-west-1\nus-west-2\nap-northeast-1")
    (options, args) = argumentsParser.parse_args()
    
    get_instance_description()

def get_instance_description():
    desc=subprocess.check_output("ec2din --show-empty-fields", shell=True)        
    desc=desc.decode()
    descarray=desc.split('\n')
    db={}
    for i in range (0,len(descarray)):
        if descarray[i].split('\t')[0]=='RESERVATION':
            insid=descarray[i+1].split('\t')[1]
            db[insid]={}
            db[insid]=(dict(zip(reservation,descarray[i].split('\t'))))
        elif descarray[i].split('\t')[0]=='INSTANCE':
            db[insid].update(dict(zip(instance,descarray[i].split('\t'))))
    #    elif descarray[i].split('\t')[0]=='BLOCKDEVICE':
    #        db[instance].append(dict(zip(ebs,lines.split('\t'))))
        elif descarray[i].split('\t')[0]=='TAG':
            db[insid].update(dict(zip(tag,descarray[i].split('\t'))))
    #for key in db:
    #    print (key)
    #    for (name,value) in db[key].items():
    #        print ('\t',name,"=>",value,)
    for record in db: 
        print("Name ",db[record]['TAG-value'])
        print("\t","publicIP ",db[record]['publicIP'])


    #print(db)


'''    
    db={'RESERVATION':[],'INSTANCE':[],'BLOCKDEVICE':[],'TAG':[]}

    for lines in desc.split('\n'):
        if lines.split('\t')[0]=='RESERVATION':
            db['RESERVATION'].append(dict(zip(reservation,lines.split('\t'))))
        if lines.split('\t')[0]=='INSTANCE':
            db['INSTANCE'].append(dict(zip(instance,lines.split('\t'))))
        if lines.split('\t')[0]=='BLOCKDEVICE':
            db['BLOCKDEVICE'].append(dict(zip(ebs,lines.split('\t'))))
        if lines.split('\t')[0]=='TAG':
            db['TAG'].append(dict(zip(tag,lines.split('\t'))))

    for key in db:
        print (key,'=>',db[key]['TAG-value'],db[key]['PublicDNS'])
'''

if __name__ == "__main__":main()
