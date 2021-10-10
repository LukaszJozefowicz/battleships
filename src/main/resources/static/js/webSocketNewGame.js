/* vars from th:inline script tag in boards.html

        var shipsToPlace = JSON.parse([[${shipsToPlace}]]);
        var opponentName = [[${opponentName}]];
        var isGameVsPC = opponentName.includes("BotEasy") || opponentName.includes("BotNormal") || opponentName.includes("BotHard");

*/

var socket = null;
var client = null;
var isGameVsPcEnded = false;
var chatText = "";

const currentURL = window.location.href;
const gameId = currentURL.substring(currentURL.indexOf("/newGame/") + 9);
const authenticatedUserName = document.getElementById("currentUsername").innerHTML;

window.onload = () => {
    resetTimer();
    idleLogout();
    connect();
    document.getElementById("chatInput").value = "";
    document.getElementById("chatOutput").value = "";
    document.getElementById("chatInput").focus();
    document.getElementById("backToMenu").style.display = "none";
    document.getElementById("backToLobby").style.display = "none";
    if(isGameVsPC){
        GameVsPcUtils.setComputerPlayerShips();
        GameVsPcUtils.setClickActionComputerShoot();
    }

}
window.onbeforeunload = () => {
    disconnect();
}

function connect(){

    socket = new SockJS("http://localhost:8080/websocket"); //https://battleships-spring-boot.herokuapp.com/websocket

    client = Stomp.over(socket);

    client.connect({}, frame => {

            client.subscribe("/user/queue/sendPlacementInfo/" + gameId, payload => {

                var payloadBody = JSON.parse(payload.body);

                if(payloadBody.shipName !== undefined
                    && payloadBody.whichOfAKind !== undefined
                    && payloadBody.isAllShipsPlaced !== undefined){

                        ShipsSetupUtils.setOpponentPlacementInfo(payloadBody);

                }

            });

            client.subscribe("/user/queue/placeShip/" + gameId, payload => {
            });

            client.subscribe("/user/queue/resetBoard/" + gameId, payload => {
            });

            client.subscribe("/sendInfoMessage/" + gameId, payload => {
                     payloadBody = JSON.parse(payload.body);

                    switch(payloadBody.messageType){
                        case "gameChatMsg":
                            TextOutputUtils.receiveChatMessage(payloadBody);
                            break;
                        case "leaveGame":
                            TextOutputUtils.playerLeftInfo(payloadBody);
                            break;
                    }
            });

            client.subscribe("/user/queue/newGame/" + gameId + "/shoot", payload => {
                    payloadBody = JSON.parse(payload.body);
                    ShotUtils.shoot(payloadBody);

                    if (isGameVsPC && payloadBody.allShipsSunk === true) isGameVsPcEnded = true;

                    if(ShotUtils.isComputerPlayer(payloadBody.currentPlayer) && !isGameVsPcEnded)
                        BoardUtils.setOpponentEmptyCellsActive();
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

$('#chatInput').on('keypress', function(e){
    chatText = this.value;

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

function sendPlaceShipTile(x, y){

    client.send('/ws/shipPlacement/' + gameId, {}, JSON.stringify({
                        "fieldStatus": "SHIP_ALIVE",
                        "type": shipsToPlace[ShipsSetupUtils.whichShip].type,
                        "length" : shipsToPlace[ShipsSetupUtils.whichShip].length,
                        "coords": "" + x + y
                        }));
}

function sendResetBoard(){

    client.send('/ws/resetBoard/' + gameId, {}, JSON.stringify({"messageType": "resetBoard", "username":"username", "message": "resetBoard"}));
}

function sendPlacementInfoToOpponent(shipName, whichOfAKind, isAllShipsPlaced){

    client.send('/ws/sendShipsPlacementInfo/' + gameId, {}, JSON.stringify({
                            "shipName": shipName,
                            "whichOfAKind": whichOfAKind,
                            "isAllShipsPlaced": isAllShipsPlaced
                            }));
}

function sendShotInfo(x, y){
    if(!isGameVsPcEnded){
        client.send('/ws/shoot/' + gameId, {}, JSON.stringify({
                            currentPlayer: "currentPlayer",
                            opponentPlayer: "opponentPlayer",
                            shotResult: "shotResult",
                            coords: "" + x + y
                            }));
    }
}

function sendShotInfoPC(x, y){
    sendShotInfo(x, y);
    setTimeout(sendShotInfo, 500, x, y);

}