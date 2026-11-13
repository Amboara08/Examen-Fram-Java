# Police et Voleur — LAN + IA Minimax

Jeu séparé en HTML, CSS et JavaScript, avec un serveur Node.js + Socket.IO pour jouer sur un réseau local ou affronter une IA Minimax.

## Structure

```text
police-voleur-lan/
├── public/
│   ├── index.html
│   ├── style.css
│   ├── game.js
│   ├── pwa.js
│   ├── manifest.webmanifest
│   ├── sw.js
│   └── icons/
│       ├── icon-192.png
│       ├── icon-512.png
│       └── icon-maskable-512.png
├── package.json
├── server.js
└── README.md
```

## Installation

1. Installe Node.js sur l'ordinateur qui hébergera le jeu.
2. Ouvre CMD dans le dossier `police-voleur-lan`.
3. Installe les dépendances : `npm install`.
4. Démarre le serveur : `npm start`.
5. Ouvre `http://localhost:3000` sur l'ordinateur serveur.
6. Pour le LAN, ouvre sur les autres appareils l'adresse IP locale affichée par le serveur (même Wi-Fi/LAN).

## PWA (installation sur l'écran d'accueil)

- Le projet inclut un manifeste PWA, des icônes, un Service Worker et un bouton d'installation.
- Sur `localhost`, la PWA peut être installée depuis un navigateur compatible (Chrome/Edge).
- Pour installer l'application sur un autre appareil du réseau local, l'adresse doit être servie en **HTTPS**. Une adresse LAN en `http://192.168.x.x:3000` ne permet généralement pas au navigateur d'activer le Service Worker, car ce n'est pas un contexte sécurisé.
- Une fois le site ouvert en HTTPS, utilise le bouton **Installer l’application** ou le menu du navigateur.
- Sur iPhone/iPad, utilise Partager → « Sur l’écran d’accueil ».
- Le Service Worker met en cache l'interface et les fichiers statiques. Une connexion au serveur reste nécessaire pour jouer en LAN ou contre l'IA.

## Fin de partie

Lorsqu'un joueur gagne, une fenêtre de fin de partie s'affiche avec le résultat, le vainqueur et la raison de la victoire. Le bouton « Rejouer » permet de relancer une partie.

## Modes de jeu

- **Créer une salle LAN** : le créateur joue le voleur et le second joueur contrôle les trois policiers.
- **Jouer le voleur contre l'IA policière** : le joueur contrôle le voleur, l'IA contrôle les trois policiers.
- **Jouer les policiers contre l'IA voleuse** : le joueur contrôle les trois policiers, l'IA contrôle le voleur et joue automatiquement dès le début.

## Intelligence artificielle

- Minimax avec élagage Alpha-Bêta.
- Approfondissement itératif jusqu'à 10 demi-coups, avec un budget maximal de 90 000 nœuds par décision.
- Tri des coups pour explorer en premier les déplacements les plus prometteurs.
- Table de transposition pour réutiliser les positions déjà évaluées.
- Évaluation stratégique : distance du voleur au centre, distance des policiers, mobilité du voleur et accès libres au centre.
- Le score est évalué du point de vue du voleur : positif pour le voleur, négatif pour les policiers.

## Règles

- Le voleur gagne immédiatement lorsqu'il atteint le sommet central.
- Les policiers gagnent lorsqu'ils bloquent tous les déplacements du voleur.
- Le voleur gagne si aucun policier ne peut se déplacer.
- Les quatre pions sont placés aléatoirement au début de chaque partie, le centre restant libre.

## À savoir

- Le serveur doit rester démarré pendant toute la partie.
- Sur Windows, autorise Node.js sur les réseaux privés si le pare-feu le demande.
- Les salles sont conservées en mémoire et disparaissent lorsque le serveur est arrêté.
- `npm install` nécessite une connexion Internet la première fois pour télécharger Express et Socket.IO.
