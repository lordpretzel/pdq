import glob
import os
import subprocess
import argparse

q_dir = "tpchqs/tpcq"

# Queries and the provenance tables we want to use
queries = {
    "01": ["lineitem"],
    "02": ["nation", "part", "partsupp", "region"],
    "03": ["customer", "lineitem", "orders"],
    "04": ["lineitem", "orders"],
    "05": ["customer", "lineitem", "nation", "orders", "region"],
    "06": ["lineitem"],
    "07": ["customer", "lineitem", "nation", "orders", "supplier"],
    "08": ["customer", "nation", "part", "supplier"],
    "09": ["lineitem", "nation", "orders", "part", "partsupp", "supplier"],
    "10": ["customer", "lineitem", "nation", "orders"],
    "11": ["nation", "partsupp", "supplier"],
    "12": ["lineitem", "orders"],
    "13": ["customer", "orders"],
    "14": ["lineitem", "part"],
    "15": ["lineitem", "supplier"],
    "16": ["part", "partsupp", "supplier"],
    "17": ["lineitem", "part"],
    "18": ["customer", "lineitem", "orders"],
    "19": ["lineitem", "part"],
    "20": ["part"],
    }

query = ""

cmd1 = ["python","query-and-schema-generator.py","translate_sql_and_query_and_prov", "-i", "./tpchqs/tpch.sql", "-o", "./tpchqs/sout.xml", "-P", "-f", "--replace_dt",  "--query_file", "./tpchqs/qout.xml", "-q"]

cmd2 = ["java", "-jar", "pdq-main-2.0.0-executable.jar", "planner", "-s", "./tpchqs/sout.xml", "-q", "./tpchqs/qout.xml"]

def run_all():
    for qi in queries:
        run_single(qi)
#    flist = glob.glob(q_dir + "{:02d}/tpcq{:02d}.dl".format(d,d))
        
def run_single(qi, run_pdq=False):
    fn = q_dir + qi + "/tpcq"+qi+".dl"
    print(fn)
    with open(fn) as f:
        lines = [line for line in f.readlines() if line.strip()]
        query = lines[0].rstrip()
#        query = '"' + query + '"'
        gen_command = cmd1 + [query] + ["--prov"] + [queries[qi][0]]
        print(gen_command)
        
        run_cmd = subprocess.Popen(gen_command, stdout=subprocess.PIPE)
        output = run_cmd.communicate()[0].decode("utf-8")
        print(output)
        
        if run_pdq:
            run_cmd = subprocess.Popen(cmd2, stdout=subprocess.PIPE)
            output = run_cmd.communicate()[0].decode("utf-8")
            print(output)
        
def main():
    parser=argparse.ArgumentParser(description="Run tcp_h tests on pdq.")
    parser.add_argument('-i', '--individual', type=int, required=False, metavar="[1-20]", choices=range(1, 21))
    parser.add_argument('-r', '--run', action='store_true')
    args=parser.parse_args()
    if args.individual:
        run_single('{:02d}'.format(args.individual), args.run)
    else:
        run_all()
    
if __name__ == '__main__':
    main()
