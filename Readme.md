# GCodeStudio

## Auteurs
**Dolci Marco** & **Toussaint Guillaume**

## Description
Ce projet a pour objectif de fournir une application capable d'analyser un programme de machine-outil (G-Code) afin d'extraire et de mettre en évidence des données pertinentes pour anticiper le comportement de la machine.  
La page principale de l'application se compose des 4 panels ci-dessous:
- Données générales du programme chargé (répertoire, nom du programme, estimation durée d'usinage, etc.)
- Données du programme chargé par outil (diagramme de gantt interactif, numéro outil, durée d'utilisation, durée d'usinage, durée improductif, etc.)
- Editeur du G-Code avec données programme ligne à ligne (Temps de départ, durée du segment, outil en cours d'utilisation, avance, vitesse de broche, type de mouvement, etc.)
- Simulateur de trajectoire 3D

## Statut du projet
Ce projet constitue une première version et est destiné à évoluer au fil du temps avec de nouvelles fonctionnalités et améliorations, notamment:
- Rendre le G-Code éditable directement depuis l'interface, avec recalcule automatique.
- Support de machines 5axes
- Support de machines de tournage
- Amélioration des performances de calcul en recodant l'interpérteur python dans un langage bas niveau (C, C++, ...)

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
Double clique sur launch.cmd

Pour tester l'application, il est possible d'utiliser les programmes G-Codes (.anc) et solide 3D (.stl) se trouvant dans le répertoire "data_testing".

### Manipulateur du viewer 3D:

Différentes touches permettent d'exécuter des fcontions spécifiques:
- "Space" → masquer/afficher la pièce.
- "Escape" → masquer/afficher toutes les trajectoires.
- "Up" et "Down" → défiliement des trajectoires rapide et travail par outil.