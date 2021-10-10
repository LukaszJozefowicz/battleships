var createGameButton = document.getElementById('createNewGame');
var isPlayerCreatedGame = false;
var isPlayerJoinedGame = false;

function generateUsersTable(){
                let usersTable = document.getElementById('users-online')
                let tbody = document.createElement('tbody');
                tbody.id = "users-online-tbody";

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

                let gamesTableBody = document.getElementById('gamesTableBody');
                if(gamesTableBody) gamesTableBody.remove();

                let gamesTable = document.getElementById('gamesTable')
                let tbody = document.createElement('tbody');
                tbody.id = "gamesTableBody";

                isPlayerCreatedGame = activeGamesList.some(game => game.player1 === authenticatedUserName);
                isPlayerJoinedGame = activeGamesList.some(game => game.player2 === authenticatedUserName);

                for(let i = 1; i < activeGamesList.length; i++){
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
                    setInputFieldProperties(input, 'hidden', '', '', "");

                    switch (activeGamesList[i].gameState){
                        case "WAITING_FOR_PLAYER":
                            setPlayerCanLeaveGame(activeGamesList[i].player1, input);
                            setPlayerCanJoinGame(activeGamesList[i].id, input);
                            break;
                        case "READY_TO_START":
                            setPlayerCanStartGame(activeGamesList[i], input);
                            setPlayerCanLeaveGame(activeGamesList[i].player2, input);
                            break;
                        case "GAME_IN_PROGRESS":
                            setGameInProgressInfo(input);
                            break;

                    }

                    td.appendChild(input);
                    tr.appendChild(td);

                    tbody.appendChild(tr);
                }

                gamesTable.appendChild(tbody);

                let gameStartInfo = document.getElementById('gameStartInfo');
                if(isPlayerJoinedGame){
                    createGameButton.disabled = true;
                    gameStartInfo.style.visibility = "visible";
                    gameStartInfo.innerHTML = "Waiting for game creator to start game";
                } else {
                    document.getElementById('gameStartInfo').style.visibility = "hidden";
                }
}

function setInputFieldProperties(element, type, value, className, onclickValue){
    element.type = type;
    element.value = value;
    element.className = className;
    element.setAttribute('onclick', onclickValue);
}

function setPlayerCanLeaveGame(player, input){
    if(player === authenticatedUserName){
        createGameButton.disabled = true;
        let onclickValue = "leaveGame()";
        setInputFieldProperties(input, 'button', 'Leave', 'bold', onclickValue);
    }
}

function setPlayerCanJoinGame(gameId, input){
    if(!isPlayerJoinedGame && !isPlayerCreatedGame){
        createGameButton.disabled = false;
        let onclickValue = "joinGame("+gameId+");";
        setInputFieldProperties(input, 'button', 'Join', 'bold', onclickValue);
    }
}

function setPlayerCanStartGame(game, input){
    if(game.player1 === authenticatedUserName){
        let onclickValue = "newGameRedirect(" + JSON.stringify(game) + "); this.disabled = true;";
        setInputFieldProperties(input, 'button', 'Start', 'bold', onclickValue);
    }
}

function setGameInProgressInfo(input){
    setInputFieldProperties(input, 'text', 'Game in progress', 'gameInProgressInfo', '');
}

function receiveChatMessage(payloadBody){

    let textOutput = document.getElementById("chatOutput");
    textOutput.value += payloadBody.username + ":" + payloadBody.message + "\n";
    textOutput.scrollTop = textOutput.scrollHeight;
}

function redirectToNewGameScreen(gameId){
    window.location.href = "/newGame/" + gameId;
}