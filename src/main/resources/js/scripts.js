function setShip(that){
        that.style.backgroundColor = "#0000cd";
        that.disabled=true;
        that.
        highlightNeighbors(that);
}

function highlightNeighbors(that){
    var cells = document.querySelectorAll(".dataCell");

    cells.forEach(function(cell){
        console.log(parseInt(cell.firstChild.id.substring(0,1)));
        console.log(parseInt(that.id.substring(0,1)));
        if(isNeighbor(cell, that)){
            cell.firstChild.style.backgroundColor = "#6495ed";
        }
    })
}

function isNeighbor(cell, button){
    if((parseInt(button.id.substring(0,1)) + 1 == parseInt(cell.firstChild.id.substring(0,1)) &&
        parseInt(button.id.substring(1,2)) == parseInt(cell.firstChild.id.substring(1,2))
        && parseInt(button.id.substring(0,1)) < 9
        && cell.firstChild.style.backgroundColor !== "#0000cd")

        || (parseInt(button.id.substring(0,1)) - 1 == parseInt(cell.firstChild.id.substring(0,1)) &&
        parseInt(button.id.substring(1,2)) == parseInt(cell.firstChild.id.substring(1,2))
        && parseInt(button.id.substring(0,1)) > 0
        && cell.firstChild.style.backgroundColor !== "#0000cd")

        || (parseInt(button.id.substring(0,1)) == parseInt(cell.firstChild.id.substring(0,1)) &&
        parseInt(button.id.substring(1,2)) + 1 == parseInt(cell.firstChild.id.substring(1,2))
        && parseInt(button.id.substring(1,2)) < 9
        && cell.firstChild.style.backgroundColor !== "#0000cd")

        || (parseInt(button.id.substring(0,1)) == parseInt(cell.firstChild.id.substring(0,1)) &&
        parseInt(button.id.substring(1,2)) - 1 == parseInt(cell.firstChild.id.substring(1,2))
        && parseInt(button.id.substring(1,2)) > 0
        && cell.firstChild.style.backgroundColor !== "#0000cd")

        ) return true;
        else return false;
}