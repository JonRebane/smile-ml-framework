#!/usr/bin/python3

import sys
import matplotlib.pyplot as plt

def extractSeq(ifile):
    lines = []
    with open(ifile, "r") as f:
        lines = f.readlines()

    sequences = {}
    for l in lines:
        interval = "".join(l.split("\n")).split(" ")

        seqid = int(interval[0])
        if seqid not in sequences.keys():
            sequences[seqid] = {}

        eventid = int(interval[1])
        if eventid not in sequences[seqid].keys():
            sequences[seqid][eventid] = []

        sequences[seqid][eventid] += [[int(interval[2]), int(interval[3])]]

    return sequences

def visualize(sequences, ofile):
    for seqid, event in sorted(sequences.items()):
        toplot = ()
        for eventid, val in sorted(event.items()):
            for beginend in val:
                toplot += beginend, [eventid]*2
        plt.plot(*toplot)
        plt.title("Sequence %d" % seqid)
        plt.xlabel("Time")
        plt.ylabel("Event ID")
        plt.savefig("sequence%d.png" % seqid)
        plt.show()
        plt.clf()

if __name__ == "__main__":
    if len(sys.argv) <= 1:
        print("Need 1 argument:\n\t- input file\n")
        exit(1)

    ifile = sys.argv[1]

    print("This script need the matplotlib package")

    sequences = extractSeq(ifile)
    visualize(sequences, "")

    print("Done!")

