# TODO

### Fonctionnalités essentielles à implémenter ###

* Améliorer l'IA de combat
* Optimiser le pathfinder (changements de direction)
* Implémenter la lecture des valeurs négatives dans la classe ByteArray (bug du readVarShort...)
* Optimiser l'envoi des paquets (avec création de variables locales dans les frames) -> très lourd à faire
* Faire une méthode "useInteractive"
* ~~Faire une "dialogFrame" (traitement des messages concernant les échanges et les dialogues avec les PNJ)~~
* Gérer les combats en groupe
* Ajouter des critères pour le choix des groupes de monstres à combattre
* Améliorer les échanges et les dialogues avec les PNJ (suppression de "sleeps" peu propres)

### Bugs à résoudre ###

* ~~**GameMapNoMovementMessage (probablement à cause d'une cellule courante fausse)**~~
* **"array index out of bounds" lors de la lecture d'un paquet**
* ~~ChangeMapMessage qui ne s'envoie pas~~
* "Connection reset" pendant la création du serveur d'émulation
* ~~Déconnexion intempestive pour une raison inconnue (en combat ou en mouvement)~~
* "None possible path found" sur les maps séparées par un "mur" d'obstacles
* Problème d'accès concurrents dans la classe RoleplayContext (et peut-être aussi dans FightContext)

### Échanges ###

* ~~Vérifier le lanceur de l'échange (côté mule)~~
* ~~Vérifier si l'échange a été un succès ou pas du côté combattant~~
* ~~Ajouter les kamas lors de l'échange (fighter -> mule)~~
* Si la demande d'échange a échoué (cible occupée ou pas encore chargée complètement sur la map), la relancer lorsque la cible sera disponible

### Interface graphique ###

* "Scroll down" automatique qui s'arrête
* Ajouter la couleur dans les logs des CharacterFrames
* Padding à ajouter autour des logs des CharacterFrames

### Facultatif ###

* Envoyer le message "GameContextReadyMessage"
* Éviter de repartir sur l'aire de combat lorsqu'on est full pods
* Améliorer la réflexion