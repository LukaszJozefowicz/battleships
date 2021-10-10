/* vars from th:inline script tag in boards.html

      var isGameVsPC = opponentName.includes("BotEasy") || opponentName.includes("BotNormal") || opponentName.includes("BotHard");
      var boardPC = isGameVsPC ? [[${opponentBoard}]] : null;

*/

class GameVsPcUtils {
    static setComputerPlayerShips(){
        for(let i = 0; i < boardPC.length; i++) {

           for(let j = 0; j < boardPC[i].length; j++) {

              console.log("coords: " + i + j + "name: " + boardPC[i][j]);
              if(boardPC[i][j] === "AROUND_PLACED_SHIP"){
                    let cell = document.querySelector("#" + CSS.escape(''+i+j) + "opp");
                    cell.style.backgroundColor = "#B0B0B0";
              }
              if(boardPC[i][j] === "SHIP_ALIVE"){
                    let cell = document.querySelector("#" + CSS.escape(''+i+j) + "opp");
                    //cell.setAttribute('fieldstatus', 'shipPlaced');
                    cell.style.backgroundColor = "#0000cd";
              }
           }
        }
        document.getElementById("opponentShipPlacingInfo").innerHTML = "Computer player is ready";
    }

    static setClickActionComputerShoot(){
        let opponentCells = document.querySelectorAll(".opp-btn");

        opponentCells.forEach(cell => {
                let row = cell.id.substring(0, 1);
                let col = cell.id.substring(1, 2);
                let onclickValue = "sendShotInfoPC(" + row + ", " + col + ");";
                cell.setAttribute('onclick', onclickValue);
          });
    }
}