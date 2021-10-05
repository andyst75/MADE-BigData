import sys

data = []

n = 0
x_sum = 0
x_sum2 = 0
for line in sys.stdin:
    try:
        x = int(line.split('\t')[1])
        x_sum += x
        x_sum2 += x ** 2
        n += 1
    except:
        pass

if n < 2:
    var = 0
else:
    var = (x_sum2 - x_sum ** 2 / n) / (n - 1)

sys.stdout.write("var\t{}".format(var))
