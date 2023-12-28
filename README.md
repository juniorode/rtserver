# Real-time server

## SSE

### Paramétrage

Dans le fichier application.properties, ajouter la ligne suivante:

```
sse.enabled=true
```
### Routes

#### Souscription aux événements d'une ressource

```shell
curl -v localhost:8080/sse/subscribe/<ID_OF_THE_RESOURCE>
```

#### Publication d'un événement

```shell
curl -v localhost:8080/sse/publish/<ID_OF_THE_RESOURCE> -H 'content-type: applicatication/json'  -d '<JSON_DATA>'
```

## WebSockets

### Paramétrage

Dans le fichier application.properties, ajouter la ligne suivante:

```
ws.enabled=true
ws.port=8081
ws.host=localhost
```

Si le port 8081 est déjà utilisé, changer `ws.port` dans application.properties et dans le fichier `src/main/resources/static/ws.html`.

### Routes

Une fois lancé, ouvrez 2 onglets A et B dans votre navigateur à l'adresse suivante : `http://localhost:8080/ws.html?resource=<ce_que_vous_voulez>`
et un onglet à l'adresse suivante: `http://localhost:8080/ws.html?resource=<autre_chose>`.

Constatez que les onglets A et B peuvent échanger des informations entre eux, mais pas avec le troisième onglet.