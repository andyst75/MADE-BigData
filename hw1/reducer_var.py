import sys

data = []

for line in sys.stdin:
    try:
        data.append(int(line.split('\t')[1]))
    except:
        pass

mean = sum(data) / len(data)
var = sum((x - mean) ** 2 for x in data) / len(data)

sys.stdout.write("var\t{}".format(var))
