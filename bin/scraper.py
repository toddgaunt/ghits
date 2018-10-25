#!/usr/bin/env python3

import requests
import json
import argparse
import sys

github_api = "https://api.github.com"

def main():
    parser = argparse.ArgumentParser(description="Pull some repo issues")
    parser.add_argument('account', type=str,
                        help="Github user account to pull from")
    parser.add_argument('repo', type=str,
                        help="Github repository associated with an account")
    args = parser.parse_args(sys.argv[1:])
    r = requests.get(f'{github_api}/repos/{args.account}/{args.repo}')
    print(f'{github_api}/repos/{args.account}/{args.repo}')
    if not r.ok:
        print("Get request did not return OK")
        sys.exit(-1)
    repo_data = json.loads(r.text)
    print(repo_data)

if __name__ == "__main__":
    main()
