import sys
import csv

try:
    reader = csv.reader(sys.stdin)
    for row in reader:
        value = row[9]
        if value != 'price':
            sys.stdout.write('price\t{}\n'.format(value))        
except:
    pass
