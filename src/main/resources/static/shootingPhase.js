function startShootingPhase(){

    let yourInfo = document.getElementById("shipPlacingInfo");
    let opponentInfo = document.getElementById("opponentShipPlacingInfo");
    let resetButton = document.getElementById("reset");
    yourInfo.remove();
    opponentInfo.remove();
    resetButton.remove();

    let staticInfo = document.getElementById("gameStaticInfo");
    let liveInfo = document.getElementById("gameLiveInfo");
    staticInfo.style.visibility = "visible";
    liveInfo.style.visibility = "visible";

    appendCurrentTurnInfo({username: startingPlayer,        //th:inline in boards.html
                           message: "Current turn: "});

//    let gameInfo = document.getElementById("gameLiveInfo");
//    let h5 = document.createElement('h5');
//    h5.className = "centered";
//    h5.innerHTML = "The game has started";
//    gameInfo.appendChild(h5);
}