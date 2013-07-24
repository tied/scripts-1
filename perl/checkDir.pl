#!/usr/bin/perl
use Getopt::Long;
use strict;
my $dirToSearch=`pwd`;
chomp($dirToSearch);
my $correctDirPerm=750;
my $correctVobFilePerm=444;
my $verbose='';
my $correctDirUid='ccadmin';
my $help;
my $scriptNmame=$0;
my $exitStatus=0;

GetOptions ("d=s"   => \$dirToSearch,
	    "h"     => \$help,
            "v"     => \$verbose);
usage () if ($help);
checkDirs();



sub checkDirs(){
    print ("
Working on directory : $dirToSearch
Checking sub directories has permission : $correctDirPerm
Checking sub directories are owned by user : $correctDirUid
Checking sub directories belong to group according to nameing convention: removing _vobs
checking <subDirectory>/.stgloc has permission : $correctVobFilePerm
checking <subDirectory>/.stgloc owner is : $correctDirUid
checking <subDirectory>/.stgloc group is the same as subdirectory group
Found errors in the following directories:\n") if ($verbose);

    opendir my $dh, $dirToSearch or die "$0: opendir: $!";
    my @dirsInFolder = grep {-d "$dirToSearch/$_" && ! /^\.{1,2}$/} readdir($dh);
    foreach my $subDir(@dirsInFolder){
	my $dirPerm=substr((sprintf "%o",(stat("$dirToSearch/$subDir"))[2]),-3);
	my $dirUid=getpwuid((stat("$dirToSearch/$subDir"))[4]);
	my $dirGid=getgrgid((stat("$dirToSearch/$subDir"))[5]);
	my $correctDirGid="$subDir";
	$correctDirGid =~ s/_vobs//g;
	my $vobFile="$dirToSearch/$subDir/.stgloc";
	my $vobFilePerm=substr((sprintf "%o",(stat($vobFile))[2]),-3);
	my $vobFileUid=getpwuid((stat($vobFile))[4]);
	my $vobFileGid=getgrgid((stat($vobFile))[5]);
	print ("checking $subDir:\n") if ($verbose);
	if ($dirPerm ne $correctDirPerm) {
	    print ("error in dir\'s permission : $dirPerm instead of $correctDirPerm\n") if ($verbose);
	    $exitStatus=1;
	}
	if($dirUid ne $correctDirUid){
	    print ("error in dir\'s owner : $dirUid instead of $correctDirUid\n") if ($verbose);
	    $exitStatus=1;
	}
	if($dirGid ne $correctDirGid){
	    print ("error in dir\'s group : $dirGid instead of $correctDirGid\n") if ($verbose);
	    $exitStatus=1;
	}
	if (-e $vobFile ){
	    if($vobFilePerm ne $correctVobFilePerm){
		print ("error in .stgloc perm : $vobFilePerm instead of $correctVobFilePerm\n") if ($verbose);
		$exitStatus=1;
	    }
	    if($vobFileUid ne $dirUid){
		print ("error in .stgloc owner : $vobFileUid instead of $dirUid\n") if ($verbose);
		$exitStatus=1;
	    }
	    if($vobFileGid ne $correctDirGid ){
		print ("error in .stgloc group : $vobFileGid instead of $correctDirGid\n") if ($verbose);
		$exitStatus=1;
	    }
	}else{
	    print (".stgloc file does not exist or script does not have permission\n") if ($verbose);
	    $exitStatus=1;
	}
	 
    }
	
    exit($exitStatus);
}


sub usage{
    print "$scriptNmame [arguments]
\t-d\t dir to search in ( . is defult)
\t-h\t for usage
\t-v\t for more details in the output\n";
    exit (1);
}

