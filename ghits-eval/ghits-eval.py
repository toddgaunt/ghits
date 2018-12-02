#!/usr/bin/env python3

import argparse
import json
import pexpect
import pytrec_eval


# def t_output_reader(proc, outq):
#     for line in iter(proc.stdout.readline, ""):
#         outq.put(line)


def output_reader(proc):
    for line in iter(proc.stdout.readline, ""):
        if line == "" and proc.poll() is not None:
            break
        print(line)
    proc.stdout.close()


def normalize(s):
    return s.replace("\r\n", " ")


def normalize_queries(obj):
    temp = {}
    for query, items in obj.items():
        temp[normalize(query)] = items
    return temp


def main():
    # argument parsing
    a = argparse.ArgumentParser(
        description='evaluates the ghits tool, given a file with the truth data and a specific measure')
    # a.add_argument("-d", help="enable debug prinouts", default=False)
    a.add_argument("-p", help='path to tool makefile', default="../", dest="tool_path")
    a.add_argument("-r", help="path to repo source", default="TestRepo", dest="repo_path")
    a.add_argument("-t", help='path of file with list of relevant repos for each query', dest="rel_file", required=True)
    a.add_argument("-m", help='<map | P@R | F1>', default="MAP", dest="method")
    arguments = a.parse_args()

    tool_dir = arguments.tool_path
    test_file = arguments.rel_file
    repo_dir = arguments.repo_path
    rel_data = json.load(open(test_file))

    print("Building index...")
    proc = pexpect.spawnu('make index ARGS={repo}'.format(repo=repo_dir), cwd=tool_dir)
    proc.expect(pexpect.EOF)
    print(proc.before)

    print("Building tool...")
    prompt = "Enter a query >> "
    proc = pexpect.spawnu('make run', cwd=tool_dir)
    proc.expect(prompt)
    print(proc.before)
    for query, files in rel_data.items():
        proc.sendline(normalize(query))
        print(proc.before + proc.after)
        proc.expect(prompt)
    if proc.isalive():
        proc.sendline("q")
        proc.close()

    if proc.isalive():
        print("tool did not exit gracefully.")
    else:
        print("Exiting tool...")

    print("Evaluating...")

    rel_data = normalize_queries(rel_data)

    results_file = json.load(open(tool_dir+"output.json"))
    evaluator = pytrec_eval.RelevanceEvaluator(rel_data, {'map', 'ndcg'})
    print(json.dumps(evaluator.evaluate(results_file), indent=4))


if __name__ == "__main__":
    main()
