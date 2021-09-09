var whichShip = 0;
    var whichFieldOfShip  = 1;
    var shipsToPlace = JSON.parse([[${shipsToPlace}]]);

function resetBoard(){
    whichShip = 0;
    whichFieldOfShip  = 1;
    var cells = document.querySelectorAll(".dataCell");

    cells.forEach(function(cell){
        cell.firstChild.style.backgroundColor = "#add8e6";  //default color

    })
}

function setShipPlacementInfo(){
        var currentShipType = shipsToPlace[whichShip].type + shipsToPlace[whichShip].whichOfAKind;
        var currentShipLength = shipsToPlace[whichShip].length;

        document.getElementById("shipPlacingInfo").innerHTML = "Placing ship: " + currentShipType
                                                                            + " length: " + currentShipLength
                                                                            + " ship squares placed: " + whichFieldOfShip;
        whichFieldOfShip++;

        console.log("which ship: " + whichShip);
        console.log("which field of ship: " + whichFieldOfShip);
        console.log("shipsToPlace length: " + shipsToPlace.length);

        if(whichShip === shipsToPlace.length - 1){
            document.getElementById("shipPlacingInfo").innerHTML = "All ships have been placed";
            whichFieldOfShip = 1;
            whichShip++;
        } else
        if(whichFieldOfShip === currentShipLength + 1){
            whichFieldOfShip = 1;
            document.getElementById("shipPlacingInfo").innerHTML = currentShipType + " placed. Next ship to place: "
                                                                    + shipsToPlace[whichShip+1].type
                                                                    + shipsToPlace[whichShip+1].whichOfAKind
                                                                    + " length: " + shipsToPlace[whichShip+1].length;
        whichShip++;
        }

}
function setShip(that){
        setShipPlacementInfo();
        that.style.backgroundColor = "#0000cd"; //ship placed color
        that.disabled=true;
        that.style.cursor = "not-allowed";
        that.setAttribute('fieldstatus', 'shipPlaced');

        highlightNeighbors();

        if(whichFieldOfShip === 1)
            disableFieldsAroundPlacedShip();

        if(whichShip === shipsToPlace.length)
            disableAllFields();
}

function highlightNeighbors(){
    var cells = document.querySelectorAll(".dataCell");
    var placedShips = document.querySelectorAll(".my-btn[fieldstatus='shipPlaced']");

        placedShips.forEach(function(that){
            cells.forEach(function(cell){
                if(isNeighbor(cell, that) && cell.firstChild.getAttribute('fieldstatus') === "empty" && whichFieldOfShip != 1){
                    cell.firstChild.style.backgroundColor = "#6495ed";  //highlighted neighbor
                    cell.firstChild.disabled = false;
                    cell.firstChild.setAttribute('fieldstatus', 'highlighted');
                } else if(cell.firstChild.getAttribute('fieldstatus') === "shipPlaced"){
                    cell.firstChild.style.backgroundColor = "#0000cd";  //placed ship
                } else if(cell.firstChild.getAttribute('fieldstatus') === "empty"){
                    cell.firstChild.style.backgroundColor = "#add8e6";  //default color
                    cell.firstChild.disabled=true;
                }
            })
        })
}

function disableFieldsAroundPlacedShip(){
    var placedShips = document.querySelectorAll(".my-btn[fieldstatus='shipPlaced']");
    var cells = document.querySelectorAll(".dataCell");

    placedShips.forEach(function(that){
        cells.forEach(function(cell){
            if(cell.firstChild.getAttribute('fieldstatus') === "highlighted"){
                cell.firstChild.setAttribute('fieldstatus', 'empty');
            }

            if(isNeighbor(cell, that) || isNeighborDiagonally(cell, that)){
                cell.firstChild.style.backgroundColor = "#B0B0B0";  //grey - disabled around ship
                cell.firstChild.disabled=true;
                cell.firstChild.style.cursor = "not-allowed";
                cell.firstChild.setAttribute('fieldstatus', 'aroundShip');
            }
            else if(cell.firstChild.getAttribute('fieldstatus') === "empty"){
                cell.firstChild.disabled=false;
            }
        })
    })
}

function disableAllFields(){
    var cells = document.querySelectorAll(".dataCell");

    cells.forEach(function(cell){
        cell.firstChild.disabled=true;
    })
}

function isNeighbor(cell, button){
    if( (isNeighborRight(cell, button) || isNeighborLeft(cell, button) || isNeighborDown(cell, button) || isNeighborUp(cell, button))
        && cell.firstChild.getAttribute('fieldstatus') === "empty"
        ) return true;
        else return false;
}
function isNeighborDiagonally(cell, button){
    if( (isNeighborUpRight(cell, button) || isNeighborUpLeft(cell, button) || isNeighborDownRight(cell, button) || isNeighborDownLeft(cell, button))
        && cell.firstChild.getAttribute('fieldstatus') === "empty"
        ) return true;
        else return false;
}

function isNeighborRight(cell, button){
    return parseInt(button.id.substring(0,1)) + 1 == parseInt(cell.firstChild.id.substring(0,1))
        && parseInt(button.id.substring(1,2)) == parseInt(cell.firstChild.id.substring(1,2))
        && parseInt(button.id.substring(0,1)) + 1 <= 9
}
function isNeighborLeft(cell, button){
    return parseInt(button.id.substring(0,1)) - 1 == parseInt(cell.firstChild.id.substring(0,1))
        && parseInt(button.id.substring(1,2)) == parseInt(cell.firstChild.id.substring(1,2))
        && parseInt(button.id.substring(0,1)) - 1 >= 0
}
function isNeighborDown(cell, button){
    return parseInt(button.id.substring(0,1)) == parseInt(cell.firstChild.id.substring(0,1)) &&
        parseInt(button.id.substring(1,2)) + 1 == parseInt(cell.firstChild.id.substring(1,2))
        && parseInt(button.id.substring(1,2)) + 1 <= 9
}
function isNeighborUp(cell, button){
    return parseInt(button.id.substring(0,1)) == parseInt(cell.firstChild.id.substring(0,1)) &&
        parseInt(button.id.substring(1,2)) - 1 == parseInt(cell.firstChild.id.substring(1,2))
        && parseInt(button.id.substring(1,2)) - 1 >= 0
}
function isNeighborUpRight(cell, button){
    return parseInt(button.id.substring(0,1)) - 1 == parseInt(cell.firstChild.id.substring(0,1))
        && parseInt(button.id.substring(1,2)) + 1 == parseInt(cell.firstChild.id.substring(1,2))
        && parseInt(button.id.substring(0,1)) - 1 >= 0
        && parseInt(button.id.substring(1,2)) + 1 <= 9
}
function isNeighborUpLeft(cell, button){
    return parseInt(button.id.substring(0,1)) - 1 == parseInt(cell.firstChild.id.substring(0,1))
        && parseInt(button.id.substring(1,2)) - 1 == parseInt(cell.firstChild.id.substring(1,2))
        && parseInt(button.id.substring(0,1)) - 1 >= 0
        && parseInt(button.id.substring(1,2)) - 1 >= 0
}
function isNeighborDownRight(cell, button){
    return parseInt(button.id.substring(0,1)) + 1 == parseInt(cell.firstChild.id.substring(0,1)) &&
        parseInt(button.id.substring(1,2)) + 1 == parseInt(cell.firstChild.id.substring(1,2))
        && parseInt(button.id.substring(0,1)) + 1 <= 9
        && parseInt(button.id.substring(1,2)) + 1 <= 9
}
function isNeighborDownLeft(cell, button){
    return parseInt(button.id.substring(0,1)) + 1 == parseInt(cell.firstChild.id.substring(0,1)) &&
        parseInt(button.id.substring(1,2)) - 1 == parseInt(cell.firstChild.id.substring(1,2))
        && parseInt(button.id.substring(0,1)) + 1 <= 9
        && parseInt(button.id.substring(1,2)) - 1 >= 0
}