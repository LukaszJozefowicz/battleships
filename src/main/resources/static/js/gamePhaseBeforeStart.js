class GameLobbyUtils {

    static generateUsersTable(){
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

    static generateGamesTable(){

                    let gamesTableBody = document.getElementById('gamesTableBody');
                    if(gamesTableBody) gamesTableBody.remove();

                    let gamesTable = document.getElementById('gamesTable')
                    let tbody = document.createElement('tbody');
                    tbody.id = "gamesTableBody";

                    this.isPlayerCreatedGame = activeGamesList.some(game => game.player1 === authenticatedUserName);
                    this.isPlayerJoinedGame = activeGamesList.some(game => game.player2 === authenticatedUserName);

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
                        this.setInputFieldProperties(input, 'hidden', '', '', "");

                        switch (activeGamesList[i].gameState){
                            case "WAITING_FOR_PLAYER":
                                this.setPlayerCanLeaveGame(activeGamesList[i].player1, input);
                                this.setPlayerCanJoinGame(activeGamesList[i].id, input);
                                break;
                            case "READY_TO_START":
                                this.setPlayerCanStartGame(activeGamesList[i], input);
                                this.setPlayerCanLeaveGame(activeGamesList[i].player2, input);
                                break;
                            case "GAME_IN_PROGRESS":
                                this.setGameInProgressInfo(input);
                                break;

                        }

                        td.appendChild(input);
                        tr.appendChild(td);

                        tbody.appendChild(tr);
                    }

                    gamesTable.appendChild(tbody);

                    let gameStartInfo = document.getElementById('gameStartInfo');
                    if(this.isPlayerJoinedGame){
                        this.createGameButton.disabled = true;
                        gameStartInfo.style.visibility = "visible";
                        gameStartInfo.innerHTML = "Waiting for game creator to start game";
                    } else {
                        document.getElementById('gameStartInfo').style.visibility = "hidden";
                    }
    }

    static setInputFieldProperties(element, type, value, className, onclickValue){
        element.type = type;
        element.value = value;
        element.className = className;
        element.setAttribute('onclick', onclickValue);
    }

    static setPlayerCanLeaveGame(player, input){
        if(player === authenticatedUserName){
            this.createGameButton.disabled = true;
            let onclickValue = "sendLeaveGame()";
            this.setInputFieldProperties(input, 'button', 'Leave', 'bold', onclickValue);
        }
    }

    static setPlayerCanJoinGame(gameId, input){
        if(!this.isPlayerJoinedGame && !this.isPlayerCreatedGame){
            this.createGameButton.disabled = false;
            let onclickValue = "sendJoinGame("+gameId+");";
            this.setInputFieldProperties(input, 'button', 'Join', 'bold', onclickValue);
        }
    }

    static setPlayerCanStartGame(game, input){
        if(game.player1 === authenticatedUserName){
            let onclickValue = "sendNewGameRedirect(" + JSON.stringify(game) + "); this.disabled = true;";
            this.setInputFieldProperties(input, 'button', 'Start', 'bold', onclickValue);
        }
    }

    static setGameInProgressInfo(input){
        this.setInputFieldProperties(input, 'text', 'Game in progress', 'gameInProgressInfo', '');
    }

    static receiveChatMessage(payloadBody){

        let textOutput = document.getElementById("chatOutput");
        textOutput.value += payloadBody.username + ":" + payloadBody.message + "\n";
        textOutput.scrollTop = textOutput.scrollHeight;
    }

    static redirectToNewGameScreen(gameId){
        window.location.href = "/newGame/" + gameId;
    }

    static setNewGameButtons(isDisabled){
        document.getElementById('createNewGame').disabled = isDisabled;
        document.getElementById('createNewGameVsPC').disabled = isDisabled;
    }
}

GameLobbyUtils.createGameButton = document.getElementById('createNewGame');
GameLobbyUtils.isPlayerCreatedGame = false;
GameLobbyUtils.isPlayerJoinedGame = false;