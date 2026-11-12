# Police et Voleur — Multijoueur LAN

Version séparée en HTML, CSS et JavaScript, avec un serveur Node.js + Socket.IO pour jouer à deux appareils sur le même réseau local.

## Structure

```text
police-voleur-lan/
├── public/
│   ├── index.html
│   ├── style.css
│   └── game.js
├── package.json
├── server.js
└── README.md
```

## Installation

1. Installe Node.js sur l'ordinateur qui hébergera la partie.
2. Ouvre CMD dans le dossier `police-voleur-lan`.
3. Installe les dépendances :

   ```bash
   npm install
   ```

4. Démarre le serveur :

   ```bash
   npm start
   ```

5. Le terminal affiche une ou plusieurs adresses IP locales, par exemple `http://192.168.1.15:3000`.
6. Sur chaque appareil connecté au même Wi-Fi, ouvre cette adresse dans le navigateur.
7. Le premier joueur clique sur **Créer une salle** et partage le code.
8. Le deuxième joueur saisit ce code puis clique sur **Rejoindre la partie**.

## Rôles

- Le joueur qui crée la salle contrôle le voleur vert.
- Le joueur qui rejoint la salle contrôle les trois policiers bleus.
- Le serveur vérifie les déplacements et synchronise le plateau chez les deux joueurs.

## À savoir

- Le serveur doit rester démarré pendant toute la partie.
- Les appareils doivent être sur le même réseau local (même Wi-Fi ou réseau LAN).
- Sur Windows, autorise Node.js sur les réseaux privés si le pare-feu le demande.
- Utilise l'adresse IP affichée par le serveur sur les autres appareils, pas `localhost`.
- Le jeu ne nécessite pas de service cloud ni d'hébergement public. `npm install` nécessite toutefois une connexion Internet la première fois pour télécharger les dépendances.
- Les salles sont conservées en mémoire : elles disparaissent lorsque le serveur est arrêté.
