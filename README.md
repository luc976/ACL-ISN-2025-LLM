 Luc Lysandre Mathys Wassim

### Septembre à décembre: 
### obj: faire un jeu ou le heros doit survivre le plus longtemps possible dans un labyrinthe ou des monstres le poursuit 
- sprint0: creation du requisitory, prise en main de git
- sprint1: labyrinthe+ deplacement du heros + presence d'un mob (__HC__: qui se déplace et peut tuer le héros)
- sprint2: ajout de mob selon le timer + capacité de tuer les mob+ plus timer
- sprint3: ajout des munitions rationnement des munitions + ajout de different labyrinthe en fonction de la difficulté (vitesse/ munition/ nbr de mob)
- sprint4: estethique propre

### Etat d'avancement
séance1: prise en main/ création de l'équipe/ création d'un requisitory
séance2:

### Structure logique: bien garder les nom comme indiquer ici

__HC__: ce sont plutôt les tâches nécessaires pour les fonctionnalités (mais c'est bon)

- 1 fct "créationtab" d'un tableau de taille (n,m) de 0 avec heros dans un coin qui vaut 2  (map)    FAIT                             Luc
- 1 fct "creationmur" (creer des mur dans le tableau (les mur sont des 1))      FAIT                                                Luc
- 1 fct "blocage" (arrete le mouvement quand on tape un mur)     FAIT                                                                 Mathys
- 1 fct "mouvement" (fait bouger le personnage avec les touches du clavier, utiliser fct blocage le personnage est 2)             Wassim
- 1 fct"mob" (creer des mob les mob )        FAIT                                                                                     Mathys
- 1 fct "mouvmob" ( mouvement des mob utiliser fct blocage pour eviter les collision mur/ les mob se dirige vers le héros  )      Lysandre   FAIT
- 1 fct "mort" (le perso meure si il est sur la meme case qu'un mob et mettre fin a la partie )                                   Lysandre  FAIT

- 2 fct "chrono" (dure jusqu'a la mort du perso)  
- 2 fct "tire" (le personnage peut tirer des projectiles qui tue les mob)
- 2 fct "repop" (fait repop les mob mort/ de plus en plus de mob en fonction du chrono)

- 3 fct "recompense" (le personnage peut ramasser des projectiles par terre)
- 3 fct "ration" (garde en ration les balles)
- 3 fct "difficulté" (plusieur map qui influ sur les parametre)Luc FAIT

- 4 fct "affichage" (presente le tout de manière estethique)
