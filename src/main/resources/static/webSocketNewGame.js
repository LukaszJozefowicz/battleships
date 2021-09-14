var socket = null;
var client = null;
var currentURL = window.location.href;
var gameId = currentURL.substring(currentURL.indexOf("newGame/") + 8)

window.onload = () => {
    resetTimer();
    idleLogout();
    connect();
    console.log("ready state: " + socket.readyState);
}
window.onbeforeunload = () => {
    disconnect();
    window.location = '/logout';
}

function connect(){

    socket = new SockJS("http://localhost:8080/websocket"); //https://battleships-spring-boot.herokuapp.com/websocket

    client = Stomp.over(socket);

    client.connect({}, frame => {

            var payloadBody = null;
            client.subscribe("/user/queue/sendPlacementInfo/" + gameId, payload => {

                payloadBody = JSON.parse(payload.body);
                console.log("INVOKED SUBSCRIBE PLACEMENT INFO!!!!!!!!!!!!!");
                console.log("payload body " + payload.body);
                console.log("json parsed " + payloadBody)
                if(payloadBody.shipName !== undefined
                    && payloadBody.whichOfAKind !== undefined
                    && payloadBody.isAllShipsPlaced !== undefined){

                        let oppPlacementInfo = document.getElementById("opponentShipPlacingInfo");
                        if(payloadBody.isAllShipsPlaced === "false"){
                            oppPlacementInfo.innerHTML = "Opponent is placing " + payloadBody.shipName + payloadBody.whichOfAKind;
                        } else
                        if(payloadBody.isAllShipsPlaced === "true"){
                            oppPlacementInfo.innerHTML = "Opponent has finished placing his ships.";
                        }
                }

            });

            client.subscribe("/user/queue/placeShip", payload => {
                   payloadBody = JSON.parse(payload.body);
            });

            client.subscribe("/user/queue/resetBoard", payload => {
                               payloadBody = JSON.parse(payload.body);
                               console.log("payload body " + payload.body);
                               console.log("json parsed " + payloadBody)
            });
        });

}

function disconnect(){
    if(client != null) {
        /*isPlayerJoinedGame = false;
        isPlayerCreatedGame = false;
        let authenticatedUserTag = document.getElementById("currentUsername");
        let authenticatedUserName = authenticatedUserTag.innerHTML;
        activeUsersList = activeUsersList.filter(e => e !== authenticatedUserName);

        client.send('/ws/userLeft', {}, JSON.stringify(activeUsersList));
        client.send('/ws/sendToChat', {}, JSON.stringify({username: authenticatedUserName, message: " left the lobby\n"}));
        //client.send('/ws/deleteGame', {}, JSON.stringify(activeGamesList));*/

        client.disconnect(payload => {
            client.disconnect();
        });
    }
}

function placeShipTile(x, y){
    console.log("sending type: " + shipsToPlace[whichShip].type + " length: " + shipsToPlace[whichShip].length + " x: " + x + " y: " + y);
    console.log("ready state: " + socket.readyState);
    client.send('/ws/shipPlacement', {}, JSON.stringify({
                        "fieldStatus": "SHIP_ALIVE",
                        "type": shipsToPlace[whichShip].type,
                        "length" : shipsToPlace[whichShip].length,
                        "coords": "" + x + y
                        }));
}

function sendResetBoard(){
    client.send('/ws/resetBoard', {}, JSON.stringify({"username":"username", "message": "resetBoard"}));
}

function sendPlacementInfoToOpponent(shipName, whichOfAKind, isAllShipsPlaced){
    console.log("send placement info, gameId: " + gameId + " shipName: " + shipName + " whichofakind: " + whichOfAKind + "isallplaced: " + isAllShipsPlaced);
    client.send('/ws/sendShipsPlacementInfo/' + gameId, {}, JSON.stringify({
                            "shipName": shipName,
                            "whichOfAKind": whichOfAKind,
                            "isAllShipsPlaced": isAllShipsPlaced
                            }));
}