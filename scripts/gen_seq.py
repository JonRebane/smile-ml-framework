#!/usr/bin/python3

import sys

ifile, ofile = "seq.txt", "out.txt"

if len(sys.argv) <= 2:
    print("Need 2 arguments:\n\t- input file\n\t- output file\n")
    exit(1)

ifile = sys.argv[1]
ofile = sys.argv[2]

lines = []
with open(ifile, "r") as f:
    lines = f.readlines()

sequencecode = {}
for l in lines:
    event = "".join(l.split("\n")).split(" ")

    if event[0] not in sequencecode.keys():
        sequencecode[event[0]] = []

    args = ",".join([event[1],event[2],event[3]])
    sequencecode[event[0]] += ["\t\t\t\tnew Interval(%s)" % args]

towrite = ""
for key, val in sequencecode.items():
    towrite += "Sequence seq%s = new Sequence(Arrays.asList(\n" % key
    towrite += ",\n".join(val)
    towrite += "));\n\n"

with open(ofile, "w") as f:
    f.write(towrite)

print("Done!")

