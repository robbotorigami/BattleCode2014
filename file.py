import glob
import os
import os
sumat = 0
for filename in glob.glob('*.java'):
    with open(filename) as f:
        for i, l in enumerate(f):
            pass
    sumat += i + 1
    print(filename, i+1)
for filename in os.listdir('util'):
    with open('util/'+filename) as f:
        for i, l in enumerate(f):
            pass
    sumat += i + 1
    print(filename, i+1)
print(sumat)
os.system("pause")
