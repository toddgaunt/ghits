#!/usr/bin/env python3

import requests
import json
import argparse
import sys

github_api = "https://api.github.com"

def get_closed_issues(account, repo):
    r = requests.get(f'{github_api}/repos/{account}/{repo}/issues?state=closed&page=0')
    print(f'{github_api}/repos/{account}/{repo}/issues?state=closed&page=0')
    if not r.ok:
        print(f'Get request did not return OK ({r.status_code})')
        sys.exit(-1)
    issues = json.loads(r.text)
    print(json.dumps(issues, indent=4, sort_keys=True))


def main():
    parser = argparse.ArgumentParser(description="Pull some repo issues")
    parser.add_argument('account', type=str,
                        help="Github user account to pull from")
    parser.add_argument('repo', type=str,
                        help="Github repository associated with an account")
    args = parser.parse_args(sys.argv[1:])
    issues = get_closed_issues(args.account, args.repo)

if __name__ == "__main__":
    main()
