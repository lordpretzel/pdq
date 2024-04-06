import glob
import os
import subprocess
import argparse
import shutil
import time
from datetime import datetime

output_folder_1 = './Generator_Folder_1'
output_folder_3 = './Generator_Folder_3'
pdq_folder_1 = './PDQ_Folder_1'
pdq_folder_3 = './PDQ_Folder_3'
sout_folder_1 = './SOUT_Folder_1'
sout_folder_3 = './SOUT_Folder_3'
qout_folder_1 = './QOUT_Folder_1'
qout_folder_3 = './QOUT_Folder_3'
table_folder_1 = './Table_1'
table_folder_3 = './Table_3'

timeoutPDQ = 3600
q_dir = os.path.join(os.path.dirname(__file__), "tpchqs/tpcq")
# store commandline parameters
options = None

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
python_executable = shutil.which('python') or shutil.which('python3.9') or shutil.which('python3')

cmd1 = [python_executable,"query-and-schema-generator.py","translate_sql_and_query_and_prov", "-i", "./tpchqs/tpch.sql",  "-P", "-p", "--replace_dt",  "-q"]
#"-p" instead of "-f"

cmd2 = ["java", "-jar", "pdq-main-2.0.0-jar-with-dependencies.jar", "planner"  ]


cmd3 = [python_executable,"query-and-schema-generator.py","translate_sql_and_query_and_prov", "-i", "./tpchqs/tpch.sql",  "-P", "-f", "--replace_dt",  "-q"]

def run_pdq(element,index,num=None):
    print(f"Running PDQ for {index}:{element}")
    pdq_time_taken=0
    pdq_status = "Success"
    try:
        sout_folder=sout_folder_1 if num==1 else sout_folder_3
        sout_file_path = os.path.join(sout_folder, f"sout_{index}_{element}.xml")

        qout_folder=qout_folder_1 if num==1 else qout_folder_3
        qout_file_path = os.path.join(qout_folder, f"qout_{index}_{element}.xml")
        gen_command = cmd2 +["-s"] + [sout_file_path]+ ["-q"] + [qout_file_path] + [f"-Dtimeout={timeoutPDQ*1000}"] +[f"-DdagThreadTimeout={timeoutPDQ*1000}"]
        start_time_pdq = time.time()
        run_cmd = subprocess.Popen(gen_command, stdout=subprocess.PIPE)
        output, error = run_cmd.communicate(timeout=timeoutPDQ)
        output = output.decode("utf-8")
        if error:
          print("PDQ Error:")
          pdq_status = f"Failure: {error}"
        if error is None and 'ERROR' in output:
            raise Exception("--custom")
    except subprocess.TimeoutExpired:
        print("PDQ Timeout:")
        pdq_status = "Timeout"
        run_cmd.kill()
        output = f"Process timed out after {timeoutPDQ} seconds."
    except Exception as e:
        pdq_status = f"ERROR: {e}"
        print("PDQ Exception:")
    finally:
        end_time_pdq = time.time()
        pdq_time_taken = end_time_pdq - start_time_pdq
        print('PDQ finally')

        pdq_folder=pdq_folder_1 if num==1 else pdq_folder_3
        pdq_file_path = os.path.join(pdq_folder, f"PDQ_{index}_{element}.txt")
        print("PDQ file path:", pdq_file_path)
        with open(pdq_file_path, 'w') as output_file:
            output_file.write(f"------------------{datetime.fromtimestamp(start_time_pdq).strftime('%Y-%m-%d %H:%M:%S')}------------------\n")
            output_file.write(output)
            output_file.write(f"\n------------------{datetime.fromtimestamp(end_time_pdq).strftime('%Y-%m-%d %H:%M:%S')}------------------\n")

    return pdq_status, pdq_time_taken

def run_all(table,num=None,overwrite=False,run_pdq=False):
    for index, qi in queries.items():
        run_single(qi,index,table=table,num=3,overwrite=overwrite) if num==3 else run_single(qi,index,table=table,num=1,overwrite=overwrite)

def run_single(qi,index,table=None,num=None):
    cmd1_status = "Success"
    cmd1_time_taken = 0

    print(f"will run Q{index} on tables {qi}")

    # iterate through tables
    for table_name in qi:
        fn = q_dir + index + "/tpcq"+index+".dl"
        # table_name = qi[0]
        print("\n" + 80 * "*" + "\nRunning query",index,":",table_name)

        # setup outputs
        output_folder = output_folder_1 if num==1 else output_folder_3
        output_file_path = os.path.join(output_folder, f"output_{index}_{table_name}.txt")
        sout_folder=sout_folder_1 if num==1 else sout_folder_3
        sout_file_path = os.path.join(sout_folder, f"sout_{index}_{table_name}.xml")
        qout_folder=qout_folder_1 if num==1 else qout_folder_3
        qout_file_path = os.path.join(qout_folder, f"qout_{index}_{table_name}.xml")

        print(f"would write to {output_file_path} {sout_file_path} {qout_file_path}")
        if (not options.overwrite) and os.path.exists(output_file_path) and os.path.exists(sout_file_path) and os.path.exists(qout_file_path):
            print(f"we are not overwriting and all output files already exist")
        else:
            if options.run_generator:
                print(f"will write results to files: {output_file_path} {sout_file_path} {qout_file_path}")

                with open(fn) as f:
                    lines = [line for line in f.readlines() if line.strip()]
                    query = lines[0].rstrip()
                    qi = qi or queries[index]

                    try:
                              cmd=cmd1 if num==1 else cmd3
                              gen_command = cmd + [query] + ["--prov"] + [table_name] + ["-o"]+ [sout_file_path] + ["--query_file"] + [qout_file_path]

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
                              with open(output_file_path, 'w') as output_file:
                                  output_file.write(f"------------------{datetime.fromtimestamp(start_time_exec).strftime('%Y-%m-%d %H:%M:%S')}------------------\n")
                                  output_file.write(output)
                                  output_file.write(f"\n------------------{datetime.fromtimestamp(end_time_exec).strftime('%Y-%m-%d %H:%M:%S')}------------------\n")

            if options.run_pdqs:
                pdq_status, pdq_time_taken = run_pdq(table_name,index,num)
            else:
                pdq_status, pdq_time_taken = "didn't run", 0

            write_to_table_and_log(table,
                      f"{index}_{table}", cmd1_status, "{:.2f}".format(cmd1_time_taken), pdq_status, "{:.2f}".format(pdq_time_taken))

def write_to_table_and_log(table, *elements):
    print(f"writing result row: {elements}")
    table.write("{:<15} {:<15} {:<15} {:<15} {:<15}\n".format(*elements))
    table.flush()

def delete_existing_folders(folders):
    for folder in folders:
        if os.path.exists(folder):
            shutil.rmtree(folder)

def create_folders():
    pdq_folders = [ pdq_folder_1, pdq_folder_3 ]
    generator_folders = [output_folder_1,output_folder_3, sout_folder_1,sout_folder_3,qout_folder_1,qout_folder_3,table_folder_1,table_folder_3]
    folders_to_check = pdq_folders + generator_folders
    # only delete result folder if we want to regenerate both the generator and pdf
    if options.overwrite:
        if options.run_pdq:
            print("REGENERATE PDQ FOLDERS")
            delete_existing_folders(pdq_folders)
        if options.run_generator:
            print("REGENERATE GENERATOR FOLDERS")
            delete_existing_folders(generator_folders)

    for folder in folders_to_check:
        os.makedirs(folder)

def main():
    # parse arguments
    global options
    parser=argparse.ArgumentParser(description="Run tcp_h tests on pdq.")
    parser.add_argument('-i', '--individual', type=int, required=False, metavar="[1-20]", choices=range(1, 21))
    parser.add_argument('-g', '--run_generator', action='store_true')
    parser.add_argument('-r', '--run_pdq', action='store_true')
    parser.add_argument('-p', '--pk', action='store_true')
    parser.add_argument('-o', '--overwrite', action='store_true', required=False)
    options=parser.parse_args()

    # if we are overwriting then delete and recreate results folders
    create_folders()

    writemode = 'w' if args.overwrite else 'a'

    print(f"Options: {args}")

    table1 = os.path.join(table_folder_1, "table.txt")
    print(table1)
    with open(table1, writemode) as table_file:
        if not args.overwrite:
            write_to_table_and_log(table_file,"Query", "cmd1_status", "cmd1_timetaken", "cmd2_status", "cmd2_timetaken")
        if args.individual:
            run_single(None,'{:02d}'.format(args.individual),run_pdqs =args.run,table=table_file)
        else:
            run_all(table_file,1,args.run)

    table = os.path.join(table_folder_3, "table.txt")
    print(table)
    with open(table, writemode) as table_file:
        if not args.overwrite:
            write_to_table_and_log(table_file,"Query", "cmd3_status", "cmd3_timetaken", "cmd2_status", "cmd2_timetaken")
        if args.individual:
            run_single(None,'{:02d}'.format(args.individual),run_pdqs =args.run,table=table_file)
        else:
            run_all(table_file,3)

if __name__ == '__main__':
    main()
