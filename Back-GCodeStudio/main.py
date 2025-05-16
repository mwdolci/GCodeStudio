
# -*- coding: utf-8 -*-

# Librairie standard
from pathlib import Path
from datetime import datetime
import os
import sys

# Modules internes
from interpreter import Interpreter
from writer import Writer
from machine import Machine
from tool_path_viewer import ToolPathViewer
from tool_path_viewer_config_loader import ToolPathConfigLoader

# Fonction pour nom de fichier à la date et heure du jour
def get_datetime_string():
    """Retourne la date et l'heure sous la forme YYYY-MM-DD_HH-MM-SS"""
    return datetime.now().strftime("%Y-%m-%d_%H-%M-%S")

# Fonction traitement G-Code
def gcode_treatment(path_gcode_file, path_export_file, file_name):

    # Charge les données de la machine
    Machine.load_config() 

    # Instanciation des classes
    obj_interpreter = Interpreter() 
    obj_writer = Writer()

    list_datas = obj_interpreter.analyze(path_gcode_file) # Récup data

    info_program_path = path_export_file / f"{file_name}_program.csv"
    info_tool_path = path_export_file / f"{file_name}_tool.csv"
    info_gcode_path = path_export_file / f"{file_name}_gcode.csv"

    obj_writer.write_info_program_csv(info_program_path, path_gcode_file, list_datas)
    obj_writer.write_info_tool_csv(info_tool_path, list_datas)
    obj_writer.write_info_gcode_file_csv(info_gcode_path, path_gcode_file, list_datas)


# Fonction traitement G-Code
def viewer_launch(path_gcode_file,stl_path_file):

    # Charge les config
    Machine.load_config() 
    ToolPathConfigLoader.load_config() 

    # Instanciation des classes
    obj_interpreter = Interpreter() 
    obj_toolpathviewer = ToolPathViewer()

    # Récup datas g-code
    list_datas = obj_interpreter.analyze(path_gcode_file)

    # Start viewer
    obj_toolpathviewer.open_viewer(stl_path_file, list_datas)

# Point d'entrée app
def main():
    if len(sys.argv) < 4:
        print("Usage: python script.py <chemin_fichier_gcode> <chemin_fichier_stl> <True|False>")
        sys.exit(1)

    path_gcode_file = Path(sys.argv[1])
    path_stl_file = Path(sys.argv[2])

    # Convertir le second argument en booléen
    flag_str = sys.argv[3].lower()
    if flag_str == 'true':
        launchViewer = True
    else:
        launchViewer = False

    temp_folder = Path(os.getenv('TEMP', '/tmp'))
    file_name = path_gcode_file.name
    export_file_csv = temp_folder
    export_file = temp_folder / get_datetime_string()

    gcode_treatment(path_gcode_file, export_file_csv, file_name)

    if (launchViewer):
        viewer_launch(path_gcode_file, path_stl_file)

if __name__ == "__main__":
    main()

    



    
    
