#!/usr/bin/python3
import sys, os
from optparse import OptionParser


usage = "usage: %prog [options] arg1 arg2"
argumentParser = OptionParser(usage=usage, version="1.0")


def main():
    argumentsParser = OptionParser()
    argumentsParser.add_option("-v", "--verbose",action="store_true", dest="verbose", default=True, help="make lots of noise [default]")
    argumentsParser.add_option("-q", "--quiet", action="store_false", dest="verbose", help="be vewwy quiet (I'm hunting wabbits)")
    (options, args) = argumentsParser.parse_args()
    




















if __name__ == "__main__":main()
