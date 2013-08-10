#!/usr/bin/python3
import sys, os, subprocess
from optparse import OptionParser

usage = "usage: %prog [options] arg1 arg2"
argumentParser = OptionParser(usage=usage, version="1.0")


def main():
    argumentsParser = OptionParser()
    argumentsParser.add_option("-v", "--verbose",action="store_true", dest="verbose", help="make lots of noise [default]")
    argumentsParser.add_option("-i", "--instance_description", action="store_true", dest="instanceDescription", help="Describe the account\'s instances")
    argumentsParser.add_option("-p", "--profile", action="store", dest="profile", help="profile as in ~/awscliconfigfile file [default] is default ")
    argumentsParser.add_option("-r", "--filter_region", action="store", dest="region", help="specify a region such as us-east-1\nus-west-1\nus-west-2\nap-northeast-1")
    (options, args) = argumentsParser.parse_args()
    
def instance_description(profile):
    subprocess.call(["aws","ec2","describe-instances"])
        
    




 __name__ == "__main__":main()
