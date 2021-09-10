var socket = null;
var client = null;
var currentURL = window.location.href;
var gameId = currentURL.substring(currentURL.indexOf("newGame/") + 8)
//var currentUserId;
//var activeUsersList = null;
//var activeGamesList = null;
//var chatText = "";
//var isPlayerJoinedGame = false;
//var isPlayerCreatedGame = false;
//document.getElementById("chatInput").value = "";
//let authenticatedUserTag = document.getElementById("currentUsername");
//let authenticatedUserName = authenticatedUserTag.innerHTML;

window.onload = function load(){
    resetTimer();
    idleLogout();
    connect();
}
window.onbeforeunload = function unload(){
    disconnect();
    window.location = '/logout';
}

function connect(){

    socket = new SockJS("http://localhost:8080/websocket");

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
    console.log("sending x: " + x + " y: " + y);
    client.send('/ws/shipPlacement', {}, JSON.stringify({
                        "fieldStatus": "SHIP_ALIVE",
                        "coords": "" + x + y
                        }));
}

function sendPlacementInfoToOpponent(shipName, whichOfAKind, isAllShipsPlaced){
    console.log("send placement info, gameId: " + gameId + " shipName: " + shipName + " whichofakind: " + whichOfAKind + "isallplaced: " + isAllShipsPlaced);
    client.send('/ws/sendShipsPlacementInfo/' + gameId, {}, JSON.stringify({
                            "shipName": shipName,
                            "whichOfAKind": whichOfAKind,
                            "isAllShipsPlaced": isAllShipsPlaced
                            }));
}