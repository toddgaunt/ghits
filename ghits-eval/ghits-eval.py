#!/usr/bin/env python3

import os
import sys
import argparse
import json
from pprint import pprint
import subprocess
import re

# argument parsing
a = argparse.ArgumentParser(
    description='evaluates the ghits tool, given a file with the truth data and a specific measure')
# a.add_argument("-d", help="enable debug prinouts", default=False)
a.add_argument("-p", help='path to tool makefile', default="../", dest="tool_path")
a.add_argument("-r", help="path to repo source", default="TestRepo", dest="repo_path")
a.add_argument("-t", help='path of file with list of relevant repos for each query', dest="rel_file", required=True)
a.add_argument("-m", help='<MAP | P@R | F1>', default="MAP", dest="method")
arguments = a.parse_args()

tool_dir = arguments.tool_path
test_file = arguments.rel_file
repo_dir = arguments.repo_path
rel_data = json.load(open(test_file))

# build the index
cwd = os.getcwd()  # get current directory
try:
    process = subprocess.Popen("make index ARGS={repo}".format(repo=repo_dir), shell=True, stdout=subprocess.PIPE,
                               cwd=tool_dir, universal_newlines=True)
    for stdout_line in iter(process.stdout.readline, ""):
        print(stdout_line)
    process.stdout.close()
    return_code = process.wait()
    if return_code:
        raise subprocess.CalledProcessError(return_code, "make index")
finally:
    os.chdir(cwd)

# #
# try:
#     process = subprocess.Popen("make run", shell=True, stdout=subprocess.PIPE, stdin=subprocess.PIPE,
#                                cwd=tool_dir, universal_newlines=True)
#     for stdout_line in iter(process.stdout.readline, ""):
#         print(stdout_line)
#     process.stdout.close()
#
#     # run queries
#     for query, files in rel_data.items():
#         print(query)
#         process.stdin.write(query)
#         # for f in files:
#         #     #print(f)
#
#     return_code = process.wait()
#     if return_code:
#         raise subprocess.CalledProcessError(return_code, "make run")
# finally:
#     os.chdir(cwd)



