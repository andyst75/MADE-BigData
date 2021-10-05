import sys

data = []

n = 0
x_sum = 0
for line in sys.stdin:
    try:
        x = int(line.split('\t')[1])
        n += 1
        x_sum += x
    except:
        pass

sys.stdout.write("mean\t{}".format(x_sum / n))
