#!/usr/bin/python
import sys
import argparse
import random
import re

parser = argparse.ArgumentParser(description='Generate random data samples')
parser.add_argument('-n', "--lines"         , type=int, default=100, help="number of transactions")
parser.add_argument('--overdraft', type=int, default=0  , help="position of overdraft transaction (default = no overdrafts)")
parser.add_argument('--print-balance', action="store_true", help="print balance together with the transaction")
parser.add_argument('--file-size', default="", help="estimated file size in this format ^(\d+)\s*(KB|MB|GB)")
args = parser.parse_args()

max_deposit = 100
balance = 0
line_count = 0
byte_count = 0

def parse_file_size(s):
    pattern = r"^(\d+)\s*(KB|MB|GB)"
    groups = re.search(pattern, s.upper())
    value = int(groups.group(1))
    unit = groups.group(2)
    factor = 1
    if "KB" in unit:
        factor = 1000
    elif "MB" in unit:
        factor = 1000 * 1000
    elif "GB" in unit:
        factor = 1000 * 1000 * 1000
    return value * factor


def loop():
    #FIXME: don't use global variable
    global balance
    global byte_count
    global line_count
    if (args.overdraft >= 0 and line_count == args.overdraft):
        transaction = -(balance + random.randint(0, max_deposit))
    else:
        transaction = random.randint(-balance, max_deposit)
    balance += transaction

    # print the transaction in normalized from: "[D|W] <positive_number>"
    if transaction > 0:
        s = 'D' + ' ' + str(transaction)
    else:
        transaction = -transaction
        s = 'W' + ' ' + str(transaction)
    if args.print_balance:
        s = s + ' ' + str(balance)
    print(s)
    byte_count += len(s)
    line_count += 1

# main work
max_byte_count = 0
if args.file_size:
    max_byte_count = parse_file_size(args.file_size)

print("BEGIN")
while True:
    loop()
    if line_count >= args.lines or (max_byte_count and byte_count >= max_byte_count):
        break
print("END")
