function startShootingPhase(){

    let yourInfo = document.getElementById("shipPlacingInfo");
    let opponentInfo = document.getElementById("opponentShipPlacingInfo");
    yourInfo.remove();
    opponentInfo.remove();

    let staticInfo = document.getElementById("gameStaticInfo");
    let liveInfo = document.getElementById("gameLiveInfo");
    staticInfo.style.visibility = "visible";
    liveInfo.style.visibility = "visible";

    appendCurrentTurnInfo({username: startingPlayer,        //th:inline in boards.html
                           message: "The game has started!\nPlayer to start chosen randomly.\nCurrent turn: "});

    if(startingPlayer === authenticatedUserName){
        let opponentCells = document.querySelectorAll(".opp-btn");
        opponentCells.forEach(function(cell){
            cell.disabled = false;
            setCellActive(cell);
        });
    }

//    let gameInfo = document.getElementById("gameLiveInfo");
//    let h5 = document.createElement('h5');
//    h5.className = "centered";
//    h5.innerHTML = "The game has started";
//    gameInfo.appendChild(h5);
}

function shoot(payloadBody){

    let tileHit;
    let cells;
    console.log("authenticatedUserName: " + authenticatedUserName);
    console.log("PAYLOAD IN SHOOT: " + payloadBody.currentPlayer + " " + payloadBody.opponentPlayer + " " + payloadBody.shotResult + " " + payloadBody.coords + " " + payloadBody.coords);

    if(payloadBody.currentPlayer === authenticatedUserName){
        cells = document.querySelectorAll(".opp-btn");
        tileHit = document.querySelector("#" + CSS.escape(payloadBody.coords) + "opp");
        setOpponentBoardInactive();
    } else {
        cells = document.querySelectorAll(".my-btn");
                document.querySelectorAll(".opp-btn").forEach(cell => {
                    if(cell.getAttribute('fieldstatus') === "empty") cell.disabled = false;
                    setCellActive(cell);
                });
        tileHit = document.querySelector("#" + CSS.escape(payloadBody.coords));
    }

    tileHit.disabled = true;

    switch(payloadBody.shotResult){
        case "SHIP_HIT":
            tileHit.setAttribute('fieldstatus', 'shipHit');
            tileHit.style.backgroundColor = "red";
            tileHit.disabled = true;
            tileHit.style.cursor = "default";
            break;
        case "SHIP_SUNK":

            tileHit.disabled = true;
            tileHit.style.cursor = "default";

            markShipSunk(cells, JSON.parse(payloadBody.sunkShipCoords));
            let sunkShips = payloadBody.currentPlayer === authenticatedUserName
                            ? document.querySelectorAll(".opp-btn[fieldstatus='shipSunk']")
                            : document.querySelectorAll(".my-btn[fieldstatus='shipSunk']")
            disableFieldsAroundSunkShip(cells, sunkShips);
            if(payloadBody.allShipsSunk === true){
                setupGameFinished(payloadBody);
                let cells = payloadBody.currentPlayer === authenticatedUserName
                            ? document.querySelectorAll(".my-btn")
                            : document.querySelectorAll(".opp-btn")
                revealPlayerWonShips(cells, JSON.parse(payloadBody.shipFieldsToReveal));
            }
            break;
        case "MISS":
            tileHit.setAttribute('fieldstatus', 'miss');
            tileHit.style.backgroundColor = "#B0B0B0";
            tileHit.disabled = true;
            tileHit.style.cursor = "default";
            break;
    }
}

function setCellActive(cell){
    $(cell).on({
                    mouseenter: function() {

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

function setOpponentBoardInactive(){
    let opponentCells = document.querySelectorAll(".opp-btn");
    opponentCells.forEach(function(cell){
        cell.disabled = true;
    });
}

function markShipSunk(cells, coordsArray){

    coordsArray.forEach( coords => {
        cells.forEach(cell => {
            if(cell.id.includes(coords)){
                cell.setAttribute('fieldstatus', 'shipSunk');
                cell.style.backgroundColor = "black";
                cell.disabled = true;
            }
        });
    });
}

function disableFieldsAroundSunkShip(cells, sunkShips){

    sunkShips.forEach(sunkShip => {
        cells.forEach(cell => {

            if(isNeighbor(cell, sunkShip) || isNeighborDiagonally(cell, sunkShip)){
                cell.style.backgroundColor = "#B0B0B0";  //grey - disabled around ship
                cell.disabled=true;
                cell.setAttribute('fieldstatus', 'miss');
            }
        });
    });
}

function setupGameFinished(payloadBody){
    let bothBoardsCells = document.querySelectorAll(".my-btn, .opp-btn");
    bothBoardsCells.forEach(cell => {
        cell.disabled=true;
    });
}

function revealPlayerWonShips(cells, shipsToReveal){
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