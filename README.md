# TODO

### Fonctionnalités/améliorations à implémenter ###

* ~~Faire une méthode "useInteractive"~~
* ~~Faire une "dialogFrame" (traitement des messages concernant les échanges et les dialogues avec les PNJ)~~
* ~~Gérer les combats en groupe~~
* ~~Ajouter des critères pour le choix des groupes de monstres à combattre~~
* ~~Mettre à jour l'aire de combat à chaque level up (que ce soit seul ou en groupe)~~
* ~~**Améliorer les échanges et les dialogues avec les PNJ (suppression de "sleeps" peu propres)**~~
* ~~Supprimer à terme les boucles infinies lors de l'attente d'états~~
* ~~**Création automatique de personnage dans les comptes vides**~~
* **Implémenter la vente de ressources**
* Déserialiser automatiquement un message reçu
* Implémenter le personnage "vendeur"
* Créer une API pour les "squads"

### Bugs à résoudre ###

* ~~**GameMapNoMovementMessage (probablement à cause d'une cellule courante fausse)**~~
* ~~**"array index out of bounds" lors de la lecture d'un paquet**~~
* ~~ChangeMapMessage qui ne s'envoie pas~~
* ~~"Connection reset" pendant la création du serveur d'émulation~~
* ~~Déconnexion intempestive pour une raison inconnue (en combat ou en mouvement)~~
* ~~"None possible path found" sur les maps séparées par un "mur" d'obstacles~~
* ~~Problème d'accès concurrents dans la classe RoleplayContext (et peut-être aussi dans FightContext)~~
* ~~Cellules dans le coin des maps ne permettant de changer de map que d'un seul côté (donc problème quand on veut aller vers l'autre côté)~~
* ~~Exceptions en double (FatalError)~~
* ~~**Launcher qui finit par s'écrouler au bout d'un certain temps**~~
* ~~Mode absent qui ne fonctionne pas pour le capitaine (probablement à cause du fait qu'il est chef de groupe)~~
* ~~Chemin de maps calculé trop souvent (il prend du temps donc à optimiser)~~
* ~~**Au bout d'un moment, un message n'est pas envoyé (ou n'est pas reçu par le serveur), de ce fait, le bot est kické**~~
* ~~**Latence au lancement d'une CharacterFrame (et même des fois, plantage de la CharacterFrame)**~~
* ~~**Erreur de frame nulle lors de certains lancements du launcher**~~
* **Pertes de connexion avec le serveur ("connection reset")**

### Échanges ###

* ~~Vérifier le lanceur de l'échange (côté mule)~~
* ~~Vérifier si l'échange a été un succès ou pas du côté combattant~~
* ~~Ajouter les kamas lors de l'échange (fighter -> mule)~~
* ~~Si la demande d'échange a échoué (cible occupée ou pas encore chargée complètement sur la map), la relancer lorsque la cible sera disponible~~

### Groupes de combat ###

* ~~Après reconnexion lors d'un combat, l'invitation de groupe émise par le capitaine n'est pas reçue~~
* ~~Gérer le cas où un soldat se déconnecte (reconnexion ou suppression du vecteur de soldats)~~
* ~~Déconnecter un combattant lorsqu'il a atteint la limite de 200 combats par jour (traitement du "TextInformationMessage" reçu)~~
* ~~La mule a du mal à enchaîner les échanges avec plusieurs combattants à la suite~~
* ~~Améliorer l'intégration d'un soldat au groupe (pas bien fait et actuellement trop long)~~
* Optimiser la régénération de la vie (beaucoup de pertes de temps actuellement)
* Promouvoir un soldat lorsque le capitaine du groupe est déconnecté

### Interface graphique (facultatif) ###

* ~~Améliorer le rafraichissement des informations dans les CharacterFrames (trop lourd actuellement)~~
* "Scroll down" automatique qui s'arrête
* Ajouter la couleur dans les logs des CharacterFrames
* Padding à ajouter autour des logs des CharacterFrames
* Ajouter l'encodage "UTF-8" dans les CharacterFrames (pour afficher les accents)

### Facultatif ###

* ~~Envoyer le message "GameContextReadyMessage"~~
* ~~Éviter de repartir sur l'aire de combat lorsqu'on est full pods~~
* ~~Gérer les "TextInformationMessages" qui peuvent donner des informations utiles pour le debuggage~~
* Optimiser le pathfinder (changements de direction)
* Améliorer l'utilisation des "interactives" (récupérer le résultat et se rendre à la cellule adjacente à l'"interactive")
* Traduire la classe "ParamsDecoder" pour un meilleur affichage des "TextInformationMessages" et des "SystemMessageDisplayMessages"
* Améliorer l'IA de combat
* Ajouter, lors de la recherche d'un chemin vers une map distante, une cellule cible pour la map cible (ça évitera de se retrouver de l'autre côté d'une muraille par rapport au capitaine par exemple)