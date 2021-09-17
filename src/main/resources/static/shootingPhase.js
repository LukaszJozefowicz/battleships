function startShootingPhase(){

    let yourInfo = document.getElementById("shipPlacingInfo");
    let opponentInfo = document.getElementById("opponentShipPlacingInfo");
    let resetButton = document.getElementById("reset");
    let randomizeButton = document.getElementById("randomPlacement");
    yourInfo.remove();
    opponentInfo.remove();
    resetButton.remove();
    randomizeButton.remove();

    let staticInfo = document.getElementById("gameStaticInfo");
    let liveInfo = document.getElementById("gameLiveInfo");
    staticInfo.style.visibility = "visible";
    liveInfo.style.visibility = "visible";

    appendCurrentTurnInfo({username: startingPlayer,        //th:inline in boards.html
                           message: "The game has started!\nCurrent turn: "});

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
    console.log("authenticatedUserName: " + authenticatedUserName);
    console.log("PAYLOAD IN SHOOT: " + payloadBody.currentPlayer + " " + payloadBody.opponentPlayer + " " + payloadBody.shotResult + " " + payloadBody.coords);

    if(payloadBody.currentPlayer === authenticatedUserName){
        tileHit = document.querySelector("#opp" + payloadBody.coords);
        setOpponentBoardInactive();
    } else {
        let opponentCells = document.querySelectorAll(".opp-btn");
                opponentCells.forEach(function(cell){
                    cell.disabled = false;
                    setCellActive(cell);
                });
        tileHit = document.querySelector("#" + CSS.escape(payloadBody.coords));
    }

    tileHit.disabled = true;

    switch(payloadBody.shotResult){
        case "SHIP_HIT":
            tileHit.setAttribute('fieldstatus', 'shipHit');
            tileHit.style.backgroundColor = "red";
            break;
        case "SHIP_SUNK":
            tileHit.setAttribute('fieldstatus', 'shipSunk');
            tileHit.style.backgroundColor = "black";
            break;
        case "MISS":
            tileHit.setAttribute('fieldstatus', 'miss');
            tileHit.style.backgroundColor = "#B0B0B0";
            break;
    }
}

function setCellActive(cell){
    $(cell).on({
                    mouseenter: function() {

                    if($(cell).attr('fieldstatus') == 'empty' && $(cell).is(':enabled')){
                            this.style.backgroundColor = "#1e90ff";
                    }
                        if(($(cell).attr('fieldstatus') == 'empty' || $(cell).attr('fieldstatus') == 'highlighted')
                            && $(cell).is(':enabled')){
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