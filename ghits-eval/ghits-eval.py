#!/usr/bin/env python

import os
import sys
import argparse
import json
from pprint import pprint
import subprocess
import re

# argument parsing
a = argparse.ArgumentParser(
    description='evaluates ghits retrieval, given a file with the relevant repos for each query and another file with the list of repos retrieved by our application.')
a.add_argument("--q", help='in addition to summary evaluation, give evaluation for each query', default=False, action="store_true",
               dest="q")
a.add_argument("--qrel_file", help='path of file with list of relevant repos for each query', dest="rel_file")
a.add_argument("--m", help='shows a specific measure, shows [xxx] by default', default="tf-idf", dest="method")
a.add_argument("--results_file", help='path the file with the list of repos retrieved by our application', dest="results_file")

arguments = a.parse_args()

#get commands
command = [arguments.q, arguments.rel_file, arguments.rel_file]