# TPE — version 2

Cinq projets Spring Boot avec une présentation volontairement simple : menu horizontal, tableaux, formulaires et boutons standards. Les fonctions métier obligatoires utilisent le socle de la première version ; cette version allège les écrans et le démarrage plutôt que de réécrire les mêmes règles de validation.

| Projet | Adresse | Première utilisation |
|---|---|---|
| `01-tournois` | http://localhost:8181/ | Créer des équipes, un tournoi et ses inscriptions |
| `02-textile` | http://localhost:8182/ | Créer un client, un produit, une commande et ses lignes |
| `03-taxis` | http://localhost:8183/ | Créer un client, un véhicule, un chauffeur et une course |
| `04-parking` | http://localhost:8184/ | Créer un usager, son véhicule et des places |
| `05-livraison` | http://localhost:8185/ | Créer un client, un livreur, une commande et ses colis |

## Ce qui est simplifié

- Présentation blanche, bordures simples, aucune bibliothèque frontend ni JavaScript ajouté.
- Accueil limité à un tableau de chiffres ; plan du parking sous forme de liens simples.
- Formulaires sur une colonne, fiches et étapes affichées comme du texte et des tableaux.
- Listes initialement vides : aucune donnée de démonstration injectée automatiquement.
- Pas de copies des PDF, de leurs analyses, de générateurs ou de captures de la première version.

## Ce qui reste nécessaire

CRUD, REST et Thymeleaf, DTO validés, services, repositories, erreurs centralisées et quatre rôles. Les recherches, filtres, tris, pages, historiques et paramètres métier sont conservés.

Les règles principales restent disponibles : génération et résultats des tournois, production et contrôle qualité textile, attribution et fin des courses, réservation et facturation du parking, attribution et preuve de livraison. Les restrictions de statut, de quantité et de disponibilité restent actives.

Le périmètre se fonde sur `../EXIGENCES.md`, conservé après la suppression des PDF.

## Lancement

Java 17 minimum. Double-cliquer sur `lancer.cmd` dans le projet choisi. Les JAR se trouvent dans `target` après compilation. `compiler.cmd` compile et teste un projet ; `compiler-tout.cmd` compile et teste les cinq.

Les lanceurs utilisent le cache Maven du dossier TPE pour éviter de télécharger les dépendances une deuxième fois. Chaque POM fonctionne également avec Maven standard depuis son propre dossier. Aucun projet ne dépend des sources ou des JAR de la première version.

Les ports 8181 à 8185 permettent d'utiliser les deux versions simultanément. Chaque projet crée sa propre base H2 dans son dossier `data` ; les données de la première version ne sont pas copiées.

Comptes et mots de passe : `ACCES_PROJETS.txt`.

## Eclipse

1. File → Import → Maven → Existing Maven Projects.
2. Choisir le dossier `version-2`, puis les cinq applications.
3. Choisir un JDK 17 ou supérieur et effectuer Maven → Update Project.
4. Lancer la configuration `01-tournois-v2.launch`, `02-textile-v2.launch`, etc., ou la classe `Application` du projet choisi.

Les noms Maven et Eclipse portent le suffixe `-v2` pour éviter les collisions avec la première version. Les configurations fixent le dossier de travail à la racine de chaque application. L'exécution dans l'interface Eclipse reste à vérifier sur l'installation utilisée.

Les cinq JAR et les 22 tests d'intégration ont été vérifiés. Résultats : `VERIFICATION.txt`.
