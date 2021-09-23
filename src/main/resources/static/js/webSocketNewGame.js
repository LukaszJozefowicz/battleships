var socket = null;
var client = null;
var currentURL = window.location.href;
var gameId = currentURL.substring(currentURL.indexOf("newGame/") + 8)
var chatText = "";
var textOutput = document.getElementById("chatOutput");
let authenticatedUserTag = document.getElementById("currentUsername");
let authenticatedUserName = authenticatedUserTag.innerHTML;
//some global vars used here are declared in boards.html script tag

window.onload = () => {
    resetTimer();
    idleLogout();
    connect();
    document.getElementById("chatInput").value = "";
    document.getElementById("chatInput").focus();
    document.getElementById("chatOutput").value = "";
    document.getElementById("backToMenu").style.display = "none";
    document.getElementById("backToLobby").style.display = "none";
}
window.onbeforeunload = () => {
    disconnect();
}

function connect(){

    socket = new SockJS("http://localhost:8080/websocket"); //https://battleships-spring-boot.herokuapp.com/websocket

    client = Stomp.over(socket);

    client.connect({}, frame => {

            var payloadBody = null;
            client.subscribe("/user/queue/sendPlacementInfo/" + gameId, payload => {

                payloadBody = JSON.parse(payload.body);

                if(payloadBody.shipName !== undefined
                    && payloadBody.whichOfAKind !== undefined
                    && payloadBody.isAllShipsPlaced !== undefined){

                        let oppPlacementInfo = document.getElementById("opponentShipPlacingInfo");
                        if(payloadBody.isAllShipsPlaced === "false"){
                            oppPlacementInfo.innerHTML = "Opponent is placing " + payloadBody.shipName + payloadBody.whichOfAKind;
                        } else if(payloadBody.isAllShipsPlaced === "true"){
                            oppPlacementInfo.innerHTML = "Opponent has finished placing his ships.";
                            if(areYouReady){
                                startShootingPhase();
                            }
                            isOpponentReady = true;
                        }
                }

            });

            client.subscribe("/user/queue/placeShip", payload => {
            });

            client.subscribe("/user/queue/resetBoard", payload => {
            });

            client.subscribe("/sendInfoMessage/" + gameId, payload => {
                     payloadBody = JSON.parse(payload.body);

                    switch(payloadBody.messageType){
                        case "gameChatMsg":
                            receiveChatMessage(payloadBody);
                            break;
                        case "leaveGame":
                            playerLeftInfo(payloadBody);
                            break;
                    }
            });

            client.subscribe("/newGame/" + gameId + "/shoot", payload => {
                    payloadBody = JSON.parse(payload.body);

                    shoot(payloadBody);

                    let row = parseInt(String.fromCharCode(parseInt(payloadBody.coords.substring(0,1)) + 48)) + 1;  // 1-10
                    let col = String.fromCharCode(parseInt(payloadBody.coords.substring(1,2)) + 65);                // A-J
                    let shotResultInfo = payloadBody.currentPlayer + " shot: " + col + row + "\n";

                    switch(payloadBody.shotResult){
                            case "SHIP_HIT":
                                shotResultInfo += payloadBody.opponentPlayer + "'s ship is hit!";
                                break;
                            case "SHIP_SUNK":
                                shotResultInfo += payloadBody.opponentPlayer + "'s ship is hit and sunk!";
                                break;
                            case "MISS":
                                shotResultInfo += payloadBody.currentPlayer + " missed!";
                                break;
                    }
                    if(payloadBody.allShipsSunk === false){
                        appendCurrentTurnInfo({username: payloadBody.opponentPlayer,        //th:inline in boards.html
                        message: shotResultInfo + "\n\nCurrent turn: "});
                    } else if(payloadBody.allShipsSunk === true){
                        textOutput.value += "All " + payloadBody.opponentPlayer + "'s ships are sunk\n"
                                       + payloadBody.currentPlayer + " won!!\n";
                        textOutput.scrollTop = textOutput.scrollHeight;
                        document.getElementById("backToLobby").style.display = "flex";
                    }
            });

        });

}

function disconnect(){
    if(client != null) {

        client.send('/ws/leaveGame/' + gameId, {}, JSON.stringify({
                messageType: "leaveGame",
                username: authenticatedUserName,
                message: "msg"}));

        client.disconnect(payload => {
            client.disconnect();
        });
    }
}

function sendPlaceShipTile(x, y){

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

    client.send('/ws/sendShipsPlacementInfo/' + gameId, {}, JSON.stringify({
                            "shipName": shipName,
                            "whichOfAKind": whichOfAKind,
                            "isAllShipsPlaced": isAllShipsPlaced
                            }));
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

function sendShotInfo(x, y){
    client.send('/ws/shoot/' + gameId, {}, JSON.stringify({

                        currentPlayer: "currentPlayer",
                        opponentPlayer: "opponentPlayer",
                        shotResult: "shotResult",
                        coords: "" + x + y
                        }));
}