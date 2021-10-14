/* globals from th:inline script tag in boards.html

        const shipsToPlace = JSON.parse([[${shipsToPlace}]]);
        const startingPlayer = [[${startingPlayer}]];

*/

// ------------ text output utils ------------

class TextOutputUtils {

    static receiveChatMessage(payloadBody){
        this.textOutput.value += payloadBody.username + ":" + payloadBody.message + "\n";
        this.textOutput.scrollTop = this.textOutput.scrollHeight;
    }

    static appendCurrentTurnInfo(message, username){
        this.textOutput.value += message + username + "\n";
        this.textOutput.scrollTop = this.textOutput.scrollHeight;
    }

    static appendGameFinishedInfo(payloadBody, shotResultInfo){
        shotResultInfo += "\nAll " + payloadBody.opponentPlayer + "'s ships are sunk\n"
                                   + payloadBody.currentPlayer + " won!!\n";
        document.getElementById("backToLobby").style.display = "flex";
        return shotResultInfo;
    }

    static playerLeftInfo(payloadBody){

        document.getElementById("backToLobby").style.display = "flex";
        if(document.getElementById("gameLiveInfo").style.display === "none"){
            let duringGameInfo = document.getElementById("duringGameInfo");
            let opponentLeftInfo = document.createElement('h3');
            opponentLeftInfo.style.textAlign = "center";
            opponentLeftInfo.style.color = "red";
            opponentLeftInfo.style.fontWeight = "bold";
            opponentLeftInfo.innerHTML = "Opponent left :(";
            duringGameInfo.appendChild(opponentLeftInfo);
        }
        BoardUtils.disableBothBoards();
        this.textOutput.value += payloadBody.username + " left :(\n";
        this.textOutput.scrollTop = this.textOutput.scrollHeight;
    }
}
TextOutputUtils.textOutput = document.getElementById("chatOutput");

// ------------ board utils ------------

class BoardUtils {

    static setTileHitProperties(tile, fieldStatus, bgColor){
        tile.setAttribute('fieldstatus', fieldStatus);
        tile.style.backgroundColor = bgColor;
        tile.disabled = true;
        tile.style.cursor = "default";
    }

    static setCellInteractive(cell){
        $(cell).on({
                        mouseover: function() {

                            if($(cell).attr('fieldstatus') == 'empty' && $(cell).is(':enabled')){
                                    this.style.backgroundColor = "#1e90ff";     //highlighted
                                    this.style.cursor = "pointer";
                            }
                        },
                        mouseleave: function() {
                            if($(cell).attr('fieldstatus') == 'empty'){
                                this.style.backgroundColor = "#add8e6";
                            }
                            this.style.cursor = "default";
                        }
                    });
    }

    static setOpponentBoardInactive(){
        let opponentCells = document.querySelectorAll(".opp-btn");
        opponentCells.forEach(cell => {
            cell.disabled = true;
        });
    }

    static setOpponentEmptyCellsActive(){
        let opponentCells = document.querySelectorAll(".opp-btn");
        opponentCells.forEach(cell => {
            if(cell.getAttribute('fieldstatus') === "empty") cell.disabled = false;
            this.setCellInteractive(cell);
        });
    }

    static disableBothBoards(){
        let bothBoardsCells = document.querySelectorAll(".my-btn, .opp-btn");
        bothBoardsCells.forEach(cell => {
            cell.disabled=true;
        });
    }

    static markShipSunk(isLoggedPlayerTurn, coordsArray){

        let cells = isLoggedPlayerTurn
                    ? document.querySelectorAll(".opp-btn")
                    : document.querySelectorAll(".my-btn");

        coordsArray.forEach( coords => {
            cells.forEach(cell => {
                if(cell.id.includes(coords)){
                    this.setTileHitProperties(cell, 'shipSunk', 'black');
                }
            });
        });
    }

    static disableFieldsAroundSunkShip(isLoggedPlayerTurn, sunkShips){

        let cells = isLoggedPlayerTurn
                    ? document.querySelectorAll(".opp-btn")
                    : document.querySelectorAll(".my-btn");

        sunkShips.forEach(sunkShip => {
            cells.forEach(cell => {

                if(ShipsSetupUtils.isNeighbor(cell, sunkShip) || ShipsSetupUtils.isNeighborDiagonally(cell, sunkShip)){
                    cell.style.backgroundColor = "#B0B0B0";  //grey - disabled around ship
                    cell.disabled=true;
                    cell.setAttribute('fieldstatus', 'miss');
                }
            });
        });
    }

    static revealPlayerWonShips(cells, shipsToReveal){
        shipsToReveal.forEach(ship => {
            cells.forEach(cell => {
                if(cell.id.includes(ship)
                    && cell.getAttribute('fieldstatus') !== "shipHit"
                    && cell.getAttribute('fieldstatus') !== "shipSunk"){
                        cell.setAttribute('fieldstatus', 'shipPlaced');
                        cell.style.backgroundColor = "#0000cd";
                }
            });
        });
    }
}

// ------------ shot utils ------------

class ShotUtils {
    static startShootingPhase(){

        let yourInfo = document.getElementById("shipPlacingInfo");
        let opponentInfo = document.getElementById("opponentShipPlacingInfo");
        yourInfo.remove();
        opponentInfo.remove();

        document.getElementById("gameStaticInfo").style.display = "inline-block";
        document.getElementById("gameLiveInfo").style.display = "inline-block";
        let chatMsgAtGameStart = "The game has started!\nCurrent turn: ";

        TextOutputUtils.appendCurrentTurnInfo(chatMsgAtGameStart, startingPlayer);

        if(startingPlayer === authenticatedUserName){
            let opponentCells = document.querySelectorAll(".opp-btn");
            opponentCells.forEach(cell => {
                cell.disabled = false;
                BoardUtils.setCellInteractive(cell);
            });
        }
        if(this.isComputerPlayer(startingPlayer)){
            setTimeout(sendShotInfo, 2000, "randomX", "randomY");
        }
    }

    static shoot(payloadBody){
        let tileHit;
        let isLoggedPlayerTurn = payloadBody.currentPlayer === authenticatedUserName;

        if(isLoggedPlayerTurn){
            tileHit = document.querySelector("#" + CSS.escape(payloadBody.coords) + "opp");
            BoardUtils.setOpponentBoardInactive();
        } else {
            tileHit = document.querySelector("#" + CSS.escape(payloadBody.coords));
            BoardUtils.setOpponentEmptyCellsActive();
        }

        tileHit.disabled = true;
        let row = parseInt(String.fromCharCode(parseInt(payloadBody.coords.substring(0,1)) + 48)) + 1;  // 1-10
        let col = String.fromCharCode(parseInt(payloadBody.coords.substring(1,2)) + 65);                // A-J
        let shotResultInfo = payloadBody.currentPlayer + " shot: " + col + row + "\n";

        switch(payloadBody.shotResult){
            case "SHIP_HIT":
                shotResultInfo = this.setShotResultShipHit(tileHit, shotResultInfo);
                break;
            case "SHIP_SUNK":
                shotResultInfo = this.setShotResultShipSunk(tileHit, shotResultInfo, isLoggedPlayerTurn, payloadBody);
                break;
            case "MISS":
                shotResultInfo = this.setShotResultMiss(tileHit, shotResultInfo);
                break;
        }

        TextOutputUtils.appendCurrentTurnInfo(shotResultInfo, "");

        if(payloadBody.allShipsSunk === false){
            TextOutputUtils.appendCurrentTurnInfo("\nCurrent turn: ", payloadBody.opponentPlayer);
        }

    }

    static setShotResultShipHit(tileHit, shotResultInfo){
        BoardUtils.setTileHitProperties(tileHit, 'shipHit', 'red');
        shotResultInfo += payloadBody.opponentPlayer + "'s ship is hit!";
        return shotResultInfo;
    }

    static setShotResultMiss(tileHit, shotResultInfo){
        BoardUtils.setTileHitProperties(tileHit, 'miss', '#B0B0B0');
        shotResultInfo += payloadBody.currentPlayer + " missed!";
        return shotResultInfo;
    }

    static setShotResultShipSunk(tileHit, shotResultInfo, isLoggedPlayerTurn, payloadBody){
        BoardUtils.setTileHitProperties(tileHit, 'shipSunk', 'black');
        shotResultInfo += payloadBody.opponentPlayer + "'s ship is hit and sunk!";

        BoardUtils.markShipSunk(isLoggedPlayerTurn, JSON.parse(payloadBody.sunkShipCoords));
        let sunkShips = isLoggedPlayerTurn
                        ? document.querySelectorAll(".opp-btn[fieldstatus='shipSunk']")
                        : document.querySelectorAll(".my-btn[fieldstatus='shipSunk']")
        BoardUtils.disableFieldsAroundSunkShip(isLoggedPlayerTurn, sunkShips);

        if(payloadBody.allShipsSunk === true){
            shotResultInfo = TextOutputUtils.appendGameFinishedInfo(payloadBody, shotResultInfo);
            BoardUtils.disableBothBoards();
            let playerWonCells = payloadBody.currentPlayer === authenticatedUserName
                        ? document.querySelectorAll(".my-btn")
                        : document.querySelectorAll(".opp-btn")
            BoardUtils.revealPlayerWonShips(playerWonCells, JSON.parse(payloadBody.shipFieldsToReveal));
        }
        return shotResultInfo;
    }

    static isComputerPlayer(playerName){
        return playerName.includes("BotEasy") || playerName.includes("BotNormal") || playerName.includes("BotHard");
    }
}