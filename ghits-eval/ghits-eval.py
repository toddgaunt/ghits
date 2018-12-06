#!/usr/bin/env python3

import argparse
import json
import pexpect
import pytrec_eval

# argument parsing
a = argparse.ArgumentParser(
    description='evaluates the ghits tool, given a file with the truth data and a specific measure')
a.add_argument("-p", help='relative path to tool makefile', default="../", dest="tool_path")
a.add_argument("-r", help="name of repo relative to makefile", default="TestRepo", dest="repo_path")
a.add_argument("-t", help='path of file with list of relevant repos for each query', dest="rel_file", required=True)
a.add_argument("-m", help='<tf | coinflip | thesaurus>', dest="method", required=True)
arguments = a.parse_args()

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


def normalize_queries(obj, repo_dir, replacePath=False):
    temp = {}
    for query, files in obj.items():
        # items = {}
        # if replacePath:
        #     for i, k in files.items():
        #         q = i.replace("a/", repo_dir+"/")
        #         items[q] = k
        # else:
        #     items = files
        temp[normalize(query)] = files
    return temp


def order(obj):
    return json.loads(json.dumps(obj, sort_keys=True))


def main():

    tool_dir = arguments.tool_path
    repo_dir = arguments.repo_path
    method = arguments.method
    rel_data = json.load(open(arguments.rel_file))
    prompt = "Enter a query >> "

    if method == 'tf':
        print("Running Team1.app...")
        proc = pexpect.spawnu('make run ARGS=\"-s lnc.ltn\"', cwd=tool_dir)
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
            print("Exiting tool.")
    elif method == 'coinflip':
        print("Running Team1.App using coinflip mappings")
        proc = pexpect.spawnu('make run ARGS=\"-m coinflip_mappings.json\"', cwd=tool_dir)
        proc.expect(prompt)
        #print(proc.before)

        for query, files in rel_data.items():
            proc.sendline(normalize(query))
            #print(proc.before + proc.after)
            proc.expect(prompt)
        if proc.isalive():
            proc.sendline("q")
            proc.close()

        if proc.isalive():
            print("team1.app did not exit gracefully.")
            proc.close()
        else:
            print("Exiting team1.app.")
    elif method == 'thesaurus':
        print("Running Team1.App using thesaurus mapping")
        proc = pexpect.spawnu('make run ARGS=\" -m thesaurus.json \"', cwd=tool_dir)
        proc.expect(prompt)
        #print(proc.before)

        for query, files in rel_data.items():
            proc.sendline(normalize(query))
            # print(proc.before + proc.after)
            proc.expect(prompt)
        if proc.isalive():
            proc.sendline("q")
            proc.close()

        if proc.isalive():
            print("team1.app did not exit gracefully.")
            proc.close()
        else:
            print("Exiting team1.app.")


    print("Evaluating results...")
    rel_data = normalize_queries(rel_data, repo_dir, True)

    results_file = json.load(open(tool_dir+"ghits_output.json"))

    # print(json.dumps(order(rel_data), indent=4))
    # print(json.dumps(order(results_file), indent=4))

    evaluator = pytrec_eval.RelevanceEvaluator(order(rel_data), {'map', 'ndcg', 'Rprec'})
    print(json.dumps(evaluator.evaluate(order(results_file)), indent=4))


if __name__ == "__main__":
    main()
