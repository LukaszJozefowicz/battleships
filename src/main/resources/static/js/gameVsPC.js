function setComputerPlayerShips(){
    for(let i = 0; i < boardPC.length; i++) {

       for(let j = 0; j < boardPC[i].length; j++) {

          console.log("coords: " + i + j + "name: " + boardPC[i][j]);
          if(boardPC[i][j] === "AROUND_PLACED_SHIP"){
                let cell = document.querySelector("#" + CSS.escape(''+i+j) + "opp");
                cell.style.backgroundColor = "#B0B0B0";
          }
          if(boardPC[i][j] === "SHIP_ALIVE"){
                let cell = document.querySelector("#" + CSS.escape(''+i+j) + "opp");
                cell.setAttribute('fieldstatus', 'shipPlaced');
                cell.style.backgroundColor = "#0000cd";
          }
       }
    }
}