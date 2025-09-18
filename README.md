 Luc Lysandre Mathys Wassim

### Septembre à décembre: 
### obj: faire un jeu ou le heros doit survivre le plus longtemps possible dans un labyrinthe ou des monstres le poursuit 
#sprint0: creation du requisitory, prise en main de git
#sprint1: labiyrinthe+ deplacement du heros + presence d'un mob
#sprint2: ajout de mob selon le timer + capacité de tuer les mob+ plus timer
#sprint3: ajout des munitions rationnement des munitions + ajout de different labyrinthe en fonction de la difficulté (vitesse/ munition/ nbr de mob)
#sprint4: estethique propre

### Etat d'avancement
séance1: prise en main/ création de l'équipe/ création d'un requisitory
séance2:

### Structure logique:
1 fct création d'un tableau de taille (n,m) de 0 (map)
1 fct creation mur (creer des mur dans le tableau (les mur sont des 1))
1 fct blocage (arrete le mouvement quand on tape un mur)
1 fct mouvement (fait bouger le personnage avec les touches du clavier, le personnage est 2)
1 fct mob (creer des mob les mob )
1 fct mouv mob ( mouvement des mob )
1 fct mort (le perso meure si il rencontre un mob )

2 fct chrono (dure jusqu'a la mort du perso)
2 fct tire (le personnage peut tirer des projectiles qui tue les mob)
2 fct repop (fait repop les mob mort/ de plus en plus de mob en fonction du chrono)

3 fct recompense (le personnage peut ramasser des projectiles par terre)
3 fct ration (garde en ration les balles)
3 fct difficulté (plusieur map qui influ sur les parametre)

4 fct affichage (presente le tout de manière estethique)
