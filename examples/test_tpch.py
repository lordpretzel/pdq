import glob
import os
import subprocess
import argparse
import shutil

output_folder = './Generator_Folder'
pdq_folder = './PDQ_Folder'
q_dir = os.path.join(os.path.dirname(__file__), "tpchqs/tpcq")


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
python_executable = shutil.which('python') or shutil.which('python3.9')

cmd1 = [python_executable,"query-and-schema-generator.py","translate_sql_and_query_and_prov", "-i", "./tpchqs/tpch.sql", "-o", "./tpchqs/sout.xml", "-P", "-f", "--replace_dt",  "--query_file", "./tpchqs/qout.xml", "-q"]

cmd2 = ["java", "-jar", "pdq-main-2.0.0-executable.jar", "planner", "-s", "./tpchqs/sout.xml", "-q", "./tpchqs/qout.xml"]

def run_pdq():
    run_cmd = subprocess.Popen(cmd2, stdout=subprocess.PIPE)
    output = run_cmd.communicate()[0].decode("utf-8")

    if not os.path.exists(pdq_folder):
        os.makedirs(pdq_folder)
        output_file_path = os.path.join(pdq_folder, "pdq_Output.txt")
        with open(output_file_path, 'w') as output_file:
                    output_file.write(output)
    

def run_all():
    for index,qi in enumerate(queries, start=1) :
        run_single(qi,index)
#    flist = glob.glob(q_dir + "{:02d}/tpcq{:02d}.dl".format(d,d))
        
def run_single(qi,index,run_pdq=False):
    fn = q_dir + qi + "/tpcq"+qi+".dl"
    with open(fn) as f:
        lines = [line for line in f.readlines() if line.strip()]
        query = lines[0].rstrip()
        print("\n\nRunning query", queries[qi])
        for element in queries[qi]:
            gen_command = cmd1 + [query] + ["--prov"] + [element]
            run_cmd = subprocess.Popen(gen_command, stdout=subprocess.PIPE)
            output = run_cmd.communicate()[0].decode("utf-8")
           
            if not os.path.exists(output_folder):
                os.makedirs(output_folder)
            output_file_path = os.path.join(output_folder, f"output_{index}_{element}.txt")
            with open(output_file_path, 'w') as output_file:
                output_file.write(output)
            if run_pdq:
                run_pdq()
                
        
def main():
    if os.path.exists(output_folder):
        shutil.rmtree(output_folder)
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
