#!/usr/bin/python3
def main():
    def read_config():
        configfile='config.txt'
        config={}
        with open(configfile) as f:
            for s in f.readlines():
                config[s.split('=')[0]]=s.split('=')[1]
        return(config)
    def get_files(**kwargs):
        import os
        # print (kwargs)
        fileList = []
        for root, dirnames, filenames in os.walk(kwargs['downloaddir'].strip()):
            for file in filenames:
                fpath = os.path.join(root, file)
                if (fpath.split('.')[-1])==kwargs['lookforfiles'].strip() and fpath.find('EBOOKS')!=-1:
                    fileList.append(fpath)   
        return fileList            
    def unzip_files(*args):
        import os
        import zipfile
        for filename in args:
            print ('working on {}'.format(os.path.abspath(filename).split('\\'))[0:-1])
            os.chdir('\\'.join((os.path.abspath(filename).split('\\'))[0:-1]))
            fh = open(filename, 'rb')
            z = zipfile.ZipFile(fh)
            for name in z.namelist():
                outfile = open(name, 'wb')
                outfile.write(z.read(name))
                outfile.close()
            fh.close()

            
    unzip_files (*get_files(**read_config()))
    

if __name__ == "__main__": main()