//global vars declared in boards.html th:inline script

function resetBoard(){
    whichShip = 0;
    whichFieldOfShip  = 1;
    var cells = document.querySelectorAll("#boardTable .my-btn");

    cells.forEach(function(cell){
        cell.style.backgroundColor = "#add8e6";  //default color
        cell.style.cursor = "default";
        cell.disabled=false;
        cell.setAttribute('fieldstatus', 'empty');

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
    });
    document.getElementById("shipPlacingInfo").innerHTML = "Ship Placement phase."
                        + "<br>Place ships by clicking."
                        + "<br>Longest ships first.";
    sendResetBoard();
}

function setShipPlacementInfo(){
        var currentShipType = shipsToPlace[whichShip].type + shipsToPlace[whichShip].whichOfAKind;
        var currentShipLength = shipsToPlace[whichShip].length;

        whichFieldOfShip++;

//        console.log("which ship: " + whichShip);
//        console.log("which field of ship: " + whichFieldOfShip);
//        console.log("shipsToPlace length: " + shipsToPlace.length);

        setPlacementInfoText(whichShip);

        if(whichShip === shipsToPlace.length - 1 && whichFieldOfShip === currentShipLength + 1){
            document.getElementById("shipPlacingInfo").className = "red";
            document.getElementById("shipPlacingInfo").innerHTML = "Your board is ready";

            sendPlacementInfoToOpponent(shipsToPlace[whichShip].type, shipsToPlace[whichShip].whichOfAKind, "true");
            areYouReady = true;
            whichFieldOfShip = 1;
            whichShip++;

        } else if(whichFieldOfShip === currentShipLength + 1){
            whichFieldOfShip = 1;
            setPlacementInfoText(whichShip+1);
            whichShip++;
        }
}

function setPlacementInfoText(shipCount){
    var placementInfoText = "Available ships:<br><span style = \"color:red\">";

            for(let i = shipCount; i < shipsToPlace.length; i++){
                placementInfoText += shipsToPlace[i].type
                                    + " length: " + shipsToPlace[i].length;
                if(i === shipCount){
                    placementInfoText += "</span>";
                }
                placementInfoText += "<br>";
            }

            document.getElementById("shipPlacingInfo").innerHTML = placementInfoText;
            if(socket.readyState === 1)
                sendPlacementInfoToOpponent(shipsToPlace[shipCount].type, shipsToPlace[shipCount].whichOfAKind, "false");
}

function setShip(that){

        setShipPlacementInfo();
        that.style.backgroundColor = "#0000cd"; //ship placed color
        that.disabled=true;
        that.setAttribute('fieldstatus', 'shipPlaced');

        highlightNeighbors();

        if(whichFieldOfShip === 1){
            disableFieldsAroundPlacedShip();
        }

        if(whichShip === shipsToPlace.length){
            disableAllFields();
            setUpForGameStart();
            if(isOpponentReady || isGameVsPC)
                startShootingPhase();
//            if(shipsToPlacePC !== undefined)
//                setComputerPlayerShips();
        }
}

function highlightNeighbors(){
    var cells = document.querySelectorAll("#boardTable .my-btn");
    var placedShips = document.querySelectorAll(".my-btn[fieldstatus='shipPlaced']");

        placedShips.forEach(function(that){
            cells.forEach(function(cell){
                if(isNeighbor(cell, that) && cell.getAttribute('fieldstatus') === "empty" && whichFieldOfShip != 1){
                    cell.style.backgroundColor = "#6495ed";  //highlighted neighbor
                    cell.disabled = false;
                    cell.setAttribute('fieldstatus', 'highlighted');
                } else if(cell.getAttribute('fieldstatus') === "shipPlaced"){
                    cell.style.backgroundColor = "#0000cd";  //placed ship
                } else if(cell.getAttribute('fieldstatus') === "empty"){
                    cell.style.backgroundColor = "#add8e6";  //default color
                    cell.disabled=true;
                }
            });
        });
}

function disableFieldsAroundPlacedShip(){
    var placedShips = document.querySelectorAll(".my-btn[fieldstatus='shipPlaced']");
    var cells = document.querySelectorAll("#boardTable .my-btn");

    placedShips.forEach(function(that){
        cells.forEach(function(cell){
            if(cell.getAttribute('fieldstatus') === "highlighted"){
                cell.setAttribute('fieldstatus', 'empty');
            }

            if(isNeighbor(cell, that) || isNeighborDiagonally(cell, that)){
                cell.style.backgroundColor = "#B0B0B0";  //grey - disabled around ship
                cell.disabled=true;
                cell.setAttribute('fieldstatus', 'aroundShip');
            }
            else if(cell.getAttribute('fieldstatus') === "empty"){
                cell.disabled=false;
            }
        });
    });
}

function disableAllFields(){
    var cells = document.querySelectorAll("#boardTable .my-btn");

    cells.forEach(function(cell){
        cell.disabled=true;
    })
}

function setUpForGameStart(){
    var cells = document.querySelectorAll("#boardTable .my-btn");

    cells.forEach(function(cell){
        if(cell.getAttribute('fieldstatus') === "aroundShip"){
            cell.setAttribute('fieldstatus', 'empty');
            cell.style.backgroundColor = "#add8e6"
        }
    });

    let resetButton = document.getElementById("reset");
    let randomizeButton = document.getElementById("randomPlacement");
    resetButton.remove();
    randomizeButton.remove();
}

function isNeighbor(cell, button){
    if( (isNeighborRight(cell, button) || isNeighborLeft(cell, button) || isNeighborDown(cell, button) || isNeighborUp(cell, button))
        && cell.getAttribute('fieldstatus') === "empty"
        ) return true;
        else return false;
}

function isNeighborDiagonally(cell, button){
    if( (isNeighborUpRight(cell, button) || isNeighborUpLeft(cell, button) || isNeighborDownRight(cell, button) || isNeighborDownLeft(cell, button))
        && cell.getAttribute('fieldstatus') === "empty"
        ) return true;
        else return false;
}

function isNeighborRight(cell, button){
    return parseInt(button.id.substring(0,1)) + 1 == parseInt(cell.id.substring(0,1))
        && parseInt(button.id.substring(1,2)) == parseInt(cell.id.substring(1,2))
        && parseInt(button.id.substring(0,1)) + 1 <= 9
}
function isNeighborLeft(cell, button){
    return parseInt(button.id.substring(0,1)) - 1 == parseInt(cell.id.substring(0,1))
        && parseInt(button.id.substring(1,2)) == parseInt(cell.id.substring(1,2))
        && parseInt(button.id.substring(0,1)) - 1 >= 0
}
function isNeighborDown(cell, button){
    return parseInt(button.id.substring(0,1)) == parseInt(cell.id.substring(0,1))
        && parseInt(button.id.substring(1,2)) + 1 == parseInt(cell.id.substring(1,2))
        && parseInt(button.id.substring(1,2)) + 1 <= 9
}
function isNeighborUp(cell, button){
    return parseInt(button.id.substring(0,1)) == parseInt(cell.id.substring(0,1))
        && parseInt(button.id.substring(1,2)) - 1 == parseInt(cell.id.substring(1,2))
        && parseInt(button.id.substring(1,2)) - 1 >= 0
}
function isNeighborUpRight(cell, button){
    return parseInt(button.id.substring(0,1)) - 1 == parseInt(cell.id.substring(0,1))
        && parseInt(button.id.substring(1,2)) + 1 == parseInt(cell.id.substring(1,2))
        && parseInt(button.id.substring(0,1)) - 1 >= 0
        && parseInt(button.id.substring(1,2)) + 1 <= 9
}
function isNeighborUpLeft(cell, button){
    return parseInt(button.id.substring(0,1)) - 1 == parseInt(cell.id.substring(0,1))
        && parseInt(button.id.substring(1,2)) - 1 == parseInt(cell.id.substring(1,2))
        && parseInt(button.id.substring(0,1)) - 1 >= 0
        && parseInt(button.id.substring(1,2)) - 1 >= 0
}
function isNeighborDownRight(cell, button){
    return parseInt(button.id.substring(0,1)) + 1 == parseInt(cell.id.substring(0,1)) &&
        parseInt(button.id.substring(1,2)) + 1 == parseInt(cell.id.substring(1,2))
        && parseInt(button.id.substring(0,1)) + 1 <= 9
        && parseInt(button.id.substring(1,2)) + 1 <= 9
}
function isNeighborDownLeft(cell, button){
    return parseInt(button.id.substring(0,1)) + 1 == parseInt(cell.id.substring(0,1)) &&
        parseInt(button.id.substring(1,2)) - 1 == parseInt(cell.id.substring(1,2))
        && parseInt(button.id.substring(0,1)) + 1 <= 9
        && parseInt(button.id.substring(1,2)) - 1 >= 0
}

function setAllShipsRandomly(){
            var cells = document.querySelectorAll("#boardTable .my-btn");
            var activeCells = [];

            cells.forEach(function(cell){
                if(cell.disabled === false){
                    activeCells.push(cell);
                }
            });

            if(activeCells.length === 0){
                resetBoard();
                setAllShipsRandomly();
            }

            var randomCell = activeCells[Math.floor(Math.random() * activeCells.length)];
            let col = parseInt(randomCell.id.substring(0,1));
            let row = parseInt(randomCell.id.substring(1,2));
            sendPlaceShipTile(col, row);
            setShip(randomCell);
            if(whichShip < shipsToPlace.length){
                setTimeout(setAllShipsRandomly, 500);
            }
}