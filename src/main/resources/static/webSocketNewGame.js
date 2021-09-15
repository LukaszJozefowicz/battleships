var socket = null;
var client = null;
var currentURL = window.location.href;
var gameId = currentURL.substring(currentURL.indexOf("newGame/") + 8)
var chatText = "";
document.getElementById("chatInput").value = "";
var textOutput = document.getElementById("chatOutput");
let authenticatedUserTag = document.getElementById("currentUsername");
let authenticatedUserName = authenticatedUserTag.innerHTML;

window.onload = () => {
    resetTimer();
    idleLogout();
    connect();
    document.getElementById("chatOutput").value = "";
    document.getElementById("chatInput").focus();
//    console.log("ready state: " + socket.readyState);
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
                            if(areYouReady){
                                startShootingPhase();
                            }
                            isOpponentReady = true;
                        }
                }

            });

            client.subscribe("/user/queue/placeShip", payload => {
//                   payloadBody = JSON.parse(payload.body);
            });

            client.subscribe("/user/queue/resetBoard", payload => {
//                               payloadBody = JSON.parse(payload.body);
//                               console.log("payload body " + payload.body);
//                               console.log("json parsed " + payloadBody)
            });

            client.subscribe("/sendInfoMessage/" + gameId, payload => {
                     payloadBody = JSON.parse(payload.body);
                     if(payloadBody.messageType === "gameChatMsg"){
                        receiveChatMessage(payloadBody);
                     } else if(payloadBody.messageType === "currentTurnInfo"){
                        appendCurrentTurnInfo(payloadBody);
                     }
            });

        });

}

function disconnect(){
    if(client != null) {

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
    client.send('/ws/resetBoard', {}, JSON.stringify({"messageType": "resetBoard", "username":"username", "message": "resetBoard"}));
}

function sendPlacementInfoToOpponent(shipName, whichOfAKind, isAllShipsPlaced){
//    console.log("send placement info, gameId: " + gameId + " shipName: " + shipName + " whichofakind: " + whichOfAKind + "isallplaced: " + isAllShipsPlaced);
    client.send('/ws/sendShipsPlacementInfo/' + gameId, {}, JSON.stringify({
                            "shipName": shipName,
                            "whichOfAKind": whichOfAKind,
                            "isAllShipsPlaced": isAllShipsPlaced
                            }));
}

function receiveChatMessage(payloadBody){

    textOutput.value += payloadBody.username + ":" + payloadBody.message + "\n";
    textOutput.scrollTop = textOutput.scrollHeight;
}

function appendCurrentTurnInfo(payloadBody){
    textOutput.value += payloadBody.message + payloadBody.username + "\n";
    textOutput.scrollTop = textOutput.scrollHeight;
}

function sendCurrentTurnInfo(){

    client.send('/ws/sendCurrentTurn/' + gameId, {}, JSON.stringify({ messageType: "currentTurnInfo",
                                                            username: "username",
                                                            message: "Current turn: "}));
}

$('#chatInput').on('keypress', function(e){
    chatText = this.value;
    console.log("in jquery");

        if(event.which === 13 || event.keyCode === 13){

            client.send('/ws/gameChat/' + gameId, {}, JSON.stringify({
                                                    messageType: "gameChatMsg",
                                                    username: authenticatedUserName,
                                                    message: chatText}));
            e.preventDefault();
            this.value = "";
            chatText = "";
            return false;
        }
});