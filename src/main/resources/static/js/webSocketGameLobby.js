var socket = null;
var client = null;
var currentUserId;
var activeUsersList = null;
var activeGamesList = null;
var chatText = "";
document.getElementById("chatInput").value = "";
var authenticatedUserTag = document.getElementById("currentUsername");
var authenticatedUserName = authenticatedUserTag.innerHTML;

window.onload = function load(){
    resetTimer();
    idleLogout();
    connect();
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
                                generateUsersTable();
                } else if(payloadBody.username !== undefined
                         && payloadBody.message !== undefined
                         && payloadBody.messageType == "lobbyChatMsg"){
                                receiveChatMessage(payloadBody);
                } else if(Array.isArray(payloadBody)
                         && payloadBody[0].id === 0
                         && payloadBody[0].gameState !== undefined){
                                activeGamesList = payloadBody;
                                generateGamesTable();
                }

            });

            client.subscribe("/user/queue/notify", payload => {
                   payloadBody = JSON.parse(payload.body);
                   if(payloadBody.id != undefined
                        && payloadBody.player1 !== undefined
                        && payloadBody.player2 !== undefined
                        && payloadBody.gameState === "READY_TO_START"){
                        gameStartCountdown();
                        setTimeout(redirectToNewGameScreen.bind(null, payloadBody.id), 3000);
                   }

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
    console.log("in jquery");

        if(event.which === 13 || event.keyCode === 13){

            client.send('/ws/sendToChat', {}, JSON.stringify({messageType: "lobbyChatMsg", username: authenticatedUserName, message: chatText}));
            e.preventDefault();
            this.value = "";
            chatText = "";
            return false;
        }
});

function createNewGame(){
                client.send('/ws/newGame', {}, JSON.stringify(
                [{"id":0,"player1":"string","player2":"string","gameState":"string"}]));
                document.getElementById('createNewGame').disabled = true;
                document.getElementById('createNewGameVsPC').disabled = true;

}

function joinGame(id){
    let url = "/ws/joinGame/" + id;
    client.send(url, {}, JSON.stringify(
                    [{"id":0,"player1":"string","player2":"string","gameState":"string"}]));
    let player2td = document.getElementById("player2spot" + id);
    player2td.innerHTML = authenticatedUserName;
    document.getElementById('createNewGame').disabled = true;
}

function leaveGame(){
    client.send('/ws/deleteGameOrUserJoined', {}, JSON.stringify(activeGamesList));
    let gameStartInfo = document.getElementById('gameStartInfo').style.visibility = "hidden";
    document.getElementById('createNewGame').disabled = false;
}

function newGameRedirect(payload){

    activeUsersList = activeUsersList.filter(e => e !== payload.player1 && e !== payload.player2);
    client.send('/ws/userLeft', {}, JSON.stringify(activeUsersList));
    client.send('/ws/newGame/redirect', {}, JSON.stringify(
                    {"id":payload.id,
                    "player1":payload.player1,
                    "player2":payload.player2,
                    "gameState":payload.gameState}));

}

// ---------- vs PC ----------

function createNewGameVsPC(){
                client.send('/ws/newGameVsPC', {}, JSON.stringify(
                [{"id":0,"player1":"string","player2":"Computer","gameState":"string"}]));
                document.getElementById('createNewGame').disabled = true;
                document.getElementById('createNewGameVsPC').disabled = true;

}