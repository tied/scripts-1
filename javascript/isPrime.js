function isPrime(num) {
    if(num < 2) return false;
    for (var i = 2; i < num; i++) {
        if(num%i==0)
            return false;
    }
    return true;
}
var myprimes = new Array()
var i=2
while (myprimes.length<100){
    if (isPrime(i)){
	myprimes.push(i)
	}
    i++ 
}

console.log(myprimes.join(","));
var fs = require('fs');
var outfile = "primes.txt";
fs.writeFileSync(outfile, myprimes.join(","));
