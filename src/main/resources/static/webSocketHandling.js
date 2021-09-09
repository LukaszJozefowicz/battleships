var socket = null;
var client = null;
var currentUserId;
var activeUsersList = null;
var activeGamesList = null;
var chatText = "";
var isPlayerJoinedGame = false;
var isPlayerCreatedGame = false;
document.getElementById("chatInput").value = "";
let authenticatedUserTag = document.getElementById("currentUsername");
let authenticatedUserName = authenticatedUserTag.innerHTML;

window.onload = function load(){
    resetTimer();
    idleLogout();
    connect();
    document.getElementById("chatOutput").value = "";
    document.getElementById("chatInput").focus();
}
window.onbeforeunload = function unload(){
    disconnect();
    window.location = '/logout';
}

//document.getElementById("chatInput").addEventListener("keypress", submitMsgOnEnter);

function connect(){

    socket = new SockJS("http://localhost:8080/websocket");

    client = Stomp.over(socket);

    client.connect({}, frame => {

            var payloadBody = null;
            client.subscribe("/gameLobby", payload => {

                payloadBody = JSON.parse(payload.body);
                console.log("payload body " + payload.body);
                console.log("json parsed " + payloadBody)
                //console.log("payload body username " + payloadBody.username);
                //console.log("payload body message " + payloadBody.message);
                console.log("is array " + Array.isArray(payloadBody));
                if(Array.isArray(payloadBody) && payloadBody[0] === "usersList"){
                    let usersTableBody = document.getElementById('users-online-tbody');
                    usersTableBody.remove();
                    activeUsersList = payloadBody;
                    generateUsersTable();
                } else if(payloadBody.username !== undefined && payloadBody.message !== undefined){
                    receiveChatMessage(payloadBody);
                } else if(Array.isArray(payloadBody) && payloadBody[0].id === 0 && payloadBody[0].gameState !== undefined){
                    let gamesTableBody = document.getElementById('gamesTableBody');
                    gamesTableBody.remove();
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

                        window.location.href = "/newGame/" + payloadBody.id;
                   }

            });

            client.send('/ws/listOfUsers', {}, JSON.stringify(activeUsersList));
            client.send('/ws/sendToChat', {}, JSON.stringify({username: authenticatedUserName, message: " joined the lobby\n"}));
            client.send('/ws/gamesList', {}, JSON.stringify(activeGamesList));
        });

}

function disconnect(){
    if(client != null) {
        isPlayerJoinedGame = false;
        isPlayerCreatedGame = false;
        let authenticatedUserTag = document.getElementById("currentUsername");
        let authenticatedUserName = authenticatedUserTag.innerHTML;
        activeUsersList = activeUsersList.filter(e => e !== authenticatedUserName);

        client.send('/ws/userLeft', {}, JSON.stringify(activeUsersList));
        client.send('/ws/sendToChat', {}, JSON.stringify({username: authenticatedUserName, message: " left the lobby\n"}));
        //client.send('/ws/deleteGame', {}, JSON.stringify(activeGamesList));

        client.disconnect(payload => {
            client.disconnect();
        });
    }
}


/*function submitMsgOnEnter(){

    let textInput = document.getElementById("chatInput");
    chatText = textInput.value;


    if(event.which === 13 || event.keyCode === 13 || UIEvent.which === 13 || UIEvent.keyCode === 13){

        client.send('/ws/sendToChat', {}, JSON.stringify({username: authenticatedUserName, message: chatText}));

        event.target.form.dispatchEvent(new Event("submit", {cancelable: true}));
        event.preventDefault();
        UIEvent.preventDefault();
        textInput.value = "";
        chatText = "";
    }
}*/

$('#chatInput').on('keypress', function(e){
    chatText = this.value;
    console.log("in jquery");

        if(event.which === 13 || event.keyCode === 13){

            client.send('/ws/sendToChat', {}, JSON.stringify({username: authenticatedUserName, message: chatText}));
            e.preventDefault();
            this.value = "";
            chatText = "";
            return false;
        }
});

function receiveChatMessage(payloadBody){

    let textOutput = document.getElementById("chatOutput");
    textOutput.value += payloadBody.username + ":" + payloadBody.message + "\n";
    textOutput.scrollTop = textOutput.scrollHeight;
}

function generateUsersTable(){
                let usersTable = document.getElementById('users-online')
                let tbody = document.createElement('tbody');
                tbody.id = "users-online-tbody";
                console.log("users table generated");
                for(let i = 1; i < activeUsersList.length; i++){
                    let tr = document.createElement('tr');
                    tr.classList.add('games');
                    tr.classList.add('active-user-row');
                    tr.id = "tr" + activeUsersList[i];

                    let td = document.createElement('td');
                    td.classList.add('games');
                    td.innerHTML = activeUsersList[i];

                    tr.appendChild(td);
                    tbody.appendChild(tr);
                    usersTable.appendChild(tbody);

                }
}

function generateGamesTable(){
                let gamesTable = document.getElementById('gamesTable')
                let tbody = document.createElement('tbody');
                tbody.id = "gamesTableBody";
                console.log("games table generated");
                for(let i = 1; i < activeGamesList.length; i++){
                    console.log("iteration " + i);
                    let tr = document.createElement('tr');
                    tr.classList.add('games');

                    let td = document.createElement('td');
                    td.classList.add('id-col');
                    td.innerHTML = activeGamesList[i].id;
                    tr.appendChild(td);

                    td = document.createElement('td');
                    td.classList.add('games');
                    td.innerHTML = activeGamesList[i].player1;
                    tr.appendChild(td);

                    td = document.createElement('td');
                    td.classList.add('games');
                    td.id = "player2spot" + activeGamesList[i].id;
                    td.innerHTML = activeGamesList[i].player2;
                    tr.appendChild(td);

                    td = document.createElement('td');
                    td.classList.add('joinButton');
                    let input = document.createElement('input');
                    if(activeGamesList[i].player1 === authenticatedUserName && activeGamesList[i].player2 === "waiting for player"){
                                let onclickValue = "leaveGame()";
                                createButton(input, 'button', 'Leave', 'bold', onclickValue);
                    } else if(activeGamesList[i].player1 === authenticatedUserName
                        && activeGamesList[i].player2 !== "waiting for player"){
                                console.log("I'm in THAT if!!!!!!!!!!!!!!!!");
                                let onclickValue = "newGameRedirect(" + JSON.stringify(activeGamesList[i]) + ")";
                                createButton(input, 'button', 'Start', 'bold', onclickValue);
                    } else if(//activeGamesList[i].player1 === authenticatedUserName
                        //|| activeGamesList[i].player2 !== "waiting for player"
                        isPlayerJoinedGame
                        || isPlayerCreatedGame){
                                let onclickValue = "joinGame("+activeGamesList[i].id+")";
                                createButton(input, 'hidden', 'Join', 'bold', onclickValue);
                    } else {
                                let onclickValue = "joinGame("+activeGamesList[i].id+")";
                                createButton(input, 'button', 'Join', 'bold', onclickValue);
                    }

                    td.appendChild(input);
                    tr.appendChild(td);

                    tbody.appendChild(tr);
                }
                gamesTable.appendChild(tbody);
}

function createNewGame(){
                isPlayerCreatedGame = true;
                client.send('/ws/newGame', {}, JSON.stringify(
                [{"id":0,"player1":"string","player2":"string","gameState":"string"}]));
                document.getElementById('createNewGame').disabled = true;

}

function joinGame(id){
    console.log("join game method");
    isPlayerJoinedGame = true;
    let url = "/ws/joinGame/" + id;
    client.send(url, {}, JSON.stringify(
                    [{"id":0,"player1":"string","player2":"string","gameState":"string"}]));
    let player2td = document.getElementById("player2spot" + id);
    player2td.innerHTML = authenticatedUserName;
}

function leaveGame(){
    isPlayerCreatedGame = false;
    client.send('/ws/deleteGame', {}, JSON.stringify(activeGamesList));
    document.getElementById('createNewGame').disabled = false;
}

function createButton(element, type, value, className, onclickValue){
    element.type = type;
    element.value = value;
    element.className = className;
    element.setAttribute('onclick', onclickValue);
}

function newGameRedirect(payload){
    client.send('/ws/newGame/redirect', {}, JSON.stringify(
                    {"id":payload.id,
                    "player1":payload.player1,
                    "player2":payload.player2,
                    "gameState":payload.gameState}));

}