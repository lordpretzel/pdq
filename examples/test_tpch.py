import glob
import os
import subprocess
import argparse
import shutil
import time
from datetime import datetime
import traceback

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
    "16": ["part", "partsupp"], # currently not supported "supplier"],
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
    start_time_pdq = time.time()
    try:
        sout_folder=sout_folder_1 if num==1 else sout_folder_3
        sout_file_path = os.path.join(sout_folder, f"sout_{index}_{element}.xml")

        qout_folder=qout_folder_1 if num==1 else qout_folder_3
        qout_file_path = os.path.join(qout_folder, f"qout_{index}_{element}.xml")
        gen_command = cmd2 +["-s"] + [sout_file_path]+ ["-q"] + [qout_file_path] + [f"-Dtimeout={options.timeout*1000}"] +[f"-DdagThreadTimeout={options.timeout*1000}"]

        output=""
        error=None
        start_time_pdq = time.time()
        run_cmd = subprocess.Popen(gen_command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        output, error = run_cmd.communicate(timeout=options.timeout)
        output = output.decode("utf-8")
        exit_code = run_cmd.returncode
        if error:
            error = error.decode("utf-8")
        if exit_code:
          print(f"PDQ Error [{exit_code}]:")
          pdq_status = f"Failure: {error}"
        if not exit_code and 'ERROR' in output:
            raise Exception("--custom: {error}")
    except subprocess.TimeoutExpired:
        print("PDQ Timeout:")
        pdq_status = "Timeout"
        run_cmd.kill()
        output = f"Process timed out after {options.timeout} seconds."
    except Exception as e:
        pdq_status = f"ERROR: {e}"
        print("PDQ Exception:")
        exc_type, exc_value, exc_traceback = sys.exc_info()
        lines = traceback.format_exception(exc_type, exc_value, exc_traceback)
        for line in lines:
            print(str(line) + "\n", file=sys.stderr)
    finally:
        if not output:
            output=""
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

def run_all(table,num=None):
    for index, table_names in queries.items():
        run_single(table_names,index,table=table,num=num)

def run_single(table_names,index,table=None,num=1):
    cmd1_status = "Success"
    cmd1_time_taken = 0

    print(f"will run Q{index} on tables {table_names}")

    table_names = table_names if table_names else queries[index]

    if options.table:
        table_names = [options.table]

    # iterate through tables
    for table_name in table_names:
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
                print(f"will write generator results to files: {output_file_path} {sout_file_path} {qout_file_path}")

                with open(fn) as f:
                    lines = [line for line in f.readlines() if line.strip()]
                    query = lines[0].rstrip()
                    #qi = qi or queries[index]

                    start_time_exec = time.time()
                    try:
                              cmd=cmd1 if num==1 else cmd3
                              gen_command = cmd + [query] + ["--prov"] + [table_name] + ["-o"]+ [sout_file_path] + ["--query_file"] + [qout_file_path]

                              run_cmd = subprocess.Popen(gen_command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
                              output,error = run_cmd.communicate()
                              exit_code = run_cmd.returncode
                              output = output.decode("utf-8")
                              if error:
                                  error = error.decode("utf-8")
                              if exit_code:
                                  print (f"Error from communicate {exit_code}:\n {error}")
                                  cmd1_status = f"Failure: {error}"
                                  output += "\n----[ERRORS]-----\n" + error
                              # if error is None and 'Traceback (most recent call last):' in output or output.strip() =="" :
                              #     raise Exception("--custom")
                    except Exception as e:
                              print(f"Error from exception: {e}")
                              output += f"\n---[EXCEPTIONS]------\n{e}"
                              cmd1_status = f"Error: {e}"
                    finally:
                              end_time_exec = time.time()
                              cmd1_time_taken = end_time_exec - start_time_exec
                              with open(output_file_path, 'w') as output_file:
                                  output_file.write(f"------------------{datetime.fromtimestamp(start_time_exec).strftime('%Y-%m-%d %H:%M:%S')}------------------\n")
                                  output_file.write(output)
                                  output_file.write(f"\n------------------{datetime.fromtimestamp(end_time_exec).strftime('%Y-%m-%d %H:%M:%S')}------------------\n")
            else:
                print("skip running generator")
            if options.run_pdq:
                pdq_status, pdq_time_taken = run_pdq(table_name,index,num)
            else:
                pdq_status, pdq_time_taken = "didn't run", 0
                print("skip running PDQ")

            write_to_table_and_log(table,
                      f"{index}_{table_name}", cmd1_status, "{:.2f}".format(cmd1_time_taken), pdq_status, "{:.2f}".format(pdq_time_taken))

def write_to_table_and_log(table, *elements):
    print(f"writing result row: {elements}")
    table.write("{:<15} {:<15} {:<15} {:<15} {:<15}\n".format(*elements))
    table.flush()

def delete_existing_folders(folders):
    for folder in folders:
        if os.path.exists(folder):
            shutil.rmtree(folder)

def create_folders():
    pdq_folders = { 1: [pdq_folder_1], 3: [pdq_folder_3] ]
    generator_folders = {
        1: [output_folder_1, sout_folder_1,qout_folder_1,table_folder_1]
        3: [output_folder_3, sout_folder_3,qout_folder_3, table_folder_3]
    }
    folders_to_check = []
    pdq_to_check = []
    generator_to_check = []

    if options.pk == 'pk' or options.pk == 'both':
        folders_to_check += pdq_folders[1] + generator_folders[1]
        pdq_to_check += pdq_folders[1]
        generator_to_check += genetator_folders[1]
    if options.pk == 'edg' or options.pk == 'both':
        folders_to_check += pdq_folders[3] + generator_folders[3]
        pdq_to_check += pdq_folders[3]
        generator_to_check += genetator_folders[3]
    # only delete result folder if we want to regenerate both the generator and pdf
    if options.overwrite and not options.individual:
        if options.run_pdq:
            print("REGENERATE PDQ FOLDERS")
            delete_existing_folders(pdq_to_check)
        if options.run_generator:
            print("REGENERATE GENERATOR FOLDERS")
            delete_existing_folders(generator_to_check)

    for folder in folders_to_check:
        if not os.path.exists(folder):
            os.makedirs(folder)

def run_experiment(num):
    print(f"RUN EXPERIMENT {'PK' if num == 1 else 'EDG'}")
    writemode = 'w' if options.overwrite else 'a'
    table_folder = table_folder_1 if num == 1 else table_folder_3
    table = os.path.join(table_folder, "table.txt")
    print(table)
    with open(table, writemode) as table_file:
        if not options.overwrite:
            write_to_table_and_log(table_file,"Query", f"cmd{3}_status", f"cmd{3}_timetaken", "cmd2_status", "cmd2_timetaken")
        if options.individual:
            run_single(None,'{:02d}'.format(options.individual),table=table_file,num=num)
        else:
            run_all(table_file,num)


def main():
    # parse arguments
    global options
    parser=argparse.ArgumentParser(description="Run tcp_h tests on pdq.")
    parser.add_argument('-i', '--individual', type=int, required=False, metavar="[1-20]", choices=range(1, 21))
    parser.add_argument('-g', '--run_generator', action='store_true')
    parser.add_argument('-r', '--run_pdq', action='store_true')
    parser.add_argument('-p', '--pk', choices=["pk","edg","both"], default="both")
    parser.add_argument('-o', '--overwrite', action='store_true', required=False)
    parser.add_argument('-t', '--timeout', default=3600,type=int)
    parser.add_argument('-T', '--table')
    options=parser.parse_args()

    # if we are overwriting then delete and recreate results folders
    create_folders()

    print(f"Options: {options}")

    if options.pk == "both" or options.pk == "pk":
        run_experiment(1)
    if options.pk == "both" or options.pk == "edg":
        run_experiment(3)

if __name__ == '__main__':
    main()
