#!/usr/bin/env python3

import requests
import json
import argparse
import sys
from unidiff import PatchSet

github_api = "https://api.github.com"

def get_closed_pulls(account, repo, n):
    r = requests.get(f'{github_api}/repos/{account}/{repo}/pulls?state=closed&page=0&per_page={n}')
    print(f'{github_api}/repos/{account}/{repo}/pulls?state=closed&page=0&per_page={n}')
    if not r.ok:
        print(f'Get request did not return OK ({r.status_code})')
        sys.exit(-1)
    pulls = json.loads(r.text)
    return pulls


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
    out = []
    for pull in pulls:
        print(pull["diff_url"])
        r = requests.get(pull["diff_url"])
        diff = r.text
        patch = PatchSet(diff)
        tmp = {"query": pull["title"] + " " + pull["body"]}
        tmp["added"] = list(map(lambda x: x.source_file, patch.added_files))
        tmp["modified"] = list(map(lambda x: x.source_file, patch.modified_files))
        tmp["removed"] = list(map(lambda x: x.source_file, patch.removed_files))
        out.append(tmp)
    print(json.dumps(out, indent=4, sort_keys=True))

if __name__ == "__main__":
    main()
