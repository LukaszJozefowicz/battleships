/* vars from th:inline script tag in boards.html

        var shipsToPlace = JSON.parse([[${shipsToPlace}]]);
        var opponentName = [[${opponentName}]];
        var isGameVsPC = opponentName.includes("BotEasy") || opponentName.includes("BotNormal") || opponentName.includes("BotHard");

*/

class ShipsSetupUtils {

    static resetBoard(){
        this.whichShip = 0;
        this.whichFieldOfShip  = 1;
        var cells = document.querySelectorAll("#boardTable .my-btn");

        cells.forEach(cell => {
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

    static setShipPlacementInfo(){
            var currentShipType = shipsToPlace[this.whichShip].type + shipsToPlace[this.whichShip].whichOfAKind;
            var currentShipLength = shipsToPlace[this.whichShip].length;

            this.whichFieldOfShip++;

            this.setPlacementInfoText(this.whichShip);

            if(this.whichShip === shipsToPlace.length - 1 && this.whichFieldOfShip === currentShipLength + 1){
                document.getElementById("shipPlacingInfo").className = "red";
                document.getElementById("shipPlacingInfo").innerHTML = "Your board is ready";

                sendPlacementInfoToOpponent(shipsToPlace[this.whichShip].type, shipsToPlace[this.whichShip].whichOfAKind, "true");
                this.areYouReady = true;
                this.whichFieldOfShip = 1;
                this.whichShip++;

            } else if(this.whichFieldOfShip === currentShipLength + 1){
                this.whichFieldOfShip = 1;
                this.setPlacementInfoText(this.whichShip + 1);
                this.whichShip++;
            }
    }

    static setPlacementInfoText(shipCount){
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

    static setOpponentPlacementInfo(payloadBody){
        let oppPlacementInfo = document.getElementById("opponentShipPlacingInfo");

        if(payloadBody.isAllShipsPlaced === "false"){
            oppPlacementInfo.innerHTML = "Opponent is placing " + payloadBody.shipName + payloadBody.whichOfAKind;
        } else if(payloadBody.isAllShipsPlaced === "true"){
            oppPlacementInfo.innerHTML = "Opponent has finished placing his ships.";
            if(this.areYouReady){
                ShotUtils.startShootingPhase();
            }
        this.isOpponentReady = true;
        }

    }

    static setShip(clickedCell){

            this.setShipPlacementInfo();
            clickedCell.style.backgroundColor = "#0000cd"; //ship placed color
            clickedCell.disabled=true;
            clickedCell.setAttribute('fieldstatus', 'shipPlaced');

            this.highlightNeighbors();

            if(this.whichFieldOfShip === 1){
                this.disableFieldsAroundPlacedShip();
            }

            if(this.whichShip === shipsToPlace.length){
                this.disableAllCells();
                this.setUpForGameStart();
                if(this.isOpponentReady || isGameVsPC)
                    ShotUtils.startShootingPhase();
            }
    }

    static highlightNeighbors(){
        var cells = document.querySelectorAll("#boardTable .my-btn");
        var placedShips = document.querySelectorAll(".my-btn[fieldstatus='shipPlaced']");

            placedShips.forEach(shipCell => {
                cells.forEach(cell => {
                    if(this.isNeighbor(cell, shipCell) && cell.getAttribute('fieldstatus') === "empty" && this.whichFieldOfShip != 1){
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

    static disableFieldsAroundPlacedShip(){
        var placedShips = document.querySelectorAll(".my-btn[fieldstatus='shipPlaced']");
        var cells = document.querySelectorAll("#boardTable .my-btn");

        placedShips.forEach(shipCell => {
            cells.forEach(cell => {
                if(cell.getAttribute('fieldstatus') === "highlighted"){
                    cell.setAttribute('fieldstatus', 'empty');
                }

                if(this.isNeighbor(cell, shipCell) || this.isNeighborDiagonally(cell, shipCell)){
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

    static disableAllCells(){
        var cells = document.querySelectorAll("#boardTable .my-btn");

        cells.forEach(cell => {
            cell.disabled=true;
        })
    }

    static setUpForGameStart(){
        var cells = document.querySelectorAll("#boardTable .my-btn");

        cells.forEach(cell => {
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

    static isNeighbor(cell, button){
        if( (this.isNeighborRight(cell, button)
            || this.isNeighborLeft(cell, button)
            || this.isNeighborDown(cell, button)
            || this.isNeighborUp(cell, button))
            && cell.getAttribute('fieldstatus') === "empty")
                return true;
            else return false;
    }

    static isNeighborDiagonally(cell, button){
        if( (this.isNeighborUpRight(cell, button)
            || this.isNeighborUpLeft(cell, button)
            || this.isNeighborDownRight(cell, button)
            || this.isNeighborDownLeft(cell, button))
            && cell.getAttribute('fieldstatus') === "empty")
                return true;
            else return false;
    }

    static isNeighborRight(cell, button){
        return parseInt(button.id.substring(0,1)) + 1 == parseInt(cell.id.substring(0,1))
            && parseInt(button.id.substring(1,2)) == parseInt(cell.id.substring(1,2))
            && parseInt(button.id.substring(0,1)) + 1 <= 9
    }
    static isNeighborLeft(cell, button){
        return parseInt(button.id.substring(0,1)) - 1 == parseInt(cell.id.substring(0,1))
            && parseInt(button.id.substring(1,2)) == parseInt(cell.id.substring(1,2))
            && parseInt(button.id.substring(0,1)) - 1 >= 0
    }
    static isNeighborDown(cell, button){
        return parseInt(button.id.substring(0,1)) == parseInt(cell.id.substring(0,1))
            && parseInt(button.id.substring(1,2)) + 1 == parseInt(cell.id.substring(1,2))
            && parseInt(button.id.substring(1,2)) + 1 <= 9
    }
    static isNeighborUp(cell, button){
        return parseInt(button.id.substring(0,1)) == parseInt(cell.id.substring(0,1))
            && parseInt(button.id.substring(1,2)) - 1 == parseInt(cell.id.substring(1,2))
            && parseInt(button.id.substring(1,2)) - 1 >= 0
    }
    static isNeighborUpRight(cell, button){
        return parseInt(button.id.substring(0,1)) - 1 == parseInt(cell.id.substring(0,1))
            && parseInt(button.id.substring(1,2)) + 1 == parseInt(cell.id.substring(1,2))
            && parseInt(button.id.substring(0,1)) - 1 >= 0
            && parseInt(button.id.substring(1,2)) + 1 <= 9
    }
    static isNeighborUpLeft(cell, button){
        return parseInt(button.id.substring(0,1)) - 1 == parseInt(cell.id.substring(0,1))
            && parseInt(button.id.substring(1,2)) - 1 == parseInt(cell.id.substring(1,2))
            && parseInt(button.id.substring(0,1)) - 1 >= 0
            && parseInt(button.id.substring(1,2)) - 1 >= 0
    }
    static isNeighborDownRight(cell, button){
        return parseInt(button.id.substring(0,1)) + 1 == parseInt(cell.id.substring(0,1)) &&
            parseInt(button.id.substring(1,2)) + 1 == parseInt(cell.id.substring(1,2))
            && parseInt(button.id.substring(0,1)) + 1 <= 9
            && parseInt(button.id.substring(1,2)) + 1 <= 9
    }
    static isNeighborDownLeft(cell, button){
        return parseInt(button.id.substring(0,1)) + 1 == parseInt(cell.id.substring(0,1)) &&
            parseInt(button.id.substring(1,2)) - 1 == parseInt(cell.id.substring(1,2))
            && parseInt(button.id.substring(0,1)) + 1 <= 9
            && parseInt(button.id.substring(1,2)) - 1 >= 0
    }

    static setAllShipsRandomly(){
                document.getElementById("reset").disabled = true;
                document.getElementById("randomPlacement").disabled = true;

                var cells = document.querySelectorAll("#boardTable .my-btn");
                var activeCells = [];

                cells.forEach(cell => {
                    if(cell.disabled === false){
                        activeCells.push(cell);
                    }
                });

                if(activeCells.length === 0){
                    this.resetBoard();
                    ShipsSetupUtils.setAllShipsRandomly();
                }

                var randomCell = activeCells[Math.floor(Math.random() * activeCells.length)];
                let col = parseInt(randomCell.id.substring(0,1));
                let row = parseInt(randomCell.id.substring(1,2));
                sendPlaceShipTile(col, row);
                ShipsSetupUtils.setShip(randomCell);
                if(ShipsSetupUtils.whichShip < shipsToPlace.length){
                    setTimeout(ShipsSetupUtils.setAllShipsRandomly, 500);
                }
    }
}
ShipsSetupUtils.areYouReady = false;
ShipsSetupUtils.isOpponentReady = false;
ShipsSetupUtils.whichShip = 0;
ShipsSetupUtils.whichFieldOfShip  = 1;