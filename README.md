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


