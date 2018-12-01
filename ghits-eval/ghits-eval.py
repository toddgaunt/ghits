#!/usr/bin/env python3

import os
import sys
import threading
import queue
import argparse
import json
import pexpect
import time
import subprocess


# def t_output_reader(proc, outq):
#     for line in iter(proc.stdout.readline, ""):
#         outq.put(line)


def output_reader(proc):
    for line in iter(proc.stdout.readline, ""):
        if line == "" and proc.poll() is not None:
            break
        print(line)
    proc.stdout.close()


def main():
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

    print("Building index...")
    proc = pexpect.spawnu('make index ARGS={repo}'.format(repo=repo_dir), cwd=tool_dir)
    proc.expect(pexpect.EOF)
    print(proc.before)

    print("Build tool...")
    proc = pexpect.spawnu('make run', cwd=tool_dir)
    proc.expect('')

    # outq = queue.Queue()
    # t = threading.Thread(target=t_output_reader, args=(proc, outq))
    # t.daemon = True
    # t.start()
    #
    # try:
    #     time.sleep(0.30000)
    #
    #     try:
    #         line = outq.get(block=False)
    #         print(line, end="")
    #     except queue.Empty:
    #         print("no output yet")
    #
    #     time.sleep(0.1)
    # finally:
    #     proc.terminate()
    #     try:
    #         proc.wait(timeout=0.2)
    #         print("== subproccess exited with rc = ", proc.returncode)
    #     except subprocess.TimeoutExpired:
    #         print("subproccess did not terminate in time")
    #
    # t.join()


if __name__ == "__main__":
    main()
