# pathway_comparison_project

**INSTALLATION DE L'ALGORITHME RSD :**

mkdir RSD

cd RSD

git clone <url du rsd> (crée dossier reciprocal_smallest_distance)

mkdir dep (contiendra les archives des outils nécessaires)

mkdir bin (contiendra les binaires des outils)

Télécharger ncbi-blast -2.6.0+-x64.linux.tar.gz , paml 4.9e.tgz, et kalign current version dans le dossier RSD/dep (suivre les liens de la page github, il dit de prendre les anciennes versions mais ça marche aussi avec les nouvelles)

cd dep

tax xf current.tar.gz (extrait l'archive kalign dans le RSD/dep)

./configure

make

cp kalign RSD/bin

tar xf paml4.9e (extrait l'archive paml dans le RSD/dep)

cd paml4.9e/src

make

copier tous les binaires (fichiers sans extension) dans RSD/bin

cd dep

tar xf ncbi-blast -2.6.0+-x64.linux.tar.gz (extrait l'archive ncbi dans RSD/dep)

cd ncbi-blast-2.6.0+/bin

copier tous les binaires dans RSD/bin

Maintenant tous les binaires se trouvent dans RSD/bin

export PATH=$PATH:RSD/bin (éventuellement à mettre dans le .bashrc aussi)

source .bashrc

cd reciprocal_smallest_distance

python setup.py install  --user (bien vérifier que vous avez Python 2.7)

export PATH=$PATH: reciprocal_smallest_distance /bin  (éventuellement à mettre dans le .bashrc aussi)

source .bashrc

Vérifier que ça marche en faisant rsd_search –h depuis un autre emplacement, puis en lançant l'example décrit sur la page github

**LIBSBML POUR JAVA**

Ajouter le chemin vers le fichier libsbmlj.so au LD_LIBRARY_PATH

**EXECUTION DU PROGRAMME PathwayComparisonProject**

Le programme s'exécute avec : java -jar <nom du jar> 

Un message d'erreur s'affichera si libsbmlj ne peut pas se charger correctement

Les fichiers d'entrée doivent être des fichiers SBML Level 3. Après exécution du programme, le fichier est annoté avec la syntaxe suivante, dans l'attribut "name" de chaque fbc:GeneProduct : 
  - Pour les orthologues : ortho:<Id NCBI de l'enzyme du genome de reference/<Id NCBI de l'enzyme du second genome>
  - Pour les non-orthologues : [ref|query]:<Id NCBI de l'enzyme du genome courant>
  
**VISUALISATION COMPARATIVE AVEC CYTOSCAPE**

Pour fusionner deux réseaux métaboliques sur Cytoscape, il est nécessaire d'avoir l'application Cy3sbml disponible ( Apps → App Manager → install dans le menu ).

Une fois les deux SBML importés, la fusion sera réalisée à l'aide de l'outil Merge présent à partir de Tools dans le menu Cytoscape. (Tools → Merge).

La fusion s'effectue avec l'outil Union au niveau de la fenètre affiché à l'écran puis s'assurer que les réseaux à fusionner sont bien dans la rubrique Networks to Merge en utilisant add to select.

La fusion doit se faire au niveau des names, il est donc important de vérifier au niveau des options avancées qu'apparaisse name dans les deux colonnes Matching Columns.

La fusion est finalisée en cliquant sur Merge.

Pour visualiser les orthologues et différencier deux bactéries entre elles (style étant uniforme), différents styles doivent être appliqués (utilisation de Control Panel - Select et Style - situé dans la partie gauche de Cytoscape).

L'outil Select agit comme une expression régulière et va venir parser le sbml pour différents labels présents au niveau de ce dernier (name,ncbigi …).

En utilisant l'outil Select du Control Panel et en sélectionnant l'attribut shared name(+ → column filter → node : shared name), il est possible de sélectionner précisément les nœuds orthologues et les noeuds des bactéries (ref|query).

Exemple :(En utilisant le style Cy3sbml disponible)

Pour appliquer un style unique aux orthologues, il faut tout d'abord sélectionner les nœuds représentant les orthologues, en remplissant « ortho » dans la barre de recherche puis apply.

Ensuite, il est nécessaire de sélectionner l'outil style et plusieurs properties sont affichées.

Afin de modifier la couleur des nœuds sélectionnés, il faut cliquer dans le cadrant Byp vide (Set bypass). Une fenètre s'affiche pour choisir un panel de couleurs puis cliquer sur ok. La tache sera effectuée dans la sous-properties Fill Color.

La même opération est à répéter pour la forme du nœud dans la sous-properties Shape (pour valider cliquer random au niveau du réseau métabolique).

Pour différencier deux bactéries, l'opération précédente est à répéter.

Il faut taper « query » dans la barre de recherche dans le but de lui appliquer un nouveau style afin de différencier les nœuds entre les deux bactéries.

