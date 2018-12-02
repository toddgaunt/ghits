#!/usr/bin/env python3

import requests
import json
import argparse
import sys
import os
from pathlib import Path
from unidiff import PatchSet

github_api = "https://api.github.com"

def get_pulls(account, repo, page, n):
        url_string = f'{github_api}/repos/{account}/{repo}/pulls?state=closed&page={page}&per_page={n}'
        print(url_string)
        r = requests.get(url_string)
        if not r.ok:
            print(f'Get request did not return OK ({r.status_code})')
            sys.exit(-1)
        return json.loads(r.text)

def get_closed_pulls(account, repo, n):
    pulls = []
    for page in range(0, (n // 30) + 1):
        if (page == n // 30):
            pulls += get_pulls(account, repo, page, n % 30)
        else:
            pulls += get_pulls(account, repo, page, 30)
        page += 1
    return pulls

def split_dict(dict):
    r1 = {}
    r2 = {}
    i = 0
    for key in dict:
        if (i < len(dict) // 2):
            r1[key] = dict[key]
        else:
            r2[key] = dict[key]
        i += 1
    return r1, r2

def main():
    parser = argparse.ArgumentParser(description="Pull some repo pulls")
    parser.add_argument('account', type=str,
                        help="Github user account to pull from")
    parser.add_argument('repo', type=str,
                        help="Github repository associated with an account")
    parser.add_argument('-n', type=int, default=30,
                        help="Number of issues to pull (default=30)")
    args = parser.parse_args(sys.argv[1:])
    pulls = get_closed_pulls(args.account, args.repo, args.n)
    #print(json.dumps(pulls, indent=4, sort_keys=True))
    out = {}
    for pull in pulls:
        print(pull["diff_url"], file=sys.stderr)
        r = requests.get(pull["diff_url"])
        diff = r.text
        patch = PatchSet(diff)
        tmp = {}
        for file in map(lambda x: x.source_file, patch.added_files):
            file = "/".join(list(Path(file).parts)[1:])
            tmp[file] = 1
        for file in map(lambda x: x.source_file, patch.modified_files):
            file = "/".join(list(Path(file).parts)[1:])
            tmp[file] = 1
        for file in map(lambda x: x.source_file, patch.removed_files):
            file = "/".join(list(Path(file).parts)[1:])
            tmp[file .os] = 1
        out[pull["title"] + pull["body"]] = tmp
    test, train = split_dict(out)
    with open("test.json", "w+") as f:
        f.write(json.dumps(test, indent=4, sort_keys=True))
    with open("train.json", "w+") as f:
        f.write(json.dumps(train, indent=4, sort_keys=True))

if __name__ == "__main__":
    main()
