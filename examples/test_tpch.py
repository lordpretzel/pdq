import glob
import os
import subprocess
import argparse
import shutil
import time

output_folder = './Generator_Folder'
pdq_folder = './PDQ_Folder'
sout_folder = './SOUT_Folder'
qout_folder = './QOUT_Folder'
table_folder = './Table'

timeoutPDQ = 3600
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

cmd1 = [python_executable,"query-and-schema-generator.py","translate_sql_and_query_and_prov", "-i", "./tpchqs/tpch.sql",  "-P", "-f", "--replace_dt",  "-q"]


cmd2 = ["java", "-jar", "pdq-main-2.0.0-executable.jar", "planner"  ]

def run_pdq(element,index):
    print(f"Running PDQ for {index}:{element}")
    pdq_time_taken=0
    pdq_status = "Success"
    try:
        sout_file_path = os.path.join(sout_folder, f"sout_{index}_{element}.xml")
        qout_file_path = os.path.join(qout_folder, f"qout_{index}_{element}.xml")
        gen_command = cmd2 +["-s"] + [sout_file_path]+ ["-q"] + [qout_file_path] + [f"-Dtimeout={timeoutPDQ}"] +[f"-DdagThreadTimeout={timeoutPDQ}"]
        start_time_pdq = time.time()
        run_cmd = subprocess.Popen(gen_command, stdout=subprocess.PIPE)
        output, error = run_cmd.communicate(timeout=timeoutPDQ)  
        output = output.decode("utf-8")
        if error:
          print("PDQ Error:")
          pdq_status = f"Failure: {error}"
        if error is None and 'Error' in output:
            raise Exception("--custom")
    except subprocess.TimeoutExpired:
        print("PDQ Timeout:")
        pdq_status = "Timeout"
        run_cmd.kill() 
        output = f"Process timed out after {timeoutPDQ} seconds."
    except Exception as e:
        pdq_status = f"Error: {e}"
        print("PDQ Exception:")
    finally:
        end_time_pdq = time.time()
        pdq_time_taken = end_time_pdq - start_time_pdq
        print('PDQ finally')
        pdq_file_path = os.path.join(pdq_folder, f"PDQ_{index}_{element}.txt")
        print("PDQ file path:", pdq_file_path)
        with open(pdq_file_path, 'w') as output_file:
            output_file.write(output)

    return pdq_status, pdq_time_taken


def run_all(table):
    for index, qi in queries.items():
        run_single(qi,index,table=table)
       


        
def run_single(qi,index,run_pdqs=True,table=None):
    cmd1_status = "Success"
    cmd1_time_taken = 0
    fn = q_dir + index + "/tpcq"+index+".dl"
    with open(fn) as f:
        lines = [line for line in f.readlines() if line.strip()]
        query = lines[0].rstrip()
        qi = qi or queries[index]
        # for element in qi:
        element = qi[0]
        print("\n\nRunning query",index,":",qi[0])
        
        try:
            sout_file_path = os.path.join(sout_folder, f"sout_{index}_{element}.xml")
            qout_file_path = os.path.join(qout_folder, f"qout_{index}_{element}.xml")
            gen_command = cmd1 + [query] + ["--prov"] + [element] + ["-o"]+ [sout_file_path] + ["--query_file"] + [qout_file_path]
            
            start_time_exec = time.time()
            run_cmd = subprocess.Popen(gen_command, stdout=subprocess.PIPE)
            output,error = run_cmd.communicate()
            output = output.decode("utf-8")
            
            if error:
                print (f"Error from communicate: {error}")
                cmd1_status = f"Failure: {error}"
                output += "\n----[ERRORS]-----\n" + error
            if error is None and 'Traceback (most recent call last):' in output or output.strip() =="" :
                raise Exception("--custom")
        except Exception as e:
            print(f"Error from exception: {e}")
            cmd1_status = f"Error: {e}"
        finally:
            end_time_exec = time.time()
            cmd1_time_taken = end_time_exec - start_time_exec
            output_file_path = os.path.join(output_folder, f"output_{index}_{element}.txt")
            with open(output_file_path, 'w') as output_file:
                output_file.write(output)
            
    if run_pdqs:
            pdq_status, pdq_time_taken = run_pdq(element,index)
    else:
        pdq_status, pdq_time_taken = "didn't run", 0
        
    table.write("{:<15} {:<15} {:<15} {:<15} {:<15}\n".format(
    f"{index}_{element}", cmd1_status, "{:.2f}".format(cmd1_time_taken), pdq_status, "{:.2f}".format(pdq_time_taken)))
        
def main():
   
    folders_to_check = [output_folder, pdq_folder,sout_folder,qout_folder,table_folder]
    for folder in folders_to_check:
        if os.path.exists(folder):
            shutil.rmtree(folder)
    
    os.makedirs(sout_folder)
    os.makedirs(qout_folder)
    os.makedirs(pdq_folder)
    os.makedirs(output_folder)
    os.makedirs(table_folder)

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
	
	
    table = os.path.join(table_folder, "table.txt")
    print(table)
    with open(table, 'w') as table_file:
        table_file.write("{:<15} {:<15} {:<15} {:<15} {:<15}\n".format("Query", "cmd1_status", "cmd1_timetaken", "cmd2_status", "cmd2_timetaken"))
        if args.individual:
            run_single(None,'{:02d}'.format(args.individual),run_pdqs =args.run,table=table_file)
        else:
            run_all(table_file)
    
if __name__ == '__main__':
    main()
