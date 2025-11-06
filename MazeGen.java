package MazeGen;

import java.util.*;

public class MazeGenAcces {

    // === CONSTANTES DU JEU ===
    // Définit les valeurs pour chaque type de case dans le labyrinthe
    private static final int WALL = 1;    // Les murs (obstacles infranchissables)
    private static final int PATH = 0;    // Les chemins libres (cases vides)
    private static final int HERO = 2;    // Le héros (joueur)
    private static final int ENEMY = 3;   // Les ennemis (monstres)

    // === VARIABLES GLOBALES ===
    private static int rows;              // Nombre de lignes du labyrinthe
    private static int cols;              // Nombre de colonnes du labyrinthe
    private static double initialWallChance = 0.45; // Probabilité qu'une case soit un mur (45%)
    private static int[][] maze;          // Tableau 2D représentant le labyrinthe
    private static Random random = new Random(); // Générateur de nombres aléatoires

    public static void main(String[] args) {
        // Crée un scanner pour lire les entrées du joueur
        Scanner scanner = new Scanner(System.in);

        // === CHOIX DE LA TAILLE DU LABYRINTHE ===
        System.out.println("Choose cave map size:");
        System.out.println("1. Small (32x32)");
        System.out.println("2. Medium (64x64)");
        System.out.println("3. Large (128x128)");
        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();     // Lit le choix de l'utilisateur
        scanner.nextLine();                 // Consomme le retour à la ligne restant dans le buffer

        // Définit les dimensions selon le choix
        switch (choice) {
            case 1:
                rows = 32;
                cols = 32;
                break;
            case 2:
                rows = 64;
                cols = 64;
                break;
            case 3:
                rows = 128;
                cols = 128;
                break;
            default:                        // Si choix invalide, utilise 21x21
                rows = 21;
                cols = 21;
                break;
        }

        // === CRÉATION DU LABYRINTHE ===
        maze = new int[rows][cols];         // Initialise le tableau 2D avec les dimensions choisies

        fillRandomMaze();                   // Remplit le labyrinthe aléatoirement avec des murs et chemins

        // === LISSAGE DU LABYRINTHE (Algorithme cellulaire) ===
        // Effectue 5 itérations pour créer des formes organiques de cavernes
        int iterations = 5;
        for (int k = 0; k < iterations; k++) {
            maze = doSimulationStep(maze);  // Chaque itération lisse le labyrinthe
        }

        // === DÉFINITION DE L'ENTRÉE ET DE LA SORTIE ===
        maze[1][1] = PATH;                  // Entrée du labyrinthe (coin haut-gauche)
        maze[rows - 2][cols - 2] = PATH;    // Sortie du labyrinthe (coin bas-droite)

        connectRegions();                   // Connecte toutes les zones isolées pour garantir l'accessibilité

        // === INITIALISATION DU HÉROS ===
        int herosX = 1;                     // Position X (colonne) du héros
        int herosY = 1;                     // Position Y (ligne) du héros
        maze[herosY][herosX] = HERO;        // Place le héros à l'entrée du labyrinthe

        // === INITIALISATION D'UN ENNEMI ===
        int[] ennemiCoord = creerEnnemi(5, 5, maze);  // Crée un ennemi à la position (5,5)
        int ennemiX = ennemiCoord[0];       // Récupère la position X de l'ennemi
        int ennemiY = ennemiCoord[1];       // Récupère la position Y de l'ennemi

        // === BOUCLE DE JEU PRINCIPALE ===
        boolean jeuEnCours = true;          // Variable de contrôle de la boucle de jeu
        
        System.out.println("\n=== DÉBUT DU JEU ===");
        System.out.println("Commandes: haut, bas, gauche, droite, quitter");
        System.out.println("Objectif: Atteignez la sortie (coin bas-droite) sans vous faire tuer!");
        
        // Boucle tant que le jeu n'est pas terminé
        while (jeuEnCours) {
            // Affiche le labyrinthe avec le héros et l'ennemi
            printMazeWithHero();
            
            // Affiche les positions actuelles
            System.out.println("\nPosition du héros: (" + herosX + ", " + herosY + ")");
            System.out.println("Position de l'ennemi: (" + ennemiX + ", " + ennemiY + ")");
            System.out.print("Entrez votre direction: ");
            
            // Lit la commande du joueur
            String direction = scanner.nextLine().toLowerCase().trim();
            
            // Vérifie si le joueur veut quitter
            if (direction.equals("quitter") || direction.equals("q")) {
                System.out.println("\nMerci d'avoir joué!");
                jeuEnCours = false;         // Arrête la boucle de jeu
                break;                      // Sort de la boucle
            }
            
            // === TOUR DU JOUEUR ===
            // Déplace le héros selon la direction entrée
            int[] nouvellesCoord = mouvement(herosX, herosY, direction, maze);
            herosX = nouvellesCoord[0];     // Met à jour la position X du héros
            herosY = nouvellesCoord[1];     // Met à jour la position Y du héros
            
            // === TOUR DE L'ENNEMI ===
            // L'ennemi se déplace automatiquement vers le héros
            int[] nouvellesCoordEnnemi = deplacementEnnemi(ennemiX, ennemiY, herosX, herosY, maze);
            ennemiX = nouvellesCoordEnnemi[0];  // Met à jour la position X de l'ennemi
            ennemiY = nouvellesCoordEnnemi[1];  // Met à jour la position Y de l'ennemi
            
            // === VÉRIFICATION DE LA MORT ===
            // Vérifie si le héros et l'ennemi sont sur la même case
            if (verifierMort(herosX, herosY, ennemiX, ennemiY)) {
                printMazeWithHero();        // Affiche le labyrinthe une dernière fois
                System.out.println("\nGAME OVER!");
                jeuEnCours = false;         // Termine le jeu
            }
            
            // === VÉRIFICATION DE LA VICTOIRE ===
            // Vérifie si le héros a atteint la sortie
            if (herosX == cols - 2 && herosY == rows - 2) {
                printMazeWithHero();        // Affiche le labyrinthe final
                System.out.println("\n=== VICTOIRE! ===");
                System.out.println("Vous avez atteint la sortie!");
                jeuEnCours = false;         // Termine le jeu
            }
        }
        
        scanner.close();                    // Ferme le scanner pour libérer les ressources
    }

    /**
     * Remplit le labyrinthe aléatoirement avec des murs et des chemins
     * Les bords sont toujours des murs, l'intérieur est aléatoire
     */
    private static void fillRandomMaze() {
        // Parcourt toutes les cases du labyrinthe
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Vérifie si on est sur un bord
                if (i == 0 || j == 0 || i == rows - 1 || j == cols - 1) {
                    maze[i][j] = WALL;      // Les bords sont toujours des murs
                } else {
                    // Pour l'intérieur, génère aléatoirement un mur ou un chemin
                    // random.nextDouble() génère un nombre entre 0.0 et 1.0
                    // Si < 0.45 (45% de chance), c'est un mur, sinon c'est un chemin
                    maze[i][j] = random.nextDouble() < initialWallChance ? WALL : PATH;
                }
            }
        }
    }

    /**
     * Effectue une itération de l'algorithme cellulaire pour lisser le labyrinthe
     * Règles: Une case devient mur si elle a beaucoup de voisins murs
     * 
     * @param oldMap Le labyrinthe avant l'itération
     * @return Le nouveau labyrinthe après lissage
     */
    private static int[][] doSimulationStep(int[][] oldMap) {
        int[][] newMap = new int[rows][cols];  // Crée un nouveau tableau pour stocker les résultats

        // Parcourt toutes les cases
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Compte combien de voisins sont des murs
                int neighbors = countWallNeighbors(oldMap, i, j);

                // Applique les règles de l'algorithme cellulaire
                if (oldMap[i][j] == WALL) {
                    // Si c'était un mur: reste mur si ≥4 voisins murs, sinon devient chemin
                    newMap[i][j] = neighbors >= 4 ? WALL : PATH;
                } else {
                    // Si c'était un chemin: devient mur si ≥5 voisins murs, sinon reste chemin
                    newMap[i][j] = neighbors >= 5 ? WALL : PATH;
                }
            }
        }
        return newMap;  // Retourne le labyrinthe lissé
    }

    /**
     * Compte le nombre de murs dans les 8 cases adjacentes (voisins)
     * Utilisé par l'algorithme cellulaire
     * 
     * @param map Le labyrinthe à analyser
     * @param r La ligne de la case
     * @param c La colonne de la case
     * @return Le nombre de voisins qui sont des murs
     */
    private static int countWallNeighbors(int[][] map, int r, int c) {
        int count = 0;  // Compteur de murs voisins
        
        // Parcourt les 9 cases (3x3) centrées sur (r,c)
        for (int i = r - 1; i <= r + 1; i++) {
            for (int j = c - 1; j <= c + 1; j++) {
                // Ne compte pas la case elle-même
                if (i == r && j == c) continue;
                
                // Si hors limites, compte comme un mur (pour solidifier les bords)
                if (i < 0 || j < 0 || i >= rows || j >= cols) {
                    count++;
                }
                // Sinon, vérifie si la case voisine est un mur
                else if (map[i][j] == WALL) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Connecte toutes les zones isolées du labyrinthe
     * Garantit qu'on peut aller de l'entrée à n'importe quel point accessible
     */
    private static void connectRegions() {
        // Tableau pour marquer les cases visitables depuis l'entrée
        boolean[][] visited = new boolean[rows][cols];
        bfs(1, 1, visited);  // Effectue un BFS depuis l'entrée pour marquer toutes les cases accessibles

        // Liste des cases de chemin qui ne sont pas connectées à l'entrée
        List<int[]> disconnectedCells = new ArrayList<>();
        for (int i = 1; i < rows - 1; i++) {
            for (int j = 1; j < cols - 1; j++) {
                // Si c'est un chemin ET qu'il n'est pas visité, c'est une zone isolée
                if (maze[i][j] == PATH && !visited[i][j]) {
                    disconnectedCells.add(new int[]{i, j});
                }
            }
        }

        // Tant qu'il reste des zones déconnectées
        while (!disconnectedCells.isEmpty()) {
            // Prend une cellule déconnectée
            int[] cell = disconnectedCells.get(0);

            // === TROUVE TOUTE LA RÉGION DÉCONNECTÉE ===
            List<int[]> region = new ArrayList<>();  // Stocke toutes les cases de cette région
            boolean[][] regionVisited = new boolean[rows][cols];
            Queue<int[]> queue = new LinkedList<>();
            queue.add(cell);
            regionVisited[cell[0]][cell[1]] = true;

            // BFS pour trouver toutes les cases de cette région isolée
            while (!queue.isEmpty()) {
                int[] current = queue.poll();
                region.add(current);  // Ajoute cette case à la région

                int r = current[0];
                int c = current[1];

                // Vérifie les 4 voisins directs (haut, bas, gauche, droite)
                int[][] neighbors = {{r - 1, c}, {r + 1, c}, {r, c - 1}, {r, c + 1}};
                for (int[] n : neighbors) {
                    int nr = n[0], nc = n[1];
                    // Si voisin valide, chemin, et pas encore visité dans cette région
                    if (nr > 0 && nr < rows - 1 && nc > 0 && nc < cols - 1 &&
                        maze[nr][nc] == PATH && !regionVisited[nr][nc]) {
                        regionVisited[nr][nc] = true;
                        queue.add(new int[]{nr, nc});
                    }
                }
            }

            // === TROUVE LE POINT LE PLUS PROCHE ENTRE CETTE RÉGION ET LA ZONE PRINCIPALE ===
            int minDist = Integer.MAX_VALUE;  // Distance minimale trouvée
            int[] bestRegionCell = null;       // Meilleure case dans la région isolée
            int[] bestVisitedCell = null;      // Meilleure case dans la zone principale

            // Compare chaque case de la région avec chaque case accessible
            for (int[] rc : region) {
                for (int i = 1; i < rows - 1; i++) {
                    for (int j = 1; j < cols - 1; j++) {
                        if (visited[i][j]) {  // Si cette case est dans la zone principale
                            // Calcule la distance Manhattan (|x1-x2| + |y1-y2|)
                            int dist = Math.abs(rc[0] - i) + Math.abs(rc[1] - j);
                            if (dist < minDist) {
                                minDist = dist;
                                bestRegionCell = rc;
                                bestVisitedCell = new int[]{i, j};
                            }
                        }
                    }
                }
            }

            // === CREUSE UN CHEMIN ENTRE LES DEUX ZONES ===
            carvePath(bestRegionCell, bestVisitedCell);

            // Met à jour les zones visitées après connexion
            bfs(1, 1, visited);

            // Recalcule les cellules déconnectées
            disconnectedCells.clear();
            for (int i = 1; i < rows - 1; i++) {
                for (int j = 1; j < cols - 1; j++) {
                    if (maze[i][j] == PATH && !visited[i][j]) {
                        disconnectedCells.add(new int[]{i, j});
                    }
                }
            }
        }
    }

    /**
     * BFS (Breadth-First Search) - Parcours en largeur
     * Marque toutes les cases accessibles depuis un point de départ
     * 
     * @param startR Ligne de départ
     * @param startC Colonne de départ
     * @param visited Tableau à remplir avec les cases visitées
     */
    private static void bfs(int startR, int startC, boolean[][] visited) {
        // Réinitialise le tableau visited
        for (int i = 0; i < rows; i++) Arrays.fill(visited[i], false);

        // File pour le BFS
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startR, startC});  // Ajoute le point de départ
        visited[startR][startC] = true;         // Marque comme visité

        // Tant qu'il y a des cases à explorer
        while (!queue.isEmpty()) {
            int[] curr = queue.poll();  // Prend la prochaine case à explorer
            int r = curr[0], c = curr[1];
            
            // Définit les 4 voisins (haut, bas, gauche, droite)
            int[][] neighbors = {{r - 1, c}, {r + 1, c}, {r, c - 1}, {r, c + 1}};

            // Pour chaque voisin
            for (int[] n : neighbors) {
                int nr = n[0], nc = n[1];
                // Si le voisin est valide, est un chemin, et pas encore visité
                if (nr > 0 && nr < rows - 1 && nc > 0 && nc < cols - 1 &&
                    maze[nr][nc] == PATH && !visited[nr][nc]) {
                    visited[nr][nc] = true;  // Marque comme visité
                    queue.add(new int[]{nr, nc});  // Ajoute à la file pour explorer ses voisins
                }
            }
        }
    }

    /**
     * Creuse un chemin direct entre deux points
     * D'abord horizontalement, puis verticalement (forme en L)
     * 
     * @param from Point de départ [ligne, colonne]
     * @param to Point d'arrivée [ligne, colonne]
     */
    private static void carvePath(int[] from, int[] to) {
        int r = from[0];
        int c = from[1];

        // === CREUSE HORIZONTALEMENT ===
        // Continue jusqu'à atteindre la colonne cible
        while (c != to[1]) {
            maze[r][c] = PATH;  // Transforme la case en chemin
            c += (to[1] > c) ? 1 : -1;  // Avance vers la droite ou la gauche
        }
        
        // === CREUSE VERTICALEMENT ===
        // Continue jusqu'à atteindre la ligne cible
        while (r != to[0]) {
            maze[r][c] = PATH;  // Transforme la case en chemin
            r += (to[0] > r) ? 1 : -1;  // Avance vers le bas ou le haut
        }
    }

    /**
     * Affiche le labyrinthe de manière simple (sans le héros ni ennemis)
     * Utilisé pour la génération initiale
     */
    private static void printMaze() {
        for (int[] row : maze) {
            for (int cell : row) {
                // Affiche # pour les murs, espace pour les chemins
                System.out.print(cell == WALL ? "#" : " ");
            }
            System.out.println();
        }
    }

    /**
     * Affiche le labyrinthe avec tous les éléments du jeu
     * Héros (H), Ennemis (E), Murs (#), Chemins (espaces)
     */
    private static void printMazeWithHero() {
        System.out.println("\n=== LABYRINTHE ===");
        // Parcourt chaque ligne
        for (int[] row : maze) {
            // Parcourt chaque case de la ligne
            for (int cell : row) {
                // Affiche le symbole correspondant à la valeur de la case
                switch (cell) {
                    case WALL:
                        System.out.print("# ");  // Mur
                        break;
                    case PATH:
                        System.out.print("  ");  // Chemin vide
                        break;
                    case HERO:
                        System.out.print("H ");  // Héros
                        break;
                    case ENEMY:
                        System.out.print("E ");  // Ennemi
                        break;
                    default:
                        System.out.print("? ");  // Valeur inconnue (ne devrait pas arriver)
                }
            }
            System.out.println();  // Retour à la ligne après chaque ligne du labyrinthe
        }
    }

    /**
     * Vérifie si le déplacement du héros est bloqué
     * Utilisé avant de déplacer le héros pour vérifier la validité du mouvement
     * 
     * @param x Position actuelle du héros (colonne)
     * @param y Position actuelle du héros (ligne)
     * @param direction "haut", "bas", "gauche" ou "droite"
     * @param carte Matrice du terrain
     * @return true si le déplacement est bloqué, false sinon
     */
    public static boolean blocage(int x, int y, String direction, int[][] carte) {
        // Initialise les coordonnées suivantes avec les coordonnées actuelles
        int xSuiv = x;
        int ySuiv = y;

        // === CALCULE LA CASE SUIVANTE SELON LA DIRECTION ===
        switch (direction.toLowerCase()) {
            case "haut":
                ySuiv = y - 1;  // Monter = diminuer Y
                break;
            case "bas":
                ySuiv = y + 1;  // Descendre = augmenter Y
                break;
            case "gauche":
                xSuiv = x - 1;  // Gauche = diminuer X
                break;
            case "droite":
                xSuiv = x + 1;  // Droite = augmenter X
                break;
            default:
                // Direction invalide = considéré comme bloqué
                return true;
        }

        // === VÉRIFIE SI LA CASE SUIVANTE EST HORS LIMITES ===
        if (ySuiv < 0 || ySuiv >= carte.length || xSuiv < 0 || xSuiv >= carte[0].length) {
            return true;  // En dehors de la carte = bloqué
        }

        // === VÉRIFIE SI LA CASE SUIVANTE EST UN MUR ===
        if (carte[ySuiv][xSuiv] == WALL) {
            return true;  // Mur = bloqué
        }

        // Si on arrive ici, le déplacement est possible
        return false;
    }

    /**
     * Gère le déplacement du héros
     * Vérifie la validité, efface l'ancienne position, place à la nouvelle
     * 
     * @param herosX Position actuelle X du héros (colonne)
     * @param herosY Position actuelle Y du héros (ligne)
     * @param direction Direction du mouvement
     * @param carte Matrice du terrain
     * @return Nouvelles coordonnées [x, y] du héros
     */
    public static int[] mouvement(int herosX, int herosY, String direction, int[][] carte) {
        // Initialise les nouvelles positions avec les positions actuelles
        int newX = herosX;
        int newY = herosY;
        
        // === VÉRIFIE SI LE MOUVEMENT EST POSSIBLE ===
        if (!blocage(herosX, herosY, direction, carte)) {
            // Le déplacement est autorisé
            
            // Efface l'ancienne position du héros
            carte[herosY][herosX] = PATH;
            
            // === CALCULE LA NOUVELLE POSITION ===
            switch (direction.toLowerCase()) {
                case "haut":
                    newY = herosY - 1;  // Monte d'une case
                    break;
                case "bas":
                    newY = herosY + 1;  // Descend d'une case
                    break;
                case "gauche":
                    newX = herosX - 1;  // Va à gauche d'une case
                    break;
                case "droite":
                    newX = herosX + 1;  // Va à droite d'une case
                    break;
            }
            
            // Place le héros à sa nouvelle position
            carte[newY][newX] = HERO;
        } else {
            // Le mouvement est bloqué, affiche un message
            System.out.println("Mouvement impossible! Il y a un obstacle.");
        }
        
        // Retourne les nouvelles coordonnées (changées ou pas)
        return new int[]{newX, newY};
    }

    /**
     * Crée un ennemi à une position donnée
     * 
     * @param x Position X (colonne) de l'ennemi
     * @param y Position Y (ligne) de l'ennemi
     * @param carte Matrice du terrain
     * @return Coordonnées [x, y] de l'ennemi créé
     */
    public static int[] creerEnnemi(int x, int y, int[][] carte) {
        carte[y][x] = ENEMY;  // Place l'ennemi sur la carte
        return new int[]{x, y};  // Retourne sa position
    }

    /**
     * Déplace l'ennemi vers le héros
     * L'ennemi se rapproche en priorité sur l'axe avec la plus grande distance
     * 
     * @param ex Position X actuelle de l'ennemi
     * @param ey Position Y actuelle de l'ennemi
     * @param hx Position X du héros
     * @param hy Position Y du héros
     * @param carte Matrice du terrain
     * @return Nouvelles coordonnées [x, y] de l'ennemi
     */
    public static int[] deplacementEnnemi(int ex, int ey, int hx, int hy, int[][] carte) {
        // Initialise les nouvelles positions
        int newEx = ex;
        int newEy = ey;

        // Variable pour stocker la direction choisie
        String direction = null;

        // === DÉTERMINE LA DIRECTION VERS LE HÉROS ===
        // Compare les distances horizontale et verticale
        if (Math.abs(hx - ex) > Math.abs(hy - ey)) {
            // Distance horizontale plus grande → se rapprocher horizontalement
            if (hx < ex) direction = "gauche";  // Héros à gauche
            else if (hx > ex) direction = "droite";  // Héros à droite
        } else {
            // Distance verticale plus grande → se rapprocher verticalement
            if (hy < ey) direction = "haut";  // Héros en haut
            else if (hy > ey) direction = "bas";  // Héros en bas
        }

        // === EFFECTUE LE DÉPLACEMENT SI POSSIBLE ===
        if (direction != null && !blocage(ex, ey, direction, carte)) {
            // Le déplacement est possible
            
            carte[ey][ex] = PATH;  // Efface l'ancienne position
            
            // Calcule la nouvelle position selon la direction
            switch (direction) {
                case "haut":    newEy--; break;
                case "bas":     newEy++; break;
                case "gauche":  newEx--; break;
                case "droite":  newEx++; break;
            }
            
            carte[newEy][newEx] = ENEMY;  // Place l'ennemi à sa nouvelle position
        }
        // Si bloqué, l'ennemi reste sur place

        return new int[]{newEx, newEy};  // Retourne les nouvelles coordonnées
    }

    /**
     * Vérifie si le héros est mort (même case qu'un ennemi)
     * 
     * @param herosX Position X du héros
     * @param herosY Position Y du héros
     * @param ennemiX Position X de l'ennemi
     * @param ennemiY Position Y de l'ennemi
     * @return true si le héros est mort, false sinon
     */
    public static boolean verifierMort(int herosX, int herosY, int ennemiX, int ennemiY) {
        // Compare les positions du héros et de l'ennemi
        if (herosX == ennemiX && herosY == ennemiY) {
            // Même position = mort du héros
            System.out.println("\n==============================");
            System.out.println("💀💀💀  VOUS ÊTES MORT  💀💀💀");
            System.out.println("==============================\n");
            return true;
        }
        // Positions différentes = héros toujours vivant
        return false;
    }
}
