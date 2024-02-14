import glob
import os
import subprocess
import argparse
import shutil

output_folder = './Generator_Folder'
pdq_folder = './PDQ_Folder'
sout_folder = './SOUT_Folder'
qout_folder = './QOUT_Folder'
timeoutPDQ = 60
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

# "-o", "./tpchqs/sout.xml",
# "--query_file", "./tpchqs/qout.xml", 
cmd1 = [python_executable,"query-and-schema-generator.py","translate_sql_and_query_and_prov", "-i", "./tpchqs/tpch.sql",  "-P", "-f", "--replace_dt",  "-q"]

# "-s", "./tpchqs/sout.xml",
# "-q", "./tpchqs/qout.xml"
cmd2 = ["java", "-jar", "pdq-main-2.0.0-executable.jar", "planner"  ]

def run_pdq(element,index):
    print(f"Running PDQ for {index}:{element}")
    try:
        sout_file_path = os.path.join(sout_folder, f"sout_{index}_{element}.xml")
        qout_file_path = os.path.join(qout_folder, f"qout_{index}_{element}.xml")
        gen_command = cmd2 +["-s"] + [sout_file_path]+ ["-q"] + [qout_file_path]
        run_cmd = subprocess.Popen(gen_command, stdout=subprocess.PIPE)
        output, error = run_cmd.communicate(timeout=timeoutPDQ)  
        output = output.decode("utf-8")
#        print("!!!!!",error)
        if error:
            output += "\n--------------[ERRORS]---------------\n" + error

    except subprocess.TimeoutExpired:
        print("in time out")
        run_cmd.kill() 
        output = f"Process timed out after {timeoutPDQ} seconds."
    finally:
        print("PDQ finally:")
        if not os.path.exists(pdq_folder):
            os.makedirs(pdq_folder)
        pdq_file_path = os.path.join(pdq_folder, f"PDQ_{index}_{element}.txt")
        print("PDQ file path:", pdq_file_path)
        print(output)
        with open(pdq_file_path, 'w') as output_file:
            output_file.write(output)


def run_all():
    for index,qi in enumerate(queries, start=1) :
        run_single(qi,index)
        
def run_single(qi,index=0,run_pdqs=True):
    fn = q_dir + qi + "/tpcq"+qi+".dl"
    with open(fn) as f:
        lines = [line for line in f.readlines() if line.strip()]
        query = lines[0].rstrip()
        print("\n\nRunning query",index,":",queries[qi][0])
        # for element in queries[qi]:
        element = queries[qi][0]

        sout_file_path = os.path.join(sout_folder, f"sout_{index}_{element}.xml")

        qout_file_path = os.path.join(qout_folder, f"qout_{index}_{element}.xml")

        gen_command = cmd1 + [query] + ["--prov"] + [element] + ["-o"]+ [sout_file_path] + ["--query_file"] + [qout_file_path]
        run_cmd = subprocess.Popen(gen_command, stdout=subprocess.PIPE)
        output,error = run_cmd.communicate()
        output = output.decode("utf-8")
        error = error
        if error:
            output += "\n----[ERRORS]-----\n" + error
        else:
            with open(qout_file_path) as fq:
                print(fq.read())
        if not os.path.exists(output_folder):
            os.makedirs(output_folder)
        output_file_path = os.path.join(output_folder, f"output_{index}_{element}.txt")
        with open(output_file_path, 'w') as output_file:
            output_file.write(output)
            
    if run_pdqs:
            run_pdq(element,index)
                
        
def main():
    folders_to_check = [output_folder, pdq_folder,sout_folder,qout_folder]
    for folder in folders_to_check:
        if os.path.exists(folder):
            shutil.rmtree(folder)
    
    os.makedirs(sout_folder)
    os.makedirs(qout_folder)
    parser=argparse.ArgumentParser(description="Run tcp_h tests on pdq.")
    parser.add_argument('-i', '--individual', type=int, required=False, metavar="[1-20]", choices=range(1, 21))
    parser.add_argument('-r', '--run', action='store_true')
    parser.add_argument('-p', '--pk', action='store_true')
    args=parser.parse_args()
    if args.pk:
        print("Encoding primary key in as part of schema.")
        cmd1[6] = "-p"
    else:
        print("Encoding primary key in as dependencies.")
    if args.individual:
        run_single('{:02d}'.format(args.individual),1, run_pdqs = args.run)
    else:
        run_all()
    
if __name__ == '__main__':
    main()
