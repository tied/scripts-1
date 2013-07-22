#!/usr/bin/perl
use Getopt::Long;
use strict;
my $dirToSearch=`pwd`;
chomp($dirToSearch);
my $permission=750;
my $verbose='';
my $user='ccadmin';
my $group='ccadmin';
my $help;
my $scriptNmame=$0;
my %dirs;
my $exitStatus=0;

GetOptions ("d=s"   => \$dirToSearch,
            "p=i"   => \$permission,
	    "u=s"   => \$user,
	    "g=s"   => \$group,
	    "h"     => \$help,
            "v"     => \$verbose);
usage () if ($help);
getDirList();



sub getDirList(){
    print ("
Working on directory : $dirToSearch
Checking sub directories has permission : $permission
Checking sub directories are owned by user : $user
Checking sub directories belong to group : $group\n\n
Found errors in the following directories:\n") if ($verbose);

    opendir my $dh, $dirToSearch or die "$0: opendir: $!";
    my @dirsInFolder = grep {-d "$dirToSearch/$_" && ! /^\.{1,2}$/} readdir($dh);
    foreach (@dirsInFolder){
	my $perm=substr((sprintf "%o",(stat($_))[2]),-3);
	my $uid=getpwuid((stat($_))[4]);
	my $gid=getgrgid((stat($_))[5]);
	if (($perm ne $permission) or ($uid ne $user) or ($gid ne $group)){
	    $dirs{$_}=[$perm,$uid,$gid];
	    print "$_: permission = $perm\tuid = $uid\tgid = $gid\n" if ($verbose);
	}
    }
    if (scalar (keys %dirs ) > 0){
	exit (1);
    }else{
	exit (0);
    }	    
}


sub usage{
    print "$scriptNmame [arguments]
\t-d\t dir to search in ( . is defult)
\t-p\t permission to check in octal ( 750 is defult )
\t-u\t the dirs' user (ccadmin is defult)
\t-g\t the dirs' group ( ccadmin is the defult)
\t-h\t for usage
\t-v\t for more details in the output\n";
    exit (1);
}

