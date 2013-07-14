#!/usr/bin/python3
def main():
    def whoisbigger2(x,y): #two numbers as arguments and returns the largest of them
        return x if x>y else y
    def whoisbigger3(x,y,z): # takes three numbers as arguments and returns the largest of them
        return max(x,y,z)
    def howLong(x): #computes the length of a given list or string
        numberOfCharacters=0
        for i in x:
                numberOfCharacters +=1
        return numberOfCharacters
    def isVowel(x):
        return 'aeiou'.find(x.lower())
    def enumerateExample(x): # Enumerate Example
        for i, c in enumerate(x):
            print (i,c) 
    def BottlesofBeer():
        for i in range(99,0,-1):
            print ("{} bottles of beer on the wall, {} bottles of beer.\nTake one down, pass it around, {} bottles of beer on the wall.".format(i,i,i-1))
    def multiplyList(*args):
        multiplyOfDigit = 1  
        for i in args:multiplyOfDigit *= i
        return multiplyOfDigit
    def reverseString(x):
#        for c in reversed(x):
#            print (c)
        for c in x[::-1]:
            print (c)
    def isPalindrome(x):
        reverse=[]
        for c in reversed(x):reverse.append(c)
        print ((''.join(reverse))==x)
    def isMember(x,*args):
        value=False
        for i in args:
            if (x==i):
                value=True
                break
        return value
    def overLapping(*args):
        value=False
        for i in args[0]:
            for j in args[1]:
                if i==j:
                    value=True
                    break
        return value
    def generate_n_chars(n,c):
        i=0
        x=c
        while i<n-1:
            x+=c
            i+=1
        return x
    def histogram(*args):
        for n in args:print(n*'*')
    def max_in_list(*args):
        return max(args)
    def wordsLength(*args):
        return [len(str(x)) for x in args]
    def find_longest_word(*args):                 
        return max([len(str(x)) for x in args])
    def filter_long_words(n,*args):
        words=[]
        for x in args:
            if len(x)>n:
                words.append(x)
        return words
    import string
    def is_pangram(sentence, alphabet=string.ascii_lowercase):
        alphaset = set(alphabet)
        return alphaset <= set(sentence.lower())
            
    print (is_pangram('qwertyuiopasdfghjklzxcvb1234'))

if __name__ == "__main__": main()
    
    