# GCodeStudio

## Auteurs
**Dolci Marco** & **Toussaint Guillaume**

## Description
Ce projet a pour objectif de fournir une application capable d'analyser un programme de machine-outil (G-Code) afin d'extraire et de mettre en évidence des données pertinentes pour anticiper le comportement de la machine.  

L’interface principale se compose de quatre panneaux :
- **Editeur de G-Code**
- **Données de la ligne active** (temps, durée, outil, vitesse, etc.)
- **Analyse par outil** avec diagramme de Gantt interactif
- **Données générales** du programme (répertoire, nom, durée estimée, etc.)

Il donne également accès à:
- **Simulateur de trajectoire 3D**

## Statut du projet
Cette version constitue une première itération. Les évolutions futures prévues incluent :
- Édition directe du G-Code avec recalcul automatique
- Prise en charge de machines 5 axes
- Prise en charge de machines de tournage
- Optimisation des performances en recodant l'interpéteur python dans un langage bas niveau (C, C++, ...)

## Installation et utilisation
### Prérequis
- Python 3
- Bibliothèques nécessaires (requirements.txt`)

### Installation
1. Clonez le dépôt :
   ```bash
   git clone https://github.com/mwdolci/CAS-IDD_GCodeStudio.git
   ```
2. Accédez au répertoire du projet :
   ```bash
   cd CAS-IDD_GCodeStudio
   ```
3. Installez les dépendances :
   ```bash
   pip install -r requirements.txt
   ```

### Utilisation
Double-cliquez sur "launch.cmd" pour lancer l'application

Pour tester l'application, il est possible d'utiliser les programmes G-Codes (.anc) et solide 3D (.stl) se trouvant dans le répertoire "data_testing".

### Manipulateur du simulateur 3D:

Différentes touches permettent d'exécuter des fonctions spécifiques:
- "Space" → masquer/afficher la pièce.
- "Escape" → masquer/afficher toutes les trajectoires.
- "Up" et "Down" → défilement des trajectoires rapides et travail par outil.