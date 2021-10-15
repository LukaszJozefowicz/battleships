var socket = null;
var client = null;
var activeUsersList = null;
var activeGamesList = null;
var chatText = "";
var errorMsg = "";
const authenticatedUserName = document.getElementById("currentUsername").innerHTML;

window.onload = function load(){
    resetTimer();
    idleLogout();
    connect();
    document.getElementById("chatInput").value = "";
    document.getElementById("chatOutput").value = "";
    document.getElementById("chatInput").focus();
    document.getElementById("backToMenu").style.display = "block";
}
window.onbeforeunload = function unload(){
    disconnect();
}

function connect(){

    socket = new SockJS("http://localhost:8080/websocket");//https://battleships-spring-boot.herokuapp.com/websocket

    client = Stomp.over(socket);

    client.connect({}, frame => {

            var payloadBody = null;
            client.subscribe("/gameLobby", payload => {

                payloadBody = JSON.parse(payload.body);

                if(Array.isArray(payloadBody)
                    && payloadBody.includes("usersList")){
                                let usersTableBody = document.getElementById('users-online-tbody');
                                usersTableBody.remove();
                                activeUsersList = payloadBody;
                                GameLobbyUtils.generateUsersTable();
                } else if(payloadBody.username !== undefined
                         && payloadBody.message !== undefined
                         && payloadBody.messageType == "lobbyChatMsg"){
                                GameLobbyUtils.receiveChatMessage(payloadBody);
                } else if(Array.isArray(payloadBody)
                         && payloadBody[0].id === 0
                         && payloadBody[0].gameState !== undefined){
                                activeGamesList = payloadBody;
                                GameLobbyUtils.generateGamesTable();
                }

            });

            client.subscribe("/user/queue/notify", payload => {
                   payloadBody = JSON.parse(payload.body);
                   if(payloadBody.id != undefined
                        && payloadBody.player1 !== undefined
                        && payloadBody.player2 !== undefined
                        && payloadBody.gameState === "READY_TO_START"){
//                            GameLobbyUtils.redirectToNewGameScreen(payloadBody.id);
                            gameStartCountdown();
                            setTimeout(GameLobbyUtils.redirectToNewGameScreen.bind(null, payloadBody.id), 3000);
                   }

            });

            client.subscribe("/user/queue/error", payload => {
                errorMsg = payload.body;
                window.location.href = "/boardSetupError?" + errorMsg;
            });

            client.send('/ws/listOfUsers', {}, JSON.stringify(activeUsersList));
            client.send('/ws/sendToChat', {}, JSON.stringify({messageType: "lobbyChatMsg", username: authenticatedUserName, message: " joined the lobby\n"}));
            client.send('/ws/gamesList', {}, JSON.stringify(activeGamesList));
        });

}

function disconnect(){
    if(client != null) {
        activeUsersList = activeUsersList.filter(e => e !== authenticatedUserName);

        client.send('/ws/userLeft', {}, JSON.stringify(activeUsersList));
        client.send('/ws/sendToChat', {}, JSON.stringify({messageType: "lobbyChatMsg", username: authenticatedUserName, message: " left the lobby\n"}));
        client.send('/ws/deleteGameOrUserJoined', {}, JSON.stringify(activeGamesList));

        client.disconnect(payload => {
            client.disconnect();
        });
    }
}

$('#chatInput').on('keypress', function(e){
    chatText = this.value;

        if(event.which === 13 || event.keyCode === 13){

            client.send('/ws/sendToChat', {}, JSON.stringify(
                                        { messageType: "lobbyChatMsg",
                                        username: authenticatedUserName,
                                        message: chatText}));
            e.preventDefault();
            this.value = "";
            chatText = "";
            return false;
        }
});

function sendCreateNewGame(){
    client.send('/ws/newGame', {}, JSON.stringify(
        [{"id":0,"player1":"string","player2":"string","gameState":"string"}]));
    GameLobbyUtils.setNewGameButtons(true);

}

function sendJoinGame(id){
    let url = "/ws/joinGame/" + id;
    client.send(url, {}, JSON.stringify(
        [{"id":0,"player1":"string","player2":"string","gameState":"string"}]));
    let player2td = document.getElementById("player2spot" + id);
    player2td.innerHTML = authenticatedUserName;
    GameLobbyUtils.setNewGameButtons(true);
}

function sendLeaveGame(){
    client.send('/ws/deleteGameOrUserJoined', {}, JSON.stringify(activeGamesList));
    let gameStartInfo = document.getElementById('gameStartInfo').style.visibility = "hidden";
    GameLobbyUtils.setNewGameButtons(false);
}

function sendNewGameRedirect(payload){

    activeUsersList = activeUsersList.filter(e => e !== payload.player1 && e !== payload.player2);
    client.send('/ws/newGame/redirect', {}, JSON.stringify(
                    {"id":payload.id,
                    "player1":payload.player1,
                    "player2":payload.player2,
                    "gameState":payload.gameState}));

}

// ---------- vs PC ----------

function sendCreateNewGameVsPC(){
    client.send('/ws/newGameVsPC', {}, JSON.stringify(
        [{"id":0,"player1":"string","player2":"Computer","gameState":"string"}]));
        GameLobbyUtils.setNewGameButtons(true);
}