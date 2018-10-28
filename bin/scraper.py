#!/usr/bin/env python3

import requests
import json
import argparse
import sys
from unidiff import PatchSet

github_api = "https://api.github.com"

def get_closed_pulls(account, repo):
    r = requests.get(f'{github_api}/repos/{account}/{repo}/pulls?state=closed&page=0')
    print(f'{github_api}/repos/{account}/{repo}/pulls?state=closed&page=0')
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
    args = parser.parse_args(sys.argv[1:])
    pulls = get_closed_pulls(args.account, args.repo)
    #print(json.dumps(pulls, indent=4, sort_keys=True))
    out = []
    for pull in pulls:
        print(pull["diff_url"])
        r = requests.get(pull["diff_url"])
        diff = r.text
        patch = PatchSet(diff)
        tmp = {"query": pull["title"] + " " + pull["body"]}
        tmp["added"] = patch.added_files
        tmp["modified"] = patch.modified_files
        tmp["removed"] = patch.removed_files
        out.append(tmp)
    print(out)

if __name__ == "__main__":
    main()
