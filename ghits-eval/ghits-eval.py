#!/usr/bin/env python

import os
import sys
import argparse
import json
from pprint import pprint
import subprocess
import re

#argument parsing
a = argparse.ArgumentParser(description='evaluates ghits retrieval, given the official results and a set of judged results.')

