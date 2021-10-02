import sys

data = []

for line in sys.stdin:
    try:
        data.append(int(line.split('\t')[1]))
    except:
        pass

sys.stdout.write("mean\t{}".format(sum(data) / len(data)))
