
# -*- coding: utf-8 -*-

from machine import Machine
import csv
import os

class Writer:
    """Classe qui permet d'écrire le rapport"""

    def __init__(self):
        self.digit_after_point_distance = 3
        self.digit_after_point_time = 4

        try:
            self.rapidfeedrate = Machine.data["machine"]["rapidfeedrate"]
        except KeyError:
            raise ValueError("MachineConfigError: Clé 'rapidfeedrate' absente du fichier JSON")

    def format_time(self, minutes):
        """Cette fonction convertit les minutes en heures, minutes, secondes"""
        total_seconds = minutes * 60  # Conversion en secondes
        hours = total_seconds // 3600  # Nombre d'heures --> // retourne l'entier arrondi vers le bas
        minutes = (total_seconds % 3600) // 60  # Nombre de minutes restantes --> // retourne l'entier arrondi vers le bas
        seconds = total_seconds % 60  # Nombre de secondes restantes

        if hours > 0:
            return f"{int(hours)}h {int(minutes)}m {int(seconds)}s"
        elif minutes > 0:
            return f"{int(minutes)}m {int(seconds)}s"
        else:
            return f"{int(seconds)}s"

    def write_report(self, file_name, program_name, list_datas):
        """Cette méthode crée et écrit les données dans le rapport"""
        current_tool = None
        time_sum = 0.0
        productive_time_sum = 0.0
        distance_sum = 0.0
        distance_in_material_sum = 0.0

        program_time = sum(item.time for item in list_datas)
        program_productive_time = sum(item.productive_time for item in list_datas)
        program_imporductive_time = program_time - program_productive_time

        with open(file_name, 'w') as file:
            file.write(f"Programme : {program_name}\n")
            file.write(f"Nombre de lignes du programme : {len(list_datas)}\n")
            file.write(f"Durée du programme : {self.format_time(program_time)}\n")
            file.write(f"Durée d'usinage : {self.format_time(program_productive_time)}\n")
            file.write(f"Durée improductive : {self.format_time(program_imporductive_time)}\n")
            
            for entry in list_datas:
                if entry.tool_number != current_tool:
                    if current_tool is not None and current_tool != 0:
                        file.write(
                            f"\nOutil N°{int(current_tool)}:\n"
                            f" Durée d'utilisation : {self.format_time(time_sum)}\n"
                            f" Durée d'usinage : {self.format_time(productive_time_sum)}\n"
                            f" Durée improductive : {self.format_time(time_sum - productive_time_sum)}\n"
                            f" Distance parcourue : {round(distance_sum, self.digit_after_point_distance)} mm\n"
                            f" Distance parcourue dans la matière : {round(distance_in_material_sum, self.digit_after_point_distance)} mm\n"
                        )

                    # Remise des compteurs à 0
                    current_tool = entry.tool_number
                    time_sum = 0.0
                    productive_time_sum = 0.0
                    distance_sum = 0.0
                    distance_in_material_sum = 0.0
                
                time_sum += entry.time
                productive_time_sum += entry.productive_time
                distance_sum += entry.distance
                distance_in_material_sum += entry.distance_in_material
            
            # Dernier outil
            if current_tool is not None and current_tool != 0:
                file.write(
                    f"\nOutil N°{int(current_tool)}:\n"
                    f" Durée d'utilisation : {self.format_time(time_sum)}\n"
                    f" Durée d'usinage : {self.format_time(productive_time_sum)}\n"
                    f" Durée improductive : {self.format_time(time_sum - productive_time_sum)}\n"
                    f" Distance parcourue : {round(distance_sum, self.digit_after_point_distance)} mm\n"
                    f" Distance parcourue dans la matière : {round(distance_in_material_sum, self.digit_after_point_distance)} mm\n"
                )

    def write_info_program_csv(self, file_name, program_name, list_datas):
        """Crée et écrit les données du programme dans un fichier CSV"""
        program_time = sum(item.time for item in list_datas) * 60
        program_productive_time = sum(item.productive_time for item in list_datas) * 60
        program_unproductive_time = program_time - program_productive_time

        # Extraire uniquement le nom du fichier
        program_filename = os.path.basename(program_name)

        with open(file_name, mode='w', newline='') as csvfile:
            fieldnames = [
                'Program Path',
                'Program File',
                'Program Lines',
                'Total Program Time (s)',
                'Total Productive Time (s)',
                'Total Unproductive Time (s)'
            ]
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            writer.writeheader()
            writer.writerow({
                'Program Path': program_name,
                'Program File': program_filename,
                'Program Lines': len(list_datas),
                'Total Program Time (s)': round(program_time, 2),
                'Total Productive Time (s)': round(program_productive_time, 2),
                'Total Unproductive Time (s)': round(program_unproductive_time, 2),
            })

    def write_info_tool_csv(self, file_name, list_datas):
        """Crée et écrit les données outil dans un fichier CSV"""
        current_tool = None
        time_sum = 0.0
        productive_time_sum = 0.0
        distance_sum = 0.0
        distance_in_material_sum = 0.0

        tools_data = []

        for entry in list_datas:
            if entry.tool_number != current_tool:
                if current_tool is not None and current_tool != 0:
                    tools_data.append({
                        'Tool Number': int(current_tool),
                        'Usage Time (s)': round(time_sum, 2),
                        'Productive Time (s)': round(productive_time_sum, 2),
                        'Unproductive Time (s)': round(time_sum - productive_time_sum, 2),
                        'Distance (mm)': round(distance_sum, self.digit_after_point_distance),
                        'Distance in Material (mm)': round(distance_in_material_sum, self.digit_after_point_distance)
                    })

                current_tool = entry.tool_number
                time_sum = 0.0
                productive_time_sum = 0.0
                distance_sum = 0.0
                distance_in_material_sum = 0.0

            time_sum += entry.time * 60
            productive_time_sum += entry.productive_time * 60
            distance_sum += entry.distance
            distance_in_material_sum += entry.distance_in_material

        # Dernier outil
        if current_tool is not None and current_tool != 0:
            tools_data.append({
                'Tool Number': int(current_tool),
                'Usage Time (s)': round(time_sum, 2),
                'Productive Time (s)': round(productive_time_sum, 2),
                'Unproductive Time (s)': round(time_sum - productive_time_sum, 2),
                'Distance (mm)': round(distance_sum, self.digit_after_point_distance),
                'Distance in Material (mm)': round(distance_in_material_sum, self.digit_after_point_distance)
            })

        # Écriture du CSV
        with open(file_name, mode='w', newline='') as csvfile:
            fieldnames = [
                'Tool Number',
                'Usage Time (s)',
                'Productive Time (s)',
                'Unproductive Time (s)',
                'Distance (mm)',
                'Distance in Material (mm)'
            ]
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            writer.writeheader()
            writer.writerows(tools_data)

    def write_debug_file(self, file_name, program_name, list_datas):
        """Cette méthode crée et écrit un fichier de debug pour analyse"""

        current_tool = None
        time_sum = 0.0
        productive_time_sum = 0.0
        distance_sum = 0.0
        void = ""

        program_time = sum(item.time for item in list_datas)
        program_productive_time = sum(item.productive_time for item in list_datas)
        program_imporductive_time = program_time - program_productive_time

        with open(file_name, 'w') as file:
            file.write(f"Programme : {program_name}\n")
            file.write(f"Nombre de lignes du programme : {len(list_datas)}\n")
            file.write(f"Durée du programme : {self.format_time(program_time)}\n")
            file.write(f"Durée d'usinage : {self.format_time(program_productive_time)}\n")
            file.write(f"Durée improductive : {self.format_time(program_imporductive_time)}\n")
            
            for entry in list_datas:

                # Données opération
                if entry.tool_number != current_tool:
                    if current_tool is not None and current_tool != 0:
                        file.write(
                            f"\n"
                            f"{void.ljust(52)} ==> "
                            f"Distance: {str(round(distance_sum, self.digit_after_point_distance)).ljust(10)}mm   "
                            f"Distance dans la matière: {str(round(distance_in_material_sum, self.digit_after_point_distance)).ljust(10)}mm   "
                            f"Durée: {self.format_time(time_sum).ljust(10)}"
                            f"Durée d'usinage: {self.format_time(productive_time_sum).ljust(10)}"
                            f"Durée imporductif: {self.format_time(time_sum - productive_time_sum)}\n\n"
                        )
                    
                    # Remise des compteurs à 0
                    current_tool = entry.tool_number
                    time_sum = 0.0
                    productive_time_sum = 0.0
                    distance_sum = 0.0
                    distance_in_material_sum = 0.0
                
                time_sum += entry.time
                productive_time_sum += entry.productive_time
                distance_sum += entry.distance
                distance_in_material_sum += entry.distance_in_material

                # Données ligne
                if entry.move_type == 0 or entry.move_type == 1 :
                    radius = 0.0
                else:
                    radius = entry.radius

                if entry.move_type == 0 :
                    feedrate = self.rapidfeedrate
                else:
                    feedrate = entry.feedrate

                if entry.g_code_line: 
                    file.write(
                        f"{entry.g_code_line.ljust(50)} --> "
                        f"Outil: {str(entry.tool_number).ljust(10)}"
                        f"Mouvement: {entry.move_type.name.ljust(20)}"
                        f"Position X: {str(round(entry.endpoint_x, self.digit_after_point_distance)).ljust(10)}"
                        f"Position Y: {str(round(entry.endpoint_y, self.digit_after_point_distance)).ljust(10)}"
                        f"Position Z: {str(round(entry.endpoint_z, self.digit_after_point_distance)).ljust(10)}"
                        f"Rayon: {str(radius).ljust(10)}"
                        f"Distance: {str(round(entry.distance, self.digit_after_point_distance)).ljust(10)}"
                        f"Distance dans la matière: {str(round(entry.distance_in_material, self.digit_after_point_distance)).ljust(10)}"
                        f"Avance: {str(feedrate).ljust(10)}"
                        f"Durée: {str(round(entry.time * 60, self.digit_after_point_time))}s \n"
                    )

            # Données outils dernier outil
            if current_tool is not None and current_tool != 0:
                file.write(
                        f"\n"
                        f"{void.ljust(52)} ==> "
                        f"Distance: {str(round(distance_sum, self.digit_after_point_distance)).ljust(10)}"
                        f"Distance dans la matière: {str(round(distance_in_material_sum, self.digit_after_point_distance)).ljust(10)}"
                        f"Durée: {self.format_time(time_sum)}\n\n"
                    )

    def write_info_gcode_file_csv(self, file_name, program_name, list_datas):
        """Cette méthode crée et écrit un fichier CSV gcode avec données ligne à ligne"""

        current_tool = None
        time_sum = 0.0
        productive_time_sum = 0.0
        distance_sum = 0.0
        distance_in_material_sum = 0.0
        cumulative_time = 0.0

        with open(file_name, mode='w', newline='') as csvfile:
            fieldnames = [
                'G-code Line',
                'Tool Number',
                'Move Type',
                'X Position',
                'Y Position',
                'Z Position',
                'Radius',
                'Distance (mm)',
                'Distance in Material (mm)',
                'Feedrate',
                'Duration (s)',
                'Cumulative Duration (s)',
                'Spindle speed'
            ]
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            writer.writeheader()

            for entry in list_datas:
                if entry.tool_number != current_tool:
                    # Remise des compteurs à 0
                    current_tool = entry.tool_number
                    time_sum = 0.0
                    productive_time_sum = 0.0
                    distance_sum = 0.0
                    distance_in_material_sum = 0.0

                cumulative_time += entry.time * 60
                
                time_sum += entry.time
                productive_time_sum += entry.productive_time
                distance_sum += entry.distance
                distance_in_material_sum += entry.distance_in_material

                # Valeurs par défaut
                if entry.move_type.name in ['CIRCULAR_MOVE_CW', 'CIRCULAR_MOVE_CCW']:
                    radius = entry.radius
                else:
                    radius = 0.0

                if entry.move_type.name == 'RAPID_MOVE':
                    feedrate = 25000
                else:
                    feedrate = entry.feedrate

                if entry.g_code_line:
                    writer.writerow({
                        'G-code Line': entry.g_code_line,
                        'Tool Number': entry.tool_number,
                        'Move Type': entry.move_type.name,
                        'X Position': round(entry.endpoint_x, self.digit_after_point_distance),
                        'Y Position': round(entry.endpoint_y, self.digit_after_point_distance),
                        'Z Position': round(entry.endpoint_z, self.digit_after_point_distance),
                        'Radius': radius,
                        'Distance (mm)': round(entry.distance, self.digit_after_point_distance),
                        'Distance in Material (mm)': round(entry.distance_in_material, self.digit_after_point_distance),
                        'Feedrate': feedrate,
                        'Duration (s)': round(entry.time * 60, self.digit_after_point_time),
                        'Cumulative Duration (s)': round(cumulative_time, self.digit_after_point_time),
                        'Spindle speed': int(entry.spindlespeed)
                    })


